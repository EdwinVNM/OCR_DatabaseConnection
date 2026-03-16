package com.example.ocr_databaseconnection;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
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
    private EditText etSuplier, etCustomerPo, etInvoiceDate, eInvoiceNo, etTotalAmount, etStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_invoice);

        // Initialize buttons
        btnInsertInvoice = findViewById(R.id.btnInsert);
        btnCrud = findViewById(R.id.btnBackToCrud);
        btnHome = findViewById(R.id.btnHome);

        // FIND EditText fields
        etSuplier = findViewById(R.id.etSuplier);
        etCustomerPo = findViewById(R.id.etCustomerPo);
        etInvoiceDate = findViewById(R.id.etInvoiceDate);
        eInvoiceNo = findViewById(R.id.etInvoiceNo);
        etTotalAmount = findViewById(R.id.etTotalAmount);
        etStatus = findViewById(R.id.etStatus);

        // RECEIVE values passed from CrudActionsActivity
        String supplier = getIntent().getStringExtra("supplier");
        String invoiceNumber = getIntent().getStringExtra("invoice_number");
        String customerPo = getIntent().getStringExtra("customer_po");
        String invoiceDate = getIntent().getStringExtra("invoice_date");
        String totalAmount = getIntent().getStringExtra("total_amount");
        String status = getIntent().getStringExtra("status");

        // SET extracted values in EditText fields (if null, set empty)
        etSuplier.setText(supplier != null ? supplier : "");
        eInvoiceNo.setText(invoiceNumber != null ? invoiceNumber : "");
        etCustomerPo.setText(customerPo != null ? customerPo : "");
        etInvoiceDate.setText(invoiceDate != null ? invoiceDate : "");
        etTotalAmount.setText(totalAmount != null ? totalAmount : "");
        etStatus.setText(status != null ? status : "");

        // Set ClickListener for Insert Invoice button
        btnInsertInvoice.setOnClickListener(v -> {
            // Get values from EditText
            String supplierValue = etSuplier.getText().toString();
            String invoiceNumberValue = eInvoiceNo.getText().toString();
            String customerPoValue = etCustomerPo.getText().toString();
            String invoiceDateValue = etInvoiceDate.getText().toString();
            String totalAmountValue = etTotalAmount.getText().toString();
            String statusValue = etStatus.getText().toString();

            // Call AsyncTask to perform background POST request to the servlet
            new InsertInvoiceTask().execute(supplierValue, invoiceNumberValue, customerPoValue, invoiceDateValue, totalAmountValue, statusValue);
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
            String supplier = params[0];
            String invoiceNumber = params[1];
            String customerPo = params[2];
            String invoiceDate = params[3];
            String totalAmount = params[4];
            String status = params[5];

            String result = "Error";

            try {
                // Create URL connection to servlet
                URL url = new URL("http://10.0.2.2:8080/OCRBackend/invoice");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                // Prepare the parameters to send to the servlet
                String postData = "supplier=" + URLEncoder.encode(supplier, "UTF-8") +
                        "&invoice_number=" + URLEncoder.encode(invoiceNumber, "UTF-8") +
                        "&customer_po=" + URLEncoder.encode(customerPo, "UTF-8") +
                        "&invoice_date=" + URLEncoder.encode(invoiceDate, "UTF-8") +
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
}
