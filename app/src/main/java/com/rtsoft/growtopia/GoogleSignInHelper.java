package com.rtsoft.growtopia;

import android.app.Activity;
import android.content.Intent;
import android.opengl.GLSurfaceView;
import android.util.Log;

import java.net.URLEncoder;
import java.util.UUID;

public class GoogleSignInHelper {
    private static final String TAG = "GoogleSignInHelper";
    private static final String CLIENT_ID =
        "389994132396-4s6ol46f60831v5blfpci7lnmsdnh8br.apps.googleusercontent.com";
    private static final String REDIRECT_URI =
        "https://login.growtopiagame.com/google/callback";
    static final int RC_GOOGLE_WEB = 9001;

    Activity mainActivity;

    public GoogleSignInHelper(Activity activity) {
        this.mainActivity = activity;
    }

    public native void OnSignIn(int code, String token);

    public void Init() {}

    public void SignIn() {
        try {
            String state = UUID.randomUUID().toString().replace("-", "");
            String url = "https://accounts.google.com/o/oauth2/v2/auth"
                + "?client_id=" + CLIENT_ID
                + "&redirect_uri=" + URLEncoder.encode(REDIRECT_URI, "utf-8")
                + "&response_type=code"
                + "&scope=" + URLEncoder.encode("openid profile email", "utf-8")
                + "&state=" + state;
            Log.d(TAG, "Starting web Google OAuth");
            Intent intent = new Intent(mainActivity, GoogleWebSignInActivity.class);
            intent.putExtra("url", url);
            mainActivity.startActivityForResult(intent, RC_GOOGLE_WEB);
        } catch (Exception e) {
            Log.e(TAG, "Failed to start web sign-in: " + e.getMessage());
            deliverResult(-1, "");
        }
    }

    public void SignOut() {}

    public void handleSignInResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != RC_GOOGLE_WEB) return;
        if (resultCode == Activity.RESULT_OK && data != null) {
            String token = data.getStringExtra(GoogleWebSignInActivity.EXTRA_TOKEN);
            Log.d(TAG, "Web sign-in success");
            deliverResult(0, token != null ? token : "");
        } else {
            Log.d(TAG, "Web sign-in canceled");
            deliverResult(-1, "");
        }
    }

    private void deliverResult(int code, String token) {
        GLSurfaceView glView = SharedActivity.mGLView;
        if (glView != null) {
            final int c = code;
            final String t = token;
            glView.queueEvent(() -> {
                try { OnSignIn(c, t); } catch (UnsatisfiedLinkError e) {
                    Log.w(TAG, "OnSignIn unavailable: " + e.getMessage());
                }
            });
        }
    }
}
