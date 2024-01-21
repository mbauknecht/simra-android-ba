package de.tuberlin.mcc.simra.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import de.tuberlin.mcc.simra.app.R;
import de.tuberlin.mcc.simra.app.databinding.ActivityAboutBinding;
import de.tuberlin.mcc.simra.app.util.BaseActivity;

public class AboutActivity2 extends BaseActivity {

    private static final String EXTRA_URL = "URL";

    ActivityAboutBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setupViews();
        setupListView();
    }

    private void setupViews() {
        binding = ActivityAboutBinding.inflate(LayoutInflater.from(this));
        setContentView(binding.getRoot());

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setTitle("");
        toolbar.setSubtitle("");
        binding.toolbar.toolbarTitle.setText(R.string.title_activity_about_simra);

        binding.toolbar.backButton.setOnClickListener(v -> finish());
    }

    private void setupListView() {
        String[] items = getResources().getStringArray(R.array.aboutSimraItems);
        binding.listView.setAdapter(new ArrayAdapter<>(AboutActivity2.this,
                android.R.layout.simple_list_item_1, items));
        binding.listView.setOnItemClickListener((parent, view, position, id) -> handleListItemClick(position));
    }

    private void handleListItemClick(int position) {
        Intent intent = null;
        switch (position) {
            case 0:
                intent = createWebActivityIntent(getString(R.string.link_simra_Page));
                break;
            case 1:
                intent = createWebActivityIntent(getString(R.string.privacyLink));
                break;
            case 2:
                intent = new Intent(AboutActivity2.this, LicenseActivity.class);
                break;
            case 3:
                intent = new Intent(AboutActivity2.this, CreditsActivity.class);
                break;
            default:
                showToast(R.string.notReady);
        }
        if (intent != null) {
            startActivity(intent);
        }
    }

    private Intent createWebActivityIntent(String url) {
        Intent intent = new Intent(AboutActivity2.this, WebActivity.class);
        intent.putExtra(EXTRA_URL, url);
        return intent;
    }

    private void showToast(int messageId) {
        Toast.makeText(AboutActivity2.this, messageId, Toast.LENGTH_SHORT).show();
    }
}