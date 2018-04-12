package com.yoshione.fingen.interfaces;

import android.database.Cursor;

/**
 * Created by slv on 02.09.2016.
 * +
 */

public interface IDaoInheritor {
    IAbstractModel cursorToModel(Cursor cursor);
}
