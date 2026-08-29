package com.rtsoft.growtopia;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.util.Log;

import androidx.browser.customtabs.CustomTabsIntent;

import java.net.URLEncoder;
import java.util.UUID;

public class GoogleSignInHelper {
    private static final String TAG = "GoogleSignInHelper";
    private static final String CLIENT_ID =
        "389994132396-4s6ol46f60831v5blfpci7lnmsdnh8br.apps.googleusercontent.com";
    private static final String REDIRECT_URI =
        "https://login.growtopiagame.com/google/callback";

    // Set true while Chrome Custom Tab is open; Main.onResume clears it if tab
    // closes without a grow:// callback (user canceled).
    public static volatile boolean webSignInPending = false;

    Activity mainActivity;

    public GoogleSignInHelper(Activity activity) {
        this.mainActivity = activity;
    }

    public native void OnSignIn(int code, String token);

    public void Init() {}

    public void SignIn() {
        webSignInPending = true;
        try {
            String state = UUID.randomUUID().toString().replace("-", "");
            String url = "https://accounts.google.com/o/oauth2/v2/auth"
                + "?client_id=" + CLIENT_ID
                + "&redirect_uri=" + URLEncoder.encode(REDIRECT_URI, "utf-8")
                + "&response_type=code"
                + "&scope=" + URLEncoder.encode("openid profile email", "utf-8")
                + "&state=" + state;
            Log.d(TAG, "Opening web OAuth: " + url);
            CustomTabsIntent customTab = new CustomTabsIntent.Builder()
                .setShareState(CustomTabsIntent.SHARE_STATE_OFF)
                .build();
            customTab.intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            customTab.launchUrl(mainActivity, Uri.parse(url));
        } catch (Exception e) {
            Log.e(TAG, "Web OAuth launch failed: " + e.getMessage());
            webSignInPending = false;
            cancelSignIn();
        }
    }

    public void SignOut() {}

    // Called by Main.handleIntent when grow:// callback arrives successfully.
    public void onWebSignInCallbackReceived() {
        webSignInPending = false;
    }

    // Called by Main.onResume when tab closed without callback (user canceled).
    public void onWebSignInCanceled() {
        webSignInPending = false;
        cancelSignIn();
    }

    private void cancelSignIn() {
        GLSurfaceView glView = SharedActivity.mGLView;
        if (glView != null) {
            glView.queueEvent(() -> {
                try { OnSignIn(-1, ""); } catch (UnsatisfiedLinkError e) {
                    Log.w(TAG, "OnSignIn unavailable: " + e.getMessage());
                }
            });
        }
    }

    // No-op: native Google Sign-In is replaced by web OAuth; onActivityResult no longer used.
    public void handleSignInResult(int requestCode, int resultCode, Intent data) {}
}
