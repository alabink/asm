package com.example.todolistse06302;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.todolistse06302.database.DatabaseHelper;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import android.widget.TextView;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;




public class ViewUserExpenseActivity extends AppCompatActivity {
    private Spinner spinnerUsers;
    private ListView listViewUserExpenses;
    private DatabaseHelper dbHelper;
    private List<String> userList;
    private List<String> expenseSummaryList;
    private ArrayAdapter<String> userAdapter, expenseAdapter;
    private TextView txtTotalSpent, txtTotalRemaining; // Thêm TextView
    private BarChart barChart;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_user_expense);


        spinnerUsers = findViewById(R.id.spinnerUsers);
        listViewUserExpenses = findViewById(R.id.listViewUserExpenses);
        txtTotalSpent = findViewById(R.id.txtTotalSpent);
        txtTotalRemaining = findViewById(R.id.txtTotalRemaining);

        dbHelper = new DatabaseHelper(this);
        barChart = findViewById(R.id.barChart);

        loadUsers();
        setupUserSelection();
    }

    private void loadUsers() {
        userList = dbHelper.getAllUserEmails(); // Gọi phương thức mới
        // Lấy danh sách người dùng từ Database
        userAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, userList);
        userAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerUsers.setAdapter(userAdapter);
    }

    private void setupUserSelection() {
        spinnerUsers.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                if (position >= 0) {
                    String selectedUser = userList.get(position);
                    int selectedUserId = dbHelper.getUserIdByEmail(selectedUser);
                    loadUserExpenses(selectedUserId);
                }
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });
    }

    private void loadUserExpenses(int userId) {
        expenseSummaryList = dbHelper.getExpenseSummaryForUser(userId);

        // Gọi DatabaseHelper để lấy tổng chi tiêu & ngân sách còn lại
        HashMap<String, Double> totalData = dbHelper.getTotalExpenseAndBudget(userId);

        if (expenseSummaryList.isEmpty()) {
            Toast.makeText(this, "No expense data available!", Toast.LENGTH_SHORT).show();
        }

        // Cập nhật giao diện người dùng
        double totalSpent = totalData.get("totalSpent");
        double totalRemaining = totalData.get("remainingBudget");

        txtTotalSpent.setText("Total Expenses: " + totalSpent + " VND");
        txtTotalRemaining.setText("Remaining Budget: " + totalRemaining + " VND");

        expenseAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, expenseSummaryList);
        listViewUserExpenses.setAdapter(expenseAdapter);
        updateChart(userId);
    }
    private void updateChart(int userId) {
        List<BarEntry> entries = new ArrayList<>();
        List<String> categories = dbHelper.getExpenseCategories(); // Lấy danh mục chi tiêu

        for (int i = 0; i < categories.size(); i++) {
            float amount = dbHelper.getTotalExpenseForCategory(userId, categories.get(i));
            entries.add(new BarEntry(i, amount));
        }

        BarDataSet dataSet = new BarDataSet(entries, "Spend by category");
        dataSet.setColors(ColorTemplate.COLORFUL_COLORS);
        BarData barData = new BarData(dataSet);
        barChart.setData(barData);
        barChart.invalidate(); // Refresh biểu đồ

        // Cấu hình trục X
        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(categories));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setLabelRotationAngle(45);
    }
    public void onBackClicked(View view) {
        finish(); // Kết thúc Activity này và quay lại màn hình trước
    }


}
