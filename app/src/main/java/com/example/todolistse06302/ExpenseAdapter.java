package com.example.todolistse06302;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

public class ExpenseAdapter extends BaseAdapter {
    private Context context;
    private List<Expense> expenses;

    public ExpenseAdapter(Context context, List<Expense> expenses) {
        this.context = context;
        this.expenses = expenses;
    }

    @Override
    public int getCount() {
        return expenses.size();
    }

    @Override
    public Object getItem(int i) {
        return expenses.get(i);
    }

    @Override
    public long getItemId(int i) {
        return expenses.get(i).getId();
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.expense_item, viewGroup, false);
        }

        Expense expense = expenses.get(i);

        TextView txtAmount = view.findViewById(R.id.txtAmount);
        TextView txtCategory = view.findViewById(R.id.txtCategory);
        TextView txtDate = view.findViewById(R.id.txtDate);

        txtAmount.setText(String.valueOf(expense.getAmount()));
        txtCategory.setText(expense.getCategory());
        txtDate.setText(expense.getDate());

        return view;
    }
}
