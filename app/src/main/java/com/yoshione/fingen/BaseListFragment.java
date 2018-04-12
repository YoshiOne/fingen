package com.yoshione.fingen;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.yoshione.fingen.interfaces.IUpdateMainListsEvents;
import com.yoshione.fingen.utils.Lg;
import com.yoshione.fingen.utils.UpdateMainListsRwHandler;
import com.yoshione.fingen.widgets.ContextMenuRecyclerView;
import com.yoshione.fingen.widgets.FgLinearLayoutManager;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by slv on 08.11.2016.
 * Базовый фрагмент, от которого наследуются фрагменты на главном экране (те, что во ViewPager)
 */

public class BaseListFragment extends Fragment implements IListFragment {

    public static final String FORCE_UPDATE_PARAM = "forceUpdateParam";
    public static final String LAYOUT_NAME_PARAM = "layoutID";
    @BindView(R.id.recycler_view)
    ContextMenuRecyclerView recyclerView;
    private Unbinder unbinder;
    private UpdateMainListsRwHandler mUpdateMainListsRwHandler;
    private boolean isUpdating = false;
    private IUpdateMainListsEvents mUpdateListsEvents;
    private String mForceUpdateParam;
//    private boolean isVisibleToUser = false;
//    private boolean isViewCreated = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Lg.log("%s onCreate", getClass().getName());
        super.onCreate(savedInstanceState);
        mUpdateMainListsRwHandler = new UpdateMainListsRwHandler(this, getClass().getName());
        if (getArguments() != null) {
            mForceUpdateParam = getArguments().getString(FORCE_UPDATE_PARAM);
        }

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Lg.log("%s onCreateView", getClass().getName());
        //R.layout.fragment_accounts
        View view = inflater.inflate(getArguments().getInt(LAYOUT_NAME_PARAM), container, false);
        unbinder = ButterKnife.bind(this, view);
        FgLinearLayoutManager layoutManager = new FgLinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
//        isViewCreated = true;
        return view;
    }

    @Override
    public void onDestroyView() {
        Lg.log("%s onDestroyView", getClass().getName());
        super.onDestroyView();
        unbinder.unbind();
    }

    @Override
    public void onResume() {
        Lg.log("%s onResume", getClass().getName());
        super.onResume();
        registerForContextMenu(recyclerView);
        fullUpdate(-1);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        Lg.log("%s setUserVisibleHint %s ", getClass().getName(), String.valueOf(isVisibleToUser));
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            if (getView() != null) {
                updateIfNecessary(-1);
            }
        }
    }

    @Override
    public void onPause() {
        Lg.log("%s onPause", getClass().getName());
        unregisterForContextMenu(recyclerView);
        super.onPause();
    }

    public IUpdateMainListsEvents getUpdateListsEvents() {
        return mUpdateListsEvents;
    }

    public void setUpdateListsEvents(IUpdateMainListsEvents updateListsEvents) {
        mUpdateListsEvents = updateListsEvents;
    }

    @Override
    public void fullUpdate(long itemID) {
        Lg.log("%s fullUpdate", getClass().getName());
        if (getView() != null && getUserVisibleHint() && !isUpdating) {
            runUpdateThread(itemID);
        } else {
            PreferenceManager.getDefaultSharedPreferences(getActivity()).edit().putBoolean(mForceUpdateParam, true).apply();
        }
    }

    private void updateIfNecessary(long itemID) {
        Lg.log("%s updateIfNecessary", getClass().getName());
        if (getView() != null && getUserVisibleHint() && !isUpdating && PreferenceManager.getDefaultSharedPreferences(getActivity()).getBoolean(mForceUpdateParam, false)) {
            runUpdateThread(itemID);
        }
    }

    private void runUpdateThread(long itemID) {
        Lg.log("%s runUpdateThread", getClass().getName());
        isUpdating = true;
        Thread t = new Thread(new UpdateRunnable(mUpdateListsEvents, itemID, getClass().getName()));
        t.start();
    }

    private class UpdateRunnable implements Runnable {
        private IUpdateMainListsEvents mUpdateEvents;
        private long mItemID;
        private String mClassName;

        UpdateRunnable(IUpdateMainListsEvents updateEvents, long itemID, String className) {
            mUpdateEvents = updateEvents;
            mItemID = itemID;
            mClassName = className;
        }

        @Override
        public void run() {
            if (mUpdateEvents != null) {

//                if (BuildConfig.DEBUG) {
//                    Debug.startMethodTracing(mClassName + "LoadData");
//                }
                PreferenceManager.getDefaultSharedPreferences(getActivity()).edit().putBoolean(mForceUpdateParam, false).apply();
                Lg.log("%s load data", mClassName);
                mUpdateEvents.loadData(mUpdateMainListsRwHandler, mItemID);
                mUpdateEvents.loadSums(mUpdateMainListsRwHandler);

//                if (BuildConfig.DEBUG) {
//                    Debug.stopMethodTracing();
//                }
            }
            isUpdating = false;
        }
    }

}
