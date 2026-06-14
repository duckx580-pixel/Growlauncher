package com.rtsoft.growtopia;

import android.app.Activity;

/** Initializes multi-touch input forwarding for the Proton engine. */
public class SharedMultiTouchInput {
    public static void init(Activity activity) {
        // The real implementation registers a touch handler; safe to no-op
        // because AppGLSurfaceView.onTouchEvent handles multi-touch directly.
    }
}
