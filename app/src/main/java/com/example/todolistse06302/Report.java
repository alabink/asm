package com.example.todolistse06302;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.todolistse06302.database.DatabaseHelper;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Report extends AppCompatActivity {
    private TextView tvTotalExpense, tvReportTitle;
    private ListView lvExpensesReport;
    private Spinner spinnerTimePeriod;
    private BarChart barChart;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.report);

        // UI Mapping
        spinnerTimePeriod = findViewById(R.id.spinnerTimePeriod122);
        tvTotalExpense = findViewById(R.id.tvTotalExpense);
        tvReportTitle = findViewById(R.id.tvReportTitle);
        lvExpensesReport = findViewById(R.id.lvExpensesReport);
        barChart = findViewById(R.id.barChart);

        dbHelper = new DatabaseHelper(this);

        // Xử lý sự kiện cho BottomNavigationView
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_home) {
                startActivity(new Intent(Report.this, HomeScreen.class));
                // Hiện tại đã ở Home, không cần làm gì
                return true;
            } else if (itemId == R.id.navigation_expenses) {
                // Chuyển đến Activity Manage Expense
                startActivity(new Intent(Report.this, ManageExpenseActivity.class));
                return true;
            } else if (itemId == R.id.navigation_profile) {
                // Chuyển đến Activity Profile khi user click vào
                startActivity(new Intent(Report.this, Profile.class));
                return true;
            } else if (itemId == R.id.navigation_budget) {
                // Chuyển đến Activity ManageBudgetActivity
                startActivity(new Intent(Report.this, ManageBudgetActivity.class));
                return true;
            }
            return false;
        });


        // Set up Spinner data
        List<String> timePeriods = new ArrayList<>();
        timePeriods.add("Monthly");
        timePeriods.add("Yearly");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, timePeriods);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTimePeriod.setAdapter(adapter);

        // Add event listener for time period selection
        spinnerTimePeriod.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedOption = timePeriods.get(position);
                loadSummary(selectedOption);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        loadSummary("Monthly"); // Default to displaying monthly data
    }

    private void loadSummary(String timePeriod) {
        List<String[]> rawExpenses = dbHelper.getExpenses();
        double totalExpense = 0;
        Map<String, Double> summaryData = new HashMap<>();

        for (String[] e : rawExpenses) {
            try {
                double amount = Double.parseDouble(e[1]);
                String date = e[3]; // Format: dd/MM/yyyy

                totalExpense += amount;

                if (timePeriod.equals("Monthly")) {
                    // Extract month and year (MM/yyyy)
                    String monthYear = date.split("/")[1] + "/" + date.split("/")[2];
                    summaryData.put(monthYear, summaryData.getOrDefault(monthYear, 0.0) + amount);
                } else if (timePeriod.equals("Yearly")) {
                    // Extract year (yyyy)
                    String year = date.split("/")[2];
                    summaryData.put(year, summaryData.getOrDefault(year, 0.0) + amount);
                }
            } catch (Exception ex) {
                Log.e("Error", "Invalid data format: " + e.toString(), ex);
            }
        }

        // Update UI
        tvTotalExpense.setText("Total Expenses: " + totalExpense + " VND");

        // Display expense list by month or year
        lvExpensesReport.setAdapter(new SummaryAdapter(this, summaryData));

        // Update chart
        updateBarChart(summaryData, timePeriod);
    }

    private void updateBarChart(Map<String, Double> expenseData, String timePeriod) {
        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        int index = 0;

        for (Map.Entry<String, Double> entry : expenseData.entrySet()) {
            entries.add(new BarEntry(index, entry.getValue().floatValue()));
            labels.add(entry.getKey());
            index++;
        }

        BarDataSet dataSet = new BarDataSet(entries, "Expenses by " + (timePeriod.equals("Monthly") ? "Month" : "Year"));
        BarData barData = new BarData(dataSet);
        barChart.setData(barData);

        // Configure X-axis to display month or year names
        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setGranularity(1f);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);

        barChart.invalidate(); // Refresh chart
    }
}