/*package de.tuberlin.mcc.simra.app.activities;


import static de.tuberlin.mcc.simra.app.entities.MetaData.getMetaDataEntryForRide;
import static de.tuberlin.mcc.simra.app.util.Utils.calculateCO2Savings;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import de.tuberlin.mcc.simra.app.R;
import de.tuberlin.mcc.simra.app.databinding.ActivitySingleRideStatisticsBinding;
import de.tuberlin.mcc.simra.app.entities.MetaDataEntry;
import de.tuberlin.mcc.simra.app.util.SharedPref;

// ... (imports remain unchanged)

public class SingleRideStatisticsActivity1 extends AppCompatActivity {

    private static final String TAG = "RideStatsActivity_LOG";
    private static final String EXTRA_RIDE_ID = "EXTRA_RIDE_ID";
    private static final int MILLISECONDS_IN_HOUR = 3600000;
    private static final int SECONDS_IN_HOUR = 3600;
    private static final int METERS_IN_KILOMETER = 1000;
    private static final int METERS_IN_MILE = 1600;

    private ImageButton backBtn;
    private TextView toolbarTxt;
    private int rideId;
    private ActivitySingleRideStatisticsBinding binding;

    public static void startSingleRideStatisticsActivity1(int rideId, Context context) {
        Intent intent = new Intent(context, SingleRideStatisticsActivity1.class);
        intent.putExtra(EXTRA_RIDE_ID, rideId);
        context.startActivity(intent);
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySingleRideStatisticsBinding.inflate(LayoutInflater.from(this));
        setContentView(binding.getRoot());
        initializeToolbar();
        initializeUI();
        processIntent();
        displayRideStatistics();
    }

    private void initializeToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setTitle("");
        toolbar.setSubtitle("");
        toolbarTxt = findViewById(R.id.toolbar_title);
        toolbarTxt.setText(R.string.title_activity_statistics);
        backBtn = findViewById(R.id.back_button);
        backBtn.setOnClickListener(v -> finish());
    }

    private void initializeUI() {
        // Initialize UI elements here if needed
    }

    private void processIntent() {
        if (!getIntent().hasExtra(EXTRA_RIDE_ID)) {
            throw new RuntimeException("Extra: " + EXTRA_RIDE_ID + " not defined.");
        }
        rideId = getIntent().getIntExtra(EXTRA_RIDE_ID, 0);
    }

    private double calculateAverageSpeed(long distance, long startTime, long endTime, long waitedTime, int distanceDivider) {
        // Calculate duration
        long duration = endTime - startTime;

        // Existing logic for average speed calculation
        double averageSpeedRaw = ((((double) distance) / (double) distanceDivider) /
                ((((double) duration / (double) 1000) - ((double) waitedTime)) / (double) 3600));

        return averageSpeedRaw;
    }

    private void displayRideStatistics() {
        MetaDataEntry metaDataEntry = getMetaDataEntryForRide(rideId, this);
        if (metaDataEntry == null) {
            // Handle the case where MetaDataEntry is null
            return;
        }

        // Distance and unit conversion
        int distanceDivider = SharedPref.Settings.DisplayUnit.isImperial(this) ? METERS_IN_MILE : METERS_IN_KILOMETER;
        String distanceUnit = SharedPref.Settings.DisplayUnit.isImperial(this) ? " mi" : " km";
        String speedUnit = SharedPref.Settings.DisplayUnit.isImperial(this) ? " mph" : " km/h";

        // ... (remaining logic remains unchanged)

        // total distance of this ride
        TextView distanceOfRide = binding.distanceOfRideText;
        double distanceOfRideRaw = (((double) metaDataEntry.distance) / distanceDivider);
        distanceOfRide.setText(getText(R.string.distance) + " " + (Math.round(distanceOfRideRaw * 100.0) / 100.0) + distanceUnit);
        distanceOfRide.invalidate();

        // duration of this ride in HH:MM
        TextView durationOfRide = binding.durationOfRideText;
        //duration in ms
        long duration = metaDataEntry.endTime - metaDataEntry.startTime;
        long rideDurationHours = (duration) / 3600000;
        long rideDurationMinutes = ((duration) % 3600000) / 60000;
        String rideDurationH;
        String rideDurationM;
        if (rideDurationHours < 10) {
            rideDurationH = "0" + rideDurationHours;
        } else {
            rideDurationH = String.valueOf(rideDurationHours);
        }
        if (rideDurationMinutes < 10) {
            rideDurationM = "0" + rideDurationMinutes;
        } else {
            rideDurationM = String.valueOf(rideDurationMinutes);
        }
        durationOfRide.setText(getText(R.string.duration) + " " + rideDurationH + ":" + rideDurationM + " h");
        durationOfRide.invalidate();

        // total duration of waited time in this rides in HH:MM
        TextView durationOfWaitedTime = binding.durationOfIdleText;
        long waitDurationHours = (metaDataEntry.waitedTime / 3600);
        long waitDurationMinutes = (metaDataEntry.waitedTime % 3600) / 60;

        String waitDurationH;
        String waitDurationM;
        if (waitDurationHours < 10) {
            waitDurationH = "0" + waitDurationHours;
        } else {
            waitDurationH = String.valueOf(waitDurationHours);
        }
        if (waitDurationMinutes < 10) {
            waitDurationM = "0" + waitDurationMinutes;
        } else {
            waitDurationM = String.valueOf(waitDurationMinutes);
        }
        durationOfWaitedTime.setText(getText(R.string.idle) + " " + waitDurationH + ":" + waitDurationM + " h");
        durationOfWaitedTime.invalidate();

        // amount of co2 emissions saved by taking a bicycle instead of a car (138g/km)
        TextView co2SavingsText = binding.co2SavingsText;

        co2SavingsText.setText(getText(R.string.co2Savings) + " " + (Math.round((double) calculateCO2Savings(metaDataEntry.distance)) + " g"));

        co2SavingsText.invalidate();

        // Distance and unit conversion
        distanceDivider = SharedPref.Settings.DisplayUnit.isImperial(this) ? METERS_IN_MILE : METERS_IN_KILOMETER;  // manually corrected

        // average speed of per ride of all uploaded rides
        TextView averageSpeedText = binding.averageSpeedText;
        double averageSpeedRaw = calculateAverageSpeed(metaDataEntry.distance, metaDataEntry.startTime, metaDataEntry.endTime, metaDataEntry.waitedTime, distanceDivider);
        averageSpeedText.setText(getText(R.string.average_Speed) + " " + (Math.round(averageSpeedRaw * 100.0) / 100.0) + speedUnit);
        averageSpeedText.invalidate();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy() called");
    }
}
