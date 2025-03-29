package com.example.todolistse06302;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.todolistse06302.database.DatabaseHelper;

import java.util.List;

public class ShowUsersActivity extends AppCompatActivity {

    private ListView listViewUsers;
    private Button btnShowUsers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_users);

        listViewUsers = findViewById(R.id.listViewUsers);

        // Load dữ liệu người dùng
        DatabaseHelper databaseHelper = new DatabaseHelper(this);
        List<String[]> users = databaseHelper.getUsers();

        // Định dạng dữ liệu người dùng thành string
        String[] userStrings = new String[users.size()];
        for (int i = 0; i < users.size(); i++) {
            userStrings[i] = "Email: " + users.get(i)[0] + "\nPassword: " + users.get(i)[1];
        }

        // Hiện thị sử dụng ArrayAdapter
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, userStrings);
        listViewUsers.setAdapter(adapter);
    }
}

//dbl code done
