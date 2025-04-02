package com.example.todolistse06302;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.todolistse06302.database.DatabaseHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.List;
import java.util.ArrayList;
import android.util.Log;



public class RecurringExpenseActivity extends AppCompatActivity {
    private EditText edtCostName, edtCostAmount, edtStartDate, edtEndDate;
    private Button btnAddRecurringCost, btnEditCost, btnDeleteCost;
    private ListView listViewRecurringCosts;
    private DatabaseHelper dbHelper;
    private RecurringExpenseAdapter adapter;
    private List<RecurringExpense> recurringExpenses;
    private int selectedExpenseId = -1;
    private int currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recurring_costs);

        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        currentUserId = sharedPreferences.getInt("userId", -1); // Sử dụng đúng key mà bạn đã lưu trước đó
        if (currentUserId == -1) {
            Toast.makeText(this, "User ID not found. Please log in again.", Toast.LENGTH_SHORT).show();
            finish();
        }

        dbHelper = new DatabaseHelper(this);
        edtCostName = findViewById(R.id.edtCostName);
        edtCostAmount = findViewById(R.id.edtCostAmount);
        edtStartDate = findViewById(R.id.edtStartDate);
        edtEndDate = findViewById(R.id.edtEndDate);
        btnAddRecurringCost = findViewById(R.id.btnAddRecurringCost);
        btnEditCost = findViewById(R.id.btnEditCost);
        btnDeleteCost = findViewById(R.id.btnDeleteCost);
        listViewRecurringCosts = findViewById(R.id.listViewRecurringCosts);

        edtStartDate.setOnClickListener(v -> showDatePickerDialog(edtStartDate));
        edtEndDate.setOnClickListener(v -> showDatePickerDialog(edtEndDate));

        btnAddRecurringCost.setOnClickListener(v -> addRecurringCost());

        loadRecurringExpenses();

        // Nút Edit Cost
        btnEditCost.setOnClickListener(v -> {
            if (selectedExpenseId == -1) {
                Toast.makeText(this, "Please select an expense to edit!", Toast.LENGTH_SHORT).show();
                return;
            }
            String costName = edtCostName.getText().toString().trim();
            String amountStr = edtCostAmount.getText().toString().trim();
            String startDate = edtStartDate.getText().toString().trim();
            String endDate = edtEndDate.getText().toString().trim();

            if (costName.isEmpty() || amountStr.isEmpty() || startDate.isEmpty() || endDate.isEmpty()) {
                Toast.makeText(this, "Please fill all fields!", Toast.LENGTH_SHORT).show();
                return;
            }
            try {
                double amount = Double.parseDouble(amountStr);

                dbHelper.editcost(selectedExpenseId, costName, amount, startDate, endDate, currentUserId);
                updateRecurringExpenseList();
                if (adapter != null) {
                    adapter.notifyDataSetChanged();
                }
                Toast.makeText(this, "Update Successful!", Toast.LENGTH_SHORT).show();
                clearInputFields();
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Invalid amount entered!", Toast.LENGTH_SHORT).show();
            }
        });
        // Nút Delete Cost
        btnDeleteCost.setOnClickListener(v -> {
            if (selectedExpenseId != -1) {
                showDeleteConfirmationDialog();
            } else {
                Toast.makeText(this, "Please select an expense to delete!", Toast.LENGTH_SHORT).show();
            }
        });
        // Xử lý sự kiện khi chọn Recurring Expense từ ListView
        listViewRecurringCosts.setOnItemClickListener((parent, view, position, id) -> {
            RecurringExpense expense = recurringExpenses.get(position);
            selectedExpenseId = expense.getId();
            edtCostName.setText(expense.getName());

            DecimalFormat df = new DecimalFormat("#");
            String formattedAmount = df.format(expense.getAmount());
            edtCostAmount.setText(formattedAmount);
            Toast.makeText(this, "Amount: " + formattedAmount, Toast.LENGTH_SHORT).show();

            edtStartDate.setText(expense.getStartDate());
            edtEndDate.setText(expense.getEndDate());
        });

        BottomNavigationView bottomNavigation = findViewById(R.id.bottomNavigation);
        bottomNavigation.setSelectedItemId(R.id.navigation_ChiPhi);
        bottomNavigation.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.navigation_ChiPhi) {
                // Hiện tại đã ở RecurringExpenseActivity, không cần làm gì
                return true;
            } else if (item.getItemId() == R.id.navigation_expenses) {
                // Chuyển đến Activity Manage Expense
                startActivity(new Intent(RecurringExpenseActivity.this, ManageExpenseActivity.class));
                return true;
            } else if (item.getItemId() == R.id.navigation_home) {
                // Chuyển đến Activity HomeScreen khi user click vào
                startActivity(new Intent(RecurringExpenseActivity.this, HomeScreen.class));
                return true;
            }else if (item.getItemId() == R.id.navigation_profile) {
                // Chuyển sang Activity Profile
                startActivity(new Intent(RecurringExpenseActivity.this, Profile.class));
                return true;
            }else if (item.getItemId() == R.id.navigation_budget) {
                // Chuyển sang Activity ManageBudgetActivity
                startActivity(new Intent(RecurringExpenseActivity.this, ManageBudgetActivity.class));
                return true;
            }
            return false;
        });
    }

    private void showDatePickerDialog(EditText editText) {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year1, month1, dayOfMonth) -> {
                    String selectedDate = dayOfMonth + "/" + (month1 + 1) + "/" + year1;
                    editText.setText(selectedDate);
                }, year, month, day);

        datePickerDialog.show();
    }
    private void updateRecurringExpenseList() {
        recurringExpenses.clear();
        recurringExpenses.addAll(dbHelper.getRecurringExpenses(currentUserId));
        adapter.notifyDataSetChanged();
    }
    private int getCurrentUserId() {
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        return prefs.getInt("userId", -1);  // Trả về -1 nếu không tìm thấy user_id
    }

    public void addRecurringCost() {
        String name = edtCostName.getText().toString().trim();
        String amountStr = edtCostAmount.getText().toString().trim();
        String startDate = edtStartDate.getText().toString().trim();
        String endDate = edtEndDate.getText().toString().trim();

        if (name.isEmpty() || amountStr.isEmpty() || startDate.isEmpty() || endDate.isEmpty()) {
            Toast.makeText(this, "Please fill all fields!", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            double amount = Double.parseDouble(amountStr);
            Log.d("DEBUG", "Adding expense - Name: " + name + ", Amount: " + amount + ", Start: " + startDate + ", End: " + endDate + ", User ID: " + currentUserId);
            dbHelper.addRecurringExpense(name, amount, startDate, endDate, currentUserId);
            loadRecurringExpenses();
            Toast.makeText(this, "Recurring cost added!", Toast.LENGTH_SHORT).show();
            clearInputFields();
        } catch (NumberFormatException e) {
            Log.e("ERROR", "Invalid amount entered: " + amountStr);
            Toast.makeText(this, "Invalid amount entered!", Toast.LENGTH_SHORT).show();
        }
    }
    private void showDeleteConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Confirmation")
                .setMessage("Are you sure you want to delete this expense?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    dbHelper.deleteCost(selectedExpenseId, currentUserId);
                    updateRecurringExpenseList();
                    adapter.notifyDataSetChanged();
                    Toast.makeText(this, "Delete Successful!", Toast.LENGTH_SHORT).show();
                    clearInputFields();
                })
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .show();
    }
    private void loadRecurringExpenses() {
        Log.d("DEBUG", "Loading expenses for user ID: " + currentUserId);
        if (recurringExpenses == null) {
            recurringExpenses = new ArrayList<>();
        }
        recurringExpenses.clear();
        recurringExpenses.addAll(dbHelper.getRecurringExpenses(currentUserId));
        Log.d("DEBUG", "Number of expenses loaded: " + recurringExpenses.size());
        if (adapter == null) {
            adapter = new RecurringExpenseAdapter(this, recurringExpenses);
            listViewRecurringCosts.setAdapter(adapter);
        } else {
            adapter.notifyDataSetChanged();
        }
    }
    private void clearInputFields() {
        edtCostName.setText("");
        edtCostAmount.setText("");
        edtStartDate.setText("");
        edtEndDate.setText("");
        selectedExpenseId = -1;
    }
}
