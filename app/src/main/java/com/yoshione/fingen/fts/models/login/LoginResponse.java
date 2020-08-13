package com.yoshione.fingen.fts.models.login;

public class LoginResponse {
    private String email;
    private String name;
    private String phone;
    private String refresh_token;
    private String region;
    private String sessionId;
    private String surname;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getRefresh_token() {
        return refresh_token;
    }

    public void setRefresh_token(String refresh_token) {
        this.refresh_token = refresh_token;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    @Override
    public String toString() {
        return "LoginResponse{" +
                "email='" + email + '\'' +
                ", name='" + name + '\'' +
                ", phone='" + phone + '\'' +
                ", refreshToken='" + refresh_token + '\'' +
                ", region='" + region + '\'' +
                ", sessionId='" + sessionId + '\'' +
                ", surname='" + surname + '\'' +
                '}';
    }
}
