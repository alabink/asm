package com.example.todolistse06302;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
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

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ManageBudgetActivity extends AppCompatActivity {

    private EditText editBudgetAmount, editBudgetCategory;
    private Button btnAddBudget, btnUpdateBudget, btnDeleteBudget;
    private ListView listViewBudgets;
    private DatabaseHelper dbHelper;
    private BudgetAdapter adapter;
    private List<Budget> budgets;
    private int selectedBudgetId = -1;
    private int currentUserId;
    private NumberFormat currencyFormatter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_budget);
        try {
            // Get current user ID from SharedPreferences
            SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
            currentUserId = sharedPreferences.getInt("userId", -1);
            
            if (currentUserId == -1) {
                Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
            initializeViews();
            setupListeners();
            loadBudgets();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error initializing: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void initializeViews() {
        dbHelper = new DatabaseHelper(this);
        editBudgetAmount = findViewById(R.id.editBudgetAmount);
        editBudgetCategory = findViewById(R.id.editBudgetCategory);
        btnAddBudget = findViewById(R.id.btnAddBudget);
        btnUpdateBudget = findViewById(R.id.btnUpdateBudget);
        btnDeleteBudget = findViewById(R.id.btnDeleteBudget);
        listViewBudgets = findViewById(R.id.listViewBudgets);
    }

    private void setupListeners() {
        btnAddBudget.setOnClickListener(v -> {
            if (validateInput()) {
                try {
                    double amount = Double.parseDouble(editBudgetAmount.getText().toString().replaceAll("[^\\d.]", ""));
                    String category = editBudgetCategory.getText().toString();

                    dbHelper.addBudget(amount, category, currentUserId);
                    loadBudgets();
                    Toast.makeText(this, "Budget Added Successfully!", Toast.LENGTH_SHORT).show();
                    clearInputFields();
                } catch (NumberFormatException e) {
                    Toast.makeText(this, "Please enter a valid amount", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Toast.makeText(this, "Error adding budget: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

//        btnUpdateBudget.setOnClickListener(v -> {
//            if (selectedBudgetId != -1 && validateInput()) {
//                try {
//                    double amount = Double.parseDouble(editBudgetAmount.getText().toString().replaceAll("[^\\d.]", ""));
//                    String category = editBudgetCategory.getText().toString();
//
//                    dbHelper.updateBudget(selectedBudgetId, amount, category);
//                    loadBudgets();
//                    Toast.makeText(this, "Budget Updated Successfully!", Toast.LENGTH_SHORT).show();
//                    clearInputFields();
//                } catch (NumberFormatException e) {
//                    Toast.makeText(this, "Please enter a valid amount", Toast.LENGTH_SHORT).show();
//                } catch (Exception e) {
//                    Toast.makeText(this, "Error updating budget: " + e.getMessage(), Toast.LENGTH_SHORT).show();
//                }
//            }
//        });

        btnDeleteBudget.setOnClickListener(v -> {
            if (selectedBudgetId != -1) {
                showDeleteConfirmationDialog();
            } else {
                Toast.makeText(this, "Please select a budget to delete", Toast.LENGTH_SHORT).show();
            }
        });

        listViewBudgets.setOnItemClickListener((parent, view, position, id) -> {
            try {
                if (position >= 0 && position < budgets.size()) {
                    Budget budget = budgets.get(position);
                    selectedBudgetId = budget.getId();
                    
                    // Remove currency formatting for editing
                    String editableAmount = String.valueOf(budget.getTotalBudget());
                    editBudgetAmount.setText(editableAmount);
                    editBudgetCategory.setText(budget.getCategory());
                }
            } catch (Exception e) {
                Toast.makeText(this, "Error selecting budget: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // Setup bottom navigation
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setSelectedItemId(R.id.navigation_budget);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_home) {
                startActivity(new Intent(this, HomeScreen.class));
                return true;
            } else if (itemId == R.id.navigation_expenses) {
                startActivity(new Intent(this, ManageExpenseActivity.class));
                return true;
            } else if (itemId == R.id.navigation_profile) {
                startActivity(new Intent(this, Profile.class));
                return true;
            } else if (itemId == R.id.navigation_budget) {
                // Already on budget screen
                return true;
            }
            return false;
        });
    }

    private void showDeleteConfirmationDialog() {
        Budget selectedBudget = null;
        for (Budget budget : budgets) {
            if (budget.getId() == selectedBudgetId) {
                selectedBudget = budget;
                break;
            }
        }

        if (selectedBudget == null) return;

        String message = String.format("Are you sure you want to delete budget for %s (%s)?", 
            selectedBudget.getCategory(),
            currencyFormatter.format(selectedBudget.getTotalBudget()));

        new AlertDialog.Builder(this)
                .setTitle("Delete Budget")
                .setMessage(message)
                .setPositiveButton("Yes", (dialog, which) -> {
                    try {
                        dbHelper.deleteBudget(selectedBudgetId);
                        loadBudgets();
                        clearInputFields();
                        Toast.makeText(this, "Budget Deleted Successfully!", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Toast.makeText(this, "Error deleting budget: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }

    private void loadBudgets() {
        budgets = dbHelper.getBudgetsForUser(currentUserId);
        if (adapter == null) {
            adapter = new BudgetAdapter(this, budgets);
            listViewBudgets.setAdapter(adapter);
        } else {
            adapter.notifyDataSetChanged();
        }
    }

    private boolean validateInput() {
        if (TextUtils.isEmpty(editBudgetAmount.getText().toString())) {
            Toast.makeText(this, "Please enter amount", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (TextUtils.isEmpty(editBudgetCategory.getText().toString())) {
            Toast.makeText(this, "Please enter category", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void clearInputFields() {
        editBudgetAmount.setText("");
        editBudgetCategory.setText("");
        selectedBudgetId = -1;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}