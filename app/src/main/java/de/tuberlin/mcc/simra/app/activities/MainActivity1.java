/*
import android.annotation.SuppressLint;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

import org.osmdroid.config.Configuration;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Arrays;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity1 extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, LocationListener, ActivityCompat.OnRequestPermissionsResultCallback{

    private static final String TAG = MainActivity1.class.getSimpleName();
    private static final int KEEP_SCREEN_ON_FLAG = WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
    private static final int GPS_UPDATE_INTERVAL = 0;
    private static final int GPS_UPDATE_DISTANCE = 0;
    private final static int REQUEST_ENABLE_BT = 1;

    public static ExecutorService myEx;
    ActivityMainBinding binding;
    Intent recService;
    RecorderService mBoundRecorderService;
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // ServiceConnection for communicating with RecorderService
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    private final ServiceConnection mRecorderServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            RecorderService.MyBinder myBinder = (RecorderService.MyBinder) service;
            mBoundRecorderService = myBinder.getService();
        }
    };
    boolean obsEnabled = false;
    private MapView mMapView;
    private MapController mMapController;
    private MyLocationNewOverlay mLocationOverlay;
    private LocationManager locationManager;
    private Boolean recording = false;
    private ConnectionEventListener connectionEventListener = null;
    private int nRetries = 0; // number of OBS connection retries
    private boolean showingOBSWarning = false;
    private ActivityResultLauncher<Intent> activityResultLauncher;

    /**
     * Prompts user to start recording or open the OBS settings.
     * Gets called, when OBS is enabled in the settings and user tries to star recording but there
     * is not a connection to an OBS yet.
     */
 /*   private void showOBSNotConnectedRecordingWarning() {
        android.app.AlertDialog.Builder alert = new android.app.AlertDialog.Builder(this);
        alert.setTitle(R.string.not_connected_warning_title);
        alert.setMessage(R.string.not_connected_recording_warning_message);
        alert.setPositiveButton(R.string.yes, (dialog, whichButton) -> {
            startRecording();
        });
        alert.setNegativeButton(R.string.no_open_settings, (dialog, whichButton) -> {
            startActivity(new Intent(this, OpenBikeSensorActivity.class));
        });
        alert.show();
    }

    /**
     * Prompts user to open OBS settings or deactivate OBS.
     * Gets called, when a connection to an OBS fails three times in a row.
     */
 /*   private void showOBSNotConnectedWarning() {
        if (showingOBSWarning) {
            return;
        }
        showingOBSWarning = true;
        android.app.AlertDialog.Builder alert = new android.app.AlertDialog.Builder(this);
        alert.setTitle(R.string.could_not_connect_warning_title);
        alert.setMessage(R.string.could_not_connect_warning_message);
        alert.setPositiveButton(R.string.open_settings, (dialog, whichButton) -> {
            showingOBSWarning = false;
            startActivity(new Intent(this, OpenBikeSensorActivity.class));
        });
        alert.setNegativeButton(R.string.disable_obs, (dialog, whichButton) -> {
            showingOBSWarning = false;
            deactivateOBS();
        });
        alert.setOnDismissListener(dialog -> showingOBSWarning = false);
        alert.show();
    }

    /**
     * Prompts user to enable Bluetooth or deactivate OBS.
     * Gets called, if OBS is enabled in the settings but Bluetooth is disabled, so SimRa cannot
     * connect to OBS.
     */
   /* private void showBluetoothNotEnableWarning() {
        android.app.AlertDialog.Builder alert = new android.app.AlertDialog.Builder(this);
        alert.setTitle(R.string.bluetooth_not_enable_title);
        alert.setMessage(R.string.bluetooth_not_enable_message);
        alert.setPositiveButton(R.string.yes, (dialog, whichButton) -> {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            activityResultLauncher.launch(enableBtIntent);
        });
        alert.setNegativeButton(R.string.no, (dialog, whichButton) -> {
            deactivateOBS();
        });
        alert.show();
    }


    /**
     * Gets called if user responds to the Bluetooth alert. Starts obs connection or disables obs.
     */
   /* @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                // Bluetooth was enabled
                if (obsEnabled) {
                    new Thread(this::tryConnectToOBS).start();
                }
            } else if (resultCode == RESULT_CANCELED) {
                // Bluetooth was not enabled
                deactivateOBS();
            }
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(LayoutInflater.from(this));
        setContentView(binding.getRoot());

        myEx = Executors.newFixedThreadPool(4);

        // Context of application environment
        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        Configuration.getInstance().setUserAgentValue(getPackageName());


        // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        // Prepare RecorderService for accelerometer and location data recording
        recService = new Intent(this, RecorderService.class);
        // set up location manager to get location updates
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        // Map configuration
        // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        mMapView = binding.appBarMain.mainContent.map;
        mMapView.getZoomController().setVisibility(CustomZoomButtonsController.Visibility.NEVER);
        mMapView.setMultiTouchControls(true); // gesture zooming
        mMapView.setFlingEnabled(true);
        mMapController = (MapController) mMapView.getController();
        mMapController.setZoom(ZOOM_LEVEL);
        binding.appBarMain.copyrightText.setMovementMethod(LinkMovementMethod.getInstance());

        // Set compass (from OSMdroid sample project:
        // https://github.com/osmdroid/osmdroid/blob/master/OpenStreetMapViewer/src/main/
        // java/org/osmdroid/samplefragments/location/SampleFollowMe.java)
        CompassOverlay mCompassOverlay = new CompassOverlay(ctx, new InternalCompassOrientationProvider(ctx), mMapView);

        // Sets the icon to device location.
        this.mLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(this), mMapView);

        // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        // MyLocationONewOverlayParameters.
        // --> enableMyLocation: Enable receiving location updates from the provided
        // IMyLocationProvider and show your location on the maps.
        // --> enableFollowLocation: Enables "follow" functionality.
        // --> setEnableAutoStop: if true, when the user pans the map, follow my
        // location will
        // automatically disable if false, when the user pans the map,
        // the map will continue to follow current location
        // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        mLocationOverlay.enableMyLocation();

        // If app has been used before and therefore a last known location is available
        // in sharedPrefs,
        // animate the map to that location.
        // Move map to last location known by locationManager if app is started for the
        // first time.
        SharedPreferences sharedPrefs = getSharedPreferences("simraPrefs", Context.MODE_PRIVATE);
        if (sharedPrefs.contains("lastLoc_latitude") & sharedPrefs.contains("lastLoc_longitude")) {
            GeoPoint lastLoc = new GeoPoint(Double.parseDouble(sharedPrefs.getString("lastLoc_latitude", "")),
                    Double.parseDouble(sharedPrefs.getString("lastLoc_longitude", "")));
            mMapController.animateTo(lastLoc);
        } else {
            try {
                mMapController.animateTo(new GeoPoint(mLocationOverlay.getLastFix().getLatitude(),
                        mLocationOverlay.getLastFix().getLongitude()));
            } catch (RuntimeException re) {
                Log.d(TAG, re.getMessage());
            }
        }

        mMapController.animateTo(mLocationOverlay.getMyLocation());

        // the map will follow the user until the user scrolls in the UI
        mLocationOverlay.enableFollowLocation();

        // scales tiles to dpi of current display
        mMapView.setTilesScaledToDpi(true);

        // Add overlays
        mMapView.getOverlays().add(this.mLocationOverlay);
        mMapView.getOverlays().add(mCompassOverlay);
        // mMapView.getOverlays().add(this.mRotationGestureOverlay);

        mLocationOverlay.setOptionsMenuEnabled(true);
        mCompassOverlay.enableCompass();

        // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        // (1): Toolbar
        setSupportActionBar(binding.appBarMain.toolbar);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, binding.appBarMain.toolbar, R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // CenterMap
        ImageButton centerMap = findViewById(R.id.center_button);
        centerMap.setOnClickListener(v -> {
            mLocationOverlay.enableFollowLocation();
            mMapController.setZoom(ZOOM_LEVEL);
        });

        binding.appBarMain.buttonStartRecording.setOnClickListener(v -> {
            if (obsEnabled) {
                if (ConnectionManager.INSTANCE.getBleState() == BLESTATE.DISCONNECTED) { // if not connected to OBS, try to connect
                    showOBSNotConnectedRecordingWarning(); // try one reconnect and show warning if that one fails too
                    return;
                }
            }
            startRecording();
        });

        Consumer<Integer> recordIncident = incidentType -> {
            Toast t = Toast.makeText(MainActivity1.this, R.string.recorded_incident, Toast.LENGTH_SHORT);
            t.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 230);
            t.show();

            IncidentBroadcaster.broadcastIncident(MainActivity1.this, incidentType);
        };

        this.<MaterialButton>findViewById(R.id.report_closepass_incident).setOnClickListener(v -> {
            recordIncident.accept(IncidentLogEntry.INCIDENT_TYPE.CLOSE_PASS);
        });

        this.<MaterialButton>findViewById(R.id.report_obstacle_incident).setOnClickListener(v -> {
            recordIncident.accept(IncidentLogEntry.INCIDENT_TYPE.OBSTACLE);
        });

        binding.appBarMain.buttonStopRecording.setOnClickListener(v -> {
            try {
                displayButtonsForMenu();
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                // Stop RecorderService which is recording accelerometer data
                unbindService(mRecorderServiceConnection);
                stopService(recService);
                recording = false;
                if (mBoundRecorderService.hasRecordedEnough()) {
                    ShowRouteActivity.startShowRouteActivity(mBoundRecorderService.getCurrentRideKey(),
                            MetaData.STATE.JUST_RECORDED, true, this);
                } else {
                    new AlertDialog.Builder(MainActivity1.this)
                            .setMessage(getString(R.string.errorRideNotRecorded))
                            .setCancelable(false)
                            .setPositiveButton("OK", (dialog, which) -> {
                            })
                            .create()
                            .show();
                }
            } catch (Exception e) {
                Log.e(TAG, "Exception: " + e.getMessage());
                Log.e(TAG, Arrays.toString(e.getStackTrace()));            }
        });

        new CheckVersionTask().execute();


        // OpenBikeSensor
        activityResultLauncher = activityResultLauncher(MainActivity1.this);
        binding.appBarMain.buttonRideSettingsObs.setOnClickListener(view -> startActivity(new Intent(this, OpenBikeSensorActivity.class)));
    }

    /**
     * Runnable to connect to OBS.
     */
  /*  FutureTask<Boolean> obsFT;
    public class OBSTryConnectRunnable implements Runnable {
        public void run() {
            if (connectionEventListener == null) {
                // Log.d(TAG, "creating connectionEventListener");
                connectionEventListener = new ConnectionEventListener();
                connectionEventListener.setOnScanStart(isSearching -> {
                    Log.d(TAG, "isSearching: " + isSearching);
                    // updateOBSButtonStatus(BLESTATE.SEARCHING);
                    updateOBSButtonStatus();
                    return null;
                });
                connectionEventListener.setOnScanStop(foundOBS -> {
                    // Log.d(TAG, "foundOBS: " + foundOBS);
                    // updateOBSButtonStatus(BLESTATE.DISCONNECTED);
                    updateOBSButtonStatus();
                    return null;
                });
                connectionEventListener.setOnConnectionFailed(bluetoothDevice -> {
                    Log.e(TAG,"Connecting to " + bluetoothDevice.getName() + " failed!");
                    obsFT.run();
                    return null;
                });
                connectionEventListener.setOnDeviceFound(bluetoothDevice -> {
                    // Log.d(TAG, "Device found: " + bluetoothDevice.getName());
                    // updateOBSButtonStatus(BLESTATE.FOUND);
                    updateOBSButtonStatus();
                    List<UUID> characteristicsToSubscribeTo = List.of(ConnectionManager.INSTANCE.getCLOSE_PASS_CHARACTERISTIC_UUID(),ConnectionManager.INSTANCE.getSENSOR_DISTANCE_CHARACTERISTIC_UUID());
                    ConnectionManager.INSTANCE.connect(characteristicsToSubscribeTo, MainActivity1.this);
                    return null;
                });
                connectionEventListener.setOnConnectionSetupComplete(bluetoothGatt -> {
                    // Log.d(TAG, "Connection setup complete.");
                    // updateOBSButtonStatus(BLESTATE.CONNECTED);
                    updateOBSButtonStatus();
                    obsFT.run();
                    return null;
                });
                connectionEventListener.setOnClosePassNotification(measurement -> {
                    // Log.d(TAG, "Close Pass! Time: " + measurement.getObsTime() + " left: " + measurement.getLeftDistance() + " right: " + measurement.getRightDistance());
                    return null;
                });
                connectionEventListener.setOnTimeRead(time -> {
                    // Log.d(TAG, "Time: " + time);
                    return null;
                });
                connectionEventListener.setOnDisconnect(bluetoothDevice -> {
                    // Log.d(TAG, "Disconnected from " + bluetoothDevice.getName());
                    // updateOBSButtonStatus(BLESTATE.DISCONNECTED);
                    updateOBSButtonStatus();
                    return null;
                });
            }

            ConnectionManager.INSTANCE.registerListener(connectionEventListener);
            if (ConnectionManager.INSTANCE.getBleState() == BLESTATE.SEARCHING) {
                ConnectionManager.INSTANCE.stopScan();
                /*if (ConnectionManager.INSTANCE.isConnected()) {
                    // updateOBSButtonStatus(BLESTATE.CONNECTED);
                }
            }*/
      /*      if (ConnectionManager.INSTANCE.getBleState() == BLESTATE.DISCONNECTED) {
                ConnectionManager.INSTANCE.startScan(MainActivity1.this);
                // updateOBSButtonStatus(BLESTATE.SEARCHING);
            }
            updateOBSButtonStatus();
        }
    }



    private void tryConnectToOBS() {
        if(hasBLEPermissions(MainActivity1.this)) {
            Log.d(TAG, "has BLE Permissions");
            if (!obsEnabled) {
                Log.e(TAG, "OBS is not enabled (anymore)");
                return;
            }
            if (ConnectionManager.INSTANCE.getBleState() == BLESTATE.CONNECTED && connectionEventListener != null) {
                Log.e(TAG, "Already connected to OBS");
                return;
            }
            obsFT = new FutureTask<>(() -> {}, null);
            OBSTryConnectRunnable runnable = new OBSTryConnectRunnable();
            // Log.d(TAG, "running tryConnectToOBS");
            runOnUiThread(runnable);
            int MAX_NUMBER_OF_OBS_CONNECTION_RETRIES = 3;
            try {
                obsFT.get(10, TimeUnit.SECONDS); // this will block 10 seconds until Runnable completes
                if (ConnectionManager.INSTANCE.getBleState() == BLESTATE.DISCONNECTED) {
                    if (nRetries <= MAX_NUMBER_OF_OBS_CONNECTION_RETRIES) {
                        nRetries++;
                        tryConnectToOBS();
                    } else {
                        runOnUiThread(this::showOBSNotConnectedWarning);
                    }
                }
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                Log.e(TAG, "Exception: " + e.getMessage());
                Log.e(TAG, Arrays.toString(e.getStackTrace()));
                e.printStackTrace();
                // Log.d(TAG, "exception while connecting to OBS, trying again");
                if (nRetries <= MAX_NUMBER_OF_OBS_CONNECTION_RETRIES) {
                    nRetries++;
                    tryConnectToOBS();
                } else {
                    runOnUiThread(this::showOBSNotConnectedWarning);
                }
            }
        } else {
            Log.d(TAG, "has not BLE permissions. Requesting...");
            requestBlePermissions(MainActivity1.this, REQUEST_ENABLE_BT);
        }
    }



    /**
     * Deactivates the OBS Settings, so that in future SimRa won't try to connect to OBS automatically
     */
  /*  private void deactivateOBS() {
        obsEnabled = false;
        binding.appBarMain.buttonRideSettingsObs.setVisibility(View.GONE);
        SharedPref.Settings.OpenBikeSensor.setEnabled(false, this);
    }


    public void displayButtonsForMenu() {
        binding.appBarMain.buttonStartRecording.setVisibility(View.VISIBLE);
        binding.appBarMain.buttonStopRecording.setVisibility(View.INVISIBLE);

        binding.appBarMain.toolbar.setVisibility(View.VISIBLE);
        binding.appBarMain.reportIncidentContainer.setVisibility(View.GONE);

        updateOBSButtonStatus();
    }

    public void displayButtonsForDrive() {
        binding.appBarMain.buttonStopRecording.setVisibility(View.VISIBLE);
        binding.appBarMain.buttonStartRecording.setVisibility(View.INVISIBLE);

        binding.appBarMain.toolbar.setVisibility(View.GONE);
        if (SharedPref.Settings.IncidentsButtonsDuringRide.getIncidentButtonsEnabled(this)) {
            binding.appBarMain.reportIncidentContainer.setVisibility(View.VISIBLE);
        }
        binding.appBarMain.buttonRideSettingsObs.setVisibility(View.GONE);
    }

    private void startRecording() {
        if (!PermissionHelper.hasBasePermissions(this)) {
            handleMissingPermissions();
        } else {
            handleLocationService();
        }
    }

    private void handleLocationService() {
        if (isLocationServiceOff(locationManager)) {
            showLocationServiceOffDialog();
        } else {
            handleRecordingStart();
        }
    }

    private void showLocationServiceOffDialog() {
        new AlertDialog.Builder(MainActivity1.this)
                .setMessage((R.string.locationServiceisOff + " " + R.string.enableToRecord))
                .setPositiveButton(android.R.string.ok,
                        (paramDialogInterface, paramInt) -> startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)))
                .setNegativeButton(R.string.cancel, null).show();
        Toast.makeText(MainActivity1.this, R.string.recording_not_started, Toast.LENGTH_LONG).show();
    }
    private void startRecorderService() {
        Intent intent = new Intent(MainActivity1.this, RecorderService.class);
        startService(intent);
        bindService(intent, mRecorderServiceConnection, Context.BIND_IMPORTANT);
    }
    private void handleRecordingStart() {
        displayButtonsForDrive();
        getWindow().addFlags(KEEP_SCREEN_ON_FLAG);

        startRecorderService();

        recording = true;
        Toast.makeText(MainActivity1.this, R.string.recording_started, Toast.LENGTH_LONG).show();
    }
    /*
    private void startRecording() {
        if (!PermissionHelper.hasBasePermissions(this)) {
            PermissionHelper.requestFirstBasePermissionsNotGranted(MainActivity1.this);
            Toast.makeText(MainActivity1.this, R.string.recording_not_started, Toast.LENGTH_LONG).show();
        } else {
            if (isLocationServiceOff(locationManager)) {
                // notify user
                new AlertDialog.Builder(MainActivity1.this).setMessage((R.string.locationServiceisOff + " " + R.string.enableToRecord))
                        .setPositiveButton(android.R.string.ok,
                                (paramDialogInterface, paramInt) -> MainActivity1.this
                                        .startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)))
                        .setNegativeButton(R.string.cancel, null).show();
                Toast.makeText(MainActivity1.this, R.string.recording_not_started, Toast.LENGTH_LONG).show();

            } else {
                // show stop button, hide start button
                displayButtonsForDrive();
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

                // start RecorderService for accelerometer data recording
                Intent intent = new Intent(MainActivity1.this, RecorderService.class);
                startService(intent);
                bindService(intent, mRecorderServiceConnection, Context.BIND_IMPORTANT);
                recording = true;
                Toast.makeText(MainActivity1.this, R.string.recording_started, Toast.LENGTH_LONG).show();
            }
        }
    }*/
        /*

    private void updateOBSButtonStatus() {
        FloatingActionButton obsButton = binding.appBarMain.buttonRideSettingsObs;
        NavigationView navigationView = findViewById(R.id.nav_view);

        if (obsEnabled) {
            // enable Bluetooth button
            navigationView.getMenu().findItem(R.id.nav_bluetooth_connection).setVisible(true);
            obsButton.setVisibility(View.VISIBLE);
        } else {
            // disable Bluetooth button
            navigationView.getMenu().findItem(R.id.nav_bluetooth_connection).setVisible(false);
            obsButton.setVisibility(View.GONE);
        }
        switch (ConnectionManager.INSTANCE.getBleState()) {
            case DISCONNECTED:
                obsButton.setImageResource(R.drawable.ic_bluetooth_disabled);
                obsButton.setContentDescription(getString(R.string.obsNotConnected));
                obsButton.setColorFilter(Color.RED);
                break;
            case SEARCHING:
                obsButton.setImageResource(R.drawable.ic_bluetooth_searching);
                obsButton.setContentDescription(getString(R.string.obsSearching));
                obsButton.setColorFilter(Color.WHITE);
                break;
            case FOUND:
            case CONNECTING:
                obsButton.setImageResource(R.drawable.ic_bluetooth_searching);
                obsButton.setContentDescription(getString(R.string.connecting));
                obsButton.setColorFilter(Color.WHITE);
                break;
            case CONNECTED:
                obsButton.setImageResource(R.drawable.ic_bluetooth_connected);
                obsButton.setContentDescription(getString(R.string.obsConnected));
                obsButton.setColorFilter(Color.GREEN);
                break;
            default:
                break;
        }
    }

    public void onResume() {
        super.onResume();
        updateOpenBikeSensor();

        if (obsEnabled) {
            BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (mBluetoothAdapter == null) {
                handleBluetoothNotSupported();
            } else if (!mBluetoothAdapter.isEnabled() && obsEnabled) {
                handleBluetoothNotEnabled();
            } else if(ConnectionManager.INSTANCE.getBleState() != BLESTATE.CONNECTED) {
                new Thread(this::tryConnectToOBS).start();
            }
        }

        handleRecordingState();
        refreshConfiguration();
        resumeLocationUpdates();
        refreshOsmdroidConfiguration();
    }

    public void onPause() {
        super.onPause();
        clearKeepScreenOnFlag();

        // Load Configuration with changes from onCreate
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        Configuration.getInstance().save(this, prefs);

        stopLocationUpdates();
        pauseOsmdroid();
        unregisterConnectionListener();
    }
    private void stopLocationUpdates() {
        locationManager.removeUpdates(MainActivity1.this);
    }

    private void pauseOsmdroid() {
        mMapView.onPause();
        mLocationOverlay.onPause();
        mLocationOverlay.disableMyLocation();
    }

    @SuppressLint("MissingPermission")
    public void onStop() {
        super.onStop();
        saveLastLocation();
    }

    private void saveLastLocation() {
        try {
            final Location myLocation = mLocationOverlay.getLastFix();
            if (myLocation != null) {
                SharedPreferences.Editor editor = getSharedPreferences("simraPrefs", Context.MODE_PRIVATE).edit();
                editor.putString("lastLoc_latitude", String.valueOf(myLocation.getLatitude()));
                editor.putString("lastLoc_longitude", String.valueOf(myLocation.getLongitude()));
                editor.apply();
            }
        } catch (Exception se) {
            Log.e(TAG, "onStop() permission not granted yet - Exception: " + se.getMessage());
            Log.e(TAG, Arrays.toString(se.getStackTrace()));
            se.printStackTrace();
        }
    }
    private void handleBluetoothNotSupported() {
        deactivateOBS();
        Toast.makeText(MainActivity1.this, R.string.openbikesensor_bluetooth_incompatible, Toast.LENGTH_LONG).show();
    }

    private void handleBluetoothNotEnabled() {
        showBluetoothNotEnableWarning();
    }

    private void resumeLocationUpdates() {
        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, GPS_UPDATE_INTERVAL, GPS_UPDATE_DISTANCE, this);
        } catch (SecurityException se) {
            handleLocationUpdateException(se);
        }
    }

    private void refreshConfiguration() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        Configuration.getInstance().load(this, prefs);
    }

    private void refreshOsmdroidConfiguration() {
        mMapView.onResume();
        mLocationOverlay.onResume();
        mLocationOverlay.enableMyLocation();
    }
    private void handleLocationUpdateException(SecurityException se) {
        Log.e(TAG, "Location update permission not granted yet - Exception: " + se.getMessage());
        Log.e(TAG, Arrays.toString(se.getStackTrace()));
        Log.d(TAG, "Location update permission not granted yet");
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (Integer.parseInt(android.os.Build.VERSION.SDK) > 5 && keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            onBackPressed();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // Navigation Drawer
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    /*@Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if (recording) {
                Intent setIntent = new Intent(Intent.ACTION_MAIN);
                setIntent.addCategory(Intent.CATEGORY_HOME);
                setIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(setIntent);
            } else {
                super.onBackPressed();
            }
        }
    }

    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_history) {
            Intent intent = new Intent(MainActivity1.this, HistoryActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_demographic_data) {
            Intent intent = new Intent(MainActivity1.this, ProfileActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_statistics) {
            Intent intent = new Intent(MainActivity1.this, StatisticsActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_aboutSimRa) {
            Intent intent = new Intent(MainActivity1.this, AboutActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_setting) {
            Intent intent = new Intent(MainActivity1.this, SettingsActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_tutorial) {
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(getString(R.string.link_to_tutorial)));
            startActivity(i);
        } else if (id == R.id.nav_dashboard) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(getString(R.string.link_simra_Dashboard)));
            startActivity(intent);
        } else if (id == R.id.nav_imprint) {
            Intent intent = new Intent(MainActivity1.this, WebActivity.class);
            intent.putExtra("URL", getString(R.string.tuberlin_impressum));
            startActivity(intent);
        } else if (id == R.id.nav_contact) {
            Intent i = new Intent(MainActivity1.this, FeedbackActivity.class);
            startActivity(i);
        } else if (id == R.id.nav_bluetooth_connection) {
            Intent intent = new Intent(MainActivity1.this, ScrollingTestActivity.class);
            startActivity(intent);
        }
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onLocationChanged(Location location) {
        // Implementation for handling location changes
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // Implementation for handling status changes
    }

    @Override
    public void onProviderEnabled(String provider) {
        // Implementation for handling provider enabled
    }

    @Override
    public void onProviderDisabled(String provider) {
        // Implementation for handling provider disabled
    }

    private void fireNewAppVersionPrompt(int installedAppVersion, int newestAppVersion, String urlToNewestAPK, Boolean critical) {
        Log.d(TAG, "fireNewAppVersionPrompt()");
        AlertDialog alertDialog = null;
        final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity1.this);
        View settingsView = getLayoutInflater().inflate(R.layout.new_update_prompt, null);
        builder.setView(settingsView);
        builder.setTitle(getString(R.string.new_app_version_title));

        setInstalledAndNewestVersionText(settingsView, installedAppVersion, newestAppVersion);

        if (critical) {
            builder.setIcon(R.drawable.ic_update_red_24dp);
            builder.setNegativeButton(getString(R.string.new_app_version_close), (dialog, which) -> {
            });
        } else {
            builder.setIcon(R.drawable.ic_update_green_24dp);
            builder.setNegativeButton(getString(R.string.new_app_version_cancel), (dialog, which) -> {
            });
        }
        builder.setPositiveButton(getString(R.string.new_app_version_update), (dialog, which) -> {
            // Open Play Store
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=de.tuberlin.mcc.simra.app"));
            startActivity(browserIntent);
        });

        if (alertDialog != null && alertDialog.isShowing()) {
            alertDialog.dismiss();
        }
        alertDialog = builder.show();
        alertDialog.show();
    }

    private void setInstalledAndNewestVersionText(View settingsView, int installedAppVersion, int newestAppVersion) {
        TextView installedVersionText = settingsView.findViewById(R.id.installed_version_text);
        TextView newestVersionText = settingsView.findViewById(R.id.newest_version_text);
        installedVersionText.setText(getString(R.string.installed_app_version) + " " + installedAppVersion);
        newestVersionText.setText(getString(R.string.newest_app_version) + " " + newestAppVersion);
    }

    private void fireNewsPrompt() {

        // get the news from the downloaded config
        String[] simRa_news_config = getNews(MainActivity1.this);
        if (simRa_news_config.length <= 1) {
            Log.e(TAG, "Empty simRa_new_config!");
            return;
        }

        // Store the created AlertDialog instance.
        // Because only AlertDialog has cancel method.
        AlertDialog alertDialog;
        // Create a alert dialog builder.
        final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity1.this);
        // Get news popup view.
        View newsView = getLayoutInflater().inflate(R.layout.news_popup, null);
        LinearLayout linearLayout = newsView.findViewById(R.id.news_blocks);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        layoutParams.setMargins(10, 10, 10, 10);
        for (int i = 1; i < simRa_news_config.length; i++) {
            if (simRa_news_config[i].startsWith("i")){
                continue;
            }
            TextView tv = new TextView(MainActivity1.this);
            int textColor = getResources().getColor(R.color.colorPrimary, null);
            if (simRa_news_config[i].startsWith("*")) {
                textColor = getResources().getColor(R.color.colorAccent, null);
            }
            tv.setTextColor(textColor);
            // set text of TextView to text of news element
            tv.setText(simRa_news_config[i].substring(1));
            tv.setWidth(linearLayout.getWidth());
            linearLayout.addView(tv, i, layoutParams);
        }

        // Set above view in alert dialog.
        builder.setView(newsView);

        builder.setTitle(getString(R.string.news));

        alertDialog = builder.create();

        Button okButton = newsView.findViewById(R.id.ok_button);

        int newsID = Integer.parseInt(simRa_news_config[0].substring(1));

        AlertDialog finalAlertDialog = alertDialog;
        okButton.setOnClickListener(v -> {
            SharedPref.App.News.setLastSeenNewsID(newsID, MainActivity1.this);
            finalAlertDialog.cancel();
            // download the newest region list from the backend and prompt user to go to "Profile" and set region, if a new region has been added and the region is set as UNKNOWN or other.
            new RegionTask().execute();
        });

        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.setCancelable(false);
        alertDialog.show();

    }

    private class CheckForUpdate extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            // Implementation for background update check
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            // Implementation for post-update check
        }
    }
    private boolean actualSelectedRegionNotInTopThreeNearestRegion() {
        if (PermissionHelper.hasBasePermissions(MainActivity1.this)) {
            @SuppressLint("MissingPermission")
            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (location == null) {
                return false;
            }
            int selectedRegion = Profile.loadProfile(null, MainActivity1.this).region;
            int[] nearestRegions = nearestRegionsToThisLocation(location.getLatitude(), location.getLongitude(), MainActivity1.this);
            for (int nearestRegion : nearestRegions) {
                if (nearestRegion == selectedRegion) {
                    return false;
                }
            }
        }
        return true;
    }

    private class CheckVersionTask extends AsyncTask<String, String, String> {
        int installedAppVersion = -1;
        int newestAppVersion = 0;
        String urlToNewestAPK = null;
        Boolean critical = null;

        private CheckVersionTask() {
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    MainActivity1.this.findViewById(R.id.checkingAppVersionProgressBarRelativeLayout).setVisibility(View.VISIBLE);
                }
            });
        }

        @Override
        protected String doInBackground(String... strings) {
            installedAppVersion = getAppVersionNumber(MainActivity1.this);

            //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
            StringBuilder response = new StringBuilder();
            try {
                URL url = new URL(BuildConfig.API_ENDPOINT + BuildConfig.API_VERSION + "check-version?clientHash=" + getClientHash(MainActivity1.this));
                Log.d(TAG, "URL: " + url.toString());
                HttpsURLConnection urlConnection =
                        (HttpsURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.setReadTimeout(10000);
                urlConnection.setConnectTimeout(15000);

                BufferedReader in = new BufferedReader(
                        new InputStreamReader(urlConnection.getInputStream()));
                String inputLine;

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                int status = urlConnection.getResponseCode();
                Log.d(TAG, "Server status: " + status);
            } catch (IOException e) {
                Log.e(TAG, "checkVersion Exception: " + e.getMessage());
                Log.e(TAG, Arrays.toString(e.getStackTrace()));
                e.printStackTrace();
            }
            Log.d(TAG, "GET version response: " + response.toString());
            String[] responseArray = response.toString().split("splitter");
            if (responseArray.length > 2) {
                critical = Boolean.valueOf(responseArray[0]);
                newestAppVersion = Integer.valueOf(responseArray[1]);
                urlToNewestAPK = responseArray[2];
                return response.toString();
            } else {
                return null;
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    MainActivity1.this.findViewById(R.id.checkingAppVersionProgressBarRelativeLayout).setVisibility(View.GONE);
                }
            });
            if ((newestAppVersion > 0 && urlToNewestAPK != null && critical != null) && installedAppVersion < newestAppVersion) {
                MainActivity1.this.fireNewAppVersionPrompt(installedAppVersion, newestAppVersion, urlToNewestAPK, critical);
            } else {
                new NewsTask().execute();
            }
        }
    }

    private class RegionTask extends AsyncTask<String, String, String> {
        int regionsID = -1;
        int lastSeenRegionsID = 0;

        private RegionTask() {
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... strings) {

            StringBuilder checkRegionsResponse = new StringBuilder();
            int status = 0;
            lastSeenRegionsID = SharedPref.App.Regions.getLastSeenRegionsID(MainActivity1.this);
            try {

                URL url = new URL(
                        BuildConfig.API_ENDPOINT + BuildConfig.API_VERSION + "check-regions?clientHash=" + getClientHash(MainActivity1.this) + "&lastSeenRegionsID=" + lastSeenRegionsID);
                Log.d(TAG, "URL: " + url.toString());
                HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.setReadTimeout(10000);
                urlConnection.setConnectTimeout(15000);
                BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    if (regionsID == -1 && inputLine.startsWith("#")) {
                        regionsID = Integer.parseInt(inputLine.replace("#", ""));
                    } else {
                        checkRegionsResponse.append(inputLine).append(System.lineSeparator());
                    }
                }
                in.close();
                status = urlConnection.getResponseCode();
                Log.d(TAG, "Server status: " + status);
            } catch (IOException e) {
                Log.e(TAG, "RegionTask Exception: " + e.getMessage());
                Log.e(TAG, Arrays.toString(e.getStackTrace()));
                e.printStackTrace();
            }
            Log.d(TAG, "GET regions response: " + checkRegionsResponse.toString());
            if (status == 200 && checkRegionsResponse.length() > 0) {
                File regionsFile = IOUtils.Files.getRegionsFile(MainActivity1.this);
                overwriteFile(checkRegionsResponse.toString(), regionsFile);
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            // prompt user to go to "Profile" and set region, if regions have been updated and the region is set as UNKNOWN or other.
            Log.d(TAG, lastSeenRegionsID + " < " + regionsID + ": " + (lastSeenRegionsID < regionsID));
            Log.d(TAG, "actualSelectedRegionNotInTopThreeNearestRegion(): " + actualSelectedRegionNotInTopThreeNearestRegion());
            if (!SharedPref.App.RegionsPrompt.getRegionPromptShownAfterV81(MainActivity1.this) || (!SharedPref.App.RegionsPrompt.getDoNotShowRegionPrompt(MainActivity1.this) &&
                    (((lastSeenRegionsID < regionsID) && profileIsInUnknownRegion(MainActivity1.this)) ||
                            actualSelectedRegionNotInTopThreeNearestRegion()))) {
                fireProfileRegionPrompt(regionsID, MainActivity1.this);
            }
        }
    }

    private class NewsTask extends AsyncTask<String, String, String> {
        int newsID = -1;

        private NewsTask() {
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... strings) {

            StringBuilder checkNewsResponseDE = new StringBuilder();
            StringBuilder checkNewsResponseEN = new StringBuilder();
            int statusDE = 0;
            int statusEN = 0;
            int lastSeenNewsID = SharedPref.App.News.getLastSeenNewsID(MainActivity1.this);
            try {

                URL url_de = new URL(
                        BuildConfig.API_ENDPOINT + BuildConfig.API_VERSION + "check-news?clientHash=" + getClientHash(MainActivity1.this) + "&lastSeenNewsID=" + lastSeenNewsID + "&newsLanguage=de");
                Log.d(TAG, "URL_DE: " + url_de.toString());
                URL url_en = new URL(
                        BuildConfig.API_ENDPOINT + "check/news?clientHash=" + getClientHash(MainActivity1.this) + "&lastSeenNewsID=" + lastSeenNewsID + "&newsLanguage=en");
                Log.d(TAG, "URL_EN: " + url_en.toString());
                HttpsURLConnection url_de_Connection = (HttpsURLConnection) url_de.openConnection();
                HttpsURLConnection url_en_Connection = (HttpsURLConnection) url_en.openConnection();
                url_de_Connection.setRequestMethod("GET");
                url_en_Connection.setRequestMethod("GET");
                url_de_Connection.setReadTimeout(10000);
                url_en_Connection.setReadTimeout(10000);
                url_de_Connection.setConnectTimeout(15000);
                url_en_Connection.setConnectTimeout(15000);
                BufferedReader in_de = new BufferedReader(new InputStreamReader(url_de_Connection.getInputStream()));
                BufferedReader in_en = new BufferedReader(new InputStreamReader(url_en_Connection.getInputStream()));

                String inputLine_de;
                String inputLine_en;

                while ((inputLine_de = in_de.readLine()) != null) {
                    if (newsID == -1 && inputLine_de.startsWith("#")) {
                        newsID = Integer.parseInt(inputLine_de.replace("#", ""));
                    }
                    checkNewsResponseDE.append(inputLine_de).append(System.lineSeparator());
                }
                in_de.close();
                while ((inputLine_en = in_en.readLine()) != null) {
                    checkNewsResponseEN.append(inputLine_en).append(System.lineSeparator());
                }
                in_en.close();
                statusDE = url_de_Connection.getResponseCode();
                statusEN = url_en_Connection.getResponseCode();

                Log.d(TAG, "Server statusDE: " + statusDE + " statusEN: " + statusEN);
            } catch (IOException e) {
                Log.e(TAG, "NewsTask Exception: " + e.getMessage());
                Log.e(TAG, Arrays.toString(e.getStackTrace()));
                e.printStackTrace();
            }
            Log.d(TAG, "GET news DE response: " + checkNewsResponseDE.toString());
            Log.d(TAG, "GET news EN response: " + checkNewsResponseEN.toString());
            if (statusDE == 200 && checkNewsResponseDE.length() > 0) {
                File newsFile = IOUtils.Files.getDENewsFile(MainActivity1.this);
                overwriteFile(checkNewsResponseDE.toString(), newsFile);
            }
            if (statusEN == 200 && checkNewsResponseDE.length() > 0) {
                File newsFile = IOUtils.Files.getENNewsFile(MainActivity1.this);
                overwriteFile(checkNewsResponseEN.toString(), newsFile);
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (SharedPref.App.News.getLastSeenNewsID(MainActivity1.this) < newsID) {
                fireNewsPrompt();
            } else {
                // download the newest region list from the backend and prompt user to go to "Profile" and set region, if a new region has been added and the region is set as UNKNOWN or other.
                new RegionTask().execute();
            }
        }
    }

}
