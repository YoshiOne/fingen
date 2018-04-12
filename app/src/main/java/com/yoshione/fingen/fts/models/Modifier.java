
package com.yoshione.fingen.fts.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Modifier {

    @SerializedName("markupSum")
    @Expose
    private long markupSum;

    public long getMarkupSum() {
        return markupSum;
    }

    public void setMarkupSum(long markupSum) {
        this.markupSum = markupSum;
    }

}
