package com.yoshione.fingen.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Pair;


import com.yoshione.fingen.DBHelper;
import com.yoshione.fingen.interfaces.IAbstractModel;
import com.yoshione.fingen.interfaces.IDaoInheritor;
import com.yoshione.fingen.model.Location;

import java.util.HashMap;
import java.util.List;

/**
 * Created by Leonid on 13.08.2015.
 *
 */
public class LocationsDAO extends BaseDAO implements AbstractDAO, IDaoInheritor {
    private static LocationsDAO sInstance;

    private LocationsDAO(Context context) {
        super(context, DBHelper.T_REF_LOCATIONS, IAbstractModel.MODEL_TYPE_LOCATION , DBHelper.T_REF_LOCATIONS_ALL_COLUMNS);
        super.setDaoInheritor(this);
    }

    public synchronized static LocationsDAO getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new LocationsDAO(context);
        }
        return sInstance;
    }

    @Override
    public IAbstractModel cursorToModel(Cursor cursor) {
        return cursorToLocation(cursor);
    }

    private Location cursorToLocation(Cursor cursor) {
        Location location = new Location();

        location.setID(cursor.getLong(mColumnIndexes.get(DBHelper.C_ID)));
        location.setName(cursor.getString(mColumnIndexes.get(DBHelper.C_REF_LOCATIONS_NAME)));
        location.setFullName(cursor.getString(mColumnIndexes.get(DBHelper.C_FULL_NAME)));
        location.setParentID(cursor.getLong(mColumnIndexes.get(DBHelper.C_PARENTID)));
        location.setAddress(cursor.getString(mColumnIndexes.get(DBHelper.C_REF_LOCATIONS_ADDRESS)));
        location.setLat(cursor.getDouble(mColumnIndexes.get(DBHelper.C_REF_LOCATIONS_LAT)));
        location.setLon(cursor.getDouble(mColumnIndexes.get(DBHelper.C_REF_LOCATIONS_LON)));
        location.setOrderNum(cursor.getInt(mColumnIndexes.get(DBHelper.C_ORDERNUMBER)));

        location = (Location) DBHelper.getSyncDataFromCursor(location, cursor, mColumnIndexes);

        return location;
    }

    @Override
    public void deleteModel(IAbstractModel model, boolean resetTS, Context context) {
        ContentValues values = new ContentValues();
        values.put(DBHelper.C_LOG_TRANSACTIONS_LOCATION, -1);
        TransactionsDAO.getInstance(context).bulkUpdateItem(DBHelper.C_LOG_TRANSACTIONS_LOCATION + " = " + model.getID(), values, resetTS);

        super.deleteModel(model, resetTS, context);
    }

    @SuppressWarnings("unchecked")
    public List<Location> getAllLocations() throws Exception {
        return (List<Location>) getItems(getTableName(), null, null,
                null, DBHelper.C_ORDERNUMBER + "," + DBHelper.C_REF_LOCATIONS_NAME, null);
    }

    public Location getLocationByID(long id) {
        return (Location) getModelById(id);
    }

    @Override
    public List<?> getAllModels() throws Exception {
        return getAllLocations();
    }
}
