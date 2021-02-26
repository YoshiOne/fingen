
package com.yoshione.fingen.fts.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class RegistrationData {

    @SerializedName("refresh_token")
    @Expose
    private String token;

    @SerializedName("sessionId")
    @Expose
    private String session;

    public String getSession() {
        return session;
    }

    public String getToken() {
        return token;
    }
}
