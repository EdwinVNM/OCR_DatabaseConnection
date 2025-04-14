package com.example.ocr_databaseconnection;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class CrudActionsActivity extends AppCompatActivity {

    private Button btnCreate, btnRead, btnUpdate, btnDelete, btnHome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.crud_actions);

        // Initialize buttons
        btnCreate = findViewById(R.id.btnCreate);
        btnRead = findViewById(R.id.btnRead);
        btnUpdate = findViewById(R.id.btnUpdate);
        btnDelete = findViewById(R.id.btnDelete);
        btnHome = findViewById(R.id.btnHome);

        // Set onClick listeners for each button
        btnCreate.setOnClickListener(view -> {
            // Retrieve the extracted data from SharedPreferences
            SharedPreferences preferences = getSharedPreferences("InvoiceData", MODE_PRIVATE);
            String customerName = preferences.getString("customer_name", "Not found");
            String invoiceDate = preferences.getString("invoice_date", "Not found");
            String dueDate = preferences.getString("due_date", "Not found");
            String totalAmount = preferences.getString("total_amount", "Not found");
            String status = preferences.getString("status", "Not found");

            Log.d("Fields", customerName);
            Log.d("Fields", invoiceDate);
            Log.d("Fields", dueDate);
            Log.d("Fields", totalAmount);
            Log.d("Fields", status);

            // Pass the values to the next screen (CreateInvoiceActivity)
            Intent intent = new Intent(CrudActionsActivity.this, CreateInvoiceActivity.class);
            intent.putExtra("customer_name", customerName);
            intent.putExtra("invoice_date", invoiceDate);
            intent.putExtra("due_date", dueDate);
            intent.putExtra("total_amount", totalAmount);
            intent.putExtra("status", status);
            startActivity(intent);
        });

        btnRead.setOnClickListener(v -> openReadInvoiceScreen());
        btnUpdate.setOnClickListener(v -> openUpdateInvoiceScreen());
        btnDelete.setOnClickListener(v -> openDeleteInvoiceScreen());
        btnHome.setOnClickListener(v -> goHome());
    }

    // Navigate to the ReadInvoiceActivity
    private void openReadInvoiceScreen() {
        Intent intent = new Intent(CrudActionsActivity.this, ReadInvoiceActivity.class);
        startActivity(intent);
    }

    // Navigate to the UpdateInvoiceActivity
    private void openUpdateInvoiceScreen() {
        Intent intent = new Intent(CrudActionsActivity.this, UpdateInvoiceActivity.class);
        startActivity(intent);
    }

    // Navigate to the DeleteInvoiceActivity
    private void openDeleteInvoiceScreen() {
        Intent intent = new Intent(CrudActionsActivity.this, DeleteInvoiceActivity.class);
        startActivity(intent);
    }

    // Navigate back to MainActivity
    private void goHome() {
        Intent intent = new Intent(CrudActionsActivity.this, MainActivity.class);
        startActivity(intent);
    }
}
