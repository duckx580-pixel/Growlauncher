package com.rtsoft.growtopia;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

/**
 * Game entry activity. Loads the native engine and starts the Proton host.
 *
 * The native library is loaded in a static block so its JNI methods are
 * resolved before {@link SharedActivity#onCreate} calls into the engine.
 * The packaged file is libgrowtopia.so, hence loadLibrary("growtopia").
 */
public class Main extends SharedActivity {
    public static Main mainApp;
    public NativeAppInterface nativeAppInterface = new NativeAppInterface();

    static {
        System.loadLibrary("growtopia");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        mainApp = this;
        super.onCreate(savedInstanceState);
        handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (intent == null) {
            return;
        }
        final Uri data = intent.getData();
        if (data == null || mGLView == null) {
            return;
        }
        Log.d(PackageName, "Handling deep link: " + data);
        mGLView.post(new Runnable() {
            @Override
            public void run() {
                NativeAppInterface.OnDeepLinkProcess(data.getSchemeSpecificPart());
            }
        });
    }
}
