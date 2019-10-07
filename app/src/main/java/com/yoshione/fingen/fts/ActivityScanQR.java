package com.yoshione.fingen.fts;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PointF;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.Toast;

import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.yoshione.fingen.R;
import com.yoshione.fingen.managers.TransactionManager;
import com.yoshione.fingen.model.Transaction;
import com.yoshione.fingen.utils.ColorUtils;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ActivityScanQR extends AppCompatActivity
        implements ActivityCompat.OnRequestPermissionsResultCallback {

    private static final String TAG = "ActivityScanQR";
    private static final int MY_PERMISSION_REQUEST_CAMERA = 0;

    private ViewGroup mainLayout;

    private CameraSource mCameraSource;
    private SurfaceView qrCodeReaderView;
    private PointsOverlayView pointsOverlayView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_decoder);
        mainLayout = findViewById(R.id.main_layout);

        // clipboard worker copied from ActivitySmsList
        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        if (!clipboard.hasPrimaryClip()) return;
        ClipData data = clipboard.getPrimaryClip();
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

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            initQRCodeReaderView();

            if (!text.equals("") && matcher.find()) {
                final String qrCode = text;
                // show dialog copied from FragmentTransaction
                final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
                alertDialogBuilder
                        .setTitle(R.string.ttl_confirm_action)
                        .setMessage(R.string.msg_use_qr_from_buffer)
                        .setPositiveButton(R.string.ok, (dialog, which) ->this.onQRCodeRead(qrCode, null))
                        .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss()).show();
            }
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
    // "points" : points where QR control points are placed
    public void onQRCodeRead(String text, PointF[] points) {
//    resultTextView.setText(text);
//    pointsOverlayView.setPoints(points);
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
        CheckBox flashlightCheckBox = content.findViewById(R.id.flashlight_checkbox);
        pointsOverlayView = content.findViewById(R.id.points_overlay_view);

        BarcodeDetector barcodeDetector = new BarcodeDetector.Builder(getApplicationContext())
                .setBarcodeFormats(Barcode.QR_CODE)
                .build();

        // Creates and starts the camera.  Note that this uses a higher resolution in comparison
        // to other detection examples to enable the barcode detector to detect small barcodes
        // at long distances.
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        Log.e("Financisto", "test " + metrics.widthPixels + " / " + metrics.heightPixels + " | " + qrCodeReaderView.getMeasuredWidth() + " / " + qrCodeReaderView.getMeasuredHeight());
        CameraSource.Builder builder = new CameraSource.Builder(getApplicationContext(), barcodeDetector)
                .setFacing(CameraSource.CAMERA_FACING_BACK)
                .setRequestedPreviewSize(metrics.heightPixels, metrics.widthPixels)
                .setRequestedFps(24.0f)
                .setFocusMode(Camera.Parameters.FOCUS_MODE_MACRO);

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
                    onQRCodeRead(qrCode.valueAt(0).displayValue, null);
                }
            }
        });

        qrCodeReaderView.setOnClickListener(view -> mCameraSource.autoFocus(null));
        flashlightCheckBox.setOnCheckedChangeListener((compoundButton, isChecked) -> mCameraSource.setFlashMode(isChecked ? Camera.Parameters.FLASH_MODE_TORCH : Camera.Parameters.FLASH_MODE_OFF));
    }
}