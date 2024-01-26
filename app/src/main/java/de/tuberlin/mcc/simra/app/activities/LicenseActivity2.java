/*package de.tuberlin.mcc.simra.app.activities;

import android.app.Dialog;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import de.tuberlin.mcc.simra.app.R;

public class LicenseActivity2 extends AppCompatActivity {

    private static final String LICENSE_FILE_PREFIX = "license";
    private static final String[] LIBRARY_BUTTONS = {
            "android_support_library", "apache_commons_library", "fbase_library",
            "gson_library", "javax_activation_library", "jersey_library",
            "jetty_library", "logback_library", "mpandroidchart_library",
            "okhttp_library", "osmbonuspack_library", "osmdroid_library",
            "rangeseekbar_library", "slf4j_simple_library", "slf4j_api_library"
    };

    private ImageButton backBtn;
    private TextView toolbarTxt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_license);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setTitle("");
        toolbar.setSubtitle("");
        toolbarTxt = findViewById(R.id.toolbar_title);
        toolbarTxt.setText(R.string.title_activity_library_license);
        toolbarTxt.setTextSize(15.0f);

        backBtn = findViewById(R.id.back_button);
        // backBtn.setOnClickListener(this::finish);
        backBtn.setOnClickListener(v -> finish());

        initializeLibraryButtons();
    }

    private void initializeLibraryButtons() {
        for (String buttonId : LIBRARY_BUTTONS) {
            Button libraryButton = findViewById(getResources().getIdentifier(buttonId, "id", getPackageName()));
            initializeLibraryButton(libraryButton, LICENSE_FILE_PREFIX + buttonId + ".txt", getDialogTitleResId(buttonId));
        }
    }

    private int getDialogTitleResId(String buttonId) {
        return getResources().getIdentifier("title_" + buttonId, "string", getPackageName());
    }

    private void initializeLibraryButton(Button button, String fileName, int titleResId) {
        createDialogWhenButtonIsPressed(button, fileName, titleResId);
    }

    private void createDialogWhenButtonIsPressed(Button button, String licenseFileName, int titleResId) {
        Dialog showLicenseDialog = new Dialog(this);
        showLicenseDialog.setContentView(R.layout.show_license_dialog);

        Window window = showLicenseDialog.getWindow();
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        window.setGravity(Gravity.CENTER);

        button.setOnClickListener(v -> {
            String message = "";
            try {
                AssetManager am = getApplicationContext().getAssets();
                InputStream is = am.open(licenseFileName);
                InputStreamReader inputStreamReader = new InputStreamReader(is, StandardCharsets.UTF_8);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString;
                StringBuilder stringBuilder = new StringBuilder();

                while ((receiveString = bufferedReader.readLine()) != null) {
                    stringBuilder.append(receiveString.trim()).append(System.lineSeparator());
                }
                is.close();
                message = stringBuilder.toString();
            } catch (IOException e) {
                Log.e("LicenseActivity", "Error reading license file: " + e.getMessage());
            }
            TextView textView = showLicenseDialog.findViewById(R.id.tv);
            textView.setText(message);
            TextView titleView = showLicenseDialog.findViewById(R.id.licenseTitle);
            titleView.setText(titleResId);
            Button closeButton = showLicenseDialog.findViewById(R.id.closeButton);
            closeButton.setOnClickListener(v1 -> showLicenseDialog.dismiss());
            showLicenseDialog.show();
        });
    }
}
