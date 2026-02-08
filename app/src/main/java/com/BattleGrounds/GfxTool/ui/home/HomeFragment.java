package com.BattleGrounds.GfxTool.ui.home;

import android.animation.ValueAnimator;
import android.app.ActivityManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.StatFs;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.BattleGrounds.GfxTool.databinding.FragmentHomeBinding;
import com.unity3d.ads.IUnityAdsInitializationListener;
import com.unity3d.ads.UnityAds;
import com.unity3d.services.banners.BannerErrorInfo;
import com.unity3d.services.banners.BannerView;
import com.unity3d.services.banners.UnityBannerSize;

import java.util.Random;

public class HomeFragment extends Fragment {

    private static final String TAG = "HomeFragment";

    // ✅ CHANGE THIS: Set to true for testing, false for production
    private static final boolean IS_TEST_MODE = true; // 👈 Change this manually

    // Unity Ads Configuration
    private static final String UNITY_GAME_ID = "5914603"; // Your Game ID
    private static final String BANNER_PLACEMENT = "Banner_Android"; // Create this in Unity Dashboard

    private FragmentHomeBinding binding;
    private Handler handler;
    private Runnable updateTask;
    private final Random random = new Random();
    private BannerView bannerView;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Initialize Unity Ads
        initializeUnityAds();

        // Start system info updates
        handler = new Handler(Looper.getMainLooper());
        setDeviceInfo();

        updateTask = new Runnable() {
            @Override
            public void run() {
                updateRamUsage(binding.ramProgress, binding.ramPercentage);
                updateCpuUsage(binding.cpuProgress, binding.cpuUsageText);
                updateStorageUsage(binding.storageProgress, binding.storageUsageText);
                updateGpuUsage(binding.gpuProgress, binding.gpuUsageText);
                handler.postDelayed(this, 1000);
            }
        };
        handler.post(updateTask);

        return root;
    }

    private void initializeUnityAds() {
        boolean isTestMode = IS_TEST_MODE;
        String gameId = UNITY_GAME_ID;

        Log.d(TAG, "Unity Ads - Test Mode: " + isTestMode + ", Game ID: " + gameId);

        if (!UnityAds.isInitialized()) {
            UnityAds.initialize(requireActivity(), gameId, isTestMode, new IUnityAdsInitializationListener() {
                @Override
                public void onInitializationComplete() {
                    Log.d(TAG, "✅ Unity Ads initialization completed successfully");
                    // Setup banner with delay
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        setupUnityBanner(BANNER_PLACEMENT);
                    }, 1000);
                }

                @Override
                public void onInitializationFailed(UnityAds.UnityAdsInitializationError error, String message) {
                    Log.e(TAG, "❌ Unity Ads initialization failed: " + error + " - " + message);
                    hideBannerContainer();
                }
            });
        } else {
            Log.d(TAG, "Unity Ads already initialized");
            setupUnityBanner(BANNER_PLACEMENT);
        }
    }

    private void setupUnityBanner(String placementId) {
        if (binding == null || binding.bannerContainer == null) {
            Log.w(TAG, "Cannot setup banner - binding or container is null");
            return;
        }

        Log.d(TAG, "Setting up Unity Banner for placement: " + placementId);

        FrameLayout bannerContainer = binding.bannerContainer;

        // Create banner view
        bannerView = new BannerView(requireActivity(), placementId, new UnityBannerSize(320, 50));

        bannerView.setListener(new BannerView.Listener() {
            @Override
            public void onBannerLoaded(BannerView view) {
                Log.d(TAG, "✅ Unity Banner loaded successfully for placement: " + placementId);
                showBannerContainer();
            }

            @Override
            public void onBannerClick(BannerView view) {
                Log.d(TAG, "Unity Banner clicked");
            }

            @Override
            public void onBannerFailedToLoad(BannerView view, BannerErrorInfo errorInfo) {
                Log.e(TAG, "❌ Unity Banner failed to load for placement '" + placementId + "': " + errorInfo.errorMessage);
                Log.e(TAG, "Error code: " + errorInfo.errorCode);
                Log.e(TAG, "💡 Make sure you have created a banner placement named '" + placementId + "' in Unity Dashboard");

                hideBannerContainer();
            }

            @Override
            public void onBannerLeftApplication(BannerView view) {
                Log.d(TAG, "User left application due to banner ad");
            }
        });

        // Clear container and add banner
        bannerContainer.removeAllViews();
        bannerContainer.addView(bannerView);
        showBannerContainer();

        // Load the banner
        bannerView.load();

        Log.d(TAG, "Unity Banner setup completed and loading started for placement: " + placementId);
    }

    private void showBannerContainer() {
        if (binding != null && binding.bannerContainer != null) {
            binding.bannerContainer.setVisibility(View.VISIBLE);
        }
    }

    private void hideBannerContainer() {
        if (binding != null && binding.bannerContainer != null) {
            binding.bannerContainer.setVisibility(View.GONE);
        }
    }

    private void setDeviceInfo() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        String deviceName = manufacturer + " " + model;
        String processor = getProcessorName();
        String androidVersion = Build.VERSION.RELEASE;

        binding.deviceNameTextView.setText("Device: " + deviceName);
        binding.processorTextView.setText("Processor: " + processor);
        binding.androidVersionTextView.setText("Android Version: " + androidVersion);
    }

    private String getProcessorName() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return Build.SOC_MODEL != null ? Build.SOC_MODEL : Build.HARDWARE;
        } else {
            return Build.HARDWARE != null ? Build.HARDWARE : "Unknown";
        }
    }

    private void updateRamUsage(ProgressBar progressBar, TextView textView) {
        try {
            ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
            ActivityManager activityManager = (ActivityManager) requireActivity().getSystemService(Context.ACTIVITY_SERVICE);

            if (activityManager != null) {
                activityManager.getMemoryInfo(memoryInfo);

                long usedMem = memoryInfo.totalMem - memoryInfo.availMem;
                int usagePercent = (int) ((usedMem * 100) / memoryInfo.totalMem);

                animateProgressBar(progressBar, usagePercent);
                textView.setText(usagePercent + "%");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error updating RAM usage", e);
        }
    }

    private void updateCpuUsage(ProgressBar progressBar, TextView textView) {
        int usage = random.nextInt(20) + 20;
        animateProgressBar(progressBar, usage);
        textView.setText(usage + "%");
    }

    private void updateStorageUsage(ProgressBar progressBar, TextView textView) {
        try {
            StatFs stat = new StatFs(requireActivity().getFilesDir().getPath());
            long total = stat.getBlockCountLong() * stat.getBlockSizeLong();
            long free = stat.getAvailableBlocksLong() * stat.getBlockSizeLong();
            int usagePercent = (int) (((total - free) * 100) / total);

            animateProgressBar(progressBar, usagePercent);
            textView.setText(usagePercent + "%");
        } catch (Exception e) {
            Log.e(TAG, "Error updating storage usage", e);
        }
    }

    private void updateGpuUsage(ProgressBar progressBar, TextView textView) {
        int usage = random.nextInt(30) + 10;
        animateProgressBar(progressBar, usage);
        textView.setText(usage + "%");
    }

    private void animateProgressBar(ProgressBar progressBar, int toProgress) {
        if (progressBar == null) return;

        ValueAnimator animator = ValueAnimator.ofInt(progressBar.getProgress(), toProgress);
        animator.setDuration(500);
        animator.addUpdateListener(animation -> {
            if (progressBar != null) {
                progressBar.setProgress((int) animation.getAnimatedValue());
            }
        });
        animator.start();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (handler != null && updateTask != null) {
            handler.removeCallbacks(updateTask);
        }

        if (bannerView != null) {
            bannerView.destroy();
            bannerView = null;
        }

        binding = null;
        Log.d(TAG, "HomeFragment destroyed and cleaned up");
    }

    @Override
    public void onPause() {
        super.onPause();
        if (handler != null && updateTask != null) {
            handler.removeCallbacks(updateTask);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (handler != null && updateTask != null) {
            handler.post(updateTask);
        }
    }
}