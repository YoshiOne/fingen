package com.yoshione.fingen.db;

import com.yoshione.fingen.interfaces.IAbstractModel;

/**
 * Created by slv on 29.03.2017.
 */

public interface IOnConflict {
    void conflict(IAbstractModel model);
}
