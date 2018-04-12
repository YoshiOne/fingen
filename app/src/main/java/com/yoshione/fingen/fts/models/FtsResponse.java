
package com.yoshione.fingen.fts.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class FtsResponse {

    @SerializedName("document")
    @Expose
    private Document document;

    public Document getDocument() {
        return document;
    }

    public void setDocument(Document document) {
        this.document = document;
    }

}
