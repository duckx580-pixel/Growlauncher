package com.rtsoft.growtopia;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class UsercentricsManager {
    private static final String TAG = "UsercentricsManager";

    private Activity baseContext;

    public UsercentricsManager(Activity activity) {
        this.baseContext = activity;
    }

    public void CheckConsentState() {
        deliverConsent(new ArrayList<>());
    }

    public void FetchUserConsent(List<Object> list) {
        deliverConsent(list == null ? new ArrayList<>() : list);
    }

    /**
     * The engine reads the consent result from its own render thread while it is still
     * booting. Answering inline from the UI thread re-enters native code before the
     * engine finished initializing, which crashes the age/consent gate, so the callback
     * is queued on the GL thread instead.
     */
    private void deliverConsent(final List<Object> consents) {
        final Runnable callback = () -> {
            try {
                OnConsentFetchedSuccess(consents);
            } catch (UnsatisfiedLinkError e) {
                Log.w(TAG, "consent callback unavailable: " + e.getMessage());
            } catch (Throwable t) {
                Log.e(TAG, "consent callback failed: " + t);
            }
        };
        if (!NativeLibraries.isGameLoaded()) {
            Log.w(TAG, "engine not loaded, dropping consent callback");
            return;
        }
        GLSurfaceView glView = SharedActivity.mGLView;
        if (glView != null) {
            glView.queueEvent(callback);
        } else {
            new Handler(Looper.getMainLooper()).post(callback);
        }
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
}
