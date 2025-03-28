package com.example.todolistse06302;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.graphics.Color;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class BudgetAdapter extends BaseAdapter {
    private Context context;
    private List<Budget> budgets;

    public BudgetAdapter(Context context, List<Budget> budgets) {
        this.context = context;
        this.budgets = budgets;
    }

    @Override
    public int getCount() {
        return budgets.size();
    }

    @Override
    public Object getItem(int i) {
        return budgets.get(i);
    }

    @Override
    public long getItemId(int i) {
        return budgets.get(i).getId();
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.budget_item, viewGroup, false);
        }

        Budget budget = budgets.get(i);
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

        TextView txtCategory = view.findViewById(R.id.txtBudgetCategory);
        TextView txtTotalBudget = view.findViewById(R.id.txtTotalBudget);
        TextView txtRemainingBudget = view.findViewById(R.id.txtRemainingBudget);

        txtCategory.setText(budget.getCategory());
        txtTotalBudget.setText(currencyFormat.format(budget.getTotalBudget()));
        txtRemainingBudget.setText(currencyFormat.format(budget.getRemainingBudget()));

        // Thêm màu sắc để chỉ ra mức độ ngân sách còn lại
        if (budget.getRemainingBudget() < budget.getTotalBudget() * 0.2) {
            txtRemainingBudget.setTextColor(Color.RED);
        } else if (budget.getRemainingBudget() < budget.getTotalBudget() * 0.5) {
            txtRemainingBudget.setTextColor(Color.YELLOW);
        } else {
            txtRemainingBudget.setTextColor(Color.GREEN);
        }

        return view;
    }
}