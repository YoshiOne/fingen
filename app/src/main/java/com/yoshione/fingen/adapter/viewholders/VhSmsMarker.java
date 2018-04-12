package com.yoshione.fingen.adapter.viewholders;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.yoshione.fingen.managers.SmsMarkerManager;
import com.yoshione.fingen.model.SmsMarker;
import com.yoshione.fingen.R;
import com.yoshione.fingen.utils.SmsParser;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by slv on 25.12.2015.
 *
 */
public class VhSmsMarker extends VhModelBase {
    @BindView(R.id.textViewType)
    TextView textViewType;
    @BindView(R.id.textViewObject)
    TextView textViewObject;
    @BindView(R.id.textViewMarker)
    TextView textViewValue;
    @BindView(R.id.imageViewIcon)
    ImageView mImageViewIcon;
    Context mContext;

    public VhSmsMarker(View itemView, VhModelBase.IOnItemClickListener onItemClickListener, Context context) {
        super(itemView, onItemClickListener, context);
        ButterKnife.bind(this, itemView);
        mContext = context;
    }

    public void bindViewHolder(List<?> mModelList, int position) {
        super.bindViewHolder(mModelList, position);
        SmsMarker smsMarker = (SmsMarker) mModelList.get(position);
        String s[] = smsMarker.toString().split("~");
        if (s.length != 3) {
            return;
        }
        int type = Integer.valueOf(s[0]);
        String value = s[2];
        String typeName = SmsMarkerManager.getMarkerTypeName(type, mContext);
        textViewType.setText(typeName);
        textViewObject.setText(SmsMarkerManager.getObjectAsText(smsMarker, mContext));
        textViewValue.setText(value);
        switch (smsMarker.getType()) {
            case SmsParser.MARKER_TYPE_ACCOUNT:
                mImageViewIcon.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_account_gray));
                break;
            case SmsParser.MARKER_TYPE_CABBAGE:
                mImageViewIcon.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_currencies_gray));
                break;
            case SmsParser.MARKER_TYPE_TRTYPE:
                mImageViewIcon.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_transfer_gray));
                break;
            case SmsParser.MARKER_TYPE_PAYEE:
                mImageViewIcon.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_payes));
                break;
            case SmsParser.MARKER_TYPE_IGNORE:
                mImageViewIcon.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_block_gray));
                break;
        }
    }
}
