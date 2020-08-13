package com.yoshione.fingen.fts.models.login;

public class ReAuthLoginResponse {
    private String refresh_token;
    private String sessionId;

    public String getRefresh_token() {
        return refresh_token;
    }

    public void setRefresh_token(String refresh_token) {
        this.refresh_token = refresh_token;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    @Override
    public String toString() {
        return "ReAuthLoginResponse{" +
                "refresh_token='" + refresh_token + '\'' +
                ", sessionId='" + sessionId + '\'' +
                '}';
    }
}
