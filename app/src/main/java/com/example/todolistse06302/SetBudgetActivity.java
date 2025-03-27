package com.example.todolistse06302;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.todolistse06302.database.DatabaseHelper;
import java.util.List;

public class SetBudgetActivity extends AppCompatActivity {
    private EditText editBudgetCategory, editBudgetAmount;
    private Button btnSetBudget;
    private RecyclerView rvBudgets;
    private BudgetAdapter budgetAdapter;
    private DatabaseHelper dbHelper;
    private List<String[]> budgetList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_budget);

        // Khởi tạo database
        dbHelper = new DatabaseHelper(this);

        // XÓA TOÀN BỘ NGÂN SÁCH KHI MỞ APP
        dbHelper.clearAllBudgets();

        // Ánh xạ view
        editBudgetCategory = findViewById(R.id.editBudgetCategory);
        editBudgetAmount = findViewById(R.id.editBudgetAmount);
        btnSetBudget = findViewById(R.id.btnSetBudget);
        rvBudgets = findViewById(R.id.rvBudgets);

        // Cấu hình RecyclerView
        budgetList = dbHelper.getAllBudgets();
        budgetAdapter = new BudgetAdapter(budgetList);
        rvBudgets.setLayoutManager(new LinearLayoutManager(this));
        rvBudgets.setAdapter(budgetAdapter);

        // Xử lý sự kiện khi bấm nút Set Budget
        btnSetBudget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String category = editBudgetCategory.getText().toString().trim();
                String amountStr = editBudgetAmount.getText().toString().trim();

                if (category.isEmpty() || amountStr.isEmpty()) {
                    Toast.makeText(SetBudgetActivity.this, "Vui lòng nhập đủ thông tin!", Toast.LENGTH_SHORT).show();
                    return;
                }

                try {
                    double amount = Double.parseDouble(amountStr);
                    dbHelper.setBudget(category, amount);
                    Toast.makeText(SetBudgetActivity.this, "Đã đặt ngân sách thành công!", Toast.LENGTH_SHORT).show();

                    // Cập nhật danh sách ngân sách
                    budgetList.clear();
                    budgetList.addAll(dbHelper.getAllBudgets());
                    budgetAdapter.notifyDataSetChanged();
                } catch (NumberFormatException e) {
                    Toast.makeText(SetBudgetActivity.this, "Số tiền không hợp lệ!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
