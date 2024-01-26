/*package de.tuberlin.mcc.simra.app.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import de.tuberlin.mcc.simra.app.R;
import de.tuberlin.mcc.simra.app.databinding.ActivityProfileBinding;
import de.tuberlin.mcc.simra.app.entities.Profile;
import de.tuberlin.mcc.simra.app.util.PermissionHelper;
import de.tuberlin.mcc.simra.app.util.SharedPref;

import static de.tuberlin.mcc.simra.app.util.Utils.*;

public class ProfileActivity2 extends AppCompatActivity {

    ActivityProfileBinding binding;

    private static final String EXTRA_PROFILE = "EXTRA_PROFILE";
    private static final String TAG = "ProfileActivity2_LOG";
    private static final String LOCATION_SERVICE_OFF = "Location service is off. Enable it to detect region.";

    Profile profile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityProfileBinding.inflate(LayoutInflater.from(this));

        setSupportActionBar(binding.toolbar.toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        binding.toolbar.toolbar.setTitle("");
        binding.toolbar.toolbar.setSubtitle("");
        binding.toolbar.toolbarTitle.setText(R.string.title_activity_profile);
        binding.toolbar.backButton.setOnClickListener(v -> finish());

        profile = Profile.loadProfile(null, this);
        String[] simRa_regions_config = getRegions(this);

        if (profile.region >= simRa_regions_config.length) {
            profile.region = 0;
        }

        ArrayList<String> regionContentArray = getRegionContentArray(simRa_regions_config);
        setupRegionSpinner(regionContentArray);

        // Get the previous saved settings
        setupProfileSettings();

        binding.profileContent.activateBehaviourSeekBarButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
            binding.profileContent.behaviourSeekBar.setEnabled(isChecked);
        });
    }

    private ArrayList<String> getRegionContentArray(String[] simRa_regions_config) {
        ArrayList<String> regionContentArray = new ArrayList<>();
        for (String s : simRa_regions_config) {
            if (!(s.startsWith("!") || s.startsWith("Please Choose"))) {
                regionContentArray.add(getCorrectRegionName(s));
            }
        }
        Collections.sort(regionContentArray);
        regionContentArray.add(0, getText(R.string.pleaseChoose).toString());
        return regionContentArray;
    }

    private void setupProfileSettings() {
        binding.profileContent.ageGroupSpinner.setSelection(profile.ageGroup);
        binding.profileContent.genderSpinner.setSelection(profile.gender);

        if (getIntent().hasExtra(EXTRA_PROFILE)) {
            setProfileRegionStyling();
        }

        toggleRegionSwitch(getRegionContentArray(getRegions(this)));

        binding.profileContent.switchRegion.setOnClickListener(v -> {
            SharedPref.Settings.RegionSetting.setRegionDetectionViaGPSEnabled(binding.profileContent.switchRegion.isChecked(), this);
            toggleRegionSwitch(getRegionContentArray(getRegions(this)));
        });
    }

    private void setProfileRegionStyling() {
        binding.profileContent.profileRegionLinearLayout.setPadding(10, 10, 10, 10);
        binding.profileContent.profileRegionLinearLayout.setBackground(ContextCompat.getDrawable(ProfileActivity2.this, R.drawable.profile_region_border));
    }

    private void toggleRegionSwitch(ArrayList<String> regionContentArray) {
        String region = getCorrectRegionName(getRegions(this)[profile.region]);

        if (!region.startsWith("!")) {
            binding.profileContent.regionSpinner.setSelection(regionContentArray.indexOf(region));
        } else {
            binding.profileContent.regionSpinner.setSelection(0);
        }

        if (SharedPref.Settings.RegionSetting.getRegionDetectionViaGPSEnabled(this)) {
            setupAutoRegionDetection(regionContentArray);
        } else {
            binding.profileContent.regionAutomaticRelativeLayout.setVisibility(View.GONE);
            binding.profileContent.regionSpinner.setVisibility(View.VISIBLE);
        }

        setContentView(binding.getRoot());
    }

    private void setupAutoRegionDetection(ArrayList<String> regionContentArray) {
        binding.profileContent.regionSpinner.setVisibility(View.GONE);
        binding.profileContent.switchRegion.setChecked(true);
        binding.profileContent.regionAutomaticRelativeLayout.setVisibility(View.VISIBLE);

        binding.profileContent.regionAutomaticButton.setOnClickListener(v -> {
            handleAutoRegionDetection(regionContentArray);
        });
    }

    @SuppressLint("MissingPermission")
    private void handleAutoRegionDetection(ArrayList<String> regionContentArray) {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (!PermissionHelper.hasBasePermissions(this)) {
            PermissionHelper.requestFirstBasePermissionsNotGranted(this);
        } else {
            handleLocationServiceStatus(locationManager, regionContentArray);
        }
    }

    private void handleLocationServiceStatus(LocationManager locationManager, ArrayList<String> regionContentArray) {
        if (isLocationServiceOff(locationManager)) {
            showLocationServiceOffDialog();
        } else {
            try {
                handleNearestRegionDetection(locationManager, regionContentArray);
            } catch (NullPointerException npe) {
                handleAutoRegionDetectionException(npe);
            }
        }
    }

    private void handleAutoRegionDetectionException(NullPointerException npe) {
        Log.e(TAG, "Automatic region detection - Exception: " + npe.getMessage());
        Log.e(TAG, Arrays.toString(npe.getStackTrace()));
        npe.printStackTrace();
        Toast.makeText(this, R.string.try_later, Toast.LENGTH_SHORT).show();
    }

    private void showLocationServiceOffDialog() {
        new AlertDialog.Builder(this)
                .setMessage((LOCATION_SERVICE_OFF + " " + R.string.enableToDetectRegion))
                .setPositiveButton(android.R.string.ok,
                        (paramDialogInterface, paramInt) -> startLocationSettingsActivity())
                .setNegativeButton(R.string.cancel, null).show();
    }

    private void startLocationSettingsActivity() {
        startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
    }

    private void handleNearestRegionDetection(LocationManager locationManager, ArrayList<String> regionContentArray) {
        int[] nearestRegionCodes = nearestRegionsToThisLocation(
                locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER).getLatitude(),
                locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER).getLongitude(),
                this);

        String[] nearestRegionNames = getCorrectRegionNames(regionsDecoder(nearestRegionCodes, this));

        showNearestRegionAlertDialog(nearestRegionNames, regionContentArray);
    }

    private void showNearestRegionAlertDialog(String[] nearestRegionNames, ArrayList<String> regionContentArray) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this).setTitle(R.string.nearestRegions);
        createButtons(builder, nearestRegionNames, regionContentArray);
        builder.setNegativeButton(R.string.cancel, null);
        builder.create().show();
    }

    private void createButtons(AlertDialog.Builder builder, String[] nearestRegionNames, ArrayList<String> regionContentArray) {
        builder.setItems(nearestRegionNames,
                (dialog, which) -> {
                    profile.region = regionEncoder(nearestRegionNames[which], this);
                    binding.profileContent.regionSpinner.setSelection(regionContentArray.indexOf(nearestRegionNames[which]));
                    toggleRegionSwitch(regionContentArray);
                });
    }

    @Override
    protected void onStop() {
        super.onStop();

        saveProfileSettings();
    }

    private void saveProfileSettings() {
        profile.ageGroup = binding.profileContent.ageGroupSpinner.getSelectedItemPosition();
        profile.gender = binding.profileContent.genderSpinner.getSelectedItemPosition();
        String selectedRegion = binding.profileContent.regionSpinner.getSelectedItem().toString();
        profile.region = regionEncoder(selectedRegion, this);
        profile.experience = binding.profileContent.experienceSpinner.getSelectedItemPosition();

        if (!binding.profileContent.behaviourSeekBar.isEnabled()) {
            profile.behaviour = -1;
        } else {
            profile.behaviour = binding.profileContent.behaviourSeekBar.getProgress();
        }

        Profile.saveProfile(profile, null, this);
    }

    private void setupRegionSpinner(ArrayList<String> regionContentArray) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, regionContentArray);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.profileContent.regionSpinner.setAdapter(adapter);
    }
}
