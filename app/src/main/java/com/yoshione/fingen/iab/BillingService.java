package com.yoshione.fingen.iab;

import android.content.Context;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.NonNull;

import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.TransactionDetails;
import com.yoshione.fingen.FGApplication;
import com.yoshione.fingen.iab.models.SkuDetailsWrapper;

import io.reactivex.Completable;
import io.reactivex.Single;

public class BillingService {
    private final static String SKU_REPORTS = "fingen.reports";

    private IBillingEventsListener mBillingEventsListener;
    private BillingProcessor mBillingProcessor;

    public BillingService() {
        FGApplication.getAppComponent().inject(this);
        mBillingProcessor = new BillingProcessor(FGApplication.getContext(), getDeveloperKey(), null, new BillingHandler());
    }

    public BillingProcessor getBillingProcessor() {
        return mBillingProcessor;
    }

    public Completable consumePurchase(final String productID) {
        return Completable.fromAction(() -> {
            mBillingProcessor.consumePurchase(productID);
            Thread.sleep(5000);
        });
    }

    public boolean isBillingAvailable() {
        return BillingProcessor.isIabServiceAvailable(FGApplication.getAppComponent().getContext());
    }

    public Single<SkuDetailsWrapper> getReportsIapInfo() {
        return Single.fromCallable(() -> {
            SkuDetailsWrapper skuDetailsWrapper = new SkuDetailsWrapper();
            skuDetailsWrapper.setSkuDetails(mBillingProcessor.getPurchaseListingDetails(SKU_REPORTS));
            skuDetailsWrapper.setPurchased(mBillingProcessor.isPurchased(SKU_REPORTS));
            return skuDetailsWrapper;
        });
    }

    public void setBillingEventsListener(IBillingEventsListener billingEventsListener) {
        mBillingEventsListener = billingEventsListener;
    }

    private class BillingHandler implements BillingProcessor.IBillingHandler {
        @Override
        public void onProductPurchased(@NonNull String productId, TransactionDetails details) {
            if (mBillingEventsListener != null) {
                mBillingEventsListener.onProductPurchased(productId, details);
            }
        }

        @Override
        public void onPurchaseHistoryRestored() {

        }

        @Override
        public void onBillingError(int errorCode, Throwable error) {
            if (mBillingEventsListener != null) {
                mBillingEventsListener.onBillingError(errorCode, error);
            }
        }

        @Override
        public void onBillingInitialized() {
            Log.d(getClass().getName(), "Billing initialized");
        }
    }

    private static String getDeveloperKey() {
        Context context = FGApplication.getContext();
        int resId = context.getResources().getIdentifier("inAppLicenseKey", "string", context.getPackageName());
        String key = context.getString(resId);
        resId = context.getResources().getIdentifier("inAppLicenseSalt", "string", context.getPackageName());
        String salt = context.getString(resId);
        return fromX(key, salt);
    }

    /**
     * Method deciphers previously ciphered message
     *
     * @param message ciphered message
     * @param salt    salt which was used for ciphering
     * @return deciphered message
     */
    @NonNull
    private static String fromX(@NonNull String message, @NonNull String salt) {
        return x(new String(Base64.decode(message, 0)), salt);
    }

    /**
     * Method ciphers message. Later {@link #fromX} method might be used for deciphering
     *
     * @param message message to be ciphered
     * @param salt    salt to be used for ciphering
     * @return ciphered message
     */
    @NonNull
    private static String toX(@NonNull String message, @NonNull String salt) {
        return new String(Base64.encode(x(message, salt).getBytes(), 0));
    }

    /**
     * Symmetric algorithm used for ciphering/deciphering. Note that in your application you probably want to modify
     * algorithm used for ciphering/deciphering.
     *
     * @param message message
     * @param salt    salt
     * @return ciphered/deciphered message
     */
    @NonNull
    private static String x(@NonNull String message, @NonNull String salt) {
        final char[] m = message.toCharArray();
        final char[] s = salt.toCharArray();

        final int ml = m.length;
        final int sl = s.length;
        final char[] result = new char[ml];

        for (int i = 0; i < ml; i++) {
            result[i] = (char) (m[i] ^ s[i % sl]);
        }
        return new String(result);
    }
}
