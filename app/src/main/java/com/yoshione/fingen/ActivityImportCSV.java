package com.yoshione.fingen;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.SwitchCompat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import com.daimajia.numberprogressbar.NumberProgressBar;
import com.github.angads25.filepicker.controller.DialogSelectionListener;
import com.github.angads25.filepicker.model.DialogConfigs;
import com.github.angads25.filepicker.model.DialogProperties;
import com.github.angads25.filepicker.view.FilePickerDialog;
import android.util.Log;
import com.yoshione.fingen.interfaces.IProgressEventsListener;
import com.yoshione.fingen.utils.CsvImporter;
import com.yoshione.fingen.utils.FileUtils;
import com.yoshione.fingen.widgets.ToolbarActivity;

import java.io.File;
import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;

@RuntimePermissions
public class ActivityImportCSV extends ToolbarActivity implements IProgressEventsListener {

    private final static int HANDLER_OPERATION_HIDE = 0;
    private final static int HANDLER_OPERATION_SHOW = 1;
    private final static int HANDLER_OPERATION_UPDATE = 2;
    private final static int HANDLER_OPERATION_TOAST = 3;
    private static final NumberProgressBar[] numberProgressBarArr = new NumberProgressBar[]{null};
    private static final Activity[] activityArr = new Activity[]{null};
    @BindView(R.id.editTextFileName)
    EditText editTextFileName;
    @BindView(R.id.progressbar)
    NumberProgressBar progressbar;
    @BindView(R.id.switchCompatSkipDuplicates)
    SwitchCompat mSwitchCompatSkipDuplicates;
    UpdateProgressHandler handler;
    private int mCurrentProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.bind(this);

        progressbar.setProgress(0);
        numberProgressBarArr[0] = progressbar;
        activityArr[0] = this;
        mCurrentProgress = 0;
        handler = new UpdateProgressHandler();

        mSwitchCompatSkipDuplicates.setChecked(PreferenceManager.getDefaultSharedPreferences(this).getBoolean("financisto_csv_skip_diplicates", false));
        mSwitchCompatSkipDuplicates.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putBoolean("financisto_csv_skip_diplicates", b).apply();
            }
        });
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_import_csv;
    }

    @Override
    protected String getLayoutTitle() {
        if (getIntent().getStringExtra("type").equals("fingen")) {
            return getString(R.string.ent_import_csv_fingen);
        } else {
            return getString(R.string.ent_import_csv_financisto);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_import, menu);
        menu.findItem(R.id.action_go_home).setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_import :
                ActivityImportCSVPermissionsDispatcher.importCSVWithPermissionCheck(ActivityImportCSV.this);
                return true;
            default:
                return false;
        }
    }

    @OnClick(R.id.editTextFileName)
    void OnSelectFileClick() {
        ActivityImportCSVPermissionsDispatcher.SelectFileWithPermissionCheck(ActivityImportCSV.this);
    }

    @NeedsPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
    void SelectFile() {
        FileUtils.SelectFileFromStorage(ActivityImportCSV.this, DialogConfigs.FILE_SELECT, new FileUtils.IOnSelectFile() {
            @Override
            public void OnSelectFile(String FileName) {
                editTextFileName.setText(FileName);
            }
        });
    }

    @NeedsPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
    void importCSV() {
        String path = editTextFileName.getText().toString();
        File f = new File(path);
        if (!f.exists()) {
            Toast.makeText(ActivityImportCSV.this, getString(R.string.msg_file_not_exist), Toast.LENGTH_SHORT).show();
            return;
        }

        final CsvImporter csvImporter = new CsvImporter(this, path, 0, false);

        csvImporter.setmCsvImportProgressChangeListener(this);

        handler.sendMessage(handler.obtainMessage(HANDLER_OPERATION_SHOW, 0, 0));
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (getIntent().getStringExtra("type").equals("fingen")) {
                        csvImporter.loadFingenCSV();
                    } else {
                        csvImporter.loadFinancistoCSV();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        t.start();
    }

    @OnShowRationale(Manifest.permission.READ_EXTERNAL_STORAGE)
    void showRationaleForReadExternalStorage(PermissionRequest request) {
        // NOTE: Show a rationale to explain why the permission is needed, e.g. with a dialog.
        // Call proceed() or cancel() on the provided PermissionRequest to continue or abort
        showRationaleDialog(R.string.msg_permission_read_external_storage_rationale, request);
    }

    @OnPermissionDenied(Manifest.permission.READ_EXTERNAL_STORAGE)
    void onReadExternalStorageDenied() {
        // NOTE: Deal with a denied permission, e.g. by showing specific UI
        // or disabling certain functionality
        Toast.makeText(this, R.string.msg_permission_read_external_storage_denied, Toast.LENGTH_SHORT).show();
    }

    @OnNeverAskAgain(Manifest.permission.READ_EXTERNAL_STORAGE)
    void onReadExternalStorageNeverAskAgain() {
        Toast.makeText(this, R.string.msg_permission_read_external_storage_never_askagain, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // NOTE: delegate the permission handling to generated method
        ActivityImportCSVPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    private void showRationaleDialog(@StringRes int messageResId, final PermissionRequest request) {
        new AlertDialog.Builder(this)
                .setPositiveButton(R.string.act_next, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        request.proceed();
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        request.cancel();
                    }
                })
                .setCancelable(false)
                .setMessage(messageResId)
                .show();
    }

    @Override
    public void onProgressChange(int progress) {
//        Log.d("csv", String.format("enter listener progress = %d", progress));
        if (progress != mCurrentProgress) {
            Log.d("csv", "progress != mCurrentProgress");
            handler.sendMessage(handler.obtainMessage(HANDLER_OPERATION_UPDATE, progress, 0));
            mCurrentProgress = progress;
        }
    }

    @Override
    public void onOperationComplete(int code) {
        handler.sendMessage(handler.obtainMessage(HANDLER_OPERATION_TOAST, code, 0));
        Intent intent = new Intent(this, ActivityMain.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra("action", ActivityMain.ACTION_OPEN_TRANSACTIONS_LIST);
        startActivity(intent);
    }

    @Override
    public void onOperationComplete(int code, int[] stats) {
        onOperationComplete(code);
    }

    private static class UpdateProgressHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case HANDLER_OPERATION_SHOW:
                    numberProgressBarArr[0].setVisibility(View.VISIBLE);
                    break;
                case HANDLER_OPERATION_UPDATE:
                    numberProgressBarArr[0].setProgress(msg.arg1);
                    break;
                case HANDLER_OPERATION_HIDE:
                    break;
                case HANDLER_OPERATION_TOAST:
                    switch (msg.arg1) {
                        case 0:
                            Toast.makeText(activityArr[0], activityArr[0].getString(R.string.msg_import_complete_ok), Toast.LENGTH_SHORT).show();
                            break;
                        case 1:
                            Toast.makeText(activityArr[0], activityArr[0].getString(R.string.msg_import_complete_error_1), Toast.LENGTH_SHORT).show();
                            break;
                    }
                    break;
            }
        }
    }
}
