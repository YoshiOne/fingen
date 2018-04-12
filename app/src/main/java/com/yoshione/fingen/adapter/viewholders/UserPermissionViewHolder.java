package com.yoshione.fingen.adapter.viewholders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.yoshione.fingen.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by slv on 14.03.2017.
 * UserPermissionViewHolder
 */

public class UserPermissionViewHolder extends RecyclerView.ViewHolder {
    @BindView(R.id.imageViewIcon)
    ImageView imageViewIcon;
    @BindView(R.id.textViewEmail)
    TextView mTextViewEmail;
    @BindView(R.id.checkBoxRead)
    CheckBox mCheckBoxRead;
    @BindView(R.id.checkBoxWrite)
    CheckBox mCheckBoxWrite;

    public UserPermissionViewHolder(View itemView) {
        super(itemView);
        itemView.setLongClickable(true);
        ButterKnife.bind(this, itemView);
    }

    public void setEmail(String email) {
        mTextViewEmail.setText(email);
    }

    public void setRead(boolean read) {
        mCheckBoxRead.setChecked(read);
    }

    public void setWrite(boolean write) {
        mCheckBoxWrite.setChecked(write);
    }

    public void setOnReadChangeListener(CompoundButton.OnCheckedChangeListener listener) {
        mCheckBoxRead.setOnCheckedChangeListener(listener);
    }

    public void setOnWriteChangeListener(CompoundButton.OnCheckedChangeListener listener) {
        mCheckBoxWrite.setOnCheckedChangeListener(listener);
    }
}
