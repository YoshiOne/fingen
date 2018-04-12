/*
 * Copyright (c) 2015.
 */

package com.yoshione.fingen.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.yoshione.fingen.managers.SmsManager;
import com.yoshione.fingen.model.Sms;
import com.yoshione.fingen.R;
import com.yoshione.fingen.utils.DateTimeFormatter;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Leonid on 21.11.2015.
 *
 */
public class AdapterSms extends RecyclerView.Adapter {

//    public List<Sms> getSmsList() {
//        return smsList;
//    }

    public void setSmsList(List<Sms> smsList) {
        this.smsList = smsList;
    }

    private List<Sms> smsList;
    private final Context context;
    private SmsEventsListener smsEventsListener;

    public void setSmsEventsListener(SmsEventsListener smsEventsListener) {
        this.smsEventsListener = smsEventsListener;
    }

    @Override
    public long getItemId(int position) {
        return smsList.get(position).getID();
    }

    //Конструктор
    public AdapterSms(List<Sms> sms, Context context) {
        this.context = context;
        smsList = sms;
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh;
        View v = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.list_item_sms, parent, false);

        vh = new SmsViewHolder(v);

        return vh;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        Sms sms = smsList.get(position);
        SmsViewHolder svh = ((SmsViewHolder) viewHolder);
        svh.itemView.setLongClickable(true);
        svh.textViewSender.setText(SmsManager.getSender(sms, context).toString());
        String date = String.format("%s %s", DateTimeFormatter.getInstance(context).getDateShortString(sms.getmDateTime()),
                DateTimeFormatter.getInstance(context).getTimeShortString(sms.getmDateTime()));
        svh.textViewDate.setText(date);
        svh.textViewBody.setText(sms.getmBody());
    }

    @Override
    public int getItemCount() {
        return smsList.size();
    }


    class SmsViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.sender)TextView textViewSender;
        @BindView(R.id.date)TextView textViewDate;
        @BindView(R.id.body)TextView textViewBody;


        SmsViewHolder(View v) {
            super(v);
            ButterKnife.bind(this,v);

            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v1) {
                    if (smsEventsListener != null) {
                        smsEventsListener.OnSmsClick(v1);
                    }
                }
            });
        }
    }

    public interface SmsEventsListener{
        void OnSmsClick(View view);
    }
}
