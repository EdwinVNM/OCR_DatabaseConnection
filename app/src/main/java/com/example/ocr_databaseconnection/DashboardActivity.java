package com.example.ocr_databaseconnection;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class DashboardActivity extends AppCompatActivity {

    private Button btnDataExplorer, btnChart, btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.dashboard);

        // Initialize buttons
        btnDataExplorer = findViewById(R.id.btnDataExplorer);
        btnChart = findViewById(R.id.btnChart);
        btnLogout = findViewById(R.id.btnLogout);

        // Set onClick listeners for each button
        btnDataExplorer.setOnClickListener(v -> openDataExplorer());
        btnChart.setOnClickListener(v -> openChart());
        btnLogout.setOnClickListener(v -> logout());
    }

    // Navigate to the DataExplorerActivity
    private void openDataExplorer() {
        Intent intent = new Intent(DashboardActivity.this, DataExplorerActivity.class);
        startActivity(intent);
    }

    // Navigate to the ChartActivity
    private void openChart() {
        Intent intent = new Intent(DashboardActivity.this, ChartActivity.class);
        startActivity(intent);
    }

    // Navigate back to LoginActivity
    private void logout() {
        Intent intent = new Intent(DashboardActivity.this, LoginActivity.class);
        startActivity(intent);
    }
}
