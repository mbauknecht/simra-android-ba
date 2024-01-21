// Import statements...
package de.tuberlin.mcc.simra.app.activities;

import android.app.Dialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.app.AppCompatActivity;
import de.tuberlin.mcc.simra.app.R;

public class LicenseActivity1 extends AppCompatActivity {

    private ImageButton backBtn;
    private TextView toolbarTxt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_license);
        setupToolbar();
        setupLibraryButton(R.id.android_support_library, "licenseandroidsupportlibrary.txt", "Android Support Library");
        setupLibraryButton(R.id.apache_commons_library, "licenseapachecommons.txt", "Apache Commons");
        setupLibraryButton(R.id.fbase_library, "licensefbase.txt", "FBase");
        setupLibraryButton(R.id.gson_library, "licensegson.txt", "Gson");
        setupLibraryButton(R.id.javax_activation_library, "licensejavaxactivation.txt", "Javax Activation");
        setupLibraryButton(R.id.jersey_library, "licensejersey.txt", "Jersey");
        setupLibraryButton(R.id.jetty_library, "licensejetty.txt", "Jetty");
        setupLibraryButton(R.id.logback_library, "licenselogback.txt", "Logback");
        setupLibraryButton(R.id.mpandroidchart_library, "licensesmpandroidchart.txt", "MPAndroidChart");
        setupLibraryButton(R.id.okhttp_library, "licenseokhttp.txt", "OkHttp");
        setupLibraryButton(R.id.osmbonuspack_library, "licenseosmbonuspack.txt", "Osmbonuspack");
        setupLibraryButton(R.id.osmdroid_library, "licenseosmdroid.txt", "Osmdroid");
        setupLibraryButton(R.id.rangeseekbar_library, "licenserangeseekbar.txt", "RangeSeekBar");
        setupLibraryButton(R.id.slf4j_simple_library, "licenseslf4jsimple.txt", "SLF4J Simple");
        setupLibraryButton(R.id.slf4j_api_library, "licenseslf4japi.txt", "SLF4J API");
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbarTxt = findViewById(R.id.toolbar_title);
        toolbarTxt.setText(R.string.title_activity_library_license);
        toolbarTxt.setTextSize(15.0f);
        backBtn = findViewById(R.id.back_button);
        backBtn.setOnClickListener(v -> finish());
    }

    private void setupLibraryButton(int buttonId, String licenseFileName, String title) {
        Button libraryButton = findViewById(buttonId);
        libraryButton.setOnClickListener(v -> showLicenseDialog(licenseFileName, title));
    }

    private void showLicenseDialog(String licenseFileName, String title) {
        Dialog showLicenseDialog = new Dialog(this);
        // Dialog setup...

        try (InputStream is = getApplicationContext().getAssets().open(licenseFileName);
             InputStreamReader inputStreamReader = new InputStreamReader(is, StandardCharsets.UTF_8);
             BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {

            StringBuilder stringBuilder = new StringBuilder();
            String receiveString;

            while ((receiveString = bufferedReader.readLine()) != null) {
                stringBuilder.append(receiveString.trim()).append(System.lineSeparator());
            }

            TextView textView = showLicenseDialog.findViewById(R.id.tv);
            textView.setText(stringBuilder.toString());
            TextView titleView = showLicenseDialog.findViewById(R.id.licenseTitle);
            titleView.setText(title);
            Button closeButton = showLicenseDialog.findViewById(R.id.closeButton);
            closeButton.setOnClickListener(v1 -> showLicenseDialog.dismiss());
            showLicenseDialog.show();

        } catch (IOException e) {
            e.printStackTrace();
            // Handle the exception (e.g., log, notify user)
        }
    }
}
