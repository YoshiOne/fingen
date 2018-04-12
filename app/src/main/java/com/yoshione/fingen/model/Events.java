package com.yoshione.fingen.model;

import android.preference.Preference;

import com.yoshione.fingen.interfaces.IAbstractModel;

import java.util.List;

/**
 * Created by slv on 08.10.2015.
 *
 */
public class Events {

    public static final int MODEL_ADDED = 0;
    public static final int MODEL_CHANGED = 1;
    public static final int MODEL_DELETED = 2;

    public static class EventOnModelChanged {
        private List<IAbstractModel> mModels;
        private int mModelType;
        private boolean mProcessInFirebaseService;
        private int mOperation;

        public boolean isProcessInFirebaseService() {
            return mProcessInFirebaseService;
        }

        public void setProcessInFirebaseService(boolean processInFirebaseService) {
            this.mProcessInFirebaseService = processInFirebaseService;
        }

        public List<IAbstractModel> getModels() {
            return mModels;
        }

        public int getModelType() {
            return mModelType;
        }

        public EventOnModelChanged(List<IAbstractModel> models, int modelType, int operation) {
            mModels = models;
            mModelType = modelType;
            mOperation = operation;
            mProcessInFirebaseService = true;
        }
    }

    public static class EventOnPreferenceChanged{
        public final Preference preference;
        public final Object newValue;
        public EventOnPreferenceChanged(Preference preference, Object newValue){
            this.preference = preference;
            this.newValue = newValue;
        }
    }

    public static class EventOnSortAccounts {
        public EventOnSortAccounts() {
        }
    }

    public static class EventOnGetSupportMessage{

    }

    public static class EventOnChangeCabbageInAmountEditor{
        private Cabbage mCabbage;

        public EventOnChangeCabbageInAmountEditor(Cabbage cabbage) {
            mCabbage = cabbage;
        }

        public Cabbage getCabbage() {
            return mCabbage;
        }
    }
}
