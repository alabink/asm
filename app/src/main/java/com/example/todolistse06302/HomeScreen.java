package com.example.todolistse06302;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View;
import android.widget.LinearLayout;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.todolistse06302.database.DatabaseHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;


<<<<<<< HEAD
public class HomeScreen extends AppCompatActivity {

    private MaterialButton btnManageExpense, btnLogout, btnReport;
=======
    private MaterialButton btnManageExpense, btnLogout, btnReport,btncost;

>>>>>>> 21def786ec3bd78acad55c194a776b0644ce4088

    private FirebaseAuth mAuth;
    private SharedPreferences sharedPreferences;
    private TextView tvExpiringExpenses;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_home_screen);

        mAuth = FirebaseAuth.getInstance();
        sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);

        btnManageExpense = findViewById(R.id.btnManageExpense);
        btnLogout = findViewById(R.id.btnLogout);
<<<<<<< HEAD
=======

        btncost = findViewById(R.id.btncost);



>>>>>>> 21def786ec3bd78acad55c194a776b0644ce4088
        btnReport = findViewById(R.id.btnreport);

        btnReport.setOnClickListener(view -> {
            Intent intent = new Intent(HomeScreen.this, Report.class);
            startActivity(intent);
        });

        btnManageExpense.setOnClickListener(view -> {
            Intent intent = new Intent(HomeScreen.this, ManageExpenseActivity.class);
            startActivity(intent);
        });

        btncost.setOnClickListener(view -> {
            Intent intent = new Intent(HomeScreen.this, CostOverview.class);
            startActivity(intent);
        });



        btnLogout.setOnClickListener(v -> showLogoutConfirmation());
        tvExpiringExpenses = findViewById(R.id.tvExpiringExpenses);
        checkExpiringRecurringExpenses();
        // Xử lý sự kiện cho BottomNavigationView
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.navigation_home) {// Hiện tại đã ở Home, không cần làm gì

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
            }else if (itemId == R.id.navigation_ChiPhi) {
                // Chuyển đến Activity RecurringExpenseActivity
                startActivity(new Intent(HomeScreen.this, RecurringExpenseActivity.class));
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
<<<<<<< HEAD
=======


>>>>>>> 21def786ec3bd78acad55c194a776b0644ce4088
                    // Return to login screen

                    Intent intent = new Intent(HomeScreen.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("No", null)
                .show();
    }
    private int getCurrentUserId() {
        return sharedPreferences.getInt("userId", -1);
    }

    private void checkExpiringRecurringExpenses() {
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        int userId = getCurrentUserId();

        List<RecurringExpense> recurringExpenses = dbHelper.getRecurringExpenses(userId);

        TextView tvExpiringExpenses = findViewById(R.id.tvExpiringExpenses);
        LinearLayout layoutExpiringExpenses = findViewById(R.id.layoutExpiringExpenses);

        StringBuilder expiringText = new StringBuilder("Notification End Recurring Expense\n");

        Calendar today = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        boolean hasExpiringExpenses = false;

        for (RecurringExpense expense : recurringExpenses) {
            try {
                Date endDate = sdf.parse(expense.getEndDate());

                if (endDate != null) {
                    long diff = endDate.getTime() - today.getTimeInMillis();
                    long daysRemaining = TimeUnit.MILLISECONDS.toDays(diff);

                    if (daysRemaining >= 0 && daysRemaining <= 3) {
                        expiringText.append(expense.getName())
                                .append(" - Expires on: ")
                                .append(expense.getEndDate())
                                .append("\n");
                        hasExpiringExpenses = true;
                    }
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        if (hasExpiringExpenses) {
            tvExpiringExpenses.setText(expiringText.toString().trim());
            layoutExpiringExpenses.setVisibility(View.VISIBLE);
        } else {
            tvExpiringExpenses.setText("No upcoming expenses");
            layoutExpiringExpenses.setVisibility(View.VISIBLE);
        }
    }
}

