package de.tuberlin.mcc.simra.app.activities;

import android.os.Bundle;
import android.webkit.WebView;

import de.tuberlin.mcc.simra.app.R;
import de.tuberlin.mcc.simra.app.util.BaseActivity;


public class WebActivity2 extends BaseActivity {

    // Constants for intent extra keys
    private static final String EXTRA_URL = "URL";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Use the layout defined in XML
        setContentView(R.layout.activity_web);

        // Retrieve the URL from the intent using a constant key
        String URL = getIntent().getStringExtra(EXTRA_URL);

        // Use the WebView defined in the layout
        WebView myWebView = findViewById(R.id.webView);

        // Load the URL into the WebView
        if (URL != null) {
            myWebView.loadUrl(URL);
        }
    }
}
