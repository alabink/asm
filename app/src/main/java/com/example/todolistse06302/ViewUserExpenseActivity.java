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
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

public class ViewUserExpenseActivity extends AppCompatActivity {
    private Spinner spinnerUsers, spinnerCategory;
    private ListView listViewUserExpenses;
    private DatabaseHelper dbHelper;
    private List<String> userList, categoryList, expenseSummaryList;
    private ArrayAdapter<String> userAdapter, categoryAdapter, expenseAdapter;
    private TextView txtTotalSpent, txtTotalRemaining;
    private BarChart barChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_user_expense);

        // Ánh xạ giao diện
        spinnerUsers = findViewById(R.id.spinnerUsers);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        listViewUserExpenses = findViewById(R.id.listViewUserExpenses);
        txtTotalSpent = findViewById(R.id.txtTotalSpent);
        txtTotalRemaining = findViewById(R.id.txtTotalRemaining);
        barChart = findViewById(R.id.barChart);
        dbHelper = new DatabaseHelper(this);

        // Load dữ liệu
        loadUsers();
        loadCategories();
        setupUserSelection();
        setupCategorySelection();
    }

    // Load danh sách user từ database
    private void loadUsers() {
        userList = dbHelper.getAllUserEmails();
        userAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, userList);
        userAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerUsers.setAdapter(userAdapter);
    }

    // Load danh mục chi tiêu từ database
    private void loadCategories() {
        int userId = dbHelper.getUserIdByEmail(spinnerUsers.getSelectedItem().toString());
        categoryList = dbHelper.getExpenseCategoriesForUser(userId); // Chỉ lấy các danh mục mà người dùng đã chi tiêu
        categoryList.add(0, "All"); // Thêm "All" để xem tất cả danh mục
        categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categoryList);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(categoryAdapter);
    }

    // Xử lý khi chọn user
    private void setupUserSelection() {
        spinnerUsers.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                if (position >= 0) {
                    String selectedUser = userList.get(position);
                    int selectedUserId = dbHelper.getUserIdByEmail(selectedUser);
                    loadCategories();  // Gọi lại loadCategories để cập nhật danh mục cho người dùng mới
                    loadUserExpenses(selectedUserId, "All");
                }
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });
    }

    // Xử lý khi chọn danh mục
    private void setupCategorySelection() {
        spinnerCategory.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                if (position >= 0) {
                    String selectedCategory = categoryList.get(position);
                    int userId = dbHelper.getUserIdByEmail(spinnerUsers.getSelectedItem().toString());
                    loadUserExpenses(userId, selectedCategory);
                }
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });
    }

    // Load dữ liệu chi tiêu theo user và danh mục
    private void loadUserExpenses(int userId, String category) {
        if (category.equals("All")) {
            expenseSummaryList = dbHelper.getExpenseSummaryForUser(userId);
        } else {
            expenseSummaryList = dbHelper.getExpenseSummaryForUserByCategory(userId, category);
        }

        // Lấy tổng chi tiêu và ngân sách còn lại
        HashMap<String, Double> totalData = dbHelper.getTotalExpenseAndBudget(userId);

        if (expenseSummaryList.isEmpty()) {
            Toast.makeText(this, "No expense data available!", Toast.LENGTH_SHORT).show();
        }

        // Cập nhật UI
        double totalSpent = category.equals("All") ? totalData.get("totalSpent") : dbHelper.getTotalExpenseForCategory(userId, category);
        double totalRemaining = totalData.get("remainingBudget");

        txtTotalSpent.setText("Total Expenses: " + totalSpent + " VND");
        txtTotalRemaining.setText("Remaining Budget: " + totalRemaining + " VND");

        expenseAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, expenseSummaryList);
        listViewUserExpenses.setAdapter(expenseAdapter);
        updateChart(userId, category);
    }

    // Cập nhật biểu đồ theo user và danh mục
    private void updateChart(int userId, String category) {
        List<BarEntry> entries = new ArrayList<>();
        List<String> categories = new ArrayList<>();

        if (category.equals("All")) {
            categories = dbHelper.getExpenseCategories();
            for (int i = 0; i < categories.size(); i++) {
                float amount = dbHelper.getTotalExpenseForCategory(userId, categories.get(i));
                entries.add(new BarEntry(i, amount));
            }
        } else {
            float amount = dbHelper.getTotalExpenseForCategory(userId, category);
            entries.add(new BarEntry(0, amount));
            categories.add(category);
        }

        BarDataSet dataSet = new BarDataSet(entries, "Spend by category");
        dataSet.setColors(ColorTemplate.COLORFUL_COLORS);
        BarData barData = new BarData(dataSet);
        barChart.setData(barData);
        barChart.invalidate();

        // Cấu hình trục X
        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(categories));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setLabelRotationAngle(45);
    }

    public void onBackClicked(View view) {
        finish();
    }
}