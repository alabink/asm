package com.example.todolistse06302;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import com.example.todolistse06302.database.DatabaseHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import android.Manifest;
import android.content.pm.PackageManager;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

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
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        checkExpiringRecurringExpenses();

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
    private void checkExpiringRecurringExpenses() {
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        List<RecurringExpense> recurringExpenses = dbHelper.getRecurringExpenses();
        TextView tvExpiringExpenses = findViewById(R.id.tvExpiringExpenses);

        StringBuilder expiringText = new StringBuilder();
        Calendar today = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        for (RecurringExpense expense : recurringExpenses) {
            try {
                Date endDate = sdf.parse(expense.getEndDate());

                if (endDate != null) {
                    long diff = endDate.getTime() - today.getTimeInMillis();
                    long daysRemaining = TimeUnit.MILLISECONDS.toDays(diff);

                    if (daysRemaining >= 0 && daysRemaining <= 3) { // Nếu còn 3 ngày hoặc ít hơn
                        expiringText.append("⚠ ")
                                .append(expense.getName())
                                .append(" - Expires on: ")
                                .append(expense.getEndDate())
                                .append("\n");
                    }
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        if (expiringText.length() > 0) {
            tvExpiringExpenses.setText(expiringText.toString().trim());
            tvExpiringExpenses.setTextColor(Color.RED);
            tvExpiringExpenses.setVisibility(View.VISIBLE);
        } else {
            tvExpiringExpenses.setVisibility(View.GONE);
        }
    }


}
