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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class CreateInvoiceActivity extends AppCompatActivity {

    private Button btnInsertInvoice, btnCrud, btnHome;
    private EditText etCustomerName, etInvoiceDate, etDueDate, etTotalAmount, etStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_invoice);

        // Initialize buttons
        btnInsertInvoice = findViewById(R.id.btnInsert);
        btnCrud = findViewById(R.id.btnBackToCrud);
        btnHome = findViewById(R.id.btnHome);

        // FIND EditText fields
        etCustomerName = findViewById(R.id.etCustomerName);
        etInvoiceDate = findViewById(R.id.etInvoiceDate);
        etDueDate = findViewById(R.id.etDueDate);
        etTotalAmount = findViewById(R.id.etTotalAmount);
        etStatus = findViewById(R.id.etStatus);

        // RECEIVE values passed from CrudActionsActivity
        String customerName = getIntent().getStringExtra("customer_name");
        String invoiceDate = getIntent().getStringExtra("invoice_date");
        String dueDate = getIntent().getStringExtra("due_date");
        String totalAmount = getIntent().getStringExtra("total_amount");
        String status = getIntent().getStringExtra("status");

        Log.d("Fields", customerName);
        Log.d("Fields", invoiceDate);
        Log.d("Fields", dueDate);
        Log.d("Fields", totalAmount);
        Log.d("Fields", status);


        // SET extracted values in EditText fields (if null, set empty)
        etCustomerName.setText(customerName != null ? customerName : "");
        etInvoiceDate.setText(invoiceDate != null ? invoiceDate : "");
        etDueDate.setText(dueDate != null ? dueDate : "");
        etTotalAmount.setText(totalAmount != null ? totalAmount : "");
        etStatus.setText(status != null ? status : "");

        // Set ClickListener for Insert Invoice button
        btnInsertInvoice.setOnClickListener(v -> {
            // Get values from EditText
            String customerNameValue = etCustomerName.getText().toString();
            String invoiceDateValue = etInvoiceDate.getText().toString();
            String dueDateValue = etDueDate.getText().toString();
            String totalAmountValue = etTotalAmount.getText().toString();
            String statusValue = etStatus.getText().toString();

            // Call AsyncTask to perform background POST request to the servlet
            new InsertInvoiceTask().execute(customerNameValue, invoiceDateValue, dueDateValue, totalAmountValue, statusValue);
        });

        // Navigate to the CrudActionsActivity
        btnCrud.setOnClickListener(v -> openCrudScreen());

        // Navigate back to the main activity
        btnHome.setOnClickListener(v -> goHome());
    }

    // AsyncTask to handle POST request in background thread
    private class InsertInvoiceTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String customerName = params[0];
            String invoiceDate = params[1];
            String dueDate = params[2];
            String totalAmount = params[3];
            String status = params[4];

            String result = "Error";

            try {
                // Create URL connection to servlet
                String ipAddress = IpAddressSingleton.getInstance().getIpAddress();
                URL url = new URL(ipAddress + "/OCR_DatabaseConnection/invoice");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                // Prepare the parameters to send to the servlet
                String postData = "customer_name=" + URLEncoder.encode(customerName, "UTF-8") +
                        "&invoice_date=" + URLEncoder.encode(invoiceDate, "UTF-8") +
                        "&due_date=" + URLEncoder.encode(dueDate, "UTF-8") +
                        "&total_amount=" + URLEncoder.encode(totalAmount, "UTF-8") +
                        "&status=" + URLEncoder.encode(status, "UTF-8");

                // Send the request data
                OutputStream os = connection.getOutputStream();
                os.write(postData.getBytes());
                os.flush();
                os.close();

                // Get response from server
                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();
                    result = response.toString(); // Server's response
                } else {
                    result = "Failed to insert invoice. Server response code: " + responseCode;
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
            // Show result to the user
            Toast.makeText(CreateInvoiceActivity.this, result, Toast.LENGTH_LONG).show();
            Log.d("InsertInvoiceTask", result);
        }
    }

    // Navigate to the CrudActionsActivity
    private void openCrudScreen() {
        Intent intent = new Intent(CreateInvoiceActivity.this, CrudActionsActivity.class);
        startActivity(intent);
    }

    // Navigate back to the main activity
    private void goHome() {
        Intent intent = new Intent(CreateInvoiceActivity.this, MainActivity.class);
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
