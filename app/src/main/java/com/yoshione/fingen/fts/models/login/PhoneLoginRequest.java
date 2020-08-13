package com.yoshione.fingen.fts.models.login;

public class PhoneLoginRequest {
    private String client_secret;
    private String code;
    private String phone;

    public PhoneLoginRequest(String client_secret, String phone, String code) {
        this.client_secret = client_secret;
        this.phone = phone;
        this.code = code;
    }

    public String getClient_secret() {
        return client_secret;
    }

    public void setClient_secret(String client_secret) {
        this.client_secret = client_secret;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    @Override
    public String toString() {
        return "PhoneLoginRequest{" +
                "client_secret='" + client_secret + '\'' +
                ", code='" + code + '\'' +
                ", phone='" + phone + '\'' +
                '}';
    }
}
