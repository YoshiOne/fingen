package com.yoshione.fingen.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.yoshione.fingen.FgConst;
import com.yoshione.fingen.R;
import com.yoshione.fingen.dao.DepartmentsDAO;
import com.yoshione.fingen.model.Department;
import com.yoshione.fingen.model.TabItem;
import com.yoshione.fingen.model.TrEditItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by slv on 26.11.2016.
 */

public class PrefUtils {
    public static TrEditItem getTrEditItemByID(List<TrEditItem> items, String id) {
        for (TrEditItem item : items) {
            if (id.equals(item.getID())) {
                return item;
            }
        }
        return null;
    }

    public static TabItem getTabItemByID(List<TabItem> items, String id) {
        for (TabItem item : items) {
            if (id.equals(item.getID())) {
                return item;
            }
        }
        return null;
    }

    public static List<TrEditItem> getTrEditorLayout(SharedPreferences preferences, Context context) {;
        String order = preferences.getString(FgConst.PREF_TRANSACTION_EDITOR_CONSTRUCTOR, "");
        String items[] = order.split(";");
        List<TrEditItem> list = new ArrayList<>();
        String params[];
        String id;
        String name;
        boolean visible;
        boolean hideUnderMore;
        boolean lockVisible;
        boolean lockHide;
        try {
            for (String item : items) {
                params = item.split("&");
                id = params[0];
                visible = Boolean.valueOf(params[1]);
                hideUnderMore = Boolean.valueOf(params[2]);
                lockVisible = false;
                lockHide = false;
                switch (id) {
                    case FgConst.TEI_DATETIME :
                        name = context.getString(R.string.ent_date_time);
                        lockVisible = true;
                        lockHide = true;
                        break;
                    case FgConst.TEI_ACCOUNT :
                        name = context.getString(R.string.ent_account);
                        lockVisible = true;
                        lockHide = true;
                        break;
                    case FgConst.TEI_PAYEE_DEST_ACC :
                        name = context.getString(R.string.ent_payee_dest_account);
                        lockVisible = true;
                        lockHide = true;
                        break;
                    case FgConst.TEI_CATEGORY :
                        name = context.getString(R.string.ent_category);
                        break;
                    case FgConst.TEI_AMOUNTS :
                        name = context.getString(R.string.ent_amount);
                        lockVisible = true;
                        lockHide = true;
                        break;
                    case FgConst.TEI_SMS :
                        name = context.getString(R.string.ent_sms_body);
                        lockVisible = true;
                        lockHide = true;
                        break;
                    case FgConst.TEI_FTS :
                        name = context.getString(R.string.ttl_scan_qr);
                        break;
                    case FgConst.TEI_PRODUCT_LIST :
                        name = context.getString(R.string.ent_products);
                        break;
                    case FgConst.TEI_PROJECT :
                        name = context.getString(R.string.ent_project);
                        break;
                    case FgConst.TEI_SIMPLE_DEBT :
                        name = context.getString(R.string.ent_debt);
                        break;
                    case FgConst.TEI_DEPARTMENT :
                        name = context.getString(R.string.ent_department);
                        break;
                    case FgConst.TEI_LOCATION :
                        name = context.getString(R.string.ent_location);
                        break;
                    case FgConst.TEI_COMMENT :
                        name = context.getString(R.string.ent_comment);
                        break;
                    default:
                        throw new Exception();
                }
                list.add(new TrEditItem(id, name, visible, hideUnderMore, lockVisible, lockHide));
            }

        } catch (Exception e) {
            list.add(new TrEditItem(FgConst.TEI_DATETIME, context.getString(R.string.ent_date_time),        true, false, true, true));
            list.add(new TrEditItem(FgConst.TEI_ACCOUNT, context.getString(R.string.ent_account),           true, false, true, true));
            list.add(new TrEditItem(FgConst.TEI_PAYEE_DEST_ACC, context.getString(R.string.ent_payee_dest_account), true, false, true, true));
            list.add(new TrEditItem(FgConst.TEI_CATEGORY, context.getString(R.string.ent_category),         true, false, false, false));
            list.add(new TrEditItem(FgConst.TEI_AMOUNTS, context.getString(R.string.ent_amount),            true, false, true, true));
            list.add(new TrEditItem(FgConst.TEI_SMS, context.getString(R.string.ent_sms_body),              true, false, true, true));
            list.add(new TrEditItem(FgConst.TEI_FTS, context.getString(R.string.ttl_scan_qr),               true, true, false, false));
            list.add(new TrEditItem(FgConst.TEI_PRODUCT_LIST, context.getString(R.string.ent_products),     true, true, false, false));
            list.add(new TrEditItem(FgConst.TEI_PROJECT, context.getString(R.string.ent_project),           true, true, false, false));
            list.add(new TrEditItem(FgConst.TEI_SIMPLE_DEBT, context.getString(R.string.ent_debt),          true, true, false, false));
            list.add(new TrEditItem(FgConst.TEI_DEPARTMENT, context.getString(R.string.ent_department),     true, true, false, false));
            list.add(new TrEditItem(FgConst.TEI_LOCATION, context.getString(R.string.ent_location),         true, true, false, false));
            list.add(new TrEditItem(FgConst.TEI_COMMENT, context.getString(R.string.ent_comment),           true, false, false, false));
        }
        return list;
    }

    public static List<TabItem> getTabsOrder(SharedPreferences preferences, Context context) {
        String order = preferences.getString(FgConst.PREF_TAB_ORDER, "");
        String items[] = order.split(";");
        List<TabItem> list = new ArrayList<>();
        String params[];
        String id;
        String name;
        boolean visible;
        boolean lockVisible;
        boolean lockHide;
        try {
            for (String item : items) {
                params = item.split("&");
                id = params[0];
                visible = Boolean.valueOf(params[1]);
                lockVisible = false;
                lockHide = false;
                switch (id) {
                    case FgConst.FRAGMENT_ACCOUNTS :
                        name = context.getString(R.string.ent_accounts);
                        lockVisible = true;
                        lockHide = true;
                        break;
                    case FgConst.FRAGMENT_TRANSACTIONS :
                        name = context.getString(R.string.ent_transactions);
                        lockVisible = true;
                        lockHide = true;
                        break;
                    case FgConst.FRAGMENT_SUMMARY :
                        name = context.getString(R.string.ent_summary);
                        break;
                    case FgConst.FRAGMENT_DEBTS :
                        name = context.getString(R.string.ent_debts);
                        break;
                    default:
                        throw new Exception();
                }
                list.add(new TabItem(id, name, visible, lockVisible, lockHide));
            }

        } catch (Exception e) {
            list.clear();
            list.add(new TabItem(FgConst.FRAGMENT_ACCOUNTS, context.getString(R.string.ent_accounts),true, true, true));
            list.add(new TabItem(FgConst.FRAGMENT_TRANSACTIONS, context.getString(R.string.ent_transactions),true, true, true));
            list.add(new TabItem(FgConst.FRAGMENT_SUMMARY, context.getString(R.string.ent_summary),true, false, false));
            list.add(new TabItem(FgConst.FRAGMENT_DEBTS, context.getString(R.string.ent_debts),true, false, false));
        }
        return list;
    }

    public static Department getDefaultDepartment(Context context) {
        String s = PreferenceManager.getDefaultSharedPreferences(context).getString(FgConst.PREF_DEFAULT_DEPARTMENT, "-1");
        long id;
        try {
            id = Long.valueOf(s);
        } catch (Exception e) {
            id = -1;
        }

        return  (Department) DepartmentsDAO.getInstance(context).getModelById(id);
    }

    public static long getDefDepID(Context context) {
        String s = PreferenceManager.getDefaultSharedPreferences(context).getString(FgConst.PREF_DEFAULT_DEPARTMENT, "-1");
        long id;
        try {
            id = Long.valueOf(s);
        } catch (Exception e) {
            id = -1;
        }

        return  id;
    }
}
