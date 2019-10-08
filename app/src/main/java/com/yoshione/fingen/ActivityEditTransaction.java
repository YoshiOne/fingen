package com.yoshione.fingen;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.BackgroundColorSpan;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.yoshione.fingen.adapter.AdapterProducts;
import com.yoshione.fingen.adapter.NestedItemFullNameAdapter;
import com.yoshione.fingen.dao.AccountsDAO;
import com.yoshione.fingen.dao.CategoriesDAO;
import com.yoshione.fingen.dao.DepartmentsDAO;
import com.yoshione.fingen.dao.LocationsDAO;
import com.yoshione.fingen.dao.PayeesDAO;
import com.yoshione.fingen.dao.ProjectsDAO;
import com.yoshione.fingen.dao.SimpleDebtsDAO;
import com.yoshione.fingen.dao.SmsDAO;
import com.yoshione.fingen.dao.SmsMarkersDAO;
import com.yoshione.fingen.dao.TemplatesDAO;
import com.yoshione.fingen.dao.TransactionsDAO;
import com.yoshione.fingen.fts.ActivityFtsLogin;
import com.yoshione.fingen.fts.ActivityScanQR;
import com.yoshione.fingen.fts.FtsHelper;
import com.yoshione.fingen.fts.IDownloadProductsListener;
import com.yoshione.fingen.interfaces.IAbstractModel;
import com.yoshione.fingen.managers.AccountManager;
import com.yoshione.fingen.managers.PayeeManager;
import com.yoshione.fingen.managers.SmsMarkerManager;
import com.yoshione.fingen.managers.TransactionManager;
import com.yoshione.fingen.managers.TransferManager;
import com.yoshione.fingen.managers.TreeManager;
import com.yoshione.fingen.model.Account;
import com.yoshione.fingen.model.AutocompleteItem;
import com.yoshione.fingen.model.Cabbage;
import com.yoshione.fingen.model.Category;
import com.yoshione.fingen.model.Credit;
import com.yoshione.fingen.model.Location;
import com.yoshione.fingen.model.Payee;
import com.yoshione.fingen.model.ProductEntry;
import com.yoshione.fingen.model.Sms;
import com.yoshione.fingen.model.SmsMarker;
import com.yoshione.fingen.model.Template;
import com.yoshione.fingen.model.TrEditItem;
import com.yoshione.fingen.model.Transaction;
import com.yoshione.fingen.receivers.SMSReceiver;
import com.yoshione.fingen.utils.BaseNode;
import com.yoshione.fingen.utils.CabbageFormatter;
import com.yoshione.fingen.utils.DateTimeFormatter;
import com.yoshione.fingen.utils.FabMenuController;
import com.yoshione.fingen.utils.NotificationCounter;
import com.yoshione.fingen.utils.NotificationHelper;
import com.yoshione.fingen.utils.PrefUtils;
import com.yoshione.fingen.utils.RequestCodes;
import com.yoshione.fingen.utils.SmartFragmentStatePagerAdapter;
import com.yoshione.fingen.utils.SmsParser;
import com.yoshione.fingen.utils.SwipeDetector;
import com.yoshione.fingen.widgets.AmountEditor;
import com.yoshione.fingen.widgets.MyViewPager;
import com.yoshione.fingen.widgets.SmsEditText;
import com.yoshione.fingen.widgets.ToolbarActivity;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.fabric.sdk.android.Fabric;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;
import uk.co.deanwild.materialshowcaseview.ShowcaseConfig;

import static com.yoshione.fingen.filters.DateRangeFilter.getFirstDayOfWeek;
import static com.yoshione.fingen.utils.RequestCodes.REQUEST_CODE_SELECT_MODEL;
import static com.yoshione.fingen.utils.RequestCodes.REQUEST_CODE_SELECT_MODEL_FOR_PRODUCT;

/**
 * Created by slv on 20.08.2015.
 * a
 */
@RuntimePermissions
public class ActivityEditTransaction extends ToolbarActivity implements
        FragmentDestAccount.FragmentDestAccountListener,
        FragmentPayee.FragmentPayeeListener{

    //<editor-fold desc="Static declarations" defaultstate="collapsed">
    private static final int FRAGMENT_PAYEE = 0;
    private static final int FRAGMENT_DEST_ACCOUNT = 1;
    private static final int ERR_EXRATE_ZERO = 1;
    private static final int ERR_EXRATE_ONE = 2;
    private static final int ERR_EMPTY_SRC_ACCOUNT = 3;
    private static final String SHOWCASE_ID = "Edit transaction showcase";
    //</editor-fold>

    //<editor-fold desc="Bind views" defaultstate="collapsed">
    @BindView(R.id.te_lay_DateTime)
    LinearLayout teLayDateTime;
    @BindView(R.id.editTextTemplateName)
    EditText editTextTemplateName;
    @BindView(R.id.layoutCategory)
    RelativeLayout layoutCategory;
    @BindView(R.id.layoutSms)
    RelativeLayout layoutSms;
    @BindView(R.id.layoutProject)
    RelativeLayout layoutProject;
    @BindView(R.id.layoutLocation)
    RelativeLayout layoutLocation;
    @BindView(R.id.textViewSrcAmount)
    EditText textViewSrcAmount;
    @BindView(R.id.textViewDepartment)
    EditText edDepartment;
    @BindView(R.id.imageButtonDeleteDepartment)
    ImageButton imageButtonDeleteDepartment;
    @BindView(R.id.layoutDepartment)
    RelativeLayout layoutDepartment;
    @BindView(R.id.textViewSimpleDebt)
    EditText mTextViewSimpleDebt;
    @BindView(R.id.imageButtonDeleteDebt)
    ImageButton mImageButtonDeleteDebt;
    @BindView(R.id.layoutSimpleDebt)
    RelativeLayout layoutSimpleDebt;
    @BindView(R.id.buttonMore)
    Button mButtonMore;
    @BindView(R.id.textInputLayoutTemplateName)
    TextInputLayout mTextInputLayoutTemplateName;
    @BindView(R.id.textInputLayoutSrcAmount)
    TextInputLayout mTextInputLayoutSrcAmount;
    @BindView(R.id.textInputLayoutExchangeRate)
    TextInputLayout mTextInputLayoutExchangeRate;
    @BindView(R.id.te_pager_payee)
    MyViewPager viewPager;
    @BindView(R.id.textInputLayoutAccount)
    TextInputLayout textInputLayoutAccount;
    @BindView(R.id.textViewAccount)
    EditText textViewAccount;
    @BindView(R.id.textViewCategory)
    EditText edCategory;
    @BindView(R.id.textViewProject)
    EditText edProject;
    @BindView(R.id.textViewLocation)
    EditText edLocation;
    @BindView(R.id.editTextDate)
    EditText edDate;
    @BindView(R.id.te_ed_Time)
    EditText edTime;
    @BindView(R.id.edit_text_exchange_rate)
    EditText edExchangeRate;
    @BindView(R.id.editTextComment)
    EditText edComment;
    @BindView(R.id.te_tv_sms)
    SmsEditText edSms;
    @BindView(R.id.amount_editor)
    AmountEditor amountEditor;
    @BindView(R.id.dest_amount_editor)
    AmountEditor destAmountEditor;
    @BindView(R.id.imageButtonDeleteLocation)
    ImageButton imageButtonDeleteLocation;
    @BindView(R.id.imageButtonDeleteCategory)
    ImageButton imageButtonDeleteCategory;
    @BindView(R.id.imageButtonDeleteProject)
    ImageButton imageButtonDeleteProject;
    @BindView(R.id.imageButtonAddMarker)
    ImageButton imageButtonAddMarker;
    @BindView(R.id.tabLayoutType)
    TabLayout tabLayoutType;
    @BindView(R.id.textViewFN)
    EditText mTextViewFN;
    @BindView(R.id.textViewFD)
    EditText mTextViewFD;
    @BindView(R.id.textViewFP)
    EditText mTextViewFP;
    @BindView(R.id.imageButtonDownloadReceipt)
    ImageButton mImageButtonDownloadReceipt;
    @BindView(R.id.imageButtonScanQR)
    ImageButton mImageButtonScanQR;
    @BindView(R.id.layoutFTS)
    ConstraintLayout mLayoutFTS;
    @BindView(R.id.textViewCaptonProductList)
    TextView mTextViewCaptonProductList;
    @BindView(R.id.expandableIndicator)
    ImageView mExpandableIndicator;
    @BindView(R.id.recyclerViewProductList)
    RecyclerView mRecyclerViewProductList;
    @BindView(R.id.layoutProductList)
    ConstraintLayout mLayoutProductList;
    @BindView(R.id.imageViewLoadingProducts)
    ImageView mImageViewLoadingProducts;
    @BindView(R.id.textViewLoadingProducts)
    TextView mTextViewLoadingProducts;
    @BindView(R.id.layoutLoadingProducts)
    ConstraintLayout mLayoutLoadingProducts;
    @BindView(R.id.imageButtonInvertExRate)
    ImageButton mImageButtonInvertExRate;
    @BindView(R.id.layoutExchangeRate)
    RelativeLayout mLayoutExchangeRate;
    @BindView(R.id.fabBGLayout)
    View mFabBGLayout;
    @BindView(R.id.fabSelectAll)
    FloatingActionButton mFabSelectAll;
    @BindView(R.id.fabSelectAllLayout)
    LinearLayout mFabSelectAllLayout;
    @BindView(R.id.fabUnselectAll)
    FloatingActionButton mFabUnselectAll;
    @BindView(R.id.fabUnselectAllLayout)
    LinearLayout mFabUnselectAllLayout;
    @BindView(R.id.fabSetCategory)
    FloatingActionButton mFabSetCategory;
    @BindView(R.id.fabSetCategoryLayout)
    LinearLayout mFabSetCategoryLayout;
    @BindView(R.id.fabSetProject)
    FloatingActionButton mFabSetProject;
    @BindView(R.id.fabSetProjectLayout)
    LinearLayout mFabSetProjectLayout;
    @BindView(R.id.fabDeleteSelected)
    FloatingActionButton mFabDeleteSelected;
    @BindView(R.id.fabDeleteSelectedLayout)
    LinearLayout mFabDeleteSelectedLayout;
    @BindView(R.id.fabMenuButtonRoot)
    FloatingActionButton mFabMenuButtonRoot;
    @BindView(R.id.fabMenuButtonRootLayout)
    LinearLayout mFabMenuButtonRootLayout;
    @BindView(R.id.layoutPayeeOrDestAcc)
    LinearLayout mLayoutPayeeOrDestAcc;
    @BindView(R.id.layoutAmounts)
    LinearLayout mLayoutAmounts;
    @BindView(R.id.layoutRoot)
    LinearLayout mLayoutRoot;
    @BindView(R.id.layoutComment)
    TextInputLayout mLayoutComment;
    //</editor-fold>

    private MyPagerAdapter fragmentPagerAdapter;
    private OnDestAmountChangeListener onDestAmountChangeListener;
    private OnExRateTextChangedListener onExRateTextChangedListener;
    private String mPayeeName;
    private Transaction transaction;
    private Transaction srcTransaction;//исходная транзакция для сплита
    private Template template;
    private Sms sms;
    private int mLastTrType = Transaction.TRANSACTION_TYPE_EXPENSE;
    private Credit mCredit;
    private int credit_action = -1;
    private BigDecimal srcAmount;
    private double lat = 0;
    private double lon = 0;
    private int accuracy = 0;
    private String provider = "";
    private LocationManager locationManager;
    private boolean forceUpdateLocation = false;
    private boolean allowUpdateLocation;
    private boolean mIsBtnMorePressed = false;
    private boolean isExRateInverted = false;
    private boolean mDoNotChangeIsAmountEdited = false;
    private boolean isAmountEdited = false;
    private boolean isErrorLoadingProducts = false;
    private FabMenuController mFabMenuController;
    private List<TrEditItem> mTrEditItems;
    private LocationListener locationListener;
    private SharedPreferences.OnSharedPreferenceChangeListener sharedPreferenceChangeListener;

    @Inject
    FtsHelper mFtsHelper;

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_edit_transaction;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FGApplication.getAppComponent().inject(this);

        if (!BuildConfig.DEBUG) {
            if (!Fabric.isInitialized()) {
                Fabric.with(this, new Crashlytics());
            }
        }

        ButterKnife.bind(this);

        mTrEditItems = PrefUtils.getTrEditorLayout(mPreferences, this);
        recreateViews();
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(sharedPreferenceChangeListener);

        //получаем объекты
        initObjects(savedInstanceState);

        amountEditor.setActivity(this);

        isAmountEdited = transaction.getAmount().compareTo(BigDecimal.ZERO) != 0;

        mRecyclerViewProductList.setLayoutManager(new LinearLayoutManager(this) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        });

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(android.location.Location location) {
                if (location == null)
                    return;
                lat = location.getLatitude();
                lon = location.getLongitude();
                accuracy = Math.round(location.getAccuracy());
                provider = location.getProvider();
                updateEdLocation();
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };

        sharedPreferenceChangeListener = (prefs, key) -> {
            if (key.equals(FgConst.PREF_TRANSACTION_EDITOR_CONSTRUCTOR)) {
                mTrEditItems = PrefUtils.getTrEditorLayout(mPreferences, ActivityEditTransaction.this);
                recreateViews();
            }
        };
    }

    @Override
    public void onDestroy() {
        PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(sharedPreferenceChangeListener);
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();

        NotificationHelper.getInstance(this).cancel(SMSReceiver.NOTIFICATION_ID_TRANSACTION_AUTO_CREATED);
        NotificationCounter notificationCounter = new NotificationCounter(PreferenceManager.getDefaultSharedPreferences(this));
        notificationCounter.removeNotification(SMSReceiver.NOTIFICATION_ID_TRANSACTION_AUTO_CREATED);

        forceUpdateLocation = transaction.getID() < 0;
        allowUpdateLocation = mPreferences.getBoolean("detect_locations", false) & forceUpdateLocation & (srcTransaction == null);

        if (allowUpdateLocation) {
            ActivityEditTransactionPermissionsDispatcher.startDetectCoordsWithPermissionCheck(this);
        }

        if (transaction.getTransactionType() != Transaction.TRANSACTION_TYPE_TRANSFER) {
            mLastTrType = transaction.getTransactionType();
        }

        initUI();

        if (getIntent().getBooleanExtra("focus_to_category", false)) {
            Intent intent = new Intent(this, ActivityList.class);
            intent.putExtra("showHomeButton", false);
            intent.putExtra("model", new Category());
            intent.putExtra("requestCode", REQUEST_CODE_SELECT_MODEL);
            startActivityForResult(intent, REQUEST_CODE_SELECT_MODEL);
        }

        if (getIntent().getBooleanExtra("scan_qr", false)) {

            getIntent().removeExtra("scan_qr");
            Intent intent = new Intent(ActivityEditTransaction.this, ActivityScanQR.class);
            intent.putExtra("transaction", transaction);
            startActivityForResult(intent, RequestCodes.REQUEST_CODE_SCAN_QR);
        }
    }

    @Override
    protected void onPause() {
        removeUpdates();
        super.onPause();
    }

    private void recreateViews() {
        mLayoutRoot.removeView(teLayDateTime);
        mLayoutRoot.removeView(textInputLayoutAccount);
        mLayoutRoot.removeView(mLayoutPayeeOrDestAcc);
        mLayoutRoot.removeView(layoutCategory);
        mLayoutRoot.removeView(mLayoutAmounts);
        mLayoutRoot.removeView(layoutSms);
        mLayoutRoot.removeView(mLayoutFTS);
        mLayoutRoot.removeView(mLayoutProductList);
        mLayoutRoot.removeView(layoutProject);
        mLayoutRoot.removeView(layoutSimpleDebt);
        mLayoutRoot.removeView(layoutDepartment);
        mLayoutRoot.removeView(layoutLocation);
        mLayoutRoot.removeView(mLayoutComment);
        mLayoutRoot.removeView(mButtonMore);

        boolean focused = false;

        for (TrEditItem item : mTrEditItems) {
            switch (item.getID()) {
                case FgConst.TEI_DATETIME :
                    mLayoutRoot.addView(teLayDateTime);
                    break;
                case FgConst.TEI_ACCOUNT :
                    mLayoutRoot.addView(textInputLayoutAccount);
                    break;
                case FgConst.TEI_PAYEE_DEST_ACC :
                    mLayoutRoot.addView(mLayoutPayeeOrDestAcc);
                    if (!focused) {
                        mLayoutPayeeOrDestAcc.requestFocus();
                        focused = true;
                    }
                    break;
                case FgConst.TEI_CATEGORY :
                    mLayoutRoot.addView(layoutCategory);
                    break;
                case FgConst.TEI_AMOUNTS :
                    mLayoutRoot.addView(mLayoutAmounts);
                    if (!focused) {
                        amountEditor.requestFocus();
                        focused = true;
                    }
                    break;
                case FgConst.TEI_SMS :
                    mLayoutRoot.addView(layoutSms);
                    break;
                case FgConst.TEI_FTS :
                    mLayoutRoot.addView(mLayoutFTS);
                    break;
                case FgConst.TEI_PRODUCT_LIST :
                    mLayoutRoot.addView(mLayoutProductList);
                    break;
                case FgConst.TEI_PROJECT :
                    mLayoutRoot.addView(layoutProject);
                    break;
                case FgConst.TEI_SIMPLE_DEBT :
                    mLayoutRoot.addView(layoutSimpleDebt);
                    break;
                case FgConst.TEI_DEPARTMENT :
                    mLayoutRoot.addView(layoutDepartment);
                    break;
                case FgConst.TEI_LOCATION :
                    mLayoutRoot.addView(layoutLocation);
                    break;
                case FgConst.TEI_COMMENT :
                    mLayoutRoot.addView(mLayoutComment);
                    break;
            }
        }
        mLayoutRoot.addView(mButtonMore);
    }

    public void initUI() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(ContextCompat.getDrawable(this, R.drawable.ic_close_white));
        }

        initSrcAmount();

        initTemplateName();

        initDateTimeButtons();

        initAccount();

        initViewPager();

//        initViewPagerPayee();

        initCategory();

        initFTS();

        initProductList();

        initProject();

        initSimpleDebt();

        initDepartment();

        initLocation();

        initAmountEditor();

        initDestAmountEditor();

        initExRate();

        initComment();

        initSms();

        initFabMenu();

        updateControlsState();
    }

    public Transaction getTransaction() {
        return transaction;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_transaction_editor, menu);
        menu.findItem(R.id.action_go_home).setVisible(false);
        menu.findItem(R.id.action_show_help).setVisible(true);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_customize_layout:
                FragmentTrEditConstructorDialog dialog = new FragmentTrEditConstructorDialog();
                dialog.show(getSupportFragmentManager(),"fragment_tr_edit_constructor_dialog");
                return true;
            default:
                return false;
        }
    }

    @Override
    protected String getLayoutTitle() {
        if (transaction == null) {
            return "";
        }
        if (template == null) {
            if (transaction.getID() < 0) {
                switch (viewPager.getCurrentItem()) {
                    case 0:
                        return getResources().getString(R.string.act_create_transaction);
                    case 1:
                        return getResources().getString(R.string.ent_new_transfer);
                }
            } else {
                switch (viewPager.getCurrentItem()) {
                    case 0:
                        return getResources().getString(R.string.ent_edit_transaction);
                    case 1:
                        return getResources().getString(R.string.ent_edit_transfer);
                }
            }
        } else {
            if (template.getID() < 0) {
                return getResources().getString(R.string.act_create_template);
            } else {
                return getResources().getString(R.string.ent_edit_template);
            }

        }
        return "";
    }

    private boolean checkPayeeAndCreateIfNecessary(boolean updateAutocompleteAdapter) {
        if ((viewPager.getCurrentItem() == 0) && !mPayeeName.isEmpty()) {
            try {
                transaction.setPayeeID(PayeeManager.checkPayeeAndCreateIfNecessary(transaction.getPayeeID(), mPayeeName, this));
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (updateAutocompleteAdapter) {
                FragmentPayee fragmentPayee = getFragmentPayee();
                if (fragmentPayee != null) {
                    fragmentPayee.setAutocompleteAdapter();
                }
            }
        }

        return transaction.getPayeeID() >= 0;
    }

    private void updatePayeeWithDefCategory() {
        if (transaction.getPayeeID() < 0) return;
        Category category = TransactionManager.getCategory(transaction, this);
        Payee payee = TransactionManager.getPayee(transaction, this);
        Category defCategory = PayeeManager.getDefCategory(payee, this);
        if (defCategory.getID() != category.getID() & category.getID() >= 0) {
            PayeesDAO payeesDAO = PayeesDAO.getInstance(getApplicationContext());
            payee.setDefCategoryID(category.getID());
            try {
                payeesDAO.createModel(payee);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void checkLocation() {
        if (transaction.getLocationID() < 0) {
            transaction.setLat(lat);
            transaction.setLon(lon);
            transaction.setAccuracy(accuracy);
        } else {
            transaction.setLat(0);
            transaction.setLon(0);
            transaction.setAccuracy(-1);
        }
    }

    private void deleteSrcSms() {
        if (sms != null) {
            SmsDAO smsDAO = SmsDAO.getInstance(getApplicationContext());
            smsDAO.deleteModel(sms, true, getApplicationContext());
            if (transaction.getComment().isEmpty()) {
                transaction.setComment(sms.getmBody());
            }
        }
    }

    public int validateTransaction() {
        Account src = TransactionManager.getSrcAccount(transaction, this);
        Account dst = TransactionManager.getDestAccount(transaction, this);
        if (transaction.getAccountID() < 0) {
            return ERR_EMPTY_SRC_ACCOUNT;
        } else if (dst.getID() >= 0 & src.getCabbageId() != dst.getCabbageId() & transaction.getExchangeRate().compareTo(BigDecimal.ZERO) == 0) {
            return ERR_EXRATE_ZERO;
        } else if (dst.getID() >= 0 & src.getCabbageId() != dst.getCabbageId() & transaction.getExchangeRate().compareTo(BigDecimal.ONE) == 0) {
            return ERR_EXRATE_ONE;
        } else {
            return 0;
        }
    }

    @OnClick(R.id.buttonSaveTransaction)
    public void onSaveClick() {

        switch (viewPager.getCurrentItem()) {
            case 0:
                if (transaction.getDestAccountID() > 0) {
                    transaction.setDestAccountID(-1);
                    initViewPagerPayee();
                    transaction.setExchangeRate(BigDecimal.ONE);
                    initExRate();
                }
                break;
            case 1:
                if (transaction.getPayeeID() >= 0) {
                    transaction.setPayeeID(-1);
                    initViewPagerPayee();
                }
                if (TransactionManager.getSrcAccount(transaction, this).getCabbageId() ==
                        TransactionManager.getDestAccount(transaction, this).getCabbageId()) {
                    transaction.setExchangeRate(BigDecimal.ONE);
                    initExRate();
                }
                break;
        }

        //region Данный блок работает только НЕ в режиме сплита
        if (srcTransaction == null) {
            //Если у транзакции нет получателя, но в поле ввода есть текст, значит создаем такого получателя
            checkPayeeAndCreateIfNecessary(false);

            //Устанавливаем текущему получателю в качестве категории по умолчанию текущую категорию
            updatePayeeWithDefCategory();
        }
        //endregion

        if (template == null) {
            switch (validateTransaction()) {
                case 0:
                    saveTransaction();
                    break;
                case ERR_EMPTY_SRC_ACCOUNT:
                    textInputLayoutAccount.setError(getString(R.string.err_specify_account));
                    break;
                case ERR_EXRATE_ZERO:
                    mTextInputLayoutExchangeRate.setError(getString(R.string.err_exrate_zero));
                    break;
                case ERR_EXRATE_ONE:
                    new AlertDialog.Builder(this)
                            .setNegativeButton(android.R.string.no, (dialog, which) -> {

                            })
                            .setPositiveButton(android.R.string.yes, (dialog, which) -> saveTransaction())
                            .setCancelable(false)
                            .setMessage(getString(R.string.err_exrate_one))
                            .show();
                    break;
                default:
                    Toast.makeText(ActivityEditTransaction.this, getResources().getString(R.string.msg_invalid_data_in_fields), Toast.LENGTH_SHORT).show();
            }
        } else {
            //Находимся в режиме редактирования шаблона
            template.extractFromTransaction(transaction);
            if (template.isValid()) {
                try {
                    TemplatesDAO.getInstance(getApplicationContext()).createModel(template);
                } catch (Exception e) {
                    Toast.makeText(this, R.string.msg_error_on_write_to_db, Toast.LENGTH_SHORT).show();
                    return;
                }
                finish();
            } else {
                Toast.makeText(ActivityEditTransaction.this, getResources().getString(R.string.msg_invalid_data_in_fields), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void saveTransaction() {
        PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putLong("last_account_id", transaction.getAccountID()).apply();

        //Если транзакция создается из смс, удаляем ее
        deleteSrcSms();

        //если явно не указано местоположение, пытаемся получить координаты. Если не удается явно задаем 0
        checkLocation();

        transaction.setAutoCreated(false);
        TransactionsDAO transactionsDAO = TransactionsDAO.getInstance(getApplicationContext());
        try {
            transaction = (Transaction) transactionsDAO.createModel(transaction);
        } catch (Exception e) {
            Toast.makeText(this, R.string.msg_error_on_write_to_db, Toast.LENGTH_SHORT).show();
            return;
        }

        if (srcTransaction != null) {
            editSrcTransaction();
            Intent result = new Intent();
            result.putExtra("src_transaction", transactionsDAO.getTransactionByID(srcTransaction.getID()));
            try {
                srcTransaction = (Transaction) transactionsDAO.createModel(srcTransaction);
            } catch (Exception e) {
                e.printStackTrace();
            }
            result.putExtra("split1", srcTransaction);
            result.putExtra("split2", transaction);
            setResult(RESULT_OK, result);
        } else {
            if (!(getIntent().getBooleanExtra("EXIT", false))) {
                setResult(RESULT_OK);
            }
        }

        if (mCredit != null) {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
            switch (credit_action) {
                case Credit.DEBT_ACTION_BORROW:
                case Credit.DEBT_ACTION_TAKE:
                    preferences.edit().putLong("credit_dest_account", transaction.getDestAccountID()).apply();
                    break;
                case Credit.DEBT_ACTION_GRANT:
                case Credit.DEBT_ACTION_REPAY:
                    preferences.edit().putLong("credit_src_account", transaction.getAccountID()).apply();
                    break;
            }
            if (credit_action == Credit.DEBT_ACTION_REPAY | credit_action == Credit.DEBT_ACTION_TAKE) {
                showDebtPercentsDialog(mCredit, credit_action, transaction);
            } else {
                finish();
            }
        } else {
            BigDecimal balanceError = BigDecimal.ZERO;
            if (sms != null) {
                if (transactionsDAO.isTransactionLastForAccount(transaction)) {
                    Account account = AccountsDAO.getInstance(getApplicationContext()).getAccountByID(transaction.getAccountID());
                    balanceError = new SmsParser(sms, this).checkBalance(account);
                }
            }
            if (balanceError.compareTo(BigDecimal.ZERO) != 0) {
                showCorrectBalanceDialog(balanceError);
            } else {
                finish();
                if (getIntent().getBooleanExtra("EXIT", false)) {
                    this.finishAffinity();
                }
            }
        }
    }

    private void editSrcTransaction() {
        if (srcTransaction.getTransactionType() != Transaction.TRANSACTION_TYPE_TRANSFER) {
            int srcType;
            if (srcAmount.compareTo(BigDecimal.ZERO) >= 0) {
                srcType = Transaction.TRANSACTION_TYPE_INCOME;
            } else {
                srcType = Transaction.TRANSACTION_TYPE_EXPENSE;
            }
            srcTransaction.setAmount(srcAmount, srcType);
        } else {
            if (srcAmount.compareTo(BigDecimal.ZERO) <= 0) {
                srcTransaction.setAmount(srcAmount, srcTransaction.getTransactionType());
            } else {
                long acc1 = srcTransaction.getAccountID();
                long acc2 = srcTransaction.getDestAccountID();
                srcTransaction.setAccountID(acc2);
                srcTransaction.setDestAccountID(acc1);
                srcTransaction.setAmount(srcAmount, srcTransaction.getTransactionType());
            }
        }
    }

    @SuppressLint("StringFormatInvalid")
    private void showCorrectBalanceDialog(BigDecimal amount) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.ttl_confirm_action);
        Account account = TransactionManager.getSrcAccount(transaction, this);
        Cabbage cabbage = AccountManager.getCabbage(account, this);
        builder.setMessage(String.format(getString(R.string.msg_confirm_create_correcting_transaction), new CabbageFormatter(cabbage).format(amount)));

        // Set up the buttons
        builder.setPositiveButton("OK", new OnShowCorrectingDialogOkListener(amount, this));
        builder.setNegativeButton("Cancel", new OnShowCorrectingDialogCancelListener(this));

        builder.show();
    }

    @OnClick(R.id.buttonMore)
    public void onClick() {
        mIsBtnMorePressed = !mIsBtnMorePressed;
        updateControlsState();
    }

    private void showDebtPercentsDialog(Credit credit, int creditAction, Transaction transaction) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.ttl_confirm_action);
        builder.setMessage(getString(R.string.msg_confirmation_create_credit_percent_transaction));

        builder.setPositiveButton("OK", new OnShowDebtPercentDialogOkListener(credit, creditAction, transaction, this));
        builder.setNegativeButton("Cancel", new OnShowCorrectingDialogCancelListener(this));

        builder.show();
    }

    private void updateControlsState() {
        mButtonMore.setVisibility(mIsBtnMorePressed ? View.GONE : View.VISIBLE);

        class ItemVisibility {
            RelativeLayout layout;
            String id;
            private long entityId;

            private ItemVisibility(RelativeLayout layout, String id, long entityId) {
                this.layout = layout;
                this.id = id;
                this.entityId = entityId;
            }
        }

        List<ItemVisibility> views = Arrays.asList(
                new ItemVisibility(layoutCategory, FgConst.TEI_CATEGORY, transaction.getCategoryID()),
                new ItemVisibility(layoutProject, FgConst.TEI_PROJECT, transaction.getProjectID()),
                new ItemVisibility(layoutSimpleDebt, FgConst.TEI_SIMPLE_DEBT, transaction.getSimpleDebtID()),
                new ItemVisibility(layoutDepartment, FgConst.TEI_DEPARTMENT, transaction.getDepartmentID()),
                new ItemVisibility(layoutLocation, FgConst.TEI_LOCATION, transaction.getLocationID()));
        TrEditItem item;
        for (ItemVisibility itemVisibility : views) {
            item = PrefUtils.getTrEditItemByID(mTrEditItems, itemVisibility.id);
            if (item != null) {
                itemVisibility.layout.setVisibility((itemVisibility.entityId >= 0 | (!item.isHideUnderMore() || mIsBtnMorePressed))
                        & item.isVisible() ? View.VISIBLE : View.GONE);
            }
        }

        item = PrefUtils.getTrEditItemByID(mTrEditItems, FgConst.TEI_PRODUCT_LIST);
        if (item != null) {
            mLayoutProductList.setVisibility(
                    (isErrorLoadingProducts | transaction.getProductEntries().size() > 0
                    | (!item.isHideUnderMore() || mIsBtnMorePressed)
                    | getIntent().getBooleanExtra("load_products", false))
                     & item.isVisible() ? View.VISIBLE : View.GONE);
        }

        boolean scanQR = mPreferences.getBoolean(FgConst.PREF_ENABLE_SCAN_QR, true);

        item = PrefUtils.getTrEditItemByID(mTrEditItems, FgConst.TEI_FTS);
        if (item != null) {
            mLayoutFTS.setVisibility(
                    (transaction.getFN() > 0 | transaction.getFD() > 0 | transaction.getFP() > 0 | (!item.isHideUnderMore() || mIsBtnMorePressed)) & scanQR & item.isVisible() ? View.VISIBLE : View.GONE);
        }

        if (transaction.getTransactionType() == Transaction.TRANSACTION_TYPE_TRANSFER) {
            Account srcAccount = TransactionManager.getSrcAccount(transaction, this);
            Account dstAccount = TransactionManager.getDestAccount(transaction, this);

            if ((srcAccount.getCabbageId() != dstAccount.getCabbageId()) & (dstAccount.getID() >= 0)) {
                mLayoutExchangeRate.setVisibility(View.VISIBLE);
            } else {
                mLayoutExchangeRate.setVisibility(View.GONE);
            }
            Cabbage dstCabbage = AccountManager.getCabbage(dstAccount, this);
            destAmountEditor.setScale(dstCabbage.getDecimalCount());
            Cabbage srcCabbage = AccountManager.getCabbage(srcAccount, this);
            String s;
            if (onExRateTextChangedListener != null)
                edExchangeRate.removeTextChangedListener(onExRateTextChangedListener);
            isExRateInverted = mPreferences.getBoolean(String.format("%s/%s", srcCabbage.getCode(), dstCabbage.getCode()), false);
            if (!isExRateInverted) {
                s = String.format("%s/%s", srcCabbage.getCode(), dstCabbage.getCode());
                edExchangeRate.setText(String.valueOf(transaction.getExchangeRate().doubleValue()));
            } else {
                s = String.format("%s/%s", dstCabbage.getCode(), srcCabbage.getCode());
                edExchangeRate.setText(String.valueOf(BigDecimal.ONE.divide(transaction.getExchangeRate(), 5, RoundingMode.HALF_UP).doubleValue()));
            }
            if (onExRateTextChangedListener != null)
                edExchangeRate.addTextChangedListener(onExRateTextChangedListener);
            String hint = String.format("%s %s", getString(R.string.ent_exchange_rate), s);
            mTextInputLayoutExchangeRate.setHint(hint);
        } else {
            mLayoutExchangeRate.setVisibility(View.GONE);
        }
        destAmountEditor.setVisibility(mLayoutExchangeRate.getVisibility());

        amountEditor.setType(transaction.getTransactionType());

        destAmountEditor.setType(transaction.getTransactionType());

        updatePayeeHint();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getLayoutTitle());
        }

        boolean productsSelected = false;
        for (ProductEntry entry : transaction.getProductEntries()) {
            if (entry.isSelected()) {
                productsSelected = true;
                break;
            }
        }

        mFabMenuButtonRootLayout.setVisibility(productsSelected ? View.VISIBLE : View.GONE);
    }

    @SuppressWarnings("ConstantConditions")
    private void initSms() {
        if (sms != null) {
            SmsParser smsParser = new SmsParser(sms, getApplicationContext());
            Spannable text = new SpannableString(sms.getmBody());
            text.setSpan(new BackgroundColorSpan(ContextCompat.getColor(this, R.color.ColorAccount)), smsParser.mAccountBorders.first, smsParser.mAccountBorders.second, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            text.setSpan(new BackgroundColorSpan(ContextCompat.getColor(this, R.color.ColorAmount)), smsParser.mAmountBorders.first, smsParser.mAmountBorders.second, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            text.setSpan(new BackgroundColorSpan(ContextCompat.getColor(this, R.color.ColorAmount)), smsParser.mBalanceBorders.first, smsParser.mBalanceBorders.second, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            text.setSpan(new BackgroundColorSpan(ContextCompat.getColor(this, R.color.ColorPayee)), smsParser.mPayeeBorders.first, smsParser.mPayeeBorders.second, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            text.setSpan(new BackgroundColorSpan(ContextCompat.getColor(this, R.color.ColorDestAccount)), smsParser.mDestAccountBorders.first, smsParser.mDestAccountBorders.second, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            text.setSpan(new BackgroundColorSpan(ContextCompat.getColor(this, R.color.ColorType)), smsParser.mTrTypeBorders.first, smsParser.mTrTypeBorders.second, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            text.setSpan(new BackgroundColorSpan(ContextCompat.getColor(this, R.color.ColorCabbage)), smsParser.mCabbageAmountBorders.first, smsParser.mCabbageAmountBorders.second, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            text.setSpan(new BackgroundColorSpan(ContextCompat.getColor(this, R.color.ColorCabbage)), smsParser.mCabbageBalanceBorders.first, smsParser.mCabbageBalanceBorders.second, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            edSms.setText(text);

//            imageButtonAddMarker.setImageDrawable(IconGenerator.getInstance(this).getAddIcon(this));
            imageButtonAddMarker.setOnClickListener(v -> ActivityEditTransaction.this.showAddMarkerDialog());

            edSms.setCustomSelectionActionModeCallback(new ActionModeCallback(this));
        } else {
            layoutSms.setVisibility(View.GONE);
        }

    }

    private void showAddMarkerDialog() {
        int selStart = edSms.getSelectionStart();
        int selEnd = edSms.getSelectionEnd();

        if (selEnd == selStart) {
            Toast.makeText(ActivityEditTransaction.this, getString(R.string.msg_select_marker_text_before), Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builderSingle = new AlertDialog.Builder(this);
        builderSingle.setTitle(this.getResources().getString(R.string.msg_select_marker_type));

        final ArrayAdapter<SmsMarkerManager.SmsMarkerType> arrayAdapter = new ArrayAdapter<>(this,
                android.R.layout.select_dialog_singlechoice);
        arrayAdapter.addAll(SmsMarkerManager.getSmsMarkerTypeObjects(transaction.getTransactionType(), this));

        builderSingle.setNegativeButton(
                this.getResources().getString(android.R.string.cancel),
                (dialog, which) -> dialog.dismiss());


        String selectedText = sms.getmBody().subSequence(Math.max(0, Math.min(selStart, selEnd)),
                Math.max(0, Math.max(selStart, selEnd))).toString();
        builderSingle.setAdapter(arrayAdapter, new OnMarkersDialogOkListener(selectedText));
        builderSingle.show();
    }

    private void selectPayeeForMarker(final int markerType, final String selectedText) {
        if (!checkPayeeAndCreateIfNecessary(true)) {
            PayeeManager.ShowSelectPayeeDialog(ActivityEditTransaction.this, selectedPayee -> {
                transaction.setPayeeID(selectedPayee.getID());
                setPayeeName(selectedPayee.getName());
                ActivityEditTransaction.this.createSmsMarker(markerType, selectedText);
            });
        } else {
            ActivityEditTransaction.this.createSmsMarker(markerType, selectedText);
        }
    }

    private void createSmsMarker(final int markerType, final String selectedText) {
        SmsMarker smsMarker = null;

        switch (markerType) {
            case SmsParser.MARKER_TYPE_PAYEE: {
                if (transaction.getPayeeID() < 0) {
                    if (mPayeeName.isEmpty()) {
                        new AlertDialog.Builder(this)
                                .setNegativeButton(R.string.act_create, (dialog, which) -> {
                                    PayeesDAO payeesDAO = PayeesDAO.getInstance(ActivityEditTransaction.this);
                                    Payee payee = new Payee();
                                    payee.setName(selectedText);
                                    try {
                                        payee = (Payee) payeesDAO.createModel(payee);
                                        transaction.setPayeeID(payee.getID());
                                        setPayeeName(payee.getName());
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    ActivityEditTransaction.this.createSmsMarker(markerType, selectedText);
                                })
                                .setPositiveButton(R.string.act_select, (dialog, which) -> selectPayeeForMarker(markerType, selectedText))
                                .setCancelable(false)
                                .setMessage(String.format(getString(R.string.ttl_create_new_payee_from_marker), selectedText))
                                .show();
                    } else {
                        selectPayeeForMarker(markerType, selectedText);
                    }
                    return;
                }
                if (transaction.getPayeeID() >= 0) {
                    smsMarker = new SmsMarker(-1, SmsParser.MARKER_TYPE_PAYEE, String.valueOf(transaction.getPayeeID()), selectedText);
                }
                break;
            }
            case SmsParser.MARKER_TYPE_DESTACCOUNT: {
                if (transaction.getDestAccountID() < 0) {
                    break;
                }
                smsMarker = new SmsMarker(-1, SmsParser.MARKER_TYPE_DESTACCOUNT, String.valueOf(transaction.getDestAccountID()), selectedText);
                break;
            }
            case SmsParser.MARKER_TYPE_IGNORE: {
                smsMarker = new SmsMarker(-1, SmsParser.MARKER_TYPE_IGNORE, "Ignore", selectedText);
                break;
            }
            case SmsParser.MARKER_TYPE_ACCOUNT: {
                if (transaction.getAccountID() < 0) {
                    break;
                }
                smsMarker = new SmsMarker(-1, SmsParser.MARKER_TYPE_ACCOUNT, String.valueOf(transaction.getAccountID()), selectedText);
                break;
            }
            case SmsParser.MARKER_TYPE_TRTYPE: {
                int type;
                if (viewPager.getCurrentItem() == 0) {
                    type = transaction.getAmountSign() ? 1 : -1;
                } else {
                    type = 0;
                }
                smsMarker = new SmsMarker(-1, SmsParser.MARKER_TYPE_TRTYPE, String.valueOf(type), selectedText);
                break;
            }
            case SmsParser.MARKER_TYPE_CABBAGE: {
                if (transaction.getAccountID() < 0) {
                    break;
                }
                Account account = TransactionManager.getSrcAccount(transaction, this);
                smsMarker = new SmsMarker(-1, SmsParser.MARKER_TYPE_CABBAGE,
                        String.valueOf(account.getCabbageId()),
                        selectedText);
                break;
            }
            default:
                break;
        }

        if (smsMarker != null) {//тру - создан новый паттерн

            //Пишем новый паттерн в БД
            SmsMarkersDAO smsMarkersDAO = SmsMarkersDAO.getInstance(getApplicationContext());
            try {
                smsMarkersDAO.createModel(smsMarker);
            } catch (Exception e) {
                Toast.makeText(this, R.string.msg_error_on_write_to_db, Toast.LENGTH_SHORT).show();
                return;
            }

            //если мы изменили паттерн валюты или знака, заново парсим смс и проверяем не нашли ли мы новую сумму.
            switch (markerType) {
                case SmsParser.MARKER_TYPE_CABBAGE:
                case SmsParser.MARKER_TYPE_TRTYPE: {
                    SmsParser smsParser1 = new SmsParser(sms, getApplicationContext());
                    int type = smsParser1.extractTrType();
                    transaction.setAmount(amountEditor.getAmount(), type);
                    if (transaction.getAmount().compareTo(BigDecimal.ZERO) == 0) {
                        Account account = TransactionManager.getSrcAccount(transaction, this);
                        BigDecimal extractedAmount = smsParser1.extractAmount(account);
                        transaction.setAmount(extractedAmount, type);
                        amountEditor.setAmount(transaction.getAmount());
                        amountEditor.setType(type);
                    }
                }
            }

            //заново оформляем TextView с смской
            initSms();
        }
    }

    private void initObjects(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            if (getIntent().getAction() != null) {
                switch (getIntent().getAction()) {
                    case FgConst.ACT_NEW_EXPENSE:
                        transaction = new Transaction(PrefUtils.getDefDepID(this));
                        transaction.setTransactionType(Transaction.TRANSACTION_TYPE_EXPENSE);
                        break;
                    case FgConst.ACT_NEW_INCOME:
                        transaction = new Transaction(PrefUtils.getDefDepID(this));
                        transaction.setTransactionType(Transaction.TRANSACTION_TYPE_INCOME);
                        break;
                    case FgConst.ACT_NEW_TRANSFER:
                        transaction = new Transaction(PrefUtils.getDefDepID(this));
                        transaction.setTransactionType(Transaction.TRANSACTION_TYPE_TRANSFER);
                        break;
                    default:
                        transaction = getIntent().getParcelableExtra("transaction");
                }
            } else {
                transaction = getIntent().getParcelableExtra("transaction");
            }
            if (transaction == null) {
                transaction = new Transaction(PrefUtils.getDefDepID(this));
            }
            if (getIntent().getBooleanExtra("update_date", false)) {
                transaction.setDateTime(new Date());
            }
            srcTransaction = getIntent().getParcelableExtra("src_transaction");
            if (srcTransaction != null) {
                srcAmount = new BigDecimal(srcTransaction.getAmount().doubleValue());
            } else {
                srcAmount = BigDecimal.ZERO;
            }
            template = getIntent().getParcelableExtra("template");
            mCredit = getIntent().getParcelableExtra("credit");
            credit_action = getIntent().getIntExtra("credit_action", -1);
            //получаем смс
            sms = getIntent().getParcelableExtra("sms");
            if (sms != null) {
                SmsParser smsParser = new SmsParser(sms, this);
                transaction = smsParser.extractTransaction();
            }

            if (getIntent().getStringExtra("template_name") != null) {
                if (!getIntent().getStringExtra("template_name").isEmpty()) {
                    try {
                        transaction = TransactionManager.templateToTransaction((Template) TemplatesDAO.getInstance(this).getModelByName(getIntent().getStringExtra("template_name")), this);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    getIntent().putExtra("transaction", transaction);
                }
            }
        } else {
            onRestoreInstanceState(savedInstanceState);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("payee_name", mPayeeName);
        outState.putParcelable("transaction", transaction);
        outState.putParcelable("sms", sms);
        outState.putParcelable("src_transaction", srcTransaction);
        outState.putParcelable("template", template);
        outState.putParcelable("credit", mCredit);
        outState.putInt("credit_action", credit_action);
        outState.putInt("viewpager_current_item", viewPager.getCurrentItem());
        outState.putInt("last_transaction_type", mLastTrType);
        outState.putBoolean("is_button_more_pressed", mIsBtnMorePressed);
        outState.putBoolean("is_exrate_inverted", isExRateInverted);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mPayeeName = savedInstanceState.getString("payee_name");
        transaction = savedInstanceState.getParcelable("transaction");
        sms = savedInstanceState.getParcelable("sms");
        srcTransaction = savedInstanceState.getParcelable("src_transaction");
        if (srcTransaction != null) {
            srcAmount = new BigDecimal(srcTransaction.getAmount().doubleValue());
        } else {
            srcAmount = BigDecimal.ZERO;
        }
        template = savedInstanceState.getParcelable("template");
        mCredit = savedInstanceState.getParcelable("credit");
        credit_action = savedInstanceState.getInt("credit_action");
        viewPager.setCurrentItem(savedInstanceState.getInt("viewpager_current_item"));
        mIsBtnMorePressed = savedInstanceState.getBoolean("is_button_more_pressed");
        isExRateInverted = savedInstanceState.getBoolean("is_exrate_inverted");
        mLastTrType = savedInstanceState.getInt("last_transaction_type");
    }

    //<editor-fold desc="Init fields" defaultstate="collapsed">
    private void initSrcAmount() {
        if (srcTransaction != null) {
            mTextInputLayoutSrcAmount.setVisibility(View.VISIBLE);
            Account account = TransactionManager.getSrcAccount(srcTransaction, this);
            Cabbage cabbage = AccountManager.getCabbage(account, this);
            CabbageFormatter cabbageFormatter = new CabbageFormatter(cabbage);
            textViewSrcAmount.setText(cabbageFormatter.format(srcAmount));
        }
    }

    private void initTemplateName() {
        if (template != null) {
            mTextInputLayoutTemplateName.setVisibility(View.VISIBLE);
            editTextTemplateName.setText(template.getName());
            editTextTemplateName.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    template.setName(s.toString());
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });
        }
    }

    private void initDateTimeButtons() {
        if (template != null || srcTransaction != null) {
            teLayDateTime.setVisibility(View.GONE);
        } else {
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.getInstance(this);
            edDate.setText(dateTimeFormatter.getDateMediumString(transaction.getDateTime()));
            edTime.setText(dateTimeFormatter.getTimeShortString(transaction.getDateTime()));
            new SwipeDetector(edDate).setOnSwipeListener(new SwipeDetector.onSwipeEvent() {
                @Override
                public void TapEventDetected(View v) {
                    onDateClick(edDate);
                }

                @Override
                public void SwipeEventDetected(View v, SwipeDetector.SwipeTypeEnum swipeType) {
                    switch (swipeType) {
                        case LEFT_TO_RIGHT:
                            transaction.setDateTime(new Date(transaction.getDateTime().getTime() - TimeUnit.DAYS.toMillis(1)));
                            break;
                        case RIGHT_TO_LEFT:
                            transaction.setDateTime(new Date(transaction.getDateTime().getTime() + TimeUnit.DAYS.toMillis(1)));
                            break;
                    }
                    initDateTimeButtons();
                }
            });
            new SwipeDetector(edTime).setOnSwipeListener(new SwipeDetector.onSwipeEvent() {
                @Override
                public void TapEventDetected(View v) {
                    onTimeClick(edTime);
                }

                @Override
                public void SwipeEventDetected(View v, SwipeDetector.SwipeTypeEnum swipeType) {
                    switch (swipeType) {
                        case LEFT_TO_RIGHT:
                            transaction.setDateTime(new Date(transaction.getDateTime().getTime() - TimeUnit.HOURS.toMillis(1)));
                            break;
                        case RIGHT_TO_LEFT:
                            transaction.setDateTime(new Date(transaction.getDateTime().getTime() + TimeUnit.HOURS.toMillis(1)));
                            break;
                    }
                    initDateTimeButtons();
                }
            });
        }
    }

    private void initAccount() {
        if (srcTransaction != null) {
            textViewAccount.setVisibility(View.GONE);
        } else {
            AccountsDAO accountsDAO = AccountsDAO.getInstance(getApplicationContext());

            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

            Account account = TransactionManager.getSrcAccount(transaction, this);
            if (preferences.getBoolean(FgConst.PREF_REMEMBER_LAST_ACCOUNT, true) && account.getID() < 0) {
                account = accountsDAO.getAccountByID(PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getLong("last_account_id", -1));
                transaction.setAccountID(account.getID());
            }
            Cabbage cabbage = AccountManager.getCabbage(account, this);
            if (account.getID() < 0) {
                textViewAccount.setText("");
            } else {
                CabbageFormatter cabbageFormatter = new CabbageFormatter(cabbage);
                textViewAccount.setText(String.format("%s (%s)", account.getName(), cabbageFormatter.format(account.getCurrentBalance())));
            }
//            textViewAccountCabbage.setText(cabbage.getCode());

            amountEditor.setScale(cabbage.getDecimalCount());

            textViewAccount.setOnClickListener(view -> {
                Intent intent = new Intent(ActivityEditTransaction.this, ActivityAccounts.class);
                intent.putExtra("showHomeButton", false);
                intent.putExtra("model", new Account());
                intent.putExtra("destAccount", false);
                ActivityEditTransaction.this.startActivityForResult(intent, REQUEST_CODE_SELECT_MODEL);
            });
        }
    }

    private void setTransactionTypeControls(int transactionType) {
        switch (transactionType) {
            case Transaction.TRANSACTION_TYPE_TRANSFER:
                viewPager.setCurrentItem(1);
                break;
            case Transaction.TRANSACTION_TYPE_INCOME:
            case Transaction.TRANSACTION_TYPE_EXPENSE:
                viewPager.setCurrentItem(0);
                break;
        }
    }

    @Override
    public void destAccountTextViewClick() {
        Intent intent = new Intent(this, ActivityAccounts.class);
        intent.putExtra("showHomeButton", false);
        intent.putExtra("model", new Account());
        intent.putExtra("destAccount", true);
        startActivityForResult(intent, RequestCodes.REQUEST_CODE_SELECT_MODEL);
    }

    @Override
    public void InvertTransferDirectionClick() {
        long src = transaction.getAccountID();
        long dst = transaction.getDestAccountID();
        transaction.setAccountID(dst);
        transaction.setDestAccountID(src);
        initUI();
    }

    @Override
    public String getDestAccountName() {
        Account destAccount = TransactionManager.getDestAccount(transaction, this);
        String name = destAccount.getName();
        String code = AccountManager.getCabbage(destAccount, this).getSimbol();
        if (name.isEmpty()) {
            return "";
        } else {
            return String.format("%s (%s)", destAccount.getName(), code);
        }
    }

    // Extend from SmartFragmentStatePagerAdapter now instead for more dynamic ViewPager items
    private class MyPagerAdapter extends SmartFragmentStatePagerAdapter {

        MyPagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        // Returns total number of pages
        @Override
        public int getCount() {
            return 2;//FRAGMENTS_COUNT;
        }

        // Returns the fragment to display for that page
        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0: // Fragment # 0 - This will show FirstFragment
                    if (mPayeeName != null && !mPayeeName.isEmpty()) {
                        setPayeeName(mPayeeName);
                    } else {
                        Payee payee = TransactionManager.getPayee(transaction, getApplicationContext());
                        setPayeeName(payee.getFullName());
                    }
                    FragmentPayee fragmentPayee = FragmentPayee.newInstance();
                    initViewPagerPayee();
                    return fragmentPayee;
                case 1:
                    return FragmentDestAccount.newInstance();
                default:
                    return null;
            }
        }

        // Returns the page title for the top indicator
        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case FRAGMENT_PAYEE:
                    return getString(R.string.ent_payment);
                case FRAGMENT_DEST_ACCOUNT:
                    return getString(R.string.ent_transfer);
                default:
                    return null;
            }
        }

    }

    private void initViewPager() {
        if (srcTransaction != null) {
            tabLayoutType.setVisibility(View.GONE);
            viewPager.setVisibility(View.GONE);
        } else {
            fragmentPagerAdapter = new MyPagerAdapter(getSupportFragmentManager());
            viewPager.setAdapter(fragmentPagerAdapter);

            tabLayoutType.setVisibility(View.VISIBLE);
            viewPager.setVisibility(View.VISIBLE);

            tabLayoutType.setupWithViewPager(viewPager);
            tabLayoutType.setVisibility(mPreferences.getBoolean(FgConst.PREF_SHOW_TRANSACTION_TYPE_TITLES, true) ?
                    View.VISIBLE : View.GONE);
            setTransactionTypeControls(transaction.getTransactionType());
            viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                }

                @Override
                public void onPageSelected(int position) {
                    if (position == 0) {
                        transaction.setTransactionType(mLastTrType);
                    } else {
                        transaction.setTransactionType(Transaction.TRANSACTION_TYPE_TRANSFER);
                    }
                    updateControlsState();
                    setTransactionTypeControls(transaction.getTransactionType());
                }

                @Override
                public void onPageScrollStateChanged(int state) {
                }
            });
        }
    }

    @Nullable
    private FragmentPayee getFragmentPayee() {
        FragmentPayee fragmentPayee;
        try {
            fragmentPayee = (FragmentPayee) fragmentPagerAdapter.getRegisteredFragment(0);
        } catch (Exception e) {
            fragmentPayee = null;
        }
        return fragmentPayee;
    }

    private void initViewPagerPayee() {
         viewPager.setVisibility(srcTransaction == null ? View.VISIBLE : View.GONE);
    }

    @Override
    public String getPayeeName() {
        return mPayeeName;
    }

    @Override
    public String getPayeeHint() {
        String hint;
        switch (transaction.getTransactionType()) {
            default:
            case Transaction.TRANSACTION_TYPE_EXPENSE:
                hint = getString(R.string.ent_payee);
                break;
            case Transaction.TRANSACTION_TYPE_INCOME:
                hint = getString(R.string.ent_payer);
                break;
        }
        return hint;
    }

    @Override
    public void onPayeeTextViewClick() {
        Intent intent = new Intent(ActivityEditTransaction.this.getApplicationContext(), ActivityList.class);
        intent.putExtra("showHomeButton", false);
        intent.putExtra("model", PayeesDAO.getInstance(getApplicationContext()).getPayeeByID(transaction.getPayeeID()));
        intent.putExtra("requestCode", REQUEST_CODE_SELECT_MODEL);
        ActivityEditTransaction.this.startActivityForResult(intent, REQUEST_CODE_SELECT_MODEL);
    }

    @Override
    public void onPayeeItemClick(long payeeID) {
        Payee payee = PayeesDAO.getInstance(this).getPayeeByID(payeeID);
        transaction.setPayeeID(payee.getID());
        mPayeeName = payee.getFullName();
        Category defCategory = PayeeManager.getDefCategory(payee, ActivityEditTransaction.this);
        if (defCategory.getID() >= 0) {
            transaction.setCategoryID(defCategory.getID());
            ActivityEditTransaction.this.initCategory();
        }
        amountEditor.requestFocus();
    }

    @Override
    public void onPayeeTyping(String payeeName) {
        mPayeeName = payeeName;
    }

    @Override
    public void onClearPayee() {
        transaction.setPayeeID(-1);
        setPayeeName("");
    }

    @Override
    public int getPayeeSelectionStyle() {
        return Integer.valueOf(mPreferences.getString("payee_selection_style", "0"));
    }

    @Override
    public boolean isShowKeyboard() {
        return transaction.getID() < 0;
    }

    @Override
    public NestedItemFullNameAdapter getPayeeNameAutocompleteAdapter() {
        PayeesDAO payeesDAO = PayeesDAO.getInstance(this);
        List<IAbstractModel> payees;
        List<AutocompleteItem> autocompleteItems = new ArrayList<>();
        try {
            payees = (List<IAbstractModel>) payeesDAO.getAllModels();
        } catch (Exception e) {
            payees = new ArrayList<>();
        }

        BaseNode tree = TreeManager.convertListToTree(payees, IAbstractModel.MODEL_TYPE_PAYEE);
        for (BaseNode node : tree.getFlatChildrenList()) {
            autocompleteItems.add(new AutocompleteItem(node.getModel().getID(), node.getModel().getFullName()));
        }

        return new NestedItemFullNameAdapter(this, android.R.layout.simple_spinner_dropdown_item, autocompleteItems);
    }

    private void initCategory() {
        edCategory.setText(CategoriesDAO.getInstance(this).getCategoryByID(transaction.getCategoryID()).getFullName());
        edCategory.setOnClickListener(v -> {
            Intent intent = new Intent(ActivityEditTransaction.this.getApplicationContext(), ActivityList.class);
            intent.putExtra("showHomeButton", false);
            intent.putExtra("model", CategoriesDAO.getInstance(getApplicationContext()).getCategoryByID(transaction.getCategoryID()));
            intent.putExtra("requestCode", REQUEST_CODE_SELECT_MODEL);
            ActivityEditTransaction.this.startActivityForResult(intent, REQUEST_CODE_SELECT_MODEL);
        });
    }

    private void initFTS() {

        mTextViewFN.setText(String.valueOf(transaction.getFN()));
        mTextViewFD.setText(String.valueOf(transaction.getFD()));
        mTextViewFP.setText(String.valueOf(transaction.getFP()));

        mTextViewFN.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                try {
                    transaction.setFN(Long.valueOf(charSequence.toString()));
                } catch (NumberFormatException e) {
                    transaction.setFN(0);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        mTextViewFD.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                try {
                    transaction.setFD(Long.valueOf(charSequence.toString()));
                } catch (NumberFormatException e) {
                    transaction.setFD(0);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        mTextViewFP.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                try {
                    transaction.setFP(Long.valueOf(charSequence.toString()));
                } catch (NumberFormatException e) {
                    transaction.setFP(0);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        mImageButtonScanQR.setOnClickListener(view -> {
            Intent intent = new Intent(ActivityEditTransaction.this, ActivityScanQR.class);
            intent.putExtra("transaction", transaction);
            startActivityForResult(intent, RequestCodes.REQUEST_CODE_SCAN_QR);
        });

        mImageButtonDownloadReceipt.setOnClickListener(view -> {
            getIntent().putExtra("load_products", true);
            initProductList();
        });
    }

    AdapterProducts mAdapterProducts;
    boolean mProductListExpanded = true;

    private void loadProducts() {
        if (mFtsHelper.isFtsCredentialsAvailiable(this)) {
            final RotateAnimation spinAnim = new RotateAnimation(360, 0f,
                    Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF, 0.5f);

            spinAnim.setInterpolator(new LinearInterpolator());
            spinAnim.setDuration(2000);
            spinAnim.setRepeatCount(Animation.INFINITE);
            mLayoutLoadingProducts.setVisibility(View.VISIBLE);
            mImageViewLoadingProducts.setVisibility(View.VISIBLE);
            mImageViewLoadingProducts.startAnimation(spinAnim);
            mTextViewLoadingProducts.setText(getString(R.string.ttl_loading_products));
            updateControlsState();
            IDownloadProductsListener downloadProductsListener = new IDownloadProductsListener() {
                @Override
                public void onDownload(List<ProductEntry> productEntries, String payeeName) {
                    spinAnim.cancel();
                    spinAnim.reset();
                    mLayoutLoadingProducts.setVisibility(View.GONE);
                    transaction.getProductEntries().clear();
                    transaction.getProductEntries().addAll(productEntries);
                    getIntent().removeExtra("load_products");
                    fillProductList();
                    if ((viewPager.getCurrentItem() == 0) && mPayeeName != null && mPayeeName.isEmpty()) {
                        setPayeeName(payeeName);
                    }
                    isErrorLoadingProducts = false;
                }

                @Override
                public void onAccepted() {
                    initProductList();
                }

                @Override
                public void onFailure(String errorMessage, boolean tryAgain) {
                    isErrorLoadingProducts = true;
                    getIntent().removeExtra("load_products");
                    spinAnim.cancel();
                    spinAnim.reset();
                    mImageViewLoadingProducts.setVisibility(View.GONE);
                    mTextViewLoadingProducts.setText(errorMessage);
                    updateControlsState();
                }
            };
            unsubscribeOnDestroy(mFtsHelper.downloadProductEntryList(transaction, downloadProductsListener));
        } else {
            mLayoutLoadingProducts.setVisibility(View.GONE);
            fillProductList();
            if (!mPreferences.getBoolean(FgConst.PREF_FTS_DO_NOT_SHOW_AGAIN, false)) {
                startActivityForResult(
                        new Intent(ActivityEditTransaction.this, ActivityFtsLogin.class),
                        RequestCodes.REQUEST_CODE_ENTER_FTS_LOGIN);
            }
        }
    }

    private void initProductList() {
        if (getIntent().getBooleanExtra("load_products", false)) {
            loadProducts();
        } else {
            fillProductList();
        }

        mRecyclerViewProductList.setVisibility(mProductListExpanded ? View.VISIBLE : View.GONE);

        mExpandableIndicator.setImageDrawable(mProductListExpanded ? getDrawable(R.drawable.ic_expand_more) : getDrawable(R.drawable.ic_expand_less));

        mExpandableIndicator.setOnClickListener(view -> {
            mProductListExpanded = !mProductListExpanded;
            initProductList();
        });

    }

    private void fillProductList() {
        isErrorLoadingProducts = false;
        mLayoutLoadingProducts.setVisibility(View.GONE);
        mAdapterProducts = new AdapterProducts(transaction, this, new AdapterProducts.IProductChangedListener() {
            @Override
            public void onProductChanged(int position, ProductEntry productEntry) {
                List<ProductEntry> entries = transaction.getProductEntries();
                if (position < entries.size()) {
                    entries.set(position, productEntry);
                } else {
                    entries.add(productEntry);
                }
                mAdapterProducts.notifyDataSetChanged();

                if (!isAmountEdited) {
                    BigDecimal sum = BigDecimal.ZERO;
                    for (ProductEntry entry : transaction.getProductEntries()) {
                        sum = sum.add(entry.getPrice().multiply(entry.getQuantity()));
                    }
                    transaction.setAmount(sum, sum.compareTo(BigDecimal.ZERO) <= 0 ?
                            Transaction.TRANSACTION_TYPE_EXPENSE : Transaction.TRANSACTION_TYPE_INCOME);
                    mDoNotChangeIsAmountEdited = true;
                    initAmountEditor();
                    mDoNotChangeIsAmountEdited = false;
                }
            }

            @Override
            public void onProductDeleted(int position) {
                transaction.getProductEntries().remove(position);
                mAdapterProducts.notifyDataSetChanged();
            }

            @Override
            public void onResidueMoved() {
                initAmountEditor();
                mAdapterProducts.notifyDataSetChanged();
            }

            @Override
            public void onProductSelected() {
                updateControlsState();
            }
        });
        mAdapterProducts.setHasStableIds(true);
        mRecyclerViewProductList.setAdapter(mAdapterProducts);
        mAdapterProducts.notifyDataSetChanged();
        updateControlsState();
    }

    private void initProject() {
        edProject.setText(ProjectsDAO.getInstance(this).getProjectByID(transaction.getProjectID()).getFullName());
        edProject.setOnClickListener(v -> {
            Intent intent = new Intent(ActivityEditTransaction.this.getApplicationContext(), ActivityList.class);
            intent.putExtra("showHomeButton", false);
            intent.putExtra("model", ProjectsDAO.getInstance(getApplicationContext()).getProjectByID(transaction.getProjectID()));
            intent.putExtra("requestCode", REQUEST_CODE_SELECT_MODEL);
            ActivityEditTransaction.this.startActivityForResult(intent, REQUEST_CODE_SELECT_MODEL);
        });
    }

    private void initSimpleDebt() {
        mTextViewSimpleDebt.setText("");
        mTextViewSimpleDebt.setText(TransactionManager.getSimpleDebt(transaction, this).getName());
        mTextViewSimpleDebt.setOnClickListener(v -> {
            Intent intent = new Intent(ActivityEditTransaction.this.getApplicationContext(), ActivityModelList.class);
            intent.putExtra("showHomeButton", false);
            intent.putExtra("model", SimpleDebtsDAO.getInstance(getApplicationContext()).getSimpleDebtByID(transaction.getSimpleDebtID()));
            intent.putExtra("cabbageID", AccountsDAO.getInstance(getApplicationContext()).getAccountByID(transaction.getAccountID()).getCabbageId());
            intent.putExtra("requestCode", REQUEST_CODE_SELECT_MODEL);
            ActivityEditTransaction.this.startActivityForResult(intent, REQUEST_CODE_SELECT_MODEL);
        });
    }

    private void initDepartment() {
        edDepartment.setText(DepartmentsDAO.getInstance(this).getDepartmentByID(transaction.getDepartmentID()).getFullName());
        edDepartment.setOnClickListener(v -> {
            Intent intent = new Intent(ActivityEditTransaction.this.getApplicationContext(), ActivityList.class);
            intent.putExtra("showHomeButton", false);
            intent.putExtra("model", DepartmentsDAO.getInstance(getApplicationContext()).getDepartmentByID(transaction.getDepartmentID()));
            intent.putExtra("requestCode", REQUEST_CODE_SELECT_MODEL);
            ActivityEditTransaction.this.startActivityForResult(intent, REQUEST_CODE_SELECT_MODEL);
        });
    }

    private void initAmountEditor() {
        amountEditor.setAmount(transaction.getAmount());
        amountEditor.setType(transaction.getTransactionType());
        amountEditor.setHint(getResources().getString(R.string.ent_amount));
        amountEditor.mOnAmountChangeListener = (newAmount, newType) -> {
            if (!mDoNotChangeIsAmountEdited) {
                isAmountEdited = true;
            }
            transaction.setAmount(newAmount, newType);
            destAmountEditor.setAmount(TransferManager.getDestAmount(transaction));
            if (newType != Transaction.TRANSACTION_TYPE_TRANSFER) {
                mLastTrType = newType;
            }
            if (srcTransaction != null) {
                srcAmount = srcTransaction.getAmount().subtract(transaction.getAmount());
                ActivityEditTransaction.this.initSrcAmount();
            }
            ActivityEditTransaction.this.updatePayeeHint();
            if (transaction.getProductEntries().size() > 0) {
//                    initProductList();
                mAdapterProducts.notifyDataSetChanged();
            }
        };

        if (getIntent().getBooleanExtra("focus_to_amount", false)) {
            amountEditor.setFocus();
        }
    }

    private void initDestAmountEditor() {
        destAmountEditor.setActivity(this);
        destAmountEditor.setAmount(transaction.getAmount().multiply(transaction.getExchangeRate()));
        destAmountEditor.setType(transaction.getTransactionType());
        destAmountEditor.setHint(getResources().getString(R.string.ent_dest_amount));
        onDestAmountChangeListener = new OnDestAmountChangeListener();
        destAmountEditor.mOnAmountChangeListener = onDestAmountChangeListener;
    }

    private void initExRate() {
        mImageButtonInvertExRate.setOnClickListener(view -> {
            isExRateInverted = !isExRateInverted;

            mPreferences.edit().putBoolean(String.format("%s/%s",
                    TransactionManager.getSrcCabbage(transaction, ActivityEditTransaction.this).getCode(),
                    TransactionManager.getDstCabbage(transaction, ActivityEditTransaction.this).getCode()),
                    isExRateInverted).apply();
            updateControlsState();
        });
        edExchangeRate.setText(String.valueOf(transaction.getExchangeRate().doubleValue()));
        if (onExRateTextChangedListener == null) {
            onExRateTextChangedListener = new OnExRateTextChangedListener();
            edExchangeRate.addTextChangedListener(onExRateTextChangedListener);
        }
    }

    private void initComment() {
        edComment.setText(transaction.getComment());
        edComment.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                transaction.setComment(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void initLocation() {
        if (srcTransaction != null) {
            layoutLocation.setVisibility(View.GONE);
        } else {
            edLocation.setText("");
//            imageButtonDeleteLocation.setImageDrawable(IconGenerator.getInstance(this).getDeleteIcon(this));

            if (transaction.getLocationID() >= 0) {
                edLocation.setText(LocationsDAO.getInstance(this).getLocationByID(transaction.getLocationID()).getFullName());
            } else {
                if (transaction.getLat() != 0 & transaction.getLon() != 0) {
                    String latSymbol = transaction.getLat() > 0 ? "N" : "S";
                    String lonSymbol = transaction.getLon() > 0 ? "E" : "W";
                    DecimalFormat df = new DecimalFormat("0.00000");
                    edLocation.setText(String.format("%s %s %s %s ±%sm",
                            latSymbol, df.format(transaction.getLat()), lonSymbol, df.format(transaction.getLon()), String.valueOf(transaction.getAccuracy())));
                }
            }

            edLocation.setOnClickListener(v -> {
                Intent intent = new Intent(ActivityEditTransaction.this.getApplicationContext(), ActivityList.class);
                intent.putExtra("showHomeButton", false);
                intent.putExtra("model", LocationsDAO.getInstance(getApplicationContext()).getLocationByID(transaction.getLocationID()));
                intent.putExtra("requestCode", REQUEST_CODE_SELECT_MODEL);
                ActivityEditTransaction.this.startActivityForResult(intent, REQUEST_CODE_SELECT_MODEL);
            });

            if (allowUpdateLocation) {
                ActivityEditTransactionPermissionsDispatcher.startDetectCoordsWithPermissionCheck(this);
            }
        }
    }
    //</editor-fold>

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // NOTE: delegate the permission handling to generated method
        ActivityEditTransactionPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    @OnClick({R.id.imageButtonDeleteLocation, R.id.imageButtonDeleteCategory, R.id.imageButtonDeleteDebt,
            R.id.imageButtonDeleteProject, R.id.imageButtonDeleteDepartment})
    void onTrashButtonClick(View v) {
        switch (v.getId()) {
            case R.id.imageButtonDeleteLocation:
                transaction.setLocationID(-1);
                transaction.setLat(0);
                transaction.setLon(0);
                allowUpdateLocation = false;
                initLocation();
                break;
            case R.id.imageButtonDeleteProject:
                transaction.setProjectID(-1);
                initProductList();
                initProject();
                break;
            case R.id.imageButtonDeleteDebt:
                transaction.setSimpleDebtID(-1);
                initSimpleDebt();
                break;
            case R.id.imageButtonDeleteDepartment:
                transaction.setDepartmentID(-1);
                initDepartment();
                break;
            case R.id.imageButtonDeleteCategory:
                transaction.setCategoryID(-1);
                initProductList();
                initCategory();
                break;
        }
    }

    private void updateEdLocation() {
        if (transaction.getLocationID() < 0 & forceUpdateLocation) {
            String latSymbol = lat > 0 ? "N" : "S";
            String lonSymbol = lon > 0 ? "E" : "W";
            DecimalFormat df = new DecimalFormat("0.00000");
            edLocation.setText(String.format("%s\n%s %s %s %s\n±%sm %s",
                    getString(R.string.ent_current_location), latSymbol, df.format(lat), lonSymbol, df.format(lon),
                    String.valueOf(accuracy), provider));
        } else {
            Location location = TransactionManager.getLocation(transaction, this);
            if (location.getID() > 0) {
                edLocation.setText(location.getName());
            }
        }
    }

    private void removeUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (locationManager != null) {
                locationManager.removeUpdates(locationListener);
            }
        }
    }

    //<editor-fold desc="Permission dispatcher" defaultstate="collapsed">
    @NeedsPermission({Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION})
    void startDetectCoords() {
        if (transaction.getLocationID() < 0 & allowUpdateLocation) {
            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

            if (locationManager != null && locationManager.getAllProviders().contains(LocationManager.NETWORK_PROVIDER))
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
                }

            if (locationManager != null && locationManager.getAllProviders().contains(LocationManager.GPS_PROVIDER))
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                }
        }
    }

    @OnShowRationale({Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION})
    void showRationaleForContact(PermissionRequest request) {
        // NOTE: Show a rationale to explain why the permission is needed, e.g. with a dialog.
        // Call proceed() or cancel() on the provided PermissionRequest to continue or abort
        showRationaleDialog(R.string.msg_permission_location_rationale, request);
    }

    @OnPermissionDenied({Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION})
    void onLocationDenied() {
        // NOTE: Deal with a denied permission, e.g. by showing specific UI
        // or disabling certain functionality
        Toast.makeText(this, R.string.msg_permission_location_denied, Toast.LENGTH_SHORT).show();
    }

    @OnNeverAskAgain({Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION})
    void onLocationNeverAskAgain() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                & ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            mPreferences.edit().putBoolean("detect_locations", false).apply();
        } else {
            allowUpdateLocation = true;
        }
        if (!allowUpdateLocation) {
            Toast.makeText(this, R.string.msg_permission_location_never_askagain, Toast.LENGTH_SHORT).show();
        }
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        getIntent().removeExtra("focus_to_category");
        if (resultCode == RESULT_OK && requestCode == REQUEST_CODE_SELECT_MODEL && data != null) {
            IAbstractModel model = data.getParcelableExtra("model");
            switch (model.getModelType()) {
                case IAbstractModel.MODEL_TYPE_ACCOUNT:
                    Account account = (Account) model;
                    if (!data.getBooleanExtra("destAccount", false)) {
                        transaction.setAccountID(account.getID());
                        viewPager.requestFocus();
                        Cabbage cabbage = AccountManager.getCabbage(account, ActivityEditTransaction.this);
                        textViewAccount.setText(String.format("%s (%s)", account.getName(), cabbage.getSimbol()));
                        amountEditor.setScale(cabbage.getDecimalCount());
                    } else {
                        transaction.setDestAccountID(account.getID());
                        transaction.setPayeeID(-1);
                        findViewById(R.id.edittext_amount).requestFocus();
                    }
                    break;
                case IAbstractModel.MODEL_TYPE_CATEGORY:
                    transaction.setCategoryID(model.getID());
                    initCategory();
                    amountEditor.setFocus();
                    break;
                case IAbstractModel.MODEL_TYPE_PAYEE:
                    Payee payee = (Payee) model;
                    transaction.setPayeeID(payee.getID());
                    mPayeeName = payee.getFullName();
                    if (transaction.getPayeeID() >= 0) {
                        Category defCategory = PayeeManager.getDefCategory(payee, ActivityEditTransaction.this);
                        if (defCategory.getID() >= 0) {
                            transaction.setCategoryID(defCategory.getID());
                            ActivityEditTransaction.this.initCategory();
                        }
                        amountEditor.requestFocus();
                    }
                    break;
                case IAbstractModel.MODEL_TYPE_PROJECT:
                    transaction.setProjectID(model.getID());
                    initProject();
                    break;
                case IAbstractModel.MODEL_TYPE_SIMPLEDEBT:
                    transaction.setSimpleDebtID(model.getID());
                    initSimpleDebt();
                    break;
                case IAbstractModel.MODEL_TYPE_DEPARTMENT:
                    transaction.setDepartmentID(model.getID());
                    initDepartment();
                    break;
                case IAbstractModel.MODEL_TYPE_LOCATION:
                    transaction.setLocationID(model.getID());
                    forceUpdateLocation = transaction.getLocationID() < 0;
                    allowUpdateLocation = true;
                    initLocation();
                    break;
            }
        } else if (resultCode == RESULT_OK && requestCode == REQUEST_CODE_SELECT_MODEL_FOR_PRODUCT && data != null) {
            IAbstractModel model = data.getParcelableExtra("model");
            switch (model.getModelType()) {
                case IAbstractModel.MODEL_TYPE_CATEGORY:
                    for (ProductEntry entry : transaction.getProductEntries()) {
                        if (entry.isSelected()) {
                            entry.setCategoryID(model.getID());
                        }
                    }
                    break;
                case IAbstractModel.MODEL_TYPE_PROJECT:
                    for (ProductEntry entry : transaction.getProductEntries()) {
                        if (entry.isSelected()) {
                            entry.setProjectID(model.getID());
                        }
                    }
                    break;
            }
            for (ProductEntry entry : transaction.getProductEntries()) {
                entry.setSelected(false);
            }
            mAdapterProducts.notifyDataSetChanged();
            mFabMenuController.forceCloseFABMenu();
            updateControlsState();
        } else if (resultCode == RESULT_OK && requestCode == RequestCodes.REQUEST_CODE_SCAN_QR) {
            transaction = data.getParcelableExtra("transaction");
            getIntent().putExtra("load_products", true);
            initUI();
        } else if (requestCode == RequestCodes.REQUEST_CODE_ENTER_FTS_LOGIN) {
            if (resultCode != RESULT_OK) {
                getIntent().removeExtra("load_products");
            }
        } else {
            switch (requestCode) {
                case IAbstractModel.MODEL_TYPE_LOCATION:
                    initLocation();
                    break;
            }
        }
//        setFieldsVisibility();
    }

    //<editor-fold desc="Date & Time pickers" defaultstate="collapsed">
//    @OnClick(R.id.editTextDate)
    public void onDateClick(View view) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(transaction.getDateTime());
        DatePickerDialog dpd = new DatePickerDialog(this, (datePicker, year, monthOfYear, dayOfMonth) -> {
            Calendar calendar1 = Calendar.getInstance();
            calendar1.setTime(transaction.getDateTime());
            calendar1.set(year, monthOfYear, dayOfMonth);
            transaction.setDateTime(calendar1.getTime());
            initDateTimeButtons();
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        dpd.getDatePicker().setFirstDayOfWeek(getFirstDayOfWeek(view.getContext()));
        dpd.show();
    }

    public void onTimeClick(View view) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(transaction.getDateTime());
        new TimePickerDialog(this,
                (timePicker, hourOfDay, minute) -> {
                    Calendar calendar1 = Calendar.getInstance();
                    calendar1.setTime(transaction.getDateTime());
                    calendar1.set(calendar1.get(Calendar.YEAR), calendar1.get(Calendar.MONTH), calendar1.get(Calendar.DAY_OF_MONTH), hourOfDay, minute);
                    transaction.setDateTime(calendar1.getTime());
                    initDateTimeButtons();
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                DateTimeFormatter.is24(this)
        ).show();
    }
    //</editor-fold>

    @Override
    public boolean showHelp() {
        super.showHelp();

        MaterialShowcaseView.resetSingleUse(this, SHOWCASE_ID);

        ShowcaseConfig config = new ShowcaseConfig();
        config.setDelay(500); // half second between each showcase view

        MaterialShowcaseSequence sequence = new MaterialShowcaseSequence(this, SHOWCASE_ID);

        sequence.setConfig(config);

        String gotIt = getResources().getString(R.string.act_next);
        //+Вводный экран
        //+дата/время Нажмите, чтобы изменить дату или время транзакции
        //Счет Выберите из списка счет
        //Получатель/счет назначения
        //Категория
        //Сумма
        //Сумма в валюте назначения
        //Курс
        //Комментарий

        sequence.addSequenceItem(new MaterialShowcaseView.Builder(this)
                .setTarget(new View(this)).setDismissText(gotIt)
                .setContentText(getString(R.string.hlp_tr_editor_intro))
                .setMaskColour(ContextCompat.getColor(this, R.color.ColorPrimaryTransparent))
                .build());

        sequence.addSequenceItem(new MaterialShowcaseView.Builder(this)
                .setTarget(edDate).setDismissText(gotIt).withRectangleShape()
                .setMaskColour(ContextCompat.getColor(this, R.color.ColorPrimaryTransparent))
                .setContentText(R.string.hlp_tr_editor_date)
                .build());

        sequence.addSequenceItem(new MaterialShowcaseView.Builder(this)
                .setTarget(edTime).setDismissText(gotIt).withRectangleShape()
                .setMaskColour(ContextCompat.getColor(this, R.color.ColorPrimaryTransparent))
                .setContentText(R.string.hlp_tr_editor_time)
                .build());

        String accountHint = "";
        switch (transaction.getTransactionType()) {
            case Transaction.TRANSACTION_TYPE_INCOME:
                accountHint = getString(R.string.hlp_tr_editor_account_income);
                break;
            case Transaction.TRANSACTION_TYPE_EXPENSE:
            case Transaction.TRANSACTION_TYPE_TRANSFER:
                accountHint = getString(R.string.hlp_tr_editor_account_outcome_or_transfer);
                break;
        }
        sequence.addSequenceItem(new MaterialShowcaseView.Builder(this)
                .setTarget(textViewAccount).setDismissText(gotIt).withRectangleShape()
                .setMaskColour(ContextCompat.getColor(this, R.color.ColorPrimaryTransparent))
                .setContentText(accountHint)
                .build());

        String payeeHint = "";
        switch (transaction.getTransactionType()) {
            case Transaction.TRANSACTION_TYPE_INCOME:
                payeeHint = getString(R.string.hlp_tr_editor_payee_income);
                break;
            case Transaction.TRANSACTION_TYPE_EXPENSE:
                payeeHint = getString(R.string.hlp_tr_editor_payee_outcome);
                break;
            case Transaction.TRANSACTION_TYPE_TRANSFER:
                payeeHint = getString(R.string.hlp_tr_editor_dest_account);
                break;
        }
        sequence.addSequenceItem(new MaterialShowcaseView.Builder(this)
                .setTarget(viewPager).setDismissText(gotIt).withRectangleShape()
                .setMaskColour(ContextCompat.getColor(this, R.color.ColorPrimaryTransparent))
                .setContentText(payeeHint)
                .build());

        sequence.addSequenceItem(new MaterialShowcaseView.Builder(this)
                .setTarget(edCategory).setDismissText(gotIt).withRectangleShape()
                .setMaskColour(ContextCompat.getColor(this, R.color.ColorPrimaryTransparent))
                .setContentText(getString(R.string.hlp_tr_editor_category))
                .build());

        sequence.addSequenceItem(new MaterialShowcaseView.Builder(this)
                .setTarget(amountEditor).setDismissText(gotIt).withRectangleShape()
                .setMaskColour(ContextCompat.getColor(this, R.color.ColorPrimaryTransparent))
                .setContentText(getString(R.string.hlp_tr_editor_amount))
                .build());

        switch (transaction.getTransactionType()) {
            case Transaction.TRANSACTION_TYPE_INCOME:
            case Transaction.TRANSACTION_TYPE_EXPENSE:
                sequence.addSequenceItem(new MaterialShowcaseView.Builder(this)
                        .setTarget(amountEditor.btnAmountSign).setDismissText(gotIt).withRectangleShape()
                        .setMaskColour(ContextCompat.getColor(this, R.color.ColorPrimaryTransparent))
                        .setContentText(getString(R.string.hlp_tr_editor_amount_sign))
                        .build());
                break;
        }

        //Если мы в режиме перевода между счетами с разными валютами, показываем хинт для суммы в валюте
        //счета назначения и курса
        if (destAmountEditor.getVisibility() == View.VISIBLE) {
            sequence.addSequenceItem(new MaterialShowcaseView.Builder(this)
                    .setTarget(destAmountEditor).setDismissText(gotIt).withRectangleShape()
                    .setMaskColour(ContextCompat.getColor(this, R.color.ColorPrimaryTransparent))
                    .setContentText(getString(R.string.hlp_tr_editor_amount_dest_account))
                    .build());

            sequence.addSequenceItem(new MaterialShowcaseView.Builder(this)
                    .setTarget(edExchangeRate).setDismissText(gotIt).withRectangleShape()
                    .setMaskColour(ContextCompat.getColor(this, R.color.ColorPrimaryTransparent))
                    .setContentText(getString(R.string.hlp_tr_editor_exchange_rate))
                    .build());
        }

        sequence.start();

        return true;
    }

    private void updatePayeeHint() {
        FragmentPayee fragmentPayee = getFragmentPayee();
        if (fragmentPayee != null) {
            fragmentPayee.setHint(getPayeeHint());
        }
    }

    private void setPayeeName(String payeeName) {
        mPayeeName = payeeName;
        if (getFragmentPayee() != null) {
            getFragmentPayee().setPayeeName(payeeName);
        }
    }

    private class OnShowCorrectingDialogCancelListener implements DialogInterface.OnClickListener {
        final Activity mActivity;

        OnShowCorrectingDialogCancelListener(Activity mActivity) {
            this.mActivity = mActivity;
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            dialog.cancel();
            mActivity.finish();
        }
    }

    private class OnShowCorrectingDialogOkListener implements DialogInterface.OnClickListener {
        final BigDecimal mAmount;
        final Activity mActivity;

        OnShowCorrectingDialogOkListener(BigDecimal mAmount, Activity mActivity) {
            this.mAmount = mAmount;
            this.mActivity = mActivity;
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            Transaction cortr = new Transaction(PrefUtils.getDefDepID(ActivityEditTransaction.this));
            cortr.setAmount(mAmount, mAmount.compareTo(BigDecimal.ZERO));
            cortr.setAccountID(transaction.getAccountID());
            Intent intent = new Intent(mActivity, ActivityEditTransaction.class);
            intent.putExtra("transaction", cortr);
            startActivityForResult(intent, RequestCodes.REQUEST_CODE_EDIT_TRANSACTION);
            mActivity.finish();
        }
    }

    private class OnShowDebtPercentDialogOkListener implements DialogInterface.OnClickListener {
        final Credit mCredit;
        final int mDebtAction;
        final Activity mActivity;
        final Transaction mTransaction;

        OnShowDebtPercentDialogOkListener(Credit credit, int debtAction, Transaction transaction, Activity activity) {
            mCredit = credit;
            mDebtAction = debtAction;
            mActivity = activity;
            mTransaction = transaction;
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            Transaction percents = new Transaction(PrefUtils.getDefDepID(ActivityEditTransaction.this));
            percents.setDateTime(new Date(mTransaction.getDateTime().getTime() + 1));

            switch (mDebtAction) {
                case Credit.DEBT_ACTION_REPAY:
                    percents.setPayeeID(mCredit.getPayeeID());
                    percents.setCategoryID(mCredit.getCategoryID());
                    percents.setTransactionType(Transaction.TRANSACTION_TYPE_EXPENSE);
                    break;
                case Credit.DEBT_ACTION_TAKE:
                    percents.setPayeeID(mCredit.getPayeeID());
                    percents.setCategoryID(mCredit.getCategoryID());
                    percents.setTransactionType(Transaction.TRANSACTION_TYPE_INCOME);
                    break;
            }

            Intent intent = new Intent(mActivity, ActivityEditTransaction.class);
            intent.putExtra("transaction", percents);
            startActivityForResult(intent, RequestCodes.REQUEST_CODE_EDIT_TRANSACTION);
            mActivity.finish();
        }
    }

    private class OnDestAmountChangeListener implements AmountEditor.OnAmountChangeListener {
        @Override
        public void OnAmountChange(BigDecimal newAmount, int newType) {
            transaction.setExchangeRate(TransferManager.getExRate(transaction, newAmount));
            edExchangeRate.removeTextChangedListener(onExRateTextChangedListener);
            BigDecimal visibleExRate;
            if (!isExRateInverted) {
                visibleExRate = transaction.getExchangeRate();
            } else {
                visibleExRate = BigDecimal.ONE.divide(transaction.getExchangeRate(), 5, RoundingMode.HALF_UP);
            }
            edExchangeRate.setText(String.valueOf(visibleExRate.doubleValue()));
            edExchangeRate.addTextChangedListener(onExRateTextChangedListener);
        }
    }

    private class OnExRateTextChangedListener implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            try {
                BigDecimal rate;
                DecimalFormatSymbols symbols = new DecimalFormatSymbols();
//                symbols.setGroupingSeparator(',');
                symbols.setDecimalSeparator('.');
                DecimalFormat df = new DecimalFormat();
                df.setDecimalFormatSymbols(symbols);
                df.setParseBigDecimal(true);
                try {
                    rate = (BigDecimal) df.parse(s.toString());
                } catch (ParseException e) {
                    rate = BigDecimal.ONE;
                }
                if (isExRateInverted) {
                    if (rate.compareTo(BigDecimal.ZERO) != 0) {
                        rate = BigDecimal.ONE.divide((rate), 5, RoundingMode.HALF_UP);
                    } else {
                        rate = BigDecimal.ONE;
                    }
                }
                transaction.setExchangeRate(rate);
                destAmountEditor.mOnAmountChangeListener = null;
                destAmountEditor.setAmount(TransferManager.getDestAmount(transaction));
                destAmountEditor.mOnAmountChangeListener = onDestAmountChangeListener;
            } catch (NumberFormatException nfe) {
                //Todo уведомлять пользователя о том, что число некорректно
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    }

    private void initFabMenu() {
        mFabMenuController = new FabMenuController(mFabMenuButtonRoot, mFabBGLayout, this,
                mFabSetCategoryLayout, mFabSetProjectLayout, mFabDeleteSelectedLayout, mFabUnselectAllLayout, mFabSelectAllLayout);
        FabMenuSelectionItemClickListener clickListener = new FabMenuSelectionItemClickListener();
        mFabDeleteSelected.setOnClickListener(clickListener);
        mFabUnselectAll.setOnClickListener(clickListener);
        mFabSelectAll.setOnClickListener(clickListener);
        mFabSetCategory.setOnClickListener(clickListener);
        mFabSetProject.setOnClickListener(clickListener);

    }

    private class FabMenuSelectionItemClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.fabSelectAll: {
                    for (ProductEntry entry : transaction.getProductEntries()) {
                        entry.setSelected(true);
                    }
                    mAdapterProducts.notifyDataSetChanged();
                    mFabMenuController.closeFABMenu();
                    updateControlsState();
                    break;
                }
                case R.id.fabUnselectAll: {
                    for (ProductEntry entry : transaction.getProductEntries()) {
                        entry.setSelected(false);
                    }
                    mAdapterProducts.notifyDataSetChanged();
                    mFabMenuController.closeFABMenu();
                    updateControlsState();
                    break;
                }
                case R.id.fabSetCategory: {
                    Intent intent = new Intent(ActivityEditTransaction.this.getApplicationContext(), ActivityList.class);
                    intent.putExtra("showHomeButton", false);
                    intent.putExtra("model", CategoriesDAO.getInstance(getApplicationContext()).getCategoryByID(transaction.getCategoryID()));
                    intent.putExtra("requestCode", REQUEST_CODE_SELECT_MODEL_FOR_PRODUCT);
                    ActivityEditTransaction.this.startActivityForResult(intent, REQUEST_CODE_SELECT_MODEL_FOR_PRODUCT);
                    break;
                }
                case R.id.fabSetProject: {
                    Intent intent = new Intent(ActivityEditTransaction.this.getApplicationContext(), ActivityList.class);
                    intent.putExtra("showHomeButton", false);
                    intent.putExtra("model", ProjectsDAO.getInstance(getApplicationContext()).getProjectByID(transaction.getProjectID()));
                    intent.putExtra("requestCode", REQUEST_CODE_SELECT_MODEL_FOR_PRODUCT);
                    ActivityEditTransaction.this.startActivityForResult(intent, REQUEST_CODE_SELECT_MODEL_FOR_PRODUCT);
                    break;
                }
                case R.id.fabDeleteSelected: {
                    for (int i = transaction.getProductEntries().size() - 1; i >= 0; i--) {
                        if (transaction.getProductEntries().get(i).isSelected()) {
                            transaction.getProductEntries().remove(i);
                        }
                    }
                    mAdapterProducts.notifyDataSetChanged();
                    mFabMenuController.closeFABMenu();
                    updateControlsState();
                    break;
                }
            }
        }
    }

    private class ActionModeCallback implements ActionMode.Callback {
        private static final int MENU_ID_SET_MARKER = 0;
        final Activity mActivity;

        ActionModeCallback(Activity mActivity) {
            this.mActivity = mActivity;
        }

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            menu.add(0, MENU_ID_SET_MARKER, MENU_ID_SET_MARKER, R.string.act_set_marker).setShowAsAction(MenuItem.SHOW_AS_ACTION_WITH_TEXT | MenuItem.SHOW_AS_ACTION_ALWAYS);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return true;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case MENU_ID_SET_MARKER: {
                    showAddMarkerDialog();
                }
            }
            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {

        }
    }

    private class OnMarkersDialogOkListener implements DialogInterface.OnClickListener {
        private final String selectedText;

        OnMarkersDialogOkListener(String selectedText) {
            this.selectedText = selectedText;
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            if (sms == null) {
                return;
            }

            ListView lw = ((AlertDialog) dialog).getListView();
            SmsMarkerManager.SmsMarkerType checkedItem = (SmsMarkerManager.SmsMarkerType) lw.getAdapter().getItem(which);

            createSmsMarker(checkedItem.id, selectedText);
        }
    }
}
