
package com.yoshione.fingen.fts.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class FtsResponse {

    private String error;
    private Integer code = 0; // remove?!

    @SerializedName("ticket")
    @Expose
    private Ticket ticket;

    public Document getDocument() {
        return this.ticket.getDocument();
    }

    public void setDocument(Document document) {
        this.ticket.setDocument(document);
    }

    public Boolean hasTicket()
    {
        return ticket != null;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }
}
