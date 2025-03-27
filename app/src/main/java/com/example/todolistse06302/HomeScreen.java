package com.example.todolistse06302;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class HomeScreen extends AppCompatActivity {
    private Button btnManageExpenses, btncost,btnreport;
    private FirebaseAuth mAuth;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home_screen);

        mAuth = FirebaseAuth.getInstance();
        sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);

        btnManageExpenses = findViewById(R.id.btnManageExpenses);
        btncost = findViewById(R.id.btncost); // Tìm button btncost
        btnreport = findViewById(R.id.btnreport);

        // Chuyển đến ManageExpenseActivity khi nhấn btnManageExpenses
        btnManageExpenses.setOnClickListener(view -> {
            Intent intent = new Intent(HomeScreen.this, ManageExpenseActivity.class);
            startActivity(intent);
        });


        // Chuyển đến DashboardActivity khi nhấn btncost
        btncost.setOnClickListener(view -> {
            Intent intent = new Intent(HomeScreen.this, DashboardActivity.class);
            startActivity(intent);
        });

        btnreport.setOnClickListener(view -> {
            Intent intent = new Intent(HomeScreen.this, Report.class);
            startActivity(intent);
        });


        // Xử lý sự kiện cho BottomNavigationView
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.navigation_home) {
                return true;
            } else if (item.getItemId() == R.id.navigation_expenses) {
                startActivity(new Intent(HomeScreen.this, ManageExpenseActivity.class));
                return true;
            } else if (item.getItemId() == R.id.navigation_profile) {
                startActivity(new Intent(HomeScreen.this, Profile.class));
                return true;
            }
            return false;
        });
    }
}
