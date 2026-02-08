package com.BattleGrounds.GfxTool.ui.notifications;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.BattleGrounds.GfxTool.R;

import rikka.shizuku.Shizuku;
import rikka.shizuku.ShizukuProvider;

public class NotificationsFragment extends Fragment {

    private static final String TAG = "NotificationsFragment";

    private CardView btnPermission, btnTelegram, btnYoutube, btnPrivacyPolicy, btnFeedback;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_notifications, container, false);

        // Initialize buttons
        btnPermission = root.findViewById(R.id.btn_permission);
        btnTelegram = root.findViewById(R.id.btn_telegram);
        btnYoutube = root.findViewById(R.id.btn_youtube);
        btnPrivacyPolicy = root.findViewById(R.id.btn_privacy_policy);
        btnFeedback = root.findViewById(R.id.btn_feedback);

        // Button click listeners
        btnPermission.setOnClickListener(v -> handleShizukuPermission());
        btnTelegram.setOnClickListener(v -> openTelegram());
        btnYoutube.setOnClickListener(v -> openYouTube());
        btnPrivacyPolicy.setOnClickListener(v -> openPrivacyPolicy());
        btnFeedback.setOnClickListener(v -> sendFeedback());

        Log.d(TAG, "onCreateView: Fragment initialized successfully");
        return root;
    }

    // ✅ Handle Shizuku permission
    private void handleShizukuPermission() {
        if (!Shizuku.pingBinder()) {
            showShizukuNotRunningDialog();
            return;
        }

        if (Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getContext(), "Shizuku permission already granted", Toast.LENGTH_SHORT).show();
        } else {
            Shizuku.requestPermission(0);
            Toast.makeText(getContext(), "Requesting Shizuku permission...", Toast.LENGTH_SHORT).show();
        }
    }

    // ✅ Show help dialog if Shizuku is not running
    private void showShizukuNotRunningDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Shizuku not running")
                .setMessage("To use this feature, you must have Shizuku running on your device.\n\n" +
                        "1. Install the Shizuku app from GitHub or Play Store\n" +
                        "2. Start Shizuku via root or wireless debugging\n" +
                        "3. Grant permission when prompted\n\n" +
                        "Would you like to open the Shizuku app or watch a setup tutorial?")
                .setPositiveButton("Open Shizuku", (dialog, which) -> {
                    try {
                        Intent intent = requireContext().getPackageManager()
                                .getLaunchIntentForPackage("moe.shizuku.privileged.api");
                        if (intent != null) {
                            startActivity(intent);
                        } else {
                            Toast.makeText(getContext(), "Shizuku app not installed", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(getContext(), "Unable to open Shizuku", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Error launching Shizuku", e);
                    }
                })
                .setNeutralButton("Watch Tutorial", (dialog, which) -> {
                    String tutorialUrl = "https://www.youtube.com/watch?v=Q_Z2Jrj_iRU"; // Replace with your preferred video
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(tutorialUrl)));
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void openTelegram() {
        String telegramUrl = "https://t.me/+BXQb-MbVmaVjODg1";
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(telegramUrl)));
    }

    private void openYouTube() {
        String youtubeUrl = "https://youtube.com/@codeforgegaming-b6v";
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(youtubeUrl)));
    }

    private void openPrivacyPolicy() {
        String privacyPolicyUrl = "https://www.freeprivacypolicy.com/live/36fce55a-e1f4-456c-a828-1b058664698a";
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(privacyPolicyUrl)));
    }

    private void sendFeedback() {
        try {
            Uri uri = Uri.parse("market://details?id=" + requireContext().getPackageName());
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            intent.setPackage("com.android.vending");
            startActivity(intent);
        } catch (android.content.ActivityNotFoundException e) {
            Uri uri = Uri.parse("https://play.google.com/store/apps/details?id=" + requireContext().getPackageName());
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        }
    }
}
