package com.rtsoft.growtopia;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

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
    private static final String SERVER_CLIENT_ID =
        "389994132396-4s6ol46f60831v5blfpci7lnmsdnh8br.apps.googleusercontent.com";

    Activity mainActivity;
    private GoogleSignInClient mGoogleSignInClient;

    public GoogleSignInHelper(Activity activity) {
        this.mainActivity = activity;
    }

    public native void OnSignIn(int code, String token);

    public void Init() {}

    public void SignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(SERVER_CLIENT_ID)
            .requestEmail()
            .build();
        mGoogleSignInClient = GoogleSignIn.getClient(mainActivity, gso);
        mGoogleSignInClient.signOut().addOnCompleteListener(task -> {
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            mainActivity.startActivityForResult(signInIntent, RC_SIGN_IN);
        });
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
