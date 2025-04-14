package com.example.ocr_databaseconnection;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;
import android.Manifest;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class PdfActivity extends Activity {

    private static final int STORAGE_PERMISSION_CODE = 100;

    public PdfActivity() {
        // Default constructor (must be present)
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = new Intent(PdfActivity.this, MainActivity.class);
        startActivity(intent);

        // Request permissions if not already granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
        }

        // Continue with generating the PDF if permissions are granted
        String invoiceData = getIntent().getStringExtra("invoiceData");
        String invoiceId = getIntent().getStringExtra("invoiceId");

        if (invoiceData != null && invoiceId != null) {
            generatePdf(invoiceData, invoiceId);

        } else {
            Toast.makeText(this, "Error: Invoice data is missing", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, generate the PDF
                String invoiceData = getIntent().getStringExtra("invoiceData");
                String invoiceId = getIntent().getStringExtra("invoiceId");
                generatePdf(invoiceData, invoiceId);
            } else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void generatePdf(String invoiceData, String invoiceId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // For Android 10 (API 29) and above, use MediaStore for saving to root directory
            ContentValues contentValues = new ContentValues();
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, invoiceId + "_Invoice.pdf");
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf");
            contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, ""); // Save to the root directory (main screen)

            Uri uri = getContentResolver().insert(MediaStore.Files.getContentUri("external"), contentValues);

            if (uri != null) {
                // Log URI to see the location
                Log.d("PdfActivity", "File URI: " + uri.toString());

                try (OutputStream outputStream = getContentResolver().openOutputStream(uri)) {
                    if (outputStream != null) {
                        // Create the PDF content
                        PdfDocument pdfDocument = new PdfDocument();
                        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(400, 600, 1).create();
                        PdfDocument.Page page = pdfDocument.startPage(pageInfo);
                        page.getCanvas().drawText(invoiceData, 50, 50, new android.graphics.Paint());
                        pdfDocument.finishPage(page);
                        pdfDocument.writeTo(outputStream);
                        pdfDocument.close();
                        Toast.makeText(this, "PDF saved to the main screen (root directory)", Toast.LENGTH_LONG).show();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Error saving PDF", Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(this, "Failed to get URI for file", Toast.LENGTH_LONG).show();
            }
        } else {
            // For devices with API levels below 29, use legacy storage method
            File directory = Environment.getExternalStorageDirectory();  // Use root directory
            File file = new File(directory, invoiceId + "_Invoice.pdf");

            try (FileOutputStream fos = new FileOutputStream(file)) {
                PdfDocument pdfDocument = new PdfDocument();
                PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(400, 600, 1).create();
                PdfDocument.Page page = pdfDocument.startPage(pageInfo);
                page.getCanvas().drawText(invoiceData, 50, 50, new android.graphics.Paint());
                pdfDocument.finishPage(page);
                pdfDocument.writeTo(fos);
                pdfDocument.close();
                Toast.makeText(this, "PDF saved to the main screen (root directory)", Toast.LENGTH_LONG).show();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Error saving PDF", Toast.LENGTH_LONG).show();
            }
        }
    }
}
