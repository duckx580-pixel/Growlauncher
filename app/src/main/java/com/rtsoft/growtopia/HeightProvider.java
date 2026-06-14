package com.rtsoft.growtopia;

import android.app.Activity;
import android.graphics.Rect;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.PopupWindow;

/** Monitors soft-keyboard height changes via a zero-height PopupWindow. */
public class HeightProvider extends PopupWindow {
    private HeightListener listener;
    private final View parentView;
    private int lastHeight = 0;

    public interface HeightListener {
        void onHeightChanged(int height);
    }

    public HeightProvider(Activity activity) {
        super(activity);
        parentView = new View(activity);
        setContentView(parentView);
        setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
                | WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        setInputMethodMode(PopupWindow.INPUT_METHOD_NEEDED);
        setWidth(0);
        setHeight(WindowManager.LayoutParams.MATCH_PARENT);

        parentView.getViewTreeObserver().addOnGlobalLayoutListener(new android.view.ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                handleOnGlobalLayout();
            }
        });
    }

    public HeightProvider setHeightListener(HeightListener listener) {
        this.listener = listener;
        return this;
    }

    public void OnResume() {
        try {
            View rootView = parentView.getRootView();
            if (rootView != null && rootView.getWindowToken() != null) {
                showAtLocation(rootView, Gravity.NO_GRAVITY, 0, 0);
            }
        } catch (Exception ignored) {}
    }

    public void OnPause() {
        try { dismiss(); } catch (Exception ignored) {}
    }

    private void handleOnGlobalLayout() {
        Rect rect = new Rect();
        parentView.getWindowVisibleDisplayFrame(rect);
        int screenHeight = parentView.getRootView().getHeight();
        int keyboardHeight = screenHeight - rect.bottom;
        if (keyboardHeight != lastHeight) {
            lastHeight = keyboardHeight;
            if (listener != null) {
                listener.onHeightChanged(keyboardHeight);
            }
        }
    }
}
