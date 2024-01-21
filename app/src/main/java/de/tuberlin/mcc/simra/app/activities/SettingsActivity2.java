package de.tuberlin.mcc.simra.app.activities;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import de.tuberlin.mcc.simra.app.BuildConfig;
import de.tuberlin.mcc.simra.app.R;
import de.tuberlin.mcc.simra.app.databinding.ActivitySettingsBinding;
import de.tuberlin.mcc.simra.app.services.DebugUploadService;
import de.tuberlin.mcc.simra.app.util.BaseActivity;
import de.tuberlin.mcc.simra.app.util.ConnectionManager;
import de.tuberlin.mcc.simra.app.util.IOUtils;
import de.tuberlin.mcc.simra.app.util.SharedPref;
import de.tuberlin.mcc.simra.app.util.UnitHelper;

import static de.tuberlin.mcc.simra.app.util.IOUtils.Directories.getBaseFolderPath;
import static de.tuberlin.mcc.simra.app.util.IOUtils.importSimRaData;
import static de.tuberlin.mcc.simra.app.util.IOUtils.zipTo;
import static de.tuberlin.mcc.simra.app.util.PermissionHelper.REQUEST_ENABLE_BT;
import static de.tuberlin.mcc.simra.app.util.PermissionHelper.requestBlePermissions;
import static de.tuberlin.mcc.simra.app.util.Utils.activityResultLauncher;
import static de.tuberlin.mcc.simra.app.util.Utils.prepareDebugZip;
import static de.tuberlin.mcc.simra.app.util.Utils.showBluetoothNotEnableWarning;
import static de.tuberlin.mcc.simra.app.util.Utils.sortFileListLastModified;

public class SettingsActivity2 extends BaseActivity {

    private static final String TAG = "SettingsActivity2_LOG";
    private static final int DIRECTORY_PICKER_EXPORT = 9999;
    private static final int FILE_PICKER_IMPORT = 8778;

    private BroadcastReceiver br;
    private ActivitySettingsBinding binding;

    private ActivityResultLauncher<Intent> activityResultLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadActivity();
    }

    private void loadActivity() {
        initializeUI();
        setupSliders();
        setupSelectMenus();
        setupCheckBoxes();
        setupSwitches();
        setupButtons();
        setupVersionText();
    }

    private void initializeUI() {
        binding = ActivitySettingsBinding.inflate(LayoutInflater.from(this));
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar.toolbar);

        try {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        } catch (NullPointerException ignored) {
            Log.d(TAG, "NullPointerException");
        }

        binding.toolbar.toolbar.setTitle("");
        binding.toolbar.toolbar.setSubtitle("");
        binding.toolbar.toolbarTitle.setText(R.string.title_activity_settings);
        binding.toolbar.backButton.setOnClickListener(v -> finish());
    }

    private void setupSliders() {
        // ... (existing logic for sliders)
        // Slider: Duration
        binding.privacyDurationSlider.setValueFrom(SharedPref.Settings.Ride.PrivacyDuration.getMinDuration());
        binding.privacyDurationSlider.setValueTo(SharedPref.Settings.Ride.PrivacyDuration.getMaxDuration());
        binding.privacyDurationSlider.setValue(SharedPref.Settings.Ride.PrivacyDuration.getDuration(this));
        binding.privacyDurationTextLeft.setText(SharedPref.Settings.Ride.PrivacyDuration.getMinDuration() + getString(R.string.seconds_short));
        binding.privacyDurationTextRight.setText(SharedPref.Settings.Ride.PrivacyDuration.getMaxDuration() + getString(R.string.seconds_short));
        binding.privacyDurationSlider.addOnChangeListener((slider, changeListener, touchChangeListener) -> {
            SharedPref.Settings.Ride.PrivacyDuration.setDuration(Math.round(slider.getValue()), this);
        });

        // Slider: Distance
        updatePrivacyDistanceSlider(SharedPref.Settings.DisplayUnit.getDisplayUnit(this));
        binding.privacyDistanceSlider.addOnChangeListener((slider, changeListener, touchChangeListener) -> {
            SharedPref.Settings.Ride.PrivacyDistance.setDistance(Math.round(slider.getValue()), SharedPref.Settings.DisplayUnit.getDisplayUnit(this), this);
        });

    }

    private void setupSelectMenus() {
        // ... (existing logic for select menus)
        // Select Menu: Bike Type
        Spinner bikeTypeSpinner = findViewById(R.id.bikeTypeSpinner);
        bikeTypeSpinner.setSelection(SharedPref.Settings.Ride.BikeType.getBikeType(this));
        bikeTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                SharedPref.Settings.Ride.BikeType.setBikeType(position, SettingsActivity2.this);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        // Select Menu: Phone Location
        Spinner phoneLocationSpinner = findViewById(R.id.locationTypeSpinner);
        phoneLocationSpinner.setSelection(SharedPref.Settings.Ride.PhoneLocation.getPhoneLocation(this));
        phoneLocationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                SharedPref.Settings.Ride.PhoneLocation.setPhoneLocation(position, SettingsActivity2.this);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

    private void setupCheckBoxes() {
        // ... (existing logic for checkboxes)
        // CheckBox: Child on Bicycle
        CheckBox childOnBikeCheckBox = findViewById(R.id.childCheckBox);
        childOnBikeCheckBox.setChecked(SharedPref.Settings.Ride.ChildOnBoard.isChildOnBoard(this));
        childOnBikeCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPref.Settings.Ride.ChildOnBoard.setChildOnBoard(isChecked, this);
        });

        // CheckBox: Bicycle with Trailer
        CheckBox trailerCheckBox = findViewById(R.id.trailerCheckBox);
        trailerCheckBox.setChecked(SharedPref.Settings.Ride.BikeWithTrailer.hasTrailer(this));
        trailerCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPref.Settings.Ride.BikeWithTrailer.setTrailer(isChecked, this);
        });
    }

    private void setupSwitches() {
        // ... (existing logic for switches)
        // Switch: Unit Select
        Switch unitSwitch = findViewById(R.id.unitSwitch);
        if (SharedPref.Settings.DisplayUnit.isImperial(this)) {
            unitSwitch.setChecked(true);
        }
        unitSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            UnitHelper.DISTANCE displayUnit = isChecked ? UnitHelper.DISTANCE.IMPERIAL : UnitHelper.DISTANCE.METRIC;
            SharedPref.Settings.DisplayUnit.setDisplayUnit(displayUnit, this);
            updatePrivacyDistanceSlider(displayUnit);
        });

        // Switch: Buttons for adding incidents during ride
        if (SharedPref.Settings.IncidentsButtonsDuringRide.getIncidentButtonsEnabled(this)) {
            binding.switchButtons.setChecked(true);
        }
        binding.switchButtons.setOnCheckedChangeListener(
                (buttonView, isChecked) -> {
                    SharedPref.Settings.IncidentsButtonsDuringRide.setIncidentButtonsEnabled(isChecked, this);
                    if (isChecked) {
                       // SettingsActivity2.fireIncidentButtonsEnablePrompt();
                    }
                });

        // Switch: AI Select
        if (SharedPref.Settings.IncidentGenerationAIActive.getAIEnabled(this)) {
            binding.switchAI.setChecked(true);
        }
        binding.switchAI.setOnCheckedChangeListener((buttonView, isChecked) -> SharedPref.Settings.IncidentGenerationAIActive.setAIEnabled(isChecked, this));


        binding.importButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(SettingsActivity2.this).setTitle(R.string.importPromptTitle);
                builder.setMessage(R.string.importButtonText);
                builder.setPositiveButton(R.string.continueText, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
                        chooseFile.setType("application/zip");
                        chooseFile = Intent.createChooser(chooseFile, getString(R.string.importFile));
                        startActivityForResult(chooseFile, FILE_PICKER_IMPORT);
                        importZipFromLocation.launch(new String[]{"application/zip"});
                    }
                });
                builder.setNegativeButton(R.string.cancel, null);
                builder.show();
            }
        });

        binding.exportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(SettingsActivity2.this).setTitle(R.string.exportPromptTitle);
                builder.setMessage(R.string.exportButtonText);
                builder.setPositiveButton(R.string.continueText, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        exportZipToLocation.launch(Uri.parse(DocumentsContract.PROVIDER_INTERFACE));
                    }
                });
                builder.setNegativeButton(R.string.cancel, null);
                builder.show();
            }
        });

        // Switch: OpenBikeSensor device enabled
        boolean obsActivated = SharedPref.Settings.OpenBikeSensor.isEnabled(this);
        binding.obsButton.setVisibility(obsActivated ? View.VISIBLE : View.GONE);
        activityResultLauncher = activityResultLauncher(SettingsActivity2.this);
        binding.obsButton.setOnClickListener(view ->
        {
            requestBlePermissions(SettingsActivity2.this, REQUEST_ENABLE_BT);
            BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (mBluetoothAdapter == null) {
                // Device does not support Bluetooth
                Toast.makeText(SettingsActivity2.this, R.string.openbikesensor_bluetooth_incompatible, Toast.LENGTH_LONG)
                        .show();
            } else if (!mBluetoothAdapter.isEnabled()) {
                // Bluetooth is disabled
                showBluetoothNotEnableWarning(activityResultLauncher, SettingsActivity2.this);
            } else {
                startActivity(new Intent(this, OpenBikeSensorActivity.class));
            }
        });

        if (BluetoothAdapter.getDefaultAdapter() == null) {
            // Device does not support Bluetooth
            binding.obsSwitch.setEnabled(false);
        }
        binding.obsSwitch.setChecked(obsActivated);
        binding.obsSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPref.Settings.OpenBikeSensor.setEnabled(isChecked, SettingsActivity2.this);
            binding.obsButton.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            if (!isChecked && ConnectionManager.INSTANCE.getBleState() == ConnectionManager.BLESTATE.CONNECTED) {
                ConnectionManager.INSTANCE.disconnect(ConnectionManager.INSTANCE.getScanResult().getDevice());
            }
        });
    }

    private void updatePrivacyDistanceSlider(UnitHelper.DISTANCE displayUnit) {
        int minDistance=0;
        int maxDistance=1000;

      /*  if (displayUnit == UnitHelper.DISTANCE.IMPERIAL) {
            // Set min and max distance values for Imperial units
            minDistance = // Your logic for setting min distance in Imperial units;
                    maxDistance = // Your logic for setting max distance in Imperial units;
        } else {
            // Set min and max distance values for Metric units
            minDistance = // Your logic for setting min distance in Metric units;
                    maxDistance = // Your logic for setting max distance in Metric units;
        }

        binding.privacyDistanceSlider.setValueFrom(minDistance);
        binding.privacyDistanceSlider.setValueTo(maxDistance);
        binding.privacyDistanceSlider.setValue(SharedPref.Settings.Ride.PrivacyDistance.getDistance(this));
        binding.privacyDistanceTextLeft.setText(minDistance + getString(R.string.meters_short));
        binding.privacyDistanceTextRight.setText(maxDistance + getString(R.string.meters_short));
*/
        binding.privacyDistanceSlider.addOnChangeListener((slider, changeListener, touchChangeListener) -> {
            SharedPref.Settings.Ride.PrivacyDistance.setDistance(Math.round(slider.getValue()), displayUnit, this);
        });
    }


    private void setupButtons() {
        // ... (existing logic for buttons)
    }

    private void setupVersionText() {
        TextView appVersionTextView = findViewById(R.id.appVersionTextView);
        // appVersionTextView.setText(getString(R.string.version_info, BuildConfig.VERSION_CODE, BuildConfig.VERSION_NAME));
        appVersionTextView.setText(getString( BuildConfig.VERSION_CODE, BuildConfig.VERSION_NAME));

        binding.appVersionTextView.setOnClickListener(v -> showDebugPrompt());
    }

    private void showDebugPrompt() {
        // ... (existing logic for debug prompt)
    }

    @Override
    protected void onResume() {
        super.onResume();
        br = new MyBroadcastReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("de.tuberlin.mcc.simra.app.UPLOAD_COMPLETE");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            this.registerReceiver(br, filter, RECEIVER_EXPORTED);
        } else {
            this.registerReceiver(br, filter);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        this.unregisterReceiver(br);
    }

    private void handleUploadResult(boolean uploadSuccessful) {
        int messageResId = uploadSuccessful ? R.string.upload_completed : R.string.upload_failed;
        Toast.makeText(getApplicationContext(), messageResId, Toast.LENGTH_LONG).show();
    }

    private class MyBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            handleUploadResult(intent.getBooleanExtra("uploadSuccessful", false));
        }
    }

    // ... (remaining code, including onActivityResult methods)

    private final ActivityResultLauncher<Uri> exportZipToLocation =
            registerForActivityResult(new ActivityResultContracts.OpenDocumentTree(),
                    new ActivityResultCallback<Uri>() {
                        @Override
                        public void onActivityResult(Uri uri) {
                            boolean successfullyExported = zipTo(SettingsActivity2.this.getFilesDir().getParent(), uri, SettingsActivity2.this);
                            if (successfullyExported) {
                                Toast.makeText(SettingsActivity2.this, R.string.exportSuccessToast, Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(SettingsActivity2.this, R.string.exportFailToast, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

    private final ActivityResultLauncher<String[]> importZipFromLocation =
            registerForActivityResult(new ActivityResultContracts.OpenDocument(),
                    new ActivityResultCallback<Uri>() {
                        @Override
                        public void onActivityResult(Uri uri) {
                            boolean successfullyImported = importSimRaData(uri, SettingsActivity2.this);
                            if (successfullyImported) {
                                Toast.makeText(SettingsActivity2.this, R.string.importSuccess, Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(SettingsActivity2.this, R.string.importFail, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
}
