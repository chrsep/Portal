package com.directdev.portal.ui.finance;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.directdev.portal.R;
import com.directdev.portal.tools.datatype.FinanceData;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class FinanceRecyclerAdapter extends RecyclerView.Adapter {
    private List<FinanceData> data;

    public FinanceRecyclerAdapter(List<FinanceData> data) {
        this.data = data;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int i) {
        FinanceViewHolder FinanceViewHolder = (FinanceViewHolder) holder;
        String amount;
        if (data.get(i).type.equals("P")) {
            amount = NumberFormat.getNumberInstance(Locale.US).format(data.get(i).amount);
            FinanceViewHolder.amount.setText("Rp. " + amount.substring(1));
            FinanceViewHolder.date.setText("Payed: " + data.get(i).date);
        } else {
            amount = NumberFormat.getNumberInstance(Locale.US).format(data.get(i).amount);
            FinanceViewHolder.amount.setText("Rp. " + amount);
            FinanceViewHolder.date.setText("Due: " + data.get(i).date);

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            Date dueDate;
            Date today = new Date();
            try {
                dueDate = dateFormat.parse(data.get(i).date);
                if (dueDate.after(today)) {
                    FinanceViewHolder.upcoming.setVisibility(View.VISIBLE);
                    FinanceViewHolder.passed.setVisibility(View.INVISIBLE);
                } else {
                    FinanceViewHolder.upcoming.setVisibility(View.INVISIBLE);
                    FinanceViewHolder.passed.setVisibility(View.VISIBLE);
                }
            } catch (ParseException e) {
                FinanceViewHolder.upcoming.setVisibility(View.INVISIBLE);
                FinanceViewHolder.passed.setVisibility(View.INVISIBLE);
            }
        }
        FinanceViewHolder.description.setText(data.get(i).description);
    }

    @Override
    public int getItemCount() {
        try {
            return data.size();
        } catch (NullPointerException e) {
            return 0;
        }
    }

    @Override
    public FinanceViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_finance, parent, false);
        return new FinanceViewHolder(v);
    }

    public static class FinanceViewHolder extends RecyclerView.ViewHolder {
        TextView amount;
        TextView date;
        TextView description;
        TextView upcoming;
        TextView passed;

        FinanceViewHolder(View itemView) {
            super(itemView);
            amount = (TextView) itemView.findViewById(R.id.finance_amount);
            date = (TextView) itemView.findViewById(R.id.finance_date);
            description = (TextView) itemView.findViewById(R.id.finance_description);
            upcoming = (TextView) itemView.findViewById(R.id.finance_upcoming);
            passed = (TextView) itemView.findViewById(R.id.finance_passed);
        }
    }
}
