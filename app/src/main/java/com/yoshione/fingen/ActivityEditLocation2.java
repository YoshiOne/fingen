package com.yoshione.fingen;

import android.Manifest;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.airbnb.android.airmapview.AirMapMarker;
import com.airbnb.android.airmapview.AirMapView;
import com.airbnb.android.airmapview.AirMapViewTypes;
import com.airbnb.android.airmapview.DefaultAirMapViewBuilder;
import com.airbnb.android.airmapview.MapType;
import com.airbnb.android.airmapview.listeners.OnCameraChangeListener;
import com.airbnb.android.airmapview.listeners.OnCameraMoveListener;
import com.airbnb.android.airmapview.listeners.OnInfoWindowClickListener;
import com.airbnb.android.airmapview.listeners.OnLatLngScreenLocationCallback;
import com.airbnb.android.airmapview.listeners.OnMapClickListener;
import com.airbnb.android.airmapview.listeners.OnMapInitializedListener;
import com.airbnb.android.airmapview.listeners.OnMapMarkerClickListener;
import com.google.android.gms.maps.model.LatLng;
import com.yoshione.fingen.dao.LocationsDAO;
import com.yoshione.fingen.fts.models.FtsResponse;
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
import retrofit2.Call;

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
    private DefaultAirMapViewBuilder mapViewBuilder;


    private double lat = 0;
    private double lon = 0;
    private int accuracy = 0;
    private String provider = "";
    private LocationManager locationManager;
//    private boolean forceUpdateLocation = false;
    private Call<FtsResponse> mFtsResponseCall;
    private final LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(android.location.Location location) {
            if (location == null)
                return;
            lat = location.getLatitude();
            lon = location.getLongitude();
            accuracy = Math.round(location.getAccuracy());
            provider = location.getProvider();
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
//    private boolean allowUpdateLocation;
    AirMapMarker mMarker;

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

        mapViewBuilder = new DefaultAirMapViewBuilder(this);

        map.setOnMapClickListener(this);
        map.setOnCameraChangeListener(this);
        map.setOnCameraMoveListener(this);
        map.setOnMarkerClickListener(this);
        map.setOnMapInitializedListener(this);
        map.setOnInfoWindowClickListener(this);
        map.initialize(getSupportFragmentManager());
//
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                map.setMyLocationEnabled(true);
            }
        }, 2000);
    }

    @Override
    public void onResume() {
        super.onResume();
//        preferences = android.preference.PreferenceManager.getDefaultSharedPreferences(this);

//        forceUpdateLocation = location.getID() < 0;
//        allowUpdateLocation = preferences.getBoolean("detect_locations", false) & forceUpdateLocation;

        if (location.getID() < 0) {
            ActivityEditLocation2PermissionsDispatcher.startDetectCoordsWithPermissionCheck(this);
        }
    }

    @Override
    protected void onPause() {
        removeUpdates();
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.findItem(R.id.action_go_home).setVisible(false);
        return true;
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
        removeUpdates();
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

    //<editor-fold desc="Permission dispatcher" defaultstate="collapsed">
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
        // NOTE: Show a rationale to explain why the permission is needed, e.g. with a dialog.
        // Call proceed() or cancel() on the provided PermissionRequest to continue or abort
        showRationaleDialog(R.string.msg_permission_location_rationale, request);
    }

    @OnPermissionDenied({Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION})
    void onLocationDenied() {
        // NOTE: Deal with a denied permission, e.g. by showing specific UI
        // or disabling certain functionality
        Toast.makeText(this, R.string.msg_permission_location_denied, Toast.LENGTH_SHORT).show();
    }

    @OnNeverAskAgain({Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION})
    void onLocationNeverAskAgain() {
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
//                & ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            preferences.edit().putBoolean("detect_locations", false).apply();
//        } else {
//            allowUpdateLocation = true;
//        }
//        if (!allowUpdateLocation) {
//            Toast.makeText(this, R.string.msg_permission_location_never_askagain, Toast.LENGTH_SHORT).show();
//        }
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
    //</editor-fold>
}
