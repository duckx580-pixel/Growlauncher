package com.rtsoft.growtopia;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;

import java.net.URLEncoder;
import java.util.UUID;

/**
 * Google Sign-In without needing com.gentz.launcher registered in Ubisoft's
 * Google Cloud project.
 *
 * Primary path  – Android AccountManager with "audience:server:client_id:"
 *   scope.  This goes through Google Play Services directly using the user's
 *   already-stored Google account and returns a real OpenID Connect ID token
 *   for the specified audience.  No SHA-1 / package registration required.
 *
 * Fallback path – WebView with Chrome UA spoofing + manual header override to
 *   bypass Google's WebView block.  Gets the Growtopia session token from
 *   login.growtopiagame.com and passes it to OnSignIn as a last resort.
 */
public class GoogleSignInHelper {
    private static final String TAG = "GoogleSignInHelper";

    static final String CLIENT_ID =
        "389994132396-4s6ol46f60831v5blfpci7lnmsdnh8br.apps.googleusercontent.com";
    private static final String REDIRECT_URI =
        "https://login.growtopiagame.com/google/callback";

    // Request codes – must stay in sync with Main.onActivityResult
    static final int RC_ACCOUNT_PICKER = 9001;
    static final int RC_AUTH_CONSENT   = 9002;
    static final int RC_GOOGLE_WEB     = 9003;

    Activity mainActivity;

    public GoogleSignInHelper(Activity activity) {
        this.mainActivity = activity;
    }

    public native void OnSignIn(int code, String token);
    public void Init() {}
    public void SignOut() {}

    // ── Entry point called by the native engine ────────────────────────────
    public void SignIn() {
        // Show the standard Android "Choose a Google account" picker.
        // newChooseAccountIntent does NOT require GET_ACCOUNTS or a registered
        // OAuth client – it just asks the user which stored account to use.
        try {
            @SuppressWarnings("deprecation")
            Intent picker = AccountManager.newChooseAccountIntent(
                null, null,
                new String[]{"com.google"},
                null, null, null, null
            );
            mainActivity.startActivityForResult(picker, RC_ACCOUNT_PICKER);
        } catch (Exception e) {
            Log.w(TAG, "AccountManager picker unavailable – falling back to WebView: " + e.getMessage());
            startWebSignIn();
        }
    }

    // ── AccountManager token fetch ─────────────────────────────────────────
    private void fetchIdToken(String accountName) {
        AccountManager am = AccountManager.get(mainActivity);
        Account account = new Account(accountName, "com.google");

        // "audience:server:client_id:<id>" returns an OpenID Connect id_token
        // whose aud claim equals CLIENT_ID – exactly what OnSignIn expects.
        am.getAuthToken(
            account,
            "audience:server:client_id:" + CLIENT_ID,
            Bundle.EMPTY,
            mainActivity,
            future -> {
                try {
                    Bundle result = future.getResult();
                    String token = result.getString(AccountManager.KEY_AUTHTOKEN);
                    if (token != null && !token.isEmpty()) {
                        Log.d(TAG, "AccountManager returned Google ID token");
                        deliverResult(0, token);
                    } else {
                        // Google Play Services needs explicit user consent first
                        Intent authIntent = result.getParcelable(AccountManager.KEY_INTENT);
                        if (authIntent != null) {
                            mainActivity.runOnUiThread(() ->
                                mainActivity.startActivityForResult(authIntent, RC_AUTH_CONSENT));
                        } else {
                            Log.w(TAG, "No token and no consent intent – falling back");
                            startWebSignIn();
                        }
                    }
                } catch (Exception e) {
                    Log.w(TAG, "getAuthToken failed (" + e.getMessage() + ") – falling back to WebView");
                    startWebSignIn();
                }
            },
            null   // handler – null = callback on main thread
        );
    }

    // ── WebView fallback ───────────────────────────────────────────────────
    private void startWebSignIn() {
        try {
            String state = UUID.randomUUID().toString().replace("-", "");
            String url = "https://accounts.google.com/o/oauth2/v2/auth"
                + "?client_id=" + CLIENT_ID
                + "&redirect_uri=" + URLEncoder.encode(REDIRECT_URI, "utf-8")
                + "&response_type=code"
                + "&scope=" + URLEncoder.encode("openid profile email", "utf-8")
                + "&state=" + state;
            Intent intent = new Intent(mainActivity, GoogleWebSignInActivity.class);
            intent.putExtra("url", url);
            mainActivity.startActivityForResult(intent, RC_GOOGLE_WEB);
        } catch (Exception e) {
            Log.e(TAG, "WebView sign-in start failed: " + e.getMessage());
            deliverResult(-1, "");
        }
    }

    // ── onActivityResult dispatcher (called from Main.onActivityResult) ────
    public void handleSignInResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == RC_ACCOUNT_PICKER) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                String name = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                if (name != null && !name.isEmpty()) {
                    Log.d(TAG, "Account picked: " + name);
                    fetchIdToken(name);
                    return;
                }
            }
            // Picker cancelled or returned no account → try WebView
            Log.d(TAG, "Account picker cancelled – trying WebView");
            startWebSignIn();
            return;
        }

        if (requestCode == RC_AUTH_CONSENT) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                // User just granted consent; KEY_ACCOUNT_NAME may be present
                String name = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                if (name != null && !name.isEmpty()) {
                    fetchIdToken(name);
                    return;
                }
            }
            Log.d(TAG, "Auth consent denied or incomplete – trying WebView");
            startWebSignIn();
            return;
        }

        if (requestCode == RC_GOOGLE_WEB) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                String token = data.getStringExtra(GoogleWebSignInActivity.EXTRA_TOKEN);
                Log.d(TAG, "WebView sign-in token length=" + (token != null ? token.length() : 0));
                // Growtopia session token from login.growtopiagame.com
                deliverResult(0, token != null ? token : "");
            } else {
                Log.d(TAG, "WebView sign-in cancelled");
                deliverResult(-1, "");
            }
        }
    }

    // ── Token delivery on the GL thread ───────────────────────────────────
    private void deliverResult(int code, String token) {
        GLSurfaceView glView = SharedActivity.mGLView;
        if (glView != null) {
            final int c = code;
            final String t = token;
            glView.queueEvent(() -> {
                try {
                    OnSignIn(c, t);
                } catch (UnsatisfiedLinkError e) {
                    Log.w(TAG, "OnSignIn native unavailable: " + e.getMessage());
                }
            });
        }
    }
}
