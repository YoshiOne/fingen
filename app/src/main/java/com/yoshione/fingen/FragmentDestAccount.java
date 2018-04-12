package com.yoshione.fingen;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.yoshione.fingen.managers.AccountManager;
import com.yoshione.fingen.managers.TransactionManager;
import com.yoshione.fingen.model.Account;
import com.yoshione.fingen.utils.RequestCodes;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Leonid on 27.09.2015.
 *
 */
public class FragmentDestAccount extends Fragment {

    private static final String TAG = "FragmentDestAccount";

    @BindView(R.id.textViewDestAccount)
    EditText textViewDestAccount;

    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frg_te_dest_account, container, false);
        ButterKnife.bind(this, view);

        textViewDestAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), ActivityAccounts.class);
                intent.putExtra("showHomeButton", false);
                intent.putExtra("model", new Account());
                intent.putExtra("destAccount", true);
                getActivity().startActivityForResult(intent, RequestCodes.REQUEST_CODE_SELECT_MODEL);
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        Account destAccount = TransactionManager.getDestAccount(((ActivityEditTransaction) getActivity()).getTransaction(), getActivity());
//        textViewDestAccount.setText(destAccount.getName());
//        textViewDestAccountCabbage.setText(AccountManager.getCabbage(destAccount, getActivity()).getCode());
        String name = destAccount.getName();
        String code = AccountManager.getCabbage(destAccount, getActivity()).getSimbol();
        if (name.isEmpty()) {
            textViewDestAccount.setText("");
        } else {
            textViewDestAccount.setText(String.format("%s (%s)", destAccount.getName(), code));
        }
    }
}
