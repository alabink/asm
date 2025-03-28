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

        // Load user data
        DatabaseHelper databaseHelper = new DatabaseHelper(this);
        List<String[]> users = databaseHelper.getUsers();

        // Format user data into strings
        String[] userStrings = new String[users.size()];
        for (int i = 0; i < users.size(); i++) {
            userStrings[i] = "Email: " + users.get(i)[0] + "\nPassword: " + users.get(i)[1];
        }

        // Display using ArrayAdapter
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, userStrings);
        listViewUsers.setAdapter(adapter);
    }
}
