package com.rtsoft.growtopia;

import android.app.Activity;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class UsercentricsManager {
    private static final String TAG = "UsercentricsManager";

    private Activity baseContext;

    public UsercentricsManager(Activity activity) {
        this.baseContext = activity;
    }

    // Native callbacks — called from the UI thread (matches real 5.55 implementation)
    public native void InitFinish(boolean success);
    public native void OnConsentFetchedFail(int code, String message);
    public native void OnConsentFetchedSuccess(List<Object> list);

    public void InitWithRuleSet(String str) {
        // Auto-succeed: no Usercentrics SDK, so signal ready immediately on UI thread
        baseContext.runOnUiThread(() -> {
            try { InitFinish(true); } catch (UnsatisfiedLinkError e) {
                Log.w(TAG, "InitFinish unavailable: " + e.getMessage());
            }
        });
    }

    public void InitWithSettings(String str) {
        baseContext.runOnUiThread(() -> {
            try { InitFinish(true); } catch (UnsatisfiedLinkError e) {
                Log.w(TAG, "InitFinish unavailable: " + e.getMessage());
            }
        });
    }

    public void CheckConsentState() {
        // Auto-accept: deliver empty consent list from UI thread (matches real threading model)
        baseContext.runOnUiThread(() -> {
            try { OnConsentFetchedSuccess(new ArrayList<>()); } catch (UnsatisfiedLinkError e) {
                Log.w(TAG, "OnConsentFetchedSuccess unavailable: " + e.getMessage());
            }
        });
    }

    public void FetchUserConsent(List<Object> list) {
        baseContext.runOnUiThread(() -> {
            try { OnConsentFetchedSuccess(list != null ? list : new ArrayList<>()); } catch (UnsatisfiedLinkError e) {
                Log.w(TAG, "OnConsentFetchedSuccess unavailable: " + e.getMessage());
            }
        });
    }

    public void RequestConsentSettings() {
        // No real consent UI — auto-accept immediately
        CheckConsentState();
    }

    public void ShowConsentSettings() {
        // No real consent UI — auto-accept immediately
        CheckConsentState();
    }
}
