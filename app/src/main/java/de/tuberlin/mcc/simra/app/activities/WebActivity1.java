/*package de.tuberlin.mcc.simra.app.activities;

import android.os.Bundle;
import android.webkit.WebView;

import de.tuberlin.mcc.simra.app.R;
import de.tuberlin.mcc.simra.app.util.BaseActivity;

public class WebActivity1 extends BaseActivity {

    private static final String EXTRA_URL = "URL";
    private static final String DEFAULT_URL = "default_url";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web);
        WebView webView = initializeWebView();
        String url = getIntent().getStringExtra(EXTRA_URL);
        if (url == null) {
            //url = getString(R.string.DEFAULT_URL);
            url = DEFAULT_URL;
        }
        webView.loadUrl(url);
    }

    private WebView initializeWebView() {
        WebView webView = new WebView(getBaseContext());
        setContentView(webView);
        return webView;
    }
}
