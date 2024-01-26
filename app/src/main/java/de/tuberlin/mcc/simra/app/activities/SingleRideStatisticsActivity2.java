/*package de.tuberlin.mcc.simra.app.activities;

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
import static de.tuberlin.mcc.simra.app.entities.MetaData.getMetaDataEntryForRide;
import static de.tuberlin.mcc.simra.app.util.Utils.calculateCO2Savings;

public class SingleRideStatisticsActivity2 extends AppCompatActivity {

    private static final String TAG = "RideStatsActivity_LOG";
    private static final String EXTRA_RIDE_ID = "EXTRA_RIDE_ID";
    private static final int SECONDS_IN_HOUR = 3600;
    private static final int MILLISECONDS_IN_HOUR = 3600000;

    private ImageButton backBtn;
    private TextView toolbarTxt;
    private int rideId;
    private ActivitySingleRideStatisticsBinding binding;

    public static void startSingleRideStatisticsActivity2(int rideId, Context context) {
        Intent intent = new Intent(context, SingleRideStatisticsActivity2.class);
        intent.putExtra(EXTRA_RIDE_ID, rideId);
        context.startActivity(intent);
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySingleRideStatisticsBinding.inflate(LayoutInflater.from(this));
        setContentView(binding.getRoot());
        setupToolbar();

        if (!getIntent().hasExtra(EXTRA_RIDE_ID)) {
            throw new RuntimeException("Extra not defined: " + EXTRA_RIDE_ID);
        }

        rideId = getIntent().getIntExtra(EXTRA_RIDE_ID, 0);

        MetaDataEntry metaDataEntry = getMetaDataEntryForRide(rideId, SingleRideStatisticsActivity2.this);
        displayRideStatistics(metaDataEntry);
    }

    private void setupToolbar() {
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

    private void displayRideStatistics(MetaDataEntry metaDataEntry) {
        boolean isImperialUnit = SharedPref.Settings.DisplayUnit.isImperial(this);
        int distanceDivider = isImperialUnit ? 1600 : 1000;
        String distanceUnit = isImperialUnit ? " mi" : " km";
        String speedUnit = isImperialUnit ? " mph" : " km/h";

        TextView distanceOfRide = binding.distanceOfRideText;
        double distanceOfRideRaw = ((double) metaDataEntry.distance) / distanceDivider;
        distanceOfRide.setText(getString(R.string.distance) + " " + (Math.round(distanceOfRideRaw * 100.0) / 100.0) + distanceUnit);

        // ... (similar changes for other TextViews)
        TextView co2SavingsText = binding.co2SavingsText; // manually corrected

        co2SavingsText.setText(getString(R.string.co2Savings) + " " + (Math.round((double) calculateCO2Savings(metaDataEntry.distance)) + " g"));
        co2SavingsText.invalidate();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy() called");
    }
}
