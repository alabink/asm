package com.example.todolistse06302;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import java.text.NumberFormat;
import java.util.Locale;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.graphics.Color;
import java.text.DecimalFormat;
import java.util.List;
import android.text.SpannableString;

public class RecurringExpenseAdapter extends BaseAdapter {
    private Context context;
    private List<RecurringExpense> RecurringExpense;

    public RecurringExpenseAdapter(Context context, List<RecurringExpense> RecurringExpense) {
        this.context = context;
        this.RecurringExpense = RecurringExpense;
    }

    @Override
    public int getCount() {
        return RecurringExpense.size();
    }

    @Override
    public Object getItem(int i) {
        return RecurringExpense.get(i);
    }

    @Override
    public long getItemId(int i) {
        return RecurringExpense.get(i).getId();
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.item_recurring_cost, viewGroup, false);
        }

        RecurringExpense recurringexpense = RecurringExpense.get(i);

        TextView tvCostName = view.findViewById(R.id.tvCostName);
        TextView tvRecurringAmount = view.findViewById(R.id.tvRecurringAmount);
        TextView tvStartDate = view.findViewById(R.id.tvStartDate);
        TextView tvEndDate = view.findViewById(R.id.tvEndDate);

        tvCostName.setText(recurringexpense.getName());

        DecimalFormat decimalFormat = new DecimalFormat("#,###");
        String formattedAmount = decimalFormat.format(recurringexpense.getAmount()) + " VND";
        tvRecurringAmount.setText(formattedAmount);
        SpannableStringBuilder styledAmount = new SpannableStringBuilder("Amount: " + formattedAmount);
        styledAmount.setSpan(new ForegroundColorSpan(Color.BLUE), 8, styledAmount.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        tvRecurringAmount.setText(styledAmount);


        // Start Date
        String startDateText = "StartDate: " + recurringexpense.getStartDate();
        SpannableString spannableStart = new SpannableString(startDateText);
        spannableStart.setSpan(new ForegroundColorSpan(Color.BLUE),
                startDateText.indexOf(recurringexpense.getStartDate()),
                startDateText.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        tvStartDate.setText(spannableStart);

        // End Date
        String endDateText = "EndDate: " + recurringexpense.getEndDate();
        SpannableString spannableEnd = new SpannableString(endDateText);
        spannableEnd.setSpan(new ForegroundColorSpan(Color.BLUE),
                endDateText.indexOf(recurringexpense.getEndDate()),
                endDateText.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        tvEndDate.setText(spannableEnd);


        return view;
    }
}
