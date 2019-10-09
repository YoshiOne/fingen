package com.yoshione.fingen;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.airbnb.android.airmapview.AirMapMarker;
import com.airbnb.android.airmapview.AirMapView;
import com.airbnb.android.airmapview.listeners.OnCameraChangeListener;
import com.airbnb.android.airmapview.listeners.OnCameraMoveListener;
import com.airbnb.android.airmapview.listeners.OnInfoWindowClickListener;
import com.airbnb.android.airmapview.listeners.OnLatLngScreenLocationCallback;
import com.airbnb.android.airmapview.listeners.OnMapClickListener;
import com.airbnb.android.airmapview.listeners.OnMapInitializedListener;
import com.airbnb.android.airmapview.listeners.OnMapMarkerClickListener;
import com.google.android.gms.maps.model.LatLng;
import com.yoshione.fingen.dao.LocationsDAO;
import com.yoshione.fingen.widgets.ToolbarActivity;

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
 * Created by slv on 07.03.2016.
 * a
 */
@RuntimePermissions
public class ActivityEditLocation2 extends ToolbarActivity implements OnCameraChangeListener, OnMapInitializedListener,
        OnMapClickListener, OnCameraMoveListener, OnMapMarkerClickListener,
        OnInfoWindowClickListener, OnLatLngScreenLocationCallback {
    @BindView(R.id.editTextName)
    EditText editTextName;
    @BindView(R.id.buttonSaveLocation)
    Button mButtonSaveLocation;
    @BindView(R.id.map)
    AirMapView map;
    private com.yoshione.fingen.model.Location location;

    private double lat = 0;
    private double lon = 0;
    private LocationManager locationManager;
    private final LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(android.location.Location location) {
            if (location == null)
                return;
            lat = location.getLatitude();
            lon = location.getLongitude();
            updateLocationOnMap();
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_edit_location2;
    }

    @Override
    protected String getLayoutTitle() {
        if (location == null) {
            return "";
        }
        if (location.getID() < 0) {
            return getResources().getString(R.string.new_new_location);
        } else {
            return getResources().getString(R.string.ent_edit_location);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.bind(this);

        if (savedInstanceState != null) {
            location = savedInstanceState.getParcelable("location");
        } else {
            location = getIntent().getParcelableExtra("location");
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getLayoutTitle());
        }

        editTextName.setText(location.getName());
        editTextName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                location.setName(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        map.setOnMapClickListener(this);
        map.setOnCameraChangeListener(this);
        map.setOnCameraMoveListener(this);
        map.setOnMarkerClickListener(this);
        map.setOnMapInitializedListener(this);
        map.setOnInfoWindowClickListener(this);
        map.setOnMapInitializedListener(new OnMapInitializedListener() {
            @Override
            public void onMapInitialized() {
                map.setMyLocationEnabled(true);
                map.setMyLocationButtonEnabled(true);
                if (location != null && !location.isUndefined()) {
                    LatLng latLng = new LatLng(location.getLat(), location.getLon());
                    map.clearMarkers();
                    addMarker(latLng);
                    map.setCenterZoom(latLng, 16);
                }

            }
        });
        map.initialize(getSupportFragmentManager());
    }

    @Override
    public void onResume() {
        super.onResume();
        ActivityEditLocation2PermissionsDispatcher.startDetectCoordsWithPermissionCheck(this);
    }

    @Override
    protected void onPause() {
        removeUpdates();
        super.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("location", location);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        location = savedInstanceState.getParcelable("location");
    }

    @OnClick(R.id.buttonSaveLocation)
    public void onSaveClick() {
        if (location.getName().isEmpty()) return;
        LocationsDAO locationsDAO = LocationsDAO.getInstance(getApplicationContext());
        try {
            locationsDAO.createModel(location);
        } catch (Exception e) {
            Toast.makeText(this, R.string.msg_error_on_write_to_db, Toast.LENGTH_SHORT).show();
            return;
        }
        finish();
    }

    private void updateLocationOnMap() {
        if (location.getID() < 0) {
            final LatLng latLng = new LatLng(lat, lon);
            map.setCenterZoom(latLng, 16);
        }
    }

    private void removeUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (locationManager != null) {
                locationManager.removeUpdates(locationListener);
            }
        }
    }

    @Override
    public void onCameraChanged(LatLng latLng, int zoom) {
        removeUpdates();
    }

    @Override
    public void onCameraMove() {
        removeUpdates();
    }

    @Override
    public void onInfoWindowClick(AirMapMarker<?> airMarker) {

    }

    @Override
    public void onLatLngScreenLocationReady(Point point) {

    }

    @Override
    public void onMapClick(LatLng latLng) {
        if (latLng != null) {
            removeUpdates();
            location.setLat(latLng.latitude);
            location.setLon(latLng.longitude);
            addMarker(latLng);
        }
    }

    @Override
    public void onMapInitialized() {
        map.setMyLocationButtonEnabled(true);
    }

    @Override
    public void onMapMarkerClick(AirMapMarker<?> airMarker) {

    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    private void addMarker(LatLng latLng) {
        map.clearMarkers();
        location.setLat(latLng.latitude);
        location.setLon(latLng.longitude);
        map.addMarker(new AirMapMarker.Builder()
                .position(latLng)
                .iconId(R.mipmap.icon_location_pin)
                .build());
    }

    @NeedsPermission({Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION})
    void startDetectCoords() {
        if (location.getID() < 0) {
            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

            if (locationManager != null && locationManager.getAllProviders().contains(LocationManager.NETWORK_PROVIDER))
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
                }

            if (locationManager != null && locationManager.getAllProviders().contains(LocationManager.GPS_PROVIDER))
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                }
        }
    }

    @OnShowRationale({Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION})
    void showRationaleForContact(PermissionRequest request) {
        showRationaleDialog(R.string.msg_permission_location_rationale, request);
    }

    @OnPermissionDenied({Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION})
    void onLocationDenied() {
        Toast.makeText(this, R.string.msg_permission_location_denied, Toast.LENGTH_SHORT).show();
    }

    @OnNeverAskAgain({Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION})
    void onLocationNeverAskAgain() {

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
}
