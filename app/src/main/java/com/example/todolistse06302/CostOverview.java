package com.example.todolistse06302;

import static com.example.todolistse06302.database.DatabaseHelper.COLUMN_AMOUNT;
import static com.example.todolistse06302.database.DatabaseHelper.COLUMN_CATEGORY;
import static com.example.todolistse06302.database.DatabaseHelper.COLUMN_DATE;

import android.content.SharedPreferences;
import android.database.Cursor;
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
    private SharedPreferences sharedPreferences;

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
        sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        loadSummary();
    }

    private void loadSummary() {
        int userId = getUserId();
        Cursor cursor = dbHelper.getExpenses(userId);

        if (cursor == null || cursor.getCount() == 0) {
            tvTotalExpense.setText("Total spending: 0 VND");
            tvRemainingBudget.setText("Remaining budget: " + budget + " VND");
            return;
        }

        double totalExpense = 0;
        Map<String, Double> categorySummary = new HashMap<>();
        Map<String, Double> dateSummary = new HashMap<>();
        Map<String, Double> monthSummary = new HashMap<>();

        while (cursor.moveToNext()) {
            try {
                double amount = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_AMOUNT));
                String category = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY));
                String date = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE));

                totalExpense += amount;
                categorySummary.put(category, categorySummary.getOrDefault(category, 0.0) + amount);
                dateSummary.put(date, dateSummary.getOrDefault(date, 0.0) + amount);

                // Lấy tháng từ chuỗi ngày (định dạng dd/MM/yyyy)
                String[] dateParts = date.split("/");
                if (dateParts.length == 3) {
                    String month = dateParts[1] + "/" + dateParts[2];
                    monthSummary.put(month, monthSummary.getOrDefault(month, 0.0) + amount);
                }
            } catch (Exception e) {
                e.printStackTrace(); // Log lỗi để debug nếu cần
            }
        }

        cursor.close();

        tvTotalExpense.setText("Total spending: " + totalExpense + " VND");
        tvRemainingBudget.setText("Remaining budget: " + (budget - totalExpense) + " VND");

        lvCategoryExpenses.setAdapter(new SummaryConverter(this, categorySummary));
        lvDateExpenses.setAdapter(new SummaryConverter(this, dateSummary));
        lvMonthExpenses.setAdapter(new SummaryConverter(this, monthSummary));
    }
    private int getUserId() {
        return sharedPreferences.getInt("userId", -1);
    }
}
