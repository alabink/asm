package com.example.todolistse06302;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
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
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.report);

        // Ánh xạ UI Elements
        spinnerTimePeriod = findViewById(R.id.spinnerTimePeriod122);
        tvTotalExpense = findViewById(R.id.tvTotalExpense);
        tvReportTitle = findViewById(R.id.tvReportTitle);
        lvExpensesReport = findViewById(R.id.lvExpensesReport);
        barChart = findViewById(R.id.barChart);

        dbHelper = new DatabaseHelper(this);
        sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);

        setupBottomNavigation();
        setupSpinner();
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            Intent intent = null;
            if (item.getItemId() == R.id.navigation_home) {
                intent = new Intent(Report.this, HomeScreen.class);
            } else if (item.getItemId() == R.id.navigation_expenses) {
                intent = new Intent(Report.this, ManageExpenseActivity.class);
            } else if (item.getItemId() == R.id.navigation_profile) {
                intent = new Intent(Report.this, Profile.class);
            } else if (item.getItemId() == R.id.navigation_budget) {
                intent = new Intent(Report.this, ManageBudgetActivity.class);
            }
            if (intent != null) startActivity(intent);
            return true;
        });
    }

    private void setupSpinner() {
        List<String> timePeriods = new ArrayList<>();
        timePeriods.add("Monthly");
        timePeriods.add("Yearly");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, timePeriods);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTimePeriod.setAdapter(adapter);

        spinnerTimePeriod.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedOption = timePeriods.get(position);
                tvReportTitle.setText("Expense Report by " + selectedOption); // ✅ Cập nhật tiêu đề
                loadSummary(selectedOption);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private int getUserId() {
        return sharedPreferences.getInt("userId", -1);
    }

    private void loadSummary(String timePeriod) {
        int userId = getUserId();
        Cursor cursor = dbHelper.getExpenses(userId);
        Map<String, Double> summaryData = new HashMap<>();
        double totalExpense = 0;

        if (cursor != null && cursor.moveToFirst()) {
            int colAmount = cursor.getColumnIndex("amount");
            int colDate = cursor.getColumnIndex("date");

            do {
                double amount = cursor.getDouble(colAmount);
                String date = cursor.getString(colDate);
                String key = timePeriod.equals("Monthly") ? date.substring(3, 10) : date.substring(6, 10);

                totalExpense += amount;
                summaryData.put(key, summaryData.getOrDefault(key, 0.0) + amount);
            } while (cursor.moveToNext());
            cursor.close();
        }

        tvTotalExpense.setText("Total Expenses: " + totalExpense + " VND");
        lvExpensesReport.setAdapter(new SummaryAdapter(this, summaryData));
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

        BarDataSet dataSet = new BarDataSet(entries, "Expenses by " + timePeriod);
        BarData barData = new BarData(dataSet);
        barChart.setData(barData);

        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setGranularity(1f);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);

        barChart.invalidate();
    }
}