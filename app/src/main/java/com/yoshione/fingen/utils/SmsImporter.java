package com.yoshione.fingen.utils;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.widget.Toast;

import android.util.Log;

import com.yoshione.fingen.R;
import com.yoshione.fingen.dao.SmsDAO;
import com.yoshione.fingen.dao.TransactionsDAO;
import com.yoshione.fingen.interfaces.IProgressEventsListener;
import com.yoshione.fingen.managers.TransactionManager;
import com.yoshione.fingen.model.Sender;
import com.yoshione.fingen.model.Sms;
import com.yoshione.fingen.model.Transaction;

import java.util.Date;

/**
 * Created by slv on 25.02.2016.
 *
 */
public class SmsImporter {
    private static final String TAG = "SmsImporter";
    private final Context mContext;
    private final Sender mSender;
    private final boolean mAutoCreate;
    private final Date mStartDate;
    private final Date mEndDate;

    private IProgressEventsListener mProgressEventsListener;

    public void setmProgressEventsListener(IProgressEventsListener mProgressEventsListener) {
        this.mProgressEventsListener = mProgressEventsListener;
    }

    public SmsImporter(Context mContext, Sender mSender, boolean mAutoCreate, Date mStartDate, Date mEndDate) {
        this.mContext = mContext;
        this.mSender = mSender;
        this.mAutoCreate = mAutoCreate;
        this.mStartDate = mStartDate;
        this.mEndDate = mEndDate;
    }

    public void importSms() {
        Uri mSmsinboxQueryUri = Uri.parse("content://sms/inbox");
        ContentResolver resolver = mContext.getContentResolver();
        String projection[] = new String[]{"_id", "thread_id", "address", "person", "date", "body", "type"};
        Cursor cursor;
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED) {
            cursor = resolver.query(mSmsinboxQueryUri, projection, "address LIKE LOWER(?)", new String[]{mSender.getPhoneNo()}, null);
        } else {
            Toast.makeText(mContext, mContext.getString(R.string.msg_permission_rw_external_storage_denied), Toast.LENGTH_SHORT).show();
            return;
        }
        if (cursor == null) {
            if (mProgressEventsListener != null) {
                mProgressEventsListener.onOperationComplete(IProgressEventsListener.CODE_ERROR);
            }
            return;
        }

        int count = 0;
        int trCreated = 0;
        int trSkipped = 0;
        int smsCreated = 0;
        int smsSkipped = 0;
        int currentRow = 0;
        if (cursor.getCount() > 0) {
            count = cursor.getCount();
//            Log.d(TAG, String.format("Count %d", count));
            Sms sms = new Sms();
            SmsParser smsParser;
            TransactionsDAO transactionsDAO = TransactionsDAO.getInstance(mContext);
            while (cursor.moveToNext()) {
//                String address = cursor.getString(cursor.getColumnIndex("address"));
                Date date = new Date(cursor.getLong(cursor.getColumnIndex("date")));
                String msg = cursor.getString(cursor.getColumnIndex("body"));

                boolean filtered = date.after(mStartDate);
                filtered = filtered & date.before(mEndDate);
//                filtered = filtered & address.trim().toLowerCase().equals(mSender.getPhoneNo().trim().toLowerCase());

                if (filtered) {
                    sms.setID(-1);
                    sms.setSenderId(mSender.getID());
                    sms.setmDateTime(date);
                    sms.setmBody(msg);

                    smsParser = new SmsParser(sms, mContext);

                    if (smsParser.goodToParse()) {
                        if (mAutoCreate) {
                            Transaction transaction = smsParser.extractTransaction();

                            if (TransactionManager.isValidToSmsAutocreate(transaction, mContext)) {
                                if (transactionsDAO.hasDuplicates(transaction).getID() < 0) {
                                    transaction.setComment(msg);
                                    transaction.setAutoCreated(true);
                                    try {
                                        transactionsDAO.createModel(transaction);
                                        Log.d(TAG, "Transaction created " + TransactionManager.transactionToString(transaction, mContext));
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    trCreated++;
                                } else {
                                    Log.d(TAG, "Transaction skipped " + TransactionManager.transactionToString(transaction, mContext));
                                    trSkipped++;
                                }
                            } else {
                                SmsDAO smsDAO = SmsDAO.getInstance(mContext);
                                try {
                                    sms = (Sms) smsDAO.createModel(sms);
                                    Log.d(TAG, "Sms created " + sms.getmBody());
                                    smsCreated++;
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        } else {
                            SmsDAO smsDAO = SmsDAO.getInstance(mContext);
                            try {
                                sms = (Sms) smsDAO.createModel(sms);
                                Log.d(TAG, "Sms created " + sms.getmBody());
                                smsCreated++;
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    } else {
                        smsSkipped++;
                    }
                } else {
                    smsSkipped++;
                }
                if (mProgressEventsListener != null) {
                    Double pr = ((double) currentRow++ / (double) count) * 100d;
                    mProgressEventsListener.onProgressChange(pr.intValue());
                }
            }
        }
        cursor.close();
        /*
        0 - Всего смс
        1 - Создано транзакций
        2 - Пропущено дублирующихся транзакций
        3 - Создано входящих смс
        4 - Пропущено дублирующихся смс
        */
        int stats[] = new int[]{count, trCreated, trSkipped, smsCreated, smsSkipped};

        if (mProgressEventsListener != null) {
            mProgressEventsListener.onOperationComplete(IProgressEventsListener.CODE_OK, stats);
        }
    }
}
