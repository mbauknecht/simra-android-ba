/*package de.tuberlin.mcc.simra.app.activities;

import android.os.Bundle;
import android.view.LayoutInflater;

import androidx.appcompat.app.AppCompatActivity;

import de.tuberlin.mcc.simra.app.R;
import de.tuberlin.mcc.simra.app.databinding.ActivityCreditsBinding;

public class CreditsActivity2 extends AppCompatActivity {

    private static final String EMPTY_STRING = "";
    private ActivityCreditsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCreditsBinding.inflate(LayoutInflater.from(this));
        setContentView(binding.getRoot());

        initializeToolbar();
        setupBackButton();
    }

    private void initializeToolbar() {
        setSupportActionBar(binding.toolbar.toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        binding.toolbar.toolbarTitle.setText(R.string.title_activity_credits);
        //binding.toolbar.toolbarTitle.setText(R.string.credits_activity_title);
    }

    private void setupBackButton() {
        binding.toolbar.backButton.setOnClickListener(v -> finish());
    }
}
