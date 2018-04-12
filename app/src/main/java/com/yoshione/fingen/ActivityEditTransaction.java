package com.yoshione.fingen;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.TabLayout;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
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
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;
import com.yoshione.fingen.adapter.AdapterProducts;
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
import com.yoshione.fingen.fts.models.FtsResponse;
import com.yoshione.fingen.interfaces.IAbstractModel;
import com.yoshione.fingen.managers.AccountManager;
import com.yoshione.fingen.managers.PayeeManager;
import com.yoshione.fingen.managers.SmsMarkerManager;
import com.yoshione.fingen.managers.TransactionManager;
import com.yoshione.fingen.model.Account;
import com.yoshione.fingen.model.Cabbage;
import com.yoshione.fingen.model.Category;
import com.yoshione.fingen.model.Credit;
import com.yoshione.fingen.model.Location;
import com.yoshione.fingen.model.Payee;
import com.yoshione.fingen.model.ProductEntry;
import com.yoshione.fingen.model.Sms;
import com.yoshione.fingen.model.SmsMarker;
import com.yoshione.fingen.model.Template;
import com.yoshione.fingen.model.Transaction;
import com.yoshione.fingen.utils.CabbageFormatter;
import com.yoshione.fingen.utils.DateTimeFormatter;
import com.yoshione.fingen.utils.PrefUtils;
import com.yoshione.fingen.utils.RequestCodes;
import com.yoshione.fingen.utils.SmsParser;
import com.yoshione.fingen.utils.SwipeDetector;
import com.yoshione.fingen.widgets.AmountEditor;
import com.yoshione.fingen.widgets.MyViewPager;
import com.yoshione.fingen.widgets.SmsEditText;
import com.yoshione.fingen.widgets.ToolbarActivity;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

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
import retrofit2.Call;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;
import uk.co.deanwild.materialshowcaseview.ShowcaseConfig;

import static com.yoshione.fingen.utils.RequestCodes.REQUEST_CODE_SELECT_MODEL;
import static com.yoshione.fingen.utils.RequestCodes.REQUEST_CODE_TUNE_EDITOR;


/**
 * Created by slv on 20.08.2015.
 * a
 */
@RuntimePermissions
public class ActivityEditTransaction extends ToolbarActivity /*implements TimePickerDialog.OnTimeSetListener,
        DatePickerDialog.OnDateSetListener*/ {

    //<editor-fold desc="Static declarations" defaultstate="collapsed">
    private static final int FRAGMENT_PAYEE = 0;
    private static final int FRAGMENT_DEST_ACCOUNT = 1;
    private static final int FRAGMENTS_COUNT = 2;
    private static final String SHOWCASE_ID = "Edit transaction showcase";
    //</editor-fold>
    private final List<Fragment> fragments = new ArrayList<>();
    //<editor-fold desc="BindView" defaultstate="collapsed">
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
    //</editor-fold>
    FragmentDestAccount fragmentDestAccount;
    @BindView(R.id.imageViewLoadingProducts)
    ImageView mImageViewLoadingProducts;
    @BindView(R.id.textViewLoadingProducts)
    TextView mTextViewLoadingProducts;
    @BindView(R.id.layoutLoadingProducts)
    ConstraintLayout mLayoutLoadingProducts;
    private OnDestAmountChangeListener onDestAmountChangeListener;
    private OnExRateTextChangedListener onExRateTextChangedListener;
    private FragmentPayee fragmentPayee;
    private String mPayeeName;
    private SharedPreferences preferences;
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
    private Call<FtsResponse> mFtsResponseCall;
    private final LocationListener locationListener = new LocationListener() {
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
    private boolean allowUpdateLocation;
    private boolean mIsBtnMorePressed = false;

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_edit_transaction;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!BuildConfig.DEBUG) {
            if (!Fabric.isInitialized()) {
                Fabric.with(this, new Crashlytics());
            }
        }

        ButterKnife.bind(this);

        preferences = PreferenceManager.getDefaultSharedPreferences(this);

        //получаем объекты
        if (savedInstanceState == null) {
            initObjects();
        }
        amountEditor.setActivity(this);

        if (srcTransaction != null) {
            tabLayoutType.setVisibility(View.GONE);
            viewPager.setVisibility(View.GONE);
        } else {
            FragmentPagerAdapter fragmentPagerAdapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
                @Override
                public int getCount() {
                    return FRAGMENTS_COUNT;
                }

                @Override
                public Fragment getItem(final int position) {
                    return fragments.get(position);
                }

                @Override
                public CharSequence getPageTitle(final int position) {
                    switch (position) {
                        case FRAGMENT_PAYEE:
                            return getString(R.string.ent_payment);
                        case FRAGMENT_DEST_ACCOUNT:
                            return getString(R.string.ent_transfer);
                        default:
                            return null;
                    }
                }
            };
            viewPager.setAdapter(fragmentPagerAdapter);
            fragmentPayee = new FragmentPayee();
            fragmentDestAccount = new FragmentDestAccount();

            fragments.add(FRAGMENT_PAYEE, fragmentPayee);
            fragments.add(FRAGMENT_DEST_ACCOUNT, fragmentDestAccount);
        }

        mRecyclerViewProductList.setLayoutManager(new LinearLayoutManager(this) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        });
    }

    @Override
    public void onDestroy() {
        if (mFtsResponseCall != null) {
            mFtsResponseCall.cancel();
        }
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (getIntent().getBooleanExtra("focus_to_amount", false)) {
            amountEditor.setFocus();
        }

        if (allowUpdateLocation) {
            startDetectCoords();
        }

        forceUpdateLocation = transaction.getID() < 0;
        allowUpdateLocation = preferences.getBoolean("detect_locations", false) & forceUpdateLocation & (srcTransaction == null);

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
    }

    @Override
    protected void onPause() {
        removeUpdates();
        super.onPause();
    }

    private void initUI() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(ContextCompat.getDrawable(this, R.drawable.ic_close_white));
        }

        initSrcAmount();

        initTemplateName();

        initDateTimeButtons();

        initAccount();

        initViewPagerPayee();

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

        updateControlsState();
    }

    public Transaction getTransaction() {
        return transaction;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
//        getMenuInflater().inflate(R.menu.menu_edit_transaction, menu);
        menu.findItem(R.id.action_go_home).setVisible(false);
        menu.findItem(R.id.action_show_help).setVisible(true);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_tune_editor :
                Intent intent = new Intent(this, ActivityEditorConstructor.class);
                startActivityForResult(intent, REQUEST_CODE_TUNE_EDITOR);
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
                fragmentPayee.setAutocompleteAdapter();
            }
        }

        return transaction.getPayeeID() >= 0;
    }

    private void updatePayeeWithDefCategory() {
        if (transaction.getPayeeID() < 0) return;
        Category category = TransactionManager.getCategory(transaction, this);
        Payee payee = TransactionManager.getPayee(transaction, this);
        Category defCategory = PayeeManager.getDefCategory(payee, this);
        if (defCategory.getID() != category.getID()) {
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

    @OnClick(R.id.buttonSaveTransaction)
    public void onSaveClick() {

        switch (viewPager.getCurrentItem()) {
            case 0:
                if (transaction.getDestAccountID() > 0) {
                    transaction.setDestAccountID(-1);
                    initViewPagerPayee();
                }
                break;
            case 1:
                if (transaction.getPayeeID() >= 0) {
                    transaction.setPayeeID(-1);
                    initViewPagerPayee();
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
            if (transaction.isValid()) {
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
            } else {
                if (transaction.getAccountID() < 0) {
                    textInputLayoutAccount.setError(getString(R.string.err_specify_account));
                }
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
        layoutProject.setVisibility(transaction.getProjectID() >= 0 | mIsBtnMorePressed ? View.VISIBLE : View.GONE);
        layoutSimpleDebt.setVisibility(transaction.getSimpleDebtID() >= 0 | mIsBtnMorePressed ? View.VISIBLE : View.GONE);
        layoutDepartment.setVisibility(transaction.getDepartmentID() >= 0 | mIsBtnMorePressed ? View.VISIBLE : View.GONE);
        layoutLocation.setVisibility(transaction.getLocationID() >= 0 | mIsBtnMorePressed ? View.VISIBLE : View.GONE);
        mLayoutProductList.setVisibility(transaction.getProductEntries().size() > 0 | mIsBtnMorePressed | getIntent().getBooleanExtra("load_products", false) ? View.VISIBLE : View.GONE);

        boolean scanQR = preferences.getBoolean(FgConst.PREF_ENABLE_SCAN_QR, true);

        mLayoutFTS.setVisibility(
                (transaction.getFN() > 0 | transaction.getFD() > 0 | transaction.getFP() > 0 | mIsBtnMorePressed) & scanQR ? View.VISIBLE : View.GONE);

        if (transaction.getTransactionType() == Transaction.TRANSACTION_TYPE_TRANSFER) {
            Account srcAccount = TransactionManager.getSrcAccount(transaction, this);
            Account destAccount = TransactionManager.getDestAccount(transaction, this);

            if ((srcAccount.getCabbageId() != destAccount.getCabbageId()) & (destAccount.getID() >= 0)) {
                mTextInputLayoutExchangeRate.setVisibility(View.VISIBLE);
            } else {
                mTextInputLayoutExchangeRate.setVisibility(View.GONE);
            }
            Cabbage destCabbage = AccountManager.getCabbage(destAccount, this);
            destAmountEditor.setScale(destCabbage.getDecimalCount());
        } else {
            mTextInputLayoutExchangeRate.setVisibility(View.GONE);
        }
        destAmountEditor.setVisibility(mTextInputLayoutExchangeRate.getVisibility());

        amountEditor.setType(transaction.getTransactionType());

        destAmountEditor.setType(transaction.getTransactionType());

        updatePayeeHint();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getLayoutTitle());
        }
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
            imageButtonAddMarker.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ActivityEditTransaction.this.showAddMarkerDialog();
                }
            });

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
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });


        String selectedText = sms.getmBody().subSequence(Math.max(0, Math.min(selStart, selEnd)),
                Math.max(0, Math.max(selStart, selEnd))).toString();
        builderSingle.setAdapter(arrayAdapter, new OnMarkersDialogOkListener(selectedText));
        builderSingle.show();
    }

    private void createSmsMarker(final int markerType, final String selectedText) {
        SmsMarker smsMarker = null;

        switch (markerType) {
            case SmsParser.MARKER_TYPE_PAYEE: {
                if (transaction.getPayeeID() < 0) {
                    if (!checkPayeeAndCreateIfNecessary(true)) {
                        PayeeManager.ShowSelectPayeeDialog(this, new PayeeManager.OnSelectPayeeListener() {
                            @Override
                            public void OnSelectPayee(Payee selectedPayee) {
                                transaction.setPayeeID(selectedPayee.getID());
                                setPayeeName(selectedPayee.getName());
                                ActivityEditTransaction.this.createSmsMarker(markerType, selectedText);
                            }
                        });
                        return;
                    }
                }
                smsMarker = new SmsMarker(-1, SmsParser.MARKER_TYPE_PAYEE, String.valueOf(transaction.getPayeeID()), selectedText);
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

    private void initObjects() {
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

            Account account = TransactionManager.getSrcAccount(transaction, this);
            if (account.getID() < 0) {
                account = accountsDAO.getAccountByID(PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getLong("last_account_id", -1));
                transaction.setAccountID(account.getID());
            }
            Cabbage cabbage = AccountManager.getCabbage(account, this);
            if (account.getID() < 0) {
                textViewAccount.setText("");
            } else {
                textViewAccount.setText(String.format("%s (%s)", account.getName(), cabbage.getSimbol()));
            }
//            textViewAccountCabbage.setText(cabbage.getCode());

            amountEditor.setScale(cabbage.getDecimalCount());

            textViewAccount.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(ActivityEditTransaction.this, ActivityAccounts.class);
                    intent.putExtra("showHomeButton", false);
                    intent.putExtra("model", new Account());
                    intent.putExtra("destAccount", false);
                    ActivityEditTransaction.this.startActivityForResult(intent, REQUEST_CODE_SELECT_MODEL);
                }
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
//        viewPager.setCurrentItem((transactionType == Transaction.TRANSACTION_TYPE_TRANSFER) ? 1 : 0);
    }

    private void initViewPagerPayee() {
        if (srcTransaction != null) {
            viewPager.setVisibility(View.GONE);
        } else {
            tabLayoutType.setupWithViewPager(viewPager);
            tabLayoutType.setVisibility(preferences.getBoolean(FgConst.PREF_SHOW_TRANSACTION_TYPE_TITLES, true) ?
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

            fragmentPayee.setShowKeyboard(transaction.getID() < 0);

            //Инициируем ViewPager Payee/DestAccount
            if (mPayeeName != null && !mPayeeName.isEmpty()) {
                setPayeeName(mPayeeName);
            } else {
                Payee payee = TransactionManager.getPayee(transaction, getApplicationContext());
                setPayeeName(payee.getFullName());
            }

            fragmentPayee.setPayeeOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(ActivityEditTransaction.this.getApplicationContext(), ActivityList.class);
                    intent.putExtra("showHomeButton", false);
                    intent.putExtra("model", PayeesDAO.getInstance(getApplicationContext()).getPayeeByID(transaction.getPayeeID()));
                    intent.putExtra("requestCode", REQUEST_CODE_SELECT_MODEL);
                    ActivityEditTransaction.this.startActivityForResult(intent, REQUEST_CODE_SELECT_MODEL);
                }
            });
            fragmentPayee.setPayeeTextChangeListener(new FragmentPayee.PayeeTextChangeListener() {
                @Override
                public void OnPayeeItemClick(Payee payee) {
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
                public void OnPayeeTyping(String payeeName) {
                    mPayeeName = payeeName;
                }

                @Override
                public void OnClearPayee() {
                    transaction.setPayeeID(-1);
                    setPayeeName("");
                }
            });
        }
    }

    private void initCategory() {
        edCategory.setText(CategoriesDAO.getInstance(this).getCategoryByID(transaction.getCategoryID()).getFullName());
        edCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ActivityEditTransaction.this.getApplicationContext(), ActivityList.class);
                intent.putExtra("showHomeButton", false);
                intent.putExtra("model", CategoriesDAO.getInstance(getApplicationContext()).getCategoryByID(transaction.getCategoryID()));
                intent.putExtra("requestCode", REQUEST_CODE_SELECT_MODEL);
                ActivityEditTransaction.this.startActivityForResult(intent, REQUEST_CODE_SELECT_MODEL);
            }
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

        mImageButtonScanQR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ActivityEditTransaction.this, ActivityScanQR.class);
                intent.putExtra("transaction", transaction);
                startActivityForResult(intent, RequestCodes.REQUEST_CODE_SCAN_QR);
            }
        });

        mImageButtonDownloadReceipt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getIntent().putExtra("load_products", true);
                initProductList();
            }
        });
    }

    AdapterProducts mAdapterProducts;
    boolean mProductListExpanded = true;
    boolean mTryDownloadAgain = true;

    private void loadProducts() {
        if (FtsHelper.isFtsCredentialsAvailiable(this)) {
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
                public void onDownload(List<ProductEntry> productEntries) {
                    spinAnim.cancel();
                    spinAnim.reset();
                    mLayoutLoadingProducts.setVisibility(View.GONE);
                    transaction.getProductEntries().clear();
                    transaction.getProductEntries().addAll(productEntries);
                    getIntent().removeExtra("load_products");
                    mTryDownloadAgain = true;
                    fillProductList();
                }

                @Override
                public void onFailure(Throwable t) {
                    if (mTryDownloadAgain) {
                        mTryDownloadAgain = false;
                        initProductList();
                    } else {
                        getIntent().removeExtra("load_products");
                        mTryDownloadAgain = true;
                        spinAnim.cancel();
                        spinAnim.reset();
                        mImageViewLoadingProducts.setVisibility(View.GONE);
                        mTextViewLoadingProducts.setText(getString(R.string.err_loading_products));
                        updateControlsState();
//                        mTextViewLoadingProducts.setTextColor(ContextCompat.getColor(ActivityEditTransaction.this, R.color.negative_color));
                    }
                }
            };
            mFtsResponseCall = FtsHelper.downloadProductEntryList(transaction, downloadProductsListener, this);
        } else {
            mLayoutLoadingProducts.setVisibility(View.GONE);
            fillProductList();
            if (!preferences.getBoolean(FgConst.PREF_FTS_DO_NOT_SHOW_AGAIN, false)) {
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

        mExpandableIndicator.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mProductListExpanded = !mProductListExpanded;
                initProductList();
            }
        });

    }

    private void fillProductList() {
        mLayoutLoadingProducts.setVisibility(View.GONE);
//        mRecyclerViewProductList.setVisibility(View.VISIBLE);
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
            }

            @Override
            public void onProductDeleted(int position) {
                transaction.getProductEntries().remove(position);
                mAdapterProducts.notifyDataSetChanged();
            }
        });
        mAdapterProducts.setHasStableIds(true);
        mRecyclerViewProductList.setAdapter(mAdapterProducts);
        mAdapterProducts.notifyDataSetChanged();
        updateControlsState();
    }

    private void initProject() {
        edProject.setText(ProjectsDAO.getInstance(this).getProjectByID(transaction.getProjectID()).getFullName());
        edProject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ActivityEditTransaction.this.getApplicationContext(), ActivityList.class);
                intent.putExtra("showHomeButton", false);
                intent.putExtra("model", ProjectsDAO.getInstance(getApplicationContext()).getProjectByID(transaction.getProjectID()));
                intent.putExtra("requestCode", REQUEST_CODE_SELECT_MODEL);
                ActivityEditTransaction.this.startActivityForResult(intent, REQUEST_CODE_SELECT_MODEL);
            }
        });
    }

    private void initSimpleDebt() {
        mTextViewSimpleDebt.setText("");
        mTextViewSimpleDebt.setText(TransactionManager.getSimpleDebt(transaction, this).getName());
        mTextViewSimpleDebt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ActivityEditTransaction.this.getApplicationContext(), ActivityModelList.class);
                intent.putExtra("showHomeButton", false);
                intent.putExtra("model", SimpleDebtsDAO.getInstance(getApplicationContext()).getSimpleDebtByID(transaction.getSimpleDebtID()));
                intent.putExtra("cabbageID", AccountsDAO.getInstance(getApplicationContext()).getAccountByID(transaction.getAccountID()).getCabbageId());
                intent.putExtra("requestCode", REQUEST_CODE_SELECT_MODEL);
                ActivityEditTransaction.this.startActivityForResult(intent, REQUEST_CODE_SELECT_MODEL);
            }
        });
    }

    private void initDepartment() {
        edDepartment.setText(DepartmentsDAO.getInstance(this).getDepartmentByID(transaction.getDepartmentID()).getFullName());
        edDepartment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ActivityEditTransaction.this.getApplicationContext(), ActivityList.class);
                intent.putExtra("showHomeButton", false);
                intent.putExtra("model", DepartmentsDAO.getInstance(getApplicationContext()).getDepartmentByID(transaction.getDepartmentID()));
                intent.putExtra("requestCode", REQUEST_CODE_SELECT_MODEL);
                ActivityEditTransaction.this.startActivityForResult(intent, REQUEST_CODE_SELECT_MODEL);
            }
        });
    }

    private void initAmountEditor() {
        amountEditor.setAmount(transaction.getAmount());
        amountEditor.setType(transaction.getTransactionType());
        amountEditor.setHint(getResources().getString(R.string.ent_amount));
        amountEditor.mOnAmountChangeListener = new AmountEditor.OnAmountChangeListener() {
            @Override
            public void OnAmountChange(BigDecimal newAmount, int newType) {
                transaction.setAmount(newAmount, newType);
                destAmountEditor.setAmount(transaction.getDestAmount());
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
            }
        };
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
        edExchangeRate.setText(String.valueOf(transaction.getExchangeRate().doubleValue()));
//        setExRateVisibility(viewPager.getCurrentItem());
        onExRateTextChangedListener = new OnExRateTextChangedListener();
        edExchangeRate.addTextChangedListener(onExRateTextChangedListener);
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

            edLocation.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(ActivityEditTransaction.this.getApplicationContext(), ActivityList.class);
                    intent.putExtra("showHomeButton", false);
                    intent.putExtra("model", LocationsDAO.getInstance(getApplicationContext()).getLocationByID(transaction.getLocationID()));
                    intent.putExtra("requestCode", REQUEST_CODE_SELECT_MODEL);
                    ActivityEditTransaction.this.startActivityForResult(intent, REQUEST_CODE_SELECT_MODEL);
                }
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
            preferences.edit().putBoolean("detect_locations", false).apply();
        } else {
            allowUpdateLocation = true;
        }
        if (!allowUpdateLocation) {
            Toast.makeText(this, R.string.msg_permission_location_never_askagain, Toast.LENGTH_SHORT).show();
        }
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
                    setPayeeName(payee.getFullName());
//                    initViewPagerPayee();

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
        } else if (resultCode == RESULT_OK && requestCode == RequestCodes.REQUEST_CODE_SCAN_QR) {
            transaction = data.getParcelableExtra("transaction");
            getIntent().putExtra("load_products", true);
            initUI();
        } else if (requestCode == RequestCodes.REQUEST_CODE_ENTER_FTS_LOGIN) {
            if (resultCode == RESULT_OK) {

            } else {
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
        DatePickerDialog dpd = DatePickerDialog.newInstance(
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTime(transaction.getDateTime());
                        calendar.set(year, monthOfYear, dayOfMonth);
                        transaction.setDateTime(calendar.getTime());
                        initDateTimeButtons();
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        int theme = Integer.valueOf(PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("theme", "0"));
        dpd.setThemeDark(theme == ActivityMain.THEME_DARK);
        dpd.vibrate(false);
        dpd.dismissOnPause(false);
        dpd.show(getFragmentManager(), "Datepickerdialog");
    }

    public void onTimeClick(View view) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(transaction.getDateTime());
        TimePickerDialog tpd = TimePickerDialog.newInstance(
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePickerDialog view, int hourOfDay, int minute, int second) {
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTime(transaction.getDateTime());
                        calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), hourOfDay, minute);
                        transaction.setDateTime(calendar.getTime());
                        initDateTimeButtons();
                    }
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                DateTimeFormatter.is24(this)
        );
        int theme = Integer.valueOf(PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("theme", "0"));
        tpd.setThemeDark(theme == ActivityMain.THEME_DARK);
        tpd.vibrate(false);
        tpd.dismissOnPause(false);
        tpd.show(getFragmentManager(), "Timepickerdialog");
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
        if (fragmentPayee != null) {
            switch (transaction.getTransactionType()) {
                case Transaction.TRANSACTION_TYPE_EXPENSE:
                    fragmentPayee.setHint(getString(R.string.ent_payee));
                    break;
                case Transaction.TRANSACTION_TYPE_INCOME:
                    fragmentPayee.setHint(getString(R.string.ent_payer));
                    break;
            }
        }
    }

    private void setPayeeName(String payeeName) {
        mPayeeName = payeeName;
        if (fragmentPayee != null) {
            fragmentPayee.setPayeeName(payeeName);
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
            transaction.setDestAmount(newAmount);
            edExchangeRate.removeTextChangedListener(onExRateTextChangedListener);
            edExchangeRate.setText(String.valueOf(transaction.getExchangeRate().doubleValue()));
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
                double rate = Double.valueOf(s.toString());
                transaction.setExchangeRate(new BigDecimal(rate));
                destAmountEditor.mOnAmountChangeListener = null;
                destAmountEditor.setAmount(transaction.getDestAmount());
                destAmountEditor.mOnAmountChangeListener = onDestAmountChangeListener;
            } catch (NumberFormatException nfe) {
                //Todo уведомлять пользователя о том, что число некорректно
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
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
