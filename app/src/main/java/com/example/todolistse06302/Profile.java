package com.example.todolistse06302;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;

public class Profile extends AppCompatActivity {

    private MaterialButton btnLogout;
    private FirebaseAuth mAuth;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);

        btnLogout = findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(view -> showLogoutConfirmation());

        BottomNavigationView bottomNavigation = findViewById(R.id.bottomNavigation);
        bottomNavigation.setSelectedItemId(R.id.navigation_profile);
        bottomNavigation.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.navigation_profile) {
                // Hiện tại đã ở Profile, không cần làm gì
                return true;
            } else if (item.getItemId() == R.id.navigation_expenses) {
                // Chuyển đến Activity Manage Expense
                startActivity(new Intent(Profile.this, ManageExpenseActivity.class));
                return true;
            } else if (item.getItemId() == R.id.navigation_home) {
                // Chuyển đến Activity HomeScreen khi user click vào
                startActivity(new Intent(Profile.this, HomeScreen.class));
                return true;
            }else if (item.getItemId() == R.id.navigation_ChiPhi) {
                // Chuyển sang Activity ChiPhi
                startActivity(new Intent(Profile.this, RecurringExpenseActivity.class));
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
