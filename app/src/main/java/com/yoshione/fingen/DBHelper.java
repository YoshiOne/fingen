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
import android.preference.PreferenceManager;
import android.provider.BaseColumns;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.widget.EditText;
import android.widget.Toast;

import android.util.Log;
import com.yoshione.fingen.dao.DatabaseUpgradeHelper;
import com.yoshione.fingen.db.IUpdateRunningBalance;
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
import java.util.HashMap;
import java.util.List;

import io.requery.android.database.sqlite.SQLiteDatabase;
import io.requery.android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by slv on 12.08.2015.
 * 1
 */
public class DBHelper extends SQLiteOpenHelper implements BaseColumns {

    private static DBHelper mInstance = null;

    public SQLiteDatabase getDatabase() {
        return mDatabase;
    }

    private SQLiteDatabase mDatabase;
    private static final String DATABASE_NAME = "fingen.db";
    public static final int DATABASE_VERSION = 34;
    public static final String TAG = "DBHelper";

    //common fields
    public static final String C_ID = "_id";
    public static final String C_SYNC_FBID = "FBID";
    public static final String C_SYNC_TS = "TS";
    public static final String C_SYNC_DELETED = "Deleted";
    public static final String C_SYNC_DIRTY = "Dirty";
    public static final String C_SYNC_LASTEDITED = "LastEdited";
    public static final String C_ORDERNUMBER = "OrderNumber";
    public static final String C_PARENTID = "ParentID";
    private static final String C_SEARCH_STRING = "SearchString";
    public static final String C_FULL_NAME = "FullName";
    private static String getFullNameColumn(String tableName) {
        return "(SELECT path FROM (with recursive m(path, _id, name) AS (SELECT Name, _id, Name FROM "+tableName+" WHERE ParentId = -1 UNION ALL  SELECT path||'\\'||t.Name, t._id, t.Name FROM "+tableName+" t, m WHERE t.ParentId = m._id) SELECT * FROM m where _id = "+tableName+"._id)) AS FullName";
    }
    private static final String COMMON_FIELDS =
            C_ID + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,\n"
                    + C_SYNC_FBID + " TEXT,\n"
                    + C_SYNC_TS + " INTEGER,\n"
                    + C_SYNC_DELETED + " INTEGER,\n"
                    + C_SYNC_DIRTY + " INTEGER,\n"
                    + C_SYNC_LASTEDITED + " TEXT,\n";

    //<editor-fold desc="ref_Currencies">
    public static final String T_REF_CURRENCIES = "ref_Currencies";
    public static final String C_REF_CURRENCIES_CODE = "Code";//трехсимвольный уникальный идентификатор валюты
    public static final String C_REF_CURRENCIES_SYMBOL = "Symbol";//символы, испульзуемые при выводе суммы (например $, руб.)
    public static final String C_REF_CURRENCIES_NAME = "Name";
    public static final String C_REF_CURRENCIES_DECIMALCOUNT = "DecimalCount";//количество знаков после запятой (-1  без ограничений)
    public static final String T_REF_CURRENCIES_ALL_COLUMNS[] = new String[]{
            C_ID, C_SYNC_FBID, C_SYNC_TS, C_SYNC_DELETED, C_SYNC_DIRTY, C_SYNC_LASTEDITED, C_REF_CURRENCIES_CODE,
            C_REF_CURRENCIES_SYMBOL, C_REF_CURRENCIES_NAME, C_REF_CURRENCIES_DECIMALCOUNT, C_ORDERNUMBER};
    private static final String SQL_CREATE_TABLE_REF_CURRENCIES = "CREATE TABLE " + T_REF_CURRENCIES + " ("
            + COMMON_FIELDS
            + C_REF_CURRENCIES_CODE + " TEXT NOT NULL, "
            + C_REF_CURRENCIES_SYMBOL + " TEXT NOT NULL, "
            + C_REF_CURRENCIES_NAME + " TEXT NOT NULL, "
            + C_REF_CURRENCIES_DECIMALCOUNT + " INTEGER NOT NULL, "
            + C_ORDERNUMBER + " INTEGER, "
            + "UNIQUE (" + C_REF_CURRENCIES_CODE + ", " + C_SYNC_DELETED + ") ON CONFLICT ABORT);";
    //private static TableInfo infoCurrencies = new TableInfo(T_REF_CURRENCIES, SQL_CREATE_TABLE_REF_CURRENCIES, T_REF_CURRENCIES_ALL_COLUMNS, IAbstractModel.MODEL_TYPE_CABBAGE,

    //<editor-fold desc="ref_Accounts">
    public static final String T_REF_ACCOUNTS = "ref_Accounts";
    public static final String C_REF_ACCOUNTS_TYPE = "Type";
    public static final String C_REF_ACCOUNTS_NAME = "Name";
    public static final String C_REF_ACCOUNTS_CURRENCY = "Currency";
    public static final String C_REF_ACCOUNTS_EMITENT = "Emitent";
    public static final String C_REF_ACCOUNTS_LAST4DIGITS = "Last4Digits";
    public static final String C_REF_ACCOUNTS_COMMENT = "Comment";
    public static final String C_REF_ACCOUNTS_STARTBALANCE = "StartBalance";
    public static final String C_REF_ACCOUNTS_ISCLOSED = "IsClosed";
    public static final String C_REF_ACCOUNTS_ORDER = "CustomOrder";
    public static final String C_REF_ACCOUNTS_CREDITLIMIT = "CreditLimit";
    private static final String C_REF_ACCOUNTS_INCOME = "(SELECT Income FROM log_Running_Balance WHERE AccountID = _id AND DateTimeRB = (SELECT MAX(DateTimeRB) FROM log_Running_Balance WHERE AccountID = _id )) AS Income";
    private static final String C_REF_ACCOUNTS_EXPENSE = "(SELECT Expense FROM log_Running_Balance WHERE AccountID = _id AND DateTimeRB = (SELECT MAX(DateTimeRB) FROM log_Running_Balance WHERE AccountID = _id )) AS Expense";
    public static final String[] T_REF_ACCOUNTS_ALL_COLUMNS = {
            C_ID, C_SYNC_FBID, C_SYNC_TS, C_SYNC_DELETED, C_SYNC_DIRTY, C_SYNC_LASTEDITED,
            C_REF_ACCOUNTS_TYPE, C_REF_ACCOUNTS_NAME, C_REF_ACCOUNTS_CURRENCY,
            C_REF_ACCOUNTS_EMITENT, C_REF_ACCOUNTS_LAST4DIGITS, C_REF_ACCOUNTS_COMMENT,
            C_REF_ACCOUNTS_STARTBALANCE, C_REF_ACCOUNTS_ISCLOSED, C_REF_ACCOUNTS_ORDER,
            C_REF_ACCOUNTS_CREDITLIMIT, C_REF_ACCOUNTS_INCOME, C_REF_ACCOUNTS_EXPENSE, C_SEARCH_STRING};
    private static final String SQL_CREATE_TABLE_REF_ACCOUNTS = "CREATE TABLE " + T_REF_ACCOUNTS + " ("
            + COMMON_FIELDS
            + C_REF_ACCOUNTS_TYPE + " INTEGER NOT NULL, "
            + C_REF_ACCOUNTS_NAME + " TEXT NOT NULL, "
            + C_REF_ACCOUNTS_CURRENCY + " INTEGER REFERENCES [" + T_REF_CURRENCIES + "]([" + C_ID + "]) ON DELETE SET NULL ON UPDATE CASCADE, "
            + C_REF_ACCOUNTS_EMITENT + " TEXT, "
            + C_REF_ACCOUNTS_LAST4DIGITS + " INTEGER, "
            + C_REF_ACCOUNTS_COMMENT + " TEXT, "
            + C_REF_ACCOUNTS_STARTBALANCE + " REAL NOT NULL, "
            + C_REF_ACCOUNTS_ISCLOSED + " INTEGER NOT NULL, "
            + C_REF_ACCOUNTS_ORDER + " INTEGER, "
            + C_REF_ACCOUNTS_CREDITLIMIT + " REAL, "
            + C_SEARCH_STRING + " TEXT, "
            + "UNIQUE (" + C_REF_ACCOUNTS_NAME + ", " + C_SYNC_DELETED + ") ON CONFLICT ABORT);";
    //private static TableInfo infoAccounts = new TableInfo(T_REF_ACCOUNTS, SQL_CREATE_TABLE_REF_ACCOUNTS, T_REF_ACCOUNTS_ALL_COLUMNS, IAbstractModel.MODEL_TYPE_ACCOUNT,

    public static final String T_REF_ACCOUNTS_SETS = "ref_Accounts_Sets";
    public static final String C_REF_ACCOUNTS_SETS_NAME = "Name";
    public static final String[] T_REF_ACCOUNTS_SETS_ALL_COLUMNS = {
            C_ID, C_SYNC_FBID, C_SYNC_TS, C_SYNC_DELETED, C_SYNC_DIRTY, C_SYNC_LASTEDITED,
            C_REF_ACCOUNTS_SETS_NAME};
    private static final String SQL_CREATE_TABLE_REF_ACCOUNTS_SETS = "CREATE TABLE " + T_REF_ACCOUNTS_SETS + " ("
            + COMMON_FIELDS
            + C_REF_ACCOUNTS_SETS_NAME + " TEXT, "
            + "UNIQUE (" + C_REF_ACCOUNTS_SETS_NAME + ", " + C_SYNC_DELETED + ") ON CONFLICT ABORT);";
    //</editor-fold>

    //<editor-fold desc="log_Accounts_Sets">
    public static final String T_LOG_ACCOUNTS_SETS = "log_Accounts_Sets";
    public static final String C_LOG_ACCOUNTS_SETS_SET = "SetID";
    public static final String C_LOG_ACCOUNTS_SETS_ACCOUNT = "AccountID";
    public static final String[] T_LOG_ACCOUNTS_SETS_ALL_COLUMNS = {
            C_ID, C_SYNC_FBID, C_SYNC_TS, C_SYNC_DELETED, C_SYNC_DIRTY, C_SYNC_LASTEDITED,
            C_LOG_ACCOUNTS_SETS_SET, C_LOG_ACCOUNTS_SETS_ACCOUNT};
    private static final String SQL_CREATE_TABLE_LOG_ACCOUNTS_SETS = "CREATE TABLE " + T_LOG_ACCOUNTS_SETS + " ("
            + COMMON_FIELDS
            + C_LOG_ACCOUNTS_SETS_SET + " INTEGER NOT NULL ON CONFLICT ABORT REFERENCES [" + T_REF_ACCOUNTS_SETS + "]([" + C_ID + "]) ON DELETE CASCADE ON UPDATE CASCADE,"
            + C_LOG_ACCOUNTS_SETS_ACCOUNT + " INTEGER NOT NULL ON CONFLICT ABORT REFERENCES [" + T_REF_ACCOUNTS + "]([" + C_ID + "]) ON DELETE CASCADE ON UPDATE CASCADE,"
            + "UNIQUE (" + C_LOG_ACCOUNTS_SETS_SET + ", " + C_LOG_ACCOUNTS_SETS_ACCOUNT + ", " + C_SYNC_DELETED + ") ON CONFLICT ABORT);";

    //</editor-fold>

    //<editor-fold desc="ref_Categories">
    public static final String T_REF_CATEGORIES = "ref_Categories";
    public static final String C_REF_CATEGORIES_NAME = "Name";
    public static final String C_REF_CATEGORIES_COLOR = "COLOR";
    public static final String C_REF_CATEGORIES_SIGN = "Sign";
    public static final String C_REF_CATEGORIES_PLAN = "Plan";
    public static final String[] T_REF_CATEGORIES_ALL_COLUMNS = {
            C_ID, C_SYNC_FBID, C_SYNC_TS, C_SYNC_DELETED, C_SYNC_DIRTY, C_SYNC_LASTEDITED,
            C_REF_CATEGORIES_NAME, C_REF_CATEGORIES_COLOR, C_PARENTID,
            C_ORDERNUMBER, C_REF_CATEGORIES_SIGN, C_FULL_NAME, C_SEARCH_STRING};
    private static final String SQL_CREATE_TABLE_REF_CATEGORIES = "CREATE TABLE " + T_REF_CATEGORIES + " ("
            + COMMON_FIELDS
            + C_REF_CATEGORIES_NAME + " TEXT NOT NULL, "
            + C_REF_CATEGORIES_COLOR + " TEXT, "
            + C_PARENTID + " INTEGER REFERENCES [" + T_REF_CATEGORIES + "]([" + C_ID + "]) ON DELETE SET NULL ON UPDATE CASCADE, "
            + C_ORDERNUMBER + " INTEGER NOT NULL, "
            + C_REF_CATEGORIES_SIGN + " INTEGER NOT NULL, "
            + C_FULL_NAME + " TEXT, "
            + C_SEARCH_STRING + " TEXT, "
            + "UNIQUE (" + C_REF_CATEGORIES_NAME + ", " + C_PARENTID + ", " + C_SYNC_DELETED + ") ON CONFLICT ABORT);";
    //private static TableInfo infoCategories = new TableInfo(T_REF_CATEGORIES, SQL_CREATE_TABLE_REF_CATEGORIES, T_REF_CATEGORIES_ALL_COLUMNS, IAbstractModel.MODEL_TYPE_CATEGORY,
    //</editor-fold>

    //<editor-fold desc="ref_Payees">
    public static final String T_REF_PAYEES = "ref_Payees";
    public static final String C_REF_PAYEES_NAME = "Name";
    public static final String C_REF_PAYEES_DEFCATEGORY = "DefCategory";
    public static final String[] T_REF_PAYEES_ALL_COLUMNS = {
            C_ID, C_SYNC_FBID, C_SYNC_TS, C_SYNC_DELETED, C_SYNC_DIRTY, C_SYNC_LASTEDITED, C_REF_PAYEES_NAME,
            C_REF_PAYEES_DEFCATEGORY, C_PARENTID, C_ORDERNUMBER, C_FULL_NAME, C_SEARCH_STRING};
    private static final String SQL_CREATE_TABLE_REF_PAYEES = "CREATE TABLE " + T_REF_PAYEES + " ("
            + COMMON_FIELDS
            + C_REF_PAYEES_NAME + " TEXT NOT NULL, "
            + C_REF_PAYEES_DEFCATEGORY + " INTEGER REFERENCES [" + T_REF_CATEGORIES + "]([" + C_ID + "]) ON DELETE SET NULL ON UPDATE CASCADE, "
            + C_PARENTID + " INTEGER REFERENCES [" + T_REF_PAYEES + "]([" + C_ID + "]) ON DELETE SET NULL ON UPDATE CASCADE, "
            + C_ORDERNUMBER + " INTEGER, "
            + C_FULL_NAME + " TEXT, "
            + C_SEARCH_STRING + " TEXT, "
            + "UNIQUE (" + C_REF_PAYEES_NAME + ", " + C_PARENTID + ", " + C_SYNC_DELETED + ") ON CONFLICT ABORT);";
    //private static TableInfo infoPayees = new TableInfo(T_REF_PAYEES, SQL_CREATE_TABLE_REF_PAYEES, T_REF_PAYEES_ALL_COLUMNS, IAbstractModel.MODEL_TYPE_PAYEE,
    //</editor-fold>

    //<editor-fold desc="ref_Projects">
    public static final String T_REF_PROJECTS = "ref_Projects";
    public static final String C_REF_PROJECTS_NAME = "Name";
    public static final String C_REF_PROJECTS_COLOR = "Color";
    public static final String C_REF_PROJECTS_ISACTIVE = "IsActive";//Boolean
    public static final String[] T_REF_PROJECTS_ALL_COLUMNS = {
            C_ID, C_SYNC_FBID, C_SYNC_TS, C_SYNC_DELETED, C_SYNC_DIRTY, C_SYNC_LASTEDITED,
            C_REF_PROJECTS_NAME, C_REF_PROJECTS_COLOR, C_REF_PROJECTS_ISACTIVE, C_PARENTID, C_ORDERNUMBER, C_FULL_NAME, C_SEARCH_STRING};
    private static final String SQL_CREATE_TABLE_REF_PROJECTS = "CREATE TABLE " + T_REF_PROJECTS + " ("
            + COMMON_FIELDS
            + C_REF_PROJECTS_NAME + " TEXT NOT NULL, "
            + C_REF_PROJECTS_COLOR + " TEXT DEFAULT '#ffffff', "
            + C_REF_PROJECTS_ISACTIVE + " INTEGER NOT NULL, "
            + C_PARENTID + " INTEGER REFERENCES [" + T_REF_PROJECTS + "]([" + C_ID + "]) ON DELETE SET NULL ON UPDATE CASCADE, "
            + C_ORDERNUMBER + " INTEGER, "
            + C_FULL_NAME + " TEXT, "
            + C_SEARCH_STRING + " TEXT, "
            + "UNIQUE (" + C_REF_PROJECTS_NAME + ", " + C_PARENTID + ", " + C_SYNC_DELETED + ") ON CONFLICT ABORT);";
    //private static TableInfo infoProjects = new TableInfo(T_REF_PROJECTS, SQL_CREATE_TABLE_REF_PROJECTS, T_REF_PROJECTS_ALL_COLUMNS, IAbstractModel.MODEL_TYPE_PROJECT,
    //</editor-fold>

    //<editor-fold desc="ref_Locations">
    public static final String T_REF_LOCATIONS = "ref_Locations";
    public static final String C_REF_LOCATIONS_NAME = "Name";
    public static final String C_REF_LOCATIONS_LON = "Lon";
    public static final String C_REF_LOCATIONS_LAT = "Lat";
    private static final String C_REF_LOCATIONS_RADIUS = "Radius";
    public static final String C_REF_LOCATIONS_ADDRESS = "Address";
    public static final String[] T_REF_LOCATIONS_ALL_COLUMNS = {
            C_ID, C_SYNC_FBID, C_SYNC_TS, C_SYNC_DELETED, C_SYNC_DIRTY, C_SYNC_LASTEDITED,
            C_REF_LOCATIONS_NAME, C_REF_LOCATIONS_LON, C_REF_LOCATIONS_LAT, C_REF_LOCATIONS_ADDRESS,
            C_PARENTID, C_ORDERNUMBER, C_FULL_NAME, C_SEARCH_STRING};
    private static final String SQL_CREATE_TABLE_REF_LOCATIONS = "CREATE TABLE " + T_REF_LOCATIONS + " ("
            + COMMON_FIELDS
            + C_REF_LOCATIONS_NAME + " TEXT NOT NULL, "
            + C_REF_LOCATIONS_LON + " REAL NOT NULL, "
            + C_REF_LOCATIONS_LAT + " REAL NOT NULL, "
            + C_REF_LOCATIONS_RADIUS + " INTEGER, "
            + C_REF_LOCATIONS_ADDRESS + " TEXT, "
            + C_PARENTID + " INTEGER REFERENCES [" + T_REF_LOCATIONS + "]([" + C_ID + "]) ON DELETE SET NULL ON UPDATE CASCADE, "
            + C_ORDERNUMBER + " INTEGER, "
            + C_FULL_NAME + " TEXT, "
            + C_SEARCH_STRING + " TEXT, "
            + "UNIQUE (" + C_REF_LOCATIONS_NAME + ", " + C_PARENTID + ", " + C_SYNC_DELETED + ") ON CONFLICT ABORT);";
    //private static TableInfo infoLocations = new TableInfo(T_REF_LOCATIONS, SQL_CREATE_TABLE_REF_LOCATIONS, T_REF_LOCATIONS_ALL_COLUMNS, IAbstractModel.MODEL_TYPE_LOCATION,
    //</editor-fold>

    //<editor-fold desc="Departments">
    public static final String T_REF_DEPARTMENTS = "ref_Departments";
    public static final String C_REF_DEPARTMENTS_NAME = "Name";
    public static final String C_REF_DEPARTMENTS_ISACTIVE = "IsActive";//Boolean
    public static final String[] T_REF_DEPARTMENTS_ALL_COLUMNS = {
            C_ID, C_SYNC_FBID, C_SYNC_TS, C_SYNC_DELETED, C_SYNC_DIRTY, C_SYNC_LASTEDITED,
            C_REF_DEPARTMENTS_NAME, C_REF_DEPARTMENTS_ISACTIVE, C_PARENTID, C_ORDERNUMBER, C_FULL_NAME, C_SEARCH_STRING};
    private static final String SQL_CREATE_TABLE_REF_DEPARTMENTS = "CREATE TABLE " + T_REF_DEPARTMENTS + " ("
            + COMMON_FIELDS
            + C_REF_DEPARTMENTS_NAME + " TEXT NOT NULL, "
            + C_REF_DEPARTMENTS_ISACTIVE + " INTEGER NOT NULL, "
            + C_PARENTID + " INTEGER REFERENCES [" + T_REF_DEPARTMENTS + "]([" + C_ID + "]) ON DELETE SET NULL ON UPDATE CASCADE, "
            + C_ORDERNUMBER + " INTEGER, "
            + C_FULL_NAME + " TEXT, "
            + C_SEARCH_STRING + " TEXT, "
            + "UNIQUE (" + C_REF_DEPARTMENTS_NAME + ", " + C_PARENTID + ", " + C_SYNC_DELETED + ") ON CONFLICT ABORT);";
    //private static TableInfo infoDepartments = new TableInfo(T_REF_DEPARTMENTS, SQL_CREATE_TABLE_REF_DEPARTMENTS, T_REF_DEPARTMENTS_ALL_COLUMNS, IAbstractModel.MODEL_TYPE_DEPARTMENT,
    //</editor-fold>

    //<editor-fold desc="ref_SimpleDebts">
    public static final String T_REF_SIMPLEDEBTS = "ref_SimpleDebts";
    public static final String C_REF_SIMPLEDEBTS_NAME = "Name";
    public static final String C_REF_SIMPLEDEBTS_ISACTIVE = "IsActive";
    public static final String C_REF_SIMPLEDEBTS_START_AMOUNT = "StartAmount";
    public static final String C_REF_SIMPLEDEBTS_CABBAGE = "Currency";
    public static final String[] T_REF_SIMPLEDEBTS_ALL_COLUMNS = {
            C_ID, C_SYNC_FBID, C_SYNC_TS, C_SYNC_DELETED, C_SYNC_DIRTY, C_SYNC_LASTEDITED,
            C_REF_SIMPLEDEBTS_NAME, C_REF_SIMPLEDEBTS_ISACTIVE, C_REF_SIMPLEDEBTS_START_AMOUNT,
            C_REF_SIMPLEDEBTS_CABBAGE, C_SEARCH_STRING};
    private static final String SQL_CREATE_TABLE_REF_SIMPLEDEBTS = "CREATE TABLE " + T_REF_SIMPLEDEBTS + " ("
            + COMMON_FIELDS
            + C_REF_SIMPLEDEBTS_NAME + " TEXT NOT NULL, "
            + C_REF_SIMPLEDEBTS_ISACTIVE + " INTEGER NOT NULL, "
            + C_REF_SIMPLEDEBTS_START_AMOUNT + " REAL NOT NULL, "
            + C_REF_SIMPLEDEBTS_CABBAGE + " INTEGER REFERENCES [" + T_REF_CURRENCIES + "]([" + C_ID + "]) ON DELETE SET NULL ON UPDATE CASCADE, "
            + C_SEARCH_STRING + " TEXT, "
            + "UNIQUE (" + C_REF_SIMPLEDEBTS_NAME + ", " + C_SYNC_DELETED + ") ON CONFLICT ABORT);";
    //private static TableInfo infoSimpleDebts = new TableInfo(T_REF_SIMPLEDEBTS, SQL_CREATE_TABLE_REF_SIMPLEDEBTS, T_REF_SIMPLEDEBTS_ALL_COLUMNS, IAbstractModel.MODEL_TYPE_SIMPLEDEBT,
    //</editor-fold>

    //<editor-fold desc="log_Transactions">
    public static final String T_LOG_TRANSACTIONS = "log_Transactions";
    public static final String C_LOG_TRANSACTIONS_DATETIME = "DateTime";
    public static final String C_LOG_TRANSACTIONS_SRCACCOUNT = "SrcAccount";
    public static final String C_LOG_TRANSACTIONS_PAYEE = "Payee";
    public static final String C_LOG_TRANSACTIONS_CATEGORY = "Category";
    public static final String C_LOG_TRANSACTIONS_AMOUNT = "Amount";
    public static final String C_LOG_TRANSACTIONS_PROJECT = "Project";
    public static final String C_LOG_TRANSACTIONS_SIMPLEDEBT = "SimpleDebt";
    public static final String C_LOG_TRANSACTIONS_DEPARTMENT = "Department";
    public static final String C_LOG_TRANSACTIONS_LOCATION = "Location";
    public static final String C_LOG_TRANSACTIONS_COMMENT = "Comment";
    public static final String C_LOG_TRANSACTIONS_FILE = "File";
    public static final String C_LOG_TRANSACTIONS_DESTACCOUNT = "DestAccount";
    public static final String C_LOG_TRANSACTIONS_EXCHANGERATE = "ExchangeRate";
    public static final String C_LOG_TRANSACTIONS_AUTOCREATED = "AutoCreated";
    public static final String C_LOG_TRANSACTIONS_LON = "Lon";
    public static final String C_LOG_TRANSACTIONS_LAT = "Lat";
    public static final String C_LOG_TRANSACTIONS_ACCURACY = "Accuracy";
    public static final String C_LOG_TRANSACTIONS_FN = "FN";
    public static final String C_LOG_TRANSACTIONS_FD = "FD";
    public static final String C_LOG_TRANSACTIONS_FP = "FP";
    public static final String C_LOG_TRANSACTIONS_SPLIT = "Split";
    public static final String I_LOG_TRANSACTIONS_IDX = "CREATE INDEX [idx] ON [log_Transactions] ([Deleted], [DateTime], [SrcAccount], [DestAccount], [Payee], [Category], [Project], [Department], [Location], [SimpleDebt]);";
    public static final String[] T_LOG_TRANSACTIONS_ALL_COLUMNS = {
            C_ID, C_SYNC_FBID, C_SYNC_TS, C_SYNC_DELETED, C_SYNC_DIRTY, C_SYNC_LASTEDITED,
            C_LOG_TRANSACTIONS_DATETIME, C_LOG_TRANSACTIONS_SRCACCOUNT, C_LOG_TRANSACTIONS_PAYEE,
            C_LOG_TRANSACTIONS_CATEGORY, C_LOG_TRANSACTIONS_AMOUNT, C_LOG_TRANSACTIONS_PROJECT,
            C_LOG_TRANSACTIONS_SIMPLEDEBT, C_LOG_TRANSACTIONS_DEPARTMENT, C_LOG_TRANSACTIONS_LOCATION,
            C_LOG_TRANSACTIONS_COMMENT, C_LOG_TRANSACTIONS_FILE, C_LOG_TRANSACTIONS_DESTACCOUNT,
            C_LOG_TRANSACTIONS_EXCHANGERATE, C_LOG_TRANSACTIONS_AUTOCREATED, C_LOG_TRANSACTIONS_LON,
            C_LOG_TRANSACTIONS_LAT, C_LOG_TRANSACTIONS_ACCURACY, C_SEARCH_STRING,
            C_LOG_TRANSACTIONS_FN, C_LOG_TRANSACTIONS_FD, C_LOG_TRANSACTIONS_FP, C_LOG_TRANSACTIONS_SPLIT};
    private static final String SQL_CREATE_TABLE_LOG_TRANSACTIONS = "CREATE TABLE " + T_LOG_TRANSACTIONS + " ("
            + COMMON_FIELDS
            + C_LOG_TRANSACTIONS_DATETIME + " INTEGER NOT NULL, "
            + C_LOG_TRANSACTIONS_SRCACCOUNT + " INTEGER NOT NULL REFERENCES [" + T_REF_ACCOUNTS + "]([" + C_ID + "]) ON DELETE CASCADE ON UPDATE CASCADE, "
            + C_LOG_TRANSACTIONS_PAYEE + " INTEGER REFERENCES [" + T_REF_PAYEES + "]([" + C_ID + "]) ON DELETE SET NULL ON UPDATE CASCADE, "
            + C_LOG_TRANSACTIONS_CATEGORY + " INTEGER REFERENCES [" + T_REF_CATEGORIES + "]([" + C_ID + "]) ON DELETE SET NULL ON UPDATE CASCADE, "
            + C_LOG_TRANSACTIONS_AMOUNT + " REAL NOT NULL, "
            + C_LOG_TRANSACTIONS_PROJECT + " INTEGER REFERENCES [" + T_REF_PROJECTS + "]([" + C_ID + "]) ON DELETE SET NULL ON UPDATE CASCADE, "
            + C_LOG_TRANSACTIONS_SIMPLEDEBT + " INTEGER REFERENCES [" + T_REF_SIMPLEDEBTS + "]([" + C_ID + "]) ON DELETE SET NULL ON UPDATE CASCADE, "
            + C_LOG_TRANSACTIONS_DEPARTMENT + " INTEGER REFERENCES [" + T_REF_DEPARTMENTS + "]([" + C_ID + "]) ON DELETE SET NULL ON UPDATE CASCADE, "
            + C_LOG_TRANSACTIONS_LOCATION + " INTEGER REFERENCES [" + T_REF_LOCATIONS + "]([" + C_ID + "]) ON DELETE SET NULL ON UPDATE CASCADE,"
            + C_LOG_TRANSACTIONS_COMMENT + " TEXT, "
            + C_LOG_TRANSACTIONS_FILE + " TEXT, "
            + C_LOG_TRANSACTIONS_DESTACCOUNT + " INTEGER NOT NULL REFERENCES [" + T_REF_ACCOUNTS + "]([" + C_ID + "]) ON DELETE CASCADE ON UPDATE CASCADE, "
            + C_LOG_TRANSACTIONS_EXCHANGERATE + " REAL NOT NULL, "
            + C_LOG_TRANSACTIONS_AUTOCREATED + " INTEGER NOT NULL, "
            + C_LOG_TRANSACTIONS_LON + " REAL, "
            + C_LOG_TRANSACTIONS_LAT + " REAL, "
            + C_LOG_TRANSACTIONS_ACCURACY + " INTEGER,"
            + C_LOG_TRANSACTIONS_FN + " INTEGER DEFAULT 0,"
            + C_LOG_TRANSACTIONS_FD + " INTEGER DEFAULT 0,"
            + C_LOG_TRANSACTIONS_FP + " INTEGER DEFAULT 0,"
            + C_LOG_TRANSACTIONS_SPLIT + " INTEGER DEFAULT 0,"//0 нет товаров, 1 есть товары
            + C_SEARCH_STRING + " TEXT);";
    //private static TableInfo infoTransactions = new TableInfo(T_LOG_TRANSACTIONS, SQL_CREATE_TABLE_LOG_TRANSACTIONS, T_LOG_TRANSACTIONS_ALL_COLUMNS, IAbstractModel.MODEL_TYPE_TRANSACTION,
    //</editor-fold>

    //<editor-fold desc="log_Templates">
    public static final String T_LOG_TEMPLATES = "log_Templates";
    public static final String C_LOG_TEMPLATES_SRCACCOUNT = "SrcAccount";
    public static final String C_LOG_TEMPLATES_PAYEE = "Payee";
    public static final String C_LOG_TEMPLATES_CATEGORY = "Category";
    public static final String C_LOG_TEMPLATES_AMOUNT = "Amount";
    public static final String C_LOG_TEMPLATES_PROJECT = "Project";
    public static final String C_LOG_TEMPLATES_DEPARTMENT = "Department";
    public static final String C_LOG_TEMPLATES_LOCATION = "Location";
    public static final String C_LOG_TEMPLATES_NAME = "Name";
    public static final String C_LOG_TEMPLATES_DESTACCOUNT = "DestAccount";
    public static final String C_LOG_TEMPLATES_EXCHANGERATE = "ExchangeRate";
    public static final String C_LOG_TEMPLATES_COMMENT = "Comment";
    public static final String C_LOG_TEMPLATES_TYPE = "Type";
    public static final String[] T_LOG_TEMPLATES_ALL_COLUMNS = {
            C_ID, C_SYNC_FBID, C_SYNC_TS, C_SYNC_DELETED, C_SYNC_DIRTY, C_SYNC_LASTEDITED,
            C_LOG_TEMPLATES_SRCACCOUNT, C_LOG_TEMPLATES_PAYEE, C_LOG_TEMPLATES_CATEGORY,
            C_LOG_TEMPLATES_AMOUNT, C_LOG_TEMPLATES_PROJECT, C_LOG_TEMPLATES_DEPARTMENT,
            C_LOG_TEMPLATES_LOCATION, C_LOG_TEMPLATES_NAME, C_LOG_TEMPLATES_DESTACCOUNT,
            C_LOG_TEMPLATES_EXCHANGERATE, C_LOG_TEMPLATES_TYPE, C_LOG_TEMPLATES_COMMENT, C_SEARCH_STRING};
    private static final String SQL_CREATE_TABLE_LOG_TEMPLATES = "CREATE TABLE " + T_LOG_TEMPLATES + " ("
            + COMMON_FIELDS
            + C_LOG_TEMPLATES_SRCACCOUNT + " INTEGER NOT NULL REFERENCES [" + T_REF_ACCOUNTS + "]([" + C_ID + "]) ON DELETE CASCADE ON UPDATE CASCADE, "
            + C_LOG_TEMPLATES_PAYEE + " INTEGER REFERENCES [" + T_REF_PAYEES + "]([" + C_ID + "]) ON DELETE SET NULL ON UPDATE CASCADE, "
            + C_LOG_TEMPLATES_CATEGORY + " INTEGER REFERENCES [" + T_REF_CATEGORIES + "]([" + C_ID + "]) ON DELETE SET NULL ON UPDATE CASCADE, "
            + C_LOG_TEMPLATES_AMOUNT + " REAL NOT NULL, "
            + C_LOG_TEMPLATES_PROJECT + " INTEGER REFERENCES [" + T_REF_PROJECTS + "]([" + C_ID + "]) ON DELETE SET NULL ON UPDATE CASCADE, "
            + C_LOG_TEMPLATES_DEPARTMENT + " INTEGER REFERENCES [" + T_REF_DEPARTMENTS + "]([" + C_ID + "]) ON DELETE SET NULL ON UPDATE CASCADE, "
            + C_LOG_TEMPLATES_LOCATION + " INTEGER REFERENCES [" + T_REF_LOCATIONS + "]([" + C_ID + "]) ON DELETE SET NULL ON UPDATE CASCADE, "
            + C_LOG_TEMPLATES_NAME + " TEXT NOT NULL, "
            + C_LOG_TEMPLATES_DESTACCOUNT + " INTEGER NOT NULL REFERENCES [" + T_REF_ACCOUNTS + "]([" + C_ID + "]) ON DELETE CASCADE ON UPDATE CASCADE, "
            + C_LOG_TEMPLATES_EXCHANGERATE + " REAL NOT NULL, "
            + C_LOG_TEMPLATES_COMMENT + " TEXT, "
            + C_SEARCH_STRING + " TEXT, "
            + C_LOG_TEMPLATES_TYPE + " INTEGER NOT NULL);";
    //private static TableInfo infoTemplates = new TableInfo(T_LOG_TEMPLATES, SQL_CREATE_TABLE_LOG_TEMPLATES, T_LOG_TEMPLATES_ALL_COLUMNS, IAbstractModel.MODEL_TYPE_TEMPLATE,
    //</editor-fold>

    //<editor-fold desc="log_Running_Balance">
    public static final String T_LOG_RB = "log_Running_Balance";
    public static final String C_LOG_RB_ACCOUNT_ID = "AccountID";
    public static final String C_LOG_RB_TRANSACTION_ID = "TransactionID";
    public static final String C_LOG_RB_DATETIME = "DateTimeRB";
    public static final String C_LOG_RB_INCOME = "Income";
    public static final String C_LOG_RB_EXPENSE = "Expense";
    private static final String IDX_RB_ACCOUNTS = "CREATE INDEX idx_RB_Accounts ON log_Running_Balance (AccountID);";
    private static final String IDX_RB_TRANSACTIONS = "CREATE INDEX idx_RB_Transactions ON log_Running_Balance (TransactionID);";
    private static final String IDX_RB_DATETIME = "CREATE INDEX idx_RB_DateTime ON log_Running_Balance (DateTimeRB);";
    private static final String SQL_CREATE_TABLE_LOG_RB = "CREATE TABLE log_Running_Balance (\n" +
            "     AccountID INTEGER NOT NULL,\n" +
            "     TransactionID INTEGER NOT NULL,\n" +
            "     DateTimeRB INTEGER NOT NULL,\n" +
            "     Income REAL NOT NULL,\n" +
            "     Expense REAL NOT NULL,\n" +
            "     PRIMARY KEY (AccountID, TransactionID) );";

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
    //</editor-fold>

    //<editor-fold desc="log_Incoming_SMS">
    public static final String T_LOG_INCOMING_SMS = "log_Incoming_SMS";
    public static final String C_LOG_INCOMING_SMS_DATETIME = "DateTime";
    public static final String C_LOG_INCOMING_SMS_SENDER = "Sender";
    public static final String C_LOG_INCOMING_SMS_BODY = "Body";
    public static final String[] T_LOG_INCOMING_SMS_ALL_COLUMNS = {
            C_ID, C_SYNC_FBID, C_SYNC_TS, C_SYNC_DELETED, C_SYNC_DIRTY, C_SYNC_LASTEDITED,
            C_LOG_INCOMING_SMS_DATETIME, C_LOG_INCOMING_SMS_SENDER, C_LOG_INCOMING_SMS_BODY};
    private static final String SQL_CREATE_TABLE_LOG_INCOMING_SMS = "CREATE TABLE " + T_LOG_INCOMING_SMS + " ("
            + COMMON_FIELDS
            + C_LOG_INCOMING_SMS_DATETIME + " TEXT NOT NULL, "
            + C_LOG_INCOMING_SMS_SENDER + " INTEGER NOT NULL, "
            + C_LOG_INCOMING_SMS_BODY + " TEXT NOT NULL);";
    //private static TableInfo infoIncomingSMS = new TableInfo(T_LOG_INCOMING_SMS, SQL_CREATE_TABLE_LOG_INCOMING_SMS, T_LOG_INCOMING_SMS_ALL_COLUMNS, IAbstractModel.MODEL_TYPE_SMS,
    //</editor-fold>

    //<editor-fold desc="ref_Sms_Parser_Patterns">
    public static final String T_LOG_SMS_PARSER_PATTERNS = "ref_Sms_Parser_Patterns";
    public static final String C_LOG_SMS_PARSER_PATTERNS_TYPE = "Type";
    public static final String C_LOG_SMS_PARSER_PATTERNS_OBJECT = "Object";
    public static final String C_LOG_SMS_PARSER_PATTERNS_PATTERN = "Pattern";
    public static final String[] T_LOG_SMS_PARSER_PATTERNS_ALL_COLUMNS = {
            C_ID, C_SYNC_FBID, C_SYNC_TS, C_SYNC_DELETED, C_SYNC_DIRTY, C_SYNC_LASTEDITED,
            C_LOG_SMS_PARSER_PATTERNS_TYPE, C_LOG_SMS_PARSER_PATTERNS_OBJECT, C_LOG_SMS_PARSER_PATTERNS_PATTERN};
    private static final String SQL_CREATE_TABLE_LOG_SMS_PARSER_PATTERNS = "CREATE TABLE " + T_LOG_SMS_PARSER_PATTERNS + " ("
            + COMMON_FIELDS
            + C_LOG_SMS_PARSER_PATTERNS_TYPE + " INTEGER NOT NULL, "
            + C_LOG_SMS_PARSER_PATTERNS_OBJECT + " TEXT NOT NULL, "
            + C_LOG_SMS_PARSER_PATTERNS_PATTERN + " TEXT NOT NULL, "
            + "UNIQUE (" + C_LOG_SMS_PARSER_PATTERNS_TYPE + ", " + C_LOG_SMS_PARSER_PATTERNS_PATTERN + ", " + C_SYNC_DELETED + ") ON CONFLICT REPLACE);";
    //private static TableInfo infoSmsParserPatterns = new TableInfo(T_LOG_SMS_PARSER_PATTERNS, SQL_CREATE_TABLE_LOG_SMS_PARSER_PATTERNS, T_LOG_SMS_PARSER_PATTERNS_ALL_COLUMNS, IAbstractModel.MODEL_TYPE_SMSMARKER,
    //</editor-fold>

    //<editor-fold desc="ref_Debts">
    public static final String T_REF_DEBTS = "ref_Debts";
    public static final String C_REF_DEBTS_ACCOUNT = "Account";
    public static final String C_REF_DEBTS_PAYEE = "Payee";
    public static final String C_REF_DEBTS_CATEGORY = "Category";
    public static final String C_REF_DEBTS_CLOSED = "Closed";
    private static final String C_REF_DEBTS_SRC_ACCOUNT = "SrcAccount";
    public static final String C_REF_DEBTS_COMMENT = "Comment";
    public static final String[] T_REF_DEBTS_ALL_COLUMNS = {
            C_ID, C_SYNC_FBID, C_SYNC_TS, C_SYNC_DELETED, C_SYNC_DIRTY, C_SYNC_LASTEDITED,
            C_REF_DEBTS_ACCOUNT, C_REF_DEBTS_PAYEE, C_REF_DEBTS_CATEGORY, C_REF_DEBTS_CLOSED,
            C_REF_DEBTS_COMMENT};
    private static final String SQL_CREATE_TABLE_REF_DEBTS = "CREATE TABLE [" + T_REF_DEBTS + "] (\n"
            + COMMON_FIELDS
            + C_REF_DEBTS_ACCOUNT + " INTEGER NOT NULL ON CONFLICT ABORT REFERENCES [" + T_REF_ACCOUNTS + "]([" + C_ID + "]) ON DELETE CASCADE ON UPDATE CASCADE,\n"
            + C_REF_DEBTS_PAYEE + " INTEGER NOT NULL REFERENCES [" + T_REF_PAYEES + "]([" + C_ID + "]) ON DELETE CASCADE ON UPDATE CASCADE,\n"
            + C_REF_DEBTS_CATEGORY + " INTEGER NOT NULL REFERENCES [" + T_REF_CATEGORIES + "]([" + C_ID + "]) ON DELETE SET NULL ON UPDATE CASCADE,\n"
            + C_REF_DEBTS_CLOSED + " INTEGER NOT NULL,\n"
            + C_REF_DEBTS_SRC_ACCOUNT + " INTEGER,\n"
            + C_REF_DEBTS_COMMENT + " TEXT, "
            + "UNIQUE (" + C_REF_DEBTS_ACCOUNT + ", " + C_SYNC_DELETED + ") ON CONFLICT ABORT);";
    //private static TableInfo infoDebts = new TableInfo(T_REF_DEBTS, SQL_CREATE_TABLE_REF_DEBTS, T_REF_DEBTS_ALL_COLUMNS, IAbstractModel.MODEL_TYPE_CREDIT,
    //</editor-fold>

    //<editor-fold desc="ref_budget">
    public static final String T_LOG_BUDGET = "ref_budget";
    public static final String C_LOG_BUDGET_YEAR = "Year";
    public static final String C_LOG_BUDGET_MONTH = "Month";
    public static final String C_LOG_BUDGET_CATEGORY = "Category";
    public static final String C_LOG_BUDGET_AMOUNT = "Amount";
    public static final String C_LOG_BUDGET_CURRENCY = "Currency";
    public static final String[] T_LOG_BUDGET_ALL_COLUMNS = {
            C_ID, C_SYNC_FBID, C_SYNC_TS, C_SYNC_DELETED, C_SYNC_DIRTY, C_SYNC_LASTEDITED,
            C_LOG_BUDGET_YEAR, C_LOG_BUDGET_MONTH, C_LOG_BUDGET_CATEGORY, C_LOG_BUDGET_AMOUNT,
            C_LOG_BUDGET_CURRENCY};
    private static final String SQL_CREATE_TABLE_LOG_BUDGET = "CREATE TABLE [" + T_LOG_BUDGET + "] (\n"
            + COMMON_FIELDS
            + C_LOG_BUDGET_YEAR + " INTEGER NOT NULL,\n"
            + C_LOG_BUDGET_MONTH + " INTEGER NOT NULL,\n"
            + C_LOG_BUDGET_CATEGORY + " INTEGER NOT NULL REFERENCES [" + T_REF_CATEGORIES + "]([" + C_ID + "]) ON DELETE CASCADE ON UPDATE CASCADE,\n"
            + C_LOG_BUDGET_AMOUNT + " REAL NOT NULL,\n"
            + C_LOG_BUDGET_CURRENCY + " INTEGER NOT NULL REFERENCES [" + T_REF_CURRENCIES + "]([" + C_ID + "]) ON DELETE CASCADE ON UPDATE CASCADE,\n"
            + "UNIQUE (" + C_SYNC_DELETED + ", " + C_LOG_BUDGET_YEAR + ", " + C_LOG_BUDGET_MONTH + ", " + C_LOG_BUDGET_CATEGORY + ", " + C_LOG_BUDGET_CURRENCY + ") ON CONFLICT REPLACE);";
    //private static TableInfo infoBudget = new TableInfo(T_LOG_BUDGET, SQL_CREATE_TABLE_LOG_BUDGET, T_LOG_BUDGET_ALL_COLUMNS, IAbstractModel.MODEL_TYPE_BUDGET,
    //</editor-fold>

    //<editor-fold desc="ref_budget_debts">
    public static final String T_LOG_BUDGET_DEBTS = "ref_budget_debts";
    public static final String C_LOG_BUDGET_DEBTS_YEAR = "Year";
    public static final String C_LOG_BUDGET_DEBTS_MONTH = "Month";
    public static final String C_LOG_BUDGET_DEBTS_CREDIT = "Debt";
    public static final String C_LOG_BUDGET_DEBTS_AMOUNT = "Amount";
    public static final String[] T_LOG_BUDGET_DEBTS_ALL_COLUMNS = {
            C_ID, C_SYNC_FBID, C_SYNC_TS, C_SYNC_DELETED, C_SYNC_DIRTY, C_SYNC_LASTEDITED,
            C_LOG_BUDGET_DEBTS_YEAR, C_LOG_BUDGET_DEBTS_MONTH, C_LOG_BUDGET_DEBTS_CREDIT,
            C_LOG_BUDGET_DEBTS_AMOUNT};
    private static final String SQL_CREATE_TABLE_LOG_BUDGET_DEBTS = "CREATE TABLE [" + T_LOG_BUDGET_DEBTS + "] (\n"
            + COMMON_FIELDS
            + C_LOG_BUDGET_DEBTS_YEAR + " INTEGER NOT NULL,\n"
            + C_LOG_BUDGET_DEBTS_MONTH + " INTEGER NOT NULL,\n"
            + C_LOG_BUDGET_DEBTS_CREDIT + " INTEGER NOT NULL REFERENCES [" + T_REF_DEBTS + "]([" + C_ID + "]) ON DELETE CASCADE ON UPDATE CASCADE,\n"
            + C_LOG_BUDGET_DEBTS_AMOUNT + " REAL NOT NULL,\n"
            + "UNIQUE (" + C_SYNC_DELETED + ", " + C_LOG_BUDGET_DEBTS_YEAR + ", " + C_LOG_BUDGET_DEBTS_MONTH + ", " + C_LOG_BUDGET_DEBTS_CREDIT + ") ON CONFLICT REPLACE);";
    //private static TableInfo infoBudgetDebts = new TableInfo(T_LOG_BUDGET_DEBTS, SQL_CREATE_TABLE_LOG_BUDGET_DEBTS, T_LOG_BUDGET_DEBTS_ALL_COLUMNS, IAbstractModel.MODEL_TYPE_BUDGET_DEBT,
    //</editor-fold>

    //<editor-fold desc="ref_Senders">
    public static final String T_REF_SENDERS = "ref_Senders";
    public static final String C_REF_SENDERS_NAME = "Name";
    public static final String C_REF_SENDERS_PHONENO = "PhoneNo";
    public static final String C_REF_SENDERS_AMOUNTPOS = "AmountPos";
    public static final String C_REF_SENDERS_BALANCEPOS = "BalancePos";
    public static final String C_REF_SENDERS_DATEFORMAT = "DateFormat";
    public static final String C_REF_SENDERS_LEADING_CURRENCY_SYMBOL = "LeadingCurrencySymbol";
    public static final String C_REF_SENDERS_ISACTIVE = "IsActive";//Boolean
    public static final String C_REF_SENDERS_ADD_CREDIT_LIMIT_TO_BALANCE = "AddCreditLimitToBalance";//Boolean
    public static final String[] T_REF_SENDERS_ALL_COLUMNS = {
            C_ID, C_SYNC_FBID, C_SYNC_TS, C_SYNC_DELETED, C_SYNC_DIRTY, C_SYNC_LASTEDITED,
            C_REF_SENDERS_NAME, C_REF_SENDERS_PHONENO, C_REF_SENDERS_AMOUNTPOS, C_REF_SENDERS_BALANCEPOS,
            C_REF_SENDERS_DATEFORMAT, C_REF_SENDERS_LEADING_CURRENCY_SYMBOL, C_REF_SENDERS_ISACTIVE, C_REF_SENDERS_ADD_CREDIT_LIMIT_TO_BALANCE};
    private static final String SQL_CREATE_TABLE_REF_SENDERS = "CREATE TABLE " + T_REF_SENDERS + " ("
            + COMMON_FIELDS
            + C_REF_SENDERS_NAME + " TEXT NOT NULL, "
            + C_REF_SENDERS_PHONENO + " TEXT NOT NULL, "
            + C_REF_SENDERS_AMOUNTPOS + " INTEGER, "
            + C_REF_SENDERS_BALANCEPOS + " INTEGER, "
            + C_REF_SENDERS_LEADING_CURRENCY_SYMBOL + " INTEGER, "
            + C_REF_SENDERS_DATEFORMAT + " TEXT, "
            + C_REF_SENDERS_ISACTIVE + " INTEGER NOT NULL, "
            + C_REF_SENDERS_ADD_CREDIT_LIMIT_TO_BALANCE + " INTEGER NOT NULL DEFAULT 0, "
            + "UNIQUE (" + C_REF_SENDERS_NAME + ", " + C_SYNC_DELETED + ") ON CONFLICT ABORT, "
            + "UNIQUE (" + C_REF_SENDERS_PHONENO + ", " + C_SYNC_DELETED + ") ON CONFLICT ABORT);";
    //private static TableInfo infoSenders = new TableInfo(T_REF_SENDERS, SQL_CREATE_TABLE_REF_SENDERS, T_REF_SENDERS_ALL_COLUMNS, IAbstractModel.MODEL_TYPE_SENDER,
    //</editor-fold>

    //<editor-fold desc="ref_Products">
    public static final String T_REF_PRODUCTS = "ref_Products";
    public static final String C_REF_PRODUCTS_NAME = "Name";
    public static final String[] T_REF_PRODUCTS_ALL_COLUMNS = {
            C_ID, C_SYNC_FBID, C_SYNC_TS, C_SYNC_DELETED, C_SYNC_DIRTY, C_SYNC_LASTEDITED,
            C_REF_PRODUCTS_NAME, C_SEARCH_STRING};
    public static final String SQL_CREATE_TABLE_REF_PRODUCTS = "CREATE TABLE " + T_REF_PRODUCTS + " ("
            + COMMON_FIELDS
            + C_REF_PRODUCTS_NAME + " TEXT NOT NULL DEFAULT '', "
            + C_SEARCH_STRING + " TEXT NOT NULL DEFAULT '', "
            + "UNIQUE (" + C_REF_PRODUCTS_NAME + ", " + C_SYNC_DELETED + ") ON CONFLICT ABORT);";
    //</editor-fold>

    //<editor-fold desc="log_Products">
    public static final String T_LOG_PRODUCTS = " log_Products";
    public static final String C_LOG_PRODUCTS_TRANSACTIONID = "TransactionID";
    public static final String C_LOG_PRODUCTS_PRODUCTID = "ProductID";
    public static final String C_LOG_PRODUCTS_CATEGORY_ID = "CategoryID";
    public static final String C_LOG_PRODUCTS_PROJECT_ID = "ProjectID";
    public static final String C_LOG_PRODUCTS_PRICE = "Price";
    public static final String C_LOG_PRODUCTS_QUANTITY = "Quantity";
    private static final String I_LOG_PRODUCTS_IDX = "CREATE INDEX [idx_Products] ON [log_Products] ([Deleted], [TransactionID], [ProductID]);";
    public static final String[] T_LOG_PRODUCTS_ALL_COLUMNS = {
            C_ID, C_SYNC_FBID, C_SYNC_TS, C_SYNC_DELETED, C_SYNC_DIRTY, C_SYNC_LASTEDITED,
            C_LOG_PRODUCTS_TRANSACTIONID, C_LOG_PRODUCTS_PRODUCTID, C_LOG_PRODUCTS_CATEGORY_ID, C_LOG_PRODUCTS_PROJECT_ID,
            C_LOG_PRODUCTS_PRICE, C_LOG_PRODUCTS_QUANTITY};
    public static final String SQL_CREATE_TABLE_LOG_PRODUCTS = "CREATE TABLE " + T_LOG_PRODUCTS + "\n("
            + COMMON_FIELDS
            + C_LOG_PRODUCTS_TRANSACTIONID  + " INTEGER REFERENCES [" + T_LOG_TRANSACTIONS + "]([" + C_ID + "]) ON DELETE SET NULL ON UPDATE CASCADE,\n"
            + C_LOG_PRODUCTS_PRODUCTID      + " INTEGER REFERENCES [" +   T_REF_PRODUCTS   + "]([" + C_ID + "]) ON DELETE SET NULL ON UPDATE CASCADE,\n"
            + C_LOG_PRODUCTS_CATEGORY_ID     + " INTEGER DEFAULT -1 REFERENCES [" + T_REF_CATEGORIES + "]([" + C_ID + "]) ON DELETE SET NULL ON UPDATE CASCADE,\n"
            + C_LOG_PRODUCTS_PROJECT_ID + " INTEGER DEFAULT -1 REFERENCES [" +  T_REF_PROJECTS  + "]([" + C_ID + "]) ON DELETE SET NULL ON UPDATE CASCADE,\n"
            + C_LOG_PRODUCTS_PRICE          + " REAL NOT NULL DEFAULT 0,\n"
            + C_LOG_PRODUCTS_QUANTITY       + " REAL NOT NULL DEFAULT 1 CHECK (Quantity >= 0));";
    //</editor-fold>

    //<editor-fold desc="Временные таблицы для подсчета суммы транзакций">
    public static final String T_SEARCH_TRANSACTIONS = "search_Transactions";
    //</editor-fold>

    private final Context mContext;

    private DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.mContext = context.getApplicationContext();
        final DatabaseUpgradeHelper dbh = DatabaseUpgradeHelper.getInstance();
        dbh.setUpgrading(true);
        mDatabase = getWritableDatabase();
        dbh.setUpgrading(false);
        while (dbh.isUpgrading()) {
            SystemClock.sleep(10);
        }
    }

    public synchronized static DBHelper getInstance(Context ctx) {
        if (mInstance == null) {
            mInstance = new DBHelper(ctx);
        }
        return mInstance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        Log.d(TAG, SQL_CREATE_TABLE_REF_CURRENCIES);
        db.execSQL(SQL_CREATE_TABLE_REF_CURRENCIES);

        Cabbage cabbage;
        List<String> codes = Arrays.asList("RUB", "USD", "EUR", "UAH", "BYR", "KZT", "ABC");
        for (String code : codes) {
            cabbage = CabbageManager.createFromCode(code, mContext);
            if (cabbage != null) {
                db.insertOrThrow(T_REF_CURRENCIES, null, cabbage.getCV());
            }
        }

        Log.d(TAG, SQL_CREATE_TABLE_REF_ACCOUNTS);
        db.execSQL(SQL_CREATE_TABLE_REF_ACCOUNTS);

        Log.d(TAG, SQL_CREATE_TABLE_REF_PROJECTS);
        db.execSQL(SQL_CREATE_TABLE_REF_PROJECTS);

        Log.d(TAG, SQL_CREATE_TABLE_REF_DEPARTMENTS);
        db.execSQL(SQL_CREATE_TABLE_REF_DEPARTMENTS);

        Log.d(TAG, SQL_CREATE_TABLE_REF_LOCATIONS);
        db.execSQL(SQL_CREATE_TABLE_REF_LOCATIONS);

        Log.d(TAG, SQL_CREATE_TABLE_REF_CATEGORIES);
        db.execSQL(SQL_CREATE_TABLE_REF_CATEGORIES);

        Log.d(TAG, SQL_CREATE_TABLE_REF_PAYEES);
        db.execSQL(SQL_CREATE_TABLE_REF_PAYEES);

        Log.d(TAG, SQL_CREATE_TABLE_LOG_TRANSACTIONS);
        db.execSQL(SQL_CREATE_TABLE_LOG_TRANSACTIONS);
        db.execSQL(I_LOG_TRANSACTIONS_IDX);

        Log.d(TAG, SQL_CREATE_TABLE_LOG_INCOMING_SMS);
        db.execSQL(SQL_CREATE_TABLE_LOG_INCOMING_SMS);

        Log.d(TAG, SQL_CREATE_TABLE_LOG_SMS_PARSER_PATTERNS);
        db.execSQL(SQL_CREATE_TABLE_LOG_SMS_PARSER_PATTERNS);

        Log.d(TAG, SQL_CREATE_TABLE_REF_DEBTS);
        db.execSQL(SQL_CREATE_TABLE_REF_DEBTS);

        Log.d(TAG, SQL_CREATE_TABLE_LOG_BUDGET);
        db.execSQL(SQL_CREATE_TABLE_LOG_BUDGET);

        Log.d(TAG, SQL_CREATE_TABLE_LOG_BUDGET_DEBTS);
        db.execSQL(SQL_CREATE_TABLE_LOG_BUDGET_DEBTS);

        Log.d(TAG, SQL_CREATE_TABLE_LOG_TEMPLATES);
        db.execSQL(SQL_CREATE_TABLE_LOG_TEMPLATES);

        Log.d(TAG, SQL_CREATE_TABLE_REF_SIMPLEDEBTS);
        db.execSQL(SQL_CREATE_TABLE_REF_SIMPLEDEBTS);

        Log.d(TAG, SQL_CREATE_TABLE_REF_SENDERS);
        db.execSQL(SQL_CREATE_TABLE_REF_SENDERS);

        Log.d(TAG, SQL_CREATE_TABLE_REF_ACCOUNTS_SETS);
        db.execSQL(SQL_CREATE_TABLE_REF_ACCOUNTS_SETS);

        Log.d(TAG, SQL_CREATE_TABLE_LOG_ACCOUNTS_SETS);
        db.execSQL(SQL_CREATE_TABLE_LOG_ACCOUNTS_SETS);

        Log.d(TAG, SQL_CREATE_TABLE_REF_PRODUCTS);
        db.execSQL(SQL_CREATE_TABLE_REF_PRODUCTS);

        Log.d(TAG, SQL_CREATE_TABLE_LOG_PRODUCTS);
        db.execSQL(SQL_CREATE_TABLE_LOG_PRODUCTS);
        db.execSQL(I_LOG_PRODUCTS_IDX);

        ContentValues cv = new ContentValues();
        cv.put(C_ID, 0);
        cv.put(C_REF_PRODUCTS_NAME, "default_product");
        db.insert(T_REF_PRODUCTS, "", cv);

        db.execSQL(SQL_CREATE_TABLE_LOG_RB);
        db.execSQL(IDX_RB_ACCOUNTS);
        db.execSQL(IDX_RB_TRANSACTIONS);
        db.execSQL(IDX_RB_DATETIME);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
//        db.setForeignKeyConstraintsEnabled(true);
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
            UpdateHelper.update27(db, new IUpdateRunningBalance() {
                @Override
                public void updateRunningBalance(SQLiteDatabase database) throws IOException {
                    DBHelper.this.updateRunningBalance(db);
                }
            });
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

        String tableNames[] = new String[]{T_REF_ACCOUNTS, T_REF_ACCOUNTS_SETS, T_LOG_ACCOUNTS_SETS,
                T_REF_CATEGORIES, T_REF_PAYEES, T_REF_PROJECTS, T_REF_LOCATIONS, T_REF_DEPARTMENTS,
                T_REF_SIMPLEDEBTS, T_LOG_TRANSACTIONS, T_LOG_TEMPLATES, T_LOG_INCOMING_SMS, T_LOG_SMS_PARSER_PATTERNS,
                T_REF_DEBTS, T_LOG_BUDGET, T_LOG_BUDGET_DEBTS, T_REF_SENDERS, T_REF_PRODUCTS, T_LOG_PRODUCTS
        };

        for (String tableName : tableNames) {
            db.delete(tableName, "Deleted > 0", null);
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

    public void updateLogProducts(SQLiteDatabase db) throws IOException {
        db.execSQL("DELETE FROM log_Products WHERE TransactionID < 0");

        Cursor cursor = db.rawQuery("SELECT _id, Amount FROM log_Transactions " +
                "WHERE _id not in (SELECT TransactionID FROM log_Products) AND Deleted = 0", null);

        ContentValues cv = new ContentValues();

        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    while (!cursor.isAfterLast()) {
                        cv.clear();
                        cv.put(DBHelper.C_SYNC_FBID, "");
                        cv.put(DBHelper.C_SYNC_TS, -1);
                        cv.put(DBHelper.C_SYNC_DELETED, 0);
                        cv.put(DBHelper.C_SYNC_DIRTY, 0);
                        cv.put(DBHelper.C_SYNC_LASTEDITED, "");
                        cv.put(DBHelper.C_LOG_PRODUCTS_TRANSACTIONID, cursor.getLong(0));
                        cv.put(DBHelper.C_LOG_PRODUCTS_PRODUCTID, 0);
                        cv.put(DBHelper.C_LOG_PRODUCTS_CATEGORY_ID, -1);
                        cv.put(DBHelper.C_LOG_PRODUCTS_PROJECT_ID, -1);
                        cv.put(DBHelper.C_LOG_PRODUCTS_PRICE, cursor.getDouble(1));
                        cv.put(DBHelper.C_LOG_PRODUCTS_QUANTITY, 1);
                        db.insert(DBHelper.T_LOG_PRODUCTS, null, cv);
                        cursor.moveToNext();
                    }
                }
            } finally {
                cursor.close();
            }
        }
    }

    public static void updateFullNames(String tableName, boolean useFullName, SQLiteDatabase db) {
        long t = System.currentTimeMillis();
        String nameColumn;
        if (tableName.equals(T_LOG_TRANSACTIONS)) {
            nameColumn = useFullName ? getFullNameColumn(tableName) : C_LOG_TRANSACTIONS_COMMENT;
        } else {
            nameColumn = useFullName ? getFullNameColumn(tableName) : "Name";
        }
        String fields[];
        if (useFullName) {
            fields = new String[]{C_ID, nameColumn, C_SEARCH_STRING, C_FULL_NAME};
        } else {
            fields = new String[]{C_ID, nameColumn, C_SEARCH_STRING};
        }
        Cursor cursor = db.query(tableName, fields, "Deleted = 0", null, null, null, null);
        ContentValues cv = new ContentValues();
        String translit;
        int i = 0;
        try {
            if (cursor.moveToFirst()) {
                while (!cursor.isAfterLast()) {
                    cv.clear();
                    if (useFullName) {
                        cv.put(C_FULL_NAME, cursor.getString(1));
                    }
                    translit = Translit.toTranslit(cursor.getString(1).toLowerCase());
                    if (!cursor.getString(2).equals(translit)) {
                        cv.put(C_SEARCH_STRING, translit);
                    }
                    if (cv.size() != 0) {
                        db.update(tableName, cv, "_id = " + cursor.getString(0), null);
                    }
                    cursor.moveToNext();
                    i++;
//                    Log.d(TAG, cursor.getString(0));
                }
            }
        } finally {
            cursor.close();
        }
//        t = System.currentTimeMillis() - t;
//        Log.d(TAG, "Update full names in " + tableName + " - " + String.valueOf(t) + "ms");
    }

    private String getDbPath() {
        return mContext.getDatabasePath(DATABASE_NAME).toString();
    }

    public File backupDB(boolean vacuum) throws IOException {
        File backup = null;
        if (vacuum) {
            mDatabase.execSQL("VACUUM");
        }
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            String backupPath = FileUtils.getExtFingenBackupFolder();
            String alpha = "";
            if (BuildConfig.FLAVOR.equals("nd")) alpha = "_alpha";
            @SuppressLint("SimpleDateFormat") String backupFile = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date()) + alpha + ".zip";

            if (!backupPath.isEmpty()) {
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
                String password = preferences.getString("backup_password", "");
                Boolean enableProtection = preferences.getBoolean("enable_backup_password", false);
                if (enableProtection && !password.isEmpty()) {
                    backup = FileUtils.zipAndEncrypt(getDbPath(), backupPath + backupFile, password);
                } else {
                    backup = FileUtils.zip(new String[]{getDbPath()}, backupPath + backupFile);
                }
                Log.d(TAG, String.format("File %s saved", backupFile));
            }
        }
        return backup;
    }

    void showRestoreDialog(final String filename, final AppCompatActivity activity) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(R.string.ttl_confirm_action);
        builder.setMessage(R.string.msg_confirm_restore_db);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                DBHelper.this.restoreDB(filename, activity);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

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
//        SQLiteDatabase db = getWritableDatabase();
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
        values.put(DBHelper.C_SYNC_FBID, baseModel.getFBID());
        values.put(DBHelper.C_SYNC_TS, baseModel.getTS());
        values.put(DBHelper.C_SYNC_DELETED, 0);
        values.put(DBHelper.C_SYNC_DIRTY, baseModel.isDirty() ? 1 : 0);
        values.put(DBHelper.C_SYNC_LASTEDITED, baseModel.getLastEdited());
        return values;
    }

    public static BaseModel getSyncDataFromCursor(BaseModel baseModel, Cursor cursor, HashMap<String, Integer> columnIndexes) {
//            baseModel.setFBID(cursor.getString(columnIndexes.get(C_SYNC_FBID)));

//            baseModel.setDeleted(cursor.getInt(columnIndexes.get(C_SYNC_DELETED)) > 0);

//            baseModel.setTS(cursor.getLong(columnIndexes.get(C_SYNC_TS)));

//            baseModel.setDirty(cursor.getInt(columnIndexes.get(C_SYNC_DIRTY)) == 1);

//            baseModel.setLastEdited(cursor.getString(columnIndexes.get(C_SYNC_LASTEDITED)));

        return baseModel;
    }

    public synchronized void clearSyncData() {
//        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DBHelper.C_SYNC_FBID, "");
        values.put(DBHelper.C_SYNC_TS, -1);
//        values.put(DBHelper.C_SYNC_DELETED, 0);
        values.put(DBHelper.C_SYNC_DIRTY, false);
        values.put(DBHelper.C_SYNC_LASTEDITED, "");
        mDatabase.beginTransaction();
//        for (TableInfo tableInfo : TABLE_INFO) {
//            mDatabase.update(tableInfo.getTableName(), values, null, null);
//        }
        mDatabase.setTransactionSuccessful();
        mDatabase.endTransaction();
    }

    public synchronized static int getMaxDel(SQLiteDatabase db, String tableName) {
        Cursor cursor = db.query(tableName, new String[]{String.format("MAX(%s) AS MAXDEL", DBHelper.C_SYNC_DELETED)}, null, null, null, null, null);
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

