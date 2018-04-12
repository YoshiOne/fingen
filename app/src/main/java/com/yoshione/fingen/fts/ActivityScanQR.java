package com.yoshione.fingen.fts;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PointF;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.dlazaro66.qrcodereaderview.QRCodeReaderView;
import com.dlazaro66.qrcodereaderview.QRCodeReaderView.OnQRCodeReadListener;
import com.yoshione.fingen.BuildConfig;
import com.yoshione.fingen.R;
import com.yoshione.fingen.managers.TransactionManager;
import com.yoshione.fingen.model.Transaction;
import com.yoshione.fingen.utils.ColorUtils;

public class ActivityScanQR extends AppCompatActivity
        implements ActivityCompat.OnRequestPermissionsResultCallback, OnQRCodeReadListener {

    private static final int MY_PERMISSION_REQUEST_CAMERA = 0;

    private ViewGroup mainLayout;

    private QRCodeReaderView qrCodeReaderView;
    private PointsOverlayView pointsOverlayView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (BuildConfig.DEBUG) {
            onQRCodeRead("t=20181219T155200&s=1286.00&fn=8710000100448399&i=106760&fp=3157595585&n=1", null);
//            onQRCodeRead("t=20180113T1024&s=1364.00&fn=8710000100354835&i=40732&fp=3964607275&n=1 ", null);
        }

        setContentView(R.layout.activity_decoder);

        mainLayout = findViewById(R.id.main_layout);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            initQRCodeReaderView();
        } else {
            requestCameraPermission();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (qrCodeReaderView != null) {
            qrCodeReaderView.startCamera();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (qrCodeReaderView != null) {
            qrCodeReaderView.stopCamera();
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
    @Override
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

        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_AUTOFOCUS)) {
            qrCodeReaderView.setAutofocusInterval(2000L);
        }
        qrCodeReaderView.setOnQRCodeReadListener(this);
        qrCodeReaderView.setBackCamera();
        flashlightCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                qrCodeReaderView.setTorchEnabled(isChecked);
            }
        });
        qrCodeReaderView.setQRDecodingEnabled(true);
        qrCodeReaderView.startCamera();
    }
}