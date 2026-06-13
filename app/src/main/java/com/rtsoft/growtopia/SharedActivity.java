package com.rtsoft.growtopia;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RelativeLayout;

/**
 * RTSoft Proton SDK Android host activity.
 *
 * This is the Java side of the native game engine packaged in libgrowtopia.so.
 * The engine is NOT a NativeActivity; it is driven from Java through an OpenGL
 * {@link AppGLSurfaceView} whose {@link AppRenderer} calls the engine's JNI
 * entry points every frame (nativeUpdate/nativeRender) and pumps OS messages
 * back out of the engine (nativeOSMessageGet).
 *
 * The native library locates these methods by their fully-qualified JNI names
 * (e.g. Java_com_rtsoft_growtopia_SharedActivity_nativeOnKey), so the package,
 * class and method names below must match exactly what the engine expects.
 */
public class SharedActivity extends Activity {
    public static final String PackageName = "com.rtsoft.growtopia";

    // Proton GUI message types (Java -> native), used when forwarding input.
    static final int MESSAGE_TYPE_GUI_CLICK_START = 0;
    static final int MESSAGE_TYPE_GUI_CLICK_END = 1;
    static final int MESSAGE_TYPE_GUI_CLICK_MOVE = 2;
    static final int MESSAGE_TYPE_GUI_CLICK_MOVE_RAW = 3;

    public static SharedActivity app = null;
    public static AppGLSurfaceView mGLView = null;
    public static boolean bIsShuttingDown = false;

    // Edit-box / soft keyboard support (driven by native OS messages).
    public static EditText m_editText = null;
    public static RelativeLayout m_editTextRoot = null;
    public static boolean isKeyboardExist = false;
    public static boolean set_allow_dimming_asap = false;
    public static boolean set_disallow_dimming_asap = false;

    public RelativeLayout mViewGroup;
    public Handler mMainThreadHandler = null;
    public Runnable mUpdateMainThread = null;

    // ---- Native methods exported by the engine (Java -> native) ----
    public static native void nativeInitActivity(Activity activity);

    public static native void nativeOnKey(int state, int virtualKey, int unicodeChar);

    public static native void nativeOnInputText(String text);

    public static native void nativeOnTrackball(float x, float y);

    public static native void nativeOnAccelerometerUpdate(float x, float y, float z);

    public static native void nativeSendGUIEx(int messageType, int parm1, int parm2, int finger);

    public static native void nativeSendGUIStringEx(int messageType, int parm1, int parm2, int finger, String text);

    public static native float nativeGetScreenWidth();

    public static native float nativeGetScreenHeight();

    public static native float nativeGetEditBoxOffset();

    public static native int nativeGetChatString();

    public static native void nativeCancelBtnPressed();

    public static native void nativeUpdateConsoleLogPos(float pos);

    public static native void appOnAdInteractionFailed(String a, String b);

    @Override
    public void onCreate(Bundle bundle) {
        app = this;
        // The engine wants a reference to the Activity before anything else.
        nativeInitActivity(this);
        super.onCreate(bundle);

        mMainThreadHandler = new Handler(Looper.getMainLooper());
        mUpdateMainThread = new Runnable() {
            @Override
            public void run() {
                updateResultsInUi();
            }
        };

        mGLView = new AppGLSurfaceView(this, this);

        Window window = getWindow();
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mViewGroup = new RelativeLayout(this);
        mViewGroup.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        RelativeLayout.LayoutParams glParams = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        glParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        mGLView.setLayoutParams(glParams);
        mViewGroup.addView(mGLView);

        setContentView(mViewGroup);
        applyImmersiveFullscreen();
        mGLView.requestFocus();
        setVolumeControlStream(android.media.AudioManager.STREAM_MUSIC);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mGLView != null) {
            mGLView.onPause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mGLView != null) {
            mGLView.onResume();
        }
        applyImmersiveFullscreen();
    }

    @Override
    protected void onDestroy() {
        bIsShuttingDown = true;
        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_DEL) {
            nativeOnKey(1, TranslateKeycodeToProtonVirtualKey(keyCode), 8);
            return true;
        }
        if (event.getRepeatCount() == 0) {
            nativeOnKey(1, TranslateKeycodeToProtonVirtualKey(keyCode), event.getUnicodeChar());
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_DEL) {
            nativeOnKey(0, TranslateKeycodeToProtonVirtualKey(keyCode), 8);
            return true;
        }
        nativeOnKey(0, TranslateKeycodeToProtonVirtualKey(keyCode), event.getUnicodeChar());
        return super.onKeyUp(keyCode, event);
    }

    @Override
    public boolean onTrackballEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_MOVE) {
            nativeOnTrackball(event.getX(), event.getY());
        }
        return true;
    }

    /** Maps a few important Android key codes to Proton virtual keys; others pass through. */
    public static int TranslateKeycodeToProtonVirtualKey(int keyCode) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_DEL:
                return 8; // VIRTUAL_KEY_BACK (backspace)
            case KeyEvent.KEYCODE_ENTER:
                return 13;
            case KeyEvent.KEYCODE_BACK:
                return 27; // escape
            default:
                return keyCode;
        }
    }

    /** Runs on the UI thread when the engine asks us to react to an OS message. */
    public void updateResultsInUi() {
        if (mGLView == null) {
            return;
        }
        if (set_allow_dimming_asap) {
            set_allow_dimming_asap = false;
            mGLView.setKeepScreenOn(false);
        }
        if (set_disallow_dimming_asap) {
            set_disallow_dimming_asap = false;
            mGLView.setKeepScreenOn(true);
        }
    }

    /** Shows/hides the soft keyboard; called from the renderer's OS-message pump. */
    public void toggle_keyboard(boolean show) {
        InputMethodManager imm =
                (InputMethodManager) getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
        if (imm == null || mGLView == null) {
            return;
        }
        if (show) {
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
            isKeyboardExist = true;
        } else {
            imm.hideSoftInputFromWindow(mGLView.getWindowToken(), 0);
            isKeyboardExist = false;
        }
    }

    private void applyImmersiveFullscreen() {
        View decor = getWindow().getDecorView();
        decor.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }
}
