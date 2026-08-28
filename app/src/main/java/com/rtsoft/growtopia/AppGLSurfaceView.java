package com.rtsoft.growtopia;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.Log;
import android.view.MotionEvent;

public class AppGLSurfaceView extends GLSurfaceView {
    private static boolean mMultiTouchClassAvailable;
    public static AppRenderer mRenderer;
    public SharedActivity app;

    public AppGLSurfaceView(Context context, SharedActivity activity) {
        super(context);
        setEGLContextClientVersion(2);
        setSystemUiVisibility(260);
        this.app = activity;
        if (SharedActivity.m_editText != null) {
            Log.d(SharedActivity.PackageName, "Setting focus options...");
            setFocusable(true);
            setFocusableInTouchMode(true);
            requestFocus();
        }
        setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        setPreserveEGLContextOnPause(false);
        AppRenderer renderer = new AppRenderer(this.app);
        try {
            setRenderer(renderer);
            mRenderer = renderer;
            setRenderMode(RENDERMODE_CONTINUOUSLY);
        } catch (Exception e) {
            Log.e(SharedActivity.PackageName, "setRenderer failed: " + e.getMessage());
        }
        try {
            WrapSharedMultiTouchInput.checkAvailable(this.app);
            mMultiTouchClassAvailable = true;
        } catch (Throwable t) {
            mMultiTouchClassAvailable = false;
        }
    }

    public static native void nativeOnTouch(int action, float x, float y, int finger);
    public static native void nativePause();
    public static native void nativeResume();

    @Override
    public void onPause() {
        if (SharedActivity.bIsShuttingDown) return;
        super.onPause();
        if (!NativeLibraries.isGameLoaded()) return;
        nativePause();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (SharedActivity.bIsShuttingDown || !NativeLibraries.isGameLoaded()) return;
        try { setSystemUiVisibility(260); } catch (Exception e) {}
        nativeResume();
    }

    @Override
    public synchronized boolean onTouchEvent(MotionEvent event) {
        if (!NativeLibraries.isGameLoaded()) return true;
        if (mMultiTouchClassAvailable) {
            return WrapSharedMultiTouchInput.OnInput(event);
        }
        nativeOnTouch(event.getAction(), event.getX(), event.getY(), 0);
        return true;
    }
}
