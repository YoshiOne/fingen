
package com.yoshione.fingen.fts.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Document {

    @SerializedName("receipt")
    @Expose
    private Receipt receipt;

    public Receipt getReceipt() {
        return receipt;
    }

    public void setReceipt(Receipt receipt) {
        this.receipt = receipt;
    }

}
