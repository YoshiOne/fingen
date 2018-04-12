/*
 * Copyright (c) 2015.
 */

package com.yoshione.fingen;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.yoshione.fingen.interfaces.IAbstractModel;
import com.yoshione.fingen.model.Credit;
import com.yoshione.fingen.model.MenuItem;
import com.yoshione.fingen.model.SimpleDebt;
import com.yoshione.fingen.model.Transaction;
import com.yoshione.fingen.utils.PrefUtils;
import com.yoshione.fingen.utils.RequestCodes;
import com.yoshione.fingen.widgets.ToolbarActivity;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * Created by slv on 07.12.2015.
 * ActivityDebtsAndCredits
 */
public class ActivityDebtsAndCredits extends ToolbarActivity {

    @BindView(R.id.viewPager)
    ViewPager viewPager;
    @BindView(R.id.tabLayout)
    TabLayout tabLayout;
    private FragmentModelList fragmentCredits;
    private FragmentModelList fragmentDebts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.bind(this);

//        if (savedInstanceState == null) {
            fragmentCredits = FragmentModelList.newInstance(new Credit(), 0);
            fragmentDebts = FragmentModelList.newInstance(new SimpleDebt(), 0);

            FragmentPagerAdapter fragmentPagerAdapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
                @Override
                public Fragment getItem(int position) {
                    switch (position) {
                        case 0:
                            return fragmentCredits;
                        case 1:
                            return fragmentDebts;
                        default:
                            return fragmentCredits;
                    }
                }

                @Override
                public int getCount() {
                    return 2;
                }

                @Override
                public CharSequence getPageTitle(int position) {
                    switch (position) {
                        case 0:
                            return getString(R.string.ent_credits);
                        case 1:
                            return getString(R.string.ent_debts);
                        default:
                            return getString(R.string.ent_credits);
                    }
                }
            };

            viewPager.setAdapter(fragmentPagerAdapter);
            tabLayout.setupWithViewPager(viewPager);
            viewPager.setCurrentItem(0);
//        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (fragmentCredits != null) {
            fragmentCredits.setmModelListEventListener(new FragmentModelList.ModelListEventListener() {
                @Override
                public void OnModelClick(IAbstractModel abstractModel) {
                    showDebtActionsDialog(abstractModel);
                }
            });
        }
        if (fragmentDebts != null) {
            fragmentDebts.setmModelListEventListener(new FragmentModelList.ModelListEventListener() {
                @Override
                public void OnModelClick(IAbstractModel abstractModel) {
                    showDebtActionsDialog(abstractModel);
                }
            });
        }
    }

    private void showDebtActionsDialog(final IAbstractModel model) {
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(this);
        builderSingle.setTitle(this.getResources().getString(R.string.ttl_select_action));

        List<MenuItem> items = new ArrayList<>();

        items.add(new MenuItem(null, getString(R.string.act_borrow), null, Credit.DEBT_ACTION_BORROW));
        items.add(new MenuItem(null, getString(R.string.act_repay), null, Credit.DEBT_ACTION_REPAY));
        items.add(new MenuItem(null, getString(R.string.act_grant_a_loan_to), null, Credit.DEBT_ACTION_GRANT));
        items.add(new MenuItem(null, getString(R.string.act_take_in_payment), null, Credit.DEBT_ACTION_TAKE));

        ArrayAdapter<MenuItem> menuItemArrayAdapter = new ArrayAdapter<>(this, android.R.layout.select_dialog_item, items);

        builderSingle.setNegativeButton(
                this.getResources().getString(android.R.string.cancel),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        builderSingle.setAdapter(menuItemArrayAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ListView lw = ((AlertDialog) dialog).getListView();
                MenuItem menuItem = (MenuItem) lw.getAdapter().getItem(which);
                ActivityDebtsAndCredits.this.doDebtAction(model, menuItem.getId());
                dialog.dismiss();
            }
        });
        builderSingle.show();
    }

    private void doDebtAction(IAbstractModel model, int actionId) {
        if (model.getClass().equals(Credit.class)) {
            Credit credit = (Credit) model;
            Transaction transaction = new Transaction(PrefUtils.getDefDepID(this));
            Intent intent = new Intent(this, ActivityEditTransaction.class);
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

            switch (actionId) {
                case Credit.DEBT_ACTION_BORROW://Занять
                    transaction.setAccountID(credit.getAccountID());
                    transaction.setDestAccountID(preferences.getLong("credit_dest_account", -1));
                    transaction.setTransactionType(Transaction.TRANSACTION_TYPE_TRANSFER);
                    break;
                case Credit.DEBT_ACTION_REPAY://Оплатить долг
                    transaction.setDestAccountID(credit.getAccountID());
                    transaction.setAccountID(preferences.getLong("credit_src_account", -1));
                    transaction.setTransactionType(Transaction.TRANSACTION_TYPE_TRANSFER);
                    break;
                case Credit.DEBT_ACTION_GRANT://Дать в долг
                    transaction.setDestAccountID(credit.getAccountID());
                    transaction.setAccountID(preferences.getLong("credit_src_account", -1));
                    transaction.setTransactionType(Transaction.TRANSACTION_TYPE_TRANSFER);
                    break;
                case Credit.DEBT_ACTION_TAKE://Принять оплату долга
                    transaction.setAccountID(credit.getAccountID());
                    transaction.setDestAccountID(preferences.getLong("credit_dest_account", -1));
                    transaction.setTransactionType(Transaction.TRANSACTION_TYPE_TRANSFER);
                    break;
            }

            intent.putExtra("credit", credit);
            intent.putExtra("credit_action", actionId);
            intent.putExtra("transaction", transaction);
            startActivityForResult(intent, RequestCodes.REQUEST_CODE_EDIT_TRANSACTION);
        } else {
            SimpleDebt simpleDebt = (SimpleDebt) model;
            Transaction transaction = new Transaction(PrefUtils.getDefDepID(this));
            Intent intent = new Intent(this, ActivityEditTransaction.class);
            transaction.setSimpleDebtID(simpleDebt.getID());

            switch (actionId) {
                case Credit.DEBT_ACTION_BORROW://Занять
                case Credit.DEBT_ACTION_TAKE://Принять оплату долга
                    transaction.setTransactionType(Transaction.TRANSACTION_TYPE_INCOME);
                    break;
                case Credit.DEBT_ACTION_REPAY://Оплатить долг
                case Credit.DEBT_ACTION_GRANT://Дать в долг
                    transaction.setTransactionType(Transaction.TRANSACTION_TYPE_EXPENSE);
                    break;
            }

            intent.putExtra("transaction", transaction);
            startActivityForResult(intent, RequestCodes.REQUEST_CODE_EDIT_TRANSACTION);
        }
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_debts_and_credits;
    }

    @Override
    protected String getLayoutTitle() {
        return getString(R.string.ent_debts);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }
}
