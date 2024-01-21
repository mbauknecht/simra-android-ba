package de.tuberlin.mcc.simra.app.activities;

import static android.app.ProgressDialog.show;

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

public class ContactActivity1 extends AppCompatActivity {


    private static final String EXTRA_URL = "URL";
    private static final String FEEDBACK_RECEIVER = "feedbackReceiver";
    private static final String FEEDBACK_HEADER = "feedbackHeader";

    private ActivityContactBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityContactBinding.inflate(LayoutInflater.from(this));
        setContentView(binding.getRoot());
        initializeToolbar();
        setListeners();
    }

    private void initializeToolbar() {
        setSupportActionBar(binding.toolbar.toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        binding.toolbar.toolbar.setTitle("");
        binding.toolbar.toolbar.setSubtitle("");
        binding.toolbar.toolbarTitle.setText(R.string.title_activity_contact);
        binding.toolbar.backButton.setOnClickListener(v -> finish());
    }

    private void setListeners() {
        binding.contentContact.buttonProjectSite.setOnClickListener(view -> openWebActivity(getString(R.string.link_simra_Page)));
        binding.contentContact.buttonFeedback.setOnClickListener(v -> sendFeedback());
        binding.contentContact.buttonTwitter.setOnClickListener(v -> openSocialMedia(getString(R.string.link_to_twitter)));
        binding.contentContact.buttonInstagram.setOnClickListener(v -> openSocialMedia(getString(R.string.link_to_instagram)));
    }

    private void openWebActivity(String url) {
        Intent intent = new Intent(ContactActivity1.this, WebActivity.class);
        intent.putExtra(EXTRA_URL, url);
        startActivity(intent);
    }

    private void sendFeedback() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("message/rfc822");
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{getString(R.string.feedbackReceiver)});
        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.feedbackHeader));
        intent.putExtra(Intent.EXTRA_TEXT, getEmailText());
        try {
            startActivity(Intent.createChooser(intent, getString(R.string.feedbackReceiver)));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, "no_email_clients",Toast.LENGTH_SHORT).show();//getString(R.string.no_email_clients)"//getString(R.string.no_email_clients), Toast.LENGTH_SHORT).show();
        }
    }

    private String getEmailText() {
        return getString(R.string.feedbackReceiver) + System.lineSeparator() +
                "App Version: " + BuildConfig.VERSION_CODE + System.lineSeparator() + "Android Version: ";
    }

    private void openSocialMedia(String link) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(link));
        startActivity(intent);
    }
}
