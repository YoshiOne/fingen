package com.yoshione.fingen.managers;

import android.content.Context;

import com.yoshione.fingen.dao.SendersDAO;
import com.yoshione.fingen.model.Sender;
import com.yoshione.fingen.model.Sms;

public class SmsManager {
    public static Sender getSender(Sms sms, Context context) {
        return SendersDAO.getInstance(context).getSenderByID(sms.getSenderId());
    }
}
