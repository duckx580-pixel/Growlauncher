package com.rtsoft.growtopia;

/**
 * Thin bridge for app-level engine callbacks (deep links, etc.).
 *
 * Kept minimal on purpose; extend it if the packaged engine reports missing
 * JNI methods on this class at runtime.
 */
public class NativeAppInterface {
    public static native void OnDeepLinkProcess(String url);
}
