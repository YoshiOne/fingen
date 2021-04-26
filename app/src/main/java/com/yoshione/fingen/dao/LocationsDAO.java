package com.yoshione.fingen.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.yoshione.fingen.db.DbUtil;
import com.yoshione.fingen.interfaces.IAbstractModel;
import com.yoshione.fingen.interfaces.IDaoInheritor;
import com.yoshione.fingen.model.Location;

import java.util.List;

public class LocationsDAO extends BaseDAO<Location> implements IDaoInheritor {

    //<editor-fold desc="ref_Locations">
    public static final String TABLE = "ref_Locations";

    public static final String COL_LON = "Lon";
    public static final String COL_LAT = "Lat";
    public static final String COL_RADIUS = "Radius";
    public static final String COL_ADDRESS = "Address";

    public static final String[] ALL_COLUMNS = joinArrays(COMMON_COLUMNS, new String[]{
            COL_NAME, COL_LON, COL_LAT, COL_ADDRESS,
            COL_PARENT_ID, COL_ORDER_NUMBER, COL_FULL_NAME, COL_SEARCH_STRING
    });

    public static final String SQL_CREATE_TABLE = "CREATE TABLE " + TABLE + " ("
            + COMMON_FIELDS +       ", "
            + COL_NAME +            " TEXT NOT NULL, "
            + COL_LON +             " REAL NOT NULL, "
            + COL_LAT +             " REAL NOT NULL, "
            + COL_RADIUS +          " INTEGER, "
            + COL_ADDRESS +         " TEXT, "
            + COL_PARENT_ID +       " INTEGER REFERENCES [" + TABLE + "]([" + COL_ID + "]) ON DELETE SET NULL ON UPDATE CASCADE, "
            + COL_ORDER_NUMBER +    " INTEGER, "
            + COL_FULL_NAME +       " TEXT, "
            + COL_SEARCH_STRING +   " TEXT, "
            + "UNIQUE (" + COL_NAME + ", " + COL_PARENT_ID + ", " + COL_SYNC_DELETED + ") ON CONFLICT ABORT);";
    //</editor-fold>

    private static LocationsDAO sInstance;

    public synchronized static LocationsDAO getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new LocationsDAO(context);
        }
        return sInstance;
    }

    private LocationsDAO(Context context) {
        super(context, TABLE, ALL_COLUMNS);
        super.setDaoInheritor(this);
    }

    @Override
    public IAbstractModel createEmptyModel() {
        return new Location();
    }

    @Override
    public IAbstractModel cursorToModel(Cursor cursor) {
        return cursorToLocation(cursor);
    }

    private Location cursorToLocation(Cursor cursor) {
        Location location = new Location();

        location.setID(DbUtil.getLong(cursor, COL_ID));
        location.setName(DbUtil.getString(cursor, COL_NAME));
        location.setFullName(DbUtil.getString(cursor, COL_FULL_NAME));
        location.setParentID(DbUtil.getLong(cursor, COL_PARENT_ID));
        location.setAddress(DbUtil.getString(cursor, COL_ADDRESS));
        location.setLat(DbUtil.getDouble(cursor, COL_LAT));
        location.setLon(DbUtil.getDouble(cursor, COL_LON));
        location.setOrderNum(DbUtil.getInt(cursor, COL_ORDER_NUMBER));

//        location = (Location) DBHelper.getSyncDataFromCursor(location, cursor, mColumnIndexes);

        return location;
    }

    @Override
    public void deleteModel(IAbstractModel model, boolean resetTS, Context context) {
        ContentValues values = new ContentValues();
        values.put(TransactionsDAO.COL_LOCATION, -1);
        TransactionsDAO.getInstance(context).bulkUpdateItem(TransactionsDAO.COL_LOCATION + " = " + model.getID(), values, resetTS);

        super.deleteModel(model, resetTS, context);
    }

    public Location getLocationByID(long id) {
        return (Location) getModelById(id);
    }

    @Override
    public List<Location> getAllModels() {
        return getItems(getTableName(), null, null,
                null, COL_ORDER_NUMBER + "," + COL_NAME, null);
    }
}
