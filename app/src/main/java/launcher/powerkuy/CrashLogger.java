package launcher.powerkuy;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Writes crash reports to getExternalFilesDir("crashes") so they can be retrieved without adb.
 *
 * Java crashes are caught by the uncaught exception handler. Native crashes kill the process
 * before any handler runs, so a breadcrumb is written before the game activity starts and
 * checked on the next launcher start: if it is still there, the previous run died natively and
 * the logcat ring buffer (which survives the process) is dumped to recover the tombstone.
 */
public final class CrashLogger {
    private static final String TAG = "CrashLogger";
    private static final String PREFS = "crash_logger";
    private static final String KEY_PENDING = "pending_launch";
    private static final int MAX_REPORTS = 10;

    private static Context appContext;

    private CrashLogger() {
    }

    public static void install(Context context) {
        appContext = context.getApplicationContext();
        final Thread.UncaughtExceptionHandler previous = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            try {
                StringWriter stack = new StringWriter();
                throwable.printStackTrace(new PrintWriter(stack));
                write("java-crash", "Thread: " + thread.getName() + "\n\n" + stack);
            } catch (Throwable t) {
                Log.e(TAG, "failed to write crash report", t);
            }
            if (previous != null) {
                previous.uncaughtException(thread, throwable);
            }
        });
    }

    /** Marks that the game activity is about to start, so a native crash can be detected later. */
    public static void markLaunchStarted() {
        prefs().edit().putString(KEY_PENDING, timestamp()).apply();
    }

    public static void markLaunchFinished() {
        prefs().edit().remove(KEY_PENDING).apply();
    }

    /**
     * @return the report for a native crash during the previous run, or null if it exited cleanly.
     */
    public static File consumePendingNativeCrash() {
        String started = prefs().getString(KEY_PENDING, null);
        if (started == null) {
            return null;
        }
        markLaunchFinished();
        return write("native-crash", "Game launched at " + started
                + " and the process died without returning.\n\n" + readLogcat());
    }

    public static File latestReport() {
        File[] reports = reportDir().listFiles();
        if (reports == null || reports.length == 0) {
            return null;
        }
        File newest = reports[0];
        for (File report : reports) {
            if (report.lastModified() > newest.lastModified()) {
                newest = report;
            }
        }
        return newest;
    }

    private static File write(String kind, String body) {
        File dir = reportDir();
        File report = new File(dir, kind + "-" + timestamp() + ".txt");
        try (FileWriter writer = new FileWriter(report)) {
            writer.write(deviceInfo());
            writer.write("\n");
            writer.write(body);
        } catch (Exception e) {
            Log.e(TAG, "cannot write " + report, e);
            return null;
        }
        prune(dir);
        return report;
    }

    private static String deviceInfo() {
        return "Growlauncher " + com.gentz.launcher.LauncherConfig.LAUNCHER_VERSION
                + " (Growtopia " + com.gentz.launcher.LauncherConfig.GROWTOPIA_VERSION + ")\n"
                + "Time: " + timestamp() + "\n"
                + "Device: " + Build.MANUFACTURER + " " + Build.MODEL + "\n"
                + "Android: " + Build.VERSION.RELEASE + " (API " + Build.VERSION.SDK_INT + ")\n"
                + "ABIs: " + android.text.TextUtils.join(", ", Build.SUPPORTED_ABIS) + "\n"
                + "Engine loaded: " + com.rtsoft.growtopia.NativeLibraries.isGameLoaded()
                + ", hook loaded: " + com.rtsoft.growtopia.NativeLibraries.isHookLoaded() + "\n";
    }

    private static String readLogcat() {
        StringBuilder out = new StringBuilder();
        try {
            Process process = Runtime.getRuntime().exec(
                    new String[]{"logcat", "-d", "-v", "threadtime", "-t", "600"});
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    out.append(line).append('\n');
                }
            }
        } catch (Exception e) {
            out.append("logcat unavailable: ").append(e);
        }
        return out.toString();
    }

    private static void prune(File dir) {
        File[] reports = dir.listFiles();
        if (reports == null || reports.length <= MAX_REPORTS) {
            return;
        }
        java.util.Arrays.sort(reports, (a, b) -> Long.compare(a.lastModified(), b.lastModified()));
        for (int i = 0; i < reports.length - MAX_REPORTS; i++) {
            if (!reports[i].delete()) {
                Log.w(TAG, "cannot delete " + reports[i]);
            }
        }
    }

    private static File reportDir() {
        File dir = new File(appContext.getExternalFilesDir(null), "crashes");
        if (!dir.exists() && !dir.mkdirs()) {
            Log.w(TAG, "cannot create " + dir);
        }
        return dir;
    }

    private static SharedPreferences prefs() {
        return appContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    private static String timestamp() {
        return new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.US).format(new Date());
    }
}
