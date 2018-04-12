package com.yoshione.fingen;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.numberprogressbar.NumberProgressBar;
import com.github.angads25.filepicker.model.DialogConfigs;
import com.yoshione.fingen.adapter.AdapterColumnIndex;
import com.yoshione.fingen.interfaces.IProgressEventsListener;
import com.yoshione.fingen.model.EntityToFieldLink;
import com.yoshione.fingen.utils.CsvImporter;
import com.yoshione.fingen.utils.FileUtils;
import com.yoshione.fingen.utils.IconGenerator;
import com.yoshione.fingen.utils.ImportParams;
import com.yoshione.fingen.widgets.ToolbarActivity;

import java.io.File;
import java.io.IOException;
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
public class ActivityImportCSVAdvanced extends ToolbarActivity implements IProgressEventsListener {

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
    @BindView(R.id.recycler_view)
    RecyclerView mRecyclerView;
    @BindView(R.id.textView3)
    TextView mTextView3;
    @BindView(R.id.layoutRW)
    LinearLayout mLayoutRW;
    @BindView(R.id.textViewSkipLines)
    EditText mTextViewSkipLines;
    @BindView(R.id.imageButtonDec)
    ImageButton mImageButtonDec;
    @BindView(R.id.imageButtonInc)
    ImageButton mImageButtonInc;
    @BindView(R.id.textInputLayoutLines)
    TextInputLayout mTextInputLayoutLines;
    @BindView(R.id.switchCompatSkipDuplicates)
    SwitchCompat mSwitchCompatSkipDuplicates;
    UpdateProgressHandler handler;
    private int mCurrentProgress;
    private int mSkipLines = 0;

    private AdapterColumnIndex mAdapterColumnIndex;

    private String mPath = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.bind(this);

        progressbar.setProgress(0);
        mCurrentProgress = 0;
        handler = new UpdateProgressHandler();
        numberProgressBarArr[0] = progressbar;
        activityArr[0] = this;

        editTextFileName.setInputType(InputType.TYPE_NULL);

        mSwitchCompatSkipDuplicates.setChecked(PreferenceManager.getDefaultSharedPreferences(this).getBoolean("custom_csv_skip_diplicates", false));
        mSwitchCompatSkipDuplicates.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putBoolean("custom_csv_skip_diplicates", b).apply();
            }
        });

        mImageButtonDec.setImageDrawable(IconGenerator.getExpandIndicatorIcon(true, this));
        mImageButtonInc.setImageDrawable(IconGenerator.getExpandIndicatorIcon(false, this));
        updateSkipLinesField();
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_import_csv_advanced;
    }

    @Override
    protected String getLayoutTitle() {
        return getString(R.string.ent_import_csv_advanced);
    }

    private void updateSkipLinesField() {
        mTextViewSkipLines.setText(String.valueOf(mSkipLines));
    }

    @OnClick({R.id.imageButtonInc, R.id.imageButtonDec})
    void OnChangeSkipLinesClick(View view) {
        if (mPath.isEmpty()) return;
        switch (view.getId()) {
            case R.id.imageButtonInc:
                mSkipLines++;
                break;
            case R.id.imageButtonDec:
                if (mSkipLines > 0) {
                    mSkipLines--;
                }
                break;
        }
        updateSkipLinesField();
        initColumns();
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
                ActivityImportCSVAdvancedPermissionsDispatcher.importCSVWithPermissionCheck(ActivityImportCSVAdvanced.this);
                return true;
            default:
                return false;
        }
    }

    @OnClick(R.id.editTextFileName)
    void OnSelectFileClick() {
        ActivityImportCSVAdvancedPermissionsDispatcher.SelectFileWithPermissionCheck(ActivityImportCSVAdvanced.this);
    }

    @NeedsPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
    void SelectFile() {
        FileUtils.SelectFileFromStorage(ActivityImportCSVAdvanced.this, DialogConfigs.FILE_SELECT, new FileUtils.IOnSelectFile() {
            @Override
            public void OnSelectFile(String fileName) {
                mPath = fileName;
                initColumns();
            }
        });
    }


    @SuppressLint("InlinedApi")
    void initColumns() {
        editTextFileName.setText(mPath);
        if (mPath.isEmpty()) return;
        File f = new File(mPath);
        if (f.exists()) {
            CsvImporter csvImporter = new CsvImporter(getApplicationContext(), f.getPath(), mSkipLines, false);
            List<String> columns = new ArrayList<>();
            columns.add(0, "-");
            columns.addAll(csvImporter.loadColumnsFromCSV());

            LinearLayoutManager layoutManager = new LinearLayoutManager(this);
            mRecyclerView.setLayoutManager(layoutManager);

            List<EntityToFieldLink> entityToFieldLinkList = new ArrayList<>();
            for (int i : EntityToFieldLink.ENTITY_TYPES) {
                entityToFieldLinkList.add(new EntityToFieldLink(i));
            }

            mAdapterColumnIndex = new AdapterColumnIndex(entityToFieldLinkList, columns, this);
            mRecyclerView.setAdapter(mAdapterColumnIndex);
            mAdapterColumnIndex.notifyDataSetChanged();
            mLayoutRW.setVisibility(View.VISIBLE);
        }
    }

    @SuppressLint("InlinedApi")
    @NeedsPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
    void importCSV() {
        String path = editTextFileName.getText().toString();
        if (path.isEmpty()) return;
        final CsvImporter csvImporter = new CsvImporter(getApplicationContext(), path, mSkipLines, false);

        File f = new File(path);
        if (!f.exists()) {
            Toast.makeText(ActivityImportCSVAdvanced.this, getString(R.string.msg_file_not_exist), Toast.LENGTH_SHORT).show();
            return;
        }

        csvImporter.setmCsvImportProgressChangeListener(this);

        final ImportParams importParams = mAdapterColumnIndex.getImportParams();

        importParams.setDateFormat(csvImporter.detectDateFormat(importParams.date));

        handler.sendMessage(handler.obtainMessage(HANDLER_OPERATION_SHOW, 0, 0));
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    csvImporter.loadCustomCSV(importParams);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        t.start();
    }

    @SuppressLint("InlinedApi")
    @OnShowRationale(Manifest.permission.READ_EXTERNAL_STORAGE)
    void showRationaleForReadExternalStorage(PermissionRequest request) {
        // NOTE: Show a rationale to explain why the permission is needed, e.g. with a dialog.
        // Call proceed() or cancel() on the provided PermissionRequest to continue or abort
        showRationaleDialog(R.string.msg_permission_read_external_storage_rationale, request);
    }

    @SuppressLint("InlinedApi")
    @OnPermissionDenied(Manifest.permission.READ_EXTERNAL_STORAGE)
    void onReadExternalStorageDenied() {
        // NOTE: Deal with a denied permission, e.g. by showing specific UI
        // or disabling certain functionality
        mPath = "";
        Toast.makeText(this, R.string.msg_permission_read_external_storage_denied, Toast.LENGTH_SHORT).show();
    }

    @SuppressLint("InlinedApi")
    @OnNeverAskAgain(Manifest.permission.READ_EXTERNAL_STORAGE)
    void onReadExternalStorageNeverAskAgain() {
        mPath = "";
        Toast.makeText(this, R.string.msg_permission_read_external_storage_never_askagain, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // NOTE: delegate the permission handling to generated method
        ActivityImportCSVAdvancedPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
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
