package com.example.todolistse06302;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.graphics.Color;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class ExpenseAdapter extends BaseAdapter {
    private Context context;
    private List<Expense> expenses;
    private NumberFormat currencyFormatter;
    private DecimalFormat decimalFormat;

    public ExpenseAdapter(Context context, List<Expense> expenses) {
        this.context = context;
        this.expenses = expenses;
        // Create currency formatter for VND without scientific notation
        this.currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        this.decimalFormat = new DecimalFormat("#,###");
        decimalFormat.setGroupingUsed(true);
        if (currencyFormatter instanceof DecimalFormat) {
            ((DecimalFormat) currencyFormatter).setDecimalSeparatorAlwaysShown(false);
        }
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

        // Format amount with grouping and VND currency
        String formattedAmount = decimalFormat.format(expense.getAmount()) + " ₫";
        txtAmount.setText(formattedAmount);
        txtCategory.setText(expense.getCategory());
        txtDate.setText(expense.getDate());

        return view;
    }
}

