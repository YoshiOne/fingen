package com.yoshione.fingen;

/**
 * Created by Leonid on 23.11.2016.
 * Константы на все случаи жизни
 */
public class FgConst {

    //<editor-fold Идентификаторы фрагментов на главном экране
    public static final String FRAGMENT_SUMMARY = "summary";
    public static final String FRAGMENT_ACCOUNTS = "accounts";
    public static final String FRAGMENT_TRANSACTIONS = "transactions";
    //</editor-fold>

    //<editor-fold Preference keys
    public static final String PREF_CURRENT_ACCOUNT_SET = "current_account_set";
    public static final String PREF_FORCE_UPDATE_ACCOUNTS = "force_update_accounts";
    public static final String PREF_FORCE_UPDATE_SUMMARY = "force_update_summary";
    public static final String PREF_FORCE_UPDATE_TRANSACTIONS = "force_update_transaction";
    public static final String PREF_SHOW_CLOSED_ACCOUNTS = "show_closed_accounts";
    public static final String PREF_SHOW_LAST_SUCCESFUL_BACKUP_TO_DROPBOX = "last_succesful_backup_to_dropbox";
//    public static final String PREF_SHOW_SHOW_ACCOUNTS_PANEL = "show_accounts_panel";
    public static final String PREF_START_TAB = "start_tab";
    public static final String PREF_SWITCH_TAB_ON_START = "switch_tab_on_start";
    public static final String PREF_TAB_ORDER = "main_screen_tab_order";
    public static final String PREF_DROPBOX_ACCOUNT = "dropbox_account";
    public static final String PREF_EXPAND_LISTS = "expand_lists";
    public static final String PREF_LAST_BUDGET_CURRENCY = "last_budget_currency";
    public static final String PREF_ACCOUNT_CLICK_ACTION = "account_click_action";
    public static final String PREF_SYNC_REMOTE_ACCOUNT = "sync_remote_account";
    public static final String PREF_SHOW_TRANSACTION_TYPE_TITLES = "show_transaction_type_titles";
    public static final String PREF_HIDE_SUMS_PANEL = "hide_sums_panel";
    public static final String PREF_DEF_DATE_RANGE = "def_date_range";
    public static final String PREF_DEF_DATE_MODIFIER = "def_date_modifier";
    public static final String PREF_SHOW_PIE_LINES = "pref_show_pie_lines";
    public static final String PREF_SHOW_PIE_PERCENTS = "pref_show_pie_percents";
    public static final String PREF_SHRINK_CHART_LABELS = "pref_shrink_chart_labels";
    public static final String PREF_COMPACT_VIEW_MODE = "compact_view_mode";
    public static final String PREF_SHOW_CLOSED_DEBTS = "show_closed_debts";
    public static final String PREF_DEFAULT_DEPARTMENT = "default_department";
    public static final String PREF_RESET_DEFAULT_DEPARTMENT = "reset_default_department";
    public static final String PREF_ENABLE_SCAN_QR = "enable_scan_qr";
    public static final String PREF_FTS_LOGIN = "fts_login";
    public static final String PREF_FTS_PASS = "fts_pass";
    public static final String PREF_FTS_DO_NOT_SHOW_AGAIN = "fts_do_not_show_again";
    public static final String PREF_FTS_CREDENTIALS = "fts_credentials";
    public static final String PREF_SHOW_INCOME_EXPENSE_FOR_ACCOUNTS = "show_income_expense_for_accounts";
    //</editor-fold>

    //<editor-fold Intent actions
    public static final String ACT_NEW_EXPENSE = "com.yoshione.fingen.ACT_NEW_EXPENSE";
    public static final String ACT_NEW_INCOME = "com.yoshione.fingen.ACT_NEW_INCOME";
    public static final String ACT_NEW_TRANSFER = "com.yoshione.fingen.ACT_NEW_TRANSFER";
    public static final String ACT_SHOW_TEMPLATES = "com.yoshione.fingen.ACT_SHOW_TEMPLATES";
    public static final String ACT_CLEAR_NEW_TRANSACTIONS_COUNTER = "com.yoshione.fingen.intent.action.CLEAR_NEW_TRANSACTION_COUNTER";
    public static final String ACT_CALC_DONE = "com.yoshione.fingen.intent.action.ACT_CALC_DONE";
    //</editor-fold>

    //<editor-fold desc="Intent params">
    public static final String HIDE_FAB = "hide_fab";
    public static final String LOCK_SLIDINGUP_PANEL = "lock_slidingup_paanel";
    public static final String SELECTED_TRANSACTIONS_IDS = "selected_transactions_ids";
    //</editor-fold>


    //<editor-fold Firebase
    public static final String FB_NODE_DB = "databases";
    public static final String FB_NODE_DB_REFS = "db_references";
    public static final String FB_NODE_PERMISSIONS = "permissions";
    //</editor-fold>


}
