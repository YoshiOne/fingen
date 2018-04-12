package com.yoshione.fingen.model;

/**
 * Created by slv on 14.03.2017.
 * UserPermission
 */

public class UserPermission {
    private String mEmail;
    private String mHash;
    private boolean mRead;
    private boolean mWrite;

    public UserPermission() {
        mEmail = "";
        mHash = "";
        mRead = true;
        mWrite = true;
    }

    public UserPermission(String email, String hash, boolean read, boolean write) {
        mEmail = email;
        mHash = hash;
        mRead = read;
        mWrite = write;
    }

    public String getEmail() {
        return mEmail;
    }

    public String getHash() {
        return mHash;
    }

    public boolean isRead() {
        return mRead;
    }

    public boolean isWrite() {
        return mWrite;
    }

    public void setEmail(String email) {
        mEmail = email;
    }

    public void setHash(String hash) {
        mHash = hash;
    }

    public void setRead(boolean read) {
        mRead = read;
    }

    public void setWrite(boolean write) {
        mWrite = write;
    }
}
