package ps.reso.instaeclipse.mods.devops.config;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

// Import the JSONObject class
import org.json.JSONObject;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import ps.reso.instaeclipse.utils.feature.FeatureFlags;

public class JsonImportActivity extends Activity {

    private static final int PICK_FILE = 1234;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FeatureFlags.isImportingConfig = false;
        openFilePicker();
    }

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        // Allow any file type initially, then specify MIME types
        intent.setType("*/*");
        // Define the MIME types we want to accept
        String[] mimeTypes = {"application/json", "application/octet-stream", "application/x-ibackup"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(Intent.createChooser(intent, "Select JSON or Instafel Config"), PICK_FILE);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICK_FILE) {
            if (resultCode == RESULT_OK && data != null) {
                Uri uri = data.getData();
                try (InputStream inputStream = getContentResolver().openInputStream(uri)) {
                    String fileContent = readStream(inputStream).trim();
                    String jsonToImport = fileContent; // Default to using the whole file content

                    // ==================== MODIFICATION START ====================
                    // Try to parse the file as a JSON Object to check for iBackup structure
                    try {
                        JSONObject rootObject = new JSONObject(fileContent);
                        // Check if it's an iBackup file by looking for the 'backup' and 'info' keys
                        if (rootObject.has("backup") && rootObject.has("info")) {
                            // It's an iBackup file, extract only the 'backup' part
                            jsonToImport = rootObject.getJSONObject("backup").toString();
                            Toast.makeText(this, "Instafel Config", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception jsonException) {
                        // Not a valid JSON object, or not an iBackup file.
                        // We will proceed assuming it's a plain JSON content file.
                    }
                    // ===================== MODIFICATION END =====================

                    // Validate the final JSON string before enabling the flag
                    if (jsonToImport.startsWith("{") && jsonToImport.endsWith("}")) {
                        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText("json", jsonToImport);
                        clipboard.setPrimaryClip(clip);

                        FeatureFlags.isImportingConfig = true; // <- only now turn it ON
                    } else {
                        FeatureFlags.isImportingConfig = false;
                        Toast.makeText(this, "❌ Not a valid JSON or Instafel Config", Toast.LENGTH_LONG).show();
                    }
                } catch (Exception e) {
                    FeatureFlags.isImportingConfig = false; // <- make sure we reset on error
                    Toast.makeText(this, "❌ Failed to read file: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            } else {
                // User pressed back / cancelled
                FeatureFlags.isImportingConfig = false; // <- ensure OFF on cancel
                Toast.makeText(this, "Cancelled or no file selected", Toast.LENGTH_SHORT).show();
            }
        }
        finish(); // Done, return to the previous screen
    }

    private String readStream(InputStream inputStream) {
        @SuppressLint({"NewApi", "LocalSuppress"}) Scanner scanner = new Scanner(inputStream, StandardCharsets.UTF_8).useDelimiter("\\A");
        return scanner.hasNext() ? scanner.next() : "";
    }
}
