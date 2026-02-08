package com.BattleGrounds.GfxTool;

import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

import rikka.shizuku.Shizuku;

public class ShizukuHelper {

    private static final String TAG = "ShizukuHelper";
    private static final int SHIZUKU_PERMISSION_REQUEST_CODE = 1000;

    /**
     * ✅ Initializes Shizuku and ensures the service is available before requesting permissions.
     */
    public static void initializeShizuku(Context context) {
        Log.d(TAG, "🔄 Initializing Shizuku...");

        if (!Shizuku.pingBinder()) {
            Log.e(TAG, "❌ Shizuku is NOT running! Waiting for connection...");
            showShizukuNotRunningDialog(context);
            return;
        }

        // 📌 Register listener to detect when Shizuku is ready
        Shizuku.addBinderReceivedListener(() -> {
            Log.i(TAG, "✅ Shizuku is now available!");
            requestPermissionIfNeeded(context);
        });

        // 📌 Register listener to detect when Shizuku service dies
        Shizuku.addBinderDeadListener(() -> {
            Log.w(TAG, "⚠️ Shizuku connection lost!");
            showShizukuNotRunningDialog(context);
        });

        // 📌 Request permission if it's already available
        requestPermissionIfNeeded(context);
    }

    /**
     * ✅ Requests Shizuku permission only if it hasn't been granted yet.
     */
    private static void requestPermissionIfNeeded(Context context) {
        if (!Shizuku.pingBinder()) {
            Log.e(TAG, "❌ Shizuku is NOT running! Cannot request permission.");
            showShizukuNotRunningDialog(context);
            return;
        }

        if (Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "✅ Shizuku permission already granted.");
            return;
        }

        if (Shizuku.shouldShowRequestPermissionRationale()) {
            Log.w(TAG, "⚠️ Shizuku permission denied permanently!");
            showPermissionDeniedDialog(context);
        } else {
            Log.d(TAG, "📌 Requesting Shizuku permission...");
            Shizuku.requestPermission(SHIZUKU_PERMISSION_REQUEST_CODE);
        }
    }

    /**
     * ✅ Checks if Shizuku permission is granted.
     */
    public static boolean checkPermission(Context context) {
        if (!Shizuku.pingBinder()) {
            Log.e(TAG, "❌ Shizuku is NOT running!");
            showShizukuNotRunningDialog(context);
            return false;
        }

        if (Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "✅ Shizuku permission is granted.");
            return true;
        } else {
            Log.w(TAG, "⚠️ Shizuku permission not granted. Requesting...");
            requestPermissionIfNeeded(context);
            return false;
        }
    }

    /**
     * ✅ Registers permission result listener.
     */
    public static void registerListener() {
        Shizuku.addRequestPermissionResultListener((requestCode, grantResult) -> {
            if (requestCode == SHIZUKU_PERMISSION_REQUEST_CODE) {
                boolean granted = grantResult == PackageManager.PERMISSION_GRANTED;
                if (granted) {
                    Log.i(TAG, "✅ Shizuku Permission Granted!");
                } else {
                    Log.e(TAG, "❌ Shizuku Permission Denied!");
                }
            }
        });
    }

    /**
     * ✅ Unregisters permission result listener.
     */
    public static void unregisterListener() {
        Shizuku.removeRequestPermissionResultListener((requestCode, grantResult) -> {});
    }

    /**
     * ⚠️ Shows an alert dialog when Shizuku is not running.
     */
    private static void showShizukuNotRunningDialog(Context context) {
        new AlertDialog.Builder(context)
                .setTitle("Shizuku Not Running")
                .setMessage("⚠️ Shizuku is not running! Please start Shizuku and restart the app.")
                .setCancelable(false)
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .show();
    }

    /**
     * ⚠️ Shows an alert dialog when Shizuku permission is denied.
     */
    private static void showPermissionDeniedDialog(Context context) {
        new AlertDialog.Builder(context)
                .setTitle("Shizuku Permission Denied")
                .setMessage("⚠️ Shizuku permission is denied. Please grant permission in Shizuku settings.")
                .setCancelable(false)
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .show();
    }
}
