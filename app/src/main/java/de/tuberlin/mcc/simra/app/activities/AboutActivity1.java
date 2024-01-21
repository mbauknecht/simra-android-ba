package de.tuberlin.mcc.simra.app.activities;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.content.Intent;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import de.tuberlin.mcc.simra.app.R;
import de.tuberlin.mcc.simra.app.databinding.ActivityAboutBinding;
import de.tuberlin.mcc.simra.app.util.BaseActivity;

public class AboutActivity1 extends BaseActivity {

    private static final String EXTRA_URL = "URL";

    ActivityAboutBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityAboutBinding.inflate(LayoutInflater.from(this));
        setContentView(binding.getRoot());

        setupToolbar();

        setupListView();

        setupBackButtonListener();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setTitle("");
        toolbar.setSubtitle("");
        binding.toolbar.toolbarTitle.setText(R.string.title_activity_about_simra);
    }

    private void setupListView() {
        String[] items = getResources().getStringArray(R.array.aboutSimraItems);
        binding.listView.setAdapter(new ArrayAdapter<>(AboutActivity1.this,
                android.R.layout.simple_list_item_1, items));

        binding.listView.setOnItemClickListener((parent, view, position, id) -> handleListItemClick(position));
    }

    private void setupBackButtonListener() {
        binding.toolbar.backButton.setOnClickListener(v -> finish());
    }

    private void handleListItemClick(int position) {
        Intent intent = null;
        switch (position) {
            case 0:
            case 1:
                intent = createWebActivityIntent(position);
                break;
            case 2:
                intent = new Intent(AboutActivity1.this, LicenseActivity.class);
                break;
            case 3:
                intent = new Intent(AboutActivity1.this, CreditsActivity.class);
                break;
            default:
                Toast.makeText(AboutActivity1.this, R.string.notReady, Toast.LENGTH_SHORT).show();
        }
        if (intent != null) {
            startActivity(intent);
        }
    }

    private Intent createWebActivityIntent(int position) {
        Intent intent = new Intent(AboutActivity1.this, WebActivity.class);
        intent.putExtra(EXTRA_URL, position == 0 ? getString(R.string.link_simra_Page) : getString(R.string.privacyLink));
        return intent;
    }
}