package de.tuberlin.mcc.simra.app.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import de.tuberlin.mcc.simra.app.BuildConfig;
import de.tuberlin.mcc.simra.app.R;
import de.tuberlin.mcc.simra.app.databinding.ActivityFeedbackBinding;

// ... imports

public class FeedbackActivity2 extends AppCompatActivity {

    private static final String INTENT_KEY_URL = "URL";
    private static final String INTENT_KEY_EMAIL = "message/rfc822";

    ActivityFeedbackBinding feedbackBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        feedbackBinding = ActivityFeedbackBinding.inflate(LayoutInflater.from(this));
        setContentView(feedbackBinding.getRoot());

        setupToolbar();
        setupListView();
    }

    private void setupToolbar() {
        Toolbar toolbar = feedbackBinding.toolbar.toolbar;
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        // feedbackBinding.toolbar.backButton.setOnClickListener(this::finish); v1
        feedbackBinding.toolbar.backButton.setOnClickListener(v -> FeedbackActivity2.this.finish());

    }

    private void setupListView() {
        String[] items = getResources().getStringArray(R.array.ContactItems);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, items);

        feedbackBinding.listView.setAdapter(adapter);
        feedbackBinding.listView.setOnItemClickListener(this::handleListItemClick);
    }

    private void handleListItemClick(AdapterView<?> parent, View view, int position, long id) {

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
        return new Intent(this, WebActivity.class).putExtra(INTENT_KEY_URL, url);
    }

    private Intent createEmailIntent() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType(INTENT_KEY_EMAIL);
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{getString(R.string.feedbackReceiver)});
        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.feedbackHeader));
        intent.putExtra(Intent.EXTRA_TEXT, getFeedbackEmailBody());

        return intent;
    }

    private String getFeedbackEmailBody() {
        return getString(R.string.feedbackReceiver) + System.lineSeparator() +
                "App Version: " + BuildConfig.VERSION_CODE + System.lineSeparator() +
                "Android Version: " + Build.VERSION.RELEASE;
    }

    private Intent createSocialMediaIntent(String link) {
        return new Intent(Intent.ACTION_VIEW, Uri.parse(link));
    }
}
