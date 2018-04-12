/*
 * Copyright (c) 2015. 
 */

package com.yoshione.fingen.filters;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.yoshione.fingen.DBHelper;
import com.yoshione.fingen.dao.AbstractDAO;
import com.yoshione.fingen.dao.BaseDAO;
import com.yoshione.fingen.interfaces.IAbstractModel;
import com.yoshione.fingen.managers.TreeManager;
import com.yoshione.fingen.utils.BaseNode;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Created by slv on 11.11.2015.
 * a
 */
public class NestedModelFilter extends AbstractFilter implements Parcelable {

    private final HashSet<Long> mIdList;
    private Boolean mEnabled = true;
    private int mModelType;
    private boolean mInverted;

    private long mId;

    public NestedModelFilter(long id, int modelType) {
        mModelType = modelType;
        mId = id;
        mIdList = new HashSet<>();
    }

    public int getModelType() {
        return mModelType;
    }

    public void setModelType(int modelType) {
        mModelType = modelType;
    }

    @Override
    public long getId() {
        return mId;
    }

    @Override
    public void setId(long id) {
        mId = id;
    }

    @Override
    public boolean isInverted() {
        return mInverted;
    }

    @Override
    public void setInverted(boolean inverted) {
        mInverted = inverted;
    }

    @Override
    public HashSet<Long> getIDsSet() {
        return mIdList;
    }

    @SuppressWarnings("unchecked")
    public HashSet<Long> getIDsSetWithNestedIDs(Context context) {
        HashSet<Long> ids = new HashSet<>();
        if (getEnabled()) {
            AbstractDAO dao = BaseDAO.getDAO(mModelType, context);
            if (dao == null) {
                return ids;
            }

            BaseNode tree;
            try {
                tree = TreeManager.convertListToTree((List<IAbstractModel>) dao.getAllModels(), mModelType);
            } catch (Exception e) {
                return ids;
            }

            String field;
            switch (mModelType) {
                case IAbstractModel.MODEL_TYPE_CATEGORY:
                    field = DBHelper.C_LOG_TRANSACTIONS_CATEGORY;
                    break;
                case IAbstractModel.MODEL_TYPE_PAYEE:
                    field = DBHelper.C_LOG_TRANSACTIONS_PAYEE;
                    break;
                case IAbstractModel.MODEL_TYPE_PROJECT:
                    field = DBHelper.C_LOG_TRANSACTIONS_PROJECT;
                    break;
                case IAbstractModel.MODEL_TYPE_SIMPLEDEBT:
                    field = DBHelper.C_LOG_TRANSACTIONS_SIMPLEDEBT;
                    break;
                case IAbstractModel.MODEL_TYPE_LOCATION:
                    field = DBHelper.C_LOG_TRANSACTIONS_LOCATION;
                    break;
                case IAbstractModel.MODEL_TYPE_DEPARTMENT:
                    field = DBHelper.C_LOG_TRANSACTIONS_DEPARTMENT;
                    break;
                default:
                    field = "";
            }

            if (field.isEmpty()) return ids;

            for (long id : mIdList) {
                ids.add(id);
                List<BaseNode> children;
                try {
                    children = tree.getNodeById(id).getFlatChildrenList();
                } catch (Exception e) {
                    children = new ArrayList<>();
                }
                for (BaseNode child : children) {
                    ids.add(child.getModel().getID());
                }
            }
            return ids;
        } else {
            return ids;
        }
    }

    @Override
    public Boolean getEnabled() {
        return mEnabled;
    }

    @Override
    public void setEnabled(Boolean enabled) {
        mEnabled = enabled;
    }

    @Override
    @SuppressWarnings("unchecked")
    public String getSelectionString() {
        return "";
    }


    @Override
    public String saveToString() {
        if (!mIdList.isEmpty()) {
            return TextUtils.join("@", mIdList);
        } else {
            return "empty";
        }
    }

    @Override
    public boolean loadFromString(String s) {
        mIdList.clear();
        if (s.equals("empty")) {
            return true;
        } else {
            String strings[] = s.split("@");
            for (String id : strings) {
                try {
                    mIdList.add(Long.valueOf(id));
                } catch (NumberFormatException e) {
                    return false;
                }
            }
            return true;
        }
    }

    public void addModel(long id) {
        mIdList.add(id);
    }

    public void removeModel(long id) {
        mIdList.remove(id);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeSerializable(this.mIdList);
        dest.writeValue(this.mEnabled);
        dest.writeInt(this.mModelType);
        dest.writeByte(this.mInverted ? (byte) 1 : (byte) 0);
        dest.writeLong(this.mId);
    }

    @SuppressWarnings("unchecked")
    private NestedModelFilter(Parcel in) {
        this.mIdList = (HashSet<Long>) in.readSerializable();
        this.mEnabled = (Boolean) in.readValue(Boolean.class.getClassLoader());
        this.mModelType = in.readInt();
        this.mInverted = in.readByte() != 0;
        this.mId = in.readLong();
    }

    public static final Creator<NestedModelFilter> CREATOR = new Creator<NestedModelFilter>() {
        @Override
        public NestedModelFilter createFromParcel(Parcel source) {
            return new NestedModelFilter(source);
        }

        @Override
        public NestedModelFilter[] newArray(int size) {
            return new NestedModelFilter[size];
        }
    };
}
