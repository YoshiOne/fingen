package com.yoshione.fingen;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
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

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import android.util.Log;
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
public class ActivityEditLocation extends ToolbarActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {
    //    private static final String TAG = "ActivityEditLocation";
    /*
     * Define a request code to send to Google Play services This code is
     * returned in Activity.onActivityResult
     */
//    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    @BindView(R.id.editTextName)
    EditText editTextName;
    @BindView(R.id.buttonSaveLocation)
    Button mButtonSaveLocation;
    private GoogleMap map;
    private GoogleApiClient mGoogleApiClient;
    private com.yoshione.fingen.model.Location location;

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_edit_location;
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

        initMap();
    }


    private void initMap() {
        SupportMapFragment mapFragment = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map));
        if (mapFragment != null) {
            mapFragment.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap googleMap) {
                    ActivityEditLocation.this.loadMap(googleMap);
                }
            });
        } else {
            Toast.makeText(this, "Error!", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadMap(GoogleMap googleMap) {
        map = googleMap;

        addLocationMarker(true);

        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                location.setLon(latLng.longitude);
                location.setLat(latLng.latitude);
                ActivityEditLocation.this.addLocationMarker(false);
            }
        });

        ActivityEditLocationPermissionsDispatcher.getMyLocationWithPermissionCheck(this);
    }

    private void addLocationMarker(boolean zoom) {
        if (location.getLat() != 0 & location.getLon() != 0) {
            LatLng latLng = new LatLng(location.getLat(), location.getLon());
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(latLng);
            markerOptions.title(location.getName());
            map.clear();
            if (zoom) {
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17));
            } else {
                map.animateCamera(CameraUpdateFactory.newLatLng(latLng));
            }
            map.addMarker(markerOptions);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.findItem(R.id.action_go_home).setVisible(false);
        return true;

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

    @NeedsPermission({Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION})
    void getMyLocation() {
        if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("detect_locations", false)) {
            if (map != null) {
                // Now that map has loaded, let's get our location!
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                    map.setMyLocationEnabled(true);
                    mGoogleApiClient = new GoogleApiClient.Builder(this)
                            .addApi(LocationServices.API)
                            .addConnectionCallbacks(this)
                            .addOnConnectionFailedListener(this).build();
                    connectClient();
                }
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
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                & ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean("detect_locations", false).apply();
        }
//        if (allowUpdateLocation) {
//            Toast.makeText(this, R.string.permission_location_never_askagain, Toast.LENGTH_SHORT).show();
//        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // NOTE: delegate the permission handling to generated method
        ActivityEditLocationPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
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

    private void connectClient() {
        // Connect the client.
        if (isGooglePlayServicesAvailable() && mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    /*
     * Called when the Activity becomes visible.
    */
    @Override
    protected void onStart() {
        super.onStart();
        connectClient();
    }

    /*
     * Called when the Activity is no longer visible.
	 */
    @Override
    protected void onStop() {
        // Disconnecting the client invalidates it.
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }

    private boolean isGooglePlayServicesAvailable() {
        // Check that Google Play services is available
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        // If Google Play services is available
        if (ConnectionResult.SUCCESS == resultCode) {
            // In debug mode, log the status
            Log.d("Location Updates", "Google Play services is available.");
            return true;
        } else {

            return false;
        }
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
// Display the connection status
            if (this.location.isUndefined()) {
                Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                if (location != null) {
                    Toast.makeText(this, "GPS location was found!", Toast.LENGTH_SHORT).show();
                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                    this.location.setLat(location.getLatitude());
                    this.location.setLon(location.getLongitude());
                    addLocationMarker(false);
                    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 17);
                    map.animateCamera(cameraUpdate);
                } else {
                    Toast.makeText(this, "Current location was null, enable GPS on emulator!", Toast.LENGTH_SHORT).show();
                }
            }
            startLocationUpdates();
        }
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationRequest locationRequest = new LocationRequest();
            locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
            long UPDATE_INTERVAL = 60000;
            locationRequest.setInterval(UPDATE_INTERVAL);
            long FASTEST_INTERVAL = 5000;
            locationRequest.setFastestInterval(FASTEST_INTERVAL);
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,
                    locationRequest, this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
