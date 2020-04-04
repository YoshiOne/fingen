package com.yoshione.fingen;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.SystemClock;
import android.provider.BaseColumns;
import android.text.InputType;
import android.widget.EditText;
import android.widget.Toast;

import android.util.Log;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.preference.PreferenceManager;

import com.yoshione.fingen.dao.AccountsDAO;
import com.yoshione.fingen.dao.AccountsSetsLogDAO;
import com.yoshione.fingen.dao.AccountsSetsRefDAO;
import com.yoshione.fingen.dao.BaseDAO;
import com.yoshione.fingen.dao.BudgetCreditsDAO;
import com.yoshione.fingen.dao.BudgetDAO;
import com.yoshione.fingen.dao.CabbagesDAO;
import com.yoshione.fingen.dao.CategoriesDAO;
import com.yoshione.fingen.dao.CreditsDAO;
import com.yoshione.fingen.dao.DatabaseUpgradeHelper;
import com.yoshione.fingen.dao.DepartmentsDAO;
import com.yoshione.fingen.dao.LocationsDAO;
import com.yoshione.fingen.dao.PayeesDAO;
import com.yoshione.fingen.dao.ProductEntrysDAO;
import com.yoshione.fingen.dao.ProductsDAO;
import com.yoshione.fingen.dao.ProjectsDAO;
import com.yoshione.fingen.dao.RunningBalanceDAO;
import com.yoshione.fingen.dao.SendersDAO;
import com.yoshione.fingen.dao.SimpleDebtsDAO;
import com.yoshione.fingen.dao.SmsDAO;
import com.yoshione.fingen.dao.SmsMarkersDAO;
import com.yoshione.fingen.dao.TemplatesDAO;
import com.yoshione.fingen.dao.TransactionsDAO;
import com.yoshione.fingen.db.UpdateHelper;
import com.yoshione.fingen.interfaces.IOnUnzipComplete;
import com.yoshione.fingen.managers.CabbageManager;
import com.yoshione.fingen.model.BaseModel;
import com.yoshione.fingen.model.Cabbage;
import com.yoshione.fingen.utils.FileUtils;
import com.yoshione.fingen.utils.Lg;
import com.yoshione.fingen.utils.Translit;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import io.requery.android.database.sqlite.SQLiteDatabase;
import io.requery.android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper implements BaseColumns {

    private static DBHelper mInstance = null;

    public SQLiteDatabase getDatabase() {
        return mDatabase;
    }

    private SQLiteDatabase mDatabase;
    private boolean mOriginDB;
    private static final String DATABASE_ORIGIN_NAME = "origin_fingen.db";
    private static final int DATABASE_ORIGIN_VERSION = 35;
    private static final String DATABASE_NAME = "fingen.db";
    public static final int DATABASE_VERSION = 37;
    public static final String TAG = "DBHelper";

    private static String getFullNameColumn(String tableName) {
        return "(SELECT path FROM (with recursive m(path, _id, name) AS (SELECT Name, _id, Name FROM "+tableName+" WHERE ParentId = -1 UNION ALL  SELECT path||'\\'||t.Name, t._id, t.Name FROM "+tableName+" t, m WHERE t.ParentId = m._id) SELECT * FROM m where _id = "+tableName+"._id)) AS FullName";
    }

    private static String[] readQueryFromAssets(String name, Context context) throws IOException {
        StringBuilder buf = new StringBuilder();
        InputStream inputStream = context.getAssets().open(name);
        BufferedReader in = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
        String str;

        while ((str = in.readLine()) != null) {
            buf.append(str);
            if (!str.isEmpty()) {
                buf.append("\n");
            }
        }

        in.close();
        return buf.toString().split(";");
    }

    //<editor-fold desc="Временные таблицы для подсчета суммы транзакций">
    public static final String T_SEARCH_TRANSACTIONS = "search_Transactions";
    //</editor-fold>

    private final Context mContext;

    public DBHelper(Context context, boolean isOriginDB) {
        super(context, isOriginDB ? DATABASE_ORIGIN_NAME : DATABASE_NAME, null, isOriginDB ? DATABASE_ORIGIN_VERSION : DATABASE_VERSION);
        this.mOriginDB = isOriginDB;
        this.mContext = context.getApplicationContext();
        final DatabaseUpgradeHelper dbh = DatabaseUpgradeHelper.getInstance();
        dbh.setUpgrading(true);
        mDatabase = getWritableDatabase();
        dbh.setUpgrading(false);
        while (dbh.isUpgrading()) {
            SystemClock.sleep(10);
        }
    }

    private DBHelper(Context context) {
        this(context, false);
    }

    public synchronized static DBHelper getInstance(Context ctx) {
        if (mInstance == null) {
            mInstance = new DBHelper(ctx);
        }
        return mInstance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, CabbagesDAO.SQL_CREATE_TABLE);
        db.execSQL(CabbagesDAO.SQL_CREATE_TABLE);

        Cabbage cabbage;
        List<String> codes = Arrays.asList("RUB", "USD", "EUR", "UAH", "BYN", "KZT", "ABC");
        for (String code : codes) {
            cabbage = CabbageManager.createFromCode(code, mContext);
            if (cabbage != null) {
                db.insertOrThrow(CabbagesDAO.TABLE, null, cabbage.getCV());
            }
        }

        Log.d(TAG, AccountsDAO.SQL_CREATE_TABLE);
        db.execSQL(AccountsDAO.SQL_CREATE_TABLE);

        Log.d(TAG, ProjectsDAO.SQL_CREATE_TABLE);
        db.execSQL(ProjectsDAO.SQL_CREATE_TABLE);

        Log.d(TAG, DepartmentsDAO.SQL_CREATE_TABLE);
        db.execSQL(DepartmentsDAO.SQL_CREATE_TABLE);

        Log.d(TAG, LocationsDAO.SQL_CREATE_TABLE);
        db.execSQL(LocationsDAO.SQL_CREATE_TABLE);

        Log.d(TAG, CategoriesDAO.SQL_CREATE_TABLE);
        db.execSQL(CategoriesDAO.SQL_CREATE_TABLE);

        Log.d(TAG, PayeesDAO.SQL_CREATE_TABLE);
        db.execSQL(PayeesDAO.SQL_CREATE_TABLE);

        Log.d(TAG, TransactionsDAO.SQL_CREATE_TABLE);
        db.execSQL(TransactionsDAO.SQL_CREATE_TABLE);
        Log.d(TAG, TransactionsDAO.SQL_CREATE_INDEX);
        db.execSQL(TransactionsDAO.SQL_CREATE_INDEX);

        Log.d(TAG, SmsDAO.SQL_CREATE_TABLE);
        db.execSQL(SmsDAO.SQL_CREATE_TABLE);

        Log.d(TAG, SmsMarkersDAO.SQL_CREATE_TABLE);
        db.execSQL(SmsMarkersDAO.SQL_CREATE_TABLE);

        Log.d(TAG, CreditsDAO.SQL_CREATE_TABLE);
        db.execSQL(CreditsDAO.SQL_CREATE_TABLE);

        Log.d(TAG, BudgetDAO.SQL_CREATE_TABLE);
        db.execSQL(BudgetDAO.SQL_CREATE_TABLE);

        Log.d(TAG, BudgetCreditsDAO.SQL_CREATE_TABLE);
        db.execSQL(BudgetCreditsDAO.SQL_CREATE_TABLE);

        Log.d(TAG, TemplatesDAO.SQL_CREATE_TABLE);
        db.execSQL(TemplatesDAO.SQL_CREATE_TABLE);

        Log.d(TAG, SimpleDebtsDAO.SQL_CREATE_TABLE);
        db.execSQL(SimpleDebtsDAO.SQL_CREATE_TABLE);

        Log.d(TAG, SendersDAO.SQL_CREATE_TABLE);
        db.execSQL(SendersDAO.SQL_CREATE_TABLE);

        Log.d(TAG, AccountsSetsRefDAO.SQL_CREATE_TABLE);
        db.execSQL(AccountsSetsRefDAO.SQL_CREATE_TABLE);

        Log.d(TAG, AccountsSetsLogDAO.SQL_CREATE_TABLE);
        db.execSQL(AccountsSetsLogDAO.SQL_CREATE_TABLE);

        Log.d(TAG, ProductsDAO.SQL_CREATE_TABLE);
        db.execSQL(ProductsDAO.SQL_CREATE_TABLE);

        Log.d(TAG, ProductEntrysDAO.SQL_CREATE_TABLE);
        db.execSQL(ProductEntrysDAO.SQL_CREATE_TABLE);
        Log.d(TAG, ProductEntrysDAO.SQL_CREATE_INDEX);
        db.execSQL(ProductEntrysDAO.SQL_CREATE_INDEX);

        ContentValues cv = new ContentValues();
        cv.put(ProductsDAO.COL_ID, 0);
        cv.put(ProductsDAO.COL_NAME, "default_product");
        db.insert(ProductsDAO.TABLE, "", cv);

        Log.d(TAG, RunningBalanceDAO.SQL_CREATE_TABLE);
        db.execSQL(RunningBalanceDAO.SQL_CREATE_TABLE);
        db.execSQL(RunningBalanceDAO.SQL_CREATE_INDEX_ACCOUNTS);
        db.execSQL(RunningBalanceDAO.SQL_CREATE_INDEX_TRANSACTIONS);
        db.execSQL(RunningBalanceDAO.SQL_CREATE_INDEX_DATETIME);
    }

    @SuppressLint({"DefaultLocale", "CallNeedsPermission"})
    @Override
    public void onUpgrade(final SQLiteDatabase db, int oldVersion, int newVersion) {
        Lg.log(TAG, "Upgrade database " + String.valueOf(oldVersion) + " -> " + String.valueOf(newVersion));

        //Сделали на всякий случай бэкап
        try {
            backupDB(false);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (oldVersion < 17) { UpdateHelper.update17(db, mContext); }
        if (oldVersion < 18) { UpdateHelper.update18(db, mContext); }
        if (oldVersion < 19) { UpdateHelper.update19(db); }
        if (oldVersion < 20) { UpdateHelper.update20(db); }
        if (oldVersion < 21) { UpdateHelper.update21(db); }
        if (oldVersion < 22) { UpdateHelper.update22(db); }
        if (oldVersion < 23) { UpdateHelper.update23(db); }
        if (oldVersion < 24) { UpdateHelper.update24(db); }
        if (oldVersion < 25) { UpdateHelper.update25(db); }
        if (oldVersion < 26) { UpdateHelper.update26(db); }
        if (oldVersion < 27) {
            UpdateHelper.update27(db, database -> DBHelper.this.updateRunningBalance(db));
        }
        if (oldVersion < 28) {
            try {
                updateRunningBalance(db);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (oldVersion < 29) { UpdateHelper.update29(db); }
        if (oldVersion < 30) { UpdateHelper.update30(db); }
        if (oldVersion < 31) { UpdateHelper.update31(db, mContext); }
        if (oldVersion < 32) { UpdateHelper.update32(db); }
        if (oldVersion < 34) { UpdateHelper.update33(db); }
        if (oldVersion < 36) { UpdateHelper.update35(db); }
        if (oldVersion < 37) { UpdateHelper.update36(db); }
        if (oldVersion < 25) {
            try {
                updateRunningBalance(db);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "Downgrade database " + oldVersion + " -> " + newVersion);

        if (oldVersion >= 37 && newVersion == DATABASE_ORIGIN_VERSION) {
            db.execSQL("ALTER TABLE ref_Departments RENAME TO ref_Departments_old");
            db.execSQL("CREATE TABLE ref_Departments ("
                    + BaseDAO.COMMON_FIELDS + ", "
                    + BaseDAO.COL_NAME + " TEXT NOT NULL, "
                    + DepartmentsDAO.COL_IS_ACTIVE + " INTEGER NOT NULL, "
                    + BaseDAO.COL_PARENT_ID + " INTEGER REFERENCES [" + DepartmentsDAO.TABLE + "]([" + BaseDAO.COL_ID + "]) ON DELETE SET NULL ON UPDATE CASCADE, "
                    + BaseDAO.COL_ORDER_NUMBER + " INTEGER, "
                    + BaseDAO.COL_FULL_NAME + " TEXT, "
                    + BaseDAO.COL_SEARCH_STRING + " TEXT, "
                    + "UNIQUE (" + BaseDAO.COL_NAME + ", " + BaseDAO.COL_PARENT_ID + ", " + BaseDAO.COL_SYNC_DELETED + ") ON CONFLICT ABORT);");
            db.execSQL(
                    "INSERT INTO ref_Departments (" + BaseDAO.COL_ID + ", " + BaseDAO.COL_SYNC_FBID + ", " + BaseDAO.COL_SYNC_TS + ", " + BaseDAO.COL_SYNC_DELETED + ", "
                            + BaseDAO.COL_SYNC_DIRTY + ", " + BaseDAO.COL_SYNC_LAST_EDITED + ", " + BaseDAO.COL_NAME + ", " + DepartmentsDAO.COL_IS_ACTIVE+ ", "
                            + BaseDAO.COL_PARENT_ID + ", " + BaseDAO.COL_ORDER_NUMBER + ", " + BaseDAO.COL_FULL_NAME + ", " + BaseDAO.COL_SEARCH_STRING + ")"
                            + " SELECT " + BaseDAO.COL_ID + ", " + BaseDAO.COL_SYNC_FBID + ", " + BaseDAO.COL_SYNC_TS + ", " + BaseDAO.COL_SYNC_DELETED + ", "
                            + BaseDAO.COL_SYNC_DIRTY + ", " + BaseDAO.COL_SYNC_LAST_EDITED + ", " + BaseDAO.COL_NAME + ", " + DepartmentsDAO.COL_IS_ACTIVE+ ", "
                            + BaseDAO.COL_PARENT_ID + ", " + BaseDAO.COL_ORDER_NUMBER + ", " + BaseDAO.COL_FULL_NAME + ", " + BaseDAO.COL_SEARCH_STRING
                            + " FROM ref_Departments_old");
            db.execSQL("DROP TABLE ref_Departments_old");

            db.execSQL("ALTER TABLE log_Products RENAME TO log_Products_old");
            db.execSQL("CREATE TABLE log_Products ("
                    + BaseDAO.COMMON_FIELDS + ", "
                    + ProductEntrysDAO.COL_TRANSACTION_ID + " INTEGER REFERENCES [" + TransactionsDAO.TABLE + "]([" + BaseDAO.COL_ID + "]) ON DELETE SET NULL ON UPDATE CASCADE, "
                    + ProductEntrysDAO.COL_PRODUCT_ID     + " INTEGER REFERENCES [" + ProductsDAO.TABLE   + "]([" + BaseDAO.COL_ID + "]) ON DELETE SET NULL ON UPDATE CASCADE, "
                    + ProductEntrysDAO.COL_CATEGORY_ID    + " INTEGER DEFAULT -1 REFERENCES [" + CategoriesDAO.TABLE + "]([" + BaseDAO.COL_ID + "]) ON DELETE SET NULL ON UPDATE CASCADE, "
                    + ProductEntrysDAO.COL_PROJECT_ID     + " INTEGER DEFAULT -1 REFERENCES [" + ProjectsDAO.TABLE  + "]([" + BaseDAO.COL_ID + "]) ON DELETE SET NULL ON UPDATE CASCADE, "
                    + ProductEntrysDAO.COL_PRICE          + " REAL NOT NULL DEFAULT 0, "
                    + ProductEntrysDAO.COL_QUANTITY       + " REAL NOT NULL DEFAULT 1 CHECK (Quantity >= 0));");
            db.execSQL(
                    "INSERT INTO log_Products (" + BaseDAO.COL_ID + ", " + BaseDAO.COL_SYNC_FBID + ", " + BaseDAO.COL_SYNC_TS + ", " + BaseDAO.COL_SYNC_DELETED + ", "
                            + BaseDAO.COL_SYNC_DIRTY + ", " + BaseDAO.COL_SYNC_LAST_EDITED + ", " + ProductEntrysDAO.COL_TRANSACTION_ID + ", "
                            + ProductEntrysDAO.COL_PRODUCT_ID + ", " + ProductEntrysDAO.COL_CATEGORY_ID + ", " + ProductEntrysDAO.COL_PROJECT_ID + ", "
                            + ProductEntrysDAO.COL_PRICE + ", "  + ProductEntrysDAO.COL_QUANTITY + ")"
                            + " SELECT " + BaseDAO.COL_ID + ", " + BaseDAO.COL_SYNC_FBID + ", " + BaseDAO.COL_SYNC_TS + ", " + BaseDAO.COL_SYNC_DELETED + ", "
                            + BaseDAO.COL_SYNC_DIRTY + ", " + BaseDAO.COL_SYNC_LAST_EDITED + ", " + ProductEntrysDAO.COL_TRANSACTION_ID + ", "
                            + ProductEntrysDAO.COL_PRODUCT_ID + ", " + ProductEntrysDAO.COL_CATEGORY_ID + ", " + ProductEntrysDAO.COL_PROJECT_ID + ", "
                            + ProductEntrysDAO.COL_PRICE + ", "  + ProductEntrysDAO.COL_QUANTITY
                            + " FROM log_Products_old");
            db.execSQL("DROP TABLE log_Products_old");
        }
        if (oldVersion >= 36 && newVersion == DATABASE_ORIGIN_VERSION) {
            db.execSQL("ALTER TABLE ref_Accounts RENAME TO ref_Accounts_old");
            db.execSQL("CREATE TABLE " + AccountsDAO.TABLE + " ("
                    + AccountsDAO.COMMON_FIELDS + ", "
                    + AccountsDAO.COL_TYPE + " INTEGER NOT NULL, "
                    + AccountsDAO.COL_NAME + " TEXT NOT NULL, "
                    + AccountsDAO.COL_CURRENCY + " INTEGER REFERENCES [" + CabbagesDAO.TABLE + "]([" + BaseDAO.COL_ID + "]) ON DELETE SET NULL ON UPDATE CASCADE, "
                    + AccountsDAO.COL_EMITENT + " TEXT, "
                    + AccountsDAO.COL_LAST4DIGITS + " INTEGER, "
                    + AccountsDAO.COL_COMMENT + " TEXT, "
                    + AccountsDAO.COL_START_BALANCE + " REAL NOT NULL, "
                    + AccountsDAO.COL_IS_CLOSED + " INTEGER NOT NULL, "
                    + AccountsDAO.COL_ORDER + " INTEGER, "
                    + AccountsDAO.COL_CREDIT_LIMIT + " REAL, "
                    + AccountsDAO.COL_SEARCH_STRING + " TEXT, "
                    + "UNIQUE (" + AccountsDAO.COL_NAME + ", " + AccountsDAO.COL_SYNC_DELETED + ") ON CONFLICT ABORT);");
            db.execSQL(
                    "INSERT INTO ref_Accounts (" + AccountsDAO.COL_ID + ", " + AccountsDAO.COL_SYNC_FBID + ", " + AccountsDAO.COL_SYNC_TS + ", " + AccountsDAO.COL_SYNC_DELETED + ", "
                            + AccountsDAO.COL_SYNC_DIRTY + ", " + AccountsDAO.COL_SYNC_LAST_EDITED + ", " + AccountsDAO.COL_TYPE + ", " + AccountsDAO.COL_NAME + ", "
                            + AccountsDAO.COL_CURRENCY + ", " + AccountsDAO.COL_EMITENT + ", " + AccountsDAO.COL_LAST4DIGITS + ", " + AccountsDAO.COL_COMMENT + ", "
                            + AccountsDAO.COL_START_BALANCE + ", " + AccountsDAO.COL_IS_CLOSED + ", " + AccountsDAO.COL_ORDER + ", " + AccountsDAO.COL_CREDIT_LIMIT + ", "
                            + AccountsDAO.COL_SEARCH_STRING + ")"
                            + " SELECT " + AccountsDAO.COL_ID + ", " + AccountsDAO.COL_SYNC_FBID + ", " + AccountsDAO.COL_SYNC_TS + ", " + AccountsDAO.COL_SYNC_DELETED + ", "
                            + AccountsDAO.COL_SYNC_DIRTY + ", " + AccountsDAO.COL_SYNC_LAST_EDITED + ", " + AccountsDAO.COL_TYPE + ", " + AccountsDAO.COL_NAME + ", "
                            + AccountsDAO.COL_CURRENCY + ", " + AccountsDAO.COL_EMITENT + ", " + AccountsDAO.COL_LAST4DIGITS + ", " + AccountsDAO.COL_COMMENT + ", "
                            + AccountsDAO.COL_START_BALANCE + ", " + AccountsDAO.COL_IS_CLOSED + ", " + AccountsDAO.COL_ORDER + ", " + AccountsDAO.COL_CREDIT_LIMIT + ", "
                            + AccountsDAO.COL_SEARCH_STRING
                            + " FROM ref_Accounts_old");
            db.execSQL("DROP TABLE ref_Accounts_old");
        } else
            super.onDowngrade(db, oldVersion, newVersion);
    }

    String getSqliteVersion() {
        Cursor cursor = getDatabase().rawQuery("select sqlite_version() AS sqlite_version", null);
        String result = "N\\A";
        try {
            if (cursor.moveToFirst()) {
                result = cursor.getString(0);
            }
        } finally {
            cursor.close();
        }
        return result;
    }

    public void rebuildDB() throws IOException {
        SQLiteDatabase db = getDatabase();
        db.beginTransaction();

        String[] tableNames = new String[]{AccountsDAO.TABLE, AccountsSetsRefDAO.TABLE, AccountsSetsLogDAO.TABLE,
                CategoriesDAO.TABLE, PayeesDAO.TABLE, ProjectsDAO.TABLE, LocationsDAO.TABLE, DepartmentsDAO.TABLE,
                SimpleDebtsDAO.TABLE, TransactionsDAO.TABLE, TemplatesDAO.TABLE, SmsDAO.TABLE, SmsMarkersDAO.TABLE,
                CreditsDAO.TABLE, BudgetDAO.TABLE, BudgetCreditsDAO.TABLE, SendersDAO.TABLE, ProductsDAO.TABLE, ProductEntrysDAO.TABLE
        };

        for (String tableName : tableNames) {
            db.delete(tableName, BaseDAO.COL_SYNC_DELETED + " > 0", null);
        }

        updateFullNames("ref_Categories", true, db);
        updateFullNames("ref_Payees", true, db);
        updateFullNames("ref_Projects", true, db);
        updateFullNames("ref_Locations", true, db);
        updateFullNames("ref_Departments", true, db);

        updateFullNames("ref_Accounts", false, db);
        updateFullNames("ref_SimpleDebts", false, db);
        updateFullNames("log_Templates", false, db);

        updateRunningBalance(db);

        updateLogProducts(db);

        db.setTransactionSuccessful();
        db.endTransaction();

        db.execSQL("VACUUM");
    }

    public void updateRunningBalance(SQLiteDatabase database) throws IOException {
        String sql[] = readQueryFromAssets("sql/update_running_balance.sql", mContext);
        for (String s : sql) {
            if (s != null && !s.isEmpty() && !s.equals("\n")) {
                android.util.Log.d(TAG, s);
                database.execSQL(s);
            }
        }
    }

    public void updateLogProducts(SQLiteDatabase db) {
        db.execSQL("DELETE FROM log_Products WHERE TransactionID < 0");

        Cursor cursor = db.rawQuery("SELECT _id, Amount FROM log_Transactions " +
                "WHERE _id not in (SELECT TransactionID FROM log_Products) AND Deleted = 0", null);

        if (cursor != null) {
            ProductEntrysDAO.updateLogProducts(db, cursor);
            cursor.close();
        }
    }

    public static void updateFullNames(String tableName, boolean useFullName, SQLiteDatabase db) {
        String nameColumn;
        if (tableName.equals(TransactionsDAO.TABLE)) {
            nameColumn = useFullName ? getFullNameColumn(tableName) : TransactionsDAO.COL_COMMENT;
        } else {
            nameColumn = useFullName ? getFullNameColumn(tableName) : "Name";
        }
        String[] fields;
        if (useFullName) {
            fields = new String[]{BaseDAO.COL_ID, nameColumn, BaseDAO.COL_SEARCH_STRING, BaseDAO.COL_FULL_NAME};
        } else {
            fields = new String[]{BaseDAO.COL_ID, nameColumn, BaseDAO.COL_SEARCH_STRING};
        }
        try (Cursor cursor = db.query(tableName, fields, BaseDAO.COL_SYNC_DELETED + " = 0", null, null, null, null)) {
            ContentValues cv = new ContentValues();
            String translit;
            if (cursor.moveToFirst()) {
                while (!cursor.isAfterLast()) {
                    cv.clear();
                    if (useFullName) {
                        cv.put(BaseDAO.COL_FULL_NAME, cursor.getString(1));
                    }
                    translit = Translit.toTranslit(cursor.getString(1).toLowerCase());
                    if (!cursor.getString(2).equals(translit)) {
                        cv.put(BaseDAO.COL_SEARCH_STRING, translit);
                    }
                    if (cv.size() != 0) {
                        db.update(tableName, cv, "_id = " + cursor.getString(0), null);
                    }
                    cursor.moveToNext();
                }
            }
        }
    }

    private String getDbPath() {
        return mContext.getDatabasePath(mOriginDB ? DATABASE_ORIGIN_NAME : DATABASE_NAME).toString();
    }

    public File backupDB(boolean vacuum) throws IOException {
        File backup = null;
        if (vacuum) {
            mDatabase.execSQL("VACUUM");
        }
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            String backupPath = FileUtils.getExtFingenBackupFolder();
            @SuppressLint("SimpleDateFormat") String backupFile = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date()) + ".zip";

            if (!backupPath.isEmpty()) {
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
                String password = preferences.getString("backup_password", "");
                boolean enableProtection = preferences.getBoolean("enable_backup_password", false);
                if (enableProtection && !password.isEmpty()) {
                    backup = FileUtils.zipAndEncrypt(getDbPath(), backupPath + backupFile, password, DATABASE_NAME);
                } else {
                    backup = FileUtils.zip(getDbPath(), backupPath + backupFile, DATABASE_NAME);
                }
                Log.d(TAG, String.format("File %s saved", backupFile));
            }
        }
        return backup;
    }

    public File backupToOriginDB(boolean vacuum) throws IOException {
        File backup = null;
        if (vacuum) {
            mDatabase.execSQL("VACUUM");
        }
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            FileUtils.copyFile(getDbPath(), mContext.getDatabasePath(DATABASE_ORIGIN_NAME).getPath());

            DBHelper dbh = new DBHelper(mContext, true);
            backup = dbh.backupDB(vacuum);
            dbh.close();
            mContext.deleteDatabase(DATABASE_ORIGIN_NAME);
        }
        return backup;
    }

    void showRestoreDialog(final String filename, final AppCompatActivity activity) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(R.string.ttl_confirm_action);
        builder.setMessage(R.string.msg_confirm_restore_db);

        // Set up the buttons
        builder.setPositiveButton("OK", (dialog, which) -> DBHelper.this.restoreDB(filename, activity));
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private class OnOkListener implements DialogInterface.OnClickListener {
        String mZipFile;
        String mLocation;
        EditText mInput;
        IOnUnzipComplete mIOnUnzipComplete;

        public OnOkListener(String zipFile, String location, EditText input, IOnUnzipComplete IOnUnzipComplete) {
            mZipFile = zipFile;
            mLocation = location;
            mInput = input;
            mIOnUnzipComplete = IOnUnzipComplete;
        }

        @Override
        public void onClick(DialogInterface dialogInterface, int i) {
            FileUtils.unzipAndDecrypt(mZipFile, mLocation, mInput.getText().toString(), mIOnUnzipComplete);
        }
    }

    synchronized private void restoreDB(String filename, final AppCompatActivity activity) {
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            final File file = new File(filename);
            final File db = new File(getDbPath());
            final IOnUnzipComplete completeListener = new IOnUnzipComplete() {
                @Override
                public void onComplete() {
                    if (db.delete()) {
                        File restored = new File(db.getParent() + "/fingen.db.ex");
                        restored.renameTo(db);
                    }
                    Intent mStartActivity = new Intent(mContext, ActivityMain.class);
                    int mPendingIntentId = 123456;
                    PendingIntent mPendingIntent = PendingIntent.getActivity(mContext, mPendingIntentId, mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT);
                    AlarmManager mgr = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
                    mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
                    System.exit(0);
                }

                @Override
                public void onError() {
                    Toast.makeText(activity, "Error on restore DB", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onWrongPassword() {
                    AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                    builder.setTitle(activity.getString(R.string.ttl_enter_password));
                    final EditText input = (EditText) activity.getLayoutInflater().inflate(R.layout.template_edittext, null);
                    input.setText("");
                    input.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    builder.setView(input);

                    builder.setPositiveButton("OK", new OnOkListener(file.toString(), db.getParent() + "/", input, this));

                    builder.show();
                    input.requestFocus();
                }
            };
            if (file.exists()) {
                final String password = PreferenceManager.getDefaultSharedPreferences(mContext).getString("backup_password", "");
                FileUtils.unzipAndDecrypt(file.toString(), db.getParent() + "/", password, completeListener);
            }
        }
    }

    synchronized void clearDB() {
        mDatabase.beginTransaction();
        Cursor c = mDatabase.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);
        if (c.moveToFirst()) {
            while ( !c.isAfterLast() ) {
                mDatabase.delete(c.getString(0), null, null);
                c.moveToNext();
            }
        }
        c.close();
        mDatabase.setTransactionSuccessful();
        mDatabase.endTransaction();
        mDatabase.rawQuery("VACUUM", null);
    }

    public static ContentValues addSyncDataToCV(ContentValues values, BaseModel baseModel) {
        values.put(BaseDAO.COL_SYNC_FBID, baseModel.getFBID());
        values.put(BaseDAO.COL_SYNC_TS, baseModel.getTS());
        values.put(BaseDAO.COL_SYNC_DELETED, 0);
        values.put(BaseDAO.COL_SYNC_DIRTY, baseModel.isDirty() ? 1 : 0);
        values.put(BaseDAO.COL_SYNC_LAST_EDITED, baseModel.getLastEdited());
        return values;
    }

    public synchronized static int getMaxDel(SQLiteDatabase db, String tableName) {
        Cursor cursor = db.query(tableName, new String[]{String.format("MAX(%s) AS MAXDEL", BaseDAO.COL_SYNC_DELETED)}, null, null, null, null, null);
        int maxDel = 0;
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    while (!cursor.isAfterLast()) {
                        maxDel = cursor.getInt(0);
                        cursor.moveToNext();
                    }
                }
            } finally {
                cursor.close();
            }
        }
        return maxDel;
    }

}
