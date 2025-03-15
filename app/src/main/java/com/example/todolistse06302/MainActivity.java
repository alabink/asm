package com.example.todolistse06302;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {
    private EditText editMail, editPassword;
    private Button btnLogin, btnRegister;
    private TextView txtError;
    private FirebaseAuth mAuth;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.login);

        mAuth = FirebaseAuth.getInstance();
        sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);

        editMail = findViewById(R.id.editEmailRegister);
        editPassword = findViewById(R.id.editPasswordRegister);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnSubmitRegister); // Thêm button đăng ký
        txtError = findViewById(R.id.txtError);

        // Kiểm tra nếu đã đăng nhập trước đó
        if (sharedPreferences.getBoolean("isLoggedIn", false)) {
            txtError.setText("You're already logged in!");
            txtError.setVisibility(View.VISIBLE);
        }

        btnLogin.setOnClickListener(view -> loginUser());

        // Sự kiện nhấn vào nút Register
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

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putBoolean("isLoggedIn", true);
                        editor.apply();

                        Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show();
                        txtError.setText("Login successful");
                        txtError.setVisibility(View.VISIBLE);

                        // Chuyển sang màn hình Home
                        GoToHome();
                    } else {
                        Toast.makeText(this, "Login failed", Toast.LENGTH_SHORT).show();
                        txtError.setText("Login failed");
                        txtError.setVisibility(View.VISIBLE);
                    }
                });
    }

    private void GoToHome() {
        Intent intent = new Intent(this, HomeScreen.class);
        startActivity(intent);
        finish();
    }
}
