package com.yoshione.fingen.dao;

import android.content.Context;
import android.database.Cursor;

import com.yoshione.fingen.db.DbUtil;
import com.yoshione.fingen.interfaces.IDaoInheritor;
import com.yoshione.fingen.interfaces.IAbstractModel;
import com.yoshione.fingen.model.SmsMarker;
import com.yoshione.fingen.utils.SmsParser;

import java.util.ArrayList;
import java.util.List;

public class SmsMarkersDAO extends BaseDAO implements AbstractDAO, IDaoInheritor {

    //<editor-fold desc="ref_Sms_Parser_Patterns">
    public static final String TABLE = "ref_Sms_Parser_Patterns";

    public static final String COL_TYPE = "Type";
    public static final String COL_OBJECT = "Object";
    public static final String COL_PATTERN = "Pattern";

    public static final String[] ALL_COLUMNS = joinArrays(COMMON_COLUMNS, new String[]{
            COL_TYPE, COL_OBJECT, COL_PATTERN
    });

    public static final String SQL_CREATE_TABLE = "CREATE TABLE " + TABLE + " ("
            + COMMON_FIELDS +   ", "
            + COL_TYPE +        " INTEGER NOT NULL, "
            + COL_OBJECT +      " TEXT NOT NULL, "
            + COL_PATTERN +     " TEXT NOT NULL, "
            + "UNIQUE (" + COL_TYPE + ", " + COL_PATTERN + ", " + COL_SYNC_DELETED + ") ON CONFLICT REPLACE);";
    //</editor-fold>

    private static SmsMarkersDAO sInstance;

    public synchronized static SmsMarkersDAO getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new SmsMarkersDAO(context);
        }
        return sInstance;
    }

    private SmsMarkersDAO(Context context) {
        super(context, TABLE, ALL_COLUMNS);
        super.setDaoInheritor(this);
    }

    @Override
    public IAbstractModel createEmptyModel() {
        return new SmsMarker();
    }

    @Override
    public IAbstractModel cursorToModel(Cursor cursor) {
        return cursorToSmsParserPattern(cursor);
    }

    private SmsMarker cursorToSmsParserPattern(Cursor cursor) {
        SmsMarker smsMarker = new SmsMarker();

        smsMarker.setID(DbUtil.getLong(cursor, COL_ID));
        smsMarker.setType(DbUtil.getInt(cursor, COL_TYPE));
        smsMarker.setObject(DbUtil.getString(cursor, COL_OBJECT));
        String pattern = DbUtil.getString(cursor, COL_PATTERN);
        smsMarker.setMarker(smsMarker.getType() == SmsParser.MARKER_TYPE_CABBAGE ? escapeSpecialSymbols(pattern): pattern);

        return smsMarker;
    }

    private String escapeSpecialSymbols(String s) {
        return s.replaceAll("(?=[]\\[+&$|!(){}^\"~*?:\\\\-])", "\\\\");
    }

    @SuppressWarnings("unchecked")
    public List<SmsMarker> getAllSmsParserPatterns() {
        return (List<SmsMarker>) getItems(getTableName(), null,
                null, null, COL_TYPE, null);
    }

    @SuppressWarnings("unchecked")
    public List<String> getAllObjectsByType(int type) {
        List<String> result = new ArrayList<>();

        List<SmsMarker> markers;
        try {
            markers = (List<SmsMarker>) getItems(getTableName(), null,
                    COL_TYPE + " = " + type,
                    COL_OBJECT,
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
                            COL_TYPE, String.valueOf(type),
                            COL_OBJECT, object),
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
                    COL_TYPE + " = " + type,
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
    public List<?> getAllModels() {
        return getAllSmsParserPatterns();
    }
}
