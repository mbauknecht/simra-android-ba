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
import androidx.appcompat.app.AlertDialog;

import de.tuberlin.mcc.simra.app.BuildConfig;
import de.tuberlin.mcc.simra.app.R;
import de.tuberlin.mcc.simra.app.databinding.ActivitySettingsBinding;
import de.tuberlin.mcc.simra.app.services.DebugUploadService;
import de.tuberlin.mcc.simra.app.util.BaseActivity;
import de.tuberlin.mcc.simra.app.util.ConnectionManager;
import de.tuberlin.mcc.simra.app.util.IOUtils;
import de.tuberlin.mcc.simra.app.util.SharedPref;
import de.tuberlin.mcc.simra.app.util.UnitHelper;
import de.tuberlin.mcc.simra.app.util.Utils;

import static de.tuberlin.mcc.simra.app.util.IOUtils.Directories.getBaseFolderPath;
import static de.tuberlin.mcc.simra.app.util.IOUtils.importSimRaData;
import static de.tuberlin.mcc.simra.app.util.IOUtils.zipTo;
import static de.tuberlin.mcc.simra.app.util.PermissionHelper.REQUEST_ENABLE_BT;
import static de.tuberlin.mcc.simra.app.util.PermissionHelper.requestBlePermissions;
import static de.tuberlin.mcc.simra.app.util.Utils.activityResultLauncher;
import static de.tuberlin.mcc.simra.app.util.Utils.prepareDebugZip;
import static de.tuberlin.mcc.simra.app.util.Utils.showBluetoothNotEnableWarning;
import static de.tuberlin.mcc.simra.app.util.Utils.sortFileListLastModified;

public class SettingsActivity1 extends BaseActivity {

    private static final String TAG = "SettingsActivity1_LOG";
    private final static int DIRECTORY_PICKER_EXPORT = 9999;
    private static final int FILE_PICKER_IMPORT = 8778;

    BroadcastReceiver br;
    ActivitySettingsBinding binding;

    private ActivityResultLauncher<Intent> activityResultLauncher;
    private final int[] clicked = {2}; // Declaration of the array

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadActivity();
    }

    private void loadActivity() {
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

        // Select Menu: Bike Type
        Spinner bikeTypeSpinner = findViewById(R.id.bikeTypeSpinner);
        bikeTypeSpinner.setSelection(SharedPref.Settings.Ride.BikeType.getBikeType(this));
        bikeTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                SharedPref.Settings.Ride.BikeType.setBikeType(position, SettingsActivity1.this);
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
                SharedPref.Settings.Ride.PhoneLocation.setPhoneLocation(position, SettingsActivity1.this);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

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
                        fireIncidentButtonsEnablePrompt();
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
                androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(SettingsActivity1.this).setTitle(R.string.importPromptTitle);
                builder.setMessage(R.string.importButtonText);
                builder.setPositiveButton(R.string.continueText, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
                        chooseFile.setType("application/zip");
                        chooseFile = Intent.createChooser(chooseFile, getString(R.string.importFile));
                        startActivityForResult(chooseFile, FILE_PICKER_IMPORT);
                       // importZipFromLocation.launch(new String[]{"application/zip"});
                    }
                });
                builder.setNegativeButton(R.string.cancel, null);
                builder.show();
            }
        });

        binding.exportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(SettingsActivity1.this).setTitle(R.string.exportPromptTitle);
                builder.setMessage(R.string.exportButtonText);
                builder.setPositiveButton(R.string.continueText, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //exportZipToLocation.launch(Uri.parse(DocumentsContract.PROVIDER_INTERFACE));
                    }
                });
                builder.setNegativeButton(R.string.cancel, null);
                builder.show();
            }
        });

        // Switch: OpenBikeSensor device enabled
        boolean obsActivated = SharedPref.Settings.OpenBikeSensor.isEnabled(this);
        binding.obsButton.setVisibility(obsActivated ? View.VISIBLE : View.GONE);
        activityResultLauncher = activityResultLauncher(SettingsActivity1.this);
        binding.obsButton.setOnClickListener(view ->
        {
            requestBlePermissions(SettingsActivity1.this, REQUEST_ENABLE_BT);
            BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (mBluetoothAdapter == null) {
                // Device does not support Bluetooth
                Toast.makeText(SettingsActivity1.this, R.string.openbikesensor_bluetooth_incompatible, Toast.LENGTH_LONG)
                        .show();
            } else if (!mBluetoothAdapter.isEnabled()) {
                // Bluetooth is disabled
                showBluetoothNotEnableWarning(activityResultLauncher, SettingsActivity1.this);
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
            SharedPref.Settings.OpenBikeSensor.setEnabled(isChecked, SettingsActivity1.this);
            binding.obsButton.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            if (!isChecked && ConnectionManager.INSTANCE.getBleState() == ConnectionManager.BLESTATE.CONNECTED) {
                ConnectionManager.INSTANCE.disconnect(ConnectionManager.INSTANCE.getScanResult().getDevice());
            }
        });
        
        // Version Text
        TextView appVersionTextView = findViewById(R.id.appVersionTextView);
        appVersionTextView.setText("Version: " + BuildConfig.VERSION_CODE + "(" + BuildConfig.VERSION_NAME + ")");

        binding.appVersionTextView.setOnClickListener(v -> showDebugPrompt());
    }

    private void updatePrivacyDistanceSlider(UnitHelper.DISTANCE unit) {
        binding.privacyDistanceSlider.setValueFrom(SharedPref.Settings.Ride.PrivacyDistance.getMinDistance(unit));
        binding.privacyDistanceSlider.setValueTo(SharedPref.Settings.Ride.PrivacyDistance.getMaxDistance(unit));
        binding.privacyDistanceSlider.setValue(SharedPref.Settings.Ride.PrivacyDistance.getDistance(unit, this));
        binding.privacyDistanceTextLeft.setText(SharedPref.Settings.Ride.PrivacyDistance.getMinDistance(unit) + UnitHelper.getShortTranslationForUnit(unit, this));
        binding.privacyDistanceTextRight.setText(SharedPref.Settings.Ride.PrivacyDistance.getMaxDistance(unit) + UnitHelper.getShortTranslationForUnit(unit, this));
    }

    private void fireIncidentButtonsEnablePrompt() {
        AlertDialog.Builder alert = new AlertDialog.Builder(SettingsActivity1.this);
        alert.setTitle(getString(R.string.warning));
        alert.setMessage(getString(R.string.incident_buttons_during_ride_warning));
        alert.setNeutralButton("Ok", (dialog, id) -> {
            // ... (Any actions you want to perform when the user clicks "Ok")
        });
        alert.show();
    }

    private void importZipFromLocation(Uri uri) {
        boolean successfullyImported = importSimRaData(uri, SettingsActivity1.this);
        if (successfullyImported) {
            Toast.makeText(SettingsActivity1.this, R.string.importSuccess, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(SettingsActivity1.this, R.string.importFail, Toast.LENGTH_SHORT).show();
        }
    }

    private void exportZipToLocation(Uri uri) {
        boolean successfullyExported = zipTo(SettingsActivity1.this.getFilesDir().getParent(), uri, SettingsActivity1.this);
        if (successfullyExported) {
            Toast.makeText(SettingsActivity1.this, R.string.exportSuccessToast, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(SettingsActivity1.this, R.string.exportFailToast, Toast.LENGTH_SHORT).show();
        }
    }


    private void showDebugPrompt() {
        AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity1.this)
                .setTitle(R.string.debugPromptTitle2);

        File[] dirFiles = new File(getBaseFolderPath(SettingsActivity1.this)).listFiles();
        List<File> ridesAndAccEvents = prepareRidesAndAccEvents(dirFiles);

        double sizeAllInMB = calculateSize(ridesAndAccEvents);
        double size10InMB = calculateSize(ridesAndAccEvents.subList(0, Math.min(ridesAndAccEvents.size(), 10)));

        CharSequence[] array = buildDebugPromptOptions(sizeAllInMB, size10InMB, ridesAndAccEvents.size() > 10);

        builder.setSingleChoiceItems(array, 2, (dialog, which) -> clicked[0] = which);
        builder.setPositiveButton(R.string.upload, (dialog, which) -> {
            prepareAndUploadDebugZip(clicked[0], ridesAndAccEvents);
            new File(IOUtils.Directories.getBaseFolderPath(SettingsActivity1.this) + "zip.zip").deleteOnExit();
        });
        builder.setNegativeButton(R.string.cancel, null);
        builder.show();
    }

    private List<File> prepareRidesAndAccEvents(File[] dirFiles) {
        List<File> files = Arrays.asList(dirFiles);
        List<File> ridesAndAccEvents = new ArrayList<>();
        sortFileListLastModified(files);

        double sizeAllInMB = 0;
        double size10InMB = 0;
        int i10 = 0;

        for (File file : files) {
            try {
                if (file.getName().contains("accGps")) {
                    int id = Integer.parseInt(file.getName().split("_")[0]);
                    String path = file.getParent() + File.separator + "accEvents" + id + ".csv";
                    File accEvents = new File(path);

                    sizeAllInMB += file.length() / 1024.0 / 1024.0;
                    ridesAndAccEvents.add(file);

                    if (accEvents.exists()) {
                        sizeAllInMB += accEvents.length() / 1024.0 / 1024.0;
                        ridesAndAccEvents.add(accEvents);
                    }

                    if (i10 < 10) {
                        size10InMB = sizeAllInMB;
                        i10++;
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "prepareRidesAndAccEvents() - Exception: " + e.getMessage());
                Log.e(TAG, Arrays.toString(e.getStackTrace()));
            }
        }
        return ridesAndAccEvents;
    }

    private double calculateSize(List<File> files) {
        double sizeInMB = 0;
        for (File file : files) {
            sizeInMB += file.length() / 1024.0 / 1024.0;
        }
        return Math.round(sizeInMB / 3.0 * 100.0) / 100.0;
    }

    private CharSequence[] buildDebugPromptOptions(double sizeAllInMB, double size10InMB, boolean isMoreThan10) {
        CharSequence[] array;
        if (isMoreThan10) {
            array = new CharSequence[]{
                    getText(R.string.debugSendAllRides) + " (" + sizeAllInMB + " MB)",
                    getText(R.string.debugSend10Rides) + " (" + size10InMB + " MB)",
                    getText(R.string.debugDoNotSendRides)
            };
        } else {
            array = new CharSequence[]{
                    getText(R.string.debugSendAllRides) + " (" + sizeAllInMB + " MB)",
                    getText(R.string.debugDoNotSendRides)
            };
        }
        return array;
    }


    private void prepareAndUploadDebugZip(int option, List<File> ridesAndAccEvents) {
        prepareDebugZip(option, ridesAndAccEvents, SettingsActivity1.this);
        Intent intent = new Intent(SettingsActivity1.this, DebugUploadService.class);
        startService(intent);
    }
}
