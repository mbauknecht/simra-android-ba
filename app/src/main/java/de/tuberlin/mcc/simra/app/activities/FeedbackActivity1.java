package de.tuberlin.mcc.simra.app.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import de.tuberlin.mcc.simra.app.BuildConfig;
import de.tuberlin.mcc.simra.app.R;
import de.tuberlin.mcc.simra.app.databinding.ActivityFeedbackBinding;

public class FeedbackActivity1 extends AppCompatActivity {
    ActivityFeedbackBinding binding;

    // ... (other class-level declarations, if any)


    // Constants for Intent keys
    private static final String EXTRA_URL = "URL";

    // ... (existing code)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityFeedbackBinding.inflate(LayoutInflater.from(this));
        setContentView(binding.getRoot());

        setupToolbar();
        setupListView();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setTitle("");
        toolbar.setSubtitle("");
        binding.toolbar.toolbarTitle.setText(R.string.title_activity_about_simra);

        binding.toolbar.backButton.setOnClickListener(v -> finish());
    }

    private void setupListView() {
        String[] items = getResources().getStringArray(R.array.ContactItems);
        binding.listView.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, items));
        binding.listView.setOnItemClickListener((parent, view, position, id) -> handleItemClick(position));
    }

    private void handleItemClick(int position) {
        Intent intent = null;
        switch (position) {
            case 0:
                intent = createWebIntent(getString(R.string.link_simra_Page));
                break;
            case 1:
                intent = createEmailIntent();
                break;
            case 2:
                intent = createSocialMediaIntent(getString(R.string.link_to_twitter));
                break;
            case 3:
                intent = createSocialMediaIntent(getString(R.string.link_to_instagram));
                break;
            default:
                Toast.makeText(this, R.string.notReady, Toast.LENGTH_SHORT).show();
        }

        if (intent != null) {
            startActivity(intent);
        }
    }

    private Intent createWebIntent(String url) {
        Intent intent = new Intent(this, WebActivity.class);
        intent.putExtra(EXTRA_URL, url);
        return intent;
    }

    private Intent createEmailIntent() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("message/rfc822");
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{getString(R.string.feedbackReceiver)});
        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.feedbackHeader));
        intent.putExtra(Intent.EXTRA_TEXT, getEmailBody());
        try {
            startActivity(Intent.createChooser(intent, "Send mail..."));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
        }
        return intent;
    }

    private Intent createSocialMediaIntent(String link) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(link));
        return intent;
    }

    private String getEmailBody() {
        return getString(R.string.feedbackReceiver) + System.lineSeparator()
                + "App Version: " + BuildConfig.VERSION_CODE + System.lineSeparator() + "Android Version: ";
    }
}


