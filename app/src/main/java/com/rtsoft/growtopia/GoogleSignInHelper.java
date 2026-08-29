package com.rtsoft.growtopia;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import androidx.browser.customtabs.CustomTabsIntent;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import android.opengl.GLSurfaceView;

public class GoogleSignInHelper {
    private static final String TAG = "GoogleSignInHelper";
    private static final int RC_SIGN_IN = 1;

    // CSTS web OAuth URL — opens Ubisoft's login page which handles Google sign-in
    // server-side and redirects back to grow://growtopia?info=...&token=...
    private static final String CSTS_LOGIN_URL =
        "https://csts-mob.ubi.com/index.php?platform=android&language=en&country=US&iap=1";

    Activity mainActivity;
    private GoogleSignInClient mGoogleSignInClient;

    public GoogleSignInHelper(Activity activity) {
        this.mainActivity = activity;
    }

    public native void OnSignIn(int code, String token);

    public void Init() {}

    public void SignIn() {
        // Open Ubisoft's CSTS web login page via Chrome Custom Tab.
        // The page handles Google OAuth server-side, then redirects to
        // grow://growtopia?info=...&token=... which Main.handleIntent() catches
        // and passes to NativeAppInterface.OnDeepLinkProcess("info=...&token=...").
        // This bypasses native Google Sign-In Error 10 (DEVELOPER_ERROR) which
        // occurs because our package is not registered in Ubisoft's Google Cloud project.
        try {
            CustomTabsIntent customTab = new CustomTabsIntent.Builder()
                    .setShareState(CustomTabsIntent.SHARE_STATE_OFF)
                    .build();
            customTab.intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            customTab.launchUrl(mainActivity, Uri.parse(CSTS_LOGIN_URL));
        } catch (Exception e) {
            Log.e(TAG, "CustomTab launch failed, falling back to ACTION_VIEW: " + e.getMessage());
            try {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(CSTS_LOGIN_URL));
                mainActivity.startActivity(browserIntent);
            } catch (Exception e2) {
                Log.e(TAG, "Browser fallback also failed: " + e2.getMessage());
            }
        }
    }

    public void SignOut() {
        if (mGoogleSignInClient != null) {
            mGoogleSignInClient.signOut();
        }
    }

    public void handleSignInResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != RC_SIGN_IN) return;
        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
        final int[] signInCode = {0};
        final String[] signInToken = {""};
        try {
            GoogleSignInAccount account = task.getResult(ApiException.class);
            String idToken = account.getIdToken();
            Log.d(TAG, "signIn success, token=" + idToken);
            signInCode[0] = 0;
            signInToken[0] = idToken != null ? idToken : "";
        } catch (ApiException e) {
            int code = e.getStatusCode();
            if (code == 12501) {
                Log.e(TAG, "signIn canceled by user");
                signInCode[0] = -1;
            } else {
                Log.e(TAG, "signIn failed: " + code);
                signInCode[0] = code;
            }
        }
        // Deliver the callback on the GL thread so the engine receives it from the correct thread.
        GLSurfaceView glView = SharedActivity.mGLView;
        if (glView != null) {
            final int code = signInCode[0];
            final String token = signInToken[0];
            glView.queueEvent(() -> {
                try { OnSignIn(code, token); } catch (UnsatisfiedLinkError e) {
                    Log.w(TAG, "OnSignIn unavailable: " + e.getMessage());
                }
            });
        } else {
            try { OnSignIn(signInCode[0], signInToken[0]); } catch (UnsatisfiedLinkError e) {
                Log.w(TAG, "OnSignIn unavailable: " + e.getMessage());
            }
        }
    }
}
