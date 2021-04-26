package com.yoshione.fingen;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.yoshione.fingen.widgets.ToolbarActivity;

import java.lang.ref.WeakReference;

import butterknife.BindView;
import de.psdev.licensesdialog.LicensesDialog;

public class ActivityAbout extends ToolbarActivity {

    private static final int MSG_REBUILD_START = 0;
    private static final int MSG_REBUILD_ERROR = 1;
    private static final int MSG_REBUILD_SUCCESS = 2;
    @BindView(R.id.textViewVersion)
    TextView mTextViewVersion;
    @BindView(R.id.textViewLicense)
    TextView mTextViewLicense;
    @BindView(R.id.buttonLicenses)
    TextView mButtonLicenses;
    @BindView(R.id.buttonChangeLog)
    TextView mButtonChangeLog;
    @BindView(R.id.buttonChangeLogX)
    TextView mButtonChangeLogX;
    @BindView(R.id.textViewSQLiteVersion)
    TextView mTextViewSQLiteVersion;
    @BindView(R.id.buttonRebuildDB)
    Button mButtonRebuildDB;
    private UpdateUIHandler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mHandler = new UpdateUIHandler(this);
        PackageInfo pInfo;
        String version;
        boolean isMod = false;
        try {
            pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            version = pInfo.versionName;
            if (version.contains("-X"))
                isMod = true;
        } catch (PackageManager.NameNotFoundException e) {
            version = "N/A";
        }
        mTextViewVersion.setText(String.format("%s: %s", getString(R.string.ttl_app_version), version));
        String s = getString(R.string.ttl_sqlite_version) + DBHelper.getInstance(getApplicationContext()).getSqliteVersion();
        mTextViewSQLiteVersion.setText(s);
        mButtonLicenses.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new LicensesDialog.Builder(ActivityAbout.this)
                        .setNotices(R.raw.license)
                        .build()
                        .showAppCompat();
            }
        });

        mButtonChangeLog.setOnClickListener(v -> FragmentChangelog.show(ActivityAbout.this, FragmentChangelog.CHANGELOG_DEFAULT));

        if (isMod) {
            mButtonChangeLogX.setOnClickListener(v -> FragmentChangelog.show(ActivityAbout.this, FragmentChangelog.CHANGELOG_X));
        } else {
            ((LinearLayout) mButtonChangeLogX.getParent()).setVisibility(View.GONE);
        }

        mButtonRebuildDB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        boolean error = false;
                        try {
                            mHandler.sendMessage(mHandler.obtainMessage(MSG_REBUILD_START));
                            DBHelper.getInstance(getApplicationContext()).rebuildDB();
                        } catch (Exception e) {
                            error = true;
                            mHandler.sendMessage(mHandler.obtainMessage(MSG_REBUILD_ERROR));
                        } finally {
                            if (!error) {
                                mHandler.sendMessage(mHandler.obtainMessage(MSG_REBUILD_SUCCESS));
                            }
                        }
                    }
                });
                thread.start();
            }
        });
    }

    private static class UpdateUIHandler extends Handler {
        WeakReference<ActivityAbout> mActivity;

        UpdateUIHandler(ActivityAbout activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            final ActivityAbout activity = mActivity.get();

            FGApplication app = (FGApplication) activity.getApplication();
            switch (msg.what) {
                case MSG_REBUILD_START:
                    app.getDialog().setMessage(app.getString(R.string.msg_db_upgrade));
                    app.getDialog().show();
                    break;
                case MSG_REBUILD_ERROR:
                    app.getDialog().hide();
                    Toast.makeText(activity, R.string.msg_db_rebuild_error, Toast.LENGTH_SHORT).show();
                    break;
                case MSG_REBUILD_SUCCESS:
                    app.getDialog().hide();
                    Toast.makeText(activity, R.string.msg_db_rebuild_success, Toast.LENGTH_SHORT).show();
                    break;
            }

        }
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_about;
    }

    @Override
    protected String getLayoutTitle() {
        return getString(R.string.ent_about);
    }
}
