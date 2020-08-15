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
    public static final String FRAGMENT_DEBTS = "loans";
    //</editor-fold>

    public static final int NUMBER_ITEMS_TO_BE_LOADED = 25;

    public static final String TEI_DATETIME = "datetime";
    public static final String TEI_ACCOUNT = "account";
    public static final String TEI_PAYEE_DEST_ACC = "payee_dest_acc";
    public static final String TEI_CATEGORY = "category";
    public static final String TEI_AMOUNTS = "amounts";
    public static final String TEI_SMS = "sms";
    public static final String TEI_FTS = "fts";
    public static final String TEI_PRODUCT_LIST = "product_list";
    public static final String TEI_PROJECT = "project";
    public static final String TEI_SIMPLE_DEBT = "simple_debt";
    public static final String TEI_DEPARTMENT = "department";
    public static final String TEI_LOCATION = "location";
    public static final String TEI_COMMENT = "comment";

    //<editor-fold Preference keys
    public static final String PREF_CURRENT_ACCOUNT_SET = "current_account_set";
    public static final String PREF_FORCE_UPDATE_ACCOUNTS = "force_update_accounts";
    public static final String PREF_FORCE_UPDATE_SUMMARY = "force_update_summary";
    public static final String PREF_FORCE_UPDATE_TRANSACTIONS = "force_update_transaction";
    public static final String PREF_FORCE_UPDATE_DEBTS = "force_update_debts";
    public static final String PREF_SHOW_CLOSED_ACCOUNTS = "show_closed_accounts";
    public static final String PREF_SHOW_CLOSED_ACCOUNT_TRANSACTIONS = "show_closed_account_transactions";
    public static final String PREF_SHOW_LAST_SUCCESFUL_BACKUP_TO_DROPBOX = "last_succesful_backup_to_dropbox";
//    public static final String PREF_SHOW_SHOW_ACCOUNTS_PANEL = "show_accounts_panel";
    public static final String PREF_START_TAB = "start_tab";
    public static final String PREF_SWITCH_TAB_ON_START = "switch_tab_on_start";
    public static final String PREF_TAB_ORDER = "main_screen_tab_order";
    public static final String PREF_TRANSACTION_EDITOR_CONSTRUCTOR = "transaction_editor_constructor";
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
    public static final String PREF_SCAN_QR_ENABLED = "scan_qr_enabled";
    public static final String PREF_SCAN_QR_AUTO_FOCUS = "scan_qr_auto_focus";
    public static final String PREF_FTS_CLIENT_SECRET = "fts_client_secret";
    public static final String PREF_FTS_REFRESH_TOKEN = "fts_refresh_token";
    public static final String PREF_FTS_SESSION_ID = "fts_session_id";
    public static final String PREF_FTS_LOGIN = "fts_login";
    public static final String PREF_FTS_PASS = "fts_pass";
    public static final String PREF_FTS_EMAIL = "fts_email";
    public static final String PREF_FTS_NAME = "fts_name";
    public static final String PREF_FTS_ENABLED = "fts_enabled";
    public static final String PREF_FTS_CREDENTIALS = "fts_credentials";
    public static final String PREF_SHOW_INCOME_EXPENSE_FOR_ACCOUNTS = "show_income_expense_for_accounts";
    public static final String PREF_FIRST_DAY_OF_WEEK = "first_day_of_week";
    public static final String PREF_VALUE_MONDAY = "monday";
    public static final String PREF_VALUE_SUNDAY = "sunday";
    public static final String PREF_PIN_LENGTH = "pin_length";
    public static final String PREF_PIN_LOCK_ENABLE = "enable_pin_lock";
    public static final String PREF_PIN_LOCK_TIMEOUT = "pin_lock_timeout";
    public static final String PREF_NEW_ACCOUNT_BUTTON_COUNTER = "new_account_button_counter";
    public static final String PREF_COLORED_TAGS = "colored_tags";
    public static final String PREF_REMEMBER_LAST_ACCOUNT = "remember_last_account";
    public static final String PREF_VERSION_NEXT_CHECK = "version_next_check";
    public static final String PREF_VERSION_X_CHECK = "version_x_check";
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
