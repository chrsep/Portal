package com.directdev.portal.ui.finance;


import android.os.Bundle;

import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.directdev.portal.R;
import com.directdev.portal.tools.database.JournalDB;
import com.directdev.portal.tools.datatype.FinanceData;

import org.solovyev.android.views.llm.LinearLayoutManager;

import java.util.List;

public class PaymentFragment extends Fragment {


    public PaymentFragment() {}


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_payment, container, false);

        JournalDB db = new JournalDB(getActivity());
        List<FinanceData> data = db.queryFinance("P");
        FinanceRecyclerAdapter adapter = new FinanceRecyclerAdapter(data);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        RecyclerView dataList = (RecyclerView) view.findViewById(R.id.payment_recycler);
        dataList.setLayoutManager(layoutManager);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);

        dataList.setLayoutManager(layoutManager);
        dataList.setAdapter(adapter);
        return view;
    }


}
