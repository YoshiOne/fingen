package com.yoshione.fingen.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Pair;

import com.yoshione.fingen.DBHelper;
import com.yoshione.fingen.classes.TableInfo;
import com.yoshione.fingen.dao.SendersDAO;
import com.yoshione.fingen.utils.ColorUtils;
import com.yoshione.fingen.utils.DateTimeFormatter;
import com.yoshione.fingen.utils.Translit;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.requery.android.database.sqlite.SQLiteDatabase;

/**
 * Created by slv on 28.03.2017.
 * UpdateHelper
 */

public class UpdateHelper {

    //<editor-fold desc="tableInfo17" defaultstate="collapsed">
    private static TableInfo tableInfo17[] = new TableInfo[]{
            new TableInfo("ref_Currencies", "", "_id, Code, Symbol, Name, DecimalCount"),
            new TableInfo("ref_Accounts", "", "_id, Type, Name, Currency, Emitent, Last4Digits, Comment, StartBalance, IsClosed, CustomOrder, CreditLimit"),
            new TableInfo("ref_Categories", "", "_id, Name, COLOR, ParentCategory, OrderNumber, Sign"),
            new TableInfo("ref_Payees", "", "_id, Name, DefCategory"),
            new TableInfo("ref_Projects", "", "_id, Name, isActive"),
            new TableInfo("ref_Locations", "", "_id, Name, Lon, Lat, Radius, Address"),
            new TableInfo("ref_Departments", "", "_id, Name, isActive"),
            new TableInfo("ref_SimpleDebts", "", "_id, Name, isActive"),
            new TableInfo("log_Transactions", "", "_id, DateTime, SrcAccount, Payee, Category, Amount, Project, Location, Comment, File, DestAccount, ExchangeRate, AutoCreated, Lat, Lon, Accuracy, Department, SimpleDebt"),
            new TableInfo("log_Templates", "", "_id, SrcAccount, Payee, Category, Amount, Project, Location, Name, DestAccount, ExchangeRate, Type, Department"),
            new TableInfo("log_Incoming_SMS", "", "_id, DateTime, Sender, Body"),
            new TableInfo("ref_Sms_Parser_Patterns", "", "_id, Type, Object, Pattern"),
            new TableInfo("ref_Debts", "", "_id, Account, Payee, Category, Closed, SrcAccount, Comment"),
            new TableInfo("ref_budget", "", "_id, Year, Month, Category, Amount, Currency"),
            new TableInfo("ref_budget_debts", "", "_id, Year, Month, Debt, Amount"),
            new TableInfo("ref_Senders", "", "_id, Name, PhoneNo, AmountPos, BalancePos, LeadingCurrencySymbol, DateFormat, isActive")
    };
    //</editor-fold>

    //<editor-fold desc="tableInfo18" defaultstate="collapsed">
    private static TableInfo tableInfo18[] = new TableInfo[]{
            new TableInfo("ref_Currencies",
                    "CREATE TABLE ref_Currencies (_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, FBID TEXT, TS INTEGER, Deleted INTEGER, Dirty INTEGER, LastEdited TEXT, Code TEXT NOT NULL, Symbol TEXT NOT NULL, Name TEXT NOT NULL, DecimalCount INTEGER NOT NULL, UNIQUE (Code, Deleted) ON CONFLICT ABORT);",
                    "_id, FBID, TS, Deleted, Dirty, LastEdited, Code, Symbol, Name, DecimalCount"),
            new TableInfo("ref_Accounts",
                    "CREATE TABLE ref_Accounts (_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, FBID TEXT, TS INTEGER, Deleted INTEGER, Dirty INTEGER, LastEdited TEXT, Type INTEGER NOT NULL, Name TEXT NOT NULL, Currency INTEGER REFERENCES ref_Currencies(_id) ON DELETE SET NULL ON UPDATE CASCADE, Emitent TEXT, Last4Digits INTEGER, Comment TEXT, StartBalance REAL NOT NULL, IsClosed INTEGER NOT NULL, CustomOrder INTEGER, CreditLimit REAL, UNIQUE (Name, Deleted) ON CONFLICT ABORT);",
                    "_id, FBID, TS, Deleted, Dirty, LastEdited, Type, Name, Currency, Emitent, Last4Digits, Comment, StartBalance, IsClosed, CustomOrder, CreditLimit"),
            new TableInfo("ref_Categories",
                    "CREATE TABLE ref_Categories (_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, FBID TEXT, TS INTEGER, Deleted INTEGER, Dirty INTEGER, LastEdited TEXT, Name TEXT NOT NULL, COLOR TEXT, ParentCategory INTEGER REFERENCES ref_Categories(_id) ON DELETE SET NULL ON UPDATE CASCADE, OrderNumber INTEGER NOT NULL, Sign INTEGER NOT NULL, UNIQUE (Name, ParentCategory, Deleted) ON CONFLICT ABORT);",
                    "_id, FBID, TS, Deleted, Dirty, LastEdited, Name, COLOR, ParentCategory, OrderNumber, Sign"),
            new TableInfo("ref_Payees",
                    "CREATE TABLE ref_Payees (_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, FBID TEXT, TS INTEGER, Deleted INTEGER, Dirty INTEGER, LastEdited TEXT, Name TEXT NOT NULL, DefCategory INTEGER REFERENCES ref_Categories(_id) ON DELETE SET NULL ON UPDATE CASCADE, UNIQUE (Name, Deleted) ON CONFLICT ABORT);",
                    "_id, FBID, TS, Deleted, Dirty, LastEdited, Name, DefCategory"),
            new TableInfo("ref_Projects",
                    "CREATE TABLE ref_Projects (_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, FBID TEXT, TS INTEGER, Deleted INTEGER, Dirty INTEGER, LastEdited TEXT, Name TEXT NOT NULL, isActive INTEGER NOT NULL, UNIQUE (Name, Deleted) ON CONFLICT ABORT);",
                    "_id, FBID, TS, Deleted, Dirty, LastEdited, Name, isActive"),
            new TableInfo("ref_Locations",
                    "CREATE TABLE ref_Locations (_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, FBID TEXT, TS INTEGER, Deleted INTEGER, Dirty INTEGER, LastEdited TEXT, Name TEXT NOT NULL, Lon REAL NOT NULL, Lat REAL NOT NULL, Radius INTEGER, Address TEXT, UNIQUE (Name, Deleted) ON CONFLICT ABORT);",
                    "_id, FBID, TS, Deleted, Dirty, LastEdited, Name, Lon, Lat, Radius, Address"),
            new TableInfo("ref_Departments",
                    "CREATE TABLE ref_Departments (_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, FBID TEXT, TS INTEGER, Deleted INTEGER, Dirty INTEGER, LastEdited TEXT, Name TEXT NOT NULL, isActive INTEGER NOT NULL, UNIQUE (Name, Deleted) ON CONFLICT ABORT);",
                    "_id, FBID, TS, Deleted, Dirty, LastEdited, Name, isActive"),
            new TableInfo("ref_SimpleDebts",
                    "CREATE TABLE ref_SimpleDebts (_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, FBID TEXT, TS INTEGER, Deleted INTEGER, Dirty INTEGER, LastEdited TEXT, Name TEXT NOT NULL, isActive INTEGER NOT NULL, UNIQUE (Name, Deleted) ON CONFLICT ABORT);",
                    "_id, FBID, TS, Deleted, Dirty, LastEdited, Name, isActive"),
            new TableInfo("log_Transactions",
                    "CREATE TABLE log_Transactions (_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, FBID TEXT, TS INTEGER, Deleted INTEGER, Dirty INTEGER, LastEdited TEXT, DateTime INTEGER NOT NULL, SrcAccount INTEGER NOT NULL REFERENCES ref_Accounts(_id) ON DELETE CASCADE ON UPDATE CASCADE, Payee INTEGER REFERENCES ref_Payees(_id) ON DELETE SET NULL ON UPDATE CASCADE, Category INTEGER REFERENCES ref_Categories(_id) ON DELETE SET NULL ON UPDATE CASCADE, Amount REAL NOT NULL, Project INTEGER REFERENCES ref_Projects(_id) ON DELETE SET NULL ON UPDATE CASCADE, SimpleDebt INTEGER REFERENCES ref_SimpleDebts(_id) ON DELETE SET NULL ON UPDATE CASCADE, Department INTEGER REFERENCES ref_Departments(_id) ON DELETE SET NULL ON UPDATE CASCADE, Location INTEGER REFERENCES ref_Locations(_id) ON DELETE SET NULL ON UPDATE CASCADE, Comment TEXT, File TEXT, DestAccount INTEGER NOT NULL REFERENCES ref_Accounts(_id) ON DELETE CASCADE ON UPDATE CASCADE, ExchangeRate REAL NOT NULL, AutoCreated INTEGER NOT NULL, Lon REAL, Lat REAL, Accuracy INTEGER);",
                    "_id, FBID, TS, Deleted, Dirty, LastEdited, DateTime, SrcAccount, Payee, Category, Amount, Project, SimpleDebt, Department, Location, Comment, File, DestAccount, ExchangeRate, AutoCreated, Lon, Lat, Accuracy"),
            new TableInfo("log_Templates",
                    "CREATE TABLE log_Templates (_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, FBID TEXT, TS INTEGER, Deleted INTEGER, Dirty INTEGER, LastEdited TEXT, SrcAccount INTEGER NOT NULL REFERENCES ref_Accounts(_id) ON DELETE CASCADE ON UPDATE CASCADE, Payee INTEGER REFERENCES ref_Payees(_id) ON DELETE SET NULL ON UPDATE CASCADE, Category INTEGER REFERENCES ref_Categories(_id) ON DELETE SET NULL ON UPDATE CASCADE, Amount REAL NOT NULL, Project INTEGER REFERENCES ref_Projects(_id) ON DELETE SET NULL ON UPDATE CASCADE, Department INTEGER REFERENCES ref_Departments(_id) ON DELETE SET NULL ON UPDATE CASCADE, Location INTEGER REFERENCES ref_Locations(_id) ON DELETE SET NULL ON UPDATE CASCADE, Name TEXT NOT NULL, DestAccount INTEGER NOT NULL REFERENCES ref_Accounts(_id) ON DELETE CASCADE ON UPDATE CASCADE, ExchangeRate REAL NOT NULL, Type INTEGER NOT NULL);",
                    "_id, FBID, TS, Deleted, Dirty, LastEdited, SrcAccount, Payee, Category, Amount, Project, Department, Location, Name, DestAccount, ExchangeRate, Type"),
            new TableInfo("log_Incoming_SMS",
                    "CREATE TABLE log_Incoming_SMS (_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, FBID TEXT, TS INTEGER, Deleted INTEGER, Dirty INTEGER, LastEdited TEXT, DateTime TEXT NOT NULL, Sender INTEGER NOT NULL, Body TEXT NOT NULL);",
                    "_id, FBID, TS, Deleted, Dirty, LastEdited, DateTime, Sender, Body"),
            new TableInfo("ref_Sms_Parser_Patterns",
                    "CREATE TABLE ref_Sms_Parser_Patterns (_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, FBID TEXT, TS INTEGER, Deleted INTEGER, Dirty INTEGER, LastEdited TEXT, Type INTEGER NOT NULL, Object TEXT NOT NULL, Pattern TEXT NOT NULL, UNIQUE (Type, Pattern, Deleted) ON CONFLICT REPLACE);",
                    "_id, FBID, TS, Deleted, Dirty, LastEdited, Type, Object, Pattern"),
            new TableInfo("ref_Debts",
                    "CREATE TABLE ref_Debts (_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, FBID TEXT, TS INTEGER, Deleted INTEGER, Dirty INTEGER, LastEdited TEXT, Account INTEGER NOT NULL ON CONFLICT ABORT REFERENCES ref_Accounts(_id) ON DELETE CASCADE ON UPDATE CASCADE, Payee INTEGER NOT NULL REFERENCES ref_Payees(_id) ON DELETE CASCADE ON UPDATE CASCADE, Category INTEGER NOT NULL REFERENCES ref_Categories(_id) ON DELETE SET NULL ON UPDATE CASCADE, Closed INTEGER NOT NULL, SrcAccount INTEGER, Comment TEXT, UNIQUE (Account, Deleted) ON CONFLICT ABORT);",
                    "_id, FBID, TS, Deleted, Dirty, LastEdited, Account ON CONFLICT ABORT, Payee, Category, Closed, SrcAccount, Comment"),
            new TableInfo("ref_budget",
                    "CREATE TABLE ref_budget (_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, FBID TEXT, TS INTEGER, Deleted INTEGER, Dirty INTEGER, LastEdited TEXT, Year INTEGER NOT NULL, Month INTEGER NOT NULL, Category INTEGER NOT NULL REFERENCES ref_Categories(_id) ON DELETE CASCADE ON UPDATE CASCADE, Amount REAL NOT NULL, Currency INTEGER NOT NULL REFERENCES ref_Currencies(_id) ON DELETE CASCADE ON UPDATE CASCADE, UNIQUE (Deleted, Year, Month, Category, Currency) ON CONFLICT REPLACE);",
                    "_id, FBID, TS, Deleted, Dirty, LastEdited, Year, Month, Category, Amount, Currency"),
            new TableInfo("ref_budget_debts",
                    "CREATE TABLE ref_budget_debts (_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, FBID TEXT, TS INTEGER, Deleted INTEGER, Dirty INTEGER, LastEdited TEXT, Year INTEGER NOT NULL, Month INTEGER NOT NULL, Debt INTEGER NOT NULL REFERENCES ref_Debts(_id) ON DELETE CASCADE ON UPDATE CASCADE, Amount REAL NOT NULL, UNIQUE (Deleted, Year, Month, Debt) ON CONFLICT REPLACE);",
                    "_id, FBID, TS, Deleted, Dirty, LastEdited, Year, Month, Debt, Amount"),
            new TableInfo("ref_Senders",
                    "CREATE TABLE ref_Senders (_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, FBID TEXT, TS INTEGER, Deleted INTEGER, Dirty INTEGER, LastEdited TEXT, Name TEXT UNIQUE NOT NULL, PhoneNo TEXT UNIQUE NOT NULL, AmountPos INTEGER, BalancePos INTEGER, LeadingCurrencySymbol INTEGER, DateFormat TEXT, isActive INTEGER NOT NULL, UNIQUE (Name, Deleted) ON CONFLICT ABORT, UNIQUE (PhoneNo, Deleted) ON CONFLICT ABORT);",
                    "_id, FBID, TS, Deleted, Dirty, LastEdited, Name, PhoneNo, AmountPos, BalancePos, LeadingCurrencySymbol, DateFormat, isActive")
    };
    //</editor-fold>

    //<editor-fold desc="tableInfo19" defaultstate="collapsed">
    private static TableInfo tableInfo19[] = new TableInfo[]{
            new TableInfo("ref_Currencies",
                    "CREATE TABLE ref_Currencies (_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, FBID TEXT, TS INTEGER, Deleted INTEGER, Dirty INTEGER, LastEdited TEXT, Code TEXT NOT NULL, Symbol TEXT NOT NULL, Name TEXT NOT NULL, DecimalCount INTEGER NOT NULL, UNIQUE (Code, Deleted) ON CONFLICT ABORT);",
                    "_id, FBID, TS, Deleted, Dirty, LastEdited, Code, Symbol, Name, DecimalCount"),
            new TableInfo("ref_Accounts",
                    "CREATE TABLE ref_Accounts (_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, FBID TEXT, TS INTEGER, Deleted INTEGER, Dirty INTEGER, LastEdited TEXT, Type INTEGER NOT NULL, Name TEXT NOT NULL, Currency INTEGER REFERENCES ref_Currencies(_id) ON DELETE SET NULL ON UPDATE CASCADE, Emitent TEXT, Last4Digits INTEGER, Comment TEXT, StartBalance REAL NOT NULL, IsClosed INTEGER NOT NULL, CustomOrder INTEGER, CreditLimit REAL, UNIQUE (Name, Deleted) ON CONFLICT ABORT);",
                    "_id, FBID, TS, Deleted, Dirty, LastEdited, Type, Name, Currency, Emitent, Last4Digits, Comment, StartBalance, IsClosed, CustomOrder, CreditLimit"),
            new TableInfo("ref_Categories",
                    "CREATE TABLE ref_Categories (_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, FBID TEXT, TS INTEGER, Deleted INTEGER, Dirty INTEGER, LastEdited TEXT, Name TEXT NOT NULL, COLOR TEXT, ParentCategory INTEGER REFERENCES ref_Categories(_id) ON DELETE SET NULL ON UPDATE CASCADE, OrderNumber INTEGER NOT NULL, Sign INTEGER NOT NULL, UNIQUE (Name, ParentCategory, Deleted) ON CONFLICT ABORT);",
                    "_id, FBID, TS, Deleted, Dirty, LastEdited, Name, COLOR, ParentCategory, OrderNumber, Sign"),
            new TableInfo("ref_Payees",
                    "CREATE TABLE ref_Payees (_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, FBID TEXT, TS INTEGER, Deleted INTEGER, Dirty INTEGER, LastEdited TEXT, Name TEXT NOT NULL, DefCategory INTEGER REFERENCES ref_Categories(_id) ON DELETE SET NULL ON UPDATE CASCADE, UNIQUE (Name, Deleted) ON CONFLICT ABORT);",
                    "_id, FBID, TS, Deleted, Dirty, LastEdited, Name, DefCategory"),
            new TableInfo("ref_Projects",
                    "CREATE TABLE ref_Projects (_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, FBID TEXT, TS INTEGER, Deleted INTEGER, Dirty INTEGER, LastEdited TEXT, Name TEXT NOT NULL, isActive INTEGER NOT NULL, UNIQUE (Name, Deleted) ON CONFLICT ABORT);",
                    "_id, FBID, TS, Deleted, Dirty, LastEdited, Name, isActive"),
            new TableInfo("ref_Locations",
                    "CREATE TABLE ref_Locations (_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, FBID TEXT, TS INTEGER, Deleted INTEGER, Dirty INTEGER, LastEdited TEXT, Name TEXT NOT NULL, Lon REAL NOT NULL, Lat REAL NOT NULL, Radius INTEGER, Address TEXT, UNIQUE (Name, Deleted) ON CONFLICT ABORT);",
                    "_id, FBID, TS, Deleted, Dirty, LastEdited, Name, Lon, Lat, Radius, Address"),
            new TableInfo("ref_Departments",
                    "CREATE TABLE ref_Departments (_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, FBID TEXT, TS INTEGER, Deleted INTEGER, Dirty INTEGER, LastEdited TEXT, Name TEXT NOT NULL, isActive INTEGER NOT NULL, UNIQUE (Name, Deleted) ON CONFLICT ABORT);",
                    "_id, FBID, TS, Deleted, Dirty, LastEdited, Name, isActive"),
            new TableInfo("ref_SimpleDebts",
                    "CREATE TABLE ref_SimpleDebts (_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, FBID TEXT, TS INTEGER, Deleted INTEGER, Dirty INTEGER, LastEdited TEXT, Name TEXT NOT NULL, isActive INTEGER NOT NULL, UNIQUE (Name, Deleted) ON CONFLICT ABORT);",
                    "_id, FBID, TS, Deleted, Dirty, LastEdited, Name, isActive"),
            new TableInfo("log_Transactions",
                    "CREATE TABLE log_Transactions (_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, FBID TEXT, TS INTEGER, Deleted INTEGER, Dirty INTEGER, LastEdited TEXT, DateTime INTEGER NOT NULL, SrcAccount INTEGER NOT NULL REFERENCES ref_Accounts(_id) ON DELETE CASCADE ON UPDATE CASCADE, Payee INTEGER REFERENCES ref_Payees(_id) ON DELETE SET NULL ON UPDATE CASCADE, Category INTEGER REFERENCES ref_Categories(_id) ON DELETE SET NULL ON UPDATE CASCADE, Amount REAL NOT NULL, Project INTEGER REFERENCES ref_Projects(_id) ON DELETE SET NULL ON UPDATE CASCADE, SimpleDebt INTEGER REFERENCES ref_SimpleDebts(_id) ON DELETE SET NULL ON UPDATE CASCADE, Department INTEGER REFERENCES ref_Departments(_id) ON DELETE SET NULL ON UPDATE CASCADE, Location INTEGER REFERENCES ref_Locations(_id) ON DELETE SET NULL ON UPDATE CASCADE, Comment TEXT, File TEXT, DestAccount INTEGER NOT NULL REFERENCES ref_Accounts(_id) ON DELETE CASCADE ON UPDATE CASCADE, ExchangeRate REAL NOT NULL, AutoCreated INTEGER NOT NULL, Lon REAL, Lat REAL, Accuracy INTEGER);",
                    "_id, FBID, TS, Deleted, Dirty, LastEdited, DateTime, SrcAccount, Payee, Category, Amount, Project, SimpleDebt, Department, Location, Comment, File, DestAccount, ExchangeRate, AutoCreated, Lon, Lat, Accuracy"),
            new TableInfo("log_Templates",
                    "CREATE TABLE log_Templates (_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, FBID TEXT, TS INTEGER, Deleted INTEGER, Dirty INTEGER, LastEdited TEXT, SrcAccount INTEGER NOT NULL REFERENCES ref_Accounts(_id) ON DELETE CASCADE ON UPDATE CASCADE, Payee INTEGER REFERENCES ref_Payees(_id) ON DELETE SET NULL ON UPDATE CASCADE, Category INTEGER REFERENCES ref_Categories(_id) ON DELETE SET NULL ON UPDATE CASCADE, Amount REAL NOT NULL, Project INTEGER REFERENCES ref_Projects(_id) ON DELETE SET NULL ON UPDATE CASCADE, Department INTEGER REFERENCES ref_Departments(_id) ON DELETE SET NULL ON UPDATE CASCADE, Location INTEGER REFERENCES ref_Locations(_id) ON DELETE SET NULL ON UPDATE CASCADE, Name TEXT NOT NULL, DestAccount INTEGER NOT NULL REFERENCES ref_Accounts(_id) ON DELETE CASCADE ON UPDATE CASCADE, ExchangeRate REAL NOT NULL, Type INTEGER NOT NULL);",
                    "_id, FBID, TS, Deleted, Dirty, LastEdited, SrcAccount, Payee, Category, Amount, Project, Department, Location, Name, DestAccount, ExchangeRate, Type"),
            new TableInfo("log_Incoming_SMS",
                    "CREATE TABLE log_Incoming_SMS (_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, FBID TEXT, TS INTEGER, Deleted INTEGER, Dirty INTEGER, LastEdited TEXT, DateTime TEXT NOT NULL, Sender INTEGER NOT NULL, Body TEXT NOT NULL);",
                    "_id, FBID, TS, Deleted, Dirty, LastEdited, DateTime, Sender, Body"),
            new TableInfo("ref_Sms_Parser_Patterns",
                    "CREATE TABLE ref_Sms_Parser_Patterns (_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, FBID TEXT, TS INTEGER, Deleted INTEGER, Dirty INTEGER, LastEdited TEXT, Type INTEGER NOT NULL, Object TEXT NOT NULL, Pattern TEXT NOT NULL, UNIQUE (Type, Pattern, Deleted) ON CONFLICT REPLACE);",
                    "_id, FBID, TS, Deleted, Dirty, LastEdited, Type, Object, Pattern"),
            new TableInfo("ref_Debts",
                    "CREATE TABLE ref_Debts (_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, FBID TEXT, TS INTEGER, Deleted INTEGER, Dirty INTEGER, LastEdited TEXT, Account INTEGER NOT NULL ON CONFLICT ABORT REFERENCES ref_Accounts(_id) ON DELETE CASCADE ON UPDATE CASCADE, Payee INTEGER NOT NULL REFERENCES ref_Payees(_id) ON DELETE CASCADE ON UPDATE CASCADE, Category INTEGER NOT NULL REFERENCES ref_Categories(_id) ON DELETE SET NULL ON UPDATE CASCADE, Closed INTEGER NOT NULL, SrcAccount INTEGER, Comment TEXT, UNIQUE (Account, Deleted) ON CONFLICT ABORT);",
                    "_id, FBID, TS, Deleted, Dirty, LastEdited, Account ON CONFLICT ABORT, Payee, Category, Closed, SrcAccount, Comment"),
            new TableInfo("ref_budget",
                    "CREATE TABLE ref_budget (_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, FBID TEXT, TS INTEGER, Deleted INTEGER, Dirty INTEGER, LastEdited TEXT, Year INTEGER NOT NULL, Month INTEGER NOT NULL, Category INTEGER NOT NULL REFERENCES ref_Categories(_id) ON DELETE CASCADE ON UPDATE CASCADE, Amount REAL NOT NULL, Currency INTEGER NOT NULL REFERENCES ref_Currencies(_id) ON DELETE CASCADE ON UPDATE CASCADE, UNIQUE (Deleted, Year, Month, Category, Currency) ON CONFLICT REPLACE);",
                    "_id, FBID, TS, Deleted, Dirty, LastEdited, Year, Month, Category, Amount, Currency"),
            new TableInfo("ref_budget_debts",
                    "CREATE TABLE ref_budget_debts (_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, FBID TEXT, TS INTEGER, Deleted INTEGER, Dirty INTEGER, LastEdited TEXT, Year INTEGER NOT NULL, Month INTEGER NOT NULL, Debt INTEGER NOT NULL REFERENCES ref_Debts(_id) ON DELETE CASCADE ON UPDATE CASCADE, Amount REAL NOT NULL, UNIQUE (Deleted, Year, Month, Debt) ON CONFLICT REPLACE);",
                    "_id, FBID, TS, Deleted, Dirty, LastEdited, Year, Month, Debt, Amount"),
            new TableInfo("ref_Senders",
                    "CREATE TABLE ref_Senders (_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, FBID TEXT, TS INTEGER, Deleted INTEGER, Dirty INTEGER, LastEdited TEXT, Name TEXT NOT NULL, PhoneNo TEXT NOT NULL, AmountPos INTEGER, BalancePos INTEGER, LeadingCurrencySymbol INTEGER, DateFormat TEXT, isActive INTEGER NOT NULL, UNIQUE (Name, Deleted) ON CONFLICT ABORT, UNIQUE (PhoneNo, Deleted) ON CONFLICT ABORT);",
                    "_id, FBID, TS, Deleted, Dirty, LastEdited, Name, PhoneNo, AmountPos, BalancePos, LeadingCurrencySymbol, DateFormat, isActive")
    };
    //</editor-fold>

    //<editor-fold desc="tableInfo20" defaultstate="collapsed">
    private static TableInfo tableInfo20[] = new TableInfo[]{
            new TableInfo("ref_Currencies",
                    "CREATE TABLE ref_Currencies (_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, FBID TEXT, TS INTEGER, Deleted INTEGER, Dirty INTEGER, LastEdited TEXT, Code TEXT NOT NULL, Symbol TEXT NOT NULL, Name TEXT NOT NULL, DecimalCount INTEGER NOT NULL, UNIQUE (Code, Deleted) ON CONFLICT ABORT);",
                    "_id, FBID, TS, Deleted, Dirty, LastEdited, Code, Symbol, Name, DecimalCount"),
            new TableInfo("ref_Accounts",
                    "CREATE TABLE ref_Accounts (_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, FBID TEXT, TS INTEGER, Deleted INTEGER, Dirty INTEGER, LastEdited TEXT, Type INTEGER NOT NULL, Name TEXT NOT NULL, Currency INTEGER REFERENCES ref_Currencies(_id) ON DELETE SET NULL ON UPDATE CASCADE, Emitent TEXT, Last4Digits INTEGER, Comment TEXT, StartBalance REAL NOT NULL, IsClosed INTEGER NOT NULL, CustomOrder INTEGER, CreditLimit REAL, UNIQUE (Name, Deleted) ON CONFLICT ABORT);",
                    "_id, FBID, TS, Deleted, Dirty, LastEdited, Type, Name, Currency, Emitent, Last4Digits, Comment, StartBalance, IsClosed, CustomOrder, CreditLimit"),
            new TableInfo("ref_Categories",
                    "CREATE TABLE ref_Categories (_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, FBID TEXT, TS INTEGER, Deleted INTEGER, Dirty INTEGER, LastEdited TEXT, Name TEXT NOT NULL, COLOR TEXT, ParentCategory INTEGER REFERENCES ref_Categories(_id) ON DELETE SET NULL ON UPDATE CASCADE, OrderNumber INTEGER NOT NULL, Sign INTEGER NOT NULL, UNIQUE (Name, ParentCategory, Deleted) ON CONFLICT ABORT);",
                    "_id, FBID, TS, Deleted, Dirty, LastEdited, Name, COLOR, ParentCategory, OrderNumber, Sign"),
            new TableInfo("ref_Payees",
                    "CREATE TABLE ref_Payees (_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, FBID TEXT, TS INTEGER, Deleted INTEGER, Dirty INTEGER, LastEdited TEXT, Name TEXT NOT NULL, DefCategory INTEGER REFERENCES ref_Categories(_id) ON DELETE SET NULL ON UPDATE CASCADE, UNIQUE (Name, Deleted) ON CONFLICT ABORT);",
                    "_id, FBID, TS, Deleted, Dirty, LastEdited, Name, DefCategory"),
            new TableInfo("ref_Projects",
                    "CREATE TABLE ref_Projects (_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, FBID TEXT, TS INTEGER, Deleted INTEGER, Dirty INTEGER, LastEdited TEXT, Name TEXT NOT NULL, isActive INTEGER NOT NULL, UNIQUE (Name, Deleted) ON CONFLICT ABORT);",
                    "_id, FBID, TS, Deleted, Dirty, LastEdited, Name, isActive"),
            new TableInfo("ref_Locations",
                    "CREATE TABLE ref_Locations (_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, FBID TEXT, TS INTEGER, Deleted INTEGER, Dirty INTEGER, LastEdited TEXT, Name TEXT NOT NULL, Lon REAL NOT NULL, Lat REAL NOT NULL, Radius INTEGER, Address TEXT, UNIQUE (Name, Deleted) ON CONFLICT ABORT);",
                    "_id, FBID, TS, Deleted, Dirty, LastEdited, Name, Lon, Lat, Radius, Address"),
            new TableInfo("ref_Departments",
                    "CREATE TABLE ref_Departments (_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, FBID TEXT, TS INTEGER, Deleted INTEGER, Dirty INTEGER, LastEdited TEXT, Name TEXT NOT NULL, isActive INTEGER NOT NULL, UNIQUE (Name, Deleted) ON CONFLICT ABORT);",
                    "_id, FBID, TS, Deleted, Dirty, LastEdited, Name, isActive"),
            new TableInfo("ref_SimpleDebts",
                    "CREATE TABLE ref_SimpleDebts (_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, FBID TEXT, TS INTEGER, Deleted INTEGER, Dirty INTEGER, LastEdited TEXT, Name TEXT NOT NULL, isActive INTEGER NOT NULL, UNIQUE (Name, Deleted) ON CONFLICT ABORT);",
                    "_id, FBID, TS, Deleted, Dirty, LastEdited, Name, isActive"),
            new TableInfo("log_Transactions",
                    "CREATE TABLE log_Transactions (_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, FBID TEXT, TS INTEGER, Deleted INTEGER, Dirty INTEGER, LastEdited TEXT, DateTime INTEGER NOT NULL, SrcAccount INTEGER NOT NULL REFERENCES ref_Accounts(_id) ON DELETE CASCADE ON UPDATE CASCADE, Payee INTEGER REFERENCES ref_Payees(_id) ON DELETE SET NULL ON UPDATE CASCADE, Category INTEGER REFERENCES ref_Categories(_id) ON DELETE SET NULL ON UPDATE CASCADE, Amount REAL NOT NULL, Project INTEGER REFERENCES ref_Projects(_id) ON DELETE SET NULL ON UPDATE CASCADE, SimpleDebt INTEGER REFERENCES ref_SimpleDebts(_id) ON DELETE SET NULL ON UPDATE CASCADE, Department INTEGER REFERENCES ref_Departments(_id) ON DELETE SET NULL ON UPDATE CASCADE, Location INTEGER REFERENCES ref_Locations(_id) ON DELETE SET NULL ON UPDATE CASCADE, Comment TEXT, File TEXT, DestAccount INTEGER NOT NULL REFERENCES ref_Accounts(_id) ON DELETE CASCADE ON UPDATE CASCADE, ExchangeRate REAL NOT NULL, AutoCreated INTEGER NOT NULL, Lon REAL, Lat REAL, Accuracy INTEGER);",
                    "_id, FBID, TS, Deleted, Dirty, LastEdited, DateTime, SrcAccount, Payee, Category, Amount, Project, SimpleDebt, Department, Location, Comment, File, DestAccount, ExchangeRate, AutoCreated, Lon, Lat, Accuracy"),
            new TableInfo("log_Templates",
                    "CREATE TABLE log_Templates (_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, FBID TEXT, TS INTEGER, Deleted INTEGER, Dirty INTEGER, LastEdited TEXT, SrcAccount INTEGER NOT NULL REFERENCES ref_Accounts(_id) ON DELETE CASCADE ON UPDATE CASCADE, Payee INTEGER REFERENCES ref_Payees(_id) ON DELETE SET NULL ON UPDATE CASCADE, Category INTEGER REFERENCES ref_Categories(_id) ON DELETE SET NULL ON UPDATE CASCADE, Amount REAL NOT NULL, Project INTEGER REFERENCES ref_Projects(_id) ON DELETE SET NULL ON UPDATE CASCADE, Department INTEGER REFERENCES ref_Departments(_id) ON DELETE SET NULL ON UPDATE CASCADE, Location INTEGER REFERENCES ref_Locations(_id) ON DELETE SET NULL ON UPDATE CASCADE, Name TEXT NOT NULL, DestAccount INTEGER NOT NULL REFERENCES ref_Accounts(_id) ON DELETE CASCADE ON UPDATE CASCADE, ExchangeRate REAL NOT NULL, Type INTEGER NOT NULL, Comment TEXT);",
                    "_id, FBID, TS, Deleted, Dirty, LastEdited, SrcAccount, Payee, Category, Amount, Project, Department, Location, Name, DestAccount, ExchangeRate, Type, Comment"),
            new TableInfo("log_Incoming_SMS",
                    "CREATE TABLE log_Incoming_SMS (_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, FBID TEXT, TS INTEGER, Deleted INTEGER, Dirty INTEGER, LastEdited TEXT, DateTime TEXT NOT NULL, Sender INTEGER NOT NULL, Body TEXT NOT NULL);",
                    "_id, FBID, TS, Deleted, Dirty, LastEdited, DateTime, Sender, Body"),
            new TableInfo("ref_Sms_Parser_Patterns",
                    "CREATE TABLE ref_Sms_Parser_Patterns (_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, FBID TEXT, TS INTEGER, Deleted INTEGER, Dirty INTEGER, LastEdited TEXT, Type INTEGER NOT NULL, Object TEXT NOT NULL, Pattern TEXT NOT NULL, UNIQUE (Type, Pattern, Deleted) ON CONFLICT REPLACE);",
                    "_id, FBID, TS, Deleted, Dirty, LastEdited, Type, Object, Pattern"),
            new TableInfo("ref_Debts",
                    "CREATE TABLE ref_Debts (_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, FBID TEXT, TS INTEGER, Deleted INTEGER, Dirty INTEGER, LastEdited TEXT, Account INTEGER NOT NULL ON CONFLICT ABORT REFERENCES ref_Accounts(_id) ON DELETE CASCADE ON UPDATE CASCADE, Payee INTEGER NOT NULL REFERENCES ref_Payees(_id) ON DELETE CASCADE ON UPDATE CASCADE, Category INTEGER NOT NULL REFERENCES ref_Categories(_id) ON DELETE SET NULL ON UPDATE CASCADE, Closed INTEGER NOT NULL, SrcAccount INTEGER, Comment TEXT, UNIQUE (Account, Deleted) ON CONFLICT ABORT);",
                    "_id, FBID, TS, Deleted, Dirty, LastEdited, Account ON CONFLICT ABORT, Payee, Category, Closed, SrcAccount, Comment"),
            new TableInfo("ref_budget",
                    "CREATE TABLE ref_budget (_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, FBID TEXT, TS INTEGER, Deleted INTEGER, Dirty INTEGER, LastEdited TEXT, Year INTEGER NOT NULL, Month INTEGER NOT NULL, Category INTEGER NOT NULL REFERENCES ref_Categories(_id) ON DELETE CASCADE ON UPDATE CASCADE, Amount REAL NOT NULL, Currency INTEGER NOT NULL REFERENCES ref_Currencies(_id) ON DELETE CASCADE ON UPDATE CASCADE, UNIQUE (Deleted, Year, Month, Category, Currency) ON CONFLICT REPLACE);",
                    "_id, FBID, TS, Deleted, Dirty, LastEdited, Year, Month, Category, Amount, Currency"),
            new TableInfo("ref_budget_debts",
                    "CREATE TABLE ref_budget_debts (_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, FBID TEXT, TS INTEGER, Deleted INTEGER, Dirty INTEGER, LastEdited TEXT, Year INTEGER NOT NULL, Month INTEGER NOT NULL, Debt INTEGER NOT NULL REFERENCES ref_Debts(_id) ON DELETE CASCADE ON UPDATE CASCADE, Amount REAL NOT NULL, UNIQUE (Deleted, Year, Month, Debt) ON CONFLICT REPLACE);",
                    "_id, FBID, TS, Deleted, Dirty, LastEdited, Year, Month, Debt, Amount"),
            new TableInfo("ref_Senders",
                    "CREATE TABLE ref_Senders (_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, FBID TEXT, TS INTEGER, Deleted INTEGER, Dirty INTEGER, LastEdited TEXT, Name TEXT NOT NULL, PhoneNo TEXT NOT NULL, AmountPos INTEGER, BalancePos INTEGER, LeadingCurrencySymbol INTEGER, DateFormat TEXT, isActive INTEGER NOT NULL, UNIQUE (Name, Deleted) ON CONFLICT ABORT, UNIQUE (PhoneNo, Deleted) ON CONFLICT ABORT);",
                    "_id, FBID, TS, Deleted, Dirty, LastEdited, Name, PhoneNo, AmountPos, BalancePos, LeadingCurrencySymbol, DateFormat, isActive")
    };
    //</editor-fold>

    //<editor-fold desc="tableInfo21" defaultstate="collapsed">
    private static TableInfo tableInfo21[] = new TableInfo[]{
            new TableInfo("ref_Currencies",
                    "CREATE TABLE ref_Currencies (_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, FBID TEXT, TS INTEGER, Deleted INTEGER, Dirty INTEGER, LastEdited TEXT, Code TEXT NOT NULL, Symbol TEXT NOT NULL, Name TEXT NOT NULL, DecimalCount INTEGER NOT NULL, UNIQUE (Code, Deleted) ON CONFLICT ABORT);",
                    "_id, FBID, TS, Deleted, Dirty, LastEdited, Code, Symbol, Name, DecimalCount"),
            new TableInfo("ref_Accounts",
                    "CREATE TABLE ref_Accounts (_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, FBID TEXT, TS INTEGER, Deleted INTEGER, Dirty INTEGER, LastEdited TEXT, Type INTEGER NOT NULL, Name TEXT NOT NULL, Currency INTEGER REFERENCES ref_Currencies(_id) ON DELETE SET NULL ON UPDATE CASCADE, Emitent TEXT, Last4Digits INTEGER, Comment TEXT, StartBalance REAL NOT NULL, IsClosed INTEGER NOT NULL, CustomOrder INTEGER, CreditLimit REAL, UNIQUE (Name, Deleted) ON CONFLICT ABORT);",
                    "_id, FBID, TS, Deleted, Dirty, LastEdited, Type, Name, Currency, Emitent, Last4Digits, Comment, StartBalance, IsClosed, CustomOrder, CreditLimit"),
            new TableInfo("ref_Categories",
                    "CREATE TABLE ref_Categories (_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, FBID TEXT, TS INTEGER, Deleted INTEGER, Dirty INTEGER, LastEdited TEXT, Name TEXT NOT NULL, COLOR TEXT, ParentCategory INTEGER REFERENCES ref_Categories(_id) ON DELETE SET NULL ON UPDATE CASCADE, OrderNumber INTEGER NOT NULL, Sign INTEGER NOT NULL, UNIQUE (Name, ParentCategory, Deleted) ON CONFLICT ABORT);",
                    "_id, FBID, TS, Deleted, Dirty, LastEdited, Name, COLOR, ParentCategory, OrderNumber, Sign"),
            new TableInfo("ref_Payees",
                    "CREATE TABLE ref_Payees (_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, FBID TEXT, TS INTEGER, Deleted INTEGER, Dirty INTEGER, LastEdited TEXT, Name TEXT NOT NULL, DefCategory INTEGER REFERENCES ref_Categories(_id) ON DELETE SET NULL ON UPDATE CASCADE, UNIQUE (Name, Deleted) ON CONFLICT ABORT);",
                    "_id, FBID, TS, Deleted, Dirty, LastEdited, Name, DefCategory"),
            new TableInfo("ref_Projects",
                    "CREATE TABLE ref_Projects (_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, FBID TEXT, TS INTEGER, Deleted INTEGER, Dirty INTEGER, LastEdited TEXT, Name TEXT NOT NULL, isActive INTEGER NOT NULL, UNIQUE (Name, Deleted) ON CONFLICT ABORT);",
                    "_id, FBID, TS, Deleted, Dirty, LastEdited, Name, isActive"),
            new TableInfo("ref_Locations",
                    "CREATE TABLE ref_Locations (_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, FBID TEXT, TS INTEGER, Deleted INTEGER, Dirty INTEGER, LastEdited TEXT, Name TEXT NOT NULL, Lon REAL NOT NULL, Lat REAL NOT NULL, Radius INTEGER, Address TEXT, UNIQUE (Name, Deleted) ON CONFLICT ABORT);",
                    "_id, FBID, TS, Deleted, Dirty, LastEdited, Name, Lon, Lat, Radius, Address"),
            new TableInfo("ref_Departments",
                    "CREATE TABLE ref_Departments (_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, FBID TEXT, TS INTEGER, Deleted INTEGER, Dirty INTEGER, LastEdited TEXT, Name TEXT NOT NULL, isActive INTEGER NOT NULL, UNIQUE (Name, Deleted) ON CONFLICT ABORT);",
                    "_id, FBID, TS, Deleted, Dirty, LastEdited, Name, isActive"),
            new TableInfo("ref_SimpleDebts",
                    "CREATE TABLE ref_SimpleDebts (_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, FBID TEXT, TS INTEGER, Deleted INTEGER, Dirty INTEGER, LastEdited TEXT, Name TEXT NOT NULL, isActive INTEGER NOT NULL, UNIQUE (Name, Deleted) ON CONFLICT ABORT);",
                    "_id, FBID, TS, Deleted, Dirty, LastEdited, Name, isActive"),
            new TableInfo("log_Transactions",
                    "CREATE TABLE log_Transactions (_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, FBID TEXT, TS INTEGER, Deleted INTEGER, Dirty INTEGER, LastEdited TEXT, DateTime INTEGER NOT NULL, SrcAccount INTEGER NOT NULL REFERENCES ref_Accounts(_id) ON DELETE CASCADE ON UPDATE CASCADE, Payee INTEGER REFERENCES ref_Payees(_id) ON DELETE SET NULL ON UPDATE CASCADE, Category INTEGER REFERENCES ref_Categories(_id) ON DELETE SET NULL ON UPDATE CASCADE, Amount REAL NOT NULL, Project INTEGER REFERENCES ref_Projects(_id) ON DELETE SET NULL ON UPDATE CASCADE, SimpleDebt INTEGER REFERENCES ref_SimpleDebts(_id) ON DELETE SET NULL ON UPDATE CASCADE, Department INTEGER REFERENCES ref_Departments(_id) ON DELETE SET NULL ON UPDATE CASCADE, Location INTEGER REFERENCES ref_Locations(_id) ON DELETE SET NULL ON UPDATE CASCADE, Comment TEXT, File TEXT, DestAccount INTEGER NOT NULL REFERENCES ref_Accounts(_id) ON DELETE CASCADE ON UPDATE CASCADE, ExchangeRate REAL NOT NULL, AutoCreated INTEGER NOT NULL, Lon REAL, Lat REAL, Accuracy INTEGER);",
                    "_id, FBID, TS, Deleted, Dirty, LastEdited, DateTime, SrcAccount, Payee, Category, Amount, Project, SimpleDebt, Department, Location, Comment, File, DestAccount, ExchangeRate, AutoCreated, Lon, Lat, Accuracy"),
            new TableInfo("log_Templates",
                    "CREATE TABLE log_Templates (_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, FBID TEXT, TS INTEGER, Deleted INTEGER, Dirty INTEGER, LastEdited TEXT, SrcAccount INTEGER NOT NULL REFERENCES ref_Accounts(_id) ON DELETE CASCADE ON UPDATE CASCADE, Payee INTEGER REFERENCES ref_Payees(_id) ON DELETE SET NULL ON UPDATE CASCADE, Category INTEGER REFERENCES ref_Categories(_id) ON DELETE SET NULL ON UPDATE CASCADE, Amount REAL NOT NULL, Project INTEGER REFERENCES ref_Projects(_id) ON DELETE SET NULL ON UPDATE CASCADE, Department INTEGER REFERENCES ref_Departments(_id) ON DELETE SET NULL ON UPDATE CASCADE, Location INTEGER REFERENCES ref_Locations(_id) ON DELETE SET NULL ON UPDATE CASCADE, Name TEXT NOT NULL, DestAccount INTEGER NOT NULL REFERENCES ref_Accounts(_id) ON DELETE CASCADE ON UPDATE CASCADE, ExchangeRate REAL NOT NULL, Type INTEGER NOT NULL, Comment TEXT);",
                    "_id, FBID, TS, Deleted, Dirty, LastEdited, SrcAccount, Payee, Category, Amount, Project, Department, Location, Name, DestAccount, ExchangeRate, Type, Comment"),
            new TableInfo("log_Incoming_SMS",
                    "CREATE TABLE log_Incoming_SMS (_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, FBID TEXT, TS INTEGER, Deleted INTEGER, Dirty INTEGER, LastEdited TEXT, DateTime TEXT NOT NULL, Sender INTEGER NOT NULL, Body TEXT NOT NULL);",
                    "_id, FBID, TS, Deleted, Dirty, LastEdited, DateTime, Sender, Body"),
            new TableInfo("ref_Sms_Parser_Patterns",
                    "CREATE TABLE ref_Sms_Parser_Patterns (_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, FBID TEXT, TS INTEGER, Deleted INTEGER, Dirty INTEGER, LastEdited TEXT, Type INTEGER NOT NULL, Object TEXT NOT NULL, Pattern TEXT NOT NULL, UNIQUE (Type, Pattern, Deleted) ON CONFLICT REPLACE);",
                    "_id, FBID, TS, Deleted, Dirty, LastEdited, Type, Object, Pattern"),
            new TableInfo("ref_Debts",
                    "CREATE TABLE ref_Debts (_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, FBID TEXT, TS INTEGER, Deleted INTEGER, Dirty INTEGER, LastEdited TEXT, Account INTEGER NOT NULL ON CONFLICT ABORT REFERENCES ref_Accounts(_id) ON DELETE CASCADE ON UPDATE CASCADE, Payee INTEGER NOT NULL REFERENCES ref_Payees(_id) ON DELETE CASCADE ON UPDATE CASCADE, Category INTEGER NOT NULL REFERENCES ref_Categories(_id) ON DELETE SET NULL ON UPDATE CASCADE, Closed INTEGER NOT NULL, SrcAccount INTEGER, Comment TEXT, UNIQUE (Account, Deleted) ON CONFLICT ABORT);",
                    "_id, FBID, TS, Deleted, Dirty, LastEdited, Account ON CONFLICT ABORT, Payee, Category, Closed, SrcAccount, Comment"),
            new TableInfo("ref_budget",
                    "CREATE TABLE ref_budget (_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, FBID TEXT, TS INTEGER, Deleted INTEGER, Dirty INTEGER, LastEdited TEXT, Year INTEGER NOT NULL, Month INTEGER NOT NULL, Category INTEGER NOT NULL REFERENCES ref_Categories(_id) ON DELETE CASCADE ON UPDATE CASCADE, Amount REAL NOT NULL, Currency INTEGER NOT NULL REFERENCES ref_Currencies(_id) ON DELETE CASCADE ON UPDATE CASCADE, UNIQUE (Deleted, Year, Month, Category, Currency) ON CONFLICT REPLACE);",
                    "_id, FBID, TS, Deleted, Dirty, LastEdited, Year, Month, Category, Amount, Currency"),
            new TableInfo("ref_budget_debts",
                    "CREATE TABLE ref_budget_debts (_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, FBID TEXT, TS INTEGER, Deleted INTEGER, Dirty INTEGER, LastEdited TEXT, Year INTEGER NOT NULL, Month INTEGER NOT NULL, Debt INTEGER NOT NULL REFERENCES ref_Debts(_id) ON DELETE CASCADE ON UPDATE CASCADE, Amount REAL NOT NULL, UNIQUE (Deleted, Year, Month, Debt) ON CONFLICT REPLACE);",
                    "_id, FBID, TS, Deleted, Dirty, LastEdited, Year, Month, Debt, Amount"),
            new TableInfo("ref_Senders",
                    "CREATE TABLE ref_Senders (_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, FBID TEXT, TS INTEGER, Deleted INTEGER, Dirty INTEGER, LastEdited TEXT, Name TEXT NOT NULL, PhoneNo TEXT NOT NULL, AmountPos INTEGER, BalancePos INTEGER, LeadingCurrencySymbol INTEGER, DateFormat TEXT, isActive INTEGER NOT NULL, UNIQUE (Name, Deleted) ON CONFLICT ABORT, UNIQUE (PhoneNo, Deleted) ON CONFLICT ABORT);",
                    "_id, FBID, TS, Deleted, Dirty, LastEdited, Name, PhoneNo, AmountPos, BalancePos, LeadingCurrencySymbol, DateFormat, isActive")
    };
    //</editor-fold>

    //<editor-fold desc="tableInfo22" defaultstate="collapsed">
    private static TableInfo tableInfo22[] = new TableInfo[]{
            new TableInfo("ref_Currencies",
                    "CREATE TABLE ref_Currencies (_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, FBID TEXT, TS INTEGER, Deleted INTEGER, Dirty INTEGER, LastEdited TEXT, Code TEXT NOT NULL, Symbol TEXT NOT NULL, Name TEXT NOT NULL, DecimalCount INTEGER NOT NULL, UNIQUE (Code, Deleted) ON CONFLICT ABORT);",
                    "_id, FBID, TS, Deleted, Dirty, LastEdited, Code, Symbol, Name, DecimalCount"),
            new TableInfo("ref_Accounts",
                    "CREATE TABLE ref_Accounts (_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, FBID TEXT, TS INTEGER, Deleted INTEGER, Dirty INTEGER, LastEdited TEXT, Type INTEGER NOT NULL, Name TEXT NOT NULL, Currency INTEGER REFERENCES ref_Currencies(_id) ON DELETE SET NULL ON UPDATE CASCADE, Emitent TEXT, Last4Digits INTEGER, Comment TEXT, StartBalance REAL NOT NULL, IsClosed INTEGER NOT NULL, CustomOrder INTEGER, CreditLimit REAL, UNIQUE (Name, Deleted) ON CONFLICT ABORT);",
                    "_id, FBID, TS, Deleted, Dirty, LastEdited, Type, Name, Currency, Emitent, Last4Digits, Comment, StartBalance, IsClosed, CustomOrder, CreditLimit"),
            new TableInfo("ref_Categories",
                    "CREATE TABLE ref_Categories (_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, FBID TEXT, TS INTEGER, Deleted INTEGER, Dirty INTEGER, LastEdited TEXT, Name TEXT NOT NULL, COLOR TEXT, ParentID INTEGER REFERENCES ref_Categories(_id) ON DELETE SET NULL ON UPDATE CASCADE, OrderNumber INTEGER NOT NULL, Sign INTEGER NOT NULL, UNIQUE (Name, ParentID, Deleted) ON CONFLICT ABORT);",
                    "_id, FBID, TS, Deleted, Dirty, LastEdited, Name, COLOR, ParentID, OrderNumber, Sign"),
            new TableInfo("ref_Payees",
                    "CREATE TABLE ref_Payees (_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, FBID TEXT, TS INTEGER, Deleted INTEGER, Dirty INTEGER, LastEdited TEXT, Name TEXT NOT NULL, DefCategory INTEGER REFERENCES ref_Categories(_id) ON DELETE SET NULL ON UPDATE CASCADE, ParentID INTEGER REFERENCES ref_Payees(_id) ON DELETE SET NULL ON UPDATE CASCADE, OrderNumber INTEGER, UNIQUE (Name, ParentID, Deleted) ON CONFLICT ABORT);",
                    "_id, FBID, TS, Deleted, Dirty, LastEdited, Name, DefCategory, ParentID, OrderNumber"),
            new TableInfo("ref_Projects",
                    "CREATE TABLE ref_Projects (_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, FBID TEXT, TS INTEGER, Deleted INTEGER, Dirty INTEGER, LastEdited TEXT, Name TEXT NOT NULL, isActive INTEGER NOT NULL, ParentID INTEGER REFERENCES ref_Projects(_id) ON DELETE SET NULL ON UPDATE CASCADE, OrderNumber INTEGER, UNIQUE (Name, ParentID, Deleted) ON CONFLICT ABORT);",
                    "_id, FBID, TS, Deleted, Dirty, LastEdited, Name, isActive, ParentID, OrderNumber"),
            new TableInfo("ref_Locations",
                    "CREATE TABLE ref_Locations (_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, FBID TEXT, TS INTEGER, Deleted INTEGER, Dirty INTEGER, LastEdited TEXT, Name TEXT NOT NULL, Lon REAL NOT NULL, Lat REAL NOT NULL, Radius INTEGER, Address TEXT, ParentID INTEGER REFERENCES ref_Locations(_id) ON DELETE SET NULL ON UPDATE CASCADE, OrderNumber INTEGER, UNIQUE (Name, ParentID, Deleted) ON CONFLICT ABORT);",
                    "_id, FBID, TS, Deleted, Dirty, LastEdited, Name, Lon, Lat, Radius, Address, ParentID, OrderNumber"),
            new TableInfo("ref_Departments",
                    "CREATE TABLE ref_Departments (_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, FBID TEXT, TS INTEGER, Deleted INTEGER, Dirty INTEGER, LastEdited TEXT, Name TEXT NOT NULL, isActive INTEGER NOT NULL, ParentID INTEGER REFERENCES ref_Departments(_id) ON DELETE SET NULL ON UPDATE CASCADE, OrderNumber INTEGER, UNIQUE (Name, ParentID, Deleted) ON CONFLICT ABORT);",
                    "_id, FBID, TS, Deleted, Dirty, LastEdited, Name, isActive, ParentID, OrderNumber"),
            new TableInfo("ref_SimpleDebts",
                    "CREATE TABLE ref_SimpleDebts (_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, FBID TEXT, TS INTEGER, Deleted INTEGER, Dirty INTEGER, LastEdited TEXT, Name TEXT NOT NULL, isActive INTEGER NOT NULL, UNIQUE (Name, Deleted) ON CONFLICT ABORT);",
                    "_id, FBID, TS, Deleted, Dirty, LastEdited, Name, isActive"),
            new TableInfo("log_Transactions",
                    "CREATE TABLE log_Transactions (_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, FBID TEXT, TS INTEGER, Deleted INTEGER, Dirty INTEGER, LastEdited TEXT, DateTime INTEGER NOT NULL, SrcAccount INTEGER NOT NULL REFERENCES ref_Accounts(_id) ON DELETE CASCADE ON UPDATE CASCADE, Payee INTEGER REFERENCES ref_Payees(_id) ON DELETE SET NULL ON UPDATE CASCADE, Category INTEGER REFERENCES ref_Categories(_id) ON DELETE SET NULL ON UPDATE CASCADE, Amount REAL NOT NULL, Project INTEGER REFERENCES ref_Projects(_id) ON DELETE SET NULL ON UPDATE CASCADE, SimpleDebt INTEGER REFERENCES ref_SimpleDebts(_id) ON DELETE SET NULL ON UPDATE CASCADE, Department INTEGER REFERENCES ref_Departments(_id) ON DELETE SET NULL ON UPDATE CASCADE, Location INTEGER REFERENCES ref_Locations(_id) ON DELETE SET NULL ON UPDATE CASCADE, Comment TEXT, File TEXT, DestAccount INTEGER NOT NULL REFERENCES ref_Accounts(_id) ON DELETE CASCADE ON UPDATE CASCADE, ExchangeRate REAL NOT NULL, AutoCreated INTEGER NOT NULL, Lon REAL, Lat REAL, Accuracy INTEGER);",
                    "_id, FBID, TS, Deleted, Dirty, LastEdited, DateTime, SrcAccount, Payee, Category, Amount, Project, SimpleDebt, Department, Location, Comment, File, DestAccount, ExchangeRate, AutoCreated, Lon, Lat, Accuracy"),
            new TableInfo("log_Templates",
                    "CREATE TABLE log_Templates (_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, FBID TEXT, TS INTEGER, Deleted INTEGER, Dirty INTEGER, LastEdited TEXT, SrcAccount INTEGER NOT NULL REFERENCES ref_Accounts(_id) ON DELETE CASCADE ON UPDATE CASCADE, Payee INTEGER REFERENCES ref_Payees(_id) ON DELETE SET NULL ON UPDATE CASCADE, Category INTEGER REFERENCES ref_Categories(_id) ON DELETE SET NULL ON UPDATE CASCADE, Amount REAL NOT NULL, Project INTEGER REFERENCES ref_Projects(_id) ON DELETE SET NULL ON UPDATE CASCADE, Department INTEGER REFERENCES ref_Departments(_id) ON DELETE SET NULL ON UPDATE CASCADE, Location INTEGER REFERENCES ref_Locations(_id) ON DELETE SET NULL ON UPDATE CASCADE, Name TEXT NOT NULL, DestAccount INTEGER NOT NULL REFERENCES ref_Accounts(_id) ON DELETE CASCADE ON UPDATE CASCADE, ExchangeRate REAL NOT NULL, Type INTEGER NOT NULL, Comment TEXT);",
                    "_id, FBID, TS, Deleted, Dirty, LastEdited, SrcAccount, Payee, Category, Amount, Project, Department, Location, Name, DestAccount, ExchangeRate, Type, Comment"),
            new TableInfo("log_Incoming_SMS",
                    "CREATE TABLE log_Incoming_SMS (_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, FBID TEXT, TS INTEGER, Deleted INTEGER, Dirty INTEGER, LastEdited TEXT, DateTime TEXT NOT NULL, Sender INTEGER NOT NULL, Body TEXT NOT NULL);",
                    "_id, FBID, TS, Deleted, Dirty, LastEdited, DateTime, Sender, Body"),
            new TableInfo("ref_Sms_Parser_Patterns",
                    "CREATE TABLE ref_Sms_Parser_Patterns (_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, FBID TEXT, TS INTEGER, Deleted INTEGER, Dirty INTEGER, LastEdited TEXT, Type INTEGER NOT NULL, Object TEXT NOT NULL, Pattern TEXT NOT NULL, UNIQUE (Type, Pattern, Deleted) ON CONFLICT REPLACE);",
                    "_id, FBID, TS, Deleted, Dirty, LastEdited, Type, Object, Pattern"),
            new TableInfo("ref_Debts",
                    "CREATE TABLE ref_Debts (_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, FBID TEXT, TS INTEGER, Deleted INTEGER, Dirty INTEGER, LastEdited TEXT, Account INTEGER NOT NULL ON CONFLICT ABORT REFERENCES ref_Accounts(_id) ON DELETE CASCADE ON UPDATE CASCADE, Payee INTEGER NOT NULL REFERENCES ref_Payees(_id) ON DELETE CASCADE ON UPDATE CASCADE, Category INTEGER NOT NULL REFERENCES ref_Categories(_id) ON DELETE SET NULL ON UPDATE CASCADE, Closed INTEGER NOT NULL, SrcAccount INTEGER, Comment TEXT, UNIQUE (Account, Deleted) ON CONFLICT ABORT);",
                    "_id, FBID, TS, Deleted, Dirty, LastEdited, Account ON CONFLICT ABORT, Payee, Category, Closed, SrcAccount, Comment"),
            new TableInfo("ref_budget",
                    "CREATE TABLE ref_budget (_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, FBID TEXT, TS INTEGER, Deleted INTEGER, Dirty INTEGER, LastEdited TEXT, Year INTEGER NOT NULL, Month INTEGER NOT NULL, Category INTEGER NOT NULL REFERENCES ref_Categories(_id) ON DELETE CASCADE ON UPDATE CASCADE, Amount REAL NOT NULL, Currency INTEGER NOT NULL REFERENCES ref_Currencies(_id) ON DELETE CASCADE ON UPDATE CASCADE, UNIQUE (Deleted, Year, Month, Category, Currency) ON CONFLICT REPLACE);",
                    "_id, FBID, TS, Deleted, Dirty, LastEdited, Year, Month, Category, Amount, Currency"),
            new TableInfo("ref_budget_debts",
                    "CREATE TABLE ref_budget_debts (_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, FBID TEXT, TS INTEGER, Deleted INTEGER, Dirty INTEGER, LastEdited TEXT, Year INTEGER NOT NULL, Month INTEGER NOT NULL, Debt INTEGER NOT NULL REFERENCES ref_Debts(_id) ON DELETE CASCADE ON UPDATE CASCADE, Amount REAL NOT NULL, UNIQUE (Deleted, Year, Month, Debt) ON CONFLICT REPLACE);",
                    "_id, FBID, TS, Deleted, Dirty, LastEdited, Year, Month, Debt, Amount"),
            new TableInfo("ref_Senders",
                    "CREATE TABLE ref_Senders (_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, FBID TEXT, TS INTEGER, Deleted INTEGER, Dirty INTEGER, LastEdited TEXT, Name TEXT NOT NULL, PhoneNo TEXT NOT NULL, AmountPos INTEGER, BalancePos INTEGER, LeadingCurrencySymbol INTEGER, DateFormat TEXT, isActive INTEGER NOT NULL, UNIQUE (Name, Deleted) ON CONFLICT ABORT, UNIQUE (PhoneNo, Deleted) ON CONFLICT ABORT);",
                    "_id, FBID, TS, Deleted, Dirty, LastEdited, Name, PhoneNo, AmountPos, BalancePos, LeadingCurrencySymbol, DateFormat, isActive")
    };
    //</editor-fold>

    //<editor-fold desc="tableInfo23" defaultstate="collapsed">
    private static TableInfo tableInfo23[] = new TableInfo[]{
            new TableInfo("ref_Currencies",
                    "CREATE TABLE ref_Currencies (_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, FBID TEXT, TS INTEGER, Deleted INTEGER, Dirty INTEGER, LastEdited TEXT, Code TEXT NOT NULL, Symbol TEXT NOT NULL, Name TEXT NOT NULL, DecimalCount INTEGER NOT NULL, OrderNumber INTEGER, UNIQUE (Code, Deleted) ON CONFLICT ABORT);",
                    "_id, FBID, TS, Deleted, Dirty, LastEdited, Code, Symbol, Name, DecimalCount, OrderNumber"),
            new TableInfo("ref_Accounts",
                    "CREATE TABLE ref_Accounts (_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, FBID TEXT, TS INTEGER, Deleted INTEGER, Dirty INTEGER, LastEdited TEXT, Type INTEGER NOT NULL, Name TEXT NOT NULL, Currency INTEGER REFERENCES ref_Currencies(_id) ON DELETE SET NULL ON UPDATE CASCADE, Emitent TEXT, Last4Digits INTEGER, Comment TEXT, StartBalance REAL NOT NULL, IsClosed INTEGER NOT NULL, CustomOrder INTEGER, CreditLimit REAL, UNIQUE (Name, Deleted) ON CONFLICT ABORT);",
                    "_id, FBID, TS, Deleted, Dirty, LastEdited, Type, Name, Currency, Emitent, Last4Digits, Comment, StartBalance, IsClosed, CustomOrder, CreditLimit"),
            new TableInfo("ref_Categories",
                    "CREATE TABLE ref_Categories (_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, FBID TEXT, TS INTEGER, Deleted INTEGER, Dirty INTEGER, LastEdited TEXT, Name TEXT NOT NULL, COLOR TEXT, ParentID INTEGER REFERENCES ref_Categories(_id) ON DELETE SET NULL ON UPDATE CASCADE, OrderNumber INTEGER NOT NULL, Sign INTEGER NOT NULL, UNIQUE (Name, ParentID, Deleted) ON CONFLICT ABORT);",
                    "_id, FBID, TS, Deleted, Dirty, LastEdited, Name, COLOR, ParentID, OrderNumber, Sign"),
            new TableInfo("ref_Payees",
                    "CREATE TABLE ref_Payees (_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, FBID TEXT, TS INTEGER, Deleted INTEGER, Dirty INTEGER, LastEdited TEXT, Name TEXT NOT NULL, DefCategory INTEGER REFERENCES ref_Categories(_id) ON DELETE SET NULL ON UPDATE CASCADE, ParentID INTEGER REFERENCES ref_Payees(_id) ON DELETE SET NULL ON UPDATE CASCADE, OrderNumber INTEGER, UNIQUE (Name, ParentID, Deleted) ON CONFLICT ABORT);",
                    "_id, FBID, TS, Deleted, Dirty, LastEdited, Name, DefCategory, ParentID, OrderNumber"),
            new TableInfo("ref_Projects",
                    "CREATE TABLE ref_Projects (_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, FBID TEXT, TS INTEGER, Deleted INTEGER, Dirty INTEGER, LastEdited TEXT, Name TEXT NOT NULL, isActive INTEGER NOT NULL, ParentID INTEGER REFERENCES ref_Projects(_id) ON DELETE SET NULL ON UPDATE CASCADE, OrderNumber INTEGER, UNIQUE (Name, ParentID, Deleted) ON CONFLICT ABORT);",
                    "_id, FBID, TS, Deleted, Dirty, LastEdited, Name, isActive, ParentID, OrderNumber"),
            new TableInfo("ref_Locations",
                    "CREATE TABLE ref_Locations (_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, FBID TEXT, TS INTEGER, Deleted INTEGER, Dirty INTEGER, LastEdited TEXT, Name TEXT NOT NULL, Lon REAL NOT NULL, Lat REAL NOT NULL, Radius INTEGER, Address TEXT, ParentID INTEGER REFERENCES ref_Locations(_id) ON DELETE SET NULL ON UPDATE CASCADE, OrderNumber INTEGER, UNIQUE (Name, ParentID, Deleted) ON CONFLICT ABORT);",
                    "_id, FBID, TS, Deleted, Dirty, LastEdited, Name, Lon, Lat, Radius, Address, ParentID, OrderNumber"),
            new TableInfo("ref_Departments",
                    "CREATE TABLE ref_Departments (_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, FBID TEXT, TS INTEGER, Deleted INTEGER, Dirty INTEGER, LastEdited TEXT, Name TEXT NOT NULL, isActive INTEGER NOT NULL, ParentID INTEGER REFERENCES ref_Departments(_id) ON DELETE SET NULL ON UPDATE CASCADE, OrderNumber INTEGER, UNIQUE (Name, ParentID, Deleted) ON CONFLICT ABORT);",
                    "_id, FBID, TS, Deleted, Dirty, LastEdited, Name, isActive, ParentID, OrderNumber"),
            new TableInfo("ref_SimpleDebts",
                    "CREATE TABLE ref_SimpleDebts (_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, FBID TEXT, TS INTEGER, Deleted INTEGER, Dirty INTEGER, LastEdited TEXT, Name TEXT NOT NULL, isActive INTEGER NOT NULL, UNIQUE (Name, Deleted) ON CONFLICT ABORT);",
                    "_id, FBID, TS, Deleted, Dirty, LastEdited, Name, isActive"),
            new TableInfo("log_Transactions",
                    "CREATE TABLE log_Transactions (_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, FBID TEXT, TS INTEGER, Deleted INTEGER, Dirty INTEGER, LastEdited TEXT, DateTime INTEGER NOT NULL, SrcAccount INTEGER NOT NULL REFERENCES ref_Accounts(_id) ON DELETE CASCADE ON UPDATE CASCADE, Payee INTEGER REFERENCES ref_Payees(_id) ON DELETE SET NULL ON UPDATE CASCADE, Category INTEGER REFERENCES ref_Categories(_id) ON DELETE SET NULL ON UPDATE CASCADE, Amount REAL NOT NULL, Project INTEGER REFERENCES ref_Projects(_id) ON DELETE SET NULL ON UPDATE CASCADE, SimpleDebt INTEGER REFERENCES ref_SimpleDebts(_id) ON DELETE SET NULL ON UPDATE CASCADE, Department INTEGER REFERENCES ref_Departments(_id) ON DELETE SET NULL ON UPDATE CASCADE, Location INTEGER REFERENCES ref_Locations(_id) ON DELETE SET NULL ON UPDATE CASCADE, Comment TEXT, File TEXT, DestAccount INTEGER NOT NULL REFERENCES ref_Accounts(_id) ON DELETE CASCADE ON UPDATE CASCADE, ExchangeRate REAL NOT NULL, AutoCreated INTEGER NOT NULL, Lon REAL, Lat REAL, Accuracy INTEGER);",
                    "_id, FBID, TS, Deleted, Dirty, LastEdited, DateTime, SrcAccount, Payee, Category, Amount, Project, SimpleDebt, Department, Location, Comment, File, DestAccount, ExchangeRate, AutoCreated, Lon, Lat, Accuracy"),
            new TableInfo("log_Templates",
                    "CREATE TABLE log_Templates (_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, FBID TEXT, TS INTEGER, Deleted INTEGER, Dirty INTEGER, LastEdited TEXT, SrcAccount INTEGER NOT NULL REFERENCES ref_Accounts(_id) ON DELETE CASCADE ON UPDATE CASCADE, Payee INTEGER REFERENCES ref_Payees(_id) ON DELETE SET NULL ON UPDATE CASCADE, Category INTEGER REFERENCES ref_Categories(_id) ON DELETE SET NULL ON UPDATE CASCADE, Amount REAL NOT NULL, Project INTEGER REFERENCES ref_Projects(_id) ON DELETE SET NULL ON UPDATE CASCADE, Department INTEGER REFERENCES ref_Departments(_id) ON DELETE SET NULL ON UPDATE CASCADE, Location INTEGER REFERENCES ref_Locations(_id) ON DELETE SET NULL ON UPDATE CASCADE, Name TEXT NOT NULL, DestAccount INTEGER NOT NULL REFERENCES ref_Accounts(_id) ON DELETE CASCADE ON UPDATE CASCADE, ExchangeRate REAL NOT NULL, Type INTEGER NOT NULL, Comment TEXT);",
                    "_id, FBID, TS, Deleted, Dirty, LastEdited, SrcAccount, Payee, Category, Amount, Project, Department, Location, Name, DestAccount, ExchangeRate, Type, Comment"),
            new TableInfo("log_Incoming_SMS",
                    "CREATE TABLE log_Incoming_SMS (_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, FBID TEXT, TS INTEGER, Deleted INTEGER, Dirty INTEGER, LastEdited TEXT, DateTime TEXT NOT NULL, Sender INTEGER NOT NULL, Body TEXT NOT NULL);",
                    "_id, FBID, TS, Deleted, Dirty, LastEdited, DateTime, Sender, Body"),
            new TableInfo("ref_Sms_Parser_Patterns",
                    "CREATE TABLE ref_Sms_Parser_Patterns (_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, FBID TEXT, TS INTEGER, Deleted INTEGER, Dirty INTEGER, LastEdited TEXT, Type INTEGER NOT NULL, Object TEXT NOT NULL, Pattern TEXT NOT NULL, UNIQUE (Type, Pattern, Deleted) ON CONFLICT REPLACE);",
                    "_id, FBID, TS, Deleted, Dirty, LastEdited, Type, Object, Pattern"),
            new TableInfo("ref_Debts",
                    "CREATE TABLE ref_Debts (_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, FBID TEXT, TS INTEGER, Deleted INTEGER, Dirty INTEGER, LastEdited TEXT, Account INTEGER NOT NULL ON CONFLICT ABORT REFERENCES ref_Accounts(_id) ON DELETE CASCADE ON UPDATE CASCADE, Payee INTEGER NOT NULL REFERENCES ref_Payees(_id) ON DELETE CASCADE ON UPDATE CASCADE, Category INTEGER NOT NULL REFERENCES ref_Categories(_id) ON DELETE SET NULL ON UPDATE CASCADE, Closed INTEGER NOT NULL, SrcAccount INTEGER, Comment TEXT, UNIQUE (Account, Deleted) ON CONFLICT ABORT);",
                    "_id, FBID, TS, Deleted, Dirty, LastEdited, Account ON CONFLICT ABORT, Payee, Category, Closed, SrcAccount, Comment"),
            new TableInfo("ref_budget",
                    "CREATE TABLE ref_budget (_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, FBID TEXT, TS INTEGER, Deleted INTEGER, Dirty INTEGER, LastEdited TEXT, Year INTEGER NOT NULL, Month INTEGER NOT NULL, Category INTEGER NOT NULL REFERENCES ref_Categories(_id) ON DELETE CASCADE ON UPDATE CASCADE, Amount REAL NOT NULL, Currency INTEGER NOT NULL REFERENCES ref_Currencies(_id) ON DELETE CASCADE ON UPDATE CASCADE, UNIQUE (Deleted, Year, Month, Category, Currency) ON CONFLICT REPLACE);",
                    "_id, FBID, TS, Deleted, Dirty, LastEdited, Year, Month, Category, Amount, Currency"),
            new TableInfo("ref_budget_debts",
                    "CREATE TABLE ref_budget_debts (_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, FBID TEXT, TS INTEGER, Deleted INTEGER, Dirty INTEGER, LastEdited TEXT, Year INTEGER NOT NULL, Month INTEGER NOT NULL, Debt INTEGER NOT NULL REFERENCES ref_Debts(_id) ON DELETE CASCADE ON UPDATE CASCADE, Amount REAL NOT NULL, UNIQUE (Deleted, Year, Month, Debt) ON CONFLICT REPLACE);",
                    "_id, FBID, TS, Deleted, Dirty, LastEdited, Year, Month, Debt, Amount"),
            new TableInfo("ref_Senders",
                    "CREATE TABLE ref_Senders (_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, FBID TEXT, TS INTEGER, Deleted INTEGER, Dirty INTEGER, LastEdited TEXT, Name TEXT NOT NULL, PhoneNo TEXT NOT NULL, AmountPos INTEGER, BalancePos INTEGER, LeadingCurrencySymbol INTEGER, DateFormat TEXT, isActive INTEGER NOT NULL, UNIQUE (Name, Deleted) ON CONFLICT ABORT, UNIQUE (PhoneNo, Deleted) ON CONFLICT ABORT);",
                    "_id, FBID, TS, Deleted, Dirty, LastEdited, Name, PhoneNo, AmountPos, BalancePos, LeadingCurrencySymbol, DateFormat, isActive")
    };
    //</editor-fold>

    public static void update17(SQLiteDatabase db, Context context) {
        //создаем таблицу отправителей
        db.execSQL("CREATE TABLE ref_Senders (_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, Name TEXT UNIQUE NOT NULL, PhoneNo TEXT UNIQUE NOT NULL, AmountPos INTEGER, BalancePos INTEGER, LeadingCurrencySymbol INTEGER, DateFormat TEXT, isActive INTEGER NOT NULL);");

        //Импортируем в новую таблицу все маркеры типа "Отправитель"
        Cursor cursor = db.query("ref_Sms_Parser_Patterns", "_id, Type, Object, Pattern".split(","),
                "Type = " + 5, null,
                null, null, null);

        List<Pair<String, Long>> senders = new ArrayList<>();
        String phoneNo;

        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    while (!cursor.isAfterLast()) {
                        ContentValues values = new ContentValues();
                        values.put("Name", cursor.getString(cursor.getColumnIndex("Object")));
                        phoneNo = cursor.getString(cursor.getColumnIndex("Pattern"));
                        values.put("PhoneNo", phoneNo);
                        values.put("AmountPos", 0);
                        values.put("BalcancePos", 1);
                        values.put("DateFormat", "");
                        values.put("LeadingCurrencySymbol", false);
                        values.put("IsActive", 1);

                        try {
                            senders.add(new Pair<>(phoneNo, SendersDAO.getInstance(context).createItem(values, -1, true)));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        cursor.moveToNext();
                    }
                }
            } finally {
                cursor.close();
            }
        }
        //После импорта грохаем все маркеры типа "Отправитель"
        db.delete("ref_Sms_Parser_Patterns", "Type = 5", null);

        //Модифицируем таблицу смс, чтобы она хранила id отправителя вместо номера телефона
        String tempSmsTableSQL = "CREATE TABLE tempsms (_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, DateTime TEXT NOT NULL, Sender INTEGER NOT NULL, Body TEXT NOT NULL);";
        db.execSQL(tempSmsTableSQL);
        cursor = db.query("log_Incoming_SMS", "_id,DateTime,Sender,Body".split(","), null, null, null, null, null);
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    while (!cursor.isAfterLast()) {
                        ContentValues values = new ContentValues();
                        for (Pair<String, Long> sender : senders) {
                            if (sender.first.toLowerCase().equals(cursor.getString(cursor.getColumnIndex("Sender")).toLowerCase())) {
                                values.put("_id", cursor.getLong(cursor.getColumnIndex("_id")));
                                values.put("DateTime", cursor.getString(cursor.getColumnIndex("DateTime")));
                                values.put("Sender", sender.second);
                                values.put("Body", cursor.getString(cursor.getColumnIndex("Body")));
                                db.insert("tempsms", null, values);
                            }
                        }
                        cursor.moveToNext();
                    }
                }
            } finally {
                cursor.close();
            }
        }
        db.execSQL("DROP TABLE log_Incoming_SMS");
        db.execSQL("ALTER TABLE tempsms RENAME TO log_Incoming_SMS");
    }

    public static void update18(SQLiteDatabase db, Context context) {
        //переходим со строкового хранения дат в таблице транзакций на числовое
        //Переименовали старую версию таблицы
        db.execSQL(String.format("ALTER TABLE %s RENAME TO t_transactions_old", "log_Transactions"));

        //Создали новую версию таблицы
        db.execSQL("CREATE TABLE log_Transactions (_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, FBID TEXT, TS INTEGER, Deleted INTEGER, Dirty INTEGER, LastEdited TEXT, DateTime INTEGER NOT NULL, SrcAccount INTEGER NOT NULL REFERENCES ref_Accounts(_id) ON DELETE CASCADE ON UPDATE CASCADE, Payee INTEGER REFERENCES ref_Payees(_id) ON DELETE SET NULL ON UPDATE CASCADE, Category INTEGER REFERENCES ref_Categories(_id) ON DELETE SET NULL ON UPDATE CASCADE, Amount REAL NOT NULL, Project INTEGER REFERENCES ref_Projects(_id) ON DELETE SET NULL ON UPDATE CASCADE, SimpleDebt INTEGER REFERENCES ref_SimpleDebts(_id) ON DELETE SET NULL ON UPDATE CASCADE, Department INTEGER REFERENCES ref_Departments(_id) ON DELETE SET NULL ON UPDATE CASCADE, Location INTEGER REFERENCES ref_Locations(_id) ON DELETE SET NULL ON UPDATE CASCADE, Comment TEXT, File TEXT, DestAccount INTEGER NOT NULL REFERENCES ref_Accounts(_id) ON DELETE CASCADE ON UPDATE CASCADE, ExchangeRate REAL NOT NULL, AutoCreated INTEGER NOT NULL, Lon REAL, Lat REAL, Accuracy INTEGER);");

        Cursor cursor = db.query("t_transactions_old", "_id, DateTime, SrcAccount, Payee, Category, Amount, Project, Location, Comment, File, DestAccount, ExchangeRate, AutoCreated, Lat, Lon, Accuracy, Department, SimpleDebt".split(","), null, null, null, null, null);

        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    DateTimeFormatter dtf = DateTimeFormatter.getInstance(context);
                    ContentValues values = new ContentValues();
                    while (!cursor.isAfterLast()) {
                        values.clear();
                        values.put("DateTime", dtf.parseDateTimeSqlString(cursor.getString(cursor.getColumnIndex("DateTime"))).getTime());
                        values.put("SrcAccount", cursor.getLong(cursor.getColumnIndex("SrcAccount")));
                        values.put("Payee", cursor.getLong(cursor.getColumnIndex("Payee")));
                        values.put("Category", cursor.getLong(cursor.getColumnIndex("Category")));
                        values.put("Amount", cursor.getDouble(cursor.getColumnIndex("Amount")));
                        values.put("Project", cursor.getLong(cursor.getColumnIndex("Project")));
                        values.put("Department", cursor.getLong(cursor.getColumnIndex("Department")));
                        values.put("Location", cursor.getLong(cursor.getColumnIndex("Location")));
                        values.put("Comment", cursor.getString(cursor.getColumnIndex("Comment")));
                        values.put("File", cursor.getString(cursor.getColumnIndex("File")));
                        values.put("DestAccount", cursor.getLong(cursor.getColumnIndex("DestAccount")));
                        values.put("ExchangeRate", cursor.getDouble(cursor.getColumnIndex("ExchangeRate")));
                        values.put("AutoCreated", cursor.getInt(cursor.getColumnIndex("AutoCreated")) == 1);
                        if (!cursor.isNull(cursor.getColumnIndex("Lat"))) {
                            values.put("Lat", cursor.getDouble(cursor.getColumnIndex("Lat")));
                        } else {
                            values.put("Lat", 0);
                        }
                        if (!cursor.isNull(cursor.getColumnIndex("Lon"))) {
                            values.put("Lon", cursor.getDouble(cursor.getColumnIndex("Lon")));
                        } else {
                            values.put("Lon", 0);
                        }
                        if (!cursor.isNull(cursor.getColumnIndex("Accuracy"))) {
                            values.put("Accuracy", cursor.getInt(cursor.getColumnIndex("Accuracy")));
                        } else {
                            values.put("Accuracy", -1);
                        }
                        if (!cursor.isNull(cursor.getColumnIndex("SimpleDebt"))) {
                            values.put("SimpleDebt", cursor.getLong(cursor.getColumnIndex("SimpleDebt")));
                        } else {
                            values.put("SimpleDebt", -1);
                        }

                        db.insert("log_Transactions", null, values);

                        cursor.moveToNext();
                    }
                }
            } finally {
                cursor.close();
            }
        }
        db.execSQL("DROP TABLE t_transactions_old");

        //создаем в таблицах поля, необходимые для работы синхронизации и меняем constraints (пересоздаем все таблицы)
        ContentValues cv = new ContentValues();
        cv.put("FBID", "");
        cv.put("TS", -1);
        cv.put("Deleted", 0);
        cv.put("Dirty", false);
        cv.put("LastEdited", "");
        for (int i = 0; i < tableInfo17.length; i++) {
            db.execSQL(String.format("ALTER TABLE %s RENAME TO %s_old", tableInfo17[i].getTableName(), tableInfo17[i].getTableName()));
            db.execSQL(tableInfo18[i].getTableCreateSQL());
            db.execSQL(String.format("INSERT INTO %s (%s) SELECT %s FROM %s_old", tableInfo18[i].getTableName(), tableInfo17[i].getTableFields(), tableInfo17[i].getTableFields(), tableInfo17[i].getTableName()));
            db.execSQL(String.format("DROP TABLE %s_old", tableInfo17[i].getTableName()));
            db.update(tableInfo18[i].getTableName(), cv, null, null);
        }
    }

    public static void update19(SQLiteDatabase db) {
        db.execSQL("ALTER TABLE ref_Senders RENAME TO ref_Senders_old");
        db.execSQL("CREATE TABLE ref_Senders (_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, FBID TEXT, TS INTEGER, Deleted INTEGER, Dirty INTEGER, LastEdited TEXT, Name TEXT NOT NULL, PhoneNo TEXT NOT NULL, AmountPos INTEGER, BalancePos INTEGER, LeadingCurrencySymbol INTEGER, DateFormat TEXT, isActive INTEGER NOT NULL, UNIQUE (Name, Deleted) ON CONFLICT ABORT, UNIQUE (PhoneNo, Deleted) ON CONFLICT ABORT);");
        db.execSQL(
                "INSERT INTO ref_Senders (_id, FBID, TS, Deleted, Dirty, LastEdited, Name, PhoneNo, AmountPos, BalancePos, LeadingCurrencySymbol, DateFormat, isActive) " +
                        "SELECT _id, FBID, TS, Deleted, Dirty, LastEdited, Name, PhoneNo, AmountPos, BalancePos, LeadingCurrencySymbol, DateFormat, isActive FROM ref_Senders_old");
        db.execSQL("DROP TABLE ref_Senders_old");
    }

    public static void update20(SQLiteDatabase db) {
        db.execSQL("ALTER TABLE log_Templates ADD COLUMN Comment TEXT;");
    }

    public static void update21(SQLiteDatabase db) {
        ContentValues cv = new ContentValues();
        cv.put("Comment", "");
        db.update("log_Templates", cv, "Comment IS NULL", null);
    }

    public static void update22(SQLiteDatabase db) {
        /*
          План:
          1. Меняем в таблице ref_Categories название поля ParentCategory на ParentID
          2. Заводим в таблицах ref_Payees, ref Projects, ref_Departments, ref_Locations поля
               ParentID и OrderNumber
         */
        db.execSQL("ALTER TABLE ref_Categories RENAME TO ref_Categories_old");
        db.execSQL("CREATE TABLE ref_Categories (_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, FBID TEXT, TS INTEGER, Deleted INTEGER, Dirty INTEGER, LastEdited TEXT, Name TEXT NOT NULL, COLOR TEXT, ParentID INTEGER REFERENCES ref_Categories(_id) ON DELETE SET NULL ON UPDATE CASCADE, OrderNumber INTEGER NOT NULL, Sign INTEGER NOT NULL, UNIQUE (Name, ParentID, Deleted) ON CONFLICT ABORT);");
        String oldColumns = "_id,FBID,TS,Deleted,Dirty,LastEdited,Name,COLOR,ParentCategory,OrderNumber,Sign";
        String newColumns = "_id,FBID,TS,Deleted,Dirty,LastEdited,Name,COLOR,ParentID,OrderNumber,Sign";
        db.execSQL(String.format("INSERT INTO ref_Categories (%s) SELECT %s FROM ref_Categories_old", newColumns, oldColumns));
        db.execSQL("DROP TABLE ref_Categories_old");

        List<TableInfo> infos = new ArrayList<>();
        String tableNames[] = new String[]{"ref_Payees", "ref_Projects", "ref_Locations", "ref_Departments"};
        String tableCreateSQL[] = new String[]{
                "CREATE TABLE ref_Payees (_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, FBID TEXT, TS INTEGER, Deleted INTEGER, Dirty INTEGER, LastEdited TEXT, Name TEXT NOT NULL, DefCategory INTEGER REFERENCES ref_Categories(_id) ON DELETE SET NULL ON UPDATE CASCADE, ParentID INTEGER REFERENCES ref_Payees(_id) ON DELETE SET NULL ON UPDATE CASCADE, OrderNumber INTEGER, UNIQUE (Name, ParentID, Deleted) ON CONFLICT ABORT);",
                "CREATE TABLE ref_Projects (_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, FBID TEXT, TS INTEGER, Deleted INTEGER, Dirty INTEGER, LastEdited TEXT, Name TEXT NOT NULL, isActive INTEGER NOT NULL, ParentID INTEGER REFERENCES ref_Projects(_id) ON DELETE SET NULL ON UPDATE CASCADE, OrderNumber INTEGER, UNIQUE (Name, ParentID, Deleted) ON CONFLICT ABORT);",
                "CREATE TABLE ref_Locations (_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, FBID TEXT, TS INTEGER, Deleted INTEGER, Dirty INTEGER, LastEdited TEXT, Name TEXT NOT NULL, Lon REAL NOT NULL, Lat REAL NOT NULL, Radius INTEGER, Address TEXT, ParentID INTEGER REFERENCES ref_Locations(_id) ON DELETE SET NULL ON UPDATE CASCADE, OrderNumber INTEGER, UNIQUE (Name, ParentID, Deleted) ON CONFLICT ABORT);",
                "CREATE TABLE ref_Departments (_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, FBID TEXT, TS INTEGER, Deleted INTEGER, Dirty INTEGER, LastEdited TEXT, Name TEXT NOT NULL, isActive INTEGER NOT NULL, ParentID INTEGER REFERENCES ref_Departments(_id) ON DELETE SET NULL ON UPDATE CASCADE, OrderNumber INTEGER, UNIQUE (Name, ParentID, Deleted) ON CONFLICT ABORT);"
        };
        String tableFields[] = new String[]{
                "_id, FBID, TS, Deleted, Dirty, LastEdited, Name, DefCategory",
                "_id, FBID, TS, Deleted, Dirty, LastEdited, Name, isActive",
                "_id, FBID, TS, Deleted, Dirty, LastEdited, Name, Lon, Lat, Radius, Address",
                "_id, FBID, TS, Deleted, Dirty, LastEdited, Name, isActive"
        };

        ContentValues cv = new ContentValues();
        cv.put("ParentID", -1);
        cv.put("OrderNumber", 0);

        for (int i = 0; i < tableNames.length; i++) {
            db.execSQL(String.format("ALTER TABLE %s RENAME TO %s_old", tableNames[i], tableNames[i]));
            db.execSQL(tableCreateSQL[i]);
            db.execSQL(String.format("INSERT INTO %s (%s) SELECT %s FROM %s_old", tableNames[i], tableFields[i], tableFields[i], tableNames[i]));
            db.execSQL(String.format("DROP TABLE %s_old", tableNames[i]));
            db.update(tableNames[i], cv, null, null);
        }

        cv = new ContentValues();
        cv.put("Project", -1);
        db.update("log_Transactions", cv, String.format("%s = 0", "Project"), null);
        cv = new ContentValues();
        cv.put("Department", -1);
        db.update("log_Transactions", cv, String.format("%s = 0", "Department"), null);
        cv = new ContentValues();
        cv.put("Location", -1);
        db.update("log_Transactions", cv, String.format("%s = 0", "Location"), null);
    }

    public static void update23(SQLiteDatabase db) {
        ContentValues cv = new ContentValues();
        cv.put("OrderNumber", 0);
        db.execSQL("ALTER TABLE ref_Currencies RENAME TO ref_Currencies_old");
        db.execSQL("CREATE TABLE ref_Currencies (_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, FBID TEXT, TS INTEGER, Deleted INTEGER, Dirty INTEGER, LastEdited TEXT, Code TEXT NOT NULL, Symbol TEXT NOT NULL, Name TEXT NOT NULL, DecimalCount INTEGER NOT NULL, OrderNumber INTEGER, UNIQUE (Code, Deleted) ON CONFLICT ABORT);");
        db.execSQL(
                "INSERT INTO ref_Currencies (_id, FBID, TS, Deleted, Dirty, LastEdited, Code, Symbol, Name, DecimalCount) " +
                "SELECT _id, FBID, TS, Deleted, Dirty, LastEdited, Code, Symbol, Name, DecimalCount " +
                "FROM ref_Currencies_old");
        db.execSQL("DROP TABLE ref_Currencies_old");
        db.update("ref_Currencies", cv, null, null);
    }

    public static void update24(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE ref_Accounts_Sets (_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, FBID TEXT, TS INTEGER, Deleted INTEGER, Dirty INTEGER, LastEdited TEXT, Name TEXT, UNIQUE (Name, Deleted) ON CONFLICT ABORT);");
        db.execSQL("CREATE TABLE log_Accounts_Sets (_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, FBID TEXT, TS INTEGER, Deleted INTEGER, Dirty INTEGER, LastEdited TEXT, SetID INTEGER NOT NULL ON CONFLICT ABORT REFERENCES [ref_Accounts_Sets]([_id]) ON DELETE CASCADE ON UPDATE CASCADE,AccountID INTEGER NOT NULL ON CONFLICT ABORT REFERENCES [ref_Accounts]([_id]) ON DELETE CASCADE ON UPDATE CASCADE,UNIQUE (SetID, AccountID, Deleted) ON CONFLICT ABORT);");

        ContentValues values = new ContentValues();

        values.put("Lat", 0);
        db.update("log_Transactions", values, "Lat IS NULL", null);

        values.clear();
        values.put("Lon", 0);
        db.update("log_Transactions", values, "Lon IS NULL", null);

        values.clear();
        values.put("Accuracy", -1);
        db.update("log_Transactions", values, "Accuracy IS NULL", null);

        values.clear();
        values.put("SimpleDebt", -1);
        db.update("log_Transactions", values, "SimpleDebt IS NULL", null);

        values.clear();
        values.put("ParentID", -1);
        db.update("ref_Categories", values, "ParentID IS NULL", null);

        db.execSQL("CREATE INDEX [idx] ON [log_Transactions] ([Deleted], [DateTime], [SrcAccount], [DestAccount], [Payee], [Category], [Project], [Department], [Location], [SimpleDebt]);");
    }

    public static void update25(SQLiteDatabase db) {
        //добавляем колонку со стартовой суммой долга
        db.execSQL("ALTER TABLE ref_SimpleDebts ADD COLUMN StartAmount REAL NOT NULL DEFAULT 0;");
        //добавляем колонку с валютой долга
        db.execSQL("ALTER TABLE ref_SimpleDebts ADD COLUMN Currency INTEGER REFERENCES ref_Currencies(_id) ON DELETE SET NULL ON UPDATE CASCADE;");
        //считываем идентификаторы всех долгов
        Cursor cursorDebts = db.rawQuery("SELECT * FROM ref_SimpleDebts", null);
        List<Long> debtIDs = new ArrayList<>();
        if (cursorDebts != null) {
            try {
                if (cursorDebts.moveToFirst()) {
                    while (!cursorDebts.isAfterLast()) {
                        debtIDs.add(cursorDebts.getLong(cursorDebts.getColumnIndex("_id")));
                        cursorDebts.moveToNext();
                    }
                }
            } finally {
                cursorDebts.close();
            }
        }
        //перебираем долги и проставляем валюту, как первую попавшуюся валюту транзакции по этому долгу
        Cursor cursorTransactions;
        Cursor cursorAccounts;
        boolean cabbageFound;
        long accountID;
        long cabbageID;
        for (long debtID : debtIDs) {
            cabbageFound = false;
            cursorTransactions = db.query("log_Transactions", new String[]{"SrcAccount"},
                    "SimpleDebt = " + String.valueOf(debtID), null, null, null, null);
            if (cursorTransactions != null) {
                try {
                    if (cursorTransactions.moveToFirst()) {
                        accountID = cursorTransactions.getLong(0);
                        cursorAccounts = db.query("ref_Accounts", new String[]{"Currency"},
                                "_id = " + String.valueOf(accountID), null, null, null, null);
                        if (cursorAccounts != null) {
                            try {
                                if (cursorAccounts.moveToFirst()) {
                                    cabbageID = cursorAccounts.getLong(0);
                                    cabbageFound = true;
                                    db.execSQL(String.format("UPDATE ref_SimpleDebts SET Currency = %s WHERE _id = %s",
                                            String.valueOf(cabbageID), String.valueOf(debtID)));
                                }
                            } finally {
                                cursorAccounts.close();
                            }
                        }
                    }
                } finally {
                    cursorTransactions.close();
                }
            }
            if (!cabbageFound) {
                db.delete("ref_SimpleDebts", String.format("_id = %s", String.valueOf(debtID)), null);
            }
        }
    }

    public static void update26(SQLiteDatabase db) {
        Cursor cursorIndexes = db.rawQuery("PRAGMA index_list(log_Transactions)", null);
        if (cursorIndexes.getCount() == 0) {
            db.execSQL("CREATE INDEX [idx] ON [log_Transactions] ([Deleted], [DateTime], [SrcAccount], [DestAccount], [Payee], [Category], [Project], [Department], [Location], [SimpleDebt]);");
        }
        cursorIndexes.close();
    }

    public static void update27(SQLiteDatabase db, IUpdateRunningBalance updateRB) {
        String tableNames[] = new String[]{"ref_Categories", "ref_Payees", "ref_Projects", "ref_Locations", "ref_Departments"};
        for (String tableName : tableNames) {
            db.execSQL("ALTER TABLE " + tableName + " ADD COLUMN FullName TEXT;");
            db.execSQL("ALTER TABLE " + tableName + " ADD COLUMN SearchString TEXT;");
            DBHelper.updateFullNames(tableName, true, db);
        }
        tableNames = new String[]{"ref_Accounts", "ref_SimpleDebts", "log_Templates"};
        for (String tableName : tableNames) {
            db.execSQL("ALTER TABLE " + tableName + " ADD COLUMN SearchString TEXT;");
            DBHelper.updateFullNames(tableName, false, db);
        }

        //Создаем таблицу с текущим балансом и вычисляем балансы счетов после каждой транзакции
        db.execSQL("CREATE TABLE log_Running_Balance (\n" +
                "     AccountID INTEGER NOT NULL,\n" +
                "     TransactionID INTEGER NOT NULL,\n" +
                "     DateTimeRB INTEGER NOT NULL,\n" +
                "     Income REAL NOT NULL,\n" +
                "     Expense REAL NOT NULL,\n" +
                "     PRIMARY KEY (AccountID, TransactionID) );");
        db.execSQL("CREATE INDEX idx_RB_Accounts ON log_Running_Balance (AccountID);");
        db.execSQL("CREATE INDEX idx_RB_Transactions ON log_Running_Balance (TransactionID);");
        db.execSQL("CREATE INDEX idx_RB_DateTime ON log_Running_Balance (DateTimeRB);");
        try {
            updateRB.updateRunningBalance(db);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void update29(SQLiteDatabase db) {
        String tableName = DBHelper.T_LOG_TRANSACTIONS;
        String columnName = DBHelper.C_LOG_TRANSACTIONS_COMMENT;
        db.execSQL("ALTER TABLE " + tableName + " ADD COLUMN SearchString TEXT;");
        db.execSQL("ALTER TABLE " + DBHelper.T_REF_SENDERS + " ADD COLUMN "+DBHelper.C_REF_SENDERS_ADD_CREDIT_LIMIT_TO_BALANCE+" INTEGER NOT NULL DEFAULT 0;");

        Cursor cursor = db.query(tableName, new String[]{DBHelper.C_ID, columnName}, "Deleted = 0", null, null, null, null);
        ContentValues cv = new ContentValues();
        try {
            if (cursor.moveToFirst()) {
                while (!cursor.isAfterLast()) {
                    cv.clear();
                    cv.put("SearchString", Translit.toTranslit(cursor.getString(1).toLowerCase()));
                    db.update(tableName, cv, "_id = " + cursor.getString(0), null);
                    cursor.moveToNext();
                }
            }
        } finally {
            cursor.close();
        }
    }

    public static void update30(SQLiteDatabase db) {
        db.execSQL("ALTER TABLE " + DBHelper.T_LOG_TRANSACTIONS + " ADD COLUMN "+DBHelper.C_LOG_TRANSACTIONS_FN+" INTEGER DEFAULT 0;");
        db.execSQL("ALTER TABLE " + DBHelper.T_LOG_TRANSACTIONS + " ADD COLUMN "+DBHelper.C_LOG_TRANSACTIONS_FD+" INTEGER DEFAULT 0;");
        db.execSQL("ALTER TABLE " + DBHelper.T_LOG_TRANSACTIONS + " ADD COLUMN "+DBHelper.C_LOG_TRANSACTIONS_FP+" INTEGER DEFAULT 0;");
    }

    public static void update31(SQLiteDatabase db, Context context) {
        db.execSQL("ALTER TABLE " + DBHelper.T_REF_PROJECTS + " ADD COLUMN "+DBHelper.C_REF_PROJECTS_COLOR+" TEXT DEFAULT '#ffffff';");

        Cursor cursorProjects = db.rawQuery("SELECT _id FROM ref_Projects", null);
        List<Long> projectIDs = new ArrayList<>();
        if (cursorProjects != null) {
            try {
                if (cursorProjects.moveToFirst()) {
                    while (!cursorProjects.isAfterLast()) {
                        projectIDs.add(cursorProjects.getLong(0));
                        cursorProjects.moveToNext();
                    }
                }
            } finally {
                cursorProjects.close();
            }
        }

        ContentValues cv = new ContentValues();
        for (long id : projectIDs) {
            cv.clear();
            cv.put(DBHelper.C_REF_PROJECTS_COLOR, String.format("#%06X", (0xFFFFFF & ColorUtils.getColor(context))));
            db.update(DBHelper.T_REF_PROJECTS, cv, String.format("_id = %s", String.valueOf(id)), null);
        }
    }

    public static void update32(SQLiteDatabase db) {
        db.execSQL("ALTER TABLE " + DBHelper.T_LOG_TRANSACTIONS + " ADD COLUMN "+DBHelper.C_LOG_TRANSACTIONS_SPLIT+" INTEGER DEFAULT 0;");

        db.execSQL(DBHelper.SQL_CREATE_TABLE_REF_PRODUCTS);
        db.execSQL(DBHelper.SQL_CREATE_TABLE_LOG_PRODUCTS);

        ContentValues cv = new ContentValues();
        cv.put(DBHelper.C_ID, 0);
        cv.put(DBHelper.C_REF_PRODUCTS_NAME, "default_product");
        db.insert(DBHelper.T_REF_PRODUCTS, "", cv);

        Cursor cursorTransactions = db.rawQuery("SELECT _id, Amount FROM log_Transactions WHERE Deleted = 0", null);
        if (cursorTransactions != null) {
            try {
                if (cursorTransactions.moveToFirst()) {
                    while (!cursorTransactions.isAfterLast()) {
                        cv.clear();
                        cv.put(DBHelper.C_SYNC_FBID, "");
                        cv.put(DBHelper.C_SYNC_TS, -1);
                        cv.put(DBHelper.C_SYNC_DELETED, 0);
                        cv.put(DBHelper.C_SYNC_DIRTY, 0);
                        cv.put(DBHelper.C_SYNC_LASTEDITED, "");
                        cv.put(DBHelper.C_LOG_PRODUCTS_TRANSACTIONID, cursorTransactions.getLong(0));
                        cv.put(DBHelper.C_LOG_PRODUCTS_PRODUCTID, 0);
                        cv.put(DBHelper.C_LOG_PRODUCTS_CATEGORY_ID, -1);
                        cv.put(DBHelper.C_LOG_PRODUCTS_PROJECT_ID, -1);
                        cv.put(DBHelper.C_LOG_PRODUCTS_PRICE, cursorTransactions.getDouble(1));
                        cv.put(DBHelper.C_LOG_PRODUCTS_QUANTITY, 1);
                        db.insert(DBHelper.T_LOG_PRODUCTS, null, cv);
                        cursorTransactions.moveToNext();
                    }
                }
            } finally {
                cursorTransactions.close();
            }
        }
        db.execSQL("CREATE INDEX [idx_Products] ON [log_Products] ([Deleted], [TransactionID], [ProductID]);");
    }

    public static void update33(SQLiteDatabase db) {
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

}
