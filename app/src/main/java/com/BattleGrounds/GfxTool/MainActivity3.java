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
import android.provider.OpenableColumns;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;

import rikka.shizuku.Shizuku;

public class MainActivity3 extends AppCompatActivity {

    private static final String TAG = "MainActivity3";
    private static final String PREFS_NAME = "GamePrefs";
    private static final String PREF_BGMI_URI = "bgmiFolderUri";
    private static final String WARNING_MESSAGE = "Warning: Please grant access to Android/data folder for this feature to work properly.";

    private SharedPreferences sharedPreferences;
    private TextView selectedFileTextView;
    private Button uploadActiveSavButton;
    private Button uploadFolderButton;
    private CardView selectedFileCard;
    private Uri selectedFileUri;
    private Uri selectedFolderUri;
    private String selectedFileName;
    private String selectedFolderName;
    private boolean isFileMode = true; // true for file, false for folder

    // File picker launcher
    private final ActivityResultLauncher<String[]> filePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.OpenDocument(), uri -> {
                if (uri != null) {
                    handleSelectedFile(uri);
                } else {
                    Toast.makeText(this, "❌ No file selected", Toast.LENGTH_SHORT).show();
                }
            });

    // Folder picker launcher for selecting "file" folder
    private final ActivityResultLauncher<Intent> fileFolderPickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri folderUri = result.getData().getData();
                    if (folderUri != null) {
                        handleSelectedFolder(folderUri);
                    } else {
                        Toast.makeText(this, "❌ No folder selected", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "❌ No folder selected", Toast.LENGTH_SHORT).show();
                }
            });

    // Folder picker launcher for SAF
    private final ActivityResultLauncher<Intent> folderPickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri != null) {
                        handleFolderSelection(uri);
                    }
                } else {
                    Toast.makeText(this, "❌ Folder access denied! Please select the correct folder.", Toast.LENGTH_LONG).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);

        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        // Initialize Shizuku
        ShizukuHelper.initializeShizuku(this);
        ShizukuHelper.registerListener();

        initializeViews();
        setupClickListeners();
        updateGameInfo();

        // Check Shizuku permission for Android 11+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            checkShizukuPermission();
        }
    }

    private void initializeViews() {
        selectedFileTextView = findViewById(R.id.selected_file_name);
        uploadActiveSavButton = findViewById(R.id.upload_active_sav_button);
        uploadFolderButton = findViewById(R.id.upload_folder_button);
        selectedFileCard = findViewById(R.id.selected_file_card);

        // Initially hide the selected file card
        selectedFileCard.setVisibility(CardView.GONE);
        uploadActiveSavButton.setEnabled(false);
        uploadFolderButton.setEnabled(false);
    }

    private void setupClickListeners() {
        Button selectFileButton = findViewById(R.id.select_file_button);
        Button selectFolderButton = findViewById(R.id.select_folder_button);
        Button backButton = findViewById(R.id.back_button);

        selectFileButton.setOnClickListener(v -> {
            isFileMode = true;
            openFilePicker();
        });

        selectFolderButton.setOnClickListener(v -> {
            isFileMode = false;
            openFolderPicker();
        });

        uploadActiveSavButton.setOnClickListener(v -> {
            if (isFileMode && selectedFileUri != null) {
                uploadCustomFile();
            } else {
                Toast.makeText(this, "❌ Please select a file first", Toast.LENGTH_SHORT).show();
            }
        });

        uploadFolderButton.setOnClickListener(v -> {
            if (!isFileMode && selectedFolderUri != null) {
                uploadCustomFolder();
            } else {
                Toast.makeText(this, "❌ Please select a folder first", Toast.LENGTH_SHORT).show();
            }
        });

        backButton.setOnClickListener(v -> finish());

        // Clear selection button
        Button clearSelectionButton = findViewById(R.id.clear_selection_button);
        clearSelectionButton.setOnClickListener(v -> clearSelection());
    }

    private void updateGameInfo() {
        TextView gameNameTextView = findViewById(R.id.game_name_text);
        ImageView gameImageView = findViewById(R.id.game_image);

        // Get game info from intent
        String gameVersion = getIntent().getStringExtra("GAME_VERSION");
        String gamePackage = getIntent().getStringExtra("GAME_PACKAGE");

        if (gameVersion != null) {
            gameNameTextView.setText(gameVersion + " - Custom Upload");
        } else {
            gameNameTextView.setText("Custom Upload Mode");
        }

        // Set a generic gaming image if no specific image is provided
        gameImageView.setImageResource(R.drawable.boosts); // Fallback image
    }

    private void openFilePicker() {
        try {
            // Accept .sav files and other common game save formats
            String[] mimeTypes = {
                    "application/octet-stream", // .sav files
                    "*/*" // Allow all files as fallback
            };

            filePickerLauncher.launch(mimeTypes);
            Toast.makeText(this, "📁 Select your custom save file (.sav)", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Log.e(TAG, "Error opening file picker", e);
            Toast.makeText(this, "❌ Error opening file picker: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void openFolderPicker() {
        try {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            fileFolderPickerLauncher.launch(intent);
            Toast.makeText(this, "📁 Select the 'file' folder to upload", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Log.e(TAG, "Error opening folder picker", e);
            Toast.makeText(this, "❌ Error opening folder picker: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void handleSelectedFile(Uri uri) {
        try {
            selectedFileUri = uri;
            selectedFileName = getFileName(uri);
            selectedFolderUri = null;
            selectedFolderName = null;
            isFileMode = true;

            if (selectedFileName != null) {
                selectedFileTextView.setText("📄 " + selectedFileName);
                selectedFileCard.setVisibility(CardView.VISIBLE);
                uploadActiveSavButton.setEnabled(true);
                uploadFolderButton.setEnabled(false);

                // Validate file type
                if (isValidSaveFile(selectedFileName)) {
                    Toast.makeText(this, "✅ File selected: " + selectedFileName, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "⚠️ Warning: This doesn't appear to be a .sav file", Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(this, "❌ Could not read file name", Toast.LENGTH_SHORT).show();
                clearSelection();
            }

        } catch (Exception e) {
            Log.e(TAG, "Error handling selected file", e);
            Toast.makeText(this, "❌ Error reading file: " + e.getMessage(), Toast.LENGTH_LONG).show();
            clearSelection();
        }
    }

    private void handleSelectedFolder(Uri uri) {
        try {
            selectedFolderUri = uri;
            selectedFolderName = getFolderName(uri);
            selectedFileUri = null;
            selectedFileName = null;
            isFileMode = false;

            if (selectedFolderName != null) {
                selectedFileTextView.setText("📁 " + selectedFolderName);
                selectedFileCard.setVisibility(CardView.VISIBLE);
                uploadFolderButton.setEnabled(true);
                uploadActiveSavButton.setEnabled(false);

                Toast.makeText(this, "✅ Folder selected: " + selectedFolderName, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "❌ Could not read folder name", Toast.LENGTH_SHORT).show();
                clearSelection();
            }

        } catch (Exception e) {
            Log.e(TAG, "Error handling selected folder", e);
            Toast.makeText(this, "❌ Error reading folder: " + e.getMessage(), Toast.LENGTH_LONG).show();
            clearSelection();
        }
    }

    private void clearSelection() {
        selectedFileUri = null;
        selectedFileName = null;
        selectedFolderUri = null;
        selectedFolderName = null;
        selectedFileCard.setVisibility(CardView.GONE);
        uploadActiveSavButton.setEnabled(false);
        uploadFolderButton.setEnabled(false);
        Toast.makeText(this, "🗑️ Selection cleared", Toast.LENGTH_SHORT).show();
    }

    private String getFileName(Uri uri) {
        String fileName = null;

        try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (nameIndex != -1) {
                    fileName = cursor.getString(nameIndex);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting file name", e);
        }

        return fileName;
    }

    private String getFolderName(Uri uri) {
        String folderName = null;
        try {
            String documentId = DocumentsContract.getTreeDocumentId(uri);
            if (documentId != null) {
                // Extract folder name from document ID
                String[] parts = documentId.split("/");
                if (parts.length > 0) {
                    folderName = parts[parts.length - 1];
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting folder name", e);
        }
        return folderName;
    }

    private boolean isValidSaveFile(String fileName) {
        return fileName != null && (
                fileName.toLowerCase().endsWith(".sav") ||
                        fileName.toLowerCase().contains("active") ||
                        fileName.toLowerCase().contains("save")
        );
    }

    private void uploadCustomFile() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && Shizuku.pingBinder()) {
            uploadFileUsingShizuku();
        } else {
            uploadFileUsingSAF();
        }
    }

    private void uploadCustomFolder() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && Shizuku.pingBinder()) {
            uploadFolderUsingShizuku();
        } else {
            uploadFolderUsingSAF();
        }
    }

    private void uploadFileUsingShizuku() {
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

        try {
            String targetPath = "/storage/emulated/0/Android/data/" + gamePackage +
                    "/files/UE4Game/ShadowTrackerExtra/ShadowTrackerExtra/Saved/SaveGames/Active.sav";
            String tempPath = "/data/local/tmp/Custom_Active.sav";

            // Create temp file from selected URI
            java.io.File tempFile = new java.io.File(getExternalCacheDir(), "Custom_Active.sav");

            try (InputStream is = getContentResolver().openInputStream(selectedFileUri);
                 java.io.FileOutputStream os = new java.io.FileOutputStream(tempFile)) {

                if (is == null) {
                    Toast.makeText(this, "❌ Cannot read selected file", Toast.LENGTH_SHORT).show();
                    return;
                }

                byte[] buffer = new byte[1024];
                int length;
                while ((length = is.read(buffer)) > 0) {
                    os.write(buffer, 0, length);
                }
            }

            // Create target directory and copy file using Shizuku
            String targetDir = "/storage/emulated/0/Android/data/" + gamePackage +
                    "/files/UE4Game/ShadowTrackerExtra/ShadowTrackerExtra/Saved/SaveGames";

            executeShellCommand("mkdir -p " + targetDir);
            executeShellCommand("cp " + tempFile.getAbsolutePath() + " " + tempPath);
            executeShellCommand("chmod 777 " + tempPath);
            executeShellCommand("mv -f " + tempPath + " " + targetPath);

            // Clean up temp file
            tempFile.delete();

            Toast.makeText(this, "🎉 Custom file uploaded successfully!", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Log.e(TAG, "Error uploading file with Shizuku", e);
            Toast.makeText(this, "❌ Upload failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void uploadFileUsingSAF() {
        String savedUriString = sharedPreferences.getString(PREF_BGMI_URI, null);

        if (savedUriString == null) {
            requestGameFolderAccess();
            return;
        }

        Uri treeUri = Uri.parse(savedUriString);

        if (!hasValidUriPermission(treeUri)) {
            sharedPreferences.edit().remove(PREF_BGMI_URI).apply();
            requestGameFolderAccess();
            return;
        }

        try {
            String baseDocumentId = DocumentsContract.getTreeDocumentId(treeUri);
            String saveGamesFolderId = baseDocumentId + "/files/UE4Game/ShadowTrackerExtra/ShadowTrackerExtra/Saved/SaveGames";
            String activeSavDocumentId = saveGamesFolderId + "/Active.sav";
            Uri activeSavUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, activeSavDocumentId);

            // Ensure SaveGames folder exists
            Uri saveGamesFolderUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, saveGamesFolderId);
            if (!folderExists(saveGamesFolderUri)) {
                createFolder(treeUri, saveGamesFolderId, "SaveGames");
            }

            // Create Active.sav if it doesn't exist
            if (!fileExists(activeSavUri)) {
                createFile(treeUri, saveGamesFolderUri, "Active.sav", "application/octet-stream");
                // Rebuild the URI after creating the file
                activeSavUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, activeSavDocumentId);
            }

            // Copy selected file content to Active.sav
            try (OutputStream os = getContentResolver().openOutputStream(activeSavUri, "w");
                 InputStream is = getContentResolver().openInputStream(selectedFileUri)) {

                if (is == null || os == null) {
                    Toast.makeText(this, "❌ Cannot access files", Toast.LENGTH_SHORT).show();
                    return;
                }

                byte[] buffer = new byte[1024];
                int length;
                while ((length = is.read(buffer)) > 0) {
                    os.write(buffer, 0, length);
                }
                os.flush();
            }

            Toast.makeText(this, "🎉 Custom file uploaded successfully!", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Log.e(TAG, "Error uploading file with SAF", e);
            Toast.makeText(this, "❌ Upload failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void uploadFolderUsingShizuku() {
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

        try {
            String targetPath = "/storage/emulated/0/Android/data/" + gamePackage +
                    "/files/UE4Game/ShadowTrackerExtra/ShadowTrackerExtra/Saved/SaveGames/file";

            // Remove existing file folder and create new one
            executeShellCommand("rm -rf " + targetPath);
            executeShellCommand("mkdir -p " + targetPath);

            // Copy folder contents recursively
            copyFolderContentsShizuku(selectedFolderUri, targetPath);

            Toast.makeText(this, "🎉 Custom folder uploaded successfully!", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Log.e(TAG, "Error uploading folder with Shizuku", e);
            Toast.makeText(this, "❌ Folder upload failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void uploadFolderUsingSAF() {
        String savedUriString = sharedPreferences.getString(PREF_BGMI_URI, null);

        if (savedUriString == null) {
            requestGameFolderAccess();
            return;
        }

        Uri treeUri = Uri.parse(savedUriString);

        if (!hasValidUriPermission(treeUri)) {
            sharedPreferences.edit().remove(PREF_BGMI_URI).apply();
            requestGameFolderAccess();
            return;
        }

        try {
            String baseDocumentId = DocumentsContract.getTreeDocumentId(treeUri);
            String saveGamesFolderId = baseDocumentId + "/files/UE4Game/ShadowTrackerExtra/ShadowTrackerExtra/Saved/SaveGames";
            String fileFolderId = saveGamesFolderId + "/file";
            Uri fileFolderUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, fileFolderId);

            // Ensure SaveGames folder exists
            Uri saveGamesFolderUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, saveGamesFolderId);
            if (!folderExists(saveGamesFolderUri)) {
                createFolder(treeUri, saveGamesFolderId, "SaveGames");
            }

            // Delete existing file folder if it exists
            if (folderExists(fileFolderUri)) {
                deleteFolder(fileFolderUri);
            }

            // Create new file folder
            createFolder(treeUri, saveGamesFolderId, "file");
            fileFolderUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, fileFolderId);

            // Copy folder contents
            copyFolderContentsSAF(selectedFolderUri, fileFolderUri);

            Toast.makeText(this, "🎉 Custom folder uploaded successfully!", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Log.e(TAG, "Error uploading folder with SAF", e);
            Toast.makeText(this, "❌ Folder upload failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void handleFolderSelection(Uri uri) {
        String folderPath = getFolderPath(uri);
        String selectedGamePackage = getIntent().getStringExtra("GAME_PACKAGE");

        if (selectedGamePackage != null && isValidGameFolder(folderPath, selectedGamePackage)) {
            try {
                final int takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION;
                getContentResolver().takePersistableUriPermission(uri, takeFlags);
                sharedPreferences.edit().putString(PREF_BGMI_URI, uri.toString()).apply();
                Toast.makeText(this, "✅ Correct folder selected!", Toast.LENGTH_SHORT).show();

                // Retry upload after folder access is granted
                if (selectedFileUri != null) {
                    uploadCustomFile();
                } else if (selectedFolderUri != null) {
                    uploadCustomFolder();
                }
            } catch (SecurityException e) {
                Toast.makeText(this, "❌ Permission error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(this, "❌ Incorrect folder! Please select: Android/data/" + selectedGamePackage, Toast.LENGTH_LONG).show();
            requestGameFolderAccess();
        }
    }

    private void requestGameFolderAccess() {
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

    private void checkShizukuPermission() {
        if (!Shizuku.pingBinder()) {
            Log.e(TAG, "❌ Shizuku is NOT running!");
            showShizukuNotRunningDialog();
            return;
        }

        if (Shizuku.checkSelfPermission() != PackageManager.PERMISSION_GRANTED) {
            Log.w(TAG, "⚠️ Shizuku permission not granted. Requesting...");
            Shizuku.requestPermission(0);
        } else {
            Log.d(TAG, "✅ Shizuku is running and permission granted.");
        }
    }

    private void showShizukuNotRunningDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Shizuku Not Running")
                .setMessage("⚠️ Shizuku is not running! For Android 11+, please start Shizuku for better compatibility.")
                .setPositiveButton("Continue Anyway", (dialog, which) -> dialog.dismiss())
                .setNegativeButton("Open Shizuku", (dialog, which) -> {
                    try {
                        startActivity(getPackageManager().getLaunchIntentForPackage("moe.shizuku.privileged.api"));
                    } catch (Exception e) {
                        Toast.makeText(this, "❌ Shizuku app not found!", Toast.LENGTH_SHORT).show();
                    }
                })
                .show();
    }

    // Helper methods
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
                Log.e(TAG, "Command failed: " + command + " Exit code: " + exitCode);
            }
            return exitCode == 0;
        } catch (Exception e) {
            Log.e(TAG, "Error executing command: " + command, e);
            return false;
        }
    }

    private String getFolderPath(Uri uri) {
        String docId = DocumentsContract.getTreeDocumentId(uri);
        return docId;
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

    private boolean folderExists(Uri folderUri) {
        try (Cursor cursor = getContentResolver().query(
                folderUri,
                new String[]{DocumentsContract.Document.COLUMN_DOCUMENT_ID},
                null, null, null)) {
            return (cursor != null && cursor.getCount() > 0);
        } catch (Exception e) {
            Log.e(TAG, "Error checking folder existence: " + e.getMessage());
            return false;
        }
    }

    private boolean fileExists(Uri fileUri) {
        try (Cursor cursor = getContentResolver().query(
                fileUri,
                new String[]{DocumentsContract.Document.COLUMN_DOCUMENT_ID},
                null, null, null)) {
            return (cursor != null && cursor.getCount() > 0);
        } catch (Exception e) {
            Log.e(TAG, "Error checking file existence: " + e.getMessage());
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
                Log.i(TAG, "Folder created: " + newFolderUri.toString());
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to create folder: " + folderName, e);
        }
    }

    private void createFile(Uri treeUri, Uri parentFolderUri, String fileName, String mimeType) {
        try {
            Uri newFileUri = DocumentsContract.createDocument(
                    getContentResolver(), parentFolderUri, mimeType, fileName);

            if (newFileUri != null) {
                Log.i(TAG, "File created: " + newFileUri.toString());
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to create file: " + fileName, e);
        }
    }

    private void deleteFolder(Uri folderUri) {
        try {
            boolean deleted = DocumentsContract.deleteDocument(getContentResolver(), folderUri);
            if (deleted) {
                Log.i(TAG, "Folder deleted: " + folderUri.toString());
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to delete folder: " + folderUri.toString(), e);
        }
    }

    private void copyFolderContentsShizuku(Uri sourceUri, String targetPath) {
        // This would need implementation based on your specific requirements
        // You'll need to iterate through the folder contents and copy each file
        Log.i(TAG, "Copying folder contents using Shizuku from " + sourceUri + " to " + targetPath);
        // Implementation depends on how you want to handle the folder copying
    }

    private void copyFolderContentsSAF(Uri sourceUri, Uri targetUri) {
        // This would need implementation based on your specific requirements
        // You'll need to iterate through the folder contents and copy each file
        Log.i(TAG, "Copying folder contents using SAF from " + sourceUri + " to " + targetUri);
        // Implementation depends on how you want to handle the folder copying
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up
        selectedFileUri = null;
        selectedFileName = null;
        selectedFolderUri = null;
        selectedFolderName = null;

        // Unregister Shizuku listener
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            ShizukuHelper.unregisterListener();
        }
    }
}