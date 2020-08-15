package com.yoshione.fingen.fts;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.google.android.material.snackbar.Snackbar;
import com.yoshione.fingen.FGApplication;
import com.yoshione.fingen.FgConst;
import com.yoshione.fingen.R;
import com.yoshione.fingen.managers.TransactionManager;
import com.yoshione.fingen.model.Transaction;
import com.yoshione.fingen.utils.ColorUtils;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

public class ActivityScanQR extends AppCompatActivity
        implements ActivityCompat.OnRequestPermissionsResultCallback {

    private static final String TAG = "ActivityScanQR";
    private static final int MY_PERMISSION_REQUEST_CAMERA = 0;
    private static final int RESULT_LOAD_IMAGE = 1;

    private ViewGroup mainLayout;

    private CameraSource mCameraSource;
    private SurfaceView qrCodeReaderView;
    private BarcodeDetector barcodeDetector;

    @Inject
    public SharedPreferences mPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_decoder);
        mainLayout = findViewById(R.id.main_layout);

        FGApplication.getAppComponent().inject(this);

        // clipboard worker copied from ActivitySmsList
        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        ClipData data = null;
        if (clipboard.hasPrimaryClip())
            data = clipboard.getPrimaryClip();
        if (data != null) {
            ClipData.Item item = data.getItemAt(0);

            String text = "";
            if (item != null) {
                try {
                    text = item.getText().toString();
                } catch (Exception e) {
                    Toast.makeText(this, getString(R.string.err_parse_clipboard), Toast.LENGTH_SHORT).show();
                }
            }

            Pattern pattern = Pattern.compile("^t=\\d+T\\d+&s=[\\d\\.]{4,12}&fn=\\d+&i=\\d+&fp=\\d+&n=\\d$", Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(text);

            if (!text.equals("") && matcher.find()) {
                final String qrCode = text;
                // show dialog copied from FragmentTransaction
                final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
                alertDialogBuilder
                        .setTitle(R.string.ttl_confirm_action)
                        .setMessage(R.string.msg_use_qr_from_buffer)
                        .setPositiveButton(R.string.ok, (dialog, which) ->this.onQRCodeRead(qrCode))
                        .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss())
                        .show();
            }
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            initQRCodeReaderView();
        } else {
            requestCameraPermission();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != MY_PERMISSION_REQUEST_CAMERA) {
            return;
        }

        Snackbar snackbar;
        if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            snackbar = Snackbar.make(mainLayout, getString(R.string.msg_camera_permission_was_granted), Snackbar.LENGTH_SHORT);
            snackbar.getView().setBackgroundColor(ColorUtils.getBackgroundColor(this));
            snackbar.show();
            initQRCodeReaderView();
        } else {
            snackbar = Snackbar.make(mainLayout, getString(R.string.msg_camera_permission_request_was_denied), Snackbar.LENGTH_SHORT);
            snackbar.getView().setBackgroundColor(ColorUtils.getBackgroundColor(this));
            snackbar.show();
        }
    }

    // Called when a QR is decoded
    // "text" : the text encoded in QR
    public void onQRCodeRead(String text) {
//    resultTextView.setText(text);
        Transaction transaction = getIntent().getParcelableExtra("transaction");
        transaction = TransactionManager.createTransactionFromQR(transaction, text, getApplicationContext());
        Intent intent = new Intent();
        intent.putExtra("transaction", transaction);
        setResult(RESULT_OK, intent);
        finish();
    }

    private void requestCameraPermission() {
        Snackbar snackbar;
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
            snackbar = Snackbar.make(mainLayout, getString(R.string.msg_camera_access_is_required),
                    Snackbar.LENGTH_INDEFINITE).setAction("OK", new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ActivityCompat.requestPermissions(ActivityScanQR.this, new String[]{
                            Manifest.permission.CAMERA
                    }, MY_PERMISSION_REQUEST_CAMERA);
                }
            });
            styleSnackbar(snackbar);
            snackbar.show();
        } else {
            snackbar = Snackbar.make(mainLayout, getString(R.string.msg_permission_is_not_available),
                    Snackbar.LENGTH_SHORT);
            styleSnackbar(snackbar);
            snackbar.show();
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, MY_PERMISSION_REQUEST_CAMERA);
        }
    }

    private void styleSnackbar(Snackbar snackbar) {
        snackbar.getView().setBackgroundColor(ColorUtils.getlistItemBackgroundColor(this));
    }

    private void initQRCodeReaderView() {
        View content = getLayoutInflater().inflate(R.layout.content_decoder, mainLayout, true);

        qrCodeReaderView = content.findViewById(R.id.qrdecoderview);
        CheckBox checkBoxFlashlight = content.findViewById(R.id.checkboxFlashlight);
        CheckBox checkBoxAutoFocus = content.findViewById(R.id.checkboxAutoFocus);
        Button buttonGallery = content.findViewById(R.id.buttonGallery);

        barcodeDetector = new BarcodeDetector.Builder(getApplicationContext())
                .setBarcodeFormats(Barcode.QR_CODE)
                .build();

        // Creates and starts the camera.  Note that this uses a higher resolution in comparison
        // to other detection examples to enable the barcode detector to detect small barcodes
        // at long distances.
        DisplayMetrics metrics = getResources().getDisplayMetrics();

        boolean autoFocusEnabled = mPreferences.getBoolean(FgConst.PREF_SCAN_QR_AUTO_FOCUS, false);
        CameraSource.Builder builder = new CameraSource.Builder(getApplicationContext(), barcodeDetector)
                .setFacing(CameraSource.CAMERA_FACING_BACK)
                .setRequestedPreviewSize(metrics.heightPixels, metrics.widthPixels)
                .setRequestedFps(24.0f)
                .setFlashMode(Camera.Parameters.FLASH_MODE_OFF)
                .setFocusMode(autoFocusEnabled ? Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE : Camera.Parameters.FOCUS_MODE_MACRO);

        mCameraSource = builder.build();

        qrCodeReaderView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                try {
                    mCameraSource.start(holder);
                } catch (IOException | SecurityException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                mCameraSource.stop();
            }
        });

        barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {

            }

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {
                SparseArray<Barcode> qrCode = detections.getDetectedItems();

                if (qrCode.size() != 0) {
                    onQRCodeRead(qrCode.valueAt(0).displayValue);
                }
            }
        });

        qrCodeReaderView.setOnClickListener(view -> {
            try {
                mCameraSource.autoFocus(null);
            } catch (Exception ignored) { }
        });
        checkBoxAutoFocus.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            mCameraSource.setFocusMode(isChecked ? Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE : Camera.Parameters.FOCUS_MODE_MACRO);
            checkBoxAutoFocus.setCompoundDrawablesWithIntrinsicBounds(0, isChecked ? R.drawable.ic_scan_qr_autofocus : R.drawable.ic_scan_qr_focus, 0, 0);
            checkBoxAutoFocus.setText(isChecked ? R.string.ttl_autoFocus : R.string.ttl_manualFocus);
            mPreferences.edit().putBoolean(FgConst.PREF_SCAN_QR_AUTO_FOCUS, isChecked).apply();
        });
        checkBoxAutoFocus.setChecked(autoFocusEnabled);
        if (mCameraSource.getFlashMode() != null) {
            checkBoxFlashlight.setOnCheckedChangeListener((compoundButton, isChecked) -> {
                mCameraSource.setFlashMode(isChecked ? Camera.Parameters.FLASH_MODE_TORCH : Camera.Parameters.FLASH_MODE_OFF);
                checkBoxFlashlight.setCompoundDrawablesWithIntrinsicBounds(0, isChecked ? R.drawable.ic_scan_qr_flash_on : R.drawable.ic_scan_qr_flash_off, 0, 0);
            });
        } else {
            checkBoxFlashlight.setVisibility(View.GONE);
        }
        buttonGallery.setOnClickListener(view -> {
            Intent i = new Intent(
                    Intent.ACTION_PICK,
                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

            startActivityForResult(i, RESULT_LOAD_IMAGE);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = { MediaStore.Images.Media.DATA };

            Cursor cursor = getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();

            SparseArray<Barcode> qrCode = barcodeDetector.detect(new Frame.Builder().setBitmap(BitmapFactory.decodeFile(picturePath)).build());
            if (qrCode.size() != 0) {
                onQRCodeRead(qrCode.valueAt(0).displayValue);
            }
        }
    }

}