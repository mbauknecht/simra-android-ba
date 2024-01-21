package de.tuberlin.mcc.simra.app.activities;

import android.os.Bundle;
import android.view.LayoutInflater;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.ActionBar;

import de.tuberlin.mcc.simra.app.R;
import de.tuberlin.mcc.simra.app.databinding.ActivityCreditsBinding;

public class CreditsActivity1 extends AppCompatActivity {

    private static final String TITLE_ACTIVITY_CREDITS = "TitleActivityCredits";
    private ActivityCreditsBinding activityCreditsBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupToolbar();
        setListeners();
    }

    private void setupToolbar() {
        activityCreditsBinding = ActivityCreditsBinding.inflate(LayoutInflater.from(this));
        setContentView(activityCreditsBinding.getRoot());

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
        }
        activityCreditsBinding.toolbar.toolbar.setTitle("");
        activityCreditsBinding.toolbar.toolbar.setSubtitle("");
        activityCreditsBinding.toolbar.toolbarTitle.setText(R.string.title_activity_credits);
    }

    private void setListeners() {
        activityCreditsBinding.toolbar.backButton.setOnClickListener(v -> finish());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //activityCreditsBinding.unbind();
    }
}
