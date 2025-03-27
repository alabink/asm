package com.example.todolistse06302;

import android.os.Bundle;
import android.util.Log;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Report extends AppCompatActivity {
    private TextView tvTotalExpense, tvRemainingBudget, tvReportTitle;
    private ListView lvCategoryExpenses, lvDateExpenses, lvMonthExpenses;
    private Spinner spinnerTimePeriod;
    private BarChart barChart;
    private DatabaseHelper dbHelper;
    private double budget = 5000000; // Ngân sách mặc định

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.report);

        // Ánh xạ UI
        spinnerTimePeriod = findViewById(R.id.spinnerTimePeriod);
        tvTotalExpense = findViewById(R.id.tvTotalExpense);
        tvRemainingBudget = findViewById(R.id.tvRemainingBudget);
        tvReportTitle = findViewById(R.id.tvReportTitle);
        lvCategoryExpenses = findViewById(R.id.lvExpensesReport);
        barChart = findViewById(R.id.barChart);

        dbHelper = new DatabaseHelper(this);

        loadSummary();
    }

    private void loadSummary() {
        List<String[]> rawExpenses = dbHelper.getExpenses();
        double totalExpense = 0;
        Map<String, Double> categorySummary = new HashMap<>();
        Map<String, Double> dateSummary = new HashMap<>();
        Map<String, Double> monthSummary = new HashMap<>();

        for (String[] e : rawExpenses) {
            try {
                double amount = Double.parseDouble(e[1]);
                String category = e[2];
                String date = e[3];

                totalExpense += amount;
                categorySummary.put(category, categorySummary.getOrDefault(category, 0.0) + amount);
                dateSummary.put(date, dateSummary.getOrDefault(date, 0.0) + amount);

                // Lấy tháng từ chuỗi ngày (định dạng dd/MM/yyyy)
                String month = date.split("/")[1] + "/" + date.split("/")[2];
                monthSummary.put(month, monthSummary.getOrDefault(month, 0.0) + amount);
            } catch (Exception ex) {
                Log.e("Error", "Invalid data format: " + e.toString(), ex);
            }
        }

        // Cập nhật UI
        tvTotalExpense.setText("Tổng chi tiêu: " + totalExpense + " VND");
        tvRemainingBudget.setText("Ngân sách còn lại: " + (budget - totalExpense) + " VND");

        // Hiển thị danh sách chi tiêu
        lvCategoryExpenses.setAdapter(new SummaryAdapter(this, categorySummary));

        // Hiển thị biểu đồ chi tiêu theo danh mục
        updateBarChart(categorySummary);
    }

    private void updateBarChart(Map<String, Double> expenseData) {
        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        int index = 0;

        for (Map.Entry<String, Double> entry : expenseData.entrySet()) {
            entries.add(new BarEntry(index, entry.getValue().floatValue()));
            labels.add(entry.getKey());
            index++;
        }

        BarDataSet dataSet = new BarDataSet(entries, "Chi tiêu theo danh mục");
        BarData barData = new BarData(dataSet);
        barChart.setData(barData);

        // Cấu hình trục X hiển thị tên danh mục
        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setGranularity(1f);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);

        barChart.invalidate(); // Refresh biểu đồ
    }
}
