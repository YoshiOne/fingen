package com.yoshione.fingen;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.dropbox.core.DbxException;
import com.dropbox.core.android.Auth;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.Metadata;
import com.dropbox.core.v2.users.FullAccount;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import android.util.Log;
import com.yoshione.fingen.dropbox.DropboxClient;
import com.yoshione.fingen.dropbox.UploadTask;
import com.yoshione.fingen.dropbox.UserAccountTask;
import com.yoshione.fingen.interfaces.IOnComplete;
import com.yoshione.fingen.utils.DateTimeFormatter;
import com.yoshione.fingen.utils.FileUtils;
import com.yoshione.fingen.widgets.ToolbarActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;

@RuntimePermissions
public class ActivityBackup extends ToolbarActivity {

    private static final String TAG = "ActivityBackup";

    private static final int MSG_SHOW_DIALOG = 0;
    private static final int MSG_DOWNLOAD_FILE = 1;

    @BindView(R.id.fabBackup)
    FloatingActionButton fabBackup;
    @BindView(R.id.fabRestore)
    FloatingActionButton fabRestore;
    @BindView(R.id.fabRestoreFromDropbox)
    FloatingActionButton fabRestoreFromDropbox;
    @BindView(R.id.fabMenu)
    FloatingActionMenu fabMenu;
    @BindView(R.id.switchCompatEnablePasswordProtection)
    SwitchCompat mSwitchCompatEnablePasswordProtection;
    @BindView(R.id.editTextPassword)
    EditText mEditTextPassword;
    @BindView(R.id.editTextDropboxAccount)
    EditText mEditTextDropboxAccount;
    @BindView(R.id.textInputLayoutDropboxAccount)
    TextInputLayout mTextInputLayoutDropboxAccount;
    @BindView(R.id.textViewLastBackupToDropbox)
    TextView mTextViewLastBackupToDropbox;
    @BindView(R.id.buttonLogoutFromDropbox)
    Button mButtonLogoutFromDropbox;

    private SharedPreferences prefs;
    private UpdateRwHandler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.bind(this);
        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        initFabMenu();
        initPasswordProtection();
        initDropbox();
        mHandler = new UpdateRwHandler(this);
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_backup;
    }

    @Override
    protected String getLayoutTitle() {
        return getString(R.string.ent_backup_data);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.findItem(R.id.action_go_home).setVisible(true);
        return true;

    }

    private void initPasswordProtection() {
        mSwitchCompatEnablePasswordProtection.setChecked(prefs.getBoolean("enable_backup_password", false));
        mSwitchCompatEnablePasswordProtection.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                String password = prefs.getString("backup_password", "");
                prefs.edit().putBoolean("enable_backup_password", isChecked & !password.isEmpty()).apply();
            }
        });
        mEditTextPassword.setText(prefs.getString("backup_password", ""));
        mEditTextPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String password = String.valueOf(charSequence);
                prefs.edit().putString("backup_password", password).apply();
                prefs.edit().putBoolean("enable_backup_password", mSwitchCompatEnablePasswordProtection.isChecked()
                        & !password.isEmpty()).apply();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    private void initDropbox() {
        getUserAccount();
        final SharedPreferences dropboxPrefs = getSharedPreferences("com.yoshione.fingen.dropbox", Context.MODE_PRIVATE);
        final String token = dropboxPrefs.getString("dropbox-token", null);
        mEditTextDropboxAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (token == null) {
                    Auth.startOAuth2Authentication(ActivityBackup.this, getString(R.string.DROPBOX_APP_KEY));
                }
            }
        });
        mButtonLogoutFromDropbox.setVisibility(token == null? View.GONE : View.VISIBLE);
        mButtonLogoutFromDropbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AsyncTask.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            DropboxClient.getClient(token).auth().tokenRevoke();
                        } catch (DbxException e) {
                            e.printStackTrace();
                        }
                    }
                });
                dropboxPrefs.edit().remove("dropbox-token").apply();
                dropboxPrefs.edit().remove(FgConst.PREF_DROPBOX_ACCOUNT).apply();
                initDropbox();
            }
        });
        initLastDropboxBackupField();
    }

    private void initLastDropboxBackupField() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        Long dateLong = preferences.getLong(FgConst.PREF_SHOW_LAST_SUCCESFUL_BACKUP_TO_DROPBOX, 0);
        String title = getString(R.string.ttl_last_backup_to_dropbox);
        if (dateLong == 0) {
            title = String.format("%s -", title);
        } else {
            Date date = new Date(dateLong);
            DateTimeFormatter dtf = DateTimeFormatter.getInstance(this);
            title = String.format("%s %s %s", title, dtf.getDateMediumString(date), dtf.getTimeShortString(date));
        }
        mTextViewLastBackupToDropbox.setText(title);
    }

    public void saveAccessToken() {
        String accessToken = Auth.getOAuth2Token(); //generate Access Token
        if (accessToken != null) {
            //Store accessToken in SharedPreferences
            SharedPreferences dropboxPrefs = getSharedPreferences("com.yoshione.fingen.dropbox", Context.MODE_PRIVATE);
            dropboxPrefs.edit().putString("dropbox-token", accessToken).apply();
        }
    }

    @Override
    public void onResume() {
        saveAccessToken();
        initDropbox();
        super.onResume();
    }

    protected void getUserAccount() {
        final SharedPreferences dropboxPrefs = getSharedPreferences("com.yoshione.fingen.dropbox", Context.MODE_PRIVATE);
        mEditTextDropboxAccount.setText(dropboxPrefs.getString(FgConst.PREF_DROPBOX_ACCOUNT, ""));
        String token = dropboxPrefs.getString("dropbox-token", null);
        if (token == null) return;
        new UserAccountTask(DropboxClient.getClient(token), new UserAccountTask.TaskDelegate() {
            @Override
            public void onAccountReceived(FullAccount account) {
                //Print account's info
                Log.d("User", account.getEmail());
                Log.d("User", account.getName().getDisplayName());
                Log.d("User", account.getAccountType().name());
                mEditTextDropboxAccount.setText(account.getEmail());
                dropboxPrefs.edit().putString(FgConst.PREF_DROPBOX_ACCOUNT, account.getEmail()).apply();
            }

            @Override
            public void onError(Exception error) {
                Log.d("User", "Error receiving account details.");
            }
        }).execute();
    }


    private void initFabMenu() {
        fabBackup.setImageDrawable(getDrawable(R.drawable.ic_backup_white));
        fabBackup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityBackupPermissionsDispatcher.backupDBWithPermissionCheck((ActivityBackup) v.getContext());
                fabMenu.close(true);
            }
        });
        fabRestore.setImageDrawable(getDrawable(R.drawable.ic_restore_white));
        fabRestore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityBackupPermissionsDispatcher.restoreDBWithPermissionCheck((ActivityBackup) v.getContext());
                fabMenu.close(true);
            }
        });
        fabRestoreFromDropbox.setImageDrawable(getDrawable(R.drawable.ic_dropbox_white));
        fabRestoreFromDropbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityBackupPermissionsDispatcher.restoreDBFromDropboxWithPermissionCheck((ActivityBackup) v.getContext());
                fabMenu.close(true);
            }
        });
        fabMenu.setClosedOnTouchOutside(true);
        fabMenu.getMenuIconView().setImageDrawable(getDrawable(R.drawable.ic_backup_and_restore_white));
    }

    @NeedsPermission({Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE})
    void backupDB() {
        SharedPreferences dropboxPrefs = getSharedPreferences("com.yoshione.fingen.dropbox", Context.MODE_PRIVATE);
        String token = dropboxPrefs.getString("dropbox-token", null);
        try {
            File zip = DBHelper.getInstance(getApplicationContext()).backupDB(true);
            if (token != null && zip != null) {
                new UploadTask(DropboxClient.getClient(token), zip, new IOnComplete() {
                    @Override
                    public void onComplete() {
                        Toast.makeText(ActivityBackup.this, "File uploaded successfully", Toast.LENGTH_SHORT).show();
                        SharedPreferences preferences = android.support.v7.preference.PreferenceManager.getDefaultSharedPreferences(ActivityBackup.this);
                        preferences.edit().putLong(FgConst.PREF_SHOW_LAST_SUCCESFUL_BACKUP_TO_DROPBOX, new Date().getTime()).apply();
                        initLastDropboxBackupField();
                    }
                }).execute();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @NeedsPermission({Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE})
    void restoreDB() {
        new FileSelectDialog(this).showSelectBackupDialog();
    }

    @NeedsPermission({Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE})
    void restoreDBFromDropbox() {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                SharedPreferences dropboxPrefs = getApplicationContext().getSharedPreferences("com.yoshione.fingen.dropbox", Context.MODE_PRIVATE);
                String token = dropboxPrefs.getString("dropbox-token", null);
                List<Metadata> metadataList;
                List<MetadataItem> items = new ArrayList<>();
                try {
                    metadataList = DropboxClient.getListFiles(DropboxClient.getClient(token));
                    for (int i = metadataList.size() - 1; i >= 0; i--) {
                        if (metadataList.get(i).getName().toLowerCase().contains(".zip")) {
                            items.add(new MetadataItem((FileMetadata) metadataList.get(i)));
                        }
                    }
                } catch (Exception e) {
                    Log.d(TAG, "Error read list of files from Dropbox");
                }
                mHandler.sendMessage(mHandler.obtainMessage(MSG_SHOW_DIALOG, items));
            }
        });
        t.start();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // NOTE: delegate the permission handling to generated method
        ActivityBackupPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    @OnShowRationale({Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE})
    void showRationaleForContact(PermissionRequest request) {
        // NOTE: Show a rationale to explain why the permission is needed, e.g. with a dialog.
        // Call proceed() or cancel() on the provided PermissionRequest to continue or abort
        showRationaleDialog(R.string.msg_permission_rw_external_storage_rationale, request);
    }

    @OnPermissionDenied({Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE})
    void onCameraDenied() {
        // NOTE: Deal with a denied permission, e.g. by showing specific UI
        // or disabling certain functionality
        Toast.makeText(this, R.string.msg_permission_rw_external_storage_denied, Toast.LENGTH_SHORT).show();
    }

    @OnNeverAskAgain({Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE})
    void onCameraNeverAskAgain() {
        Toast.makeText(this, R.string.msg_permission_rw_external_storage_never_askagain, Toast.LENGTH_SHORT).show();
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
                .setMessage(R.string.msg_permission_rw_external_storage_rationale)
                .show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        ActivityBackupPermissionsDispatcher.checkPermissionsWithPermissionCheck(this);
    }

    @NeedsPermission({Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE})
    void checkPermissions() {
        Log.d(TAG, "Check permissions");
    }

    private static class UpdateRwHandler extends Handler {
        WeakReference<ActivityBackup> mActivity;

        UpdateRwHandler(ActivityBackup activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            final ActivityBackup activity = mActivity.get();
            switch (msg.what) {
                case MSG_SHOW_DIALOG:
                    AlertDialog.Builder builderSingle = new AlertDialog.Builder(activity);
                    builderSingle.setTitle(activity.getResources().getString(R.string.ttl_select_db_file));

                    final ArrayAdapter<MetadataItem> arrayAdapter = new ArrayAdapter<>(activity, android.R.layout.select_dialog_singlechoice);
                    arrayAdapter.addAll((List<MetadataItem>) msg.obj);

                    builderSingle.setNegativeButton(
                            activity.getResources().getString(android.R.string.cancel),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });

                    builderSingle.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ListView lw = ((AlertDialog) dialog).getListView();
                            final MetadataItem item = (MetadataItem) lw.getAdapter().getItem(which);
                            Thread thread = new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    File file = new File(activity.getCacheDir(), item.mMetadata.getName());
                                    try {
                                        OutputStream outputStream = new FileOutputStream(file);
                                        SharedPreferences dropboxPrefs = activity.getSharedPreferences("com.yoshione.fingen.dropbox", Context.MODE_PRIVATE);
                                        String token = dropboxPrefs.getString("dropbox-token", null);
                                        DbxClientV2 dbxClient = DropboxClient.getClient(token);
                                        dbxClient.files().download(item.mMetadata.getPathLower(), item.mMetadata.getRev())
                                                .download(outputStream);
                                        activity.mHandler.sendMessage(activity.mHandler.obtainMessage(MSG_DOWNLOAD_FILE, file));
                                    } catch (DbxException e) {
                                        e.printStackTrace();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                            thread.start();
                        }
                    });
                    builderSingle.show();
                    break;
                case MSG_DOWNLOAD_FILE:
                    try {
                        DBHelper.getInstance(activity).showRestoreDialog(((File) msg.obj).getCanonicalPath(), activity);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
            }

        }
    }

    private class MetadataItem {
        private FileMetadata mMetadata;

        public MetadataItem(FileMetadata metadata) {
            mMetadata = metadata;
        }

        public String toString() {
            return mMetadata.getName();
        }
    }

    private class FileSelectDialog {
        final AppCompatActivity activity;

        FileSelectDialog(AppCompatActivity activity) {
            this.activity = activity;
        }

        void showSelectBackupDialog() {
            AlertDialog.Builder builderSingle = new AlertDialog.Builder(activity);
            builderSingle.setTitle(activity.getResources().getString(R.string.ttl_select_db_file));

            List<File> files = FileUtils.getListFiles(getApplicationContext(), new File(FileUtils.getExtFingenBackupFolder()), ".zip");
            List<String> names = new ArrayList<>();
            String path;
            for (int i = files.size() - 1; i >= 0; i--) {
                path = files.get(i).toString();
                names.add(path.substring(path.lastIndexOf("/") + 1));
            }

            final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(activity, android.R.layout.select_dialog_singlechoice);
            arrayAdapter.addAll(names);

            builderSingle.setNegativeButton(
                    activity.getResources().getString(android.R.string.cancel),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });

            builderSingle.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    ListView lw = ((AlertDialog) dialog).getListView();
                    String fileName = (String) lw.getAdapter().getItem(which);
                    DBHelper.getInstance(activity).showRestoreDialog(FileUtils.getExtFingenBackupFolder() + fileName, activity);
                }
            });
            builderSingle.show();
        }
    }
}
