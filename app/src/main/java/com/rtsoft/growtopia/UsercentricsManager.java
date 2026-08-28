package com.rtsoft.growtopia;

import android.app.Activity;
import android.util.Log;

import com.usercentrics.sdk.UsercentricsConsentHistoryEntry;
import com.usercentrics.sdk.UsercentricsServiceConsent;
import com.usercentrics.sdk.models.settings.UsercentricsConsentType;

import java.util.ArrayList;
import java.util.Collections;
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
    public native void OnConsentFetchedSuccess(List<UsercentricsServiceConsent> list);

    private List<UsercentricsServiceConsent> buildAcceptedConsentList() {
        UsercentricsConsentHistoryEntry historyEntry = new UsercentricsConsentHistoryEntry(
                true, UsercentricsConsentType.EXPLICIT, System.currentTimeMillis());
        List<UsercentricsConsentHistoryEntry> history = new ArrayList<>();
        history.add(historyEntry);

        UsercentricsServiceConsent consent = new UsercentricsServiceConsent(
                "growtopia",   // templateId
                true,          // status = accepted
                history,
                UsercentricsConsentType.EXPLICIT,
                "Ubisoft",     // dataProcessor
                "1.0",         // version
                true,          // isEssential
                "Essential"    // category
        );
        return Collections.singletonList(consent);
    }

    public void InitWithRuleSet(String str) {
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
        baseContext.runOnUiThread(() -> {
            try { OnConsentFetchedSuccess(buildAcceptedConsentList()); } catch (UnsatisfiedLinkError e) {
                Log.w(TAG, "OnConsentFetchedSuccess unavailable: " + e.getMessage());
            }
        });
    }

    public void FetchUserConsent(List<UsercentricsServiceConsent> list) {
        final List<UsercentricsServiceConsent> consents =
                (list != null && !list.isEmpty()) ? list : buildAcceptedConsentList();
        baseContext.runOnUiThread(() -> {
            try { OnConsentFetchedSuccess(consents); } catch (UnsatisfiedLinkError e) {
                Log.w(TAG, "OnConsentFetchedSuccess unavailable: " + e.getMessage());
            }
        });
    }

    public void RequestConsentSettings() {
        CheckConsentState();
    }

    public void ShowConsentSettings() {
        CheckConsentState();
    }
}
