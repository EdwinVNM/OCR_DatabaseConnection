package com.example.ocr_databaseconnection;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.components.YAxis;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ChartActivity extends AppCompatActivity {

    private BarChart barChart;
    private RadioGroup radioGroup;
    private Button btnGenerateChart, btnDashboard;
    String ipAddress = IpAddressSingleton.getInstance().getIpAddress();
    private String baseUrl = ipAddress+"/OCR_DatabaseConnection/chart";//OCRBackend

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chart);

        radioGroup = findViewById(R.id.radioGroupCharts);
        btnGenerateChart = findViewById(R.id.btnGenerateChart);
        btnDashboard = findViewById(R.id.btnChartToDashboard);

        btnGenerateChart.setOnClickListener(v -> generateChart());

        btnDashboard.setOnClickListener(v -> {
            Intent intent = new Intent(ChartActivity.this, DashboardActivity.class);
            startActivity(intent);
        });

        FrameLayout chartContainer = findViewById(R.id.chartContainer);
        barChart = new BarChart(this);
        barChart.setLayoutParams(new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
        ));
        chartContainer.addView(barChart);
    }

    private void generateChart() {
        int selectedId = radioGroup.getCheckedRadioButtonId();
        if (selectedId == -1) {
            Toast.makeText(this, "Please select a status", Toast.LENGTH_SHORT).show();
            return;
        }

        RadioButton selectedRadioButton = findViewById(selectedId);
        String status = selectedRadioButton.getText().toString().toLowerCase();

        new Thread(() -> {
            try {
                URL url = new URL(baseUrl + "?status=" + status);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                // Log the raw response to check its format
                Log.d("ChartActivity", "Response: " + response.toString());

                HashMap<Integer, Integer> invoiceCounts = new HashMap<>();
                HashMap<Integer, Double> totalEuros = new HashMap<>();
                parseResponse(response.toString(), invoiceCounts, totalEuros);

                // Log to confirm the data
                Log.d("ChartActivity", "Invoice Counts: " + invoiceCounts);
                Log.d("ChartActivity", "Total Euros: " + totalEuros);

                runOnUiThread(() -> updateChart(invoiceCounts, totalEuros, status));

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(ChartActivity.this, "Error fetching data", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void parseResponse(String json, HashMap<Integer, Integer> invoiceCounts, HashMap<Integer, Double> totalEuros) {
        try {
            JSONArray jsonArray = new JSONArray(json);

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                int month = obj.getInt("month"); // Directly handling the month as an integer
                int count = obj.getInt("count");
                double total = obj.optDouble("total_amount"); // Using double for better precision

                // Check if month is valid (1 to 12)
                if (month >= 1 && month <= 12) {
                    invoiceCounts.put(month, count);
                    totalEuros.put(month, total);
                } else {
                    Log.w("ChartActivity", "Invalid month: " + month); // Log any invalid months
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateChart(HashMap<Integer, Integer> invoiceCounts, HashMap<Integer, Double> totalEuros, String status) {
        List<BarEntry> totalAmountEntries = new ArrayList<>();
        List<BarEntry> invoiceCountEntries = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        // Log data to confirm the values
        Log.d("ChartActivity", "Updating chart with Total Euros: " + totalEuros);
        Log.d("ChartActivity", "Updating chart with Invoice Counts: " + invoiceCounts);

        for (int i = 1; i <= 12; i++) {
            float totalAmount = totalEuros.getOrDefault(i, 0.0).floatValue();
            float invoiceCount = invoiceCounts.getOrDefault(i, 0).floatValue();

            totalAmountEntries.add(new BarEntry(i, totalAmount));
            invoiceCountEntries.add(new BarEntry(i, invoiceCount));

            labels.add(getMonthName(i));
        }

        // Dataset for Total Amount (€) - Left Axis
        BarDataSet totalAmountDataSet = new BarDataSet(totalAmountEntries, "Total Amount (€)");
        if ("paid".equals(status)) {
            totalAmountDataSet.setColor(ColorTemplate.COLORFUL_COLORS[3]);
        } else if ("partially".equals(status)) {
            totalAmountDataSet.setColor(ColorTemplate.COLORFUL_COLORS[2]);
        } else {
            totalAmountDataSet.setColor(ColorTemplate.COLORFUL_COLORS[0]);
        }
        totalAmountDataSet.setAxisDependency(YAxis.AxisDependency.LEFT); // Assign to LEFT Y-axis

        // Dataset for Invoice Count - Right Axis (Blue color)
        BarDataSet invoiceCountDataSet = new BarDataSet(invoiceCountEntries, "Invoice Count");
        invoiceCountDataSet.setColor(Color.BLUE);
        invoiceCountDataSet.setAxisDependency(YAxis.AxisDependency.RIGHT); // Assign to RIGHT Y-axis

        // Combine datasets
        BarData barData = new BarData(totalAmountDataSet, invoiceCountDataSet);
        barData.setBarWidth(0.40f); // Adjust bar width for better spacing

        // Apply data to the chart
        barChart.setData(barData);

        // Enable and format Right Y-Axis for Invoice Count
        YAxis rightAxis = barChart.getAxisRight();
        rightAxis.setEnabled(true);
        rightAxis.setAxisMinimum(0f); // Ensure it starts at 0
        rightAxis.setGranularity(1f); // Step size
        rightAxis.setTextColor(Color.BLUE); // Make it visually match the invoice count bars

        // Adjust Left Y-Axis for Total Amount
        YAxis leftAxis = barChart.getAxisLeft();
        leftAxis.setAxisMinimum(0f);
        leftAxis.setGranularity(50f); // Step size (adjust based on your values)

        // Configure X-Axis
        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setLabelRotationAngle(0f);
        xAxis.setYOffset(20f);
        xAxis.setAxisMinimum(-2f);
        xAxis.setAxisMaximum(12f);

        // Group bars correctly (to avoid overlapping)
        float groupSpace = 0.2f;
        float barSpace = 0.05f;
        float barWidth = 0.4f;
        barChart.groupBars(0.5f, groupSpace, barSpace);

        // Hide chart description
        barChart.getDescription().setEnabled(false);

        // Refresh the chart
        barChart.invalidate();
    }

    // Function to get month names
    private String getMonthName(int month) {
        String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
        return months[month - 1];
    }

}
//package com.example.ocr_databaseconnection;
//
//import android.content.Intent;
//import android.graphics.Color;
//import android.os.Bundle;
//import android.util.Log;
//import android.widget.Button;
//import android.widget.FrameLayout;
//import android.widget.RadioButton;
//import android.widget.RadioGroup;
//import android.widget.Toast;
//import androidx.appcompat.app.AppCompatActivity;
//
//import com.github.mikephil.charting.charts.BarChart;
//import com.github.mikephil.charting.components.XAxis;
//import com.github.mikephil.charting.data.BarData;
//import com.github.mikephil.charting.data.BarDataSet;
//import com.github.mikephil.charting.data.BarEntry;
//import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
//import com.github.mikephil.charting.utils.ColorTemplate;
//import com.github.mikephil.charting.components.YAxis;
//
//import org.json.JSONArray;
//import org.json.JSONObject;
//
//import java.io.BufferedReader;
//import java.io.InputStreamReader;
//import java.net.HttpURLConnection;
//import java.net.URL;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//
//public class ChartActivity extends AppCompatActivity {
//
//    private BarChart barChart;
//    private RadioGroup radioGroup;
//    private Button btnGenerateChart, btnDashboard;
//    String ipAddress = IpAddressSingleton.getInstance().getIpAddress();
//    private String baseUrl = ipAddress + "/OCR_DatabaseConnection/chart";//OCRBackend
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.chart);
//
//        radioGroup = findViewById(R.id.radioGroupCharts);
//        btnGenerateChart = findViewById(R.id.btnGenerateChart);
//        btnDashboard = findViewById(R.id.btnChartToDashboard);
//
//        btnGenerateChart.setOnClickListener(v -> generateChart());
//
//        btnDashboard.setOnClickListener(v -> {
//            Intent intent = new Intent(ChartActivity.this, DashboardActivity.class);
//            startActivity(intent);
//        });
//
//        FrameLayout chartContainer = findViewById(R.id.chartContainer);
//        barChart = new BarChart(this);
//        barChart.setLayoutParams(new FrameLayout.LayoutParams(
//                FrameLayout.LayoutParams.MATCH_PARENT,
//                FrameLayout.LayoutParams.MATCH_PARENT
//        ));
//        chartContainer.addView(barChart);
//    }
//
//    private void generateChart() {
//        int selectedId = radioGroup.getCheckedRadioButtonId();
//        if (selectedId == -1) {
//            Toast.makeText(this, "Please select a status", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        RadioButton selectedRadioButton = findViewById(selectedId);
//        String status = selectedRadioButton.getText().toString().toLowerCase();
//
//        new Thread(() -> {
//            try {
//                URL url = new URL(baseUrl + "?status=" + status);
//                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//                conn.setRequestMethod("GET");
//
//                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
//                StringBuilder response = new StringBuilder();
//                String line;
//                while ((line = reader.readLine()) != null) {
//                    response.append(line);
//                }
//                reader.close();
//
//                // Log the raw response to check its format
//                Log.d("ChartActivity", "Response: " + response.toString());
//
//                HashMap<Integer, Integer> invoiceCounts = new HashMap<>();
//                HashMap<Integer, Double> totalEuros = new HashMap<>();
//                parseResponse(response.toString(), invoiceCounts, totalEuros);
//
//                // Log to confirm the data
//                Log.d("ChartActivity", "Invoice Counts: " + invoiceCounts);
//                Log.d("ChartActivity", "Total Euros: " + totalEuros);
//
//                runOnUiThread(() -> updateChart(invoiceCounts, totalEuros, status));
//
//            } catch (Exception e) {
//                e.printStackTrace();
//                runOnUiThread(() -> Toast.makeText(ChartActivity.this, "Error fetching data", Toast.LENGTH_SHORT).show());
//            }
//        }).start();
//    }
//
//    private void parseResponse(String json, HashMap<Integer, Integer> invoiceCounts, HashMap<Integer, Double> totalEuros) {
//        try {
//            JSONArray jsonArray = new JSONArray(json);
//
//            for (int i = 0; i < jsonArray.length(); i++) {
//                JSONObject obj = jsonArray.getJSONObject(i);
//                int month = obj.getInt("month"); // Directly handling the month as an integer
//                int count = obj.getInt("count");
//                double total = obj.optDouble("total_amount"); // Using double for better precision
//
//                // Check if month is valid (1 to 12)
//                if (month >= 1 && month <= 12) {
//                    invoiceCounts.put(month, count);
//                    totalEuros.put(month, total);
//                } else {
//                    Log.w("ChartActivity", "Invalid month: " + month); // Log any invalid months
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    private void updateChart(HashMap<Integer, Integer> invoiceCounts, HashMap<Integer, Double> totalEuros, String status) {
//        List<BarEntry> totalAmountEntries = new ArrayList<>();
//        List<BarEntry> invoiceCountEntries = new ArrayList<>();
//        List<String> labels = new ArrayList<>();
//
//        // Log data to confirm the values
//        Log.d("ChartActivity", "Updating chart with Total Euros: " + totalEuros);
//        Log.d("ChartActivity", "Updating chart with Invoice Counts: " + invoiceCounts);
//
//        for (int i = 1; i <= 12; i++) {
//            float totalAmount = totalEuros.getOrDefault(i, 0.0).floatValue();
//            float invoiceCount = invoiceCounts.getOrDefault(i, 0).floatValue();
//
//            totalAmountEntries.add(new BarEntry(i, totalAmount));
//            invoiceCountEntries.add(new BarEntry(i, invoiceCount));
//
//            labels.add(getMonthName(i));
//        }
//
//        // Dataset for Total Amount (€) - Left Axis
//        BarDataSet totalAmountDataSet = new BarDataSet(totalAmountEntries, "Total Amount (€)");
//        if ("paid".equals(status)) {
//            totalAmountDataSet.setColor(ColorTemplate.COLORFUL_COLORS[3]);
//        } else if ("partially paid".equals(status)) {
//            Log.d("Yellow", "Updating chart Yellow: " + status);
//            totalAmountDataSet.setColor(ColorTemplate.COLORFUL_COLORS[2]);
//        } else {
//            totalAmountDataSet.setColor(ColorTemplate.COLORFUL_COLORS[0]);
//        }
//        totalAmountDataSet.setAxisDependency(YAxis.AxisDependency.LEFT); // Assign to LEFT Y-axis
//
//        // Dataset for Invoice Count - Right Axis (Blue color)
//        BarDataSet invoiceCountDataSet = new BarDataSet(invoiceCountEntries, "Invoice Count");
//        invoiceCountDataSet.setColor(Color.BLUE);
//        invoiceCountDataSet.setAxisDependency(YAxis.AxisDependency.RIGHT); // Assign to RIGHT Y-axis
//
//        // Combine datasets
//        BarData barData = new BarData(totalAmountDataSet, invoiceCountDataSet);
//        barData.setBarWidth(0.40f); // Adjust bar width for better spacing
//
//        // Apply data to the chart
//        barChart.setData(barData);
//
//        // Enable and format Right Y-Axis for Invoice Count
//        YAxis rightAxis = barChart.getAxisRight();
//        rightAxis.setEnabled(true);
//        rightAxis.setAxisMinimum(0f); // Ensure it starts at 0
//        rightAxis.setGranularity(1f); // Step size
//        rightAxis.setTextColor(Color.BLUE); // Make it visually match the invoice count bars
//
//        // Adjust Left Y-Axis for Total Amount
//        YAxis leftAxis = barChart.getAxisLeft();
//        leftAxis.setAxisMinimum(0f);
//        leftAxis.setGranularity(50f); // Step size (adjust based on your values)
//
//        // Configure X-Axis
//        XAxis xAxis = barChart.getXAxis();
//        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
//        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
//        xAxis.setGranularity(1f);
//        xAxis.setLabelRotationAngle(0f);
//        xAxis.setYOffset(20f);
//        xAxis.setAxisMinimum(-2f);
//        xAxis.setAxisMaximum(12f);
//
//        // Group bars correctly (to avoid overlapping)
//        float groupSpace = 0.2f;
//        float barSpace = 0.05f;
//        float barWidth = 0.4f;
//        barChart.groupBars(0.5f, groupSpace, barSpace);
//
//        // Hide chart description
//        barChart.getDescription().setEnabled(false);
//
//        // Refresh the chart
//        barChart.invalidate();
//    }
//
//    // Function to get month names
//    private String getMonthName(int month) {
//        String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
//        return months[month - 1];
//    }
//
//}
