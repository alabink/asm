package com.example.todolistse06302;

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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Report extends AppCompatActivity {
    private Spinner spinnerTimePeriod;
    private TextView tvTotalExpense, tvRemainingBudget, tvReportTitle;
    private ListView lvExpensesReport;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.report);

        // Ánh xạ View
        spinnerTimePeriod = findViewById(R.id.spinnerTimePeriod);
        tvTotalExpense = findViewById(R.id.tvTotalExpense);
        tvRemainingBudget = findViewById(R.id.tvRemainingBudget);
        tvReportTitle = findViewById(R.id.tvReportTitle);
        lvExpensesReport = findViewById(R.id.lvExpensesReport);

        dbHelper = new DatabaseHelper(this);

        // Cấu hình Spinner chọn khoảng thời gian
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.time_period_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTimePeriod.setAdapter(adapter);

        // Xử lý khi chọn khoảng thời gian
        spinnerTimePeriod.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedPeriod = parent.getItemAtPosition(position).toString();
                updateReportTitle(selectedPeriod);
                loadReport(selectedPeriod);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        String selectedPeriod = spinnerTimePeriod.getSelectedItem().toString();
        loadReport(selectedPeriod); // Load lại báo cáo khi quay lại trang
    }

    private void updateReportTitle(String period) {
        if (period.equals("Hàng tháng")) {
            tvReportTitle.setText("Báo cáo chi tiêu theo tháng");
        } else if (period.equals("Hàng năm")) {
            tvReportTitle.setText("Báo cáo chi tiêu theo năm");
        }
    }

    private void loadReport(String period) {
        List<String[]> rawExpenses = dbHelper.getExpenses();
        double totalExpense = 0;
        Map<String, Double> expenseSummary = new HashMap<>();

        for (String[] e : rawExpenses) {
            double amount = Double.parseDouble(e[1]);
            String category = e[2];
            String date = e[3];

            Log.d("ExpenseData", "ID: " + e[0] + ", Amount: " + e[1] + ", Category: " + e[2] + ", Date: " + e[3]);

            // Lọc dữ liệu theo khoảng thời gian
            if (isExpenseInPeriod(date, period)) {
                totalExpense += amount;
                if (period.equals("Hàng năm")) {
                    String year = date.split("/")[2];
                    expenseSummary.put(year, expenseSummary.getOrDefault(year, 0.0) + amount);
                } else {
                    expenseSummary.put(category, expenseSummary.getOrDefault(category, 0.0) + amount);
                }
            }
        }

        // Cập nhật tổng chi tiêu & ngân sách còn lại
        tvTotalExpense.setText("Tổng chi tiêu: " + totalExpense + " VND");
        double budget = 5000000; // Ngân sách mặc định, có thể thay đổi
        tvRemainingBudget.setText("Ngân sách còn lại: " + (budget - totalExpense) + " VND");

        // Hiển thị danh sách chi tiêu
        lvExpensesReport.setAdapter(new SummaryAdapter(this, expenseSummary));
        ((ArrayAdapter) lvExpensesReport.getAdapter()).notifyDataSetChanged();
    }

    private boolean isExpenseInPeriod(String date, String period) {
        String[] dateParts = date.split("/"); // Giả sử định dạng dd/MM/yyyy
        int month = Integer.parseInt(dateParts[1]);
        int year = Integer.parseInt(dateParts[2]);

        // Lấy tháng và năm hiện tại
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        int currentMonth = calendar.get(java.util.Calendar.MONTH) + 1;
        int currentYear = calendar.get(java.util.Calendar.YEAR);

        // Kiểm tra chi tiêu thuộc khoảng thời gian nào
        if (period.equals("Hàng tháng")) {
            return month == currentMonth && year == currentYear;
        } else if (period.equals("Hàng năm")) {
            return year == currentYear;
        }
        return false;
    }
}
