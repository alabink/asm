package com.example.todolistse06302;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SummaryAdapter extends BaseAdapter {
    private Context context;
    private List<Map.Entry<String, Double>> data;

    public SummaryAdapter(Context context, Map<String, Double> summaryData) {
        this.context = context;
        this.data = new ArrayList<>(summaryData.entrySet());
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.summary_item, parent, false);
        }

        TextView txtCategory = convertView.findViewById(R.id.txtCategory);
        TextView txtAmount = convertView.findViewById(R.id.txtAmount);

        Map.Entry<String, Double> entry = data.get(position);
        txtCategory.setText(entry.getKey());
        txtAmount.setText(entry.getValue() + " VND");

        return convertView;
    }
}
