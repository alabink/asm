package com.example.todolistse06302;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.todolistse06302.database.DatabaseHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class AdminPanelProfile extends AppCompatActivity {
    private MaterialButton btnManageUser, btnViewUserExpenses, btnLogout;
    private TextView welcomeUser;
    private FirebaseAuth mAuth;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_panel_profile);
        mAuth = FirebaseAuth.getInstance();
        sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        welcomeUser = findViewById(R.id.welcomeUser);
        displayUserEmail();
        btnManageUser = findViewById(R.id.btnManageUsers);
        btnManageUser.setOnClickListener(v -> {
            startActivity(new Intent(AdminPanelProfile.this, AdminPanelActivity.class));
        });
        btnViewUserExpenses = findViewById(R.id.btnViewUserExpenses);
        btnLogout = findViewById(R.id.btnLogout);

        btnLogout.setOnClickListener(v -> showLogoutConfirmation());

        // Xử lý sự kiện cho BottomNavigationView
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setSelectedItemId(R.id.navigation_profile);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_profile) {
                // Hiện tại đã ở Profile, không cần làm gì
                return true;
            } else if (itemId == R.id.navigation_manage_users) {
                // Chuyển đến Activity Manage Expense
                startActivity(new Intent(AdminPanelProfile.this, AdminPanelActivity.class));
                return true;
            } else if (itemId == R.id.navigation_home) {
                // Chuyển đến Activity Home khi user click vào
                startActivity(new Intent(AdminPanelProfile.this, AdminDashboard.class));
                return true;
            } else if (itemId == R.id.navigation_view_users_expenses) {
                // Chuyển đến Activity ManageBudgetActivity
                startActivity(new Intent(AdminPanelProfile.this, AdminDashboard.class));
                return true;
            }
            return false;
        });
    }
    //test

    // Phương thức hiển thị email
    private void displayUserEmail() {
        // Thử lấy email từ Firebase trước
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser != null) {
            String email = firebaseUser.getEmail();
            if (email != null) {
                welcomeUser.setText("Welcome, " + email);
                return;
            }
        }

        // Nếu không có từ Firebase, thử lấy từ SharedPreferences
        int userId = sharedPreferences.getInt("userId", -1);
        if (userId != -1) {
            try {
                DatabaseHelper dbHelper = new DatabaseHelper(this);
                Cursor cursor = dbHelper.getReadableDatabase().query(
                        DatabaseHelper.TABLE_USERS,
                        new String[]{DatabaseHelper.COLUMN_EMAIL},
                        DatabaseHelper.COLUMN_USER_ID + " = ?",
                        new String[]{String.valueOf(userId)},
                        null, null, null
                );

                if (cursor != null && cursor.moveToFirst()) {
                    String email = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_EMAIL));
                    welcomeUser.setText("Welcome, " + email);
                    cursor.close();
                }
            } catch (Exception e) {
                Log.e("AdminDashboard", "Error fetching user email", e);
                welcomeUser.setText("Welcome, Admin");
            }
        } else {
            // Nếu không tìm thấy, quay về màn hình đăng nhập
            Intent intent = new Intent(AdminPanelProfile.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
    }

    // ✅ Di chuyển phương thức ra ngoài
    private void showLogoutConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    // Xóa SharedPreferences
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.clear();
                    editor.apply();

                    // Đăng xuất Firebase
                    if (mAuth.getCurrentUser() != null) {
                        mAuth.signOut();
                    }

                    // Chuyển về màn hình đăng nhập
                    Intent intent = new Intent(AdminPanelProfile.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("No", null)
                .show();
    }
}