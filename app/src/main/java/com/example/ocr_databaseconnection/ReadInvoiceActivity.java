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
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class ReadInvoiceActivity extends AppCompatActivity {

    private Button btnCrud, btnHome, btnReadInvoice, btnConvertToPdf;
    private TextView tvInvoiceDetails, invoiceTextView;
    private EditText etInvoiceId, etInvoiceIdRead;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.read_invoice);

        // Initialize buttons and TextView
        btnCrud = findViewById(R.id.btnBackToCrudRead);
        btnHome = findViewById(R.id.btnHome);
        btnReadInvoice = findViewById(R.id.btnRead);
        tvInvoiceDetails = findViewById(R.id.tvInvoiceDetails);
        etInvoiceId = findViewById(R.id.etInvoiceIdRead);
        etInvoiceIdRead = findViewById(R.id.etInvoiceIdRead);


        btnConvertToPdf = findViewById(R.id.btnConvertToPdf);
        invoiceTextView = findViewById(R.id.tvInvoiceDetails);

        // ------------------ Setting the ClickListener for Read Invoice button ------------------ //

        btnReadInvoice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String invoiceId = etInvoiceId.getText().toString();
                if (!invoiceId.isEmpty()) {
                    new ReadInvoiceTask().execute(invoiceId);
                } else {
                    Toast.makeText(ReadInvoiceActivity.this, "Please enter an Invoice ID", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // ------------------ Setting the ClickListener for Convert to PDF button ------------------ //

        btnConvertToPdf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String invoiceData = invoiceTextView.getText().toString();
                String invoiceId = etInvoiceIdRead.getText().toString(); // Get Invoice ID

                Intent intent = new Intent(ReadInvoiceActivity.this, PdfActivity.class);
                intent.putExtra("invoiceData", invoiceData);
                intent.putExtra("invoiceId", invoiceId);
                startActivity(intent);
            }
        });


        // ------------------ Setting the ClickListener to each button ------------------ //

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

    // ------------------ Methods to redirect the user to chosen clicked action ------------------ //

    private void openCrudScreen() {
        Intent intent = new Intent(ReadInvoiceActivity.this, CrudActionsActivity.class);
        startActivity(intent);
    }

    private void goHome() {
        Intent intent = new Intent(ReadInvoiceActivity.this, MainActivity.class);
        startActivity(intent);
    }

    // AsyncTask to handle GET request in background thread
    private class ReadInvoiceTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String invoiceId = params[0];
            String result = "Error";

            try {
                // Create URL connection to servlet
                String ipAddress = IpAddressSingleton.getInstance().getIpAddress();
                URL url = new URL(ipAddress + "/OCR_DatabaseConnection/invoice?id=" + invoiceId);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setDoInput(true);

                // Read response data
                InputStreamReader reader = new InputStreamReader(connection.getInputStream());
                StringBuilder response = new StringBuilder();
                int data = reader.read();
                while (data != -1) {
                    response.append((char) data);
                    data = reader.read();
                }

                connection.disconnect();

                // Log the raw response for debugging purposes
                Log.d("ReadInvoiceActivity", "Raw JSON Response: " + response.toString());

                // Check if the response contains data
                if (response.length() > 0) {
                    result = parseInvoiceDetails(response.toString());
                } else {
                    result = "Invoice Not Found.";
                }

            } catch (Exception e) {
                e.printStackTrace();
                result = "Error: " + e.getMessage();
            }
            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            // Show result to the user in the TextView
            tvInvoiceDetails.setText(result);
        }

        // Method to parse the JSON response and format the invoice details
        private String parseInvoiceDetails(String jsonResponse) {
            String formattedInvoiceDetails = "";

            try {
                // Parse the JSON response as an array
                JSONArray jsonArray = new JSONArray(jsonResponse);

                // Log the JSON array to verify its structure
                Log.d("ReadInvoiceActivity", "Parsed JSON Array: " + jsonArray.toString());

                // Check if the array is empty
                if (jsonArray.length() == 0) {
                    return "Invoice Not Found.";
                }

                // Extract the first invoice object from the array
                JSONObject invoiceObject = jsonArray.getJSONObject(0);

                // Extract the necessary fields
                String invoiceId = invoiceObject.optString("id", "Unknown").trim();
                String customerName = invoiceObject.optString("customer_name", "").trim();
                String invoiceDate = invoiceObject.optString("invoice_date", "").trim();
                String dueDate = invoiceObject.optString("due_date", "").trim();
                String totalAmount = invoiceObject.optString("total_amount", "").trim();
                String status = invoiceObject.optString("status", "").trim();

                // Clean the total_amount string if necessary (removing currency symbol, etc.)
                totalAmount = totalAmount.replace("EUR", "").trim(); // remove EUR if it's included as part of the value

                // Check if any field is missing or empty
                if (customerName.isEmpty() || invoiceDate.isEmpty() || dueDate.isEmpty() ||
                        totalAmount.isEmpty() || status.isEmpty()) {
                    return "Invoice Not Found.";
                }

                // Format the details as a readable string
                formattedInvoiceDetails = "Invoice ID: " + invoiceId + "\n" +
                        "Customer Name: " + customerName + "\n" +
                        "Invoice Date: " + invoiceDate + "\n" +
                        "Due Date: " + dueDate + "\n" +
                        "Total Amount: EUR " + totalAmount + "\n" +
                        "Status: " + status;

            } catch (Exception e) {
                e.printStackTrace();
                formattedInvoiceDetails = "Error: Could not parse invoice details.";
            }

            return formattedInvoiceDetails;
        }
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
