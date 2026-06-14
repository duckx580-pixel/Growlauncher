package com.rtsoft.growtopia;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;

/**
 * RTSoft Proton SDK Android host activity.
 *
 * Matches the decompiled structure of the real Growtopia/Growlauncher
 * so that the native engine's JNI lookups find all expected classes,
 * fields and methods.
 */
public class SharedActivity extends Activity {

    // ---- Proton GUI message types ----
    static final int MESSAGE_TYPE_GUI_CLICK_START = 0;
    static final int MESSAGE_TYPE_GUI_CLICK_END = 1;
    static final int MESSAGE_TYPE_GUI_CLICK_MOVE = 2;
    static final int MESSAGE_TYPE_GUI_CLICK_MOVE_RAW = 3;
    static final int MESSAGE_TYPE_GUI_ACCELEROMETER = 4;
    static final int MESSAGE_TYPE_GUI_TRACKBALL = 5;
    static final int MESSAGE_TYPE_GUI_CHAR = 6;
    static final int MESSAGE_TYPE_GUI_KEYBWD_STRING = 7;
    static final int MESSAGE_TYPE_GUI_KEYBWD_CURSORPOS = 8;
    static final int MESSAGE_TYPE_GUI_COPY = 9;
    static final int MESSAGE_TYPE_GUI_PASTE = 10;
    static final int MESSAGE_TYPE_GUI_TOGGLE_FULLSCREEN = 11;
    static final int MESSAGE_TYPE_SET_ENTITY_VARIANT = 12;
    static final int MESSAGE_TYPE_CALL_ENTITY_FUNCTION = 13;
    static final int MESSAGE_TYPE_CALL_COMPONENT_FUNCTION_BY_NAME = 14;
    static final int MESSAGE_TYPE_PLAY_SOUND = 15;
    static final int MESSAGE_TYPE_REMOVE_COMPONENT = 17;
    static final int MESSAGE_TYPE_ADD_COMPONENT = 18;
    static final int MESSAGE_TYPE_OS_CONNECTION_CHECKED = 19;
    static final int MESSAGE_TYPE_PLAY_MUSIC = 20;
    static final int MESSAGE_TYPE_PRELOAD_SOUND = 22;
    static final int MESSAGE_TYPE_GUI_CHAR_RAW = 23;
    static final int MESSAGE_TYPE_SET_SOUND_ENABLED = 24;
    static final int MESSAGE_TYPE_TAPJOY_AD_READY = 25;
    static final int MESSAGE_TYPE_TAPJOY_FEATURED_APP_READY = 26;
    static final int MESSAGE_TYPE_TAPJOY_MOVIE_AD_READY = 27;
    static final int MESSAGE_TYPE_IAP_RESULT = 28;
    static final int MESSAGE_TYPE_IAP_ITEM_STATE = 29;
    static final int MESSAGE_TYPE_GUI_JOYPAD_BUTTONS = 37;
    static final int MESSAGE_TYPE_GUI_JOYPAD = 38;
    static final int MESSAGE_TYPE_GUI_JOYPAD_CONNECT = 39;
    static final int MESSAGE_TYPE_CALL_ENTITY_FUNCTION_RECURSIVELY = 40;
    static final int MESSAGE_OPEN_TEXTBOX_SECRET = 41;
    static final int MESSAGE_TYPE_HW_TOUCH_KEYBOARD_WILL_SHOW = 41;
    static final int MESSAGE_TYPE_HW_TOUCH_KEYBOARD_WILL_HIDE = 42;
    static final int MESSAGE_TYPE_HW_KEYBOARD_INPUT_ENDING = 43;
    static final int MESSAGE_TYPE_HW_KEYBOARD_INPUT_STARTING = 44;
    static final int MESSAGE_TYPE_IAP_PURCHASED_LIST_STATE = 45;
    static final int MESSAGE_TYPE_CALL_STATIC_FUNCTION = 46;
    static final int MESSAGE_TYPE_APP_VERSION = 47;
    static final int MESSAGE_TYPE_IAP_ITEM_INFO_RESULT = 54;

    // ---- Static fields the engine reads/writes ----
    public static String PackageName = "com.rtsoft.growtopia";
    public static String dllname = "rtsomething";
    public static SharedActivity app = null;
    public static AppGLSurfaceView mGLView = null;
    public static boolean bIsShuttingDown = false;
    public static int apiVersion = 0;

    public static boolean securityEnabled = false;
    public static boolean HookedEnabled = false;
    public static boolean IAPEnabled = false;

    public static boolean isKeyboardExist = false;
    public static boolean passwordField = false;
    public static int m_KeyBoardHeight = 0;
    public static boolean set_allow_dimming_asap = false;
    public static boolean set_disallow_dimming_asap = false;
    public static boolean m_focusOnKeyboard = false;
    public static boolean m_focusOffKeyboard = false;

    public static EditText m_editText = null;
    public static Button m_DoneButton = null;
    public static Button m_CancelButton = null;
    public static RelativeLayout m_editTextRoot = null;
    public static String m_before = "";
    public static View adView = null;
    public static RelativeLayout adLinearLayout = null;
    public static int adBannerWidth = 0;
    public static int adBannerHeight = 0;

    public static boolean update_display_ad = false;
    public static boolean run_hooked = false;
    public static int tapjoy_ad_show = 0;

    public RelativeLayout mViewGroup;
    public Handler mMainThreadHandler = null;
    public Runnable mUpdateMainThread = null;
    public String BASE64_PUBLIC_KEY = "";
    public boolean is_demo = false;

    // ---- Native methods (Java -> native) ----
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

    // ---- Lifecycle ----

    @Override
    public void onCreate(Bundle bundle) {
        app = this;
        nativeInitActivity(this);
        apiVersion = Build.VERSION.SDK_INT;
        Log.d(PackageName, "API Level: " + apiVersion);
        SharedMultiTouchInput.init(this);
        super.onCreate(bundle);

        mMainThreadHandler = new Handler(Looper.getMainLooper());
        mUpdateMainThread = new Runnable() {
            @Override public void run() { updateResultsInUi(); }
        };

        mGLView = new AppGLSurfaceView(this, this);

        Window window = getWindow();
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        mViewGroup = new RelativeLayout(this);
        mViewGroup.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        RelativeLayout.LayoutParams glParams = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        glParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        mGLView.setLayoutParams(glParams);
        mViewGroup.addView(mGLView);

        setContentView(mViewGroup);

        mGLView.requestFocus();
        setVolumeControlStream(android.media.AudioManager.STREAM_MUSIC);

        adLinearLayout = new RelativeLayout(this);
        update_display_ad = false;
        run_hooked = false;
        tapjoy_ad_show = 0;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mGLView != null) mGLView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mGLView != null) mGLView.onResume();
        applyImmersiveFullscreen();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Log.d(PackageName, "Destroying...");
        bIsShuttingDown = true;
        super.onDestroy();
    }

    // ---- Key / trackball input ----

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_DEL) {
            return true;
        }
        if (event.getRepeatCount() > 0) {
            return super.onKeyDown(keyCode, event);
        }
        if (event.isAltPressed() && keyCode == KeyEvent.KEYCODE_BACK) {
            return true;
        }
        nativeOnKey(1, TranslateKeycodeToProtonVirtualKey(keyCode), event.getUnicodeChar());
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_DEL) {
            return true;
        }
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            nativeOnKey(1, 27, 27);
            nativeOnKey(0, 27, 27);
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

    public static int TranslateKeycodeToProtonVirtualKey(int keyCode) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_DEL: return 8;
            case KeyEvent.KEYCODE_ENTER: return 13;
            case KeyEvent.KEYCODE_BACK: return 27;
            default: return keyCode;
        }
    }

    // ---- UI helpers ----

    public void updateResultsInUi() {
        if (mGLView == null) return;
        if (set_allow_dimming_asap) {
            set_allow_dimming_asap = false;
            mGLView.setKeepScreenOn(false);
        }
        if (set_disallow_dimming_asap) {
            set_disallow_dimming_asap = false;
            mGLView.setKeepScreenOn(true);
        }
        if (m_focusOnKeyboard) {
            m_focusOnKeyboard = false;
        }
        if (m_focusOffKeyboard) {
            m_focusOffKeyboard = false;
            mGLView.requestFocus();
        }
    }

    public void toggle_keyboard(boolean show) {
        InputMethodManager imm =
                (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (imm == null || mGLView == null) return;
        if (show) {
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
            isKeyboardExist = true;
        } else {
            imm.hideSoftInputFromWindow(mGLView.getWindowToken(), 0);
            isKeyboardExist = false;
        }
    }

    public void clearIngameInputBox() {
        // stub – called by real SharedActivity.onCreate
    }

    public void sendVersionDetails() {
        // stub – sends version info to the engine
    }

    public void UpdateEditBoxInView(boolean show, boolean secret) {
        // stub – manages the in-game text edit overlay
    }

    public void UpdateEditBoxRootViewPosition() {
        // stub – repositions the edit overlay on keyboard height change
    }

    public String GetAppsflyerUID() {
        return "";
    }

    public void setupSystemBarAppearance() {
        // stub
    }

    public void setupInsetsHandling() {
        // stub
    }

    public void applyImmersiveFullscreen() {
        if (Build.VERSION.SDK_INT >= 30) {
            WindowInsetsController c = getWindow().getInsetsController();
            if (c != null) {
                c.hide(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
                c.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            }
        } else {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

    public void doCheck() {
        // stub – license check
    }

    public static void create_dir_recursively(String base, String dir) {
        // stub – native helper
    }

    public static void setViewVisibility(View v, boolean visible) {
        if (v != null) v.setVisibility(visible ? View.VISIBLE : View.GONE);
    }
}
