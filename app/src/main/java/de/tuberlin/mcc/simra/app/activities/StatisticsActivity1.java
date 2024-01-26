/*package de.tuberlin.mcc.simra.app.activities;


import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import de.tuberlin.mcc.simra.app.R;
import de.tuberlin.mcc.simra.app.entities.Profile;
import de.tuberlin.mcc.simra.app.util.SharedPref;

// Import statements remain unchanged

public class StatisticsActivity1 extends AppCompatActivity {
    private static final String LOG_TAG = "StatisticsActivity1_LOG";

    private ImageButton backBtn;
    private TextView toolbarTxt;
    private BarChart chart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        setupToolbar();
        setupUI();
        setupChartData();
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

    private void setupUI() {
        // Remaining UI setup code remains unchanged
        Boolean isImperialUnit = SharedPref.Settings.DisplayUnit.isImperial(this);
        String locale = Resources.getSystem().getConfiguration().locale.getLanguage();

        Profile profile = Profile.loadProfile(null, this);
        int ridesCount = profile.numberOfRides;

        updateTextView(R.id.numberOfRidesText, R.string.uploaded_rides, profile.numberOfRides);
        updateTextView(R.id.co2SavingsText, R.string.co2Savings, (Math.round(((double) (long) profile.co2 / 1000.0) * 100.0) / 100.0) + " kg");
        updateTextView(R.id.numberOFIncidentsText, R.string.incidents, profile.numberOfIncidents);
        updateTextView(R.id.numberOfScaryIncidentsText, R.string.scary, profile.numberOfScaryIncidents);

        updateDistanceTextView(R.id.distanceOfRidesText, R.string.distance, profile.distance, isImperialUnit);
        updateAverageDistanceTextView(R.id.averageDistanceOfRidesText, R.string.avgDistance, profile.distance, ridesCount, isImperialUnit);

      /*  updateDurationTextView(R.id.durationOfRidesText, R.string.duration, profile.duration);
        updateAverageSpeedTextView(R.id.averageSpeedText, R.string.average_Speed, profile.distance, profile.duration, profile.waitedTime, isImperialUnit);

        updateDurationTextView(R.id.durationOfIdleText, R.string.idle, profile.waitedTime);
        updateAverageDurationOfIdleTextView(R.id.averageDurationOfIdleText, R.string.avgIdle, profile.waitedTime, ridesCount);
         //  manually corrected */
   /*     updateTextView(R.id.durationOfRidesText, R.string.duration, profile.duration);
       // updateTextView(R.id.averageSpeedText, R.string.average_Speed, profile.distance, profile.duration, profile.waitedTime, isImperialUnit);

        updateTextView(R.id.durationOfIdleText, R.string.idle, profile.waitedTime);
      //  updateTextView(R.id.averageDurationOfIdleText, R.string.avgIdle, profile.waitedTime, ridesCount);

    }

    private void updateTextView(int textViewId, int stringResourceId, Object value) {
        TextView textView = findViewById(textViewId);
        textView.setText(getString(stringResourceId) + " " + value);
        textView.invalidate();
    }

    private void updateDistanceTextView(int textViewId, int stringResourceId, double distance, boolean isImperialUnit) {
        TextView distanceTextView = findViewById(textViewId);
        double convertedDistance = isImperialUnit ? (distance / 1600.0) : (distance / 1000.0);
        String unit = isImperialUnit ? "mi" : "km";
        distanceTextView.setText(getString(stringResourceId) + " " + (Math.round(convertedDistance * 100.0) / 100.0) + " " + unit);
        distanceTextView.invalidate();
    }

    private void updateAverageDistanceTextView(int textViewId, int stringResourceId, double distance, int ridesCount, boolean isImperialUnit) {
        TextView avgDistanceTextView = findViewById(textViewId);
        if (ridesCount > 0) {
            double avgDistance = isImperialUnit ? ((distance / 1600.0) / ridesCount) : ((distance / 1000.0) / ridesCount);
            String unit = isImperialUnit ? "mi" : "km";
            avgDistanceTextView.setText(getString(stringResourceId) + " " + (Math.round(avgDistance * 100.0) / 100.0) + " " + unit);
        } else {
            avgDistanceTextView.setText(getString(stringResourceId) + " -");
        }
        avgDistanceTextView.invalidate();
    }


    private void setupChartData() {
        // Remaining chart setup code remains unchanged
        String locale = Resources.getSystem().getConfiguration().locale.getLanguage();
        Profile profile = Profile.loadProfile(null, this);

        chart = findViewById(R.id.timeBucketBarChart);
        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setLabelsToSkip(0);
        List<BarEntry> entries = new ArrayList<>();
        int counter = 0;
        for (Float f : profile.timeDistribution) {
            entries.add(new BarEntry(f, counter));
            if (f == 0.0) {
                xAxis.setPosition(XAxis.XAxisPosition.BOTTOM_INSIDE);
            }
            counter++;
        }

        BarDataSet bardataset = new BarDataSet(entries, null);

        List<String> labels = new ArrayList<>();

        if (locale.equals(new Locale("en").getLanguage())) {
            labels.add("12am");
            labels.add("01");
            labels.add("02");
            labels.add("03");
            labels.add("04");
            labels.add("05");
            labels.add("06");
            labels.add("07");
            labels.add("08");
            labels.add("09");
            labels.add("10");
            labels.add("11");
            labels.add("12");
            labels.add("01pm");
            labels.add("02");
            labels.add("03");
            labels.add("04");
            labels.add("05");
            labels.add("06");
            labels.add("07");
            labels.add("08");
            labels.add("09");
            labels.add("10");
            labels.add("11");
        } else {
            labels.add("00");
            labels.add("01");
            labels.add("02");
            labels.add("03");
            labels.add("04");
            labels.add("05");
            labels.add("06");
            labels.add("07");
            labels.add("08");
            labels.add("09");
            labels.add("10");
            labels.add("11");
            labels.add("12");
            labels.add("13");
            labels.add("14");
            labels.add("15");
            labels.add("16");
            labels.add("17");
            labels.add("18");
            labels.add("19");
            labels.add("20");
            labels.add("21");
            labels.add("22");
            labels.add("23");
        }
        BarData data = new BarData(labels, bardataset);
        chart.setData(data); // set the data and list of lables into chart

        // bardataset.setColors(ColorTemplate.PASTEL_COLORS);
        bardataset.setColor(getResources().getColor(R.color.colorAccent, this.getTheme()));
        bardataset.setDrawValues(false);

        chart.animateY(2000);
        chart.getLegend().setEnabled(false);
        chart.setDescription(null);
        chart.setPinchZoom(false);
        chart.setDrawBarShadow(false);
        chart.setDrawGridBackground(false);
        chart.getAxisLeft().setEnabled(false);
        chart.getAxisRight().setEnabled(false);
        chart.setDoubleTapToZoomEnabled(false);
        chart.setHighlightPerTapEnabled(false);
        chart.setHighlightPerDragEnabled(false);

        chart.invalidate();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(LOG_TAG, "onDestroy() called");
    }
}
