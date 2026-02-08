package com.BattleGrounds.GfxTool;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.annotation.NonNull;

import com.unity3d.ads.IUnityAdsInitializationListener;
import com.unity3d.ads.IUnityAdsLoadListener;
import com.unity3d.ads.IUnityAdsShowListener;
import com.unity3d.ads.UnityAds;
import com.unity3d.ads.UnityAdsShowOptions;

import com.ncorti.slidetoact.SlideToActView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;

import rikka.shizuku.Shizuku;

public class MainActivity2 extends AppCompatActivity implements IUnityAdsInitializationListener, IUnityAdsLoadListener, IUnityAdsShowListener {

    private static final String PREFS_NAME = "GamePrefs";
    private static final String PREF_BGMI_URI = "bgmiFolderUri";
    private static final int REPOSITION_DELAY = 2000;
    private static final String WARNING_MESSAGE = "Warning: Please grant access to Android/data folder for this feature to work properly.";

    // Unity Ads configuration
    private static final String UNITY_GAME_ID = "5914603"; // Test Game ID - Replace with your actual Game ID
    private static final String REWARDED_AD_UNIT_ID = "Rewarded_Android"; // Default rewarded placement ID
    private static final boolean TEST_MODE = false; // Set to false for production
    private static final String TAG = "MainActivity2";

    private SharedPreferences sharedPreferences;
    private String pendingAssetFileName; // Store which FPS setting to apply after ad
    private String pendingFpsType; // Store FPS type for display purposes
    private boolean isAdLoaded = false;

    private final ActivityResultLauncher<Intent> folderPickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri != null) {
                        String folderPath = getFolderPath(uri);
                        String selectedGamePackage = getIntent().getStringExtra("GAME_PACKAGE");

                        if (selectedGamePackage != null && isValidGameFolder(folderPath, selectedGamePackage)) {
                            try {
                                final int takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION;
                                getContentResolver().takePersistableUriPermission(uri, takeFlags);
                                sharedPreferences.edit().putString(PREF_BGMI_URI, uri.toString()).apply();
                                Toast.makeText(this, "✅ Correct folder selected!", Toast.LENGTH_SHORT).show();
                            } catch (SecurityException e) {
                                Toast.makeText(this, "❌ Permission error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        } else {
                            Toast.makeText(this, "❌ Incorrect folder! Please select the folder: Android/data/" + selectedGamePackage, Toast.LENGTH_LONG).show();
                            requestBGMIFolderAccess();
                        }
                    }
                } else {
                    Toast.makeText(this, "❌ Folder access denied! Please select the correct folder.", Toast.LENGTH_LONG).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        Log.d(TAG, "onCreate() started");

        // Initialize Unity Ads
        initializeUnityAds();

        // Initialize Shizuku and wait until it's ready before using it
        ShizukuHelper.initializeShizuku(this);
        ShizukuHelper.registerListener();

        // Fetch the ImageView for displaying the game image
        ImageView mainGameImageView = findViewById(R.id.mainGameImage);
        Button resetButton = findViewById(R.id.reset);
        TextView gameVersionTextView = findViewById(R.id.game_name_text);

        // Get the selected game image from the intent
        int gameImageResId = getIntent().getIntExtra("GAME_IMAGE", R.drawable.boosts);
        mainGameImageView.setImageResource(gameImageResId);

        // Get the selected game version from the intent
        String gameVersion = getIntent().getStringExtra("GAME_VERSION");
        if (gameVersion != null) {
            gameVersionTextView.setText(gameVersion);
        } else {
            gameVersionTextView.setText("Unknown Version");
        }

        // Functionality for reset button
        resetButton.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && Shizuku.pingBinder()) {
                deleteActiveSavWithShizuku();
            } else {
                deleteActiveSavWithSAF();
            }
        });

        // Setup swipe buttons with reward ads
        setupSwipeButtons();

        // Shizuku Permission Check (For Android 11+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Shizuku.pingBinder()) {
                Log.e("Shizuku", "❌ Shizuku is NOT running!");
                showShizukuNotRunningDialog();
                return;
            }

            if (Shizuku.checkSelfPermission() != PackageManager.PERMISSION_GRANTED) {
                Log.w("Shizuku", "⚠️ Shizuku permission not granted. Requesting...");
                Shizuku.requestPermission(0);
            } else {
                Log.d("Shizuku", "✅ Shizuku is already running and permission granted.");
            }
        }
        Button customizedUploadBtn = findViewById(R.id.customized_upload);
        customizedUploadBtn.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity2.this, MainActivity3.class);

            // Optional: Pass the current game info if needed
            intent.putExtra("GAME_PACKAGE", getIntent().getStringExtra("GAME_PACKAGE"));
            intent.putExtra("GAME_VERSION", getIntent().getStringExtra("GAME_VERSION"));

            startActivity(intent);
        });
    }

    /**
     * Initialize Unity Ads SDK
     */
    private void initializeUnityAds() {
        UnityAds.initialize(getApplicationContext(), UNITY_GAME_ID, TEST_MODE, this);
    }

    // Unity Ads Initialization Listener

    @Override
    public void onInitializationComplete() {
        Log.d(TAG, "✅ Unity Ads initialization complete");
        Toast.makeText(this, "✅ Ads Ready!", Toast.LENGTH_SHORT).show();

        // Load the rewarded ad
        loadRewardedAd();
    }

    @Override
    public void onInitializationFailed(UnityAds.UnityAdsInitializationError error, String message) {
        Log.e(TAG, "❌ Unity Ads initialization failed: " + error.toString() + " - " + message);
        Toast.makeText(this, "⚠️ Ads initialization failed", Toast.LENGTH_SHORT).show();
    }

    /**
     * Load rewarded ad
     */
    private void loadRewardedAd() {
        UnityAds.load(REWARDED_AD_UNIT_ID, this);
    }

    // Unity Ads Load Listener
    @Override
    public void onUnityAdsAdLoaded(String placementId) {
        Log.d(TAG, "✅ Unity Ads ad loaded: " + placementId);
        isAdLoaded = true;
    }

    @Override
    public void onUnityAdsFailedToLoad(String placementId, UnityAds.UnityAdsLoadError error, String message) {
        Log.e(TAG, "❌ Unity Ads failed to load: " + error.toString() + " - " + message);
        isAdLoaded = false;
    }

    // Unity Ads Show Listener
    @Override
    public void onUnityAdsShowStart(String placementId) {
        Log.d(TAG, "Unity Ads show start: " + placementId);
    }

    @Override
    public void onUnityAdsShowClick(String placementId) {
        Log.d(TAG, "Unity Ads show click: " + placementId);
    }

    @Override
    public void onUnityAdsShowComplete(String placementId, UnityAds.UnityAdsShowCompletionState state) {
        Log.d(TAG, "Unity Ads show complete: " + placementId + " - " + state.toString());

        if (state == UnityAds.UnityAdsShowCompletionState.COMPLETED) {
            // User watched the complete ad - reward them
            Log.d(TAG, "✅ User earned reward!");
            Toast.makeText(this, "🎉 " + pendingFpsType + " Unlocked!", Toast.LENGTH_SHORT).show();

            // Apply the FPS setting
            if (pendingAssetFileName != null) {
                applyActiveSav(pendingAssetFileName);
                pendingAssetFileName = null;
                pendingFpsType = null;
            }
        } else if (state == UnityAds.UnityAdsShowCompletionState.SKIPPED) {
            // User skipped the ad - don't reward
            Log.w(TAG, "⚠️ User skipped the ad");
            Toast.makeText(this, "❌ You need to watch the complete ad to unlock " + pendingFpsType, Toast.LENGTH_LONG).show();
            pendingAssetFileName = null;
            pendingFpsType = null;
        }

        // Load next ad for future use
        isAdLoaded = false;
        loadRewardedAd();
    }

    @Override
    public void onUnityAdsShowFailure(String placementId, UnityAds.UnityAdsShowError error, String message) {
        Log.e(TAG, "❌ Unity Ads show failure: " + error.toString() + " - " + message);
        Toast.makeText(this, "❌ Ad failed to show. You can still use the feature.", Toast.LENGTH_SHORT).show();

        // Still apply the FPS setting even if ad fails
        if (pendingAssetFileName != null) {
            applyActiveSav(pendingAssetFileName);
            pendingAssetFileName = null;
            pendingFpsType = null;
        }

        // Try to load ad again
        isAdLoaded = false;
        loadRewardedAd();
    }

    /**
     * Setup swipe buttons with reward ad integration
     */
    private void setupSwipeButtons() {
        SlideToActView swipe60 = findViewById(R.id.swipe_60fps);
        SlideToActView swipe90 = findViewById(R.id.swipe_90fps);
        SlideToActView swipe120 = findViewById(R.id.swipe_120fps);

        swipe60.setOnSlideCompleteListener(slider -> {
            showRewardedAdAndApply("Active_60.sav", "60 FPS");
            slider.resetSlider();
        });

        swipe90.setOnSlideCompleteListener(slider -> {
            showRewardedAdAndApply("Active_90.sav", "90 FPS");
            slider.resetSlider();
        });

        swipe120.setOnSlideCompleteListener(slider -> {
            showRewardedAdAndApply("Active_120.sav", "120 FPS");
            slider.resetSlider();
        });
    }

    /**
     * Show rewarded ad before applying FPS setting
     */
    private void showRewardedAdAndApply(String assetFileName, String fpsType) {
        pendingAssetFileName = assetFileName;
        pendingFpsType = fpsType;

        if (isAdLoaded) {
            // Show loading message
            Toast.makeText(this, "📺 Please watch the ad to unlock " + fpsType, Toast.LENGTH_SHORT).show();

            // Show the rewarded ad
            UnityAds.show(this, REWARDED_AD_UNIT_ID, new UnityAdsShowOptions(), this);
        } else {
            // No ad available, but still allow the feature
            Log.w(TAG, "⚠️ No rewarded ad available, applying directly");
            Toast.makeText(this, "⚠️ Ad not ready. " + fpsType + " applied directly.", Toast.LENGTH_SHORT).show();
            applyActiveSav(assetFileName);

            // Try to load ad for next time
            loadRewardedAd();
        }
    }

    private void deleteActiveSavWithShizuku() {
        if (!Shizuku.pingBinder()) {
            Toast.makeText(this, "❌ Shizuku is NOT running!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (Shizuku.checkSelfPermission() != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "⚠️ Shizuku permission required!", Toast.LENGTH_SHORT).show();
            return;
        }

        String gamePackage = getIntent().getStringExtra("GAME_PACKAGE");
        if (gamePackage == null) {
            Toast.makeText(this, "❌ Game package not found!", Toast.LENGTH_SHORT).show();
            return;
        }

        String targetPath = "/storage/emulated/0/Android/data/" + gamePackage + "/files/UE4Game/ShadowTrackerExtra/ShadowTrackerExtra/Saved/SaveGames/Active.sav";

        boolean success = executeShellCommand("rm -f " + targetPath);

        if (success) {
            Toast.makeText(this, "✅  Reset Successfully", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "❌ Failed to Reset ", Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteActiveSavWithSAF() {
        String savedUriString = sharedPreferences.getString(PREF_BGMI_URI, null);

        if (savedUriString == null) {
            requestBGMIFolderAccess();
            return;
        }

        Uri treeUri = Uri.parse(savedUriString);

        if (!hasValidUriPermission(treeUri)) {
            sharedPreferences.edit().remove(PREF_BGMI_URI).apply();
            requestBGMIFolderAccess();
            return;
        }

        try {
            String baseDocumentId = DocumentsContract.getTreeDocumentId(treeUri);
            String saveGamesFolderId = baseDocumentId + "/files/UE4Game/ShadowTrackerExtra/ShadowTrackerExtra/Saved/SaveGames";
            String activeSavDocumentId = saveGamesFolderId + "/Active.sav";
            Uri activeSavUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, activeSavDocumentId);

            if (fileExists(activeSavUri)) {
                boolean deleted = DocumentsContract.deleteDocument(getContentResolver(), activeSavUri);
                if (deleted) {
                    Toast.makeText(this, "✅ Active.sav Reset Successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "❌ Failed to Reset Active.sav", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "ℹ️ Active.sav not found", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            Log.e("ResetActiveSav", "Error deleting Active.sav", e);
            Toast.makeText(this, "❌ Error while deleting", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Shows an AlertDialog when Shizuku is not running.
     */
    private void showShizukuNotRunningDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Shizuku Not Running")
                .setMessage("⚠️ Shizuku is not running! Please start Shizuku and restart the app.")
                .setCancelable(false)
                .setPositiveButton("OK", (dialog, which) -> {
                    dialog.dismiss();
                    finish();
                })
                .show();
    }

    // Function to retrieve folder path from URI
    private String getFolderPath(Uri uri) {
        String docId = DocumentsContract.getTreeDocumentId(uri);
        if (docId != null) {
            return docId;
        }
        return null;
    }

    private void applyActiveSav(String assetFileName) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && Shizuku.pingBinder()) {
            applyActiveSavUsingShizuku(assetFileName);
        } else {
            applyActiveSavUsingSAF(assetFileName);
        }
    }

    private void applyActiveSavUsingShizuku(String assetFileName) {
        if (!Shizuku.pingBinder()) {
            Toast.makeText(this, "❌ Shizuku is NOT running!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (Shizuku.checkSelfPermission() != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "⚠️ Shizuku permission required!", Toast.LENGTH_SHORT).show();
            return;
        }

        String gamePackage = getIntent().getStringExtra("GAME_PACKAGE");
        if (gamePackage == null) {
            Toast.makeText(this, "❌ Game package not found!", Toast.LENGTH_SHORT).show();
            return;
        }

        String targetPath = "/storage/emulated/0/Android/data/" + gamePackage + "/files/UE4Game/ShadowTrackerExtra/ShadowTrackerExtra/Saved/SaveGames/Active.sav";
        String tempPath = "/data/local/tmp/Active.sav";

        try {
            File tempFile = new File(getExternalCacheDir(), "Active.sav");
            try (InputStream is = getAssets().open(assetFileName);
                 OutputStream os = new FileOutputStream(tempFile)) {
                byte[] buffer = new byte[1024];
                int length;
                while ((length = is.read(buffer)) > 0) {
                    os.write(buffer, 0, length);
                }
            }

            String targetDir = "/storage/emulated/0/Android/data/" + gamePackage + "/files/UE4Game/ShadowTrackerExtra/ShadowTrackerExtra/Saved/SaveGames";
            executeShellCommand("mkdir -p " + targetDir);
            executeShellCommand("cp " + tempFile.getAbsolutePath() + " " + tempPath);
            executeShellCommand("chmod 777 " + tempPath);
            executeShellCommand("mv -f " + tempPath + " " + targetPath);

            Toast.makeText(this, "✅ FPS Unlocked!", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Log.e("ShizukuCopy", "Error copying asset file", e);
            Toast.makeText(this, "❌ Failed to copy file!", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean executeShellCommand(String command) {
        try {
            Method newProcessMethod = Shizuku.class.getDeclaredMethod("newProcess", String[].class, String[].class, String.class);
            newProcessMethod.setAccessible(true);

            Process process = (Process) newProcessMethod.invoke(null, new Object[]{
                    new String[]{"sh", "-c", command},
                    null,
                    null
            });

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                Log.e("ShizukuShell", "Command failed: " + command + " Exit code: " + exitCode);
            }
            return exitCode == 0;
        } catch (Exception e) {
            Log.e("ShizukuShell", "Error executing command: " + command, e);
            return false;
        }
    }

    /**
     * Show an alert dialog when Shizuku is not running or permission is missing.
     */
    private void showShizukuErrorDialog(String title, String message) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .setNegativeButton("Open Shizuku", (dialog, which) -> {
                    try {
                        startActivity(getPackageManager().getLaunchIntentForPackage("moe.shizuku.privileged.api"));
                    } catch (Exception e) {
                        Toast.makeText(this, "❌ Shizuku app not found!", Toast.LENGTH_SHORT).show();
                    }
                })
                .show();
    }

    private void applyActiveSavUsingSAF(String assetFileName) {
        String savedUriString = sharedPreferences.getString(PREF_BGMI_URI, null);

        if (savedUriString == null) {
            requestBGMIFolderAccess();
            return;
        }

        Uri treeUri = Uri.parse(savedUriString);

        if (!hasValidUriPermission(treeUri)) {
            sharedPreferences.edit().remove(PREF_BGMI_URI).apply();
            requestBGMIFolderAccess();
            return;
        }

        try {
            String baseDocumentId = DocumentsContract.getTreeDocumentId(treeUri);
            String saveGamesFolderId = baseDocumentId + "/files/UE4Game/ShadowTrackerExtra/ShadowTrackerExtra/Saved/SaveGames";
            Uri saveGamesFolderUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, saveGamesFolderId);

            if (!folderExists(saveGamesFolderUri)) {
                createFolder(treeUri, saveGamesFolderId, "SaveGames");
            }

            String activeSavDocumentId = saveGamesFolderId + "/Active.sav";
            Uri activeSavUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, activeSavDocumentId);

            if (!fileExists(activeSavUri)) {
                Log.i("FileCheck", "Active.sav does not exist, creating...");
                createFile(treeUri, saveGamesFolderUri, "Active.sav", "application/octet-stream");
            }

            if (!isAssetExists(assetFileName)) {
                Log.e("ApplyActiveSav", "Error: Asset file " + assetFileName + " not found!");
                return;
            }

            try (OutputStream os = getContentResolver().openOutputStream(activeSavUri, "w");
                 InputStream is = getAssets().open(assetFileName)) {

                byte[] buffer = new byte[1024];
                int length;
                while ((length = is.read(buffer)) > 0) {
                    os.write(buffer, 0, length);
                }
                os.flush();
            }

            Toast.makeText(this, "FPS Unlocked!", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Log.e("ApplyActiveSav", "Error applying Active.sav", e);
        }
    }

    private boolean isAssetExists(String fileName) {
        try {
            String[] assets = getAssets().list("");
            for (String asset : assets) {
                if (asset.equals(fileName)) {
                    return true;
                }
            }
        } catch (IOException e) {
            Log.e("isAssetExists", "Error checking assets", e);
        }
        return false;
    }

    private void createFolder(Uri treeUri, Uri parentFolderUri, String folderName) {
        try {
            Uri newFolderUri = DocumentsContract.createDocument(
                    getContentResolver(), parentFolderUri,
                    DocumentsContract.Document.MIME_TYPE_DIR,
                    folderName);

            if (newFolderUri != null) {
                Log.i("CreateFolder", "Folder created: " + newFolderUri.toString());
            }
        } catch (Exception e) {
            Log.e("CreateFolder", "Failed to create folder: " + folderName, e);
        }
    }

    private boolean folderExists(Uri folderUri) {
        try (Cursor cursor = getContentResolver().query(
                folderUri,
                new String[]{DocumentsContract.Document.COLUMN_DOCUMENT_ID},
                null, null, null)) {
            return (cursor != null && cursor.getCount() > 0);
        } catch (Exception e) {
            Log.e("FolderExists", "Error checking folder existence: " + e.getMessage());
            return false;
        }
    }

    private void createFolder(Uri treeUri, String parentFolderId, String folderName) {
        try {
            Uri parentUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, parentFolderId);
            Uri newFolderUri = DocumentsContract.createDocument(
                    getContentResolver(), parentUri,
                    DocumentsContract.Document.MIME_TYPE_DIR,
                    folderName);

            if (newFolderUri != null) {
                Log.i("CreateFolder", "Folder created: " + newFolderUri.toString());
            }
        } catch (Exception e) {
            Log.e("CreateFolder", "Failed to create folder: " + folderName, e);
        }
    }

    private boolean fileExists(Uri fileUri) {
        try (Cursor cursor = getContentResolver().query(
                fileUri,
                new String[]{DocumentsContract.Document.COLUMN_DOCUMENT_ID},
                null, null, null)) {
            return (cursor != null && cursor.getCount() > 0);
        } catch (Exception e) {
            Log.e("FileExists", "Error checking file existence: " + e.getMessage());
            return false;
        }
    }

    private void createFile(Uri treeUri, Uri parentFolderUri, String fileName, String mimeType) {
        try {
            Uri newFileUri = DocumentsContract.createDocument(
                    getContentResolver(), parentFolderUri, mimeType, fileName);

            if (newFileUri != null) {
                Log.i("CreateFile", "File created: " + newFileUri.toString());
            }
        } catch (Exception e) {
            Log.e("CreateFile", "Failed to create file: " + fileName, e);
        }
    }

    private void requestBGMIFolderAccess() {
        String selectedGamePackage = getIntent().getStringExtra("GAME_PACKAGE");

        try {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
            Uri initialUri = Uri.parse("content://com.android.externalstorage.documents/document/primary:Android/data/" + selectedGamePackage);
            intent.putExtra("android.provider.extra.INITIAL_URI", initialUri);
            folderPickerLauncher.launch(intent);

            Toast.makeText(this, "📂 Select 'Android/data/" + selectedGamePackage + "' folder!", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(this, "❌ Failed to open folder picker: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private boolean hasValidUriPermission(Uri uri) {
        try {
            getContentResolver().takePersistableUriPermission(uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            return true;
        } catch (SecurityException e) {
            return false;
        } catch (Exception e) {
            Toast.makeText(this, "Error checking URI permission: " + e.getMessage() + "\n" + WARNING_MESSAGE,
                    Toast.LENGTH_LONG).show();
            return false;
        }
    }

    private boolean isValidGameFolder(String folderPath, String selectedPackage) {
        return folderPath != null && folderPath.contains("Android/data/" + selectedPackage);
    }

    private Uri getSaveGamesFolderUri(Uri baseUri) {
        String baseDocumentId = DocumentsContract.getTreeDocumentId(baseUri);

        if (baseDocumentId.endsWith("files")) {
            baseDocumentId = baseDocumentId.replace("/files", "");
        }

        String saveGamesFolderId = baseDocumentId + "/files/UE4Game/ShadowTrackerExtra/ShadowTrackerExtra/Saved/SaveGames";
        return DocumentsContract.buildDocumentUriUsingTree(baseUri, saveGamesFolderId);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up
        pendingAssetFileName = null;
        pendingFpsType = null;
    }
}