package com.yoshione.fingen;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.aurelhubert.ahbottomnavigation.AHBottomNavigation;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationItem;
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
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.yoshione.fingen.adapter.TransactionsArrayAdapter;
import com.yoshione.fingen.dao.SendersDAO;
import com.yoshione.fingen.dao.SmsDAO;
import com.yoshione.fingen.dao.SmsMarkersDAO;
import com.yoshione.fingen.dao.TransactionsDAO;
import com.yoshione.fingen.filters.AbstractFilter;
import com.yoshione.fingen.filters.AccountFilter;
import com.yoshione.fingen.iab.BillingService;
import com.yoshione.fingen.interfaces.IAbstractModel;
import com.yoshione.fingen.interfaces.ITransactionItemEventListener;
import com.yoshione.fingen.managers.AccountsSetManager;
import com.yoshione.fingen.managers.TransactionManager;
import com.yoshione.fingen.model.AccountsSet;
import com.yoshione.fingen.model.BaseModel;
import com.yoshione.fingen.model.Events;
import com.yoshione.fingen.model.Sender;
import com.yoshione.fingen.model.SimpleDebt;
import com.yoshione.fingen.model.SmsMarker;
import com.yoshione.fingen.model.Template;
import com.yoshione.fingen.model.TrEditItem;
import com.yoshione.fingen.model.Transaction;
import com.yoshione.fingen.receivers.SMSReceiver;
import com.yoshione.fingen.utils.ColorUtils;
import com.yoshione.fingen.utils.Lg;
import com.yoshione.fingen.utils.NotificationCounter;
import com.yoshione.fingen.utils.NotificationHelper;
import com.yoshione.fingen.utils.PrefUtils;
import com.yoshione.fingen.utils.RequestCodes;
import com.yoshione.fingen.utils.ScreenUtils;
import com.yoshione.fingen.widgets.ToolbarActivity;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.lang.ref.WeakReference;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import dagger.Lazy;
import io.fabric.sdk.android.Fabric;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;

@RuntimePermissions
public class ActivityMain extends ToolbarActivity {

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

    @BindView(R.id.bottomNavigation)
    AHBottomNavigation mBottomNavigation;
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
    @BindView(R.id.textViewSubtitle)
    TextView mTextViewActiveSet;

    private FragmentAccounts fragmentAccounts;
    FragmentTransactions fragmentTransactions;
    private FragmentSimpleDebts fragmentDebts;
    private FragmentSummary fragmentSummary;
    private Drawer mMaterialDrawer = null;
    FragmentPagerAdapter fragmentPagerAdapter;
    private final List<Fragment> fragments = new ArrayList<>();
    private final SharedPreferences.OnSharedPreferenceChangeListener sharedPreferenceChangeListener =
            (prefs, key) -> {
                if (key.equals("theme")) {
                    ActivityMain.this.recreate(); // the function you want called
                }
            };

    private volatile long lastEventTime = 0L;
    private volatile boolean waitForQueue = false;
    UpdateUIHandler mUpdateUIHandler;
    private int mUnreadSms = 0;

    @Inject
    Lazy<BillingService> mBillingService;
    @Inject
    Lazy<SmsMarkersDAO> mSmsMarkersDAO;
    @Inject
    Lazy<SmsDAO> mSmsDAO;
    @Inject
    Lazy<SendersDAO> mSendersDAO;
    @Inject
    Lazy<TransactionsDAO> mTransactionsDAO;

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
        FGApplication.getAppComponent().inject(this);
        getIntent().putExtra("showHomeButton", false);

        switch (Integer.valueOf(mPreferences.getString("theme", "0"))) {
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

        super.onCreate(null);

        mUpdateUIHandler = new UpdateUIHandler(this);

        if (!BuildConfig.DEBUG) {
            Fabric.with(this, new Crashlytics());
        }

        mPreferences.edit().putBoolean(FgConst.PREF_SWITCH_TAB_ON_START, true).apply();

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

        //<editor-fold desc="Setup fragments">
        mBottomNavigation.setDefaultBackgroundColor(ColorUtils.getlistItemBackgroundColor(ActivityMain.this));
        mBottomNavigation.setOnTabSelectedListener((position, wasSelected) -> {
            if (wasSelected) {
                return true;
            }
            viewPager.setCurrentItem(position);
            return true;
        });
        viewPager.setOffscreenPageLimit(3);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int i) {
                mBottomNavigation.setCurrentItem(i);
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });
        tabLayout.setVisibility(View.GONE);
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
            int prevVersion = mPreferences.getInt("version_code", -1);
            if (version != prevVersion) {
                onUpdateVersion(prevVersion, version);
            }
        }

        mPreferences.registerOnSharedPreferenceChangeListener(sharedPreferenceChangeListener);
        //</editor-fold>
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
        mPreferences.edit().putInt("version_code", newVersion).apply();

        if (prevVersion < 59) {
            try {
                int startTabInt = Integer.valueOf(mPreferences.getString(FgConst.PREF_START_TAB, "0"));
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
                    case 3:
                        startTabString = FgConst.FRAGMENT_DEBTS;
                        break;
                    default:
                        startTabString = FgConst.FRAGMENT_SUMMARY;
                }
                mPreferences.edit().putString(FgConst.PREF_START_TAB, startTabString).apply();
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }

        if (prevVersion > 0 & prevVersion < 113) {
            mPreferences.edit().putInt(FgConst.PREF_NEW_ACCOUNT_BUTTON_COUNTER, 4).apply();
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

    private class FgFragmentPagerAdapter extends FragmentPagerAdapter {

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
        public long getItemId(int position) {
            if (fragments.get(position).getClass().equals(FragmentAccounts.class)) {
                return 1;
            } else if (fragments.get(position).getClass().equals(FragmentTransactions.class)) {
                return 2;
            } else if (fragments.get(position).getClass().equals(FragmentSummary.class)) {
                return 3;
            } else if (fragments.get(position).getClass().equals(FragmentSimpleDebts.class)) {
                return 4;
            } else {
                return 0;
            }
        }

        @Override
        public CharSequence getPageTitle(final int position) {
            if (fragments.get(position).getClass().equals(FragmentAccounts.class)) {
                return getString(R.string.ent_accounts).toUpperCase();
            } else if (fragments.get(position).getClass().equals(FragmentTransactions.class)) {
                return getString(R.string.ent_transactions).toUpperCase();
            } else if (fragments.get(position).getClass().equals(FragmentSummary.class)) {
                return getString(R.string.ent_summary).toUpperCase();
            } else if (fragments.get(position).getClass().equals(FragmentSimpleDebts.class)) {
                return getString(R.string.ent_debts).toUpperCase();
            } else {
                return null;
            }
        }

    }


    private void addFragments(List<TrEditItem> tabs) {
        if (fragmentAccounts == null) {
            fragmentAccounts = FragmentAccounts.newInstance(FgConst.PREF_FORCE_UPDATE_ACCOUNTS, R.layout.fragment_accounts, this::updateLists);
            fragmentAccounts.setmAccountEventListener(account -> {
                int action = Integer.valueOf(mPreferences.getString(FgConst.PREF_ACCOUNT_CLICK_ACTION, String.valueOf(ACCOUNT_CLICK_ACTION_LIST_TRANSACTIONS)));
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

            });
        }
        if (fragmentSummary == null) {
            fragmentSummary = FragmentSummary.newInstance(FgConst.PREF_FORCE_UPDATE_SUMMARY, R.layout.fragment_summary);
        }
        if (fragmentTransactions == null) {
            fragmentTransactions = FragmentTransactions.newInstance(FgConst.PREF_FORCE_UPDATE_TRANSACTIONS, R.layout.fragment_transactions);
        }
        if (fragmentDebts == null) {
            fragmentDebts = FragmentSimpleDebts.newInstance(FgConst.PREF_FORCE_UPDATE_DEBTS, R.layout.fragment_simpledebts);
        }

        fragments.clear();
        mBottomNavigation.removeAllItems();
        for (TrEditItem tab : tabs) {
            if (!tab.isVisible()) {
                continue;
            }
            switch (tab.getID()) {
                case FgConst.FRAGMENT_SUMMARY:
                    fragments.add(fragmentSummary);
                    mBottomNavigation.addItem(new AHBottomNavigationItem(getString(R.string.ent_summary).toUpperCase(), R.drawable.selector_reports));
                    break;
                case FgConst.FRAGMENT_ACCOUNTS:
                    fragments.add(fragmentAccounts);
                    mBottomNavigation.addItem(new AHBottomNavigationItem(getString(R.string.ent_accounts).toUpperCase(), R.drawable.ic_account_gray));
                    break;
                case FgConst.FRAGMENT_TRANSACTIONS:
                    fragments.add(fragmentTransactions);
                    mBottomNavigation.addItem(new AHBottomNavigationItem(getString(R.string.ent_transactions).toUpperCase(), R.drawable.ic_transfer_gray));
                    break;
                case FgConst.FRAGMENT_DEBTS:
                    fragments.add(fragmentDebts);
                    mBottomNavigation.addItem(new AHBottomNavigationItem(getString(R.string.ent_debts).toUpperCase(), R.drawable.ic_debt_gray));
                    break;
            }
        }
    }

    @Override
    public void onDestroy() {
        mPreferences.unregisterOnSharedPreferenceChangeListener(sharedPreferenceChangeListener);
        super.onDestroy();
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);

        checkPermissionsAndShowAlert();

        List<TrEditItem> tabs = PrefUtils.getTabsOrder(mPreferences, ActivityMain.this);

        int cntVisible = 0;
        for (TrEditItem tab : tabs) {
            if (tab.isVisible()) {
                cntVisible++;
            }
        }

        boolean actual = (cntVisible == fragments.size());
        int i = 0, j = 0;
        for (; actual && i < tabs.size() && j < fragments.size(); i++, j++) {
            while (i < tabs.size() && !tabs.get(i).isVisible()) {
                i++;
            }
            if (i < tabs.size()) {
                actual = getFragmentID(fragments.get(j)).equals(tabs.get(i).getID());
            }
        }
        if (!actual) {
            addFragments(tabs);
            int currentItem = viewPager.getCurrentItem();
            fragmentPagerAdapter = new FgFragmentPagerAdapter(getSupportFragmentManager());
            viewPager.setAdapter(fragmentPagerAdapter);
            viewPager.setCurrentItem(currentItem);
        }

        int currentItem = -1;
        boolean switchOnStart = mPreferences.getBoolean(FgConst.PREF_SWITCH_TAB_ON_START, false);
        if (getIntent().getIntExtra("action", 0) == ACTION_OPEN_TRANSACTIONS_LIST) {
            currentItem = tabs.indexOf(PrefUtils.getTrEditItemByID(tabs, FgConst.FRAGMENT_TRANSACTIONS));
        } else if (switchOnStart) {
            currentItem = tabs.indexOf(PrefUtils.getTrEditItemByID(tabs, mPreferences.getString(FgConst.PREF_START_TAB, FgConst.FRAGMENT_SUMMARY)));
            mPreferences.edit().putBoolean(FgConst.PREF_SWITCH_TAB_ON_START, false).apply();
        }
        if (currentItem >= 0 && currentItem < fragments.size()) {
            viewPager.setCurrentItem(currentItem);
        }
    }

    private String getFragmentID(Fragment fragment) {
        if (fragment.getClass().equals(FragmentAccounts.class)) {
            return FgConst.FRAGMENT_ACCOUNTS;
        } else if (fragment.getClass().equals(FragmentTransactions.class)) {
            return FgConst.FRAGMENT_TRANSACTIONS;
        } else if (fragment.getClass().equals(FragmentSummary.class)) {
            return FgConst.FRAGMENT_SUMMARY;
        } else if (fragment.getClass().equals(FragmentSimpleDebts.class)) {
            return FgConst.FRAGMENT_DEBTS;
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
                        new PrimaryDrawerItem().withName(R.string.act_ask_question).withIcon(getIcon(R.drawable.ic_drawer_support)).withIdentifier(DRAWER_ITEM_ID_SUPPORT),
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
                .withOnDrawerItemClickListener((view, position, drawerItem) -> {
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
                            case DRAWER_ITEM_ID_HELP: {
                                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://faq.fingen-app.com/"));
                                startActivity(browserIntent);
                                break;
                            }
                            case DRAWER_ITEM_ID_SUPPORT: {
                                intent = new Intent(Intent.ACTION_SEND);
                                intent.setType("message/rfc822");
                                intent.putExtra(Intent.EXTRA_EMAIL,  new String[] {"support@fingen-app.com"});
                                intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.ttl_email_subject));
                                intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.ttl_email_body));

                                startActivity(Intent.createChooser(intent, "Send Email"));
                                break;
                            }
                            case DRAWER_ITEM_ID_ABOUT: {
                                intent = new Intent(ActivityMain.this, ActivityAbout.class);
                                ActivityMain.this.startActivity(intent);
                                break;
                            }
                        }
                    }
                    return false;
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

        mLastBackPressed = -1;

        NotificationHelper.getInstance(this).cancel(SMSReceiver.NOTIFICATION_ID_TRANSACTION_AUTO_CREATED);
        NotificationCounter notificationCounter = new NotificationCounter(mPreferences);
        notificationCounter.removeNotification(SMSReceiver.NOTIFICATION_ID_TRANSACTION_AUTO_CREATED);

        mMaterialDrawer.deselect();
        updateCounters();

//        updateLists();

        if (mPreferences.getBoolean(FgConst.PREF_HIDE_SUMS_PANEL, true)) {
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
        if (mMaterialDrawer.isDrawerOpen())
        {
            mMaterialDrawer.closeDrawer();
        }
        else if (
                fragments.get(viewPager.getCurrentItem()).getClass().equals(FragmentAccounts.class) &
                        (mSlidingUpAccounts != null &&
                                (mSlidingUpAccounts.getPanelState() ==
                                        SlidingUpPanelLayout.PanelState.EXPANDED ||
                                        mSlidingUpAccounts.getPanelState() ==
                                                SlidingUpPanelLayout.PanelState.ANCHORED)
                        )
                )
        {
            mSlidingUpAccounts.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        }
        else if (fragments.get(viewPager.getCurrentItem()).getClass().equals(FragmentTransactions.class) & (mSlidingUpTransactions != null &&
                (mSlidingUpTransactions.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED || mSlidingUpTransactions.getPanelState() == SlidingUpPanelLayout.PanelState.ANCHORED))) {
            mSlidingUpTransactions.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        } else if (fragments.get(viewPager.getCurrentItem()).getClass().equals(FragmentTransactions.class) && fragmentTransactions.mFabMenuController.isFABOpen()) {
            fragmentTransactions.mFabMenuController.closeFABMenu();
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
                .setPositiveButton(R.string.act_next, (dialog, which) -> request.proceed())
                .setNegativeButton(android.R.string.cancel, (dialog, which) -> request.cancel())
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
                .setPositiveButton(R.string.act_next, (dialog, which) -> request.proceed())
                .setNegativeButton(android.R.string.cancel, (dialog, which) -> request.cancel())
                .setCancelable(false)
                .setMessage(messageResId)
                .show();
    }
    //</editor-fold>

    private void checkPermissionsAndShowAlert() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED) {
            List<SmsMarker> smsMarkers;
            try {
                smsMarkers = mSmsMarkersDAO.get().getAllSmsParserPatterns();
            } catch (Exception e) {
                smsMarkers = new ArrayList<>();
            }
            List<Sender> senders;
            try {
                senders = mSendersDAO.get().getAllSenders();
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
            mPreferences.edit().putBoolean(FgConst.PREF_FORCE_UPDATE_SUMMARY, true).apply();
        }
    }

    private void updateAccounts() {
        if (fragmentAccounts != null && fragmentAccounts.recyclerView != null
                && fragments.get(viewPager.getCurrentItem()).getClass().equals(FragmentAccounts.class)) {
            fragmentAccounts.fullUpdate(-1);
        } else {
            mPreferences.edit().putBoolean(FgConst.PREF_FORCE_UPDATE_ACCOUNTS, true).apply();
        }
    }

    private void updateTransactions(long transactionID) {
        if (fragmentTransactions != null && fragmentTransactions.recyclerView != null
                && fragments.get(viewPager.getCurrentItem()).getClass().equals(FragmentTransactions.class)) {
            fragmentTransactions.fullUpdate(transactionID);
        } else {
            mPreferences.edit().putBoolean(FgConst.PREF_FORCE_UPDATE_TRANSACTIONS, true).apply();
        }
    }

    private void updateDebts() {
        if (fragmentDebts != null && fragmentDebts.recyclerView != null
                && fragments.get(viewPager.getCurrentItem()).getClass().equals(FragmentSimpleDebts.class)) {
            fragmentDebts.fullUpdate(-1);
        } else {
            mPreferences.edit().putBoolean(FgConst.PREF_FORCE_UPDATE_DEBTS, true).apply();
        }
    }

    private void updateLists() {
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
        updateDebts();
    }

    private void updateCounters() {
        Thread t = new Thread(() -> {
            try {
                mUnreadSms = mSmsDAO.get().getAllModels().size();
            } catch (Exception e) {
                mUnreadSms = 0;
            }
            mUpdateUIHandler.sendMessage(mUpdateUIHandler.obtainMessage(MSG_UPDATE_SMS_COUNTERS, mUnreadSms, 0));
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
                        mTransactionsDAO.get().createModel(transaction);
                    } catch (Exception e) {
                        Toast.makeText(this, R.string.msg_error_on_write_to_db, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }else if (data != null && resultCode == RESULT_OK && requestCode == RequestCodes.REQUEST_CODE_SCAN_QR) {
            final Intent intent = new Intent(this, ActivityEditTransaction.class);
            final Transaction transaction = data.getParcelableExtra("transaction");
            List<Transaction> transactions = mTransactionsDAO.get().getTransactionsByQR(transaction, getApplicationContext());
            if (transactions.isEmpty()) {
                intent.putExtra("transaction", transaction);
                intent.putExtra("load_products", true);
                this.startActivityForResult(intent, RequestCodes.REQUEST_CODE_EDIT_TRANSACTION);
            } else {
                AlertDialog.Builder builderSingle = new AlertDialog.Builder(this);
                builderSingle.setTitle(getResources().getString(R.string.ttl_attach_receipt_to));

                final Dialog[] dialog = new Dialog[]{null};
                final TransactionsArrayAdapter arrayAdapter = new TransactionsArrayAdapter(
                        this, transactions, new ITransactionItemEventListener() {
                    @Override
                    public void onTransactionItemClick(Transaction foundTransaction) {
                        dialog[0].dismiss();
                        foundTransaction.setFN(transaction.getFN());
                        foundTransaction.setFD(transaction.getFD());
                        foundTransaction.setFP(transaction.getFP());
                        intent.putExtra("transaction", foundTransaction);
                        intent.putExtra("load_products", true);
                        startActivityForResult(intent, RequestCodes.REQUEST_CODE_EDIT_TRANSACTION);
                    }

                    @Override
                    public void onSelectionChange(int selectedCount) {

                    }
                });
                arrayAdapter.addAll(transactions);

                builderSingle.setPositiveButton(getResources().getString(R.string.act_create_new),
                        (dialogInterface, i) -> {
                            intent.putExtra("transaction", transaction);
                            intent.putExtra("load_products", true);
                            startActivityForResult(intent, RequestCodes.REQUEST_CODE_EDIT_TRANSACTION);
                        });

                builderSingle.setAdapter(arrayAdapter, null);
                dialog[0] = builderSingle.show();

                WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
                lp.copyFrom(Objects.requireNonNull(dialog[0].getWindow()).getAttributes());
                lp.width = WindowManager.LayoutParams.MATCH_PARENT;
                lp.height = ScreenUtils.dpToPx(500f, this);
                dialog[0].show();
                dialog[0].getWindow().setAttributes(lp);
            }
        }else if (requestCode == RequestCodes.REQUEST_CODE_OPEN_PREFERENCES) {
//            updateLists();
        } else {
            new Handler().postDelayed(() -> fragments.get(viewPager.getCurrentItem()).onActivityResult(requestCode, resultCode, data), 200);
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
                lastEventTime = System.currentTimeMillis();
                Lg.log("eventsQueue set lastEventTime %s", String.valueOf(lastEventTime));
                if (!waitForQueue) {
                    waitForQueue = true;
                    Thread t = new Thread(() -> {
                        Lg.log("eventsQueue thread started lastEventTime = %s", String.valueOf(lastEventTime));
                        try {
                            Thread.sleep(300);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
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
                        waitForQueue = false;
                        Lg.log("eventsQueue sendMessage");
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
}
