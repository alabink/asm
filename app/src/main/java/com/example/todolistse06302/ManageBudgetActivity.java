package com.example.todolistse06302;

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

import java.util.List;

public class ManageBudgetActivity extends AppCompatActivity {

    private EditText editBudgetAmount, editBudgetCategory;
    private Button btnAddBudget, btnUpdateBudget, btnDeleteBudget;
    private ListView listViewBudgets;
    private DatabaseHelper dbHelper;
    private BudgetAdapter adapter;
    private List<Budget> budgets;
    private int selectedBudgetId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_budget);

        try {
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
                    double amount = Double.parseDouble(editBudgetAmount.getText().toString());
                    String category = editBudgetCategory.getText().toString();

                    dbHelper.addBudget(amount, category);
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
//                    double amount = Double.parseDouble(editBudgetAmount.getText().toString());
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
                    editBudgetAmount.setText(String.valueOf(budget.getTotalBudget()));
                    editBudgetCategory.setText(budget.getCategory());
                }
            } catch (Exception e) {
                Toast.makeText(this, "Error selecting budget", Toast.LENGTH_SHORT).show();
            }
        });

        setupBottomNavigation();
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigation = findViewById(R.id.bottomNavigation);
        bottomNavigation.setSelectedItemId(R.id.navigation_budget);
        bottomNavigation.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.navigation_budget) {
                return true;
            } else if (item.getItemId() == R.id.navigation_home) {
                startActivity(new Intent(ManageBudgetActivity.this, HomeScreen.class));
                return true;
            } else if (item.getItemId() == R.id.navigation_expenses) {
                startActivity(new Intent(ManageBudgetActivity.this, ManageExpenseActivity.class));
                return true;
            } else if (item.getItemId() == R.id.navigation_profile) {
                startActivity(new Intent(ManageBudgetActivity.this, Profile.class));
                return true;
            }
            return false;
        });
    }

    private boolean validateInput() {
        if (TextUtils.isEmpty(editBudgetAmount.getText())) {
            Toast.makeText(this, "Please enter budget amount", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (TextUtils.isEmpty(editBudgetCategory.getText())) {
            Toast.makeText(this, "Please enter budget category", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void showDeleteConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Budget")
                .setMessage("Are you sure you want to delete this budget?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    try {
                        dbHelper.deleteBudget(selectedBudgetId);
                        loadBudgets();
                        Toast.makeText(this, "Budget Deleted Successfully!", Toast.LENGTH_SHORT).show();
                        clearInputFields();
                    } catch (Exception e) {
                        Toast.makeText(this, "Error deleting budget: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void loadBudgets() {
        try {
            budgets = dbHelper.getBudgets();
            adapter = new BudgetAdapter(this, budgets);
            listViewBudgets.setAdapter(adapter);
        } catch (Exception e) {
            Toast.makeText(this, "Error loading budgets: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
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