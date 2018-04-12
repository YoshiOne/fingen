package com.yoshione.fingen.adapter.viewholders;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.TextView;

import com.yoshione.fingen.R;
import com.yoshione.fingen.model.Product;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by slv on 25.12.2015.
 *
 */
public class VhProduct extends VhModelBase {
    @BindView(R.id.textViewName)
    TextView textViewName;
    Context mContext;

    public VhProduct(View itemView, IOnItemClickListener onItemClickListener, Context context) {
        super(itemView, onItemClickListener, context);
        ButterKnife.bind(this, itemView);
        mContext = context;
    }

    public void bindViewHolder(List<?> mModelList, int position) {
        super.bindViewHolder(mModelList, position);
        Product product = (Product) mModelList.get(position);
        textViewName.setText(product.getName());
    }
}
