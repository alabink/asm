package com.example.todolistse06302;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.todolistse06302.database.DatabaseHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ManageExpenseActivity extends AppCompatActivity {

    private EditText editAmount, editCategory, editDate;
    private Button btnAdd, btnUpdate, btnDelete;
    private ListView listViewExpenses;
    private DatabaseHelper dbHelper;
    private ExpenseAdapter adapter;
    private List<Expense> expenses;
    private int selectedExpenseId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_expense);

        try {
            initializeViews();
            setupListeners();
            loadExpenses();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error initializing: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void initializeViews() {
        dbHelper = new DatabaseHelper(this);
        editAmount = findViewById(R.id.editAmount);
        editCategory = findViewById(R.id.editCategory);
        editDate = findViewById(R.id.editDate);
        btnAdd = findViewById(R.id.btnAdd);
        btnUpdate = findViewById(R.id.btnUpdate);
        btnDelete = findViewById(R.id.btnDelete);
        listViewExpenses = findViewById(R.id.listViewExpenses);
    }

    private void setupListeners() {
        editDate.setOnClickListener(v -> showDatePickerDialog());

        btnAdd.setOnClickListener(v -> {
            if (validateInput()) {
                try {
                    double amount = Double.parseDouble(editAmount.getText().toString());
                    String category = editCategory.getText().toString();
                    String date = editDate.getText().toString();

                    // Deduct from budget before adding expense
                    dbHelper.deductFromBudget(category, amount);

                    dbHelper.addExpense(amount, category, date);
                    loadExpenses();
                    Toast.makeText(this, "Add Successful !", Toast.LENGTH_SHORT).show();
                    clearInputFields();
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
                    double amount = Double.parseDouble(editAmount.getText().toString());
                    String category = editCategory.getText().toString();
                    String date = editDate.getText().toString();

                    dbHelper.updateExpense(selectedExpenseId, amount, category, date);
                    loadExpenses();
                    Toast.makeText(this, "Update Successful !", Toast.LENGTH_SHORT).show();
                    clearInputFields();
                } catch (NumberFormatException e) {
                    Toast.makeText(this, "Please enter a valid amount", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Toast.makeText(this, "Error updating expense: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnDelete.setOnClickListener(v -> {
            if (selectedExpenseId != -1) {
                showDeleteConfirmationDialog();
            } else {
                Toast.makeText(this, "Please select an expense to delete", Toast.LENGTH_SHORT).show();
            }
        });

        listViewExpenses.setOnItemClickListener((parent, view, position, id) -> {
            try {
                if (position >= 0 && position < expenses.size()) {
                    Expense expense = expenses.get(position);
                    selectedExpenseId = expense.getId();
                    editAmount.setText(String.valueOf(expense.getAmount()));
                    editCategory.setText(expense.getCategory());
                    editDate.setText(expense.getDate());
                }
            } catch (Exception e) {
                Toast.makeText(this, "Error selecting expense", Toast.LENGTH_SHORT).show();
            }
        });

        setupBottomNavigation();
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigation = findViewById(R.id.bottomNavigation);
        bottomNavigation.setSelectedItemId(R.id.navigation_expenses);
        bottomNavigation.setOnItemSelectedListener(item -> {
            try {
                if (item.getItemId() == R.id.navigation_expenses) {
                    return true;
                } else if (item.getItemId() == R.id.navigation_home) {
                    startActivity(new Intent(ManageExpenseActivity.this, HomeScreen.class));
                    return true;
                } else if (item.getItemId() == R.id.navigation_profile) {
                    startActivity(new Intent(ManageExpenseActivity.this, Profile.class));
                    return true;
                }else if (item.getItemId() == R.id.navigation_budget) {
                    startActivity(new Intent(ManageExpenseActivity.this, ManageBudgetActivity.class));
                    return true;
                }
            } catch (Exception e) {
                Toast.makeText(this, "Navigation error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
            return false;
        });
    }

    private boolean validateInput() {
        if (TextUtils.isEmpty(editAmount.getText())) {
            Toast.makeText(this, "Please enter amount", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (TextUtils.isEmpty(editCategory.getText())) {
            Toast.makeText(this, "Please enter category", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (TextUtils.isEmpty(editDate.getText())) {
            Toast.makeText(this, "Please enter date", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void showDatePickerDialog() {
        try {
            final Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                    (view, year1, month1, dayOfMonth) -> {
                        String selectedDate = dayOfMonth + "/" + (month1 + 1) + "/" + year1;
                        editDate.setText(selectedDate);
                    }, year, month, day);

            datePickerDialog.show();
        } catch (Exception e) {
            Toast.makeText(this, "Error showing date picker", Toast.LENGTH_SHORT).show();
        }
    }

    private void showDeleteConfirmationDialog() {
        try {
            new AlertDialog.Builder(this)
                    .setTitle("Delete Confirmation")
                    .setMessage("Are you sure you want to delete this expense?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        try {
                            dbHelper.deleteExpense(selectedExpenseId);
                            Toast.makeText(this, "Delete Successful !", Toast.LENGTH_SHORT).show();
                            loadExpenses();
                            clearInputFields();
                        } catch (Exception e) {
                            Toast.makeText(this, "Error deleting expense: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                    .show();
        } catch (Exception e) {
            Toast.makeText(this, "Error showing delete dialog", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadExpenses() {
        try {
            List<String[]> rawExpenses = dbHelper.getExpenses();
            expenses = new ArrayList<>();
            for (String[] e : rawExpenses) {
                expenses.add(new Expense(
                        Integer.parseInt(e[0]),
                        Double.parseDouble(e[1]),
                        e[2],
                        e[3]
                ));
            }
            adapter = new ExpenseAdapter(this, expenses);
            listViewExpenses.setAdapter(adapter);
        } catch (Exception e) {
            Toast.makeText(this, "Error loading expenses: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void clearInputFields() {
        editAmount.setText("");
        editCategory.setText("");
        editDate.setText("");
        selectedExpenseId = -1;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}
