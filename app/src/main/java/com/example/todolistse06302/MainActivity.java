package com.example.todolistse06302;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.todolistse06302.database.DatabaseHelper;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {
    private EditText editMail, editPassword;
    private Button btnLogin, btnRegister;
    private TextView txtError;
    private DatabaseHelper dbHelper;
    private SharedPreferences sharedPreferences;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        
        dbHelper = new DatabaseHelper(this);
        sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);

        // Clear any existing login session
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();

        // Sign out from Firebase
        if (mAuth.getCurrentUser() != null) {
            mAuth.signOut();
        }

        editMail = findViewById(R.id.editEmailRegister);
        editPassword = findViewById(R.id.editPasswordRegister);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnSubmitRegister);
        txtError = findViewById(R.id.txtError);

        btnLogin.setOnClickListener(view -> loginUser());
        btnRegister.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, Register.class);
            startActivity(intent);
        });
    }

    private void loginUser() {
        String email = editMail.getText().toString().trim();
        String password = editPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            txtError.setText("Please enter email and password!");
            txtError.setVisibility(View.VISIBLE);
            return;
        }

        // First check SQLite database
        checkLocalDatabase(email, password, true);
    }

    private void checkLocalDatabase(String email, String password, boolean tryFirebaseNext) {
        try {
            // Query the database for user
            Cursor cursor = dbHelper.getReadableDatabase().query(
                DatabaseHelper.TABLE_USERS,
                new String[]{DatabaseHelper.COLUMN_USER_ID, DatabaseHelper.COLUMN_USER_ROLE},
                DatabaseHelper.COLUMN_EMAIL + "=? AND " + DatabaseHelper.COLUMN_PASSWORD + "=?",
                new String[]{email, password},
                null, null, null
            );

            if (cursor != null && cursor.moveToFirst()) {
                // Local login successful
                handleSuccessfulLogin(cursor);
            } else {
                if (tryFirebaseNext) {
                    // If not found in SQLite, try Firebase
                    tryFirebaseLogin(email, password);
                } else {
                    txtError.setText("Invalid email or password");
                    txtError.setVisibility(View.VISIBLE);
                }
            }

            if (cursor != null) {
                cursor.close();
            }
        } catch (Exception e) {
            txtError.setText("Login error: " + e.getMessage());
            txtError.setVisibility(View.VISIBLE);
        }
    }

    private void tryFirebaseLogin(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Firebase login successful
                        // Create user in SQLite if not exists
                        createUserInSQLiteIfNeeded(email, password);
                    } else {
                        // Both SQLite and Firebase failed
                        txtError.setText("Invalid email or password");
                        txtError.setVisibility(View.VISIBLE);
                    }
                });
    }

    private void createUserInSQLiteIfNeeded(String email, String password) {
        try {
            // Check if user exists in SQLite
            Cursor cursor = dbHelper.getReadableDatabase().query(
                DatabaseHelper.TABLE_USERS,
                new String[]{DatabaseHelper.COLUMN_USER_ID},
                DatabaseHelper.COLUMN_EMAIL + "=?",
                new String[]{email},
                null, null, null
            );

            if (cursor != null && cursor.moveToFirst()) {
                // User exists, just login
                handleSuccessfulLogin(cursor);
            } else {
                // User doesn't exist in SQLite, create new
                long userId = dbHelper.addUser(email, password);
                if (userId != -1) {
                    // Save login session
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean("isLoggedIn", true);
                    editor.putInt("userId", (int)userId);
                    editor.putString("userRole", DatabaseHelper.ROLE_USER); // New Firebase users get USER role
                    editor.apply();

                    Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show();
                    goToHome();
                } else {
                    txtError.setText("Error creating local user");
                    txtError.setVisibility(View.VISIBLE);
                }
            }

            if (cursor != null) {
                cursor.close();
            }
        } catch (Exception e) {
            txtError.setText("Error: " + e.getMessage());
            txtError.setVisibility(View.VISIBLE);
        }
    }

    private void handleSuccessfulLogin(Cursor cursor) {
        int userIdColumnIndex = cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_ID);
        int userRoleColumnIndex = cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_ROLE);

        int userId = cursor.getInt(userIdColumnIndex);
        String userRole = cursor.getString(userRoleColumnIndex);

        // Save login session
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("isLoggedIn", true);
        editor.putInt("userId", userId);
        editor.putString("userRole", userRole);
        editor.apply();

        Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show();

        // Redirect based on role
        if (DatabaseHelper.ROLE_ADMIN.equals(userRole)) {
            goToAdminPanel();
        } else {
            goToHome();
        }
    }

    private void goToHome() {
        Intent intent = new Intent(MainActivity.this, HomeScreen.class);
        startActivity(intent);
        finish();
    }

    private void goToAdminPanel() {
        Intent intent = new Intent(MainActivity.this, AdminDashboard.class);
        startActivity(intent);
        finish();
    }
}

