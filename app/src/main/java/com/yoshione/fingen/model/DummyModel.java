package com.yoshione.fingen.model;

import android.content.Context;
import android.os.Parcel;

import com.yoshione.fingen.R;
import com.yoshione.fingen.interfaces.IAbstractModel;

public class DummyModel extends BaseModel implements IAbstractModel {
    public static final String TAG = "com.yoshione.fingen.Model.DummyModel";

    public enum PredefinedKeys {

        SELECT_ALL(0),
        UNSELECT_ALL(1);

        private long key;

        private PredefinedKeys(long key) { this.key = key; }

        public long getKey() { return key; }
    }

    public DummyModel() {
        super();
    }

    public DummyModel(DummyModel.PredefinedKeys key, Context context) {
        super(key.getKey());

        switch (key) {
            case SELECT_ALL:
                super.setName(context.getString(R.string.act_select_all));
                break;
            case UNSELECT_ALL:
                super.setName(context.getString(R.string.act_unselect_all));
                break;
            default:
                super.setName("");
        }
    }

    public DummyModel(long key, String name) {
        super(key);
        super.setName(name);
    }

    public DummyModel(Parcel in) {
        super(in);
    }

    public static final Creator<DummyModel> CREATOR = new Creator<DummyModel>() {
        @Override
        public DummyModel createFromParcel(Parcel source) {
            return new DummyModel(source);
        }

        @Override
        public DummyModel[] newArray(int size) {
            return new DummyModel[size];
        }
    };

    @Override
    public String toString() {
        return super.getName();
    }
}
