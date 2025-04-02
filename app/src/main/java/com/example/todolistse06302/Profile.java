package com.example.todolistse06302;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.todolistse06302.database.DatabaseHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;

public class Profile extends AppCompatActivity {

    private MaterialButton btnLogout, btnManageExpense, btnManageBudget;
    private TextView txtUserEmail;
    private FirebaseAuth mAuth;
    private SharedPreferences sharedPreferences;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        dbHelper = new DatabaseHelper(this);

        txtUserEmail = findViewById(R.id.txtUserEmail);
        btnLogout = findViewById(R.id.btnLogout);
        btnManageExpense = findViewById(R.id.btnManageExpense);
        btnManageBudget = findViewById(R.id.btnManageBudget);

        // Lấy userId từ SharedPreferences
        int userId = sharedPreferences.getInt("userId", -1);

        // Nếu có userId, tìm và hiển thị email
        if (userId != -1) {
            try {
                // Lấy cursor chứa thông tin người dùng
                Cursor cursor = dbHelper.getReadableDatabase().query(
                        DatabaseHelper.TABLE_USERS,
                        new String[]{DatabaseHelper.COLUMN_EMAIL},
                        DatabaseHelper.COLUMN_USER_ID + "=?",
                        new String[]{String.valueOf(userId)},
                        null, null, null
                );

                if (cursor != null && cursor.moveToFirst()) {
                    int emailColumnIndex = cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_EMAIL);
                    String userEmail = cursor.getString(emailColumnIndex);
                    txtUserEmail.setText("Welcome, " + userEmail);
                    cursor.close();
                }
            } catch (Exception e) {
                Log.e("Profile", "Error getting user email", e);
            }
        }

        btnLogout.setOnClickListener(view -> showLogoutConfirmation());

        btnManageExpense.setOnClickListener(v -> {
            startActivity(new Intent(Profile.this, ManageExpenseActivity.class));
        });

        btnManageBudget.setOnClickListener(v -> {
            startActivity(new Intent(Profile.this, ManageBudgetActivity.class));
        });

        BottomNavigationView bottomNavigation = findViewById(R.id.bottomNavigation);
        bottomNavigation.setSelectedItemId(R.id.navigation_profile);
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_profile) {
                // Hiện tại đã ở Profile, không cần làm gì
                return true;
            } else if (itemId == R.id.navigation_expenses) {
                // Chuyển đến Activity Manage Expense
                startActivity(new Intent(Profile.this, ManageExpenseActivity.class));
                return true;
            } else if (itemId == R.id.navigation_home) {
                // Chuyển đến Activity HomeScreen khi user click vào
                startActivity(new Intent(Profile.this, HomeScreen.class));
                return true;
            } else if (itemId == R.id.navigation_budget) {
                // Chuyển đến Activity ManageBudgetActivity
                startActivity(new Intent(Profile.this, ManageBudgetActivity.class));
                return true;
            }
            return false;
        });
    }

    private void showLogoutConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Logout Confirmation")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", (dialog, which) -> logoutUser())
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void logoutUser() {
        mAuth.signOut();
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("isLoggedIn", false);
        editor.apply();
        Intent intent = new Intent(Profile.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}

