package com.example.todolistse06302;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.todolistse06302.database.DatabaseHelper;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class Register extends AppCompatActivity {
    private EditText editEmailRegister, editPasswordRegister;
    private Button btnSubmitRegister;
    private DatabaseHelper databaseHelper;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        databaseHelper = new DatabaseHelper(this);

        editEmailRegister = findViewById(R.id.editEmailRegister);
        editPasswordRegister = findViewById(R.id.editPasswordRegister);
        btnSubmitRegister = findViewById(R.id.btnSubmitRegister);

        btnSubmitRegister.setOnClickListener(v -> registerUser());
        Button btnShowUsers = findViewById(R.id.btnShowUsers);
        btnShowUsers.setOnClickListener(v -> {
            Intent intent = new Intent(this,ShowUsersActivity.class);
            startActivity(intent);
        });
    }

    private void registerUser() {
        String email = editEmailRegister.getText().toString().trim();
        String password = editPasswordRegister.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }


        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Đăng kí firebase thành công , thêm vào SQLite
                            addUserToLocalDatabase(email, password);
                        } else {
                            // Nếu lỗi , thử SQLite
                            addUserToLocalDatabase(email, password);
                        }
                    }
                });
    }

    private void addUserToLocalDatabase(String email, String password) {
        try {
            // Đăng kí người dùng role mặc định là user
            long userId = databaseHelper.addUser(email, password);
            if (userId != -1) {
                Toast.makeText(this, "Registration successful", Toast.LENGTH_SHORT).show();
                finish(); // trở về đăng nhập

            } else {
                Toast.makeText(this, "Registration failed in local database", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Registration failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.d("test","abc");
        }
    }
}