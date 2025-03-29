package com.example.todolistse06302;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;

public class HomeScreen extends AppCompatActivity {
    private MaterialButton btnManageExpense, btnLogout;
    private FirebaseAuth mAuth;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_home_screen);

        mAuth = FirebaseAuth.getInstance();
        sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);

        btnManageExpense = findViewById(R.id.btnManageExpense);
        btnLogout = findViewById(R.id.btnLogout);

        btnManageExpense.setOnClickListener(view -> {
            Intent intent = new Intent(HomeScreen.this, ManageExpenseActivity.class);
            startActivity(intent);
        });

        btnLogout.setOnClickListener(v -> showLogoutConfirmation());

        // Xử lý sự kiện cho BottomNavigationView
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_home) {
                // Hiện tại đã ở Home, không cần làm gì
                return true;
            } else if (itemId == R.id.navigation_expenses) {
                // Chuyển đến Activity Manage Expense
                startActivity(new Intent(HomeScreen.this, ManageExpenseActivity.class));
                return true;
            } else if (itemId == R.id.navigation_profile) {
                // Chuyển đến Activity Profile khi user click vào
                startActivity(new Intent(HomeScreen.this, Profile.class));
                return true;
            } else if (itemId == R.id.navigation_budget) {
                // Chuyển đến Activity ManageBudgetActivity
                startActivity(new Intent(HomeScreen.this, ManageBudgetActivity.class));
                return true;
            }
            return false;
        });
    }

    private void showLogoutConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    // Clear SharedPreferences
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.clear();
                    editor.apply();

                    // Sign out from Firebase
                    if (mAuth.getCurrentUser() != null) {
                        mAuth.signOut();
                    }

                    // Return to login screen
                    Intent intent = new Intent(HomeScreen.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("No", null)
                .show();
    }
}

