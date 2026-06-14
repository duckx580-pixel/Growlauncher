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

    public native void OnConsentFetchedFail(int code, String message);
    public native void OnConsentFetchedSuccess(List<Object> list);

    public void RequestConsentSettings() {}
    public void ShowConsentSettings() {}
    public void InitWithRuleSet(String ruleSet) {}
    public void InitWithSettings(String settings) {}
}
