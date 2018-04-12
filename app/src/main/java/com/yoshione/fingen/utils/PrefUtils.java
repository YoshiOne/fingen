package com.yoshione.fingen.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.yoshione.fingen.FgConst;
import com.yoshione.fingen.dao.DepartmentsDAO;
import com.yoshione.fingen.model.Department;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by slv on 26.11.2016.
 */

public class PrefUtils {
    public static List<String> getTabsOrder(SharedPreferences preferences) {
        String order = preferences.getString(FgConst.PREF_TAB_ORDER, "");
        String items[] = order.split(";");
        List<String> tabs = new ArrayList<>();
        try {
            for (int i = 0; i < 3; i++) {
                if ((items[i].equals(FgConst.FRAGMENT_SUMMARY) | items[i].equals(FgConst.FRAGMENT_ACCOUNTS)
                        | items[i].equals(FgConst.FRAGMENT_TRANSACTIONS)) & tabs.indexOf(items[i]) < 0) {
                    tabs.add(items[i]);
                } else {
                    throw new Exception("Parse tabs order preference exception");
                }
            }
        } catch (Exception e) {
            tabs.clear();
            tabs.add(FgConst.FRAGMENT_SUMMARY);
            tabs.add(FgConst.FRAGMENT_ACCOUNTS);
            tabs.add(FgConst.FRAGMENT_TRANSACTIONS);
        }
        return tabs;
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
