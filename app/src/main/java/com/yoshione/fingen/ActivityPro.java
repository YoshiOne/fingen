package com.yoshione.fingen;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.yoshione.fingen.adapter.AdapterSku;
import com.yoshione.fingen.iab.BillingService;
import com.yoshione.fingen.iab.models.ReportsItem;
import com.yoshione.fingen.widgets.ToolbarActivity;

import java.util.ArrayList;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class ActivityPro extends ToolbarActivity {

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;
    boolean isBillingAvailable;
    @Inject
    BillingService mBillingService;
    private AdapterSku mAdapterSku;

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

        FGApplication.getAppComponent().inject(this);

        isBillingAvailable = mBillingService.isBillingAvailable();
        Log.d(TAG, String.format("isBillingAvailable == %s", String.valueOf(isBillingAvailable)));

        if (!isBillingAvailable) return;

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        mAdapterSku = new AdapterSku(new ArrayList<>(), ContextCompat.getColor(this, R.color.positive_color));
        mAdapterSku.setHasStableIds(true);
        recyclerView.setAdapter(mAdapterSku);
        recyclerView.setHasFixedSize(false);
    }

    @Override
    public void onResume() {
        super.onResume();
        updateRW();
    }

    private void updateRW() {
        if (!isBillingAvailable) return;
        mAdapterSku.getItemList().clear();
        unsubscribeOnDestroy(
                mBillingService.getReportsIapInfo()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(skuDetailsWrapper -> {
                            if (skuDetailsWrapper.getSkuDetails() != null) {
                                ReportsItem reportsItem = new ReportsItem(skuDetailsWrapper, R.drawable.ic_chart_gray, view -> {
                                    if (!skuDetailsWrapper.isPurchased()) {
                                        mBillingService.getBillingProcessor().purchase(ActivityPro.this, skuDetailsWrapper.getSkuDetails().productId);
                                    } else {
//                                        unsubscribeOnDestroy(
//                                                mBillingService.consumePurchase(skuDetailsWrapper.getSkuDetails().productId)
//                                                        .subscribeOn(Schedulers.io())
//                                                        .observeOn(AndroidSchedulers.mainThread())
//                                                        .subscribe(() -> mAdapterSku.notifyDataSetChanged()));
                                    }
                                });
                                mAdapterSku.getItemList().add(reportsItem);
                                mAdapterSku.notifyDataSetChanged();
                            }
                        }, throwable -> mAdapterSku.notifyDataSetChanged()));
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!mBillingService.getBillingProcessor().handleActivityResult(requestCode, resultCode, data))
            super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
