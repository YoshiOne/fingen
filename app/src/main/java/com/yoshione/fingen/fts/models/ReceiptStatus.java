
package com.yoshione.fingen.fts.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ReceiptStatus {

    @SerializedName("id")
    @Expose
    private String id;

    @SerializedName("kind")
    @Expose
    private String kind;

    @SerializedName("status")
    @Expose
    private int status;

    public int getStatus() {
        return status;
    }

    public String getId() {
        return id;
    }
}
