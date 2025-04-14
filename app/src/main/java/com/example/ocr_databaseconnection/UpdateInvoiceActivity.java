package com.example.ocr_databaseconnection;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class UpdateInvoiceActivity extends AppCompatActivity {

    private Button btnUpdateInvoice, btnCrud, btnHome;
    private EditText etInvoiceIdUpdate, etUpdateCustomerName, etUpdateInvoiceDate, etUpdateDueDate, etUpdateTotalAmount, etUpdateStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.update_invoice);

        // Initialize buttons and fields
        btnUpdateInvoice = findViewById(R.id.btnUpdate);
        btnCrud = findViewById(R.id.btnBackToCrudUpdate);
        btnHome = findViewById(R.id.btnHome);

        etInvoiceIdUpdate = findViewById(R.id.etInvoiceIdUpdate);
        etUpdateCustomerName = findViewById(R.id.etUpdateCustomerName);
        etUpdateInvoiceDate = findViewById(R.id.etUpdateInvoiceDate);
        etUpdateDueDate = findViewById(R.id.etUpdateDueDate);
        etUpdateTotalAmount = findViewById(R.id.etUpdateTotalAmount);
        etUpdateStatus = findViewById(R.id.etUpdateStatus);

        // Setting click listeners for buttons
        btnUpdateInvoice.setOnClickListener(v -> {
            // Retrieve data entered in the fields
            String invoiceId = etInvoiceIdUpdate.getText().toString();
            String customerName = etUpdateCustomerName.getText().toString();
            String invoiceDate = etUpdateInvoiceDate.getText().toString();
            String dueDate = etUpdateDueDate.getText().toString();
            String totalAmount = etUpdateTotalAmount.getText().toString();
            String status = etUpdateStatus.getText().toString();

            // Ensure invoice ID is not empty
            if (invoiceId.isEmpty()) {
                Toast.makeText(UpdateInvoiceActivity.this, "Invoice ID is required", Toast.LENGTH_SHORT).show();
                return;
            }
            Log.d("UpdateInvoice", "invoiceId: " + invoiceId);
            Log.d("UpdateInvoice", "customerName: " + customerName);
            Log.d("UpdateInvoice", "invoiceDate: " + invoiceDate);
            Log.d("UpdateInvoice", "dueDate: " + dueDate);
            Log.d("UpdateInvoice", "totalAmount: " + totalAmount);
            Log.d("UpdateInvoice", "status: " + status);

            // Execute AsyncTask to send data to the server for updating
            new UpdateInvoiceTask().execute(invoiceId, customerName, invoiceDate, dueDate, totalAmount, status);
        });

        // Navigate to the CrudActionsActivity
        btnCrud.setOnClickListener(v -> openCrudScreen());

        // Navigate back to the main activity
        btnHome.setOnClickListener(v -> goHome());
    }

    // AsyncTask to handle POST request for updating invoice
    private class UpdateInvoiceTask extends AsyncTask<String, Void, String> {
        
        @Override
        protected String doInBackground(String... params) {
            String invoiceId = params[0];
            String customerName = params[1];
            String invoiceDate = params[2];
            String dueDate = params[3];
            String totalAmount = params[4];
            String status = params[5];

            String result = "Error";

            try {

                Log.d("UpdateInvoiceTask", "customerName: " + customerName);
                Log.d("UpdateInvoiceTask", "invoiceDate: " + invoiceDate);
                Log.d("UpdateInvoiceTask", "dueDate: " + dueDate);
                Log.d("UpdateInvoiceTask", "totalAmount: " + totalAmount);
                Log.d("UpdateInvoiceTask", "status: " + status);


                // Create URL with invoiceId as a query parameter
                String ipAddress = IpAddressSingleton.getInstance().getIpAddress();
                URL url = new URL(ipAddress + "/OCR_DatabaseConnection/invoice?id=" + URLEncoder.encode(invoiceId, "UTF-8"));
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("PUT");
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                // Build parameters dynamically (only non-empty fields)
                StringBuilder putData = new StringBuilder();

                if (!customerName.isEmpty()) {
                    putData.append("customer_name=").append(URLEncoder.encode(customerName, "UTF-8")).append("&");
                    Log.d("PLUS", "Updating With Plus: " + putData);
                    Log.d("PLUSCustomer", "Updating With PlusCustomer: " + customerName);


                }
                if (!invoiceDate.isEmpty()) {
                    putData.append("invoice_date=").append(URLEncoder.encode(invoiceDate, "UTF-8")).append("&");
                }
                if (!dueDate.isEmpty()) {
                    putData.append("due_date=").append(URLEncoder.encode(dueDate, "UTF-8")).append("&");
                }
                if (!totalAmount.isEmpty()) {
                    putData.append("total_amount=").append(URLEncoder.encode(totalAmount, "UTF-8")).append("&");
                }
                if (!status.isEmpty()) {
                    putData.append("status=").append(URLEncoder.encode(status, "UTF-8")).append("&");
                }

                // Remove the last '&' if necessary
                if (putData.length() > 0) {
                    putData.setLength(putData.length() - 1);
                }

                // Log the putData for debugging
                Log.d("UpdateInvoiceTask", "putData: " + putData.toString());


                // Send the request data if there's any
                if (putData.length() > 0) {
                    OutputStream os = connection.getOutputStream();
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                    writer.write(putData.toString());
                    writer.flush();
                    writer.close();
                    os.close();
                }

                // Log the putData for debugging
                Log.d("UpdateInvoiceTask", "putData: " + putData.toString());


                // Check if the request was successful
                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    result = "Invoice updated successfully!";
                } else {
                    result = "Failed to update invoice. Response code: " + responseCode;
                }

                connection.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
                result = "Error: " + e.getMessage();
            }

            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            Toast.makeText(getApplicationContext(), result, Toast.LENGTH_LONG).show();
        }
    }


    // Navigate to the CrudActionsActivity
    private void openCrudScreen() {
        Intent intent = new Intent(UpdateInvoiceActivity.this, CrudActionsActivity.class);
        startActivity(intent);
    }

    // Navigate back to the main activity
    private void goHome() {
        Intent intent = new Intent(UpdateInvoiceActivity.this, MainActivity.class);
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
