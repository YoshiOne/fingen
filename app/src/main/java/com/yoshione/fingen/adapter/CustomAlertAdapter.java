package com.yoshione.fingen.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.yoshione.fingen.R;

import java.util.ArrayList;
import java.util.Currency;

/**
 * Created by slv on 06.12.2017.
 *
 */

public class CustomAlertAdapter extends BaseAdapter {
    Context ctx = null;
    ArrayList<Currency> listarray = null;
    private LayoutInflater mInflater = null;

    public CustomAlertAdapter(Activity activty, ArrayList<Currency> list) {
        this.ctx = activty;
        mInflater = activty.getLayoutInflater();
        this.listarray = list;
    }

    @Override
    public int getCount() {

        return listarray.size();
    }

    @Override
    public Object getItem(int arg0) {
        return listarray.get(arg0);
    }

    @Override
    public long getItemId(int arg0) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup arg2) {
        final ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.alertlistrow, null);

            holder.titlename = convertView.findViewById(R.id.textView_titllename);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        String datavalue = String.format("%s (%s)", listarray.get(position).getDisplayName(), listarray.get(position).getCurrencyCode());
        holder.titlename.setText(datavalue);

        return convertView;
    }

    private static class ViewHolder {
        TextView titlename;
    }
}