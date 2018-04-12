/*
 * Copyright (c) 2015. 
 */

package com.yoshione.fingen.managers;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;

import com.yoshione.fingen.FgConst;
import com.yoshione.fingen.dao.AccountsDAO;
import com.yoshione.fingen.dao.CabbagesDAO;
import com.yoshione.fingen.dao.PayeesDAO;
import com.yoshione.fingen.FragmentSmsMarkerEdit;
import com.yoshione.fingen.interfaces.IAbstractModel;
import com.yoshione.fingen.model.Account;
import com.yoshione.fingen.model.Cabbage;
import com.yoshione.fingen.model.Payee;
import com.yoshione.fingen.model.SmsMarker;
import com.yoshione.fingen.model.Transaction;
import com.yoshione.fingen.R;
import com.yoshione.fingen.utils.SmsParser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by slv on 17.12.2015.
 *
 */
public class SmsMarkerManager {

    private static final int[] mTypesTransaction = {SmsParser.MARKER_TYPE_ACCOUNT, SmsParser.MARKER_TYPE_CABBAGE,
            SmsParser.MARKER_TYPE_TRTYPE, SmsParser.MARKER_TYPE_PAYEE, SmsParser.MARKER_TYPE_IGNORE};
    private static final int[] mTypesTransfer = {SmsParser.MARKER_TYPE_ACCOUNT, SmsParser.MARKER_TYPE_CABBAGE,
            SmsParser.MARKER_TYPE_TRTYPE, SmsParser.MARKER_TYPE_DESTACCOUNT, SmsParser.MARKER_TYPE_IGNORE};

    public static void showEditdialog(SmsMarker smsMarker, FragmentManager fragmentManager,
                               FragmentSmsMarkerEdit.IDialogDismissListener dialogDismissListener, Context context) {

        String title;
        if (smsMarker.getID() < 0) {
            title = context.getResources().getString(R.string.act_create_marker);
        } else {
            title = context.getResources().getString(R.string.ttl_edit_sms_parser_marker);
        }

        FragmentSmsMarkerEdit alertDialog = FragmentSmsMarkerEdit.newInstance(title, smsMarker);
        alertDialog.setDialogDismissListener(dialogDismissListener);
        alertDialog.show(fragmentManager, "fragment_smsmarker_edit");
    }

    @SuppressWarnings("unchecked")
    public static void setObjctFromText(SmsMarker smsMarker, String text, Context context) {
        switch (smsMarker.getType()) {
            case SmsParser.MARKER_TYPE_ACCOUNT:
            case SmsParser.MARKER_TYPE_DESTACCOUNT:
                AccountsDAO accountsDAO = AccountsDAO.getInstance(context);
                long accID = 0;
                try {
                    accID = accountsDAO.getModelByName(text).getID();
                } catch (Exception e) {
                    accID = -1;
                }
                smsMarker.setObject(String.valueOf(accID));
                break;
            case SmsParser.MARKER_TYPE_CABBAGE:
                CabbagesDAO cabbagesDAO = CabbagesDAO.getInstance(context);
                List<Cabbage> cabbages = null;
                try {
                    cabbages = (List<Cabbage>) cabbagesDAO.getAllModels();
                } catch (Exception e) {
                    cabbages = new ArrayList<>();
                }
                for (Cabbage cabbage : cabbages) {
                    if (cabbage.toString().equals(text)) {
                        smsMarker.setObject(String.valueOf(cabbage.getID()));
                    }
                }
                break;
            case SmsParser.MARKER_TYPE_PAYEE:
                PayeesDAO payeesDAO = PayeesDAO.getInstance(context);
                try {
                    smsMarker.setObject(String.valueOf(payeesDAO.getModelByName(text).getID()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case SmsParser.MARKER_TYPE_TRTYPE:
                TrType trType = new TrType(text, context);
                smsMarker.setObject(String.valueOf(trType.mType));
                break;
            case SmsParser.MARKER_TYPE_IGNORE:
                smsMarker.setObject("");
                break;
        }
    }

    public static String getObjectAsText(SmsMarker smsMarker, Context context) {
        String text = "";
        String object = smsMarker.getObject();
        switch (smsMarker.getType()) {
            case SmsParser.MARKER_TYPE_ACCOUNT:
            case SmsParser.MARKER_TYPE_DESTACCOUNT:
                AccountsDAO accountsDAO = AccountsDAO.getInstance(context);
                text = accountsDAO.getModelById(getObjectIdFromString(object)).toString();
                break;
            case SmsParser.MARKER_TYPE_CABBAGE:
                CabbagesDAO cabbagesDAO = CabbagesDAO.getInstance(context);
                text = cabbagesDAO.getModelById(getObjectIdFromString(object)).toString();
                break;
            case SmsParser.MARKER_TYPE_PAYEE:
                PayeesDAO payeesDAO = PayeesDAO.getInstance(context);
                text = payeesDAO.getModelById(getObjectIdFromString(object)).toString();
                break;
            case SmsParser.MARKER_TYPE_TRTYPE:
                text = new TrType(getObjectIdFromString(object), context).toString();
                break;
            case SmsParser.MARKER_TYPE_IGNORE:
                text = "";
                break;
        }
        return text;
    }

    public static  @Nullable
    IAbstractModel getObject(SmsMarker smsMarker, Context context) {
        String objectID = smsMarker.getObject();
        switch (smsMarker.getType()) {
            case SmsParser.MARKER_TYPE_ACCOUNT:
            case SmsParser.MARKER_TYPE_DESTACCOUNT:
                AccountsDAO accountsDAO = AccountsDAO.getInstance(context);
                return accountsDAO.getModelById(getObjectIdFromString(objectID));
            case SmsParser.MARKER_TYPE_CABBAGE:
                CabbagesDAO cabbagesDAO = CabbagesDAO.getInstance(context);
                return cabbagesDAO.getModelById(getObjectIdFromString(objectID));
            case SmsParser.MARKER_TYPE_PAYEE:
                PayeesDAO payeesDAO = PayeesDAO.getInstance(context);
                return payeesDAO.getModelById(getObjectIdFromString(objectID));
            default:
                return null;
        }
    }

    public static List<?> getAllObjects(SmsMarker smsMarker, Context context) {
        List<?> objects = new ArrayList<>();
        switch (smsMarker.getType()) {
            case SmsParser.MARKER_TYPE_ACCOUNT:
            case SmsParser.MARKER_TYPE_DESTACCOUNT:
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
                AccountsDAO accountsDAO = AccountsDAO.getInstance(context);
                try {
                    objects = accountsDAO.getAllAccounts(preferences.getBoolean(FgConst.PREF_SHOW_CLOSED_ACCOUNTS, true));
                } catch (Exception e) {
                    objects = new ArrayList<Account>();
                }
                break;
            case SmsParser.MARKER_TYPE_CABBAGE:
                CabbagesDAO cabbagesDAO = CabbagesDAO.getInstance(context);
                try {
                    objects = cabbagesDAO.getAllModels();
                } catch (Exception e) {
                    objects = new ArrayList<Cabbage>();
                }
                break;
            case SmsParser.MARKER_TYPE_PAYEE:
                PayeesDAO payeesDAO = PayeesDAO.getInstance(context);
                try {
                    objects = payeesDAO.getAllModels();
                } catch (Exception e) {
                    objects = new ArrayList<Payee>();
                }
                break;
            case SmsParser.MARKER_TYPE_TRTYPE:
                objects = new ArrayList<>(Arrays.asList(new TrType(-1, context), new TrType(1, context), new TrType(0, context)));
                break;
        }
        return objects;
    }

    private static long getObjectIdFromString(String id) {
        long result;
        try {
            result = Long.valueOf(id);
        } catch (NumberFormatException e) {
            result = -1;
        }
        return result;
    }

    private static String[] idsToStrings(int[] ids, Context context) {
        String result[] = new String[ids.length];
        for (int i = 0; i < ids.length; i++) {
            result[i] = getMarkerTypeName(ids[i], context);
        }
        return result;
    }

    private static String[] getNames(int trType, Context context) {
        switch (trType) {
            case Transaction.TRANSACTION_TYPE_INCOME:
            case Transaction.TRANSACTION_TYPE_EXPENSE:
                return idsToStrings(mTypesTransaction, context);
            case Transaction.TRANSACTION_TYPE_TRANSFER:
                return idsToStrings(mTypesTransfer, context);
        }
        return new String[]{};
    }

    private static int[] getTypes(int trType) {
        switch (trType) {
            case Transaction.TRANSACTION_TYPE_INCOME:
            case Transaction.TRANSACTION_TYPE_EXPENSE:
                return mTypesTransaction;
            case Transaction.TRANSACTION_TYPE_TRANSFER:
                return mTypesTransfer;
        }
        return new int[]{};
    }

    public static SmsMarkerType[] getSmsMarkerTypeObjects(int trType, Context context) {
        int ids[] = getTypes(trType);
        String names[] = getNames(trType, context);
        SmsMarkerType result[] = new SmsMarkerType[ids.length];
        for (int i = 0; i < ids.length; i++) {
            result[i] = new SmsMarkerType(ids[i], names[i]);
        }
        return result;
    }

    public static String getMarkerTypeName(int id, Context context) {
        Resources res = context.getResources();
        switch (id) {
            case SmsParser.MARKER_TYPE_ACCOUNT:
                return res.getString(R.string.ent_account);
            case SmsParser.MARKER_TYPE_CABBAGE:
                return res.getString(R.string.ent_currency);
            case SmsParser.MARKER_TYPE_TRTYPE:
                return res.getString(R.string.ent_type);
            case SmsParser.MARKER_TYPE_PAYEE:
                return res.getString(R.string.ent_payee_or_payer);
            case SmsParser.MARKER_TYPE_IGNORE:
                return res.getString(R.string.ent_skip);
            case SmsParser.MARKER_TYPE_DESTACCOUNT:
                return res.getString(R.string.ent_dest_account);
            default:
                return "";
        }
    }

    public static class SmsMarkerType {
        public final int id;
        public final String name;

        SmsMarkerType(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public String toString() {
            return name;
        }
    }

    public static class TrType {
        private final Long mType;
        private final Context mContext;

        TrType(long mType, Context context) {
            this.mType = mType;
            this.mContext = context;
        }

        public String toString() {
            int retval = mType.compareTo(0L);
            if (retval > 0) {
                return mContext.getResources().getString(R.string.ent_income);
            } else if (retval < 0) {
                return mContext.getResources().getString(R.string.ent_outcome);
            } else {
                return mContext.getResources().getString(R.string.ent_transfer);
            }
        }

        TrType(String s, Context context) {
            this.mContext = context;
            if (s.equals(context.getResources().getString(R.string.ent_income))) {
                mType = 1L;
            } else if (s.equals(context.getResources().getString(R.string.ent_outcome))) {
                mType = -1L;
            } else {
                mType = 0L;
            }
        }
    }

    public static String loadNames(SmsMarker marker, Context context) {
        String type = getMarkerTypeName(marker.getType(), context);
        String object = "";
        String pattern = marker.getMarker();
        switch (marker.getType()) {
            case SmsParser.MARKER_TYPE_ACCOUNT:
                object = AccountsDAO.getInstance(context).getAccountByID(Long.valueOf(marker.getObject())).getName();
                break;
            case SmsParser.MARKER_TYPE_CABBAGE:
                object = CabbagesDAO.getInstance(context).getCabbageByID(Long.valueOf(marker.getObject())).toString();
                break;
            case SmsParser.MARKER_TYPE_TRTYPE:
                object = new TrType(marker.getType(), context).toString();
                break;
            case SmsParser.MARKER_TYPE_PAYEE:
                object = PayeesDAO.getInstance(context).getPayeeByID(Long.valueOf(marker.getObject())).getName();
                break;
            case SmsParser.MARKER_TYPE_IGNORE:
                break;
            case SmsParser.MARKER_TYPE_DESTACCOUNT:
                object = AccountsDAO.getInstance(context).getAccountByID(Long.valueOf(marker.getObject())).getName();
                break;
        }
        return String.format("%s %s %s", type, object, pattern);
    }
}
