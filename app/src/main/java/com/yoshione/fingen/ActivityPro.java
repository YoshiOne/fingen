package com.yoshione.fingen;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.SkuDetails;
import com.anjlab.android.iab.v3.TransactionDetails;
import android.util.Log;
import com.yoshione.fingen.adapter.AdapterSku;
import com.yoshione.fingen.model.SkuDetailsItem;
import com.yoshione.fingen.utils.IconGenerator;
import com.yoshione.fingen.utils.InApp;
import com.yoshione.fingen.widgets.ToolbarActivity;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.anjlab.android.iab.v3.Constants.BILLING_RESPONSE_RESULT_USER_CANCELED;

public class ActivityPro extends ToolbarActivity {

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;
    private BillingProcessor mBillingProcessor = null;
    private AdapterSku mAdapterSku;
    private static final ActivityPro[] ACTIVITY_PROS = new ActivityPro[]{null};
    private UpdateHandler mHandler;

    private static final int HANDLER_OPERATION_UPDATE = 0;
    private static final int HANDLER_OPERATION_ERROR = 1;
    private static final int HANDLER_OPERATION_THANKS = 2;

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_pro;
    }

    @Override
    protected String getLayoutTitle() {
        return getString(R.string.ent_pro_features);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.bind(this);

        ACTIVITY_PROS[0] = this;
        mHandler = new UpdateHandler();

        boolean isBillingAvailable = BillingProcessor.isIabServiceAvailable(this);
        Log.d(TAG, String.format("isBillingAvailable == %s", String.valueOf(isBillingAvailable)));

        if (!isBillingAvailable) return;

        mBillingProcessor = new BillingProcessor(getApplicationContext(), InApp.getDeveloperKey(), null, new BillingHandler());

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        mAdapterSku = new AdapterSku(new ArrayList<SkuDetailsItem>(), ContextCompat.getColor(this, R.color.positive_color));
        mAdapterSku.setHasStableIds(true);
        recyclerView.setAdapter(mAdapterSku);
        recyclerView.setHasFixedSize(false);
    }

    private void updateRW() {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "start updateRW");
                mAdapterSku.getSkuDetailsItemList().clear();
                SkuDetails details = mBillingProcessor.getPurchaseListingDetails(InApp.SKU_REPORTS);
                boolean skuPurchased;
                if (details != null) {
                    skuPurchased = mBillingProcessor.isPurchased(InApp.SKU_REPORTS);
                    Log.d(TAG, String.format("skuPurchased == %s", String.valueOf(skuPurchased)));
                    SkuDetailsItem skuDetailsItem = new SkuDetailsItem(details,
                            ActivityPro.this.getDrawable(R.drawable.ic_chart_gray), 0, skuPurchased,
                            new OnItemClickListener(details));
                    mAdapterSku.getSkuDetailsItemList().add(skuDetailsItem);
                } else {
                    Log.d(TAG, "details is null");
                }
                mHandler.sendMessage(mHandler.obtainMessage(HANDLER_OPERATION_UPDATE, 0, 0));
            }
        });
        t.start();
    }

    private class OnItemClickListener implements View.OnClickListener {
        private SkuDetails mSkuDetails;

        OnItemClickListener(SkuDetails skuDetails) {
            mSkuDetails = skuDetails;
        }

        @Override
        public void onClick(View v) {
            if (!mBillingProcessor.isPurchased(mSkuDetails.productId)) {
                mBillingProcessor.purchase(ActivityPro.this, mSkuDetails.productId);
            } else {
                if (BuildConfig.DEBUG) {
                    mBillingProcessor.consumePurchase(mSkuDetails.productId);
                }
                if (mSkuDetails.productId.equals(InApp.SKU_REPORTS)) {
                    mHandler.sendMessage(mHandler.obtainMessage(HANDLER_OPERATION_THANKS, InApp.SKU_REPORTS_ID, 0));
                }
            }
//            updateRW();
        }
    }


    private class BillingHandler implements BillingProcessor.IBillingHandler {
        @Override
        public void onProductPurchased(@NonNull String productId, TransactionDetails details) {
            updateRW();
            if (productId.equals(InApp.SKU_REPORTS)) {
                mHandler.sendMessage(mHandler.obtainMessage(HANDLER_OPERATION_THANKS, InApp.SKU_REPORTS_ID, 0));
            }
        }

        @Override
        public void onPurchaseHistoryRestored() {
            updateRW();
        }

        @Override
        public void onBillingError(int errorCode, Throwable error) {
            mHandler.sendMessage(mHandler.obtainMessage(HANDLER_OPERATION_ERROR, errorCode, 0));
        }

        @Override
        public void onBillingInitialized() {
            updateRW();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!mBillingProcessor.handleActivityResult(requestCode, resultCode, data))
            super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onDestroy() {
        if (mBillingProcessor != null)
            mBillingProcessor.release();

        super.onDestroy();
    }

    private static class UpdateHandler extends Handler {
        @SuppressLint("StringFormatInvalid")
        @Override
        public void handleMessage(Message msg) {
            if (ACTIVITY_PROS[0] != null && !ACTIVITY_PROS[0].isFinishing()) {
                switch (msg.what) {
                    case HANDLER_OPERATION_UPDATE:
                        ACTIVITY_PROS[0].mAdapterSku.notifyDataSetChanged();
                        break;
                    case HANDLER_OPERATION_ERROR:
                        if (msg.arg1 == BILLING_RESPONSE_RESULT_USER_CANCELED) return;
                        AlertDialog.Builder builder = new AlertDialog.Builder(ACTIVITY_PROS[0]);
                        builder.setTitle(R.string.ttl_error);
                        builder.setMessage(String.format(ACTIVITY_PROS[0].getString(R.string.ttl_billing_error), String.valueOf(msg.arg1)));
                        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });

                        builder.show();
                        break;
                    case HANDLER_OPERATION_THANKS:
                        String message;
                        switch (msg.arg1) {
                            case InApp.SKU_REPORTS_ID:
                                message = ACTIVITY_PROS[0].getString(R.string.msg_thanks_for_purchase_reports);
                                break;
                            default:
                                message = "";
                        }
                        AlertDialog.Builder builder1 = new AlertDialog.Builder(ACTIVITY_PROS[0]);
                        builder1.setMessage(message);
                        builder1.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });

                        builder1.show();
                        break;
                }
            }
        }
    }
}
