package com.rtsoft.growtopia;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;

/**
 * OpenGL ES surface that drives the Proton engine.
 *
 * It installs an {@link AppRenderer} (which calls the native render loop) and
 * forwards multi-touch input to the engine via SharedActivity.nativeSendGUIEx
 * on the GL thread, matching the engine's expected message protocol.
 */
public class AppGLSurfaceView extends GLSurfaceView {
    private final AppRenderer mRenderer;
    private final SharedActivity mActivity;

    public AppGLSurfaceView(Context context, SharedActivity activity) {
        super(context);
        mActivity = activity;

        setEGLContextClientVersion(2);
        setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        setPreserveEGLContextOnPause(true);

        mRenderer = new AppRenderer(activity);
        setRenderer(mRenderer);
        setRenderMode(RENDERMODE_CONTINUOUSLY);

        setFocusable(true);
        setFocusableInTouchMode(true);
    }

    @Override
    public boolean onTouchEvent(final MotionEvent event) {
        final int action = event.getActionMasked();
        final int pointerIndex = event.getActionIndex();
        final int pointerId = event.getPointerId(pointerIndex);
        final int pointerCount = event.getPointerCount();

        // Snapshot coordinates because MotionEvent is recycled off the UI thread.
        final float[] xs = new float[pointerCount];
        final float[] ys = new float[pointerCount];
        final int[] ids = new int[pointerCount];
        for (int i = 0; i < pointerCount; i++) {
            xs[i] = event.getX(i);
            ys[i] = event.getY(i);
            ids[i] = event.getPointerId(i);
        }
        final float downX = event.getX(pointerIndex);
        final float downY = event.getY(pointerIndex);

        queueEvent(new Runnable() {
            @Override
            public void run() {
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                    case MotionEvent.ACTION_POINTER_DOWN:
                        SharedActivity.nativeSendGUIEx(
                                SharedActivity.MESSAGE_TYPE_GUI_CLICK_START,
                                (int) downX, (int) downY, pointerId);
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_POINTER_UP:
                    case MotionEvent.ACTION_CANCEL:
                        SharedActivity.nativeSendGUIEx(
                                SharedActivity.MESSAGE_TYPE_GUI_CLICK_END,
                                (int) downX, (int) downY, pointerId);
                        break;
                    case MotionEvent.ACTION_MOVE:
                        for (int i = 0; i < xs.length; i++) {
                            SharedActivity.nativeSendGUIEx(
                                    SharedActivity.MESSAGE_TYPE_GUI_CLICK_MOVE,
                                    (int) xs[i], (int) ys[i], ids[i]);
                        }
                        break;
                    default:
                        break;
                }
            }
        });
        return true;
    }
}
