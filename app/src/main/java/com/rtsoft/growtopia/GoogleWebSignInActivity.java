package com.rtsoft.growtopia;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;

import org.json.JSONObject;

public class GoogleWebSignInActivity extends Activity {
    static final String EXTRA_TOKEN = "token";
    private static final String TAG = "GoogleWebSignIn";

    private WebView webView;
    private boolean finished = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String url = getIntent().getStringExtra("url");
        if (url == null) {
            setResult(RESULT_CANCELED);
            finish();
            return;
        }

        webView = new WebView(this);
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setUserAgentString(
            "Mozilla/5.0 (Linux; Android 10; Mobile) AppleWebKit/537.36 "
            + "(KHTML, like Gecko) Chrome/126.0.0.0 Mobile Safari/537.36");

        webView.addJavascriptInterface(new TokenBridge(), "TokenBridge");

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String pageUrl) {
                if (pageUrl != null && pageUrl.contains("login.growtopiagame.com")) {
                    // Body should be JSON {"token":"..."}
                    view.loadUrl("javascript:TokenBridge.onBodyText(document.body.innerText)");
                }
            }
        });

        FrameLayout frame = new FrameLayout(this);
        frame.addView(webView);
        setContentView(frame);
        webView.loadUrl(url);
    }

    private class TokenBridge {
        @JavascriptInterface
        public void onBodyText(String bodyText) {
            if (finished) return;
            try {
                JSONObject json = new JSONObject(bodyText.trim());
                String token = json.optString("token", "");
                if (!token.isEmpty()) {
                    Log.d(TAG, "Got Growtopia token via web OAuth, length=" + token.length());
                    Intent result = new Intent();
                    result.putExtra(EXTRA_TOKEN, token);
                    finished = true;
                    setResult(RESULT_OK, result);
                } else {
                    Log.e(TAG, "No token field in JSON: " + bodyText);
                    deliverCancel();
                }
            } catch (Exception e) {
                Log.e(TAG, "Parse error: " + e.getMessage() + " body=" + bodyText);
                deliverCancel();
            }
            runOnUiThread(GoogleWebSignInActivity.this::finish);
        }
    }

    void deliverCancel() {
        if (finished) return;
        finished = true;
        setResult(RESULT_CANCELED);
    }

    @Override
    public void onBackPressed() {
        if (webView != null && webView.canGoBack()) {
            webView.goBack();
        } else {
            deliverCancel();
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        if (webView != null) {
            webView.destroy();
            webView = null;
        }
        super.onDestroy();
    }
}
