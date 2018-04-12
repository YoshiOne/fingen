package com.yoshione.fingen.model;

/**
 * Created by slv on 16.03.2017.
 */

public class SyncModelInfo {
    private final int mModel;
    private final boolean mSync;

    public SyncModelInfo(int model, boolean sync) {
        mModel = model;
        mSync = sync;
    }

    public int getModel() {
        return mModel;
    }

    public boolean isSync() {
        return mSync;
    }
}
