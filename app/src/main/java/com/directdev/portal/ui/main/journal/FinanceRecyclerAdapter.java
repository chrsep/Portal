package com.directdev.portal.ui.main.journal;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.directdev.portal.R;
import com.directdev.portal.tools.model.Finance;

import java.text.NumberFormat;
import java.util.Locale;

import io.realm.RealmResults;

public class FinanceRecyclerAdapter extends RecyclerView.Adapter {
    private RealmResults<Finance> data;

    public FinanceRecyclerAdapter(RealmResults<Finance> data){
        this.data = data;
    }

    //This binds data to the view
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int i) {
        ViewHolder viewHolder = (ViewHolder) holder;
        final Finance finance = data.get(i);
        final String amount = "Rp. " + NumberFormat.getNumberInstance(Locale.US).format(finance.getITEM_AMT());
        viewHolder.description.setText(finance.getDESCR());
        if(finance.getITEM_TYPE_CD().equals("P")){
            viewHolder.type.setText("Payment");
            viewHolder.amount.setText(amount);
        }else {
            viewHolder.type.setText("Incoming Billing");
            viewHolder.amount.setText(amount);
        }
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
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_journal_finance, parent, false);
        return new ViewHolder(v);
    }

    //This to holds all the view
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView description;
        TextView amount;
        TextView type;

        ViewHolder(View itemView) {
            super(itemView);
            description = (TextView) itemView.findViewById(R.id.journal_finance_description);
            amount = (TextView) itemView.findViewById(R.id.journal_finance_amount);
            type = (TextView) itemView.findViewById(R.id.journal_finance_type);
        }
    }
}
