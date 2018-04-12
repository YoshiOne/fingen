package com.yoshione.fingen;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.DragEvent;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.TransactionDetails;
import com.crashlytics.android.Crashlytics;
import com.mikepenz.actionitembadge.library.ActionItemBadge;
import com.mikepenz.actionitembadge.library.utils.BadgeStyle;
import com.mikepenz.actionitembadge.library.utils.NumberUtils;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.yoshione.fingen.backup.BackupJob;
import com.yoshione.fingen.dao.SendersDAO;
import com.yoshione.fingen.dao.SmsDAO;
import com.yoshione.fingen.dao.SmsMarkersDAO;
import com.yoshione.fingen.dao.TransactionsDAO;
import com.yoshione.fingen.filters.AbstractFilter;
import com.yoshione.fingen.filters.AccountFilter;
import com.yoshione.fingen.fts.ActivityScanQR;
import com.yoshione.fingen.interfaces.IAbstractModel;
import com.yoshione.fingen.interfaces.IUpdateCallback;
import com.yoshione.fingen.managers.AccountsSetManager;
import com.yoshione.fingen.managers.TransactionManager;
import com.yoshione.fingen.model.Account;
import com.yoshione.fingen.model.AccountsSet;
import com.yoshione.fingen.model.BaseModel;
import com.yoshione.fingen.model.Events;
import com.yoshione.fingen.model.Sender;
import com.yoshione.fingen.model.SmsMarker;
import com.yoshione.fingen.model.Template;
import com.yoshione.fingen.model.Transaction;
import com.yoshione.fingen.receivers.SMSReceiver;
import com.yoshione.fingen.utils.ColorUtils;
import com.yoshione.fingen.utils.InApp;
import com.yoshione.fingen.utils.Lg;
import com.yoshione.fingen.utils.NotificationCounter;
import com.yoshione.fingen.utils.NotificationHelper;
import com.yoshione.fingen.utils.PrefUtils;
import com.yoshione.fingen.utils.RequestCodes;
import com.yoshione.fingen.widgets.ToolbarActivity;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.lang.ref.WeakReference;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.fabric.sdk.android.Fabric;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;

@RuntimePermissions
public class ActivityMain extends ToolbarActivity implements BillingProcessor.IBillingHandler {

    /*Sample sms
        VISA1234 01.01.16 12:00 покупка 106.40р SUPERMARKET Баланс: 6623.34р
    */

    public static final String TAG = "ActivityMain";
    public static final int ACTION_OPEN_TRANSACTIONS_LIST = 1;
    public static final int THEME_LIGHT = 0;
    public static final int THEME_DARK = 1;

    private static final int MSG_UPDATE_LISTS = 1;
    private static final int MSG_UPDATE_SMS_COUNTERS = 3;

    private static final int ACCOUNT_CLICK_ACTION_LIST_TRANSACTIONS = 0;
    private static final int ACCOUNT_CLICK_ACTION_NEW_TRANSACTION = 1;

    private static final int DRAWER_ITEM_ID_DEBTS = 1;
    private static final int DRAWER_ITEM_ID_REFERENCES = 3;
    private static final int DRAWER_ITEM_ID_BUDGETS = 5;
    private static final int DRAWER_ITEM_ID_INCOMING_SMS = 6;
    private static final int DRAWER_ITEM_ID_ADDITIONAL = 7;
    private static final int DRAWER_ITEM_ID_SETTINGS = 8;
    private static final int DRAWER_ITEM_ID_SUPPORT = 9;
    private static final int DRAWER_ITEM_ID_HELP = 11;
    private static final int DRAWER_ITEM_ID_ABOUT = 10;
    private static final int DRAWER_ITEM_ID_PRO = 12;
    private final List<Fragment> fragments = new ArrayList<>();
    private final SharedPreferences.OnSharedPreferenceChangeListener sharedPreferenceChangeListener =
            new SharedPreferences.OnSharedPreferenceChangeListener() {
                @Override
                public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
                    if (key.equals("theme")) {
                        ActivityMain.this.recreate(); // the function you want called
                    }
                }
            };
    @BindView(R.id.pager)
    ViewPager viewPager;
    @BindView(R.id.tabLayout)
    TabLayout tabLayout;
    @Nullable
    @BindView(R.id.sliding_layout_transactions)
    SlidingUpPanelLayout mSlidingUpTransactions;
    @Nullable
    @BindView(R.id.sliding_layout_accounts)
    SlidingUpPanelLayout mSlidingUpAccounts;
    @BindView(R.id.coordinatorLayout)
    CoordinatorLayout coordinatorLayout;
    @BindView(R.id.appbar)
    AppBarLayout mAppBarLayout;
    boolean mReportsPurchased = false;
    @BindView(R.id.textViewSubtitle)
    TextView mTextViewActiveSet;
    @BindView(R.id.buttonTemplates)
    Button mButtonTemplates;
    @BindView(R.id.buttonScanQR)
    Button mButtonScanQR;
    @BindView(R.id.buttonNewExpense)
    Button mButtonNewExpense;
    @BindView(R.id.buttonNewIncome)
    Button mButtonNewIncome;
    @BindView(R.id.buttonNewTransfer)
    Button mButtonNewTransfer;
    @BindView(R.id.buttonsBar)
    LinearLayout mButtonsBar;
    @BindView(R.id.buttonsBarContainer)
    FrameLayout mButtonsBarContainer;
    private SharedPreferences mySharedPreferences;
    private FragmentAccounts fragmentAccounts;
    FragmentTransactions fragmentTransactions;
    private FragmentSummary fragmentSummary;
    private Drawer mMaterialDrawer = null;
    private BillingProcessor mBillingProcessor = null;
    //    private UpdateInAppHandler mInAppHandler;
    FragmentStatePagerAdapter fragmentPagerAdapter;

    private Long lastEventTime = 0L;
    //    private final List<Events.EventOnModelChanged> eventsQueue = new ArrayList<>();
    private Boolean waitForQueue = false;
    UpdateUIHandler mUpdateUIHandler;
    private int mUnreadSms = 0;

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_main;
    }

    @Override
    protected String getLayoutTitle() {
        return getString(R.string.app_name);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getIntent().putExtra("showHomeButton", false);
        mUpdateUIHandler = new UpdateUIHandler(this);

        switch (Integer.valueOf(PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("theme", "0"))) {
            case THEME_LIGHT:
                setTheme(R.style.AppThemeLight);
                break;
            case THEME_DARK:
                setTheme(R.style.AppThemeDark);
                break;
            default:
                setTheme(R.style.AppThemeLight);
                break;
        }

//        if (BuildConfig.DEBUG) {
//            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
//                    .detectLeakedSqlLiteObjects()
//                    .detectLeakedClosableObjects()
//                    .penaltyLog()
//                    .penaltyDeath()
//                    .build());
//        }

        super.onCreate(null);
        TransactionsDAO transactionsDAO = TransactionsDAO.getInstance(this);
        if (!BuildConfig.DEBUG) {
            Fabric.with(this, new Crashlytics());
        }

//        mInAppHandler = new UpdateInAppHandler();

        mySharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mySharedPreferences.edit().putBoolean(FgConst.PREF_SWITCH_TAB_ON_START, true).apply();

        BackupJob.schedule();

        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }

        mTextViewActiveSet.setBackgroundColor(ColorUtils.getBackgroundColor(this));

        buildDrawer();

        ImageButton drawerToggle = getNavButtonView(toolbar);
        if (drawerToggle != null) {
            drawerToggle.setImageDrawable(getDrawable(R.drawable.ic_menu_white));
        }
        addFragments();

        setupBottomBar();

        //<editor-fold desc="Setup fragments">
        fragmentPagerAdapter = new FgFragmentPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(fragmentPagerAdapter);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.setOnDragListener(new View.OnDragListener() {
            @Override
            public boolean onDrag(View view, DragEvent dragEvent) {
                return true;
            }
        });
        viewPager.setOffscreenPageLimit(2);
        //</editor-fold>


        //<editor-fold desc="Check version and show changelog if necessary">
        PackageInfo pInfo;
        int version;
        try {
            pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            version = pInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            version = -1;
        }
        if (version > 0) {
            int prevVersion = mySharedPreferences.getInt("version_code", -1);
            if (version != prevVersion) {
                onUpdateVersion(prevVersion, version);
            }
        }

        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(sharedPreferenceChangeListener);
        //</editor-fold>

        mBillingProcessor = new BillingProcessor(this, InApp.getDeveloperKey(), null, this);

        final View rootView = getWindow().getDecorView().getRootView();
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Rect rect = new Rect();
                rootView.getWindowVisibleDisplayFrame(rect);

                int screenHeight = rootView.getHeight();
                int keyboardHeight = screenHeight - (rect.bottom - rect.top);
                if (keyboardHeight > screenHeight / 3) {
                    mButtonsBarContainer.setVisibility(View.GONE);
                } else {
                    mButtonsBarContainer.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem menuItemInbox = menu.findItem(R.id.action_incoming);
        Drawable icon = ContextCompat.getDrawable(this, R.drawable.ic_sms_white);
        int badgeColor = ContextCompat.getColor(this, R.color.negative_color);
        BadgeStyle badgeStyle = new BadgeStyle(BadgeStyle.Style.DEFAULT, com.mikepenz.actionitembadge.library.R.layout.menu_action_item_badge, badgeColor, badgeColor, -1);
        if (mUnreadSms > 0) {
            ActionItemBadge.update(this, menuItemInbox, icon, badgeStyle, NumberUtils.formatNumber(mUnreadSms));
        } else {
            ActionItemBadge.hide(menu.findItem(R.id.action_incoming));
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_incoming) {
            Intent intent = new Intent(ActivityMain.this, ActivitySmsList.class);
            ActivityMain.this.startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void onUpdateVersion(int prevVersion, int newVersion) {
        fragmentTransactions.isNewVersion = true;
        mySharedPreferences.edit().putInt("version_code", newVersion).apply();
//        mySharedPreferences.edit().putBoolean(FgConst.PREF_SHOW_ACCOUNTS_PANEL, true).apply();

        if (prevVersion < 59) {
            try {
                int startTabInt = Integer.valueOf(mySharedPreferences.getString(FgConst.PREF_START_TAB, "0"));
                String startTabString;
                switch (startTabInt) {
                    case 0:
                        startTabString = FgConst.FRAGMENT_SUMMARY;
                        break;
                    case 1:
                        startTabString = FgConst.FRAGMENT_ACCOUNTS;
                        break;
                    case 2:
                        startTabString = FgConst.FRAGMENT_TRANSACTIONS;
                        break;
                    default:
                        startTabString = FgConst.FRAGMENT_SUMMARY;
                }
                mySharedPreferences.edit().putString(FgConst.PREF_START_TAB, startTabString).apply();
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }

        FragmentChangelog fragmentChangelog = new FragmentChangelog();
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        Fragment prev = fm.findFragmentByTag("changelogdemo_dialog");
        if (prev != null) {
            ft.remove(prev);
        }
        fragmentChangelog.show(ft, "changelogdemo_dialog");
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    private class FgFragmentPagerAdapter extends FragmentStatePagerAdapter {

        FgFragmentPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        @Override
        public Fragment getItem(final int position) {
            return fragments.get(position);
        }

        @Override
        public CharSequence getPageTitle(final int position) {

            if (fragments.get(position).getClass().equals(FragmentAccounts.class)) {
                return getString(R.string.ent_accounts).toUpperCase();
            } else if (fragments.get(position).getClass().equals(FragmentTransactions.class)) {
                return getString(R.string.ent_transactions).toUpperCase();
            } else if (fragments.get(position).getClass().equals(FragmentSummary.class)) {
                return getString(R.string.ent_summary).toUpperCase();
            } else {
                return null;
            }
        }

        @Override
        public Parcelable saveState() {
            // Do Nothing
            return null;
        }

        @Override
        public int getItemPosition(@NonNull Object object) {
            return POSITION_NONE;
        }
    }


    private void addFragments() {
        fragmentAccounts = FragmentAccounts.newInstance(FgConst.PREF_FORCE_UPDATE_ACCOUNTS, R.layout.fragment_accounts, new IUpdateCallback() {
            @Override
            public void update() {
                updateLists();
            }
        });
        fragmentAccounts.setmAccountEventListener(new FragmentAccounts.AccountEventListener() {
            @Override
            public void OnItemClick(Account account) {
                int action = Integer.valueOf(mySharedPreferences.getString(FgConst.PREF_ACCOUNT_CLICK_ACTION, String.valueOf(ACCOUNT_CLICK_ACTION_LIST_TRANSACTIONS)));
                switch (action) {
                    case ACCOUNT_CLICK_ACTION_LIST_TRANSACTIONS:
                        AccountFilter filter = new AccountFilter(0);
                        filter.addAccount(account.getID());
                        ArrayList<AbstractFilter> filters = new ArrayList<>();
                        filters.add(filter);
                        Intent intent = new Intent(ActivityMain.this, ActivityTransactions.class);
                        intent.putParcelableArrayListExtra("filter_list", filters);
                        intent.putExtra("caption", account.getName());
                        intent.putExtra(FgConst.HIDE_FAB, true);
                        intent.putExtra(FgConst.LOCK_SLIDINGUP_PANEL, true);
                        startActivity(intent);
                        break;
                    case ACCOUNT_CLICK_ACTION_NEW_TRANSACTION:
                        Transaction transaction = new Transaction(PrefUtils.getDefDepID(ActivityMain.this));
                        transaction.setAccountID(account.getID());
                        Intent intentNT = new Intent(ActivityMain.this, ActivityEditTransaction.class);
                        intentNT.putExtra("transaction", transaction);
                        startActivity(intentNT);
                        break;
                }

            }
        });
        fragmentSummary = FragmentSummary.newInstance(FgConst.PREF_FORCE_UPDATE_SUMMARY, R.layout.fragment_summary);
        fragmentTransactions = FragmentTransactions.newInstance(FgConst.PREF_FORCE_UPDATE_TRANSACTIONS, R.layout.fragment_transactions);

        List<String> tabs = PrefUtils.getTabsOrder(mySharedPreferences);

        fragments.clear();
        for (String tabID : tabs) {
            switch (tabID) {
                case FgConst.FRAGMENT_SUMMARY:
                    fragments.add(fragmentSummary);
                    break;
                case FgConst.FRAGMENT_ACCOUNTS:
                    fragments.add(fragmentAccounts);
                    break;
                case FgConst.FRAGMENT_TRANSACTIONS:
                    fragments.add(fragmentTransactions);
                    break;
            }
        }
    }

    @Override
    public void onDestroy() {
//        Debug.stopMethodTracing();
        if (mBillingProcessor != null)
            mBillingProcessor.release();
        PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(sharedPreferenceChangeListener);
        super.onDestroy();
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);

        if (getIntent().getIntExtra("action", 0) == ACTION_OPEN_TRANSACTIONS_LIST) {
            String startTab = FgConst.FRAGMENT_TRANSACTIONS;
            List<String> tabsOrder = PrefUtils.getTabsOrder(mySharedPreferences);
            int currentItem = tabsOrder.indexOf(startTab);
            if (currentItem >= 0 && currentItem < fragments.size()) {
                viewPager.setCurrentItem(currentItem);
            }
        } else {
            if (mySharedPreferences.getBoolean(FgConst.PREF_SWITCH_TAB_ON_START, false)) {
                String startTab = mySharedPreferences.getString(FgConst.PREF_START_TAB, FgConst.FRAGMENT_SUMMARY);
                List<String> tabsOrder = PrefUtils.getTabsOrder(mySharedPreferences);
                int currentItem = tabsOrder.indexOf(startTab);
                if (currentItem >= 0 && currentItem < fragments.size()) {
                    viewPager.setCurrentItem(currentItem);
                }
                mySharedPreferences.edit().putBoolean(FgConst.PREF_SWITCH_TAB_ON_START, false).apply();
            }
        }

        checkPermissionsAndShowAlert();

        List<String> tabs = PrefUtils.getTabsOrder(mySharedPreferences);
        boolean actual = true;
        if (fragments.size() == tabs.size()) {
            for (int i = 0; i < fragments.size(); i++) {
                actual = actual & getFragmentID(fragments.get(i)).equals(tabs.get(i));
            }
        }
        if (!actual) {
            addFragments();
            fragmentPagerAdapter.notifyDataSetChanged();
//            updateLists();
        }
    }

    private String getFragmentID(Fragment fragment) {
        if (fragment.getClass().equals(FragmentAccounts.class)) {
            return FgConst.FRAGMENT_ACCOUNTS;
        } else if (fragment.getClass().equals(FragmentTransactions.class)) {
            return FgConst.FRAGMENT_TRANSACTIONS;
        } else if (fragment.getClass().equals(FragmentSummary.class)) {
            return FgConst.FRAGMENT_SUMMARY;
        } else {
            return "";
        }
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    private Drawable getIcon(int id) {
        return ContextCompat.getDrawable(this, id);
    }

    private void buildDrawer() {
        AccountHeader headerResult;
        headerResult = new AccountHeaderBuilder()
                .withHeightDp(16)
                .withActivity(this)
//                .withHeaderBackground(R.drawable.header)
                .withCompactStyle(true)
                .build();


        mMaterialDrawer = new DrawerBuilder()
                .withActivity(this)
                .withToolbar(toolbar)
                .withTranslucentStatusBar(true)
                .withAccountHeader(headerResult)
                .withSelectedItem(-1)
                .addDrawerItems(
                        new PrimaryDrawerItem().withName(R.string.ent_debts).withIcon(getIcon(R.drawable.ic_drawer_debts)).withIdentifier(DRAWER_ITEM_ID_DEBTS),
                        new PrimaryDrawerItem().withName(R.string.ent_budget).withIcon(getIcon(R.drawable.ic_drawer_budget)).withIdentifier(DRAWER_ITEM_ID_BUDGETS),
                        new PrimaryDrawerItem().withName(R.string.ent_references).withIcon(getIcon(R.drawable.ic_drawer_references)).withIdentifier(DRAWER_ITEM_ID_REFERENCES),
                        new PrimaryDrawerItem().withName(R.string.ent_incoming).withIcon(getIcon(R.drawable.ic_drawer_inbox)).withIdentifier(DRAWER_ITEM_ID_INCOMING_SMS),
                        new PrimaryDrawerItem().withName(R.string.ent_additional).withIcon(getIcon(R.drawable.ic_drawer_additional)).withIdentifier(DRAWER_ITEM_ID_ADDITIONAL),
                        new DividerDrawerItem(),
                        new PrimaryDrawerItem().withName(R.string.ent_pro_features).withIcon(getIcon(R.drawable.ic_drawer_pro))
                                .withIconColor(ContextCompat.getColor(this, R.color.ColorMain)).withIdentifier(DRAWER_ITEM_ID_PRO),
                        new PrimaryDrawerItem().withName(R.string.ent_settings).withIcon(getIcon(R.drawable.ic_drawer_settings)).withIdentifier(DRAWER_ITEM_ID_SETTINGS),
                        new PrimaryDrawerItem().withName(R.string.ent_help).withIcon(getIcon(R.drawable.ic_drawer_help)).withIdentifier(DRAWER_ITEM_ID_HELP),
//                        new PrimaryDrawerItem().withName(R.string.act_ask_question).withIcon(getIcon(R.drawable.ic_drawer_support)).withIdentifier(DRAWER_ITEM_ID_SUPPORT),
                        new PrimaryDrawerItem().withName(R.string.ent_about).withIcon(getIcon(R.drawable.ic_drawer_about)).withIdentifier(DRAWER_ITEM_ID_ABOUT)
                )
                .withOnDrawerListener(new Drawer.OnDrawerListener() {
                    @Override
                    public void onDrawerOpened(View drawerView) {
                        // Скрываем клавиатуру при открытии Navigation Drawer
                        InputMethodManager inputMethodManager = (InputMethodManager) ActivityMain.this.getSystemService(Activity.INPUT_METHOD_SERVICE);
                        if (ActivityMain.this.getCurrentFocus() != null & inputMethodManager != null) {
                            inputMethodManager.hideSoftInputFromWindow(ActivityMain.this.getCurrentFocus().getWindowToken(), 0);
                        }
                    }

                    @Override
                    public void onDrawerClosed(View drawerView) {
                    }

                    @Override
                    public void onDrawerSlide(View view, float v) {

                    }
                })
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        if (drawerItem instanceof PrimaryDrawerItem) {
                            Intent intent;
                            switch ((int) drawerItem.getIdentifier()) {
                                case DRAWER_ITEM_ID_DEBTS:
                                    intent = new Intent(ActivityMain.this, ActivityDebtsAndCredits.class);
                                    ActivityMain.this.startActivity(intent);
                                    break;
                                case DRAWER_ITEM_ID_REFERENCES:
                                    ActivityMain.this.openReferences();
                                    break;
                                case DRAWER_ITEM_ID_INCOMING_SMS: {
                                    intent = new Intent(ActivityMain.this, ActivitySmsList.class);
                                    ActivityMain.this.startActivity(intent);
                                    break;
                                }
                                case DRAWER_ITEM_ID_BUDGETS: {
                                    intent = new Intent(ActivityMain.this, ActivityBudget.class);
                                    ActivityMain.this.startActivity(intent);
                                    break;
                                }
                                case DRAWER_ITEM_ID_ADDITIONAL: {
                                    intent = new Intent(ActivityMain.this, ActivityAdditional.class);
                                    ActivityMain.this.startActivity(intent);
                                    break;
                                }
                                case DRAWER_ITEM_ID_PRO: {
                                    intent = new Intent(ActivityMain.this, ActivityPro.class);
                                    startActivityForResult(intent, RequestCodes.REQUEST_CODE_OPEN_PRO);
                                    break;
                                }
                                case DRAWER_ITEM_ID_SETTINGS: {
                                    intent = new Intent(ActivityMain.this, ActivitySettings.class);
                                    ActivityMain.this.startActivityForResult(intent, RequestCodes.REQUEST_CODE_OPEN_PREFERENCES);
                                    break;
                                }
//                                case DRAWER_ITEM_ID_HELP: {
//                                    break;
//                                }
//                                case DRAWER_ITEM_ID_SUPPORT: {
//                                    break;
//                                }
                                case DRAWER_ITEM_ID_ABOUT: {
                                    intent = new Intent(ActivityMain.this, ActivityAbout.class);
                                    ActivityMain.this.startActivity(intent);
                                    break;
                                }
                            }
                        }
                        return false;
                    }
                })
                .build();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        InputMethodManager inputMethodManager = (InputMethodManager) ActivityMain.this.getSystemService(Activity.INPUT_METHOD_SERVICE);
        if (ActivityMain.this.getCurrentFocus() != null & inputMethodManager != null) {
            inputMethodManager.hideSoftInputFromWindow(ActivityMain.this.getCurrentFocus().getWindowToken(), 0);
        }
        return true;
    }

    private ImageButton getNavButtonView(Toolbar toolbar) {
        for (int i = 0; i < toolbar.getChildCount(); i++)
            if (toolbar.getChildAt(i) instanceof ImageButton)
                return (ImageButton) toolbar.getChildAt(i);

        return null;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "MainActivity onResume");
        checkInAppPurchases();

        mLastBackPressed = -1;

        NotificationHelper.getInstance(this).cancel(SMSReceiver.NOTIFICATION_ID_TRANSACTION_AUTO_CREATED);
        NotificationCounter notificationCounter = new NotificationCounter(mySharedPreferences);
        notificationCounter.removeNotification(SMSReceiver.NOTIFICATION_ID_TRANSACTION_AUTO_CREATED);

        mMaterialDrawer.deselect();
        updateCounters();

        updateLists();

        if (mySharedPreferences.getBoolean(FgConst.PREF_HIDE_SUMS_PANEL, true)) {
            AppBarLayout.LayoutParams paramsABL = (AppBarLayout.LayoutParams) toolbar.getLayoutParams();
            paramsABL.setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL | AppBarLayout.LayoutParams.SCROLL_FLAG_SNAP);
            toolbar.requestLayout();
        }
    }

    private long mLastBackPressed;

    @Override
    public void onBackPressed() {
        // Закрываем Navigation Drawer по нажатию системной кнопки "Назад" если он открыт
        ButterKnife.bind(this);
        if (mMaterialDrawer.isDrawerOpen()) {
            mMaterialDrawer.closeDrawer();
        } else if (fragments.get(viewPager.getCurrentItem()).getClass().equals(FragmentAccounts.class) & (mSlidingUpAccounts != null &&
                (mSlidingUpAccounts.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED
                        || mSlidingUpAccounts.getPanelState() == SlidingUpPanelLayout.PanelState.ANCHORED))) {
            mSlidingUpAccounts.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        } else if (fragments.get(viewPager.getCurrentItem()).getClass().equals(FragmentTransactions.class) & (mSlidingUpTransactions != null &&
                (mSlidingUpTransactions.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED || mSlidingUpTransactions.getPanelState() == SlidingUpPanelLayout.PanelState.ANCHORED))) {
            mSlidingUpTransactions.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        } else if (fragments.get(viewPager.getCurrentItem()).getClass().equals(FragmentTransactions.class) && fragmentTransactions.fabMenuSelection != null && fragmentTransactions.fabMenuSelection.isOpened()) {
            fragmentTransactions.fabMenuSelection.close(true);
        } else if (fragments.get(viewPager.getCurrentItem()).getClass().equals(FragmentTransactions.class) && fragmentTransactions.mCardViewSearch != null && fragmentTransactions.mCardViewSearch.getVisibility() == View.VISIBLE) {
            fragmentTransactions.hideSearchView();
        } else if (fragmentAccounts != null && fragmentAccounts.adapter != null && fragmentAccounts.adapter.ismDragMode()) {
            fragmentAccounts.adapter.setmDragMode(false);
            fragmentAccounts.adapter.notifyDataSetChanged();
        } else {
            if (mLastBackPressed < 0 || System.currentTimeMillis() - mLastBackPressed > 2_000) {
                mLastBackPressed = System.currentTimeMillis();
                Toast.makeText(this, getString(R.string.msg_press_back_again_to_exit), Toast.LENGTH_SHORT).show();
            } else {
                super.onBackPressed();
                finishAndRemoveTask();
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_MENU:
                mMaterialDrawer.openDrawer();
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    //<editor-fold desc="Получаем права на доступ к external storage" defaultstate="collapsed">
    @NeedsPermission({Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE})
    void getExternalStoragepermission() {
        Log.d(TAG, "getExternalStoragepermission");
    }

    @OnShowRationale({Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE})
    void showRationaleForExStorage(PermissionRequest request) {
        showExStorageRationaleDialog(R.string.msg_permission_rw_external_storage_rationale, request);
    }

    @OnPermissionDenied({Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE})
    void onExStorageDenied() {
        Toast.makeText(this, R.string.msg_permission_rw_external_storage_denied, Toast.LENGTH_SHORT).show();
    }

    @OnNeverAskAgain({Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE})
    void onExStorageNeverAskAgain() {
        Toast.makeText(this, R.string.msg_permission_rw_external_storage_never_askagain, Toast.LENGTH_SHORT).show();
    }

    private void showExStorageRationaleDialog(@StringRes int messageResId, final PermissionRequest request) {
        new AlertDialog.Builder(this)
                .setPositiveButton(R.string.act_next, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        request.proceed();
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        request.cancel();
                    }
                })
                .setCancelable(false)
                .setMessage(messageResId)
                .show();
    }
    //</editor-fold>

    //<editor-fold desc="Получаем права на чтение смс" defaultstate="collapsed">
    @NeedsPermission(Manifest.permission.RECEIVE_SMS)
    void getReceiveSmsPermission() {
        Log.d(TAG, "getReceiveSmsPermission");
    }

    @OnShowRationale(Manifest.permission.RECEIVE_SMS)
    void showRationaleForReceiveSms(PermissionRequest request) {
        showRationaleDialog(R.string.msg_permission_recieve_sms_rationale, request);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        ActivityMainPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    private void showRationaleDialog(@StringRes int messageResId, final PermissionRequest request) {
        new AlertDialog.Builder(this)
                .setPositiveButton(R.string.act_next, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        request.proceed();
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        request.cancel();
                    }
                })
                .setCancelable(false)
                .setMessage(messageResId)
                .show();
    }
    //</editor-fold>

    private void checkPermissionsAndShowAlert() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED) {
            List<SmsMarker> smsMarkers;
            try {
                smsMarkers = SmsMarkersDAO.getInstance(this).getAllSmsParserPatterns();
            } catch (Exception e) {
                smsMarkers = new ArrayList<>();
            }
            List<Sender> senders;
            try {
                senders = SendersDAO.getInstance(this).getAllSenders();
            } catch (Exception e) {
                senders = new ArrayList<>();
            }
            if (smsMarkers.size() > 0 | senders.size() > 0) {
                ActivityMainPermissionsDispatcher.getReceiveSmsPermissionWithPermissionCheck(this);
            }
        }
        ActivityMainPermissionsDispatcher.getExternalStoragepermissionWithPermissionCheck(this);
    }

    private void openReferences() {
        Intent intent = new Intent(this, ActivityReferences.class);
        startActivity(intent);
    }

    private void updateSummary() {
        if (fragmentSummary != null && fragmentSummary.recyclerView != null
                && fragments.get(viewPager.getCurrentItem()).getClass().equals(FragmentSummary.class)) {
            fragmentSummary.fullUpdate(-1);
        } else {
            mySharedPreferences.edit().putBoolean(FgConst.PREF_FORCE_UPDATE_SUMMARY, true).apply();
        }
    }

    private void updateAccounts() {
        if (fragmentAccounts != null && fragmentAccounts.recyclerView != null
                && fragments.get(viewPager.getCurrentItem()).getClass().equals(FragmentAccounts.class)) {
            fragmentAccounts.fullUpdate(-1);
        } else {
            mySharedPreferences.edit().putBoolean(FgConst.PREF_FORCE_UPDATE_ACCOUNTS, true).apply();
        }
    }

    private void updateTransactions(long transactionID) {
        if (fragmentTransactions != null && fragmentTransactions.recyclerView != null
                && fragments.get(viewPager.getCurrentItem()).getClass().equals(FragmentTransactions.class)) {
            fragmentTransactions.fullUpdate(transactionID);
        } else {
            mySharedPreferences.edit().putBoolean(FgConst.PREF_FORCE_UPDATE_TRANSACTIONS, true).apply();
        }
    }

    private void updateLists() {
//        long accountSetID = mySharedPreferences.getLong(FgConst.PREF_CURRENT_ACCOUNT_SET, -1);
        AccountsSet currentAccountsSet = AccountsSetManager.getInstance().getCurrentAccountSet(this);

        if (currentAccountsSet.getAccountsSetRef().getID() >= 0) {
            mTextViewActiveSet.setText(currentAccountsSet.getAccountsSetRef().getName());
            mTextViewActiveSet.setVisibility(View.VISIBLE);
        } else {
            mTextViewActiveSet.setVisibility(View.GONE);
        }

        updateSummary();
        updateAccounts();
        updateTransactions(-1);
    }

    private void updateCounters() {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                SmsDAO smsDAO = SmsDAO.getInstance(getApplicationContext());
                try {
                    mUnreadSms = smsDAO.getAllModels().size();
                } catch (Exception e) {
                    mUnreadSms = 0;
                }
                mUpdateUIHandler.sendMessage(mUpdateUIHandler.obtainMessage(MSG_UPDATE_SMS_COUNTERS, mUnreadSms, 0));
            }
        });
        t.start();
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null && resultCode == RESULT_OK && requestCode == RequestCodes.REQUEST_CODE_SELECT_MODEL) {
            IAbstractModel model = data.getParcelableExtra("model");
            if (model.getModelType() == IAbstractModel.MODEL_TYPE_TEMPLATE) {
                Template template = (Template) model;
                Transaction transaction = TransactionManager.templateToTransaction(template, this);
                if (transaction.getAmount().compareTo(BigDecimal.ZERO) == 0 || !transaction.isValidToAutoCreate()) {
                    Intent intent = new Intent(this, ActivityEditTransaction.class);
                    intent.putExtra("transaction", transaction);
                    intent.putExtra("focus_to_amount", true);
                    this.startActivityForResult(intent, RequestCodes.REQUEST_CODE_EDIT_TRANSACTION);
                } else {
                    try {
                        TransactionsDAO.getInstance(this).createModel(transaction);
                    } catch (Exception e) {
                        Toast.makeText(this, R.string.msg_error_on_write_to_db, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }else if (data != null && resultCode == RESULT_OK && requestCode == RequestCodes.REQUEST_CODE_SCAN_QR) {
            Intent intent = new Intent(this, ActivityEditTransaction.class);
            intent.putExtra("transaction", data.getParcelableExtra("transaction"));
            intent.putExtra("load_products", true);
            this.startActivityForResult(intent, RequestCodes.REQUEST_CODE_EDIT_TRANSACTION);
        }else if (requestCode == RequestCodes.REQUEST_CODE_OPEN_PREFERENCES) {
            updateLists();
        } else if (requestCode == RequestCodes.REQUEST_CODE_OPEN_PRO) {
            checkInAppPurchases();
        } else {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    fragments.get(viewPager.getCurrentItem()).onActivityResult(requestCode, resultCode, data);
                }
            }, 200);

        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onEvent(Events.EventOnModelChanged event) {
        Lg.log("EventOnModelChanged model class %s", BaseModel.createModelByType(event.getModelType()).getClass().getName());
        switch (event.getModelType()) {
            case IAbstractModel.MODEL_TYPE_SMS:
                updateCounters();
                break;
            case IAbstractModel.MODEL_TYPE_ACCOUNT:
            case IAbstractModel.MODEL_TYPE_TRANSACTION:
            case IAbstractModel.MODEL_TYPE_CATEGORY:
            case IAbstractModel.MODEL_TYPE_PAYEE:
            case IAbstractModel.MODEL_TYPE_PROJECT:
            case IAbstractModel.MODEL_TYPE_LOCATION:
            case IAbstractModel.MODEL_TYPE_DEPARTMENT:
                synchronized (lastEventTime) {
                    lastEventTime = System.currentTimeMillis();
                    Lg.log("eventsQueue set lastEventTime %s", String.valueOf(lastEventTime));
                }
                if (!waitForQueue) {
                    synchronized (waitForQueue) {
                        waitForQueue = true;
                    }
                    Thread t = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Lg.log("eventsQueue thread started lastEventTime = %s", String.valueOf(lastEventTime));
                            try {
                                Thread.sleep(300);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            synchronized (lastEventTime) {
                                long d = System.currentTimeMillis() - lastEventTime;
                                Lg.log("eventsQueue d = %s", String.valueOf(d));
                                while (d < 300) {
                                    try {
                                        Thread.sleep(50);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                    d = System.currentTimeMillis() - lastEventTime;
                                    Lg.log("eventsQueue sleep 50ms d = %s", String.valueOf(d));
                                    if (d > 1000) {
                                        break;
                                    }
                                }
                                mUpdateUIHandler.sendMessage(mUpdateUIHandler.obtainMessage(MSG_UPDATE_LISTS));
                                synchronized (waitForQueue) {
                                    waitForQueue = false;
                                }
                                Lg.log("eventsQueue sendMessage");
                            }
                        }
                    });
                    t.start();
                }

                break;
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onEvent(Events.EventOnSortAccounts event) {
        if (fragmentAccounts != null && fragmentAccounts.adapter != null) {
            fragmentAccounts.fullUpdate(-1);
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onEvent(Events.EventOnGetSupportMessage event) {
        updateCounters();
    }

    @Override
    public void onProductPurchased(@NonNull String productId, TransactionDetails details) {

    }

    @Override
    public void onPurchaseHistoryRestored() {

    }

    @SuppressLint("StringFormatInvalid")
    @Override
    public void onBillingError(int errorCode, Throwable error) {
        Toast.makeText(this, String.format(getString(R.string.ttl_billing_error), String.valueOf(errorCode)), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBillingInitialized() {
        checkInAppPurchases();
    }

    private void checkInAppPurchases() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "Start checkInAppPurchases thread");
                mReportsPurchased = BuildConfig.DEBUG || mBillingProcessor.isPurchased(InApp.SKU_REPORTS);
                Log.d(TAG, "mReportsPurchased is " + String.valueOf(mReportsPurchased));
            }
        });
        thread.start();
    }

    private static class UpdateUIHandler extends Handler {
        WeakReference<ActivityMain> mActivity;

        UpdateUIHandler(ActivityMain activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            final ActivityMain activity = mActivity.get();
            switch (msg.what) {
                case MSG_UPDATE_LISTS:
                    activity.updateLists();
                    break;
                case MSG_UPDATE_SMS_COUNTERS:
                    activity.invalidateOptionsMenu();
                    break;
            }

        }
    }

    private void setupBottomBar() {
        BottomButtonClickListener clickListener = new BottomButtonClickListener();

        boolean scanQR = mySharedPreferences.getBoolean(FgConst.PREF_ENABLE_SCAN_QR, true);
        mButtonScanQR.setVisibility(scanQR ? View.VISIBLE : View.GONE);

        mButtonTemplates.setOnClickListener(clickListener);
        mButtonScanQR.setOnClickListener(clickListener);
        mButtonNewExpense.setOnClickListener(clickListener);
        mButtonNewIncome.setOnClickListener(clickListener);
        mButtonNewTransfer.setOnClickListener(clickListener);
    }

    private class BottomButtonClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.buttonTemplates) {
                Intent intent = new Intent(ActivityMain.this, ActivityModelList.class);
                intent.putExtra("showHomeButton", false);
                intent.putExtra("model", new Template());
                intent.putExtra("requestCode", RequestCodes.REQUEST_CODE_SELECT_MODEL);
                ActivityMain.this.startActivityForResult(intent, RequestCodes.REQUEST_CODE_SELECT_MODEL);
            } else if (v.getId() == R.id.buttonScanQR) {
                Intent intent = new Intent(ActivityMain.this, ActivityScanQR.class);
                startActivityForResult(intent, RequestCodes.REQUEST_CODE_SCAN_QR);
            } else {
                Transaction transaction = new Transaction(PrefUtils.getDefDepID(ActivityMain.this));
                switch (v.getId()) {
                    case R.id.buttonNewIncome:
                        transaction.setTransactionType(Transaction.TRANSACTION_TYPE_INCOME);
                        break;
                    case R.id.buttonNewExpense:
                        transaction.setTransactionType(Transaction.TRANSACTION_TYPE_EXPENSE);
                        break;
                    case R.id.buttonNewTransfer:
                        transaction.setTransactionType(Transaction.TRANSACTION_TYPE_TRANSFER);
                        break;
                }
                Intent intent = new Intent(ActivityMain.this, ActivityEditTransaction.class);
                intent.putExtra("transaction", transaction);
                ActivityMain.this.startActivityForResult(intent, RequestCodes.REQUEST_CODE_EDIT_TRANSACTION);
            }
        }
    }
}
