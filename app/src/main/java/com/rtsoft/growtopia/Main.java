package com.rtsoft.growtopia;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.inputmethod.InputMethodManager;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Game entry activity – faithful to the decompiled official structure.
 *
 * Loads libgrowtopia.so, sets up the engine's expected fields, then delegates
 * to {@link SharedActivity#onCreate} which builds the GLSurfaceView host.
 */
public class Main extends SharedActivity {
    public static boolean OriginalKeyboard = false;
    public static boolean block_pause;
    public static HelpShiftManager helpshiftManager;
    public static Main mainApp;
    public static GLSurfaceView mygl;

    public NativeAppInterface nativeAppInterface = new NativeAppInterface();
    public AppsFlyerManager appsflyerManager = new AppsFlyerManager(this);
    public IronSourceManager ironSourceManager = new IronSourceManager(this);
    public WebViewManager webViewManager = new WebViewManager(this);
    public AppReviewManager appReviewManager = new AppReviewManager(this);
    public FirebaseCrashlyticsManager firebaseCrashlyticsManager = new FirebaseCrashlyticsManager();
    public FirebaseCloudMessageManager firebaseCloudMessageManager = new FirebaseCloudMessageManager();
    public GoogleSignInHelper googleSignInHelper = new GoogleSignInHelper(this);
    public MAFManager mafManager = new MAFManager(this);
    public UsercentricsManager usercentricsManager = null;

    private HeightProvider heightProvider;

    // Additional native method declared on Main (engine may look it up here).
    public static native boolean nativeOnTouch(float x, float y, int action);

    // ---- Static getters the engine calls via JNI ----
    public static AppReviewManager GetAppReviewManager() { return mainApp.appReviewManager; }
    public static AppsFlyerManager GetAppsflyerManager() { return mainApp.appsflyerManager; }
    public static FirebaseCloudMessageManager GetFirebaseCloudMessageManager() {
        return mainApp.firebaseCloudMessageManager;
    }
    public static FirebaseCrashlyticsManager GetFirebaseCrashlyticsManager() {
        return new FirebaseCrashlyticsManager();
    }
    public static GoogleSignInHelper GetGoogleSignInHelper() { return mainApp.googleSignInHelper; }
    public static Object GetHelpShiftManager() { return helpshiftManager; }
    public static Object GetIronSourceManager() { return mainApp.ironSourceManager; }
    public static MAFManager GetMAFManager() { return mainApp.mafManager; }
    public static UsercentricsManager GetUsercentricsManager() { return mainApp.usercentricsManager; }
    public static WebViewManager GetWebViewManager() { return mainApp.webViewManager; }

    @Override
    public String GetAppsflyerUID() { return ""; }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        mainApp = this;
        helpshiftManager = new HelpShiftManager(this);

        // Engine reads these fields before init.
        SharedActivity.dllname = "growtopia";
        SharedActivity.securityEnabled = false;
        SharedActivity.IAPEnabled = true;
        SharedActivity.HookedEnabled = false;
        SharedActivity.PackageName = BuildConfig.APPLICATION_ID;

        // Load the engine BEFORE super.onCreate (which calls nativeInitActivity).
        System.loadLibrary("growtopia");

        super.onCreate(savedInstanceState);

        // Fix orientation if needed (matches real Main).
        Configuration cfg = getResources().getConfiguration();
        if (cfg.screenHeightDp > cfg.screenWidthDp) {
            cfg.screenHeightDp = cfg.screenWidthDp;
            cfg.screenWidthDp = cfg.screenHeightDp;
        }

        heightProvider = new HeightProvider(this).setHeightListener(
                new HeightProvider.HeightListener() {
                    @Override
                    public void onHeightChanged(int height) {
                        OnKeyboardHeightChanged(height);
                    }
                });

        usercentricsManager = new UsercentricsManager(this);
        getWindow().addFlags(128); // FLAG_KEEP_SCREEN_ON
        applyImmersiveFullscreen();
    }

    @Override
    public void onConfigurationChanged(Configuration config) {
        if (config.screenHeightDp > config.screenWidthDp) {
            int tmp = config.screenHeightDp;
            config.screenHeightDp = config.screenWidthDp;
            config.screenWidthDp = tmp;
        }
        super.onConfigurationChanged(config);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntent(intent);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (heightProvider != null) heightProvider.OnPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (heightProvider != null) heightProvider.OnResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void OnKeyboardHeightChanged(int height) {
        if (OriginalKeyboard) {
            if (webViewManager.IsVisible()) {
                webViewManager.MoveView(height);
                return;
            }
            SharedActivity.m_KeyBoardHeight = height;
            boolean opening = height > 0;
            if (opening && m_editText != null && !m_editText.isFocused()) {
                UpdateEditBoxInView(true, false);
            } else if (!opening && m_editText != null && m_editText.isFocused()) {
                OriginalKeyboard = false;
                if (m_editText != null) {
                    nativeOnInputText(m_editText.getText().toString());
                }
                nativeCancelBtnPressed();
                UpdateEditBoxInView(false, false);
            }
        }
    }

    public void hideKeyboard(Activity activity) {
        View v = activity.findViewById(android.R.id.content);
        if (v != null) {
            ((InputMethodManager) activity.getSystemService("input_method"))
                    .hideSoftInputFromWindow(v.getWindowToken(), 0);
        }
    }

    public static boolean HandleDeeplink(Intent intent) {
        final Uri data = intent.getData();
        if (data == null) return false;
        if (mGLView != null) {
            mGLView.post(new Runnable() {
                @Override
                public void run() {
                    NativeAppInterface.OnDeepLinkProcess(data.getSchemeSpecificPart());
                }
            });
        }
        return true;
    }

    private void handleIntent(Intent intent) {
        if (intent == null) return;
        String action = intent.getAction();
        Uri data = intent.getData();
        if (!"android.intent.action.VIEW".equals(action) || data == null) return;
        Log.d(PackageName, "Deep link: " + data);
    }

    /**
     * PowerKuy mod renderer – the official libgrowtopia.so does NOT export
     * these symbols, so the catch(UnsatisfiedLinkError) keeps it safe.
     */
    public static class PowerKuyRootRenderer implements GLSurfaceView.Renderer {
        public static native void nativeDrawFrame();
        public static native int nativeGetMessagePowerKuy();
        public static native void nativeSurfaceChanged(int w, int h);

        @Override public void onDrawFrame(GL10 gl) {
            try { nativeDrawFrame(); } catch (UnsatisfiedLinkError ignored) {}
        }
        @Override public void onSurfaceChanged(GL10 gl, int w, int h) {
            try { nativeSurfaceChanged(w, h); } catch (UnsatisfiedLinkError ignored) {}
        }
        @Override public void onSurfaceCreated(GL10 gl, EGLConfig config) {}
    }
}
