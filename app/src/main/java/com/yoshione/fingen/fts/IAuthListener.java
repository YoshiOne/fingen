package com.yoshione.fingen.fts;

import com.yoshione.fingen.fts.models.AuthResponse;

public interface IAuthListener {
    void onAccepted(Object response);
    void onFailure(String errMsg);

    interface OnCallback {
        void onCallback(AuthResponse auth);
    }
}
