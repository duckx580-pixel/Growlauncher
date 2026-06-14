package com.rtsoft.growtopia;

import android.app.Activity;

public class WebViewManager {
    private Activity activity;

    public WebViewManager(Activity activity) {
        this.activity = activity;
    }

    public boolean IsVisible() { return false; }
    public void MoveView(int height) {}
    public void Show(String url) {}
    public void Hide() {}
    public void Destroy() {}
}
