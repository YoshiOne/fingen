package com.yoshione.fingen.utils;

/*
 Created by slv on 23.10.2015.
 CSV Import/Export unit
 */

import android.annotation.SuppressLint;
import android.content.Context;

import io.requery.android.database.sqlite.SQLiteDatabase;

import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;

import com.yoshione.fingen.DBHelper;
import com.yoshione.fingen.dao.AbstractDAO;
import com.yoshione.fingen.dao.AccountsDAO;
import com.yoshione.fingen.dao.CabbagesDAO;
import com.yoshione.fingen.dao.CategoriesDAO;
import com.yoshione.fingen.dao.DepartmentsDAO;
import com.yoshione.fingen.dao.LocationsDAO;
import com.yoshione.fingen.dao.PayeesDAO;
import com.yoshione.fingen.dao.ProjectsDAO;
import com.yoshione.fingen.dao.TransactionsDAO;
import com.yoshione.fingen.interfaces.IAbstractModel;
import com.yoshione.fingen.interfaces.IProgressEventsListener;
import com.yoshione.fingen.managers.AccountManager;
import com.yoshione.fingen.managers.CabbageManager;
import com.yoshione.fingen.managers.TransactionManager;
import com.yoshione.fingen.model.Account;
import com.yoshione.fingen.model.BaseModel;
import com.yoshione.fingen.model.Cabbage;
import com.yoshione.fingen.model.Category;
import com.yoshione.fingen.model.Location;
import com.yoshione.fingen.model.Payee;
import com.yoshione.fingen.model.Project;
import com.yoshione.fingen.model.Transaction;

import org.mozilla.universalchardet.UniversalDetector;

import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Currency;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import au.com.bytecode.opencsv.CSV;
import au.com.bytecode.opencsv.CSVReadProc;
import au.com.bytecode.opencsv.CSVWriteProc;
import au.com.bytecode.opencsv.CSVWriter;

public class CsvImporter {
    private static final String TAG = "CsvImporter";

    private static final char sSeparators[] = {//возможные разделители
            '\t',//табуляция
            ';',//точка с запятой
            ',',//запятая
            ' '//пробел
    };
    private static final String sDateFormats[] = {
            "yyyy-MM-dd HH:mm:ss",
            "MM/dd/yyyy HH:mm:ss",
            "dd.MM.yyyy HH:mm:ss",
            "yyyy-MM-dd HH:mm",
            "MM/dd/yyyy HH:mm",
            "dd.MM.yyyy HH:mm",
            "dd.MM.yyyy, HH:mm",
            "dd.MM.yy",
            "yyyy-MM-dd",
            "MM/dd/yyyy",
            "dd.MM.yyyy"};
    private final String mFileName;
    private final Context mContext;
    private char mSeparator = ';';
    private char mQuote = '"';
    private int mCount = 0;
    private int mCurrentRow = 0;
    private int mSkipLines = 0;
    private String mCharset = "UTF-8";
    private IProgressEventsListener mCsvImportProgressChangeListener;
    private String mHeaders[];
    private boolean mIsValidDateFormat;
    private List<Integer> mListColumnsCount;

    public CsvImporter(Context mContext, String fileName, int skipLines, boolean export) {
        this.mContext = mContext;
        mFileName = fileName;
        mSkipLines = skipLines;
        if (!export) {
            try {
                mCharset = detectCharset();
                mSeparator = detectSeparators();
                mQuote = '"';
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void setmCsvImportProgressChangeListener(IProgressEventsListener mCsvImportProgressChangeListener) {
        this.mCsvImportProgressChangeListener = mCsvImportProgressChangeListener;
    }

    @SuppressLint("SimpleDateFormat")
    public void saveCSV(final List<Transaction> transactions) {
        final CSV csv = CSV
                .separator(mSeparator)  // delimiter of fields
                .quote(mQuote)      // quote character
                .charset(mCharset)
                .create();

        mCount = transactions.size();

        csv.write(mFileName, new CSVWriteProc() {
            @Override
            public void process(CSVWriter out) {
                out.writeNext("date", "time", "account", "amount", "currency", "type", "exrate", "category", "payee", "location", "project", "department", "note", "lon", "lat");
                String date;
                String time;
                String account;
                String amount;
                String type;
                String exrate;
                String currency;
                int decimalCount;
                String category;
                String payee;
                String location;
                String project;
                String department;
                String note;
                String lon;
                String lat;
                HashMap<Long, String> categoriesNameCache = new HashMap<>();
                HashMap<Long, String> payeesNameCache = new HashMap<>();
                HashMap<Long, String> projectsNameCache = new HashMap<>();
                HashMap<Long, String> locationsNameCache = new HashMap<>();
                HashMap<Long, String> departmentsNameCache = new HashMap<>();
                HashMap<Long, String> accountsNameCache = new HashMap<>();
                HashMap<Long, String> cabbagesNameCache = new HashMap<>();
                HashMap<Long, String> lonsCache = new HashMap<>();
                HashMap<Long, String> latsCache = new HashMap<>();
                HashMap<Long, Integer> cabbagesDecimalCountCache = new HashMap<>();

                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                DateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
                Account accountObj;
                Cabbage cabbageObj;

                mCurrentRow = 0;
                try {
                    for (Transaction transaction : transactions) {
                        date = dateFormat.format(transaction.getDateTime());
                        time = timeFormat.format(transaction.getDateTime());

                        if (accountsNameCache.containsKey(transaction.getAccountID())) {
                            account = accountsNameCache.get(transaction.getAccountID());
                            currency = cabbagesNameCache.get(transaction.getAccountID());
                            decimalCount = cabbagesDecimalCountCache.get(transaction.getAccountID());
                        } else {
                            accountObj = TransactionManager.getSrcAccount(transaction, mContext);
                            account = accountObj.getName();
                            cabbageObj = AccountManager.getCabbage(accountObj, mContext);
                            currency = cabbageObj.getCode();
                            decimalCount = cabbageObj.getDecimalCount();
                            accountsNameCache.put(transaction.getAccountID(), account);
                            cabbagesNameCache.put(transaction.getAccountID(), currency);
                            cabbagesDecimalCountCache.put(transaction.getAccountID(), decimalCount);
                        }
                        if (categoriesNameCache.containsKey(transaction.getCategoryID())) {
                            category = categoriesNameCache.get(transaction.getCategoryID());
                        } else {
                            category = CategoriesDAO.getInstance(mContext).getCategoryByID(transaction.getCategoryID()).getFullName();
                            category = category.replaceAll("\\\\", ":");
                            categoriesNameCache.put(transaction.getCategoryID(), category);
                        }
                        if (payeesNameCache.containsKey(transaction.getPayeeID())) {
                            payee = payeesNameCache.get(transaction.getPayeeID());
                        } else {
                            payee = PayeesDAO.getInstance(mContext).getPayeeByID(transaction.getPayeeID()).getFullName();
                            payee = payee.replaceAll("\\\\", ":");
                            payeesNameCache.put(transaction.getPayeeID(), payee);
                        }
                        if (projectsNameCache.containsKey(transaction.getProjectID())) {
                            project = projectsNameCache.get(transaction.getProjectID());
                        } else {
                            project = ProjectsDAO.getInstance(mContext).getProjectByID(transaction.getProjectID()).getFullName();
                            project = project.replaceAll("\\\\", ":");
                            projectsNameCache.put(transaction.getProjectID(), project);
                        }
                        if (locationsNameCache.containsKey(transaction.getLocationID())) {
                            location = locationsNameCache.get(transaction.getLocationID());
                            lon = lonsCache.get(transaction.getLocationID());
                            lat = latsCache.get(transaction.getLocationID());
                        } else {
                            location = LocationsDAO.getInstance(mContext).getLocationByID(transaction.getLocationID()).getFullName();
                            location = location.replaceAll("\\\\", ":");
                            lon = String.valueOf(LocationsDAO.getInstance(mContext).getLocationByID(transaction.getLocationID()).getLon());
                            lat = String.valueOf(LocationsDAO.getInstance(mContext).getLocationByID(transaction.getLocationID()).getLat());
                            locationsNameCache.put(transaction.getLocationID(), location);
                            lonsCache.put(transaction.getLocationID(), lon);
                            latsCache.put(transaction.getLocationID(), lat);
                        }
                        if (Double.valueOf(lon) == 0d & Double.valueOf(lat) == 0d) {
                            lon = String.valueOf(transaction.getLon());
                            lat = String.valueOf(transaction.getLat());
                        }
                        if (departmentsNameCache.containsKey(transaction.getDepartmentID())) {
                            department = departmentsNameCache.get(transaction.getDepartmentID());
                        } else {
                            department = DepartmentsDAO.getInstance(mContext).getDepartmentByID(transaction.getDepartmentID()).getFullName();
                            department = department.replaceAll("\\\\", ":");
                            departmentsNameCache.put(transaction.getDepartmentID(), department);
                        }

                        amount = transaction.getAmount().setScale(decimalCount, RoundingMode.HALF_EVEN).toString();
                        exrate = String.valueOf(transaction.getExchangeRate().doubleValue());
                        note = transaction.getComment();

                        if (transaction.getDestAccountID() >= 0) {
                            type = "Transfer Out";
                            out.writeNext(date, time, account, amount, currency, type, exrate, category, payee, location, project, department, note, lon, lat);
                            type = "Transfer In";
                            if (accountsNameCache.containsKey(transaction.getDestAccountID())) {
                                account = accountsNameCache.get(transaction.getDestAccountID());
                                currency = cabbagesNameCache.get(transaction.getDestAccountID());
                                decimalCount = cabbagesDecimalCountCache.get(transaction.getDestAccountID());
                            } else {
                                accountObj = TransactionManager.getDestAccount(transaction, mContext);
                                account = accountObj.getName();
                                cabbageObj = AccountManager.getCabbage(accountObj, mContext);
                                currency = cabbageObj.getCode();
                                decimalCount = cabbageObj.getDecimalCount();
                                accountsNameCache.put(transaction.getDestAccountID(), account);
                                cabbagesNameCache.put(transaction.getDestAccountID(), currency);
                                cabbagesDecimalCountCache.put(transaction.getDestAccountID(), decimalCount);
                            }
                            amount = transaction.getDestAmount().setScale(decimalCount, RoundingMode.HALF_EVEN).toString();
                            out.writeNext(date, time, account, amount, currency, type, exrate, category, payee, location, project, department, note, lon, lat);
                        } else {
                            type = transaction.getAmount().compareTo(BigDecimal.ZERO) > 0 ? "Income" : "Expense";
                            out.writeNext(date, time, account, amount, currency, type, exrate, category, payee, location, project, department, note, lon, lat);
                        }
                        mCurrentRow++;
                        if (mCsvImportProgressChangeListener != null) {
                            Double pr = ((double) mCurrentRow / (double) mCount) * 100d;
                            mCsvImportProgressChangeListener.onProgressChange(pr.intValue());
                        }
                    }
                } catch (Exception e) {
                    if (mCsvImportProgressChangeListener != null) {
                        mCsvImportProgressChangeListener.onOperationComplete(IProgressEventsListener.CODE_ERROR);
                    }
//                    return;
                }
            }
        });

        mCsvImportProgressChangeListener.onOperationComplete(IProgressEventsListener.CODE_OK);
    }

    public void loadFinancistoCSV() throws IOException {
        final CSV csv = CSV.separator(mSeparator).quote(mQuote).skipLines(1).charset(mCharset).create();       // new instance is immutable
//        (0)date	(1)time	(2)account	(3)amount	(4)currency	(5)original amount	(6)original currency	(7)category	(8)parent	(9)payee	(10)location	(11)project	(12)note
        final Transaction transaction = new Transaction(PrefUtils.getDefDepID(mContext));
        final boolean skipDuplicates = PreferenceManager.getDefaultSharedPreferences(mContext).getBoolean("financisto_csv_skip_diplicates", false);
        final TransactionsDAO transactionsDAO = TransactionsDAO.getInstance(mContext);
        final String transfer_marker = "Transfer Out";
        final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.getInstance(mContext);
        final List<IAbstractModel> transactionList = new ArrayList<>();
        final CsvImportCache cabbagesCache = new CsvImportCache(IAbstractModel.MODEL_TYPE_CABBAGE, mContext);
        final CsvImportCache accountsCache = new CsvImportCache(IAbstractModel.MODEL_TYPE_ACCOUNT, mContext);
        final CsvImportCache categoriesCache = new CsvImportCache(IAbstractModel.MODEL_TYPE_CATEGORY, mContext);
        final CsvImportCache payeesCache = new CsvImportCache(IAbstractModel.MODEL_TYPE_PAYEE, mContext);
        final CsvImportCache projectsCache = new CsvImportCache(IAbstractModel.MODEL_TYPE_PROJECT, mContext);
        final CsvImportCache locationsCache = new CsvImportCache(IAbstractModel.MODEL_TYPE_LOCATION, mContext);


        mCount = 0;
//        Debug.startMethodTracing("loadFinancistoCSV");

        try {
            final SQLiteDatabase database = DBHelper.getInstance(mContext).getDatabase();
            database.beginTransaction();
            csv.read(mFileName, new CSVReadProc() {
                @Override
                public void procRow(int i, String... values) {
                    if (Arrays.asList(values).size() == 13) {
                        mCount++;
                    }
                }
            });

            mCurrentRow = 0;

            csv.read(mFileName, new CSVReadProc() {
                @Override
                public void procRow(int rowIndex, String... values) {
                    if (values.length == 13) {
                        //дата время
                        transaction.setDateTime(dateTimeFormatter.parseDateTimeSqlString(values[0] + " " + values[1]));

                        //<editor-fold desc="Account">
                        //Если счет с таким именем уже есть в БД, берем его, если нет - создаем
                        Account account;
                        try {
                            account = accountsCache.getAccountByName(values[2]);
                        } catch (Exception e) {
                            return;
                        }
                        if (account.getID() < 0) {
                            account.setName(values[2]);
                            //<editor-fold desc="Cabbage">
                            Cabbage cabbage;
                            try {
                                cabbage = cabbagesCache.getCabbageByCode(values[4]);
                            } catch (Exception e) {
                                return;
                            }
                            if (cabbage.getID() < 0) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                                    cabbage = CabbageManager.createFromCode(values[4], mContext);
                                    if (cabbage != null) {
                                        try {
                                            cabbage = (Cabbage) CabbagesDAO.getInstance(mContext).createModel(cabbage);
                                            cabbagesCache.add(cabbage.getCode().hashCode(), cabbage);
                                        } catch (Exception e) {
                                            return;
                                        }
                                    }
                                }
                            }
                            //</editor-fold>
                            if (cabbage != null) {
                                account.setCabbageId(cabbage.getID());
                            }
                            try {
                                account = (Account) AccountsDAO.getInstance(mContext).createModel(account);
                                accountsCache.add(account.getName().hashCode(), account);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        //</editor-fold>

                        //<editor-fold desc="Category">
                        //Родительская категория
                        Category parentCategory = new Category();
                        String fullName = "";
                        long parentID = -1;
                        if (values[8].length() > 0) {
                            String parents[] = values[8].split(":");
                            for (int i = 0; i < parents.length; i++) {
                                if (i == 0) {
                                    fullName = parents[0];
                                } else {
                                    fullName = String.format("%s\\%s", fullName, parents[i]);
                                }
                                try {
                                    parentCategory = (Category) categoriesCache.getNestedModelByName(fullName);
                                } catch (Exception e) {
                                    parentCategory = new Category();
                                }
                                if (parentCategory.getID() < 0) {
                                    parentCategory.setName(parents[i]);
                                    parentCategory.setParentID(parentID);
                                    try {
                                        parentCategory = CategoriesDAO.getInstance(mContext).createCategory(parentCategory, mContext);
                                    } catch (Exception e) {
                                        return;
                                    }
                                }
                                parentID = parentCategory.getID();
                            }
                        }

                        //Категория
                        Category category = new Category();
                        if (values[7].length() > 0) {
                            if (!fullName.isEmpty()) {
                                fullName = String.format("%s\\%s", fullName, values[7]);
                            } else {
                                fullName = values[7];
                            }
                            try {
                                category = (Category) categoriesCache.getNestedModelByName(fullName);
                            } catch (Exception e) {
                                category = new Category();
                            }
                            if (category.getID() < 0) {
                                category.setName(values[7]);
                                category.setParentID(parentCategory.getID());
                                try {
                                    category = CategoriesDAO.getInstance(mContext).createCategory(category, mContext);
                                } catch (Exception e) {
                                    return;
                                }
                            }
                        }
                        transaction.setCategoryID(category.getID());
                        //</editor-fold>

                        //<editor-fold desc="Payee">
                        Payee payee = new Payee();
                        if (values[9].length() > 0) {
                            try {
                                payee = (Payee) payeesCache.getNestedModelByName(values[9]);
                            } catch (Exception e) {
                                payee = new Payee();
                            }
                            if (payee.getID() < 0) {
                                payee.setName(values[9]);
                                try {
                                    payee = (Payee) PayeesDAO.getInstance(mContext).createModel(payee);
                                    payeesCache.add(payee.getName().hashCode(), payee);
                                } catch (Exception e) {
                                    return;
                                }
                            }
                        }
                        transaction.setPayeeID(payee.getID());
                        //</editor-fold>

                        //<editor-fold desc="Project">
                        Project project = new Project();
                        if (values[11].length() > 0) {
                            try {
                                project = (Project) projectsCache.getNestedModelByName(values[11]);
                            } catch (Exception e) {
                                project = new Project();
                            }
                            if (project.getID() < 0) {
                                project.setName(values[11]);
                                try {
                                    project = ProjectsDAO.getInstance(mContext).createProject(project, mContext);
                                    projectsCache.add(project.getName().hashCode(), project);
                                } catch (Exception e) {
                                    return;
                                }
                            }
                        }
                        transaction.setProjectID(project.getID());
                        //</editor-fold>

                        //<editor-fold desc="Location" defaultstate="collapsed">
                        //Местоположение
                        Location location = new Location();
                        if (values[10].length() > 0) {
                            try {
                                location = (Location) locationsCache.getNestedModelByName(values[10]);
                            } catch (Exception e) {
                                location = new Location();
                            }
                            if (location.getID() < 0) {
                                location.setName(values[10]);
                                try {
                                    location = (Location) LocationsDAO.getInstance(mContext).createModel(location);
                                    locationsCache.add(location.getName().hashCode(), location);
                                } catch (Exception e) {
                                    return;
                                }
                            }
                        }
                        transaction.setLocationID(location.getID());
                        //</editor-fold>

                        transaction.setComment(values[12]);

                        if (values[10].toLowerCase().equals(transfer_marker.toLowerCase())) {
                            transaction.setID(-1);
                            transaction.setTransactionOpened(true);
                            transaction.setAccountID(account.getID());
                            transaction.setDestAccountID(new Account().getID());
                            //Сумма транзакции, если исходящий перевод
                            transaction.setExchangeRate(BigDecimal.ONE);
                            transaction.setAmount(BigDecimal.ZERO, Transaction.TRANSACTION_TYPE_EXPENSE);
                            transaction.setAmount(BigDecimal.valueOf(Double.valueOf(values[3])), Transaction.TRANSACTION_TYPE_TRANSFER);
                        } else {
                            if (transaction.isTransactionOpened()) {
                                transaction.setDestAccountID(account.getID());
                                transaction.setDestAmount(BigDecimal.valueOf(Double.valueOf(values[3])));
                            } else {
                                transaction.setID(-1);
                                transaction.setDestAccountID(-1);
                                transaction.setAccountID(account.getID());
                                //Сумма транзакции, если не перевод
                                transaction.setExchangeRate(BigDecimal.ONE);
                                transaction.setAmount(BigDecimal.ZERO, Transaction.TRANSACTION_TYPE_EXPENSE);
                                transaction.setAmount(BigDecimal.valueOf(Double.valueOf(values[3])), Transaction.TRANSACTION_TYPE_UNDEFINED);
                            }
                            transaction.setTransactionOpened(false);
                            if (skipDuplicates) {
                                if (transactionsDAO.hasDuplicates(transaction).getID() < 0) {
                                    try {
                                        transactionsDAO.createModel(transaction);
                                    } catch (Exception e) {
                                        return;
                                    }
                                }
                            } else {
                                transactionList.add(new Transaction(transaction));
                            }
                        }


                        mCurrentRow++;
                        if (mCsvImportProgressChangeListener != null) {
                            Double pr = ((double) mCurrentRow / (double) mCount) * 100d;
                            mCsvImportProgressChangeListener.onProgressChange(pr.intValue());
                        }
                    }
                }
            });
            database.setTransactionSuccessful();
            database.endTransaction();

//            database.beginTransaction();
            if (!transactionList.isEmpty()) {
                transactionsDAO.bulkCreateModel(transactionList, null, false);

            }

            DBHelper.getInstance(mContext).rebuildDB();
//            database.setTransactionSuccessful();
//            database.endTransaction();
        } catch (Exception e) {
            mCsvImportProgressChangeListener.onOperationComplete(IProgressEventsListener.CODE_ERROR);
            return;
        }

        mCsvImportProgressChangeListener.onOperationComplete(IProgressEventsListener.CODE_OK);
    }

    public void loadCustomCSV(ImportParams params) throws IOException {
        final CSV csv = CSV.separator(mSeparator).quote(mQuote).skipLines(mSkipLines).charset(mCharset).create();
        final Transaction transaction = new Transaction(PrefUtils.getDefDepID(mContext));
        final boolean skipDuplicates = PreferenceManager.getDefaultSharedPreferences(mContext).getBoolean("custom_csv_skip_diplicates", false);
        List<IAbstractModel> transactionList = new ArrayList<>();
        mCount = 0;
        CsvCachesSet caches = new CsvCachesSet(mContext);

        try {
            //<editor-fold desc="Row counting" defaultstate="collapsing">
            csv.read(mFileName, new CSVReadProc() {
                @Override
                public void procRow(int i, String... values) {
                    if (Arrays.asList(values).size() > 0) {
                        mCount++;
                    }
                }
            });
            //</editor-fold>

            mCurrentRow = 0;

            CustomCsvImportProc customCsvImportProc = new CustomCsvImportProc(params, transaction, skipDuplicates, transactionList, caches);

            final SQLiteDatabase database = DBHelper.getInstance(mContext).getDatabase();
            database.beginTransaction();
            csv.read(mFileName, customCsvImportProc);
            database.setTransactionSuccessful();
            database.endTransaction();
            if (!transactionList.isEmpty()) {
                TransactionsDAO.getInstance(mContext).bulkCreateModel(transactionList, null, false);
            }
            DBHelper.getInstance(mContext).rebuildDB();
        } catch (Exception e) {
            mCsvImportProgressChangeListener.onOperationComplete(IProgressEventsListener.CODE_ERROR);
            return;
        }


        mCsvImportProgressChangeListener.onOperationComplete(IProgressEventsListener.CODE_OK);
    }

    public void loadFingenCSV() throws IOException {
        final CSV csv = CSV.separator(mSeparator).quote(mQuote).skipLines(0).charset(mCharset).create();

        final Transaction transaction = new Transaction(PrefUtils.getDefDepID(mContext));
        final boolean skipDuplicates = PreferenceManager.getDefaultSharedPreferences(mContext).getBoolean("custom_csv_skip_diplicates", false);
        List<IAbstractModel> transactionList = new ArrayList<>();
        CsvCachesSet caches = new CsvCachesSet(mContext);

        mCount = 0;

        try {

            csv.read(mFileName, new CSVReadProc() {
                @Override
                public void procRow(int i, String... values) {
                    if (Arrays.asList(values).size() > 0) {
                        mCount++;
                    }
                }
            });

            mCurrentRow = 0;
//            date time account amount currency type exrate category payee location project department note
            ImportParams params = new ImportParams(0, 1, 2, 3, 4, 7, 8, 9, 10, 11, 12, 5, true);
            params.setDateFormat("yyyy-MM-dd HH:mm:ss");
            FingenCsvImportProc fingenCsvImportProc = new FingenCsvImportProc(params, transaction, skipDuplicates, transactionList, caches);

            final SQLiteDatabase database = DBHelper.getInstance(mContext).getDatabase();
            database.beginTransaction();
            csv.read(mFileName, fingenCsvImportProc);
            database.setTransactionSuccessful();
            database.endTransaction();
            if (!transactionList.isEmpty()) {
                TransactionsDAO.getInstance(mContext).bulkCreateModel(transactionList, null, false);
            }

            DBHelper.getInstance(mContext).rebuildDB();
        } catch (Exception e) {
            mCsvImportProgressChangeListener.onOperationComplete(IProgressEventsListener.CODE_ERROR);
            return;
        }


        mCsvImportProgressChangeListener.onOperationComplete(IProgressEventsListener.CODE_OK);
    }

    private IAbstractModel parseNestedModel(CsvImportCache cache, String vls[], int pos, int modelType, AbstractDAO abstractDAO, Context context) throws Exception {
        IAbstractModel model = BaseModel.createModelByType(modelType);

        if (pos < 0 || vls[pos].isEmpty()) {
            return model;
        }

        if (pos < vls.length) {
            String models[] = vls[pos].split(":");
            String fullName = "";
            long parentID = -1;
            if (models.length >= 1) {
                for (int i = 0; i < models.length; i++) {
                    if (i == 0) {
                        fullName = models[0];
                    } else {
                        fullName = fullName + "\\" + models[i];
                    }
                    model = cache.getNestedModelByName(fullName);
                    if (model.getID() < 0) {
                        model.setName(models[i]);
                        model.setParentID(parentID);
                        try {
                            if (model.getClass().equals(Category.class)) {
                                model = ((CategoriesDAO) abstractDAO).createCategory((Category) model, context);
                            } else if (model.getClass().equals(Project.class)) {
                                model = ((ProjectsDAO) abstractDAO).createProject((Project) model, context);
                            } else {
                                model = abstractDAO.createModel(model);
                            }
                            cache.add(fullName.hashCode(), model);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    parentID = model.getID();
                }
            }
        }
        return model;
    }

    private Date parseDateTime(String vls[], int posDate, int posTime, DateFormat dateFormat) throws ParseException {
        if (dateFormat != null) {
            if (posDate >= 0 & posDate < vls.length & posTime >= 0 & posTime < vls.length) {
                return dateFormat.parse(vls[posDate] + " " + vls[posTime]);
            }
            if (posDate >= 0 & posDate < vls.length) {
                return dateFormat.parse(vls[posDate]);
            }
        }
        return new Date();
    }

    private Cabbage parseCabbage(CsvImportCache cache, String vls[], int pos, Context context) throws Exception {
        Cabbage cabbage;
        if (pos >= 0 & pos < vls.length) {
            String code = vls[pos];
//            cabbage = CabbagesDAO.getInstance(context).getCabbageByCode(code);
            cabbage = cache.getCabbageByCode(code);
            if (cabbage.getID() < 0) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    List<Currency> currencies = new ArrayList<>();
                    currencies.addAll(Currency.getAvailableCurrencies());
                    for (Currency currency : currencies) {
                        if (currency.getCurrencyCode().equals(code)) {
                            cabbage.setCode(code);
                            cabbage.setDecimalCount(2);
                            cabbage.setName(currency.getDisplayName());
                            cabbage.setSimbol(currency.getSymbol());
                            try {
                                cabbage = (Cabbage) CabbagesDAO.getInstance(context).createModel(cabbage);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        } else {
            cabbage = new Cabbage();
        }
        return cabbage;
    }

    private Account parseAccount(CsvImportCache cache, String vls[], int pos, long cabbageID, Context context) throws Exception {
        Account account;
        if (pos >= 0 & pos < vls.length) {
            String s = vls[pos];
            if (s.charAt(0) == '[' && s.charAt(s.length() - 1) == ']') {
                s = s.substring(1, s.length() - 1);
            }
            account = cache.getAccountByName(s);
//                account = AccountsDAO.getInstance(context).getAccountByName(s);
            if (account.getID() < 0) {
                account.setName(s);
                account.setCabbageId(cabbageID);
                if (!account.getName().isEmpty()) {
                    try {
                        account = (Account) AccountsDAO.getInstance(context).createModel(account);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        } else {
            account = new Account();
        }
        return account;
    }

    private String parseComment(String vls[], int pos) {
        String s = "";
        if (pos >= 0 & pos < vls.length) {
            s = vls[pos];
        }
        return s;
    }

    private BigDecimal parseAmount(String vls[], int pos) {
        BigDecimal amount;
        String s = vls[pos];
        s = s.replaceAll("\\s+", "");
        s = s.replaceAll(",", ".");
        if (pos >= 0 & pos < vls.length) {
            try {
                amount = new BigDecimal(s);
            } catch (Exception e) {
                amount = BigDecimal.ZERO;
            }
        } else {
            amount = BigDecimal.ZERO;
        }
        return amount;
    }

    private class CustomCsvImportProc implements CSVReadProc {
        private final ImportParams mParams;
        private final Transaction mTransaction;
        private final TransactionsDAO transactionsDAO;
        boolean mSkipDuplicates;
        List<IAbstractModel> mTransactionList;
        CsvCachesSet mCaches;

        CustomCsvImportProc(ImportParams params, Transaction transaction, boolean skipDuplicates, List<IAbstractModel> transactionList, CsvCachesSet caches) {
            mParams = params;
            mTransaction = transaction;
            mSkipDuplicates = skipDuplicates;
            mTransactionList = transactionList;
            transactionsDAO = TransactionsDAO.getInstance(mContext);
            mCaches = caches;
        }

        @Override
        public void procRow(int i, String... vls) {
            Log.d(TAG, "Start import row " + String.valueOf(i));
            if (mParams.hasHeader & i == 0) return;

            if (vls.length == 1 && vls[0].isEmpty()) return;

            mTransaction.setID(-1);
            mTransaction.setDestAccountID(-1);

            //Date/Time
            try {
                mTransaction.setDateTime(parseDateTime(vls, mParams.date, mParams.time, mParams.getDateFormat()));
            } catch (ParseException e) {
                mTransaction.setDateTime(new Date());
            }
            //Account
            Cabbage cabbage = null;
            try {
                cabbage = parseCabbage(mCaches.getCabbagesCache(), vls, mParams.currency, mContext);
            } catch (Exception e) {
                return;
            }
            try {
                mTransaction.setAccountID(parseAccount(mCaches.getAccountsCache(), vls, mParams.account, cabbage.getID(), mContext).getID());
            } catch (Exception e) {
                return;
            }

            //Payee
            try {
                mTransaction.setPayeeID(parseNestedModel(mCaches.getPayeesCache(), vls, mParams.payee, IAbstractModel.MODEL_TYPE_PAYEE, PayeesDAO.getInstance(mContext), mContext).getID());
            } catch (Exception e) {
                mTransaction.setPayeeID(-1);
            }
            String categoryStr = vls[mParams.category];
            mTransaction.setCategoryID(-1);
            if (!categoryStr.isEmpty()) {
                if (categoryStr.charAt(0) == '[' && categoryStr.charAt(categoryStr.length() - 1) == ']') {
                    try {
                        mTransaction.setDestAccountID(parseAccount(mCaches.getAccountsCache(), vls, mParams.category, cabbage.getID(), mContext).getID());
                    } catch (Exception e) {
                        return;
                    }
                } else {
                    //Category
                    try {
                        mTransaction.setCategoryID(parseNestedModel(mCaches.getCategoriesCache(), vls, mParams.category, IAbstractModel.MODEL_TYPE_CATEGORY, CategoriesDAO.getInstance(mContext), mContext).getID());
                    } catch (Exception e) {
                        mTransaction.setCategoryID(-1);
                    }
                }
            }
            //Project
            try {
                mTransaction.setProjectID(parseNestedModel(mCaches.getProjectsCache(), vls, mParams.project, IAbstractModel.MODEL_TYPE_PROJECT, ProjectsDAO.getInstance(mContext), mContext).getID());
            } catch (Exception e) {
                mTransaction.setProjectID(-1);
            }
            //Department
            try {
                mTransaction.setDepartmentID(parseNestedModel(mCaches.getDepartmentsCache(), vls, mParams.department, IAbstractModel.MODEL_TYPE_DEPARTMENT, DepartmentsDAO.getInstance(mContext), mContext).getID());
            } catch (Exception e) {
                mTransaction.setDepartmentID(-1);
            }
            //Location
            try {
                mTransaction.setLocationID(parseNestedModel(mCaches.getLocationsCache(), vls, mParams.location, IAbstractModel.MODEL_TYPE_LOCATION, LocationsDAO.getInstance(mContext), mContext).getID());
            } catch (Exception e) {
                mTransaction.setLocationID(-1);
            }
            //Comment
            mTransaction.setComment(parseComment(vls, mParams.comment));
            //Amount
            mTransaction.setExchangeRate(BigDecimal.ONE);
            mTransaction.setAmount(BigDecimal.ZERO, Transaction.TRANSACTION_TYPE_EXPENSE);
            mTransaction.setAmount(parseAmount(vls, mParams.amount), Transaction.TRANSACTION_TYPE_UNDEFINED);

            if (mTransaction.getAccountID() >= 0 && mTransaction.getAmount().compareTo(BigDecimal.ZERO) != 0) {
                if (mSkipDuplicates) {
                    if (transactionsDAO.hasDuplicates(mTransaction).getID() < 0) {
                        try {
                            transactionsDAO.createModel(mTransaction);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    mTransactionList.add(new Transaction(mTransaction));
                }
            }

            mCurrentRow++;
            if (mCsvImportProgressChangeListener != null) {
                Double pr = ((double) mCurrentRow / (double) mCount) * 100d;
                mCsvImportProgressChangeListener.onProgressChange(pr.intValue());
            }
        }
    }

    private class FingenCsvImportProc implements CSVReadProc {
        private final ImportParams mParams;
        private final Transaction mTransaction;
        private final TransactionsDAO transactionsDAO;
        boolean mSkipDuplicates;
        List<IAbstractModel> mTransactionList;
        CsvCachesSet mCaches;

        FingenCsvImportProc(ImportParams params, Transaction transaction, boolean skipDuplicates, List<IAbstractModel> transactionList, CsvCachesSet caches) {
            mParams = params;
            mTransaction = transaction;
            mSkipDuplicates = skipDuplicates;
            mTransactionList = transactionList;
            transactionsDAO = TransactionsDAO.getInstance(mContext);
            mCaches = caches;
        }

        @Override
        public void procRow(int i, String... vls) {
            Log.d(TAG, "Start import row " + String.valueOf(i));
//            if (vls[mParams.amount].equals("33128.25")) {
//                Log.d(TAG, "1486");
//            }
            if (mParams.hasHeader & i == 0) return;

//            List<String> vls = Arrays.asList(values);

            if (vls.length == 1 && vls[0].isEmpty()) return;

            if (!mTransaction.isTransactionOpened()) {
                //Date/Time
                try {
                    mTransaction.setDateTime(parseDateTime(vls, mParams.date, mParams.time, mParams.getDateFormat()));
                } catch (ParseException e) {
                    mTransaction.setDateTime(new Date());
                }
                mTransaction.setDestAccountID(-1);
                //Payee
                try {
                    mTransaction.setPayeeID(parseNestedModel(mCaches.getPayeesCache(), vls, mParams.payee, IAbstractModel.MODEL_TYPE_PAYEE, PayeesDAO.getInstance(mContext), mContext).getID());
                } catch (Exception e) {
                    mTransaction.setPayeeID(-1);
                }
                //Category
                try {
                    mTransaction.setCategoryID(parseNestedModel(mCaches.getCategoriesCache(), vls, mParams.category, IAbstractModel.MODEL_TYPE_CATEGORY, CategoriesDAO.getInstance(mContext), mContext).getID());
                } catch (Exception e) {
                    mTransaction.setCategoryID(-1);
                }
                //Project
                try {
                    mTransaction.setProjectID(parseNestedModel(mCaches.getProjectsCache(), vls, mParams.project, IAbstractModel.MODEL_TYPE_PROJECT, ProjectsDAO.getInstance(mContext), mContext).getID());
                } catch (Exception e) {
                    mTransaction.setProjectID(-1);
                }
                //Department
                try {
                    mTransaction.setDepartmentID(parseNestedModel(mCaches.getDepartmentsCache(), vls, mParams.department, IAbstractModel.MODEL_TYPE_DEPARTMENT, DepartmentsDAO.getInstance(mContext), mContext).getID());
                } catch (Exception e) {
                    mTransaction.setDepartmentID(-1);
                }
                //Location
                try {
                    mTransaction.setLocationID(parseNestedModel(mCaches.getLocationsCache(), vls, mParams.location, IAbstractModel.MODEL_TYPE_LOCATION, LocationsDAO.getInstance(mContext), mContext).getID());
                } catch (Exception e) {
                    mTransaction.setLocationID(-1);
                }
                //Comment
                mTransaction.setComment(parseComment(vls, mParams.comment));
                //Amount
                mTransaction.setExchangeRate(BigDecimal.ONE);
                mTransaction.setAmount(BigDecimal.ZERO, Transaction.TRANSACTION_TYPE_EXPENSE);
                mTransaction.setAmount(parseAmount(vls, mParams.amount), Transaction.TRANSACTION_TYPE_UNDEFINED);
                mTransaction.setID(-1);
                Cabbage cabbage;
                try {
                    cabbage = parseCabbage(mCaches.getCabbagesCache(), vls, mParams.currency, mContext);
                } catch (Exception e) {
                    return;
                }
                if (cabbage.getID() < 0) {
                    return;
                }
                try {
                    mTransaction.setAccountID(parseAccount(mCaches.getAccountsCache(), vls, mParams.account, cabbage.getID(), mContext).getID());
                } catch (Exception e) {
                    return;
                }
            }

            if (vls[mParams.type].toLowerCase().equals("transfer out")) {
                mTransaction.setTransactionOpened(true);
            } else {
                if (mTransaction.isTransactionOpened()) {
                    Cabbage cabbage;
                    try {
                        cabbage = parseCabbage(mCaches.getCabbagesCache(), vls, mParams.currency, mContext);
                    } catch (Exception e) {
                        return;
                    }
                    try {
                        mTransaction.setDestAccountID(parseAccount(mCaches.getAccountsCache(), vls, mParams.account, cabbage.getID(), mContext).getID());
                    } catch (Exception e) {
                        return;
                    }
                    //Сумма транзакции, если входящий перевод
                    mTransaction.setDestAmount(parseAmount(vls, 3));
                }
                mTransaction.setTransactionOpened(false);
                if (mTransaction.getAccountID() >= 0 && mTransaction.getAmount().compareTo(BigDecimal.ZERO) != 0) {
                    if (mSkipDuplicates) {
                        if (transactionsDAO.hasDuplicates(mTransaction).getID() < 0) {
                            try {
                                transactionsDAO.createModel(mTransaction);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    } else {
                        mTransactionList.add(new Transaction(mTransaction));
                    }
                }
            }

            mCurrentRow++;
            if (mCsvImportProgressChangeListener != null) {
                Double pr = ((double) mCurrentRow / (double) mCount) * 100d;
                mCsvImportProgressChangeListener.onProgressChange(pr.intValue());
            }
        }
    }

    //<editor-fold desc="Service utils" defaultstate="collapsed">
    public List<String> loadColumnsFromCSV() {
        final CSV csv = CSV
                .separator(mSeparator)  // delimiter of fields
                .quote(mQuote)      // quote character
                .skipLines(mSkipLines)
                .charset(mCharset)
                .create();       // new instance is immutable

        mHeaders = new String[]{};

        csv.read(mFileName, new CSVReadProc() {
            @Override
            public void procRow(int i, String... values) {
                if (i == 0) {
                    mHeaders = values;
                }
            }
        });

        if (mHeaders.length == 0) {
            return new ArrayList<>();
        } else {
            return Arrays.asList(mHeaders);
        }
    }

    private String detectCharset() throws IOException {
        String result = "UTF-8";
        UniversalDetector detector = new UniversalDetector(null);
        byte[] buf = new byte[4096];

        try {
            FileInputStream fis = new FileInputStream(mFileName);
            int nread;
            while ((nread = fis.read(buf)) > 0 && !detector.isDone()) {
                detector.handleData(buf, 0, nread);
            }
            fis.close();
            detector.dataEnd();
            String encoding = detector.getDetectedCharset();
            if (encoding != null) {
                result = encoding;
            }

            detector.reset();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    private char detectSeparators() {
        char result = ';';
        for (char c : sSeparators) {
            if (checkSeparator(c)) {
                return c;
            }
        }
        return result;
    }

    private boolean checkSeparator(char separator) {
        final CSV csv = CSV
                .separator(separator)  // delimiter of fields
                .quote(mQuote)      // quote character
                .charset(mCharset)
                .skipLines(mSkipLines)
                .create();

        mListColumnsCount = new ArrayList<>();

        csv.read(mFileName, new CSVReadProc() {
            @Override
            public void procRow(int i, String... values) {
                if (values.length > 1) {
                    mListColumnsCount.add(values.length);
                }
            }
        });

        boolean isValidSeparator = true;
        if (mListColumnsCount.size() > 0) {
            for (int i = 1; i < mListColumnsCount.size(); i++) {
                isValidSeparator = isValidSeparator & (mListColumnsCount.get(i).equals(mListColumnsCount.get(i - 1)));
            }
            return isValidSeparator;
        } else {
            return false;
        }
    }

    public String detectDateFormat(int dateColumnInd) {
        String result = "";
        if (dateColumnInd >= 0) {
            for (String s : sDateFormats) {
                try {
                    if (checkDateFormat(s, dateColumnInd)) {
                        return s;
                    }
                } catch (Exception e) {
//                    e.printStackTrace();
                }
            }
        }
        return result;
    }

    private boolean checkDateFormat(final String dfs, final int dateColumnInd) {
        final CSV csv = CSV
                .separator(mSeparator)  // delimiter of fields
                .quote(mQuote)      // quote character
                .charset(mCharset)
                .skipLines(mSkipLines)
                .create();

        mIsValidDateFormat = false;

        csv.read(mFileName, new CSVReadProc() {
            @Override
            public void procRow(int i, String... values) {
                if (!mIsValidDateFormat) {
                    if (values.length >= dateColumnInd) {
                        @SuppressLint("SimpleDateFormat") DateFormat df = new SimpleDateFormat(dfs);
                        try {
                            df.parse(values[dateColumnInd]);
                            mIsValidDateFormat = true;
                        } catch (ParseException e) {
//                        e.printStackTrace();
                        }
                    }
                }
            }
        });

        return mIsValidDateFormat;
    }
    //</editor-fold>
}
