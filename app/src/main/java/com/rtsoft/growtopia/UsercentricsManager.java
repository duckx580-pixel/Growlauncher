package com.rtsoft.growtopia;

import android.app.Activity;
import java.util.ArrayList;
import java.util.List;

public class UsercentricsManager {
    private Activity baseContext;

    public UsercentricsManager(Activity activity) {
        this.baseContext = activity;
    }

    public void CheckConsentState() {
        OnConsentFetchedSuccess(new ArrayList());
    }

    public void FetchUserConsent(List<Object> list) {
        OnConsentFetchedSuccess(list);
    }

    public native void OnConsentFetchedFail(int i10, String str);

    public native void OnConsentFetchedSuccess(List<Object> list);

    public void RequestConsentSettings() {
    }

    public void ShowConsentSettings() {
    }

    public void InitWithRuleSet(String str) {
    }

    public void InitWithSettings(String str) {
    }

    // Stubs needed by official libgrowtopia.so consent processing flow.
    // The native OnConsentFetchedSuccess caches these method IDs; without
    // them GetMethodID returns NULL → SIGSEGV.
    public void SetAge(int age) {}
    public void SetCMPAge(int age) {}
    public void SetABTestingVariant(String variant) {}
    public boolean IsReady() { return true; }
    public String GetConsentStatus() { return ""; }
}
