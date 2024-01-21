package de.tuberlin.mcc.simra.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.appbar.CollapsingToolbarLayout;

import de.tuberlin.mcc.simra.app.databinding.ActivityScrollingTestBinding;

public class ScrollingTestActivity extends AppCompatActivity {

    private ActivityScrollingTestBinding binding;

    // Array of activity names
    private String[] activityNames = {
            "AboutActivity",
            "ContactActivity",
            "CreditsActivity",
            "FeedbackActivity",
            "HistoryActivity",
            "LicenseActivity",
            "MainActivity",
            "OpenBikeSensorActivity",
            "ProfileActivity",
            "SettingsActivity",
            "ShowRouteActivity",
            "SingleRideStatisticsActivity",
            "StartActivity",
            "StatisticsActivity",
            "WebActivity"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityScrollingTestBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Setup Toolbar
        Toolbar toolbar = binding.toolbar;
        setSupportActionBar(toolbar);

        // Setup CollapsingToolbarLayout
        CollapsingToolbarLayout toolBarLayout = binding.toolbarLayout;
        toolBarLayout.setTitle(getTitle());

        // Main LinearLayout
        LinearLayout mainLayout = binding.linearLayout;

        // Loop through the array of activity names
        for (String activityName : activityNames) {
            // Create a layout for each entry (title + buttons)
            LinearLayout entryLayout = new LinearLayout(this);
            entryLayout.setOrientation(LinearLayout.VERTICAL);

            // Title TextView
            TextView titleTextView = new TextView(this);
            titleTextView.setText(activityName);
            entryLayout.addView(titleTextView);

            // Button layout
            LinearLayout buttonLayout = new LinearLayout(this);
            buttonLayout.setOrientation(LinearLayout.HORIZONTAL);


            // Left Button
            Button leftButton = createButton("1", activityName+"1");
            buttonLayout.addView(leftButton);

            // Right Button
            Button rightButton = createButton("2", activityName+"2");
            buttonLayout.addView(rightButton);

            // Add the button layout to the entry layout
            entryLayout.addView(buttonLayout);

            // Add the entry layout to the main linear layout
            mainLayout.addView(entryLayout);
        }
    }

    private Button createButton(String buttonText, String activityName) {
        Button button = new Button(this);
        button.setText(buttonText);
        button.setOnClickListener(view -> startSelectedActivity(activityName, buttonText));
        return button;
    }

    private void startSelectedActivity(String activityName, String buttonText) {
        // Replace with your actual package name
        String packageName = "de.tuberlin.mcc.simra.app.activities";
        String className = packageName + "." + activityName;

        try {
            Class<?> activityClass = Class.forName(className);
            Intent intent = new Intent(this, activityClass);
            intent.putExtra("ButtonClicked", buttonText); // Optional: Pass extra data
            startActivity(intent);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
