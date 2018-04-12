package com.yoshione.fingen.utils;

import android.os.Handler;
import android.os.Message;

import com.yoshione.fingen.BaseListFragment;
import com.yoshione.fingen.classes.ListSumsByCabbage;

import java.lang.ref.WeakReference;

/**
 * Created by slv on 08.11.2016.
 */
public class UpdateMainListsRwHandler extends Handler {
    public static final int UPDATE_LIST = 0;
    public static final int UPDATE_SUMS = 1;

    private WeakReference<BaseListFragment> mReference;
    private String mClassName;

    public UpdateMainListsRwHandler(BaseListFragment reference, String className) {
        mReference = new WeakReference<>(reference);
        mClassName = className;
    }

    @Override
    public void handleMessage(Message msg) {
        BaseListFragment fragment = mReference.get();
        if (fragment != null && fragment.getUpdateListsEvents() != null) {
            int itemID = msg.arg1;
            switch (msg.what) {
                case UPDATE_LIST:
                    Lg.log("%s update lists", mClassName);
                    fragment.getUpdateListsEvents().updateLists(itemID);
                    break;
                case UPDATE_SUMS:
                    ListSumsByCabbage listSumsByCabbage = (ListSumsByCabbage) msg.obj;
                    fragment.getUpdateListsEvents().updateSums(listSumsByCabbage);
                    break;
            }
        }
    }
}
