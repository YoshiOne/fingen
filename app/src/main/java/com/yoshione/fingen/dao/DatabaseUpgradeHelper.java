package com.yoshione.fingen.dao;

/**
 * Created by slv on 03.02.2017.
 */

public class DatabaseUpgradeHelper {
    private static DatabaseUpgradeHelper mInstance = null;
    private boolean isUpgrading;

    public synchronized static DatabaseUpgradeHelper getInstance() {
        if (mInstance == null) {
            mInstance = new DatabaseUpgradeHelper();
        }
        return mInstance;
    }

    public DatabaseUpgradeHelper() {
        isUpgrading = false;
    }

    public boolean isUpgrading() {
        return isUpgrading;
    }

    public void setUpgrading(boolean upgrading) {
        isUpgrading = upgrading;
    }
}
