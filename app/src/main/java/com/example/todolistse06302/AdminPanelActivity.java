package com.example.todolistse06302;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.todolistse06302.database.DatabaseHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

public class AdminPanelActivity extends AppCompatActivity {
    private DatabaseHelper dbHelper;
    private ListView userListView;
    private List<UserItem> userList;
    private UserAdapter adapter;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_panel);

        dbHelper = new DatabaseHelper(this);
        userListView = findViewById(R.id.userListView);
        userList = new ArrayList<>();
        adapter = new UserAdapter();
        userListView.setAdapter(adapter);
        loadUsers();

        bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setSelectedItemId(R.id.navigation_manage_users);

        // Xử lý sự kiện cho BottomNavigationView
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_home) {
                // Chuyển đến màn hình Home
                startActivity(new Intent(AdminPanelActivity.this, AdminDashboard.class));
                return true;
            } else if (itemId == R.id.navigation_view_users_expenses) {
                // Chuyển đến màn hình xem chi tiêu của người dùng
                startActivity(new Intent(AdminPanelActivity.this, AdminDashboard.class));
                return true;
            } else if (itemId == R.id.navigation_profile) {
                // Chuyển đến màn hình hồ sơ
                startActivity(new Intent(AdminPanelActivity.this, AdminDashboard.class));
                return true;
            }
            return false;
        });


    }


    private void loadUsers() {
        userList.clear();
        Cursor cursor = dbHelper.getAllUsers();

        if (cursor != null && cursor.moveToFirst()) {
            do {
                // Get column indices
                int idColumnIndex = cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_ID);
                int emailColumnIndex = cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_EMAIL);
                int roleColumnIndex = cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_ROLE);

                // Get values using indices
                int id = cursor.getInt(idColumnIndex);
                String email = cursor.getString(emailColumnIndex);
                String role = cursor.getString(roleColumnIndex);

                userList.add(new UserItem(id, email, role));
            } while (cursor.moveToNext());
            cursor.close();
        }

        adapter.notifyDataSetChanged();
    }

    private class UserItem {
        int id;
        String email;
        String role;

        UserItem(int id, String email, String role) {
            this.id = id;
            this.email = email;
            this.role = role;
        }
    }

    private class UserAdapter extends ArrayAdapter<UserItem> {
        UserAdapter() {
            super(AdminPanelActivity.this, R.layout.user_list_item, userList);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext())
                        .inflate(R.layout.user_list_item, parent, false);
            }

            UserItem user = userList.get(position);

            TextView emailText = convertView.findViewById(R.id.userEmail);
            TextView roleText = convertView.findViewById(R.id.userRole);
            Button editButton = convertView.findViewById(R.id.editButton);
            Button deleteButton = convertView.findViewById(R.id.deleteButton);

            emailText.setText(user.email);
            roleText.setText(user.role);

            editButton.setOnClickListener(v -> showEditRoleDialog(user));
            deleteButton.setOnClickListener(v -> showDeleteConfirmation(user));

            return convertView;
        }
    }

    private void showEditRoleDialog(UserItem user) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_edit_role, null);
        Spinner roleSpinner = view.findViewById(R.id.roleSpinner);

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                new String[]{DatabaseHelper.ROLE_USER, DatabaseHelper.ROLE_ADMIN});
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        roleSpinner.setAdapter(spinnerAdapter);

        // Set current role
        roleSpinner.setSelection(user.role.equals(DatabaseHelper.ROLE_ADMIN) ? 1 : 0);

        builder.setTitle("Edit User Role")
                .setView(view)
                .setPositiveButton("Save", (dialog, which) -> {
                    String newRole = roleSpinner.getSelectedItem().toString();
                    try {
                        dbHelper.updateUserRole(user.id, newRole);
                        user.role = newRole;
                        adapter.notifyDataSetChanged();
                        Toast.makeText(this, "Role updated successfully", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Toast.makeText(this, "Error updating role: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showDeleteConfirmation(UserItem user) {
        new AlertDialog.Builder(this)
                .setTitle("Delete User")
                .setMessage("Are you sure you want to delete " + user.email + "?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    try {
                        dbHelper.deleteUser(user.id);
                        userList.remove(user);
                        adapter.notifyDataSetChanged();
                        Toast.makeText(this, "User deleted successfully", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Toast.makeText(this, "Error deleting user: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
//dbl code done