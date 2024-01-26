/*package de.tuberlin.mcc.simra.app.activities;

import static de.tuberlin.mcc.simra.app.util.LogHelper.showDataDirectory;
import static de.tuberlin.mcc.simra.app.util.LogHelper.showKeyPrefs;
import static de.tuberlin.mcc.simra.app.util.LogHelper.showMetadata;
import static de.tuberlin.mcc.simra.app.util.LogHelper.showStatistics;
import static de.tuberlin.mcc.simra.app.util.SharedPref.lookUpBooleanSharedPrefs;
import static de.tuberlin.mcc.simra.app.util.SharedPref.writeBooleanToSharedPrefs;
import static de.tuberlin.mcc.simra.app.util.Utils.deleteErrorLogsForVersion;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.google.android.material.button.MaterialButton;

import java.io.File;
import java.util.Arrays;

import de.tuberlin.mcc.simra.app.R;
import de.tuberlin.mcc.simra.app.services.UploadService;
import de.tuberlin.mcc.simra.app.util.BaseActivity;
import de.tuberlin.mcc.simra.app.util.PermissionHelper;
import de.tuberlin.mcc.simra.app.util.SharedPref;
import de.tuberlin.mcc.simra.app.util.UpdateHelper;

// Import statements (unchanged)

public class StartActivity2 extends BaseActivity {

    private static final String TAG = "StartActivity2_LOG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        initializeLogs();
        UpdateHelper.migrate(this);
        checkPermissionsAndContinue();

        Button next = findViewById(R.id.nextBtn);
        next.setOnClickListener(v -> navigateIfAllPermissionsGranted());
    }


    private boolean privacyPolicyAccepted() {
        return lookUpBooleanSharedPrefs("Privacy-Policy-Accepted", false, "simraPrefs", this);
    }

    private boolean privacyPolicyUpdateAccepted() {
        return lookUpBooleanSharedPrefs("Privacy-Policy-Update-Accepted", false, "simraPrefs", this);
    }

    private void checkPermissionsAndContinue() {
        if (privacyPolicyAccepted() && privacyPolicyUpdateAccepted() && PermissionHelper.hasBasePermissions(this)) {
            startMainActivity();
        }
    }

    private void initializeLogs() {
        Log.d(TAG, "getFilesDir(): " + Arrays.toString(new File(getFilesDir(), "../shared_prefs").listFiles()));
        Log.d(TAG, "onCreate() started");
        showKeyPrefs(this);
        showDataDirectory(this);
        showMetadata(this);
        showStatistics(this);
        deleteErrorLogsForVersion(this, 26);
    }

    private void navigateIfAllPermissionsGranted() {
        if (!privacyPolicyAccepted()) {
            handlePrivacyDialog();
        } else if (!privacyPolicyUpdateAccepted()) {
            handlePrivacyUpdateDialog();
        } else if (!PermissionHelper.hasBasePermissions(this)) {
            PermissionHelper.requestFirstBasePermissionsNotGranted(this);
        } else {
            startMainActivity();
        }
    }

    private boolean isSmallScreen() {
        int screenSize = getResources().getConfiguration().screenLayout &
                Configuration.SCREENLAYOUT_SIZE_MASK;
        return screenSize == Configuration.SCREENLAYOUT_SIZE_SMALL;
    }

    private void handlePrivacyDialog() {
        if (isSmallScreen()) {
            createPrivacyDialogSmallScreen();
        } else {
            createPrivacyDialogNormalScreen();
        }
    }

    private void createPrivacyDialogNormalScreen() {
        View checkBoxView = View.inflate(this, R.layout.checkbox, null);
        CheckBox checkBox = checkBoxView.findViewById(R.id.checkbox);

        checkBox.setText(getString(R.string.iAccept));
        AlertDialog.Builder builder = new AlertDialog.Builder(StartActivity2.this);

        //
        builder.setView(checkBoxView);
        DialogInterface.OnClickListener positiveButtonListener = (dialogInterface, i) -> {
            writeBooleanToSharedPrefs("Privacy-Policy-Accepted", checkBox.isChecked(), "simraPrefs", StartActivity2.this);
            writeBooleanToSharedPrefs("Privacy-Policy-Update-Accepted", checkBox.isChecked(), "simraPrefs", StartActivity2.this);
            PermissionHelper.requestFirstBasePermissionsNotGranted(StartActivity2.this);
        };
        builder.setPositiveButton(R.string.next, positiveButtonListener);
        builder.setNegativeButton(R.string.close_simra, (dialog, id) -> {
            finish();
            Toast.makeText(StartActivity2.this, getString(R.string.simra_closed), Toast.LENGTH_LONG).show();
        });
        final AlertDialog dialog = builder.create();
        dialog.show();
        ((TextView) dialog.findViewById(android.R.id.message)).setMovementMethod(LinkMovementMethod.getInstance());

        // Initially disable the button
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
        checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(isChecked));
    }

    private void createPrivacyDialogSmallScreen() {
        AlertDialog.Builder builder = new AlertDialog.Builder(StartActivity2.this);
        AlertDialog dialog = null;
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(35, 35, 35, 35);

        TextView title = new TextView(this);
        title.setText(getString(R.string.privacyAgreementTitle));
        title.getTextSize();
        // increase text size of title
        title.setTextSize(TypedValue.COMPLEX_UNIT_PX, (title.getTextSize() * 1.2f));
        title.setTypeface(null, Typeface.BOLD);
        title.setPadding(0, 0, 0, 35);
        layout.addView(title);

        TextView message = new TextView(this);
        message.setText(getResources().getText(R.string.privacyAgreementMessage));
        message.setMovementMethod(LinkMovementMethod.getInstance());
        layout.addView(message);

        View checkBoxView = View.inflate(this, R.layout.checkbox, null);
        CheckBox checkBox = checkBoxView.findViewById(R.id.checkbox);

        checkBox.setText(getString(R.string.iAccept));
        layout.addView(checkBoxView);

        RelativeLayout buttonsLayout = new RelativeLayout(this);

        MaterialButton negativeButton = new MaterialButton(this);
        RelativeLayout r2 = new RelativeLayout(this);
        r2.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT));
        RelativeLayout.LayoutParams negativeParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        negativeParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
        negativeButton.setLayoutParams(negativeParams);
        negativeButton.setOnClickListener(v -> {
            finish();
            Toast.makeText(StartActivity2.this, getString(R.string.simra_closed), Toast.LENGTH_LONG).show();
        });
        negativeButton.setText(R.string.close_simra);
        r2.setPadding(35, 0, 0, 0);
        r2.addView(negativeButton);
        buttonsLayout.addView(r2);

        MaterialButton positiveButton = new MaterialButton(this);
        RelativeLayout r1 = new RelativeLayout(this);
        r1.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT));
        RelativeLayout.LayoutParams positiveParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        positiveParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
        positiveButton.setLayoutParams(positiveParams);
        positiveButton.setText(R.string.next);
        r1.setPadding(0, 0, 35, 0);
        r1.addView(positiveButton);
        buttonsLayout.addView(r1);

        positiveButton.setEnabled(false);
        checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> positiveButton.setEnabled(isChecked));
        layout.addView(buttonsLayout);
        final ScrollView scrollView = new ScrollView(this);
        scrollView.addView(layout);
        builder.setView(scrollView);
        dialog = builder.create();
        dialog.show();
        AlertDialog finalDialog = dialog;
        positiveButton.setOnClickListener(v -> {
            writeBooleanToSharedPrefs("Privacy-Policy-Update-Accepted", checkBox.isChecked(), "simraPrefs", StartActivity2.this);
            writeBooleanToSharedPrefs("Privacy-Policy-Accepted", checkBox.isChecked(), "simraPrefs", StartActivity2.this);
            PermissionHelper.requestFirstBasePermissionsNotGranted(StartActivity2.this);
            finalDialog.dismiss();
        });
    }

    private void handlePrivacyUpdateDialog() {
        // Existing functionality for privacy update dialog...
    }

    private void startMainActivity() {
        Intent intent = new Intent(StartActivity2.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    // Other methods...
}

