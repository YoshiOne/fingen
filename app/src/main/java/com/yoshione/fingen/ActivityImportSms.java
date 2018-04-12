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
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatCheckBox;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.daimajia.numberprogressbar.NumberProgressBar;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;
import com.yoshione.fingen.dao.SendersDAO;
import com.yoshione.fingen.interfaces.IAbstractModel;
import com.yoshione.fingen.interfaces.IProgressEventsListener;
import com.yoshione.fingen.model.Sender;
import com.yoshione.fingen.utils.DateTimeFormatter;
import com.yoshione.fingen.utils.IconGenerator;
import com.yoshione.fingen.utils.RequestCodes;
import com.yoshione.fingen.utils.SmsImporter;
import com.yoshione.fingen.widgets.ToolbarActivity;

import java.util.Calendar;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;

/**
 * Created by Leonid on 28.02.2016.
 *
 */
@RuntimePermissions
public class ActivityImportSms extends ToolbarActivity implements IProgressEventsListener,
        TimePickerDialog.OnTimeSetListener,
        DatePickerDialog.OnDateSetListener {

    private final static int HANDLER_OPERATION_HIDE = 0;
    private final static int HANDLER_OPERATION_SHOW = 1;
    private final static int HANDLER_OPERATION_UPDATE = 2;
    private final static int HANDLER_OPERATION_COMPLETE = 3;

    @BindView(R.id.progressbar)
    NumberProgressBar progressbar;
    @BindView(R.id.editTextSender)
    EditText editTextSender;
    @BindView(R.id.editTextStartDate)
    EditText editTextStartDate;
    @BindView(R.id.editTextStartTime)
    EditText editTextStartTime;
    @BindView(R.id.editTextEndDate)
    EditText editTextEndDate;
    @BindView(R.id.editTextEndTime)
    EditText editTextEndTime;
    @BindView(R.id.checkboxAutoCreateTransactions)
    AppCompatCheckBox checkboxAutoCreateTransactions;

    private int mCurrentProgress;
    UpdateProgressHandler handler;
    private static final Activity[] activityArr = new Activity[]{null};
    private static final OnDialogOkListener[] onDialogOkListenerArr = new OnDialogOkListener[]{null};

    private Date mStartDate;
    private Date mEndDate;
    private int mTimeType;
    private Sender mSender;
    private static final NumberProgressBar[] progressbarArr = {null};

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_import_sms;
    }

    @Override
    protected String getLayoutTitle() {
        return getString(R.string.ent_import_sms);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.bind(this);

        progressbarArr[0] = progressbar;
        progressbar.setProgress(0);
        mCurrentProgress = 0;
        handler = new UpdateProgressHandler();
        activityArr[0] = this;
        onDialogOkListenerArr[0] = new OnDialogOkListener(this);

        long defaultSenderId = PreferenceManager.getDefaultSharedPreferences(this).getLong("import_sender_id", -1);
        mSender = SendersDAO.getInstance(this).getSenderByID(defaultSenderId);
        editTextSender.setText(mSender.toString());

        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        mStartDate = c.getTime();
        c.set(Calendar.HOUR_OF_DAY, 23);
        c.set(Calendar.MINUTE, 59);
        c.set(Calendar.SECOND, 59);
        mEndDate = c.getTime();

        initDates();
        handler = new UpdateProgressHandler();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_import, menu);
        menu.findItem(R.id.action_import).setIcon(getDrawable(R.drawable.ic_import_white));
        menu.findItem(R.id.action_go_home).setVisible(false);

        menu.findItem(R.id.action_import).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                ActivityImportSmsPermissionsDispatcher.importSmsWithPermissionCheck(ActivityImportSms.this);
                return true;
            }
        });

        return true;
    }

    private void initDates() {

//        SimpleDateFormat dateFormat = new SimpleDateFormat(getResources().getString(R.string.date_format_date));
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.getInstance(this);
        editTextStartDate.setText(dateTimeFormatter.getDateShortString(mStartDate));
        editTextEndDate.setText(dateTimeFormatter.getDateShortString(mEndDate));

        editTextStartTime.setText(dateTimeFormatter.getTimeShortString(mStartDate));
        editTextEndTime.setText(dateTimeFormatter.getTimeShortString(mEndDate));
    }

    @OnClick({R.id.editTextStartDate, R.id.editTextEndDate})
    public void onDateClick(View view) {
        Calendar calendar = Calendar.getInstance();
        switch (view.getId()) {
            case R.id.editTextStartDate:
                calendar.setTime(mStartDate);
                break;
            case R.id.editTextEndDate:
                calendar.setTime(mEndDate);
                break;
        }
        calendar.setTime(mStartDate);
        DatePickerDialog dpd = DatePickerDialog.newInstance(
                this,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        Bundle bundle = new Bundle();
        bundle.putInt("id", view.getId());
        dpd.setArguments(bundle);


        int theme = Integer.valueOf(PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("theme", "0"));
        dpd.setThemeDark(theme == ActivityMain.THEME_DARK);
        dpd.vibrate(false);
        dpd.dismissOnPause(false);
        dpd.show(getFragmentManager(), "Datepickerdialog");
    }

    @Override
    public void onDateSet(DatePickerDialog datePickerDialog, int i, int i1, int i2) {
        int id = datePickerDialog.getArguments().getInt("id");
        Calendar calendar = Calendar.getInstance();
        switch (id) {
            case R.id.editTextStartDate:
                calendar.setTime(mStartDate);
                calendar.set(i, i1, i2);
                mStartDate.setTime(calendar.getTimeInMillis());
                break;
            case R.id.editTextEndDate:
                calendar.setTime(mEndDate);
                calendar.set(i, i1, i2);
                mEndDate.setTime(calendar.getTimeInMillis());
                break;
        }
        initDates();
    }

    @OnClick({R.id.editTextStartTime, R.id.editTextEndTime})
    public void onTimeClick(View view) {
        Calendar calendar = Calendar.getInstance();
        switch (view.getId()) {
            case R.id.editTextStartTime:
                calendar.setTime(mStartDate);
                break;
            case R.id.editTextEndTime:
                calendar.setTime(mEndDate);
                break;
        }
        TimePickerDialog tpd = TimePickerDialog.newInstance(
                this,
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                DateTimeFormatter.is24(this)
        );
        mTimeType = view.getId();
        int theme = Integer.valueOf(PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("theme", "0"));
        tpd.setThemeDark(theme == ActivityMain.THEME_DARK);
        tpd.vibrate(false);
        tpd.dismissOnPause(false);
        tpd.show(getFragmentManager(), "Timepickerdialog");
    }

    @Override
    public void onTimeSet(TimePickerDialog view, int hourOfDay, int minute, int second) {
        Calendar calendar = Calendar.getInstance();
        switch (mTimeType) {
            case R.id.editTextStartTime:
                calendar.setTime(mStartDate);
                calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), hourOfDay, minute);
                mStartDate.setTime(calendar.getTimeInMillis());
                break;
            case R.id.editTextEndTime:
                calendar.setTime(mEndDate);
                calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), hourOfDay, minute);
                mEndDate.setTime(calendar.getTimeInMillis());
                break;
        }
        initDates();
    }

    @NeedsPermission(Manifest.permission.READ_SMS)
    void importSms() {
//        String sender = editTextSender.getText().toString();
        boolean autoCreate = checkboxAutoCreateTransactions.isChecked();
        if (mSender.getID() < 0) {
            Toast.makeText(this, getString(R.string.err_empty_sender), Toast.LENGTH_SHORT).show();
            return;
        }

        PreferenceManager.getDefaultSharedPreferences(this).edit().putLong("import_sender_id", mSender.getID()).apply();
        final SmsImporter smsImporter = new SmsImporter(this, mSender, autoCreate, mStartDate, mEndDate);
        smsImporter.setmProgressEventsListener(this);

        handler.sendMessage(handler.obtainMessage(HANDLER_OPERATION_SHOW, 0, 0));
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                smsImporter.importSms();
            }
        });
        t.start();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // NOTE: delegate the permission handling to generated method
        ActivityImportSmsPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    @OnShowRationale(Manifest.permission.READ_SMS)
    void showRationaleForCamera(PermissionRequest request) {
        // NOTE: Show a rationale to explain why the permission is needed, e.g. with a dialog.
        // Call proceed() or cancel() on the provided PermissionRequest to continue or abort
        showRationaleDialog(R.string.msg_permission_read_sms_rationale, request);
    }

    @OnPermissionDenied(Manifest.permission.READ_SMS)
    void onReadSmsDenied() {
        // NOTE: Deal with a denied permission, e.g. by showing specific UI
        // or disabling certain functionality
        Toast.makeText(this, R.string.msg_permission_read_sms_denied, Toast.LENGTH_SHORT).show();
    }

    @OnNeverAskAgain(Manifest.permission.READ_SMS)
    void onReadSmsNeverAskAgain() {
        Toast.makeText(this, R.string.msg_permission_read_sms_never_askagain, Toast.LENGTH_SHORT).show();
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
        if (progress != mCurrentProgress) {
            handler.sendMessage(handler.obtainMessage(HANDLER_OPERATION_UPDATE, progress, 0));
            mCurrentProgress = progress;
        }
    }

    @Override
    public void onOperationComplete(int code) {
//        handler.sendMessage(handler.obtainMessage(HANDLER_OPERATION_COMPLETE, code, 0));
        onOperationComplete(code, new int[]{0, 0, 0, 0, 0});
    }

    @Override
    public void onOperationComplete(int code, int[] stats) {
        String s = String.format("%s - %s\n%s - %s\n%s - %s\n%s - %s\n%s - %s",
                getString(R.string.ttl_total_sms_found), String.valueOf(stats[0]),
                getString(R.string.ttl_transactions_created), String.valueOf(stats[1]),
                getString(R.string.ttl_transactions_skipped), String.valueOf(stats[2]),
                getString(R.string.ttl_sms_created), String.valueOf(stats[3]),
                getString(R.string.ttl_sms_skipped), String.valueOf(stats[4]));
        handler.sendMessage(handler.obtainMessage(HANDLER_OPERATION_COMPLETE, 0, 0, s));
    }

    @OnClick(R.id.editTextSender)
    public void onClick() {
        Intent intent = new Intent(this.getApplicationContext(), ActivityList.class);
        intent.putExtra("showHomeButton", false);
        intent.putExtra("model", new Sender());
        intent.putExtra("requestCode", RequestCodes.REQUEST_CODE_SELECT_MODEL);
        this.startActivityForResult(intent, RequestCodes.REQUEST_CODE_SELECT_MODEL);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == RequestCodes.REQUEST_CODE_SELECT_MODEL) {
            if (data != null) {
                IAbstractModel model = data.getParcelableExtra("model");
                if (model.getModelType() == IAbstractModel.MODEL_TYPE_SENDER) {
                    mSender = (Sender) model;
                    editTextSender.setText(mSender.toString());
                }
            }
        }
    }

    private class OnDialogOkListener implements DialogInterface.OnClickListener {
        private final Activity mActivity;

        OnDialogOkListener(Activity mActivity) {
            this.mActivity = mActivity;
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            mActivity.finish();
        }
    }

    private static class UpdateProgressHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case HANDLER_OPERATION_SHOW:
                    progressbarArr[0].setVisibility(View.VISIBLE);
                    break;
                case HANDLER_OPERATION_UPDATE:
                    progressbarArr[0].setProgress(msg.arg1);
                    break;
                case HANDLER_OPERATION_HIDE:
                    break;
                case HANDLER_OPERATION_COMPLETE:
                    AlertDialog.Builder builder = new AlertDialog.Builder(activityArr[0]);
                    builder.setTitle(R.string.ttl_import_complete);

                    builder.setMessage((String) msg.obj);

                    // Set up the buttons
                    builder.setPositiveButton("OK", onDialogOkListenerArr[0]);

                    builder.show();
                    break;
            }
        }
    }
}
