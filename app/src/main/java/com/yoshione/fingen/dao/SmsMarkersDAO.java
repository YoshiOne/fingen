/*
 * Copyright (c) 2015. 
 */

package com.yoshione.fingen.dao;

import android.content.Context;
import android.database.Cursor;

import com.yoshione.fingen.DBHelper;
import com.yoshione.fingen.interfaces.IDaoInheritor;
import com.yoshione.fingen.interfaces.IAbstractModel;
import com.yoshione.fingen.model.SmsMarker;
import com.yoshione.fingen.utils.SmsParser;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Leonid on 01.11.2015.
 */
public class SmsMarkersDAO extends BaseDAO implements AbstractDAO, IDaoInheritor {
    public static final String TAG = "SmsMarkersDAO";

    private static SmsMarkersDAO sInstance;

//    char spec[] = {'[', '\\', '^', '$', '.', '|', '?', '*', '+', '(', ')'};

    private SmsMarkersDAO(Context context) {
        super(context, DBHelper.T_LOG_SMS_PARSER_PATTERNS, IAbstractModel.MODEL_TYPE_SMSMARKER , DBHelper.T_LOG_SMS_PARSER_PATTERNS_ALL_COLUMNS);
        super.setDaoInheritor(this);
    }

    public synchronized static SmsMarkersDAO getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new SmsMarkersDAO(context);
        }
        return sInstance;
    }

    @Override
    public IAbstractModel cursorToModel(Cursor cursor) {
        return cursorToSmsParserPattern(cursor);
    }

    private SmsMarker cursorToSmsParserPattern(Cursor cursor) {
        SmsMarker smsMarker = new SmsMarker();

        smsMarker.setID(cursor.getLong(mColumnIndexes.get(DBHelper.C_ID)));
        smsMarker.setType(cursor.getInt(mColumnIndexes.get(DBHelper.C_LOG_SMS_PARSER_PATTERNS_TYPE)));
        smsMarker.setObject(cursor.getString(mColumnIndexes.get(DBHelper.C_LOG_SMS_PARSER_PATTERNS_OBJECT)));
        if (smsMarker.getType() == SmsParser.MARKER_TYPE_CABBAGE) {
            smsMarker.setMarker(escapeSpecialSymbols(cursor.getString(mColumnIndexes.get(DBHelper.C_LOG_SMS_PARSER_PATTERNS_PATTERN))));
        } else {
            smsMarker.setMarker(cursor.getString(mColumnIndexes.get(DBHelper.C_LOG_SMS_PARSER_PATTERNS_PATTERN)));
        }

        return smsMarker;
    }

    private String escapeSpecialSymbols(String s) {
        return s.replaceAll("(?=[]\\[+&$|!(){}^\"~*?:\\\\-])", "\\\\");
    }

    @SuppressWarnings("unchecked")
    public List<SmsMarker> getAllSmsParserPatterns() throws Exception {
        return (List<SmsMarker>) getItems(getTableName(), null,
                null, null, DBHelper.C_LOG_SMS_PARSER_PATTERNS_TYPE, null);
    }

    @SuppressWarnings("unchecked")
    public List<String> getAllObjectsByType(int type) {
        List<String> result = new ArrayList<>();

        List<SmsMarker> markers;
        try {
            markers = (List<SmsMarker>) getItems(getTableName(), null,
                    DBHelper.C_LOG_SMS_PARSER_PATTERNS_TYPE + " = " + type,
                    DBHelper.C_LOG_SMS_PARSER_PATTERNS_OBJECT,
                    null, null);
        } catch (Exception e) {
            markers = new ArrayList<>();
        }

        for (SmsMarker marker : markers) {
            result.add(marker.getObject());
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    public List<String> getAllMarkersByObject(int type, String object) {
        List<String> result = new ArrayList<>();

        List<SmsMarker> markers;
        try {
            markers = (List<SmsMarker>) getItems(getTableName(), null,
                    String.format("(%s = %s) AND (%s = '%s')",
                            DBHelper.C_LOG_SMS_PARSER_PATTERNS_TYPE, String.valueOf(type),
                            DBHelper.C_LOG_SMS_PARSER_PATTERNS_OBJECT, object),
                    null,
                    null, null);
        } catch (Exception e) {
            markers = new ArrayList<>();
        }

        for (SmsMarker marker : markers) {
            result.add(marker.getMarker());
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    public List<String> getAllPatternsByType(int type) {
        List<String> result = new ArrayList<>();

        List<SmsMarker> markers;
        try {
            markers = (List<SmsMarker>) getItems(getTableName(), null,
                    DBHelper.C_LOG_SMS_PARSER_PATTERNS_TYPE + " = " + type,
                    null,
                    null, null);
        } catch (Exception e) {
            markers = new ArrayList<>();
        }

        for (SmsMarker marker : markers) {
            result.add(marker.getMarker());
        }
        return result;
    }

    @Override
    public List<?> getAllModels() throws Exception {
        return getAllSmsParserPatterns();
    }
}
