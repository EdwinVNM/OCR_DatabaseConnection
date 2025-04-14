package com.example.ocr_databaseconnection;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.InputStreamReader;
import java.io.BufferedReader;

public class DeleteInvoiceActivity extends AppCompatActivity {

    private Button btnDelete, btnCrud, btnHome;
    private EditText edtInvoiceId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.delete_invoice);

        // Initialize buttons and EditText
        btnDelete = findViewById(R.id.btnDelete);
        btnCrud = findViewById(R.id.btnBackToCrudDelete);
        btnHome = findViewById(R.id.btnHome);
        edtInvoiceId = findViewById(R.id.etInvoiceIdDelete); // Make sure you have this EditText in the layout

        // Set click listener for Delete button
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmDelete();
            }
        });

        // Setting listeners for other buttons
        btnCrud.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openCrudScreen();
            }
        });

        btnHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goHome();
            }
        });
    }

    // ------------------ Confirm Deletion ------------------ //
    private void confirmDelete() {
        String invoiceId = edtInvoiceId.getText().toString().trim();

        if (invoiceId.isEmpty()) {
            Toast.makeText(DeleteInvoiceActivity.this, "Please enter an invoice ID", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show confirmation dialog
        new AlertDialog.Builder(this)
                .setTitle("Delete Invoice")
                .setMessage("Are you sure you want to delete this invoice?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Call the delete method if user clicks "Yes"
                        deleteInvoice(invoiceId);
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }

    // ------------------ Send DELETE Request to the Servlet ------------------ //
    private void deleteInvoice(String invoiceId) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // Create the URL with the invoiceId as query parameter
                    String ipAddress = IpAddressSingleton.getInstance().getIpAddress();
                    URL url = new URL(ipAddress + "/OCR_DatabaseConnection/invoice?id=" + invoiceId);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("DELETE");
                    connection.setDoOutput(true);
                    connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                    // Get the response code
                    int responseCode = connection.getResponseCode();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();
                    connection.disconnect();

                    // Check the response
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        // Successfully deleted, show Toast
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(DeleteInvoiceActivity.this, "Invoice deleted successfully", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        // Deletion failed, show error message
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(DeleteInvoiceActivity.this, "Failed to delete invoice", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(DeleteInvoiceActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        }).start();
    }

    // ------------------ Methods to redirect the user to chosen clicked action ------------------ //

    private void openCrudScreen() {
        Intent intent = new Intent(DeleteInvoiceActivity.this, CrudActionsActivity.class);
        startActivity(intent);
    }

    private void goHome() {
        Intent intent = new Intent(DeleteInvoiceActivity.this, MainActivity.class);
        startActivity(intent);
    }
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View view = getCurrentFocus();
            if (view instanceof EditText) {
                hideKeyboard();
            }
        }
        return super.dispatchTouchEvent(event);
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        View view = getCurrentFocus();
        if (view != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}
