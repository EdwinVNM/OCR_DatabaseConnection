package com.example.ocr_databaseconnection;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.io.IOException;
import java.util.regex.Pattern;
import java.util.regex.Matcher;


public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_PICK = 2;
    private static final int REQUEST_FILE_PICK = 100;

    private ImageView selectedImage;
    private TextView extractedText;
    private Uri imageUri;

    @SuppressLint("QueryPermissionsNeeded")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // --------------------------------- Buttons Declaration --------------------------------- //

        Button btnCamera = findViewById(R.id.btnCamera);
        Button btnGallery = findViewById(R.id.btnGallery);
        Button btnCopyText = findViewById(R.id.btnCopyText);
        Button btnShare = findViewById(R.id.btnShare);
        Button btnCRUD = findViewById(R.id.btnCRUD);
        Button btnLogin = findViewById(R.id.btnLogin);

        selectedImage = findViewById(R.id.selectedImage);
        extractedText = findViewById(R.id.extractedText);

        // ----------------------- Start Buttons Actions For activity_main ----------------------- //

        // Take a picture with the phone camera
        btnCamera.setOnClickListener(v -> {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        });

        // Select an image or any file from the device
        btnGallery.setOnClickListener(v -> {
            // Create an Intent to get content (pick a file)
            Intent pickFile = new Intent(Intent.ACTION_GET_CONTENT);

            // Allow any type of file to be selected
            pickFile.setType("*/*");

            // Add the category to open the file
            pickFile.addCategory(Intent.CATEGORY_OPENABLE);

            // Start the activity for result to pick the file
            startActivityForResult(pickFile, REQUEST_FILE_PICK);
        });



        // Copy extracted text to clipboard
        btnCopyText.setOnClickListener(v -> {
            String text = extractedText.getText().toString();
            if (imageUri == null || text.equals("Extracted text will appear here, please wait few seconds...")) {
                Toast.makeText(this, "Please select an image and extract text first", Toast.LENGTH_SHORT).show();
            } else {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Extracted Text", text);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(this, "Text copied to clipboard", Toast.LENGTH_SHORT).show();
            }
        });

        // Share extracted text via available sharing options
        btnShare.setOnClickListener(v -> {
            String text = extractedText.getText().toString();
            if (imageUri == null || text.equals("Extracted text will appear here, please wait few seconds...")) {
                Toast.makeText(this, "Please select an image and extract text first", Toast.LENGTH_SHORT).show();
            } else {
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_TEXT, text);
                startActivity(Intent.createChooser(shareIntent, "Share via"));
            }
        });

        // Navigate to CRUD actions screen and takes the persistent data forward
        btnCRUD.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CrudActionsActivity.class);

            // Retrieve values from SharedPreferences
            SharedPreferences preferences = getSharedPreferences("InvoiceData", MODE_PRIVATE);
            String customerName = preferences.getString("customer_name", "Not found");
            String invoiceDate = preferences.getString("invoice_date", "Not found");
            String dueDate = preferences.getString("due_date", "Not found");
            String totalAmount = preferences.getString("total_amount", "Not found");
            String status = preferences.getString("status", "Not found");

            // Pass the data to CrudActionsActivity
            intent.putExtra("customer_name", customerName);
            intent.putExtra("invoice_date", invoiceDate);
            intent.putExtra("due_date", dueDate);
            intent.putExtra("total_amount", totalAmount);
            intent.putExtra("status", status);

            startActivity(intent);
        });

        // Navigate to Login page
        btnLogin.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);

        });
    }

    // ----------------------- End Buttons Actions For activity_main ----------------------- //

    // ----------- Display the selected image from gallery in the ImageView area ----------- //

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null) {

            if (requestCode == REQUEST_IMAGE_CAPTURE) {
                // Handle the camera image
                Bitmap imageBitmap = (Bitmap) data.getExtras().get("data");
                selectedImage.setImageBitmap(imageBitmap);
                extractTextFromBitmap(imageBitmap);
                imageUri = null; // No URI for captured images

            } else if (requestCode == REQUEST_IMAGE_PICK) {
                // Handle the selected image from gallery
                imageUri = data.getData();
                selectedImage.setImageURI(imageUri);
                extractTextFromUri(imageUri);

            } else if (requestCode == REQUEST_FILE_PICK) {
                // Handle the selected file from file manager (e.g., PDFs or other types of files)
                imageUri = data.getData();
                String fileType = getContentResolver().getType(imageUri);

                // If the file is an image, display it in the ImageView
                if (fileType != null && fileType.startsWith("image/")) {
                    selectedImage.setImageURI(imageUri);
                    extractTextFromUri(imageUri); // You can extract text from images (like OCR) if needed

                } else {
                    // Handle non-image files, for instance PDFs or other files
                    openFile(imageUri); // Open the file using the appropriate app (for PDFs, etc.)
                }
            }
        }
    }

    // Function to open files (non-images)
    private void openFile(Uri fileUri) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(fileUri, getContentResolver().getType(fileUri));
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(intent);
    }


// -------------------------------------- Start of OCR Logic ------------------------------------- //

    // Extract text from a Bitmap image
    private void extractTextFromBitmap(Bitmap bitmap) {
        TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
        InputImage image = InputImage.fromBitmap(bitmap, 0);

        recognizer.process(image)
                .addOnSuccessListener(text -> {
                    String extractedText = text.getText();
                    // Set the extracted text to the TextView
                    this.extractedText.setText(extractedText);

                    // Parse the extracted text to find specific details
                    parseExtractedText(extractedText);
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to extract text", Toast.LENGTH_SHORT).show());
    }

    private void extractTextFromUri(Uri uri) {
        try {
            InputImage image = InputImage.fromFilePath(this, uri);
            TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

            recognizer.process(image)
                    .addOnSuccessListener(text -> {
                        String extractedText = text.getText();
                        // Set the extracted text to the TextView
                        this.extractedText.setText(extractedText);

                        // Parse the extracted text to find specific details
                        parseExtractedText(extractedText);
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Failed to extract text", Toast.LENGTH_SHORT).show());
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error loading image", Toast.LENGTH_SHORT).show();
        }
    }

// --------------------------------------- End of OCR Logic -------------------------------------- //

// ----------------------------- Start Parse/Find Specific Words Logic --------------------------- //

    // Parse the extracted text to get specific information
    private void parseExtractedText(String extractedText) {
        // Extract values from the OCR text
        String customerName = extractValueAfterLabel(extractedText, "Customer Name:");
        String invoiceDate = extractValueAfterLabel(extractedText, "Invoice Date:");
        String dueDate = extractValueAfterLabel(extractedText, "Due Date:");
        String totalAmount = extractValueAfterLabel(extractedText, "Total Amount:");
        String status = extractValueAfterLabel(extractedText, "Status:");

        // Log extracted values (optional)
        Log.d("OCR Results", "Customer Name: " + customerName);
        Log.d("OCR Results", "Invoice Date: " + invoiceDate);
        Log.d("OCR Results", "Due Date: " + dueDate);
        Log.d("OCR Results", "Total Amount: " + totalAmount);
        Log.d("OCR Results", "Status: " + status);

        // Store the extracted values in SharedPreferences
        SharedPreferences preferences = getSharedPreferences("InvoiceData", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("customer_name", customerName);
        editor.putString("invoice_date", invoiceDate);
        editor.putString("due_date", dueDate);
        editor.putString("total_amount", totalAmount);
        editor.putString("status", status);
        editor.apply();  // Save the data
    }

    // Extract the value after a specific label in the text
    private String extractValueAfterLabel(String text, String label) {
        // Define a pattern to find the label and the characters after it, including symbols
        String regex = label + "\\s*[:\\-]?\\s*(.*)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(text);

        // If the pattern is found, return the value after the label
        if (matcher.find()) {
            String value = matcher.group(1).trim();  // Remove any leading/trailing spaces
            // Allow symbols such as the Euro symbol (€) in the value
            value = value.replaceAll("[^\\x00-\\x7F]", ""); // Remove any non-ASCII characters if needed
            return value;
        } else {
            return "Not found";  // Return a default value if the label is not found
        }
    }
// ------------------------------ End Parse/Find Specific Words Logic ---------------------------- //

}

