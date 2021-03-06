package com.directdev.portal.ui.main.account.finance;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.directdev.portal.R;
import com.directdev.portal.tools.model.Finance;

import org.solovyev.android.views.llm.LinearLayoutManager;

import io.realm.Realm;
import io.realm.RealmResults;

public class BillingFragment extends Fragment {
    private Realm realm;

    public BillingFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_billing, container, false);
        realm = Realm.getDefaultInstance();
        RealmResults<Finance> bills = realm.where(Finance.class).equalTo("ITEM_TYPE_CD","C").findAll();
        FinanceRecyclerAdapter adapter = new FinanceRecyclerAdapter(bills);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        RecyclerView dataList = (RecyclerView) view.findViewById(R.id.billing_recycler);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);

        dataList.setLayoutManager(layoutManager);
        dataList.setAdapter(adapter);
        return view;
    }

    @Override
    public void onDestroyView() {
        realm.close();
        super.onDestroyView();
    }
}
