package de.tuberlin.mcc.simra.app.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import de.tuberlin.mcc.simra.app.BuildConfig;
import de.tuberlin.mcc.simra.app.R;
import de.tuberlin.mcc.simra.app.databinding.ActivityContactBinding;

public class ContactActivity2 extends AppCompatActivity {


    ActivityContactBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityContactBinding.inflate(LayoutInflater.from(this));
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar.toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        binding.toolbar.toolbar.setTitle("");
        binding.toolbar.toolbar.setSubtitle("");
        binding.toolbar.toolbarTitle.setText(R.string.title_activity_contact);

        setupUI();
        setButtonClickListeners();
    }

    private void setupUI() {
        binding.toolbar.backButton.setOnClickListener(v -> finish());
    }

    private void setButtonClickListeners() {
        binding.contentContact.buttonProjectSite.setOnClickListener(v -> openWebPage(getString(R.string.link_simra_Page)));
        binding.contentContact.buttonFeedback.setOnClickListener(v -> sendFeedbackEmail());
        binding.contentContact.buttonTwitter.setOnClickListener(v -> openWebPage(getString(R.string.link_to_twitter)));
        binding.contentContact.buttonInstagram.setOnClickListener(v -> openWebPage(getString(R.string.link_to_instagram)));
    }

    private void openWebPage(String url) {
        Intent intent = new Intent(ContactActivity2.this, WebActivity.class);
        intent.putExtra("URL", url);
        startActivity(intent);
    }

    private void sendFeedbackEmail() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("message/rfc822");
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{getString(R.string.feedbackReceiver)});
        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.feedbackHeader));
        intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.feedbackReceiver) + System.lineSeparator()
                + "App Version: " + BuildConfig.VERSION_CODE + System.lineSeparator() + "Android Version: ");

        try {
            startActivity(Intent.createChooser(intent, "Send mail..."));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(ContactActivity2.this, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
        }
    }
}
