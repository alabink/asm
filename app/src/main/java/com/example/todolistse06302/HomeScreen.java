package com.example.todolistse06302;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class HomeScreen extends AppCompatActivity {
    private Button btnManageExpenses;
    private FirebaseAuth mAuth;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_home_screen);

        mAuth = FirebaseAuth.getInstance();
        sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);

        btnManageExpenses = findViewById(R.id.btnManageExpenses);


        btnManageExpenses.setOnClickListener(view -> {
            Intent intent = new Intent(HomeScreen.this, ManageExpenseActivity.class);
            startActivity(intent);
        });

        // Xử lý sự kiện cho BottomNavigationView
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.navigation_home) {
                // Hiện tại đã ở Home, không cần làm gì
                return true;
            } else if (item.getItemId() == R.id.navigation_expenses) {
                // Chuyển đến Activity Manage Expense
                startActivity(new Intent(HomeScreen.this, ManageExpenseActivity.class));
                return true;
            }else if (item.getItemId() == R.id.navigation_ChiPhi) {
                // Chuyển sang Activity ChiPhi
                startActivity(new Intent(HomeScreen.this, RecurringExpenseActivity.class));
                return true;
            } else if (item.getItemId() == R.id.navigation_profile) {
                // Chuyển đến Activity Profile khi user click vào
                startActivity(new Intent(HomeScreen.this, Profile.class));
                return true;
            }
            return false;
        });
    }
}
