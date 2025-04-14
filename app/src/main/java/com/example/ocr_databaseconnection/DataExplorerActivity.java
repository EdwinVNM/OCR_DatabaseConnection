package com.example.ocr_databaseconnection;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.os.AsyncTask;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class DataExplorerActivity extends AppCompatActivity {

    Button btnDataExplorerToDashboard, btnExploreData;
    private TextView exploredText;
    private EditText etSearchByCustomerName, etSearchByInvoiceDate, etSearchByInvoiceDueDate, etSearchByTotalAmount, etSearchByStatus;
    private EditText etEuroTotal, etInvoiceTotal;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.data_explorer);

        btnDataExplorerToDashboard = findViewById(R.id.btnDataExplorerToDashboard);
        btnExploreData = findViewById(R.id.btnExploreData);

        etSearchByCustomerName = findViewById(R.id.etSearchByCustomerName);
        etSearchByInvoiceDate = findViewById(R.id.etSearchByInvoiceDate);
        etSearchByInvoiceDueDate = findViewById(R.id.etSearchByInvoiceDueDate);
        etSearchByTotalAmount = findViewById(R.id.etSearchByTotalAmount);
        etSearchByStatus = findViewById(R.id.etSearchByStatus);
        exploredText = findViewById(R.id.exploredText);
        etEuroTotal = findViewById(R.id.etEuroTotal);
        etInvoiceTotal = findViewById(R.id.etInvoiceTotal);

        // Call setUpFieldFocusListener for each EditText
        setUpFieldFocusListener(etSearchByCustomerName);
        setUpFieldFocusListener(etSearchByInvoiceDate);
        setUpFieldFocusListener(etSearchByInvoiceDueDate);
        setUpFieldFocusListener(etSearchByTotalAmount);
        setUpFieldFocusListener(etSearchByStatus);

        btnDataExplorerToDashboard.setOnClickListener(v -> goBackToDashboard());

        btnExploreData.setOnClickListener(v -> {
            String customerName = etSearchByCustomerName.getText().toString().trim();
            String invoiceDate = etSearchByInvoiceDate.getText().toString().trim();
            String invoiceDueDate = etSearchByInvoiceDueDate.getText().toString().trim();
            String totalAmount = etSearchByTotalAmount.getText().toString().trim();
            String status = etSearchByStatus.getText().toString().trim();

            if (!customerName.isEmpty()) {
                new ExploreData().execute("customer_name", customerName);
            } else if (!invoiceDate.isEmpty()) {
                new ExploreData().execute("invoice_date", invoiceDate);
            } else if (!invoiceDueDate.isEmpty()) {
                new ExploreData().execute("due_date", invoiceDueDate);
            } else if (!totalAmount.isEmpty()) {
                new ExploreData().execute("total_amount", totalAmount);
            } else if (!status.isEmpty()) {
                new ExploreData().execute("status", status);
            } else {
                Toast.makeText(DataExplorerActivity.this, "Please enter a value in one of the search fields", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Set up the focus listener for each EditText
    private void setUpFieldFocusListener(EditText editText) {
        editText.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                // Clear all other fields when one is selected
                clearOtherFields(editText);

                // Enable the clicked field and make it ready for typing
                editText.setEnabled(true);
                editText.setText(""); // Clear the content of the clicked field to prompt user for input

                // Make sure the clicked field gets focus and set the cursor to the start
                editText.post(() -> {
                    editText.requestFocus();
                    editText.setSelection(0); // Ensure the cursor is placed at the start
                });
            }
        });
    }

    // Clear all other fields, keeping the selected one active
    private void clearOtherFields(EditText selectedField) {
        // Clear all fields except the selected one
        if (selectedField != etSearchByCustomerName) etSearchByCustomerName.setText("");
        if (selectedField != etSearchByInvoiceDate) etSearchByInvoiceDate.setText("");
        if (selectedField != etSearchByInvoiceDueDate) etSearchByInvoiceDueDate.setText("");
        if (selectedField != etSearchByTotalAmount) etSearchByTotalAmount.setText("");
        if (selectedField != etSearchByStatus) etSearchByStatus.setText("");
    }

    private void goBackToDashboard() {
        Intent intent = new Intent(DataExplorerActivity.this, DashboardActivity.class);
        startActivity(intent);
    }


    private class ExploreData extends AsyncTask<String, Void, String> {
        double totalAmountSum = 0;
        int invoiceCount = 0;

        @Override
        protected String doInBackground(String... params) {
            String fieldName = params[0];
            String fieldValue = params[1];
            String result = "Error";

            try {
                String ipAddress = IpAddressSingleton.getInstance().getIpAddress();
                URL url = new URL(ipAddress + "/OCR_DatabaseConnection/explore?field=" + fieldName + "&value=" + fieldValue);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setDoInput(true);

                InputStreamReader reader = new InputStreamReader(connection.getInputStream());
                StringBuilder response = new StringBuilder();
                int data = reader.read();
                while (data != -1) {
                    response.append((char) data);
                    data = reader.read();
                }

                connection.disconnect();
                Log.d("ExploreData", "Raw JSON Response: " + response.toString());

                result = parseInvoiceDetails(response.toString());
            } catch (Exception e) {
                e.printStackTrace();
                result = "Error: " + e.getMessage();
            }
            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            exploredText.setText(result);
            etEuroTotal.setText(String.format("%.2f", totalAmountSum));
            etInvoiceTotal.setText(String.valueOf(invoiceCount));
        }

        private String parseInvoiceDetails(String jsonResponse) {
            StringBuilder formattedInvoices = new StringBuilder();
            try {
                JSONArray jsonArray = new JSONArray(jsonResponse);
                invoiceCount = jsonArray.length();

                if (invoiceCount == 0) {
                    return "No invoices found.";
                }

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject invoiceObject = jsonArray.getJSONObject(i);
                    String invoiceId = invoiceObject.optString("id", "Unknown").trim();
                    String customerName = invoiceObject.optString("customer_name", "").trim();
                    String invoiceDate = invoiceObject.optString("invoice_date", "").trim();
                    String dueDate = invoiceObject.optString("due_date", "").trim();
                    String totalAmountStr = invoiceObject.optString("total_amount", "0").trim().replace("EUR", "").trim();
                    String status = invoiceObject.optString("status", "").trim();

                    try {
                        totalAmountSum += Double.parseDouble(totalAmountStr);
                    } catch (NumberFormatException e) {
                        Log.e("ExploreData", "Invalid total amount format: " + totalAmountStr);
                    }

                    formattedInvoices.append("Invoice ID: ").append(invoiceId).append("\n")
                            .append("Customer Name: ").append(customerName).append("\n")
                            .append("Invoice Date: ").append(invoiceDate).append("\n")
                            .append("Due Date: ").append(dueDate).append("\n")
                            .append("Total Amount: EUR ").append(totalAmountStr).append("\n")
                            .append("Status: ").append(status).append("\n\n");
                }
            } catch (Exception e) {
                e.printStackTrace();
                return "Error: Could not parse invoice details.";
            }
            return formattedInvoices.toString();
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
