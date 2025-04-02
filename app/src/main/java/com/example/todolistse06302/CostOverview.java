package com.example.todolistse06302;

import android.os.Bundle;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.todolistse06302.database.DatabaseHelper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CostOverview extends AppCompatActivity {
    private TextView tvTotalExpense, tvRemainingBudget;
    private ListView lvCategoryExpenses, lvDateExpenses, lvMonthExpenses;
    private DatabaseHelper dbHelper;
    private double budget = 5000000; // Ngân sách mặc định

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.costoverview);

        tvTotalExpense = findViewById(R.id.tvTotalExpense);
        tvRemainingBudget = findViewById(R.id.tvRemainingBudget);
        lvCategoryExpenses = findViewById(R.id.lvCategoryExpenses);
        lvDateExpenses = findViewById(R.id.lvDateExpenses);
        lvMonthExpenses = findViewById(R.id.lvMonthExpenses);

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
            double amount = Double.parseDouble(e[1]);
            String category = e[2];
            String date = e[3];

            totalExpense += amount;
            categorySummary.put(category, categorySummary.getOrDefault(category, 0.0) + amount);
            dateSummary.put(date, dateSummary.getOrDefault(date, 0.0) + amount);


            // Lấy tháng từ chuỗi ngày (định dạng dd/MM/yyyy)
            String month = date.split("/")[1] + "/" + date.split("/")[2];
            monthSummary.put(month, monthSummary.getOrDefault(month, 0.0) + amount);
        }



        tvTotalExpense.setText("Total spending: " + totalExpense + " VND");
        tvRemainingBudget.setText("Remaining budget: " + (budget - totalExpense) + " VND");

        lvCategoryExpenses.setAdapter(new SummaryConverter(this, categorySummary));
        lvDateExpenses.setAdapter(new SummaryConverter(this, dateSummary));
        lvMonthExpenses.setAdapter(new SummaryConverter(this, monthSummary));
    }
}
