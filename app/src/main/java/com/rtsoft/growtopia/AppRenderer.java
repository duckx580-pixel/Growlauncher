package com.rtsoft.growtopia;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Looper;
import android.view.Surface;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * GLSurfaceView renderer that runs the Proton engine's frame loop.
 *
 * Each frame it advances the engine (nativeUpdate), draws (nativeRender) and
 * drains the engine's outgoing OS-message queue (nativeOSMessageGet) so the
 * Java side can react (e.g. show/hide the soft keyboard).
 */
public class AppRenderer implements GLSurfaceView.Renderer {
    // Proton OS messages (native -> Java) that the host must act on.
    static final int MESSAGE_OPEN_TEXT_BOX = 1;
    static final int MESSAGE_CLOSE_TEXT_BOX = 2;
    static final int MESSAGE_FINISH_APP = 6;

    private final SharedActivity app;

    public AppRenderer(SharedActivity activity) {
        this.app = activity;
    }

    // ---- Native methods exported by the engine (Java -> native) ----
    private static native void nativeInit();

    private static native void nativeDone();

    public static native void nativeRender();

    public static native void nativeResize(int width, int height);

    public static native void nativeSetWindow(Surface surface);

    public static native void nativeUpdate();

    private static native int nativeOSMessageGet();

    private static native int nativeGetLastOSMessageParm1();

    private static native float nativeGetLastOSMessageX();

    private static native float nativeGetLastOSMessageY();

    private static native String nativeGetLastOSMessageString();

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        nativeSetWindow(SharedActivity.mGLView.getHolder().getSurface());
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        nativeResize(width, height);
        nativeSetWindow(SharedActivity.mGLView.getHolder().getSurface());
    }

    @Override
    public synchronized void onDrawFrame(GL10 gl) {
        if (app == null || SharedActivity.bIsShuttingDown) {
            return;
        }
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        if (Looper.myLooper() != Looper.getMainLooper()) {
            nativeUpdate();
            nativeRender();
        }

        int message;
        while (!SharedActivity.bIsShuttingDown && (message = nativeOSMessageGet()) != 0) {
            switch (message) {
                case MESSAGE_OPEN_TEXT_BOX:
                    app.mMainThreadHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            app.toggle_keyboard(true);
                        }
                    });
                    break;
                case MESSAGE_CLOSE_TEXT_BOX:
                    app.mMainThreadHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            app.toggle_keyboard(false);
                        }
                    });
                    break;
                case MESSAGE_FINISH_APP:
                    SharedActivity.bIsShuttingDown = true;
                    app.mMainThreadHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            app.finish();
                        }
                    });
                    break;
                default:
                    // Other OS messages (ads, IAP, dimming, etc.) are not handled
                    // by this minimal host; they are safely ignored.
                    break;
            }
        }
    }
}
