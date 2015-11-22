package com.directdev.portal.ui.main.account.finance;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.directdev.portal.R;
import com.directdev.portal.tools.model.Finance;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import io.realm.RealmResults;

public class FinanceRecyclerAdapter extends RecyclerView.Adapter {
    private RealmResults<Finance> data;

    public FinanceRecyclerAdapter(RealmResults<Finance> data) {
        this.data = data;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int i) {
        FinanceViewHolder FinanceViewHolder = (FinanceViewHolder) holder;
        Finance finance = data.get(i);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        String amount;
        if (finance.getITEM_TYPE_CD().equals("P")) {
            amount = NumberFormat.getNumberInstance(Locale.US).format(finance.getITEM_AMT());
            FinanceViewHolder.amount.setText("Rp. " + amount.substring(1));
            if(finance.getITEM_EFFECTIVE_DT() == null){
                FinanceViewHolder.date.setText("Due: N/A");
            }else{
                FinanceViewHolder.date.setText("Payed: " + dateFormat.format(finance.getITEM_EFFECTIVE_DT()));
            }
        } else {
            amount = NumberFormat.getNumberInstance(Locale.US).format(finance.getITEM_AMT());
            FinanceViewHolder.amount.setText("Rp. " + amount);
            if(finance.getDUE_DT() == null){
                FinanceViewHolder.date.setText("Due: N/A");
                FinanceViewHolder.upcoming.setVisibility(View.INVISIBLE);
                FinanceViewHolder.passed.setVisibility(View.INVISIBLE);
            }else {
                FinanceViewHolder.date.setText("Due: " + dateFormat.format(finance.getDUE_DT()));
                Date today = new Date();
                if (finance.getDUE_DT().after(today)) {
                    FinanceViewHolder.upcoming.setVisibility(View.VISIBLE);
                    FinanceViewHolder.passed.setVisibility(View.INVISIBLE);
                } else {
                    FinanceViewHolder.upcoming.setVisibility(View.INVISIBLE);
                    FinanceViewHolder.passed.setVisibility(View.VISIBLE);
                }
            }
        }
        FinanceViewHolder.description.setText(finance.getDESCR());
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
