package com.yoshione.fingen.fts;

public interface IFtsCallback {
    void onAccepted(Object response);
    void onFailure(String errMsg);
}
