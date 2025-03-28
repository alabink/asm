package com.example.todolistse06302;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.HashMap;

public class SetBudgetActivity extends AppCompatActivity {

    private EditText editBudgetCategory, editBudgetAmount;
    private Button btnSetBudget, btnDeleteBudget;
    private ListView listViewBudgets;

    // HashMap để lưu ngân sách theo danh mục
    private HashMap<String, Integer> budgetMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_budget);

        // Ánh xạ các View
        editBudgetCategory = findViewById(R.id.editBudgetCategory);
        editBudgetAmount = findViewById(R.id.editBudgetAmount);
        btnSetBudget = findViewById(R.id.btnSetBudget);
        btnDeleteBudget = findViewById(R.id.btnDeleteBudget);
        listViewBudgets = findViewById(R.id.listViewBudgets);

        // Sự kiện đặt ngân sách
        btnSetBudget.setOnClickListener(view -> setBudget());

        // Sự kiện xóa ngân sách
        btnDeleteBudget.setOnClickListener(view -> deleteBudget());

        // Load danh sách ngân sách hiện có
        loadBudgets();
    }

    private void setBudget() {
        String category = editBudgetCategory.getText().toString().trim();
        String amountStr = editBudgetAmount.getText().toString().trim();

        if (category.isEmpty() || amountStr.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
            return;
        }

        int amount = Integer.parseInt(amountStr);
        budgetMap.put(category, amount); // Lưu ngân sách theo danh mục

        // Cập nhật danh sách
        loadBudgets();
        Toast.makeText(this, "Ngân sách đã được đặt!", Toast.LENGTH_SHORT).show();
    }

    private void deleteBudget() {
        String category = editBudgetCategory.getText().toString().trim();

        if (budgetMap.containsKey(category)) {
            budgetMap.remove(category);
            loadBudgets();
            Toast.makeText(this, "Đã xóa ngân sách!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Không tìm thấy danh mục!", Toast.LENGTH_SHORT).show();
        }
    }

    public void updateBudget(String category, int expenseAmount) {
        if (budgetMap.containsKey(category)) {
            int remainingBudget = budgetMap.get(category) - expenseAmount;
            budgetMap.put(category, Math.max(remainingBudget, 0)); // Đảm bảo không xuống dưới 0
            loadBudgets();
        }
    }

    private void loadBudgets() {
        // Hiển thị danh sách ngân sách (có thể dùng Adapter)
        System.out.println("Cập nhật danh sách ngân sách:");
        for (String category : budgetMap.keySet()) {
            System.out.println(category + " còn " + budgetMap.get(category) + "k");
        }
    }
}
