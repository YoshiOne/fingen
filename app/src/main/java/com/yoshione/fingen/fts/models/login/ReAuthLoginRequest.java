package com.yoshione.fingen.fts.models.login;

public class ReAuthLoginRequest {
    private String clientSecret;
    private String refreshToken;

    public ReAuthLoginRequest(String clientSecret, String refreshToken) {
        this.clientSecret = clientSecret;
        this.refreshToken = refreshToken;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String toJson() {
        return "{\"client_secret\": \"" + getClientSecret() + "\", \"refresh_token\": \"" + getRefreshToken() + "\"}";
    }

    @Override
    public String toString() {
        return "ReAuthLoginRequest{" +
                "clientSecret='" + clientSecret + '\'' +
                ", refreshToken='" + refreshToken + '\'' +
                '}';
    }
}
