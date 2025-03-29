package com.example.todolistse06302;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
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

//import androidx.appcompat.app.AppCompatActivity;


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

        dbHelper = new DatabaseHelper(this);

        editAmount = findViewById(R.id.editAmount);
        editCategory = findViewById(R.id.editCategory);
        editDate = findViewById(R.id.editDate);
        btnAdd = findViewById(R.id.btnAdd);
        btnUpdate = findViewById(R.id.btnUpdate);
        btnDelete = findViewById(R.id.btnDelete);
        listViewExpenses = findViewById(R.id.listViewExpenses);

        // Hiển thị DatePicker khi người dùng nhấn vào editDate
        editDate.setOnClickListener(v -> showDatePickerDialog());

        loadExpenses();

        // Thêm Expense
        btnAdd.setOnClickListener(v -> {
            double amount = Double.parseDouble(editAmount.getText().toString());
            String category = editCategory.getText().toString();
            String date = editDate.getText().toString();

            if (!category.isEmpty() && !date.isEmpty()) {
                dbHelper.addExpense(amount, category, date);
                loadExpenses();
                Toast.makeText(this, "Add Successful !", Toast.LENGTH_SHORT).show();
                clearInputFields();
            }
        });

        // Cập nhật Expense
        btnUpdate.setOnClickListener(v -> {
            if (selectedExpenseId != -1) {
                double amount = Double.parseDouble(editAmount.getText().toString());
                String category = editCategory.getText().toString();
                String date = editDate.getText().toString();

                if (!category.isEmpty() && !date.isEmpty()) {
                    dbHelper.updateExpense(selectedExpenseId, amount, category, date);
                    loadExpenses();
                    Toast.makeText(this, "Update Successful !", Toast.LENGTH_SHORT).show();
                    clearInputFields();
                }
            }
        });

        // Xoá Expense (với AlertDialog xác nhận)
        btnDelete.setOnClickListener(v -> {
            if (selectedExpenseId != -1) {
                showDeleteConfirmationDialog();
            }
        });

        // Xử lý sự kiện khi chọn Expense từ ListView
        listViewExpenses.setOnItemClickListener((parent, view, position, id) -> {
            Expense expense = expenses.get(position);
            selectedExpenseId = expense.getId();
            editAmount.setText(String.valueOf(expense.getAmount()));
            editCategory.setText(expense.getCategory());
            editDate.setText(expense.getDate());
        });

        BottomNavigationView bottomNavigation = findViewById(R.id.bottomNavigation);
        bottomNavigation.setSelectedItemId(R.id.navigation_expenses);
        bottomNavigation.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.navigation_expenses) {
                // Hiện đang ở Manage Expense , giữ nguyên
                return true;
            } else if (item.getItemId() == R.id.navigation_home) {
                // Chuyển đến HomeScreen
                startActivity(new Intent(ManageExpenseActivity.this, HomeScreen.class));
                return true;
            } else if (item.getItemId() == R.id.navigation_profile) {
                // Chuyển sang Activity Profile
                startActivity(new Intent(ManageExpenseActivity.this, Profile.class));
                return true;
            } else if (item.getItemId() == R.id.navigation_ChiPhi) {
                // Chuyển sang Activity ChiPhi
                startActivity(new Intent(ManageExpenseActivity.this, RecurringExpenseActivity.class));
                return true;
            }
            return false;
        });

    }

    // Hiển thị DatePickerDialog
    private void showDatePickerDialog() {
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
    }

    // Hiển thị AlertDialog để xác nhận xoá
    private void showDeleteConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Confirmation")
                .setMessage("Are you sure you want to delete this expense?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    // Xoá Expense
                    dbHelper.deleteExpense(selectedExpenseId);
                    Toast.makeText(this, "Delete Successful !", Toast.LENGTH_SHORT).show();
                    loadExpenses();
                    clearInputFields();
                })
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss()) // Không làm gì
                .show();
    }

    // Load dữ liệu từ database vào ListView
    private void loadExpenses() {
        List<String[]> rawExpenses = dbHelper.getExpenses();
        expenses = new ArrayList<>();
        for (String[] e : rawExpenses) {
            expenses.add(new Expense(
                    Integer.parseInt(e[0]), // Expense ID
                    Double.parseDouble(e[1]), // Amount
                    e[2], // Category
                    e[3]  // Date
            ));
        }
        adapter = new ExpenseAdapter(this, expenses);
        listViewExpenses.setAdapter(adapter);
    }

    // Xoá dữ liệu trong các trường nhập liệu
    private void clearInputFields() {
        editAmount.setText("");
        editCategory.setText("");
        editDate.setText("");
        selectedExpenseId = -1;
    }
}
