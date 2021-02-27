package com.yoshione.fingen.model;

import android.content.ContentValues;
import android.os.Parcel;

import com.yoshione.fingen.dao.ProductsDAO;
import com.yoshione.fingen.interfaces.IAbstractModel;

public class Product extends BaseModel implements IAbstractModel {

    private String mName;

    public Product() {
        super();
        mName = "";
    }

    public Product(long id, String name) {
        super();
        setID(id);
        mName = name;
    }

    @Override
    public String getName() {
        return mName;
    }

    @Override
    public String getFullName() {
        return mName;
    }

    @Override
    public void setName(String name) {
        mName = name;
    }

    @Override
    public String toString() {
        return mName;
    }

    @Override
    public String getSearchString() {
        return mName;
    }

    @Override
    public ContentValues getCV() {
        ContentValues values = super.getCV();

        values.put(ProductsDAO.COL_NAME, getName());

        return values;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(this.mName);
    }

    protected Product(Parcel in) {
        super(in);
        this.mName = in.readString();
    }

    public static final Creator<Product> CREATOR = new Creator<Product>() {
        @Override
        public Product createFromParcel(Parcel source) {
            return new Product(source);
        }

        @Override
        public Product[] newArray(int size) {
            return new Product[size];
        }
    };
}
