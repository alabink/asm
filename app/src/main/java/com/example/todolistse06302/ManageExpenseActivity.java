package com.example.todolistse06302;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.todolistse06302.database.DatabaseHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class ManageExpenseActivity extends AppCompatActivity {
    private EditText editAmount, editDate;
    private Spinner spinnerCategory;
    private Button btnAdd, btnUpdate, btnDelete;
    private ListView listViewExpenses;
    private DatabaseHelper dbHelper;
    private ExpenseAdapter adapter;
    private List<Expense> expenses;
    private int selectedExpenseId = -1;
    private int currentUserId;
    private Calendar calendar;
    private List<String> categories;
    private ArrayAdapter<String> categoryAdapter;
    private String currentUserEmail;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_expense);

        // Get current user ID from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        currentUserId = sharedPreferences.getInt("userId", -1);

        if (currentUserId == -1) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initializeViews();
        setupListeners();
        loadExpenses();
        loadCategories();
    }

    private void initializeViews() {
        dbHelper = new DatabaseHelper(this);
        editAmount = findViewById(R.id.editAmount);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        editDate = findViewById(R.id.editDate);
        btnAdd = findViewById(R.id.btnAdd);
        btnUpdate = findViewById(R.id.btnUpdate);
        btnDelete = findViewById(R.id.btnDelete);
        listViewExpenses = findViewById(R.id.listViewExpenses);
        calendar = Calendar.getInstance();

        // Initialize categories list and adapter
        categories = new ArrayList<>();
        categoryAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, categories);
        categoryAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spinnerCategory.setAdapter(categoryAdapter);
    }

    private void loadCategories() {
        categories.clear();
        categories.add("Choose Category");
        categories.addAll(dbHelper.getBudgetCategoriesForUser(currentUserId));
        if (categories.size() == 1) {
            categories.add("No budgets available");
        }
        categoryAdapter.notifyDataSetChanged();
    }
<<<<<<< HEAD
=======



>>>>>>> 21def786ec3bd78acad55c194a776b0644ce4088
    private void setupListeners() {
        editDate.setOnClickListener(v -> showDatePicker());

        btnAdd.setOnClickListener(v -> {
            if (validateInput()) {
                try {
                    double amount = Double.parseDouble(editAmount.getText().toString());
                    String category = spinnerCategory.getSelectedItem().toString();

                    if ("Choose Category".equals(category) || "No budgets available".equals(category)) {
                        Toast.makeText(this, "Please select a valid category", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String date = editDate.getText().toString();

                    // Kiểm tra số dư ngân sách trước khi thêm expense
                    double remainingBudget = dbHelper.getRemainingBudgetByCategory(category, currentUserId);
                    if (amount > remainingBudget) {
                        Toast.makeText(this, "Expense exceeds remaining budget", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Thêm expense và trừ ngân sách
                    dbHelper.addExpense(amount, category, date, currentUserId);

                    // Trừ ngân sách
                    dbHelper.deductFromBudget(category, amount, currentUserId);

                    loadExpenses();
                    clearInputFields();
                    Toast.makeText(this, "Expense Added Successfully!", Toast.LENGTH_SHORT).show();
                } catch (NumberFormatException e) {
                    Toast.makeText(this, "Please enter a valid amount", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Toast.makeText(this, "Error adding expense: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnUpdate.setOnClickListener(v -> {
            if (selectedExpenseId != -1 && validateInput()) {
                try {
                    // Lấy expense cũ
                    Expense oldExpense = null;
                    for (Expense e : expenses) {
                        if (e.getId() == selectedExpenseId) {
                            oldExpense = e;
                            break;
                        }
                    }

                    double newAmount = Double.parseDouble(editAmount.getText().toString());
                    String category = spinnerCategory.getSelectedItem().toString();

                    if ("Choose Category".equals(category) || "No budgets available".equals(category)) {
                        Toast.makeText(this, "Please select a valid category", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String date = editDate.getText().toString();

                    // Kiểm tra số dư ngân sách
                    double remainingBudget = dbHelper.getRemainingBudgetByCategory(category, currentUserId);
                    double budgetDifference = newAmount - oldExpense.getAmount();

                    if (budgetDifference > remainingBudget) {
                        Toast.makeText(this, "Expense exceeds remaining budget", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Cập nhật expense
                    dbHelper.updateExpense(selectedExpenseId, newAmount, category, date);

                    // Điều chỉnh ngân sách
                    dbHelper.deductFromBudget(category, budgetDifference, currentUserId);

                    loadExpenses();
                    clearInputFields();
                    Toast.makeText(this, "Expense Updated Successfully!", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Toast.makeText(this, "Error updating expense: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnDelete.setOnClickListener(v -> {
            if (selectedExpenseId != -1) {
                try {
                    // Lấy expense sắp xóa
                    Expense expenseToDelete = null;
                    for (Expense e : expenses) {
                        if (e.getId() == selectedExpenseId) {
                            expenseToDelete = e;
                            break;
                        }
                    }

                    // Trả lại ngân sách về trạng thái ban đầu
                    dbHelper.addBudgetAmountBack(expenseToDelete.getCategory(), expenseToDelete.getAmount(), currentUserId);

                    // Xóa expense
                    dbHelper.deleteExpense(selectedExpenseId);

                    loadExpenses();
                    clearInputFields();
                    Toast.makeText(this, "Expense Deleted Successfully!", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Toast.makeText(this, "Error deleting expense: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Please select an expense to delete", Toast.LENGTH_SHORT).show();
            }
        });
        listViewExpenses.setOnItemClickListener((parent, view, position, id) -> {
            if (position >= 0 && position < expenses.size()) {
                Expense expense = expenses.get(position);
                selectedExpenseId = expense.getId();
                editAmount.setText(String.valueOf(expense.getAmount()));

                // Find and set the spinner position
                int categoryPosition = categories.indexOf(expense.getCategory());
                if (categoryPosition >= 0) {
                    spinnerCategory.setSelection(categoryPosition);
                }
                editDate.setText(expense.getDate());
            }
        });

        // Setup bottom navigation
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setSelectedItemId(R.id.navigation_expenses);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_home) {
                startActivity(new Intent(this, HomeScreen.class));
                return true;
            } else if (itemId == R.id.navigation_expenses) {
                // Already on expenses screen
                return true;
            } else if (itemId == R.id.navigation_budget) {
                startActivity(new Intent(this, ManageBudgetActivity.class));
                return true;
            } else if (itemId == R.id.navigation_profile) {
                startActivity(new Intent(this, Profile.class));
                return true;
            }else if (itemId == R.id.navigation_ChiPhi) {
                // Chuyển đến Activity RecurringExpenseActivity
                startActivity(new Intent(this, RecurringExpenseActivity.class));
                return true;
            }
            return false;
        });
    }

    private void showDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(

            this,
            (view, year, month, dayOfMonth) -> {
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, month);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateDateInView();
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void updateDateInView() {
        String myFormat = "dd/MM/yyyy";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.getDefault());
        editDate.setText(sdf.format(calendar.getTime()));
    }

    private void loadExpenses() {
        expenses = dbHelper.getExpensesForUser(currentUserId);
        adapter = new ExpenseAdapter(this, expenses);
        listViewExpenses.setAdapter(adapter);
    }

    private boolean validateInput() {
        if (TextUtils.isEmpty(editAmount.getText().toString())) {
            Toast.makeText(this, "Please enter amount", Toast.LENGTH_SHORT).show();
            return false;
        }
        String selectedCategory = spinnerCategory.getSelectedItem().toString();
        if ("Choose Category".equals(selectedCategory) || "No budgets available".equals(selectedCategory)) {
            Toast.makeText(this, "Please select a valid category", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (TextUtils.isEmpty(editDate.getText().toString())) {
            Toast.makeText(this, "Please select date", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void clearInputFields() {
        editAmount.setText("");
        editDate.setText("");
        selectedExpenseId = -1;
        spinnerCategory.setSelection(0);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCategories();
        loadExpenses();
    }
}

