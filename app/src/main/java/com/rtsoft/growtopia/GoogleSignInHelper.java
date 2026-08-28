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
        // Sign out first so the account chooser always appears
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
        try {
            GoogleSignInAccount account = task.getResult(ApiException.class);
            String idToken = account.getIdToken();
            Log.d(TAG, "signIn success, token=" + idToken);
            OnSignIn(0, idToken != null ? idToken : "");
        } catch (ApiException e) {
            int code = e.getStatusCode();
            if (code == 12501) {
                Log.e(TAG, "signIn canceled by user");
                OnSignIn(-1, "");
            } else {
                Log.e(TAG, "signIn failed: " + code);
                OnSignIn(code, "");
            }
        }
    }
}
