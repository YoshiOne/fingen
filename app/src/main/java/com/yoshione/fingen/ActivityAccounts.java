package com.yoshione.fingen;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.preference.PreferenceManager;

import com.yoshione.fingen.interfaces.IAbstractModel;
import com.yoshione.fingen.model.Account;
import com.yoshione.fingen.model.Events;
import com.yoshione.fingen.widgets.ToolbarActivity;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class ActivityAccounts extends ToolbarActivity {

    FragmentAccounts mFragmentAccounts;

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_accounts;
    }

    @Override
    protected String getLayoutTitle() {
        return getString(R.string.ent_accounts);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        if (savedInstanceState == null) {
            FragmentAccounts fragmentAccounts = FragmentAccounts.newInstance(FgConst.PREF_FORCE_UPDATE_ACCOUNTS, R.layout.fragment_accounts, null);
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.add(R.id.container, fragmentAccounts, "fragment_accounts").commit();
//        }
        PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean(FgConst.PREF_FORCE_UPDATE_ACCOUNTS, true).apply();
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        super.onAttachFragment(fragment);
        if (fragment.getClass().equals(FragmentAccounts.class)) {
            mFragmentAccounts = (FragmentAccounts) fragment;
            mFragmentAccounts.setmAccountEventListener(new FragmentAccounts.AccountEventListener() {
                @Override
                public void OnItemClick(Account account) {
                    Intent intent = new Intent();
                    intent.putExtra("model", account);
                    intent.putExtra("destAccount", getIntent().getBooleanExtra("destAccount", false));
                    intent.putStringArrayListExtra(FgConst.SELECTED_TRANSACTIONS_IDS, getIntent().getStringArrayListExtra(FgConst.SELECTED_TRANSACTIONS_IDS));
                    ActivityAccounts.this.setResult(RESULT_OK, intent);
                    ActivityAccounts.this.finish();
                }
            });
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onEvent(Events.EventOnModelChanged event) {
        switch (event.getModelType()) {
            case IAbstractModel.MODEL_TYPE_ACCOUNT:
                if (mFragmentAccounts.recyclerView != null) {
                    mFragmentAccounts.fullUpdate(-1);
                }
                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mFragmentAccounts.recyclerView != null) {
            mFragmentAccounts.fullUpdate(-1);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }
}
