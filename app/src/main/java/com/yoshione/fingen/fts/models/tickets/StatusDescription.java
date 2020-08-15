package com.yoshione.fingen.fts.models.tickets;

import com.google.gson.annotations.SerializedName;

public class StatusDescription {
    @SerializedName("long")
    private String longMessage;
    @SerializedName("short")
    private String shortMessage;

    public String getLongMessage() {
        return longMessage;
    }

    public void setLongMessage(String longMessage) {
        this.longMessage = longMessage;
    }

    public String getShortMessage() {
        return shortMessage;
    }

    public void setShortMessage(String shortMessage) {
        this.shortMessage = shortMessage;
    }

    @Override
    public String toString() {
        return "StatusDescription{" +
                "longMessage='" + longMessage + '\'' +
                ", shortMessage='" + shortMessage + '\'' +
                '}';
    }
}
