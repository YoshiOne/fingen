/*
 * Copyright (c) 2015.
 */
package com.yoshione.fingen;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.view.Gravity;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import com.evernote.android.job.JobManager;
import com.github.omadahealth.lollipin.lib.managers.LockManager;
import android.util.Log;
import com.yoshione.fingen.backup.BackupJobCreator;
import com.yoshione.fingen.backup.BackupTestJobCreator;
import com.yoshione.fingen.fts.FtsApi;
import com.yoshione.fingen.interfaces.ISyncAnimMethods;
import com.yoshione.fingen.widgets.CustomPinActivity;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class FGApplication extends Application implements ISyncAnimMethods {

    private static String TAG = "FGApplication";
    public static final int MSG_SHOW_ANIM = 1;
    public static final int MSG_HIDE_ANIM = 2;
    public static final int MSG_SHOW_DIALOG = 3;
    public static final int MSG_HIDE_DIALOG = 4;

    boolean mBound = false;
    ServiceConnection sConn;
    Intent intent;

    // Popup to show persistent view
    public PopupWindow mPopupWindow;
    // View held by Popup
    public LinearLayout mLinearLayout;
    ImageView mImageView;

    public AlertDialog getDialog() {
        return mDialog;
    }

    AlertDialog mDialog;
    RotateAnimation mSpinAnim;

    public boolean mAppPaused = true;

    public UpdateUIHandler mUpdateUIHandler;

    public static FtsApi getFtsApi() {
        return sFtsApi;
    }

    private static FtsApi sFtsApi;

    private static FGApplication mContext;

    public static FGApplication getContext() {
        return mContext;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onCreate() {
//        Debug.startMethodTracing("Startup");
        super.onCreate();

        mContext = this;

        JobManager.create(this).addJobCreator(new BackupJobCreator());
        JobManager.create(this).addJobCreator(new BackupTestJobCreator());

        mUpdateUIHandler = new UpdateUIHandler(this);

        mAppPaused = true;

        LockManager<CustomPinActivity> lockManager = LockManager.getInstance();
        lockManager.enableAppLock(this, CustomPinActivity.class);
        lockManager.getAppLock().setOnlyBackgroundTimeout(true);
        lockManager.getAppLock().setLogoId(R.drawable.ic_main);

        registerActivityLifecycleCallbacks(new FgActivityLifecycleCallbacks());

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(1, TimeUnit.MINUTES)
                .readTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .addInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Interceptor.Chain chain) throws IOException {
                        Request request = chain.request();
                        Response response = chain.proceed(request);
//                        if (response.code() == 403) {
//                            handleForbiddenResponse();
//                        }
                        return response;
                    }
                })
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://proverkacheka.nalog.ru:8888/")
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClient)
                .build();
        sFtsApi = retrofit.create(FtsApi.class);
    }

    @Override
    public void onTerminate() {
        if (mBound) {
            unbindService(sConn);
            mBound = false;
        }
        stopService(intent);
        super.onTerminate();
    }

    private final class FgActivityLifecycleCallbacks implements ActivityLifecycleCallbacks {

        int numOfRunning = 0;

        @Override
        public void onActivityCreated(Activity arg0, Bundle arg1) {
            switch (Integer.valueOf(PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("theme", "0"))) {
                case ActivityMain.THEME_LIGHT:
                    arg0.setTheme(R.style.AppThemeLight);
                    break;
                case ActivityMain.THEME_DARK:
                    arg0.setTheme(R.style.AppThemeDark);
                    break;
                default:
                    arg0.setTheme(R.style.AppThemeLight);
                    break;
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(arg0)
//                    .setMessage(getString(R.string.msg_db_upgrade))
                    .setCancelable(false);
            mDialog = builder.create();
        }

        @Override
        public void onActivityDestroyed(Activity arg0) { }

        @Override
        public void onActivityPaused(Activity arg0) {

            // An activity has been paused
            // Decrement count, but wait for a certain
            // period of time, in case another activity
            // from this application is being launched
            numOfRunning--;

            // Delay: 100 ms
            // If no activity's onResumed() was called,
            // its safe to assume that the application
            // has been paused, in which case, dismiss
            // the popup
            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    if (numOfRunning == 0) {
                        hideSyncAnim();
                        mAppPaused = true;
                    }
                }
            }, 100L);
        }

        @Override
        public void onActivityResumed(Activity arg0) {

            // If no activities were running, show the popup
            if (numOfRunning == 0) {
//                mPopupWindow.showAtLocation(mLinearLayout, Gravity.BOTTOM, 0, 0);
            }
            mAppPaused = false;
            // Now, one activity is running
            numOfRunning++;
        }

        @Override
        public void onActivitySaveInstanceState(Activity arg0, Bundle arg1) { }

        @Override
        public void onActivityStarted(Activity arg0) { }

        @Override
        public void onActivityStopped(Activity arg0) { }

    }

    public void showSyncAnim() {
        if (!mAppPaused) {
            mPopupWindow.showAtLocation(mLinearLayout, Gravity.BOTTOM, 0, 0);
            mImageView.startAnimation(mSpinAnim);
        }
    }

    public void hideSyncAnim() {
        if (mImageView != null) {
            mImageView.clearAnimation();
            mPopupWindow.dismiss();
        }
    }

    public static class UpdateUIHandler extends Handler {
        WeakReference<FGApplication> mReference;

        UpdateUIHandler(FGApplication activity) {
            mReference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            final FGApplication app = mReference.get();

            switch (msg.what) {
                case MSG_SHOW_ANIM:
//                    app.showSyncAnim();
//                    Toast.makeText(app, "Testttttttttttttt", Toast.LENGTH_SHORT).show();
                    break;
                case MSG_HIDE_ANIM:
//                    app.hideSyncAnim();
//                    Toast.makeText(activity, R.string.msg_db_rebuild_error, Toast.LENGTH_SHORT).show();
                    break;
                case MSG_SHOW_DIALOG:
//                    AlertDialog.Builder builder = new AlertDialog.Builder(app)
//                            .setMessage("UPGRADE")
//                            .setCancelable(false);
//                    app.mDialog = builder.create();
                    app.mDialog.show();
                    break;
                case MSG_HIDE_DIALOG:
                    app.mDialog.setMessage((CharSequence) msg.obj);
                    app.mDialog.dismiss();
                    break;
            }

        }
    }
}
