package com.yoshione.fingen;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.daimajia.numberprogressbar.NumberProgressBar;
import com.github.angads25.filepicker.model.DialogConfigs;
import com.yoshione.fingen.dao.TransactionsDAO;
import com.yoshione.fingen.interfaces.IProgressEventsListener;
import com.yoshione.fingen.model.Transaction;
import com.yoshione.fingen.utils.CsvImporter;
import com.yoshione.fingen.utils.FileUtils;
import com.yoshione.fingen.widgets.ToolbarActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

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
public class ActivityExportCSV extends ToolbarActivity implements IProgressEventsListener {

    private final static int HANDLER_OPERATION_HIDE = 0;
    private final static int HANDLER_OPERATION_SHOW = 1;
    private final static int HANDLER_OPERATION_UPDATE = 2;
    private final static int HANDLER_OPERATION_TOAST = 3;
    private static final NumberProgressBar[] numberProgressBarArr = new NumberProgressBar[]{null};
    private static final Activity[] activityArr = new Activity[]{null};
    @BindView(R.id.progressbar)
    NumberProgressBar progressbar;
    UpdateProgressHandler handler;
    @BindView(R.id.editTextDirectory)
    EditText mEditTextDirectory;
    @BindView(R.id.editTextFileName)
    EditText mEditTextFileName;
    @BindView(R.id.textInputLayoutDir)
    TextInputLayout mTextInputLayoutDir;
    @BindView(R.id.textInputLayoutFileName)
    TextInputLayout mTextInputLayoutFileName;
    private int mCurrentProgress;

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_export_csv;
    }

    @Override
    protected String getLayoutTitle() {
        return getString(R.string.ent_export_data);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.bind(this);

        progressbar.setProgress(0);
        numberProgressBarArr[0] = progressbar;
        activityArr[0] = this;
        mCurrentProgress = 0;
        handler = new UpdateProgressHandler();

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(ActivityExportCSV.this);

        mEditTextDirectory.setText(preferences.getString("export_dir", ""));
        mEditTextFileName.setText(preferences.getString("export_file", ""));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_export, menu);
        menu.findItem(R.id.action_go_home).setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_export:
                ActivityExportCSVPermissionsDispatcher.exportCSVWithPermissionCheck(ActivityExportCSV.this);
                return true;
            default:
                return false;
        }
    }

    @OnClick(R.id.editTextDirectory)
    void OnSelectFileClick() {
        ActivityExportCSVPermissionsDispatcher.SelectFileWithPermissionCheck(ActivityExportCSV.this);
    }

    @NeedsPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
    void SelectFile() {
        FileUtils.SelectFileFromStorage(ActivityExportCSV.this, DialogConfigs.FILE_AND_DIR_SELECT, new FileUtils.IOnSelectFile() {
            @Override
            public void OnSelectFile(String path) {
                String dir;
                String fn;
                File file = new File(path);
                if (file.isDirectory()) {
                    dir = path;
                    fn = "";
                } else {
                    dir = file.getParent();
                    fn = file.getName();
                }
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(ActivityExportCSV.this);
                preferences.edit().putString("export_dir", dir).putString("export_file", fn).apply();
                mEditTextDirectory.setText(dir);
                mEditTextFileName.setText(fn);
            }
        });
    }

    @NeedsPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    void exportCSV() {
        String dir = mEditTextDirectory.getText().toString();
        String fn = mEditTextFileName.getText().toString();

        if (dir.isEmpty()) {
            mTextInputLayoutDir.setError(getString(R.string.err_set_dir));
            return;
        }

        if (fn.isEmpty()) {
            mTextInputLayoutFileName.setError(getString(R.string.err_set_file_name));
            return;
        }

        if (!fn.toLowerCase().contains(".csv")) {
            fn = fn + ".csv";
        }

        String path = dir + "/" + fn;

        final CsvImporter csvImporter = new CsvImporter(this, path, 0, true);

        csvImporter.setmCsvImportProgressChangeListener(this);

        handler.sendMessage(handler.obtainMessage(HANDLER_OPERATION_SHOW, 0, 0));
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                List<Transaction> transactions;
                try {
                    transactions = getIntent().getParcelableArrayListExtra("transactions");
                    if (transactions == null) {
                        transactions = TransactionsDAO.getInstance(ActivityExportCSV.this).getAllTransactions();
                    }
                } catch (Exception e) {
                    transactions = new ArrayList<>();
                }
                csvImporter.saveCSV(transactions);
            }
        });
        t.start();
    }

    @OnShowRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    void showRationaleForWrite_external_storage(PermissionRequest request) {
        // NOTE: Show a rationale to explain why the permission is needed, e.g. with a dialog.
        // Call proceed() or cancel() on the provided PermissionRequest to continue or abort
        showRationaleDialog(R.string.msg_permission_write_external_storage_rationale, request);
    }

    @OnPermissionDenied(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    void onWrite_external_storageDenied() {
        // NOTE: Deal with a denied permission, e.g. by showing specific UI
        // or disabling certain functionality
        Toast.makeText(this, R.string.msg_permission_write_external_storage_denied, Toast.LENGTH_SHORT).show();
    }

    @OnNeverAskAgain(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    void onWrite_external_storageNeverAskAgain() {
        Toast.makeText(this, R.string.msg_permission_write_external_storage_never_askagain, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // NOTE: delegate the permission handling to generated method
        ActivityExportCSVPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
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
//            Log.d("csv", "progress != mCurrentProgress");
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
                            Toast.makeText(activityArr[0], activityArr[0].getString(R.string.msg_export_complete_ok), Toast.LENGTH_SHORT).show();
                            break;
                        case 1:
                            Toast.makeText(activityArr[0], activityArr[0].getString(R.string.msg_export_complete_error_1), Toast.LENGTH_SHORT).show();
                            break;
                    }
                    break;
            }
        }
    }
}
