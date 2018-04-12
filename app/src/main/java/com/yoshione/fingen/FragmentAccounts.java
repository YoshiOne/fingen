package com.yoshione.fingen;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Animatable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.AppBarLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.Toast;

import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.yoshione.fingen.adapter.AdapterAccounts;
import com.yoshione.fingen.adapter.AdapterAccountsSets;
import com.yoshione.fingen.adapter.helper.OnStartDragListener;
import com.yoshione.fingen.adapter.helper.SimpleItemTouchHelperCallback;
import com.yoshione.fingen.classes.ListSumsByCabbage;
import com.yoshione.fingen.dao.AccountsDAO;
import com.yoshione.fingen.dao.CabbagesDAO;
import com.yoshione.fingen.dao.CreditsDAO;
import com.yoshione.fingen.dao.TransactionsDAO;
import com.yoshione.fingen.filters.AbstractFilter;
import com.yoshione.fingen.filters.AccountFilter;
import com.yoshione.fingen.filters.FilterListHelper;
import com.yoshione.fingen.interfaces.IAbstractModel;
import com.yoshione.fingen.interfaces.IAdapterEventsListener;
import com.yoshione.fingen.interfaces.IOnComplete;
import com.yoshione.fingen.interfaces.IOnEditAction;
import com.yoshione.fingen.interfaces.IUpdateCallback;
import com.yoshione.fingen.interfaces.IUpdateMainListsEvents;
import com.yoshione.fingen.managers.AccountManager;
import com.yoshione.fingen.managers.AccountsSetManager;
import com.yoshione.fingen.managers.SumsManager;
import com.yoshione.fingen.model.Account;
import com.yoshione.fingen.model.AccountsSet;
import com.yoshione.fingen.model.AccountsSetLog;
import com.yoshione.fingen.model.AccountsSetRef;
import com.yoshione.fingen.model.Cabbage;
import com.yoshione.fingen.model.Transaction;
import com.yoshione.fingen.utils.PrefUtils;
import com.yoshione.fingen.utils.RequestCodes;
import com.yoshione.fingen.utils.ScreenUtils;
import com.yoshione.fingen.utils.UpdateMainListsRwHandler;
import com.yoshione.fingen.widgets.AmountEditor;
import com.yoshione.fingen.widgets.ContextMenuRecyclerView;
import com.yoshione.fingen.widgets.FgLinearLayoutManager;

import java.lang.ref.WeakReference;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import butterknife.BindView;

/**
 * Created by Leonid on 16.08.2015.
 * a
 */
public class FragmentAccounts extends BaseListFragment implements OnStartDragListener, IUpdateMainListsEvents {

    public static final String TAG = "FragmentAccountsRW";
    private static final int CONTEXT_MENU_ACCOUNTS = 0;
    private static final int CONTEXT_MENU_SETS = 1;
    private static final int MSG_UPDATE_SETS = 1;
    public AdapterAccounts adapter;
    @BindView(R.id.layoutSummaryTable)
    TableLayout mLayoutSumTable;
    @BindView(R.id.sliding_layout_accounts)
    SlidingUpPanelLayout mSlidingUpPanelLayout;
    @BindView(R.id.recycler_view_accounts_sets)
    ContextMenuRecyclerView mRecyclerViewAccountsSets;
    @BindView(R.id.buttonAddAccountSet)
    Button mButtonAddAccountSet;
    @BindView(R.id.buttonAddAccount)
    Button mButtonAddAccount;
    @BindView(R.id.imageViewPullMe)
    ImageView mImageViewPullMe;
    AdapterAccountsSets mAdapterAccountsSets;
    private AccountEventListener mAccountEventListener;
    private ItemTouchHelper mItemTouchHelper;
    private int contextMenuTarget = -1;
    private boolean mSumsLoaded;
    UpdateUIHandler mUpdateUIHandler;
    private String mAllAccountsSetCaption;

    public void setFullUpdateCallback(IUpdateCallback fullUpdateCallback) {
        mFullUpdateCallback = fullUpdateCallback;
    }

    private IUpdateCallback mFullUpdateCallback;

    public static FragmentAccounts newInstance(String forceUpdateParam, int layoutID, IUpdateCallback fullUpdateCallback) {
        FragmentAccounts fragment = new FragmentAccounts();
        Bundle args = new Bundle();
        args.putString(FORCE_UPDATE_PARAM, forceUpdateParam);
        args.putInt(LAYOUT_NAME_PARAM, layoutID);
        fragment.setArguments(args);
//        fragment.setUpdateListsEvents(fragment);
        fragment.setFullUpdateCallback(fullUpdateCallback);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        setUpdateListsEvents(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        mSumsLoaded = false;

        mAllAccountsSetCaption = getString(R.string.ent_all_accounts);
//        mSlidingUpPanelLayout.setEnabled(false);
        mUpdateUIHandler = new UpdateUIHandler(this);

//        mImageViewFilterSumIcon.setImageDrawable(IconGenerator.getInstance(getActivity()).getSumIcon(getActivity()).color(ContextCompat.getColor(getActivity(), R.color.ColorAccent)));

        adapter = new AdapterAccounts(getActivity(), this);
        adapter.setHasStableIds(true);
        recyclerView.setAdapter(adapter);

        adapter.setmOnAccountItemClickListener(new AdapterAccounts.OnAccountItemClickListener() {
            @Override
            public void OnAccountItemClick(Account account) {
                if (mAccountEventListener != null) {
                    mAccountEventListener.OnItemClick(account);
                }
            }
        });

        initAccountsSets();

        mButtonAddAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FragmentAccounts.this.getActivity(), ActivityEditAccount.class);
                intent.putExtra("account", new Account());
                if (getActivity() != null) {
                    getActivity().startActivityForResult(intent, RequestCodes.REQUEST_CODE_EDIT_ACCOUNT);
                }
            }
        });

        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(adapter);
        mItemTouchHelper = new ItemTouchHelper(callback);
        mItemTouchHelper.attachToRecyclerView(recyclerView);

        if (getActivity() instanceof ActivityAccounts) {
            mSlidingUpPanelLayout.setEnabled(false);
            mSlidingUpPanelLayout.setPanelHeight(0);
            mSlidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
        } else {
            mLayoutSumTable.getViewTreeObserver().addOnGlobalLayoutListener(
                    new SumsTableOnGlobalLayoutListener(getActivity(), mLayoutSumTable, mSlidingUpPanelLayout));
        }

        if (getActivity() instanceof ActivityMain) {
            ((ActivityMain) getActivity()).mAppBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
                @Override
                public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                    if (getActivity() != null) {
                        int verticalOffsetDp = Math.round(ScreenUtils.PxToDp(getActivity(), verticalOffset));
                        if (verticalOffsetDp == -48) {
                            animatePullMe();
                        } else {
                            animatePullMeReverse();
                        }
                    }
                }
            });
        }

        return view;
    }

    private void initAccountsSets() {
        mButtonAddAccountSet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AccountsSetManager.getInstance().createAccountSet(getActivity(), new IOnComplete() {
                    @Override
                    public void onComplete() {
                        loadAccountsSets();
                        if (mFullUpdateCallback != null) {
                            mFullUpdateCallback.update();
                        } else {
                            fullUpdate(-1);
                        }
                    }
                });
            }
        });
        mAdapterAccountsSets = new AdapterAccountsSets(new IAdapterEventsListener() {
            @Override
            public void onItemClick(IAbstractModel model) {
                if (getActivity() != null) {
                    PreferenceManager.getDefaultSharedPreferences(getActivity()).edit().putLong(FgConst.PREF_CURRENT_ACCOUNT_SET, model.getID()).apply();
                }
                loadAccountsSets();
                if (mFullUpdateCallback != null) {
                    mFullUpdateCallback.update();
                } else {
                    fullUpdate(-1);
                }
            }
        });
        mAdapterAccountsSets.setHasStableIds(true);
        FgLinearLayoutManager layoutManagerFilters = new FgLinearLayoutManager(getActivity());
        layoutManagerFilters.setItemPrefetchEnabled(false);
        mRecyclerViewAccountsSets.setLayoutManager(layoutManagerFilters);
        mRecyclerViewAccountsSets.setAdapter(mAdapterAccountsSets);
    }

    private void loadAccountsSets() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                List<AccountsSet> accountsSetList = AccountsSetManager.getInstance().getAcoountsSets(getActivity());
                AccountsSetRef allAccountsSetRef = new AccountsSetRef(-1, mAllAccountsSetCaption);
                AccountsSet allAccountsSet = new AccountsSet(allAccountsSetRef, new HashSet<AccountsSetLog>());
                accountsSetList.add(0, allAccountsSet);
                long currentSetID;
                if (getActivity() != null) {
                    currentSetID = PreferenceManager.getDefaultSharedPreferences(getActivity()).getLong(FgConst.PREF_CURRENT_ACCOUNT_SET, -1);
                } else {
                    currentSetID = -1;
                }
                for (AccountsSet accountsSet : accountsSetList) {
                    accountsSet.setSelected(accountsSet.getAccountsSetRef().getID() == currentSetID);
                }
                mUpdateUIHandler.sendMessage(mUpdateUIHandler.obtainMessage(MSG_UPDATE_SETS, accountsSetList));
            }
        });
        thread.start();

    }

    @Override
    public void onStart() {
        super.onStart();
        registerForContextMenu(mRecyclerViewAccountsSets);
        loadAccountsSets();
        if (AccountsDAO.getInstance(getActivity()).getModelCount() == 0) {
            mSlidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.ANCHORED);
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if (getActivity() != null) {
            MenuInflater menuInflater = getActivity().getMenuInflater();
            switch (v.getId()) {
                case R.id.recycler_view:
                    contextMenuTarget = CONTEXT_MENU_ACCOUNTS;
                    menuInflater.inflate(R.menu.context_menu_accounts, menu);
                    break;
                case R.id.recycler_view_accounts_sets:
                    contextMenuTarget = CONTEXT_MENU_SETS;
                    menuInflater.inflate(R.menu.context_menu_accounts_sets, menu);
                    break;
            }
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (getUserVisibleHint()) {
            switch (contextMenuTarget) {
                case CONTEXT_MENU_ACCOUNTS:
                    initContextMenuAccounts(item);
                    break;
                case CONTEXT_MENU_SETS:
                    initContextMenuAccountSets(item);
                    break;
            }
            return true;
        } else
            return false;
    }

    @SuppressLint("StringFormatInvalid")
    public void initContextMenuAccountSets(MenuItem item) {
        ContextMenuRecyclerView.RecyclerContextMenuInfo info = (ContextMenuRecyclerView.RecyclerContextMenuInfo) item.getMenuInfo();
        AccountsSet accountsSet = mAdapterAccountsSets.getList().get(info.position);
        switch (item.getItemId()) {
            case R.id.action_edit_name:
                AccountsSetManager.getInstance().editName(accountsSet, getActivity(), new IOnEditAction() {
                    @Override
                    public void onEdit(AccountsSet accountsSet) {
                        AccountsSetManager.getInstance().writeAccountsSet(accountsSet, getActivity(), new IOnComplete() {
                            @Override
                            public void onComplete() {
                                loadAccountsSets();
                                if (mFullUpdateCallback != null) {
                                    mFullUpdateCallback.update();
                                } else {
                                    fullUpdate(-1);
                                }
                            }
                        });
                    }
                });
                break;
            case R.id.action_edit_accounts:
                AccountsSetManager.getInstance().editAccounts(accountsSet, getActivity(), new IOnEditAction() {
                    @Override
                    public void onEdit(AccountsSet accountsSet) {
                        AccountsSetManager.getInstance().writeAccountsSet(accountsSet, getActivity(), new IOnComplete() {
                            @Override
                            public void onComplete() {
                                loadAccountsSets();
                                if (mFullUpdateCallback != null) {
                                    mFullUpdateCallback.update();
                                } else {
                                    fullUpdate(-1);
                                }
                            }
                        });
                    }
                });
                break;
            case R.id.action_delete:
                if (getActivity() != null) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle(R.string.ttl_confirm_action);
                    builder.setMessage(String.format(getString(R.string.msg_delete_account_set_confirmation), accountsSet.getAccountsSetRef().getName()));

                    OnDeleteAccountSetDialogOkClickListener clickListener = new OnDeleteAccountSetDialogOkClickListener(accountsSet);
                    // Set up the buttons
                    builder.setPositiveButton("OK", clickListener);
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });

                    builder.show();
                }
                break;
        }
    }

    @SuppressLint("StringFormatInvalid")
    public void initContextMenuAccounts(MenuItem item) {
        ContextMenuRecyclerView.RecyclerContextMenuInfo info = (ContextMenuRecyclerView.RecyclerContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case R.id.action_add: {
                Intent intent = new Intent(getActivity(), ActivityEditAccount.class);
                intent.putExtra("account", new Account());
                getActivity().startActivityForResult(intent, RequestCodes.REQUEST_CODE_EDIT_ACCOUNT);
                break;
            }
            case R.id.action_edit: {
                Intent intent = new Intent(getActivity(), ActivityEditAccount.class);
                intent.putExtra("account", AccountsDAO.getInstance(getActivity()).getAccountByID(info.id));
                getActivity().startActivityForResult(intent, RequestCodes.REQUEST_CODE_EDIT_ACCOUNT);
                break;
            }
            case R.id.action_delete: {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(R.string.ttl_confirm_action);
                Account account = AccountsDAO.getInstance(getActivity()).getAccountByID(info.id);
                builder.setMessage(String.format(getString(R.string.msg_delete_account_confirmation), account.getName()));

                OnDeleteAccountDialogOkClickListener clickListener = new OnDeleteAccountDialogOkClickListener(account);
                // Set up the buttons
                builder.setPositiveButton("OK", clickListener);
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.show();
                break;
            }
            case R.id.action_show_transactions: {
                if (getActivity().getClass().equals(ActivityMain.class)) {
                    ActivityMain activityMain = (ActivityMain) getActivity();
                    if (activityMain.fragmentTransactions.adapterFilters != null) {
                        Account account = AccountsDAO.getInstance(getActivity()).getAccountByID(info.id);
                        AccountFilter filter = new AccountFilter(0);
                        filter.addAccount(account.getID());
                        ArrayList<AbstractFilter> filters = new ArrayList<>();
                        filters.add(filter);
                        Intent intent = new Intent(getActivity(), ActivityTransactions.class);
                        intent.putParcelableArrayListExtra("filter_list", filters);
                        intent.putExtra("caption", account.getName());
                        intent.putExtra(FgConst.HIDE_FAB, true);
                        intent.putExtra(FgConst.LOCK_SLIDINGUP_PANEL, true);
                        startActivity(intent);
                    }
                }
                break;
            }
            case R.id.action_balance_adjustment: {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                final Account account = AccountsDAO.getInstance(getActivity()).getAccountByID(info.id);
                Cabbage cabbage = AccountManager.getCabbage(account, getActivity());
                builder.setTitle(getString(R.string.ttl_enter_actual_balance));
                final AmountEditor amountEditor = new AmountEditor(getActivity(), new AmountEditor.OnAmountChangeListener() {
                    @Override
                    public void OnAmountChange(BigDecimal newAmount, int newType) {
                    }
                }, (account.getCurrentBalance().compareTo(BigDecimal.ZERO) <= 0) ? Transaction.TRANSACTION_TYPE_EXPENSE : Transaction.TRANSACTION_TYPE_INCOME,
                        cabbage.getDecimalCount(), getActivity());

                amountEditor.setAmount(account.getCurrentBalance());
                builder.setView(amountEditor);

                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        BigDecimal amount = amountEditor.getAmount().multiply(new BigDecimal((amountEditor.getType() > 0) ? 1 : -1));
                        BigDecimal adjustment = amount.subtract(account.getCurrentBalance());
                        Transaction transaction = new Transaction(PrefUtils.getDefDepID(getActivity()));
                        transaction.setAccountID(account.getID());
                        transaction.setAmount(adjustment, (adjustment.compareTo(BigDecimal.ZERO) <= 0) ? Transaction.TRANSACTION_TYPE_EXPENSE : Transaction.TRANSACTION_TYPE_INCOME);
                        Intent intent = new Intent(getActivity(), ActivityEditTransaction.class);
                        intent.putExtra("transaction", transaction);
                        getActivity().startActivityForResult(intent, RequestCodes.REQUEST_CODE_EDIT_TRANSACTION);
                    }
                });

                builder.show();
                break;
            }
            case R.id.action_sort: {
                AccountManager.showSortDialog(getActivity().getSupportFragmentManager(), getActivity());
                break;
            }
            case R.id.action_drag_mode: {
                adapter.setmDragMode(true);
                adapter.notifyDataSetChanged();
                Toast.makeText(getActivity(), getString(R.string.hint_press_back_to_exit_drag_mode), Toast.LENGTH_SHORT).show();
                break;
            }
        }
    }

    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
        mItemTouchHelper.startDrag(viewHolder);
    }

    void setmAccountEventListener(AccountEventListener mAccountEventListener) {
        this.mAccountEventListener = mAccountEventListener;
    }

    @Override
    public void loadData(UpdateMainListsRwHandler handler, long itemID) {
//        if (Looper.myLooper() == Looper.getMainLooper()) {
//            Log.d(TAG, "Main thread!!!");
//        }
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        boolean showClosed = preferences.getBoolean(FgConst.PREF_SHOW_CLOSED_ACCOUNTS, true);
        int sortType = preferences.getInt("accounts_sort_type", 0);
        int sortOrder = preferences.getInt("accounts_sort_order", 0);

        AccountsDAO accountsDAO = AccountsDAO.getInstance(getActivity());
        List<Account> accounts;
        try {
            accounts = accountsDAO.getAllAccounts(showClosed);
        } catch (Exception e) {
            accounts = new ArrayList<>();
        }

        if (!preferences.getBoolean("show_debt_accounts", true)) {
            CreditsDAO creditsDAO = CreditsDAO.getInstance(getActivity());
            for (int i = accounts.size() - 1; i >= 0; i--) {
                if (creditsDAO.isAccountBindToDebt(accounts.get(i).getID())) {
                    accounts.remove(i);
                }
            }
        }

        AccountsSet currentAccountsSet = AccountsSetManager.getInstance().getCurrentAccountSet(getContext());

        List<Long> accountsIDs = currentAccountsSet.getAccountsIDsList();
        if (!accountsIDs.isEmpty()) {
            for (int i = accounts.size() - 1; i >= 0; i--) {
                if (accountsIDs.indexOf(accounts.get(i).getID()) < 0) {
                    accounts.remove(accounts.get(i));
                }
            }
        }

        for (Account account : accounts) {
            account.setSortType(sortType);
            account.setSortOrder(sortOrder);
        }
        Collections.sort(accounts);
        adapter.setAccountList(accounts);
        handler.sendMessage(handler.obtainMessage(UpdateMainListsRwHandler.UPDATE_LIST, 0, 0));
    }

    @Override
    public void loadSums(UpdateMainListsRwHandler handler) {
        if (getActivity() instanceof ActivityAccounts) return;
        TransactionsDAO transactionsDAO = TransactionsDAO.getInstance(FragmentAccounts.this.getActivity());
        ListSumsByCabbage listSumsByCabbage;

        List<Long> accountsIDs = AccountsSetManager.getInstance().getCurrentAccountSet(getContext()).getAccountsIDsList();
        AccountFilter accountFilter = new AccountFilter(0);
        accountFilter.addList(accountsIDs);
        List<AbstractFilter> filters = new ArrayList<>();
        filters.add(accountFilter);

        try {
            listSumsByCabbage = transactionsDAO.getGroupedSums(new FilterListHelper(filters, "", getActivity()), false, null, getActivity());
        } catch (Exception e) {
            listSumsByCabbage = new ListSumsByCabbage();
        }
        handler.sendMessage(handler.obtainMessage(UpdateMainListsRwHandler.UPDATE_SUMS, listSumsByCabbage));
    }

    @Override
    public void updateLists(long itemID) {
        adapter.notifyDataSetChanged();
    }

    @Override
    public void updateSums(ListSumsByCabbage listSumsByCabbage) {
        SumsManager.updateSummaryTable(getActivity(), mLayoutSumTable, true, listSumsByCabbage,
                CabbagesDAO.getInstance(getActivity()).getCabbagesMap(), null);
        mSumsLoaded = true;
    }

    interface AccountEventListener {
        void OnItemClick(Account account);
    }

    private class OnDeleteAccountSetDialogOkClickListener implements DialogInterface.OnClickListener {
        private AccountsSet mAccountsSet;

        OnDeleteAccountSetDialogOkClickListener(AccountsSet accountsSet) {
            mAccountsSet = accountsSet;
        }

        @Override
        public void onClick(DialogInterface dialogInterface, int i) {
            AccountsSetManager.getInstance().deleteAccountSet(mAccountsSet, getActivity());
            PreferenceManager.getDefaultSharedPreferences(getActivity()).edit().putLong(FgConst.PREF_CURRENT_ACCOUNT_SET, -1).apply();
            loadAccountsSets();
            if (mFullUpdateCallback != null) {
                mFullUpdateCallback.update();
            } else {
                fullUpdate(-1);
            }
        }
    }

    private class OnDeleteAccountDialogOkClickListener implements DialogInterface.OnClickListener {
        private final Account mAccount;

        OnDeleteAccountDialogOkClickListener(Account account) {
            mAccount = account;
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            AccountsDAO accountsDAO = AccountsDAO.getInstance(getActivity());
            accountsDAO.deleteModel(mAccount, true, getActivity());
        }
    }

    private static class UpdateUIHandler extends Handler {
        WeakReference<FragmentAccounts> mFragment;

        UpdateUIHandler(FragmentAccounts fragment) {
            mFragment = new WeakReference<>(fragment);
        }

        @Override
        public void handleMessage(Message msg) {
            final FragmentAccounts fragment = mFragment.get();
            switch (msg.what) {
                case MSG_UPDATE_SETS:
                    fragment.mAdapterAccountsSets.setList((List<AccountsSet>) msg.obj);
                    fragment.mAdapterAccountsSets.notifyDataSetChanged();
                    break;
            }

        }
    }

    private boolean mPullMeBended = false;

    private void animatePullMe() {
        if (!mPullMeBended) {
            mPullMeBended = true;
            mImageViewPullMe.setImageDrawable(getContext().getDrawable(R.drawable.pull_me_animated));
            ((Animatable) mImageViewPullMe.getDrawable()).start();
        }
    }

    private void animatePullMeReverse() {
        if (mPullMeBended) {
            mPullMeBended = false;
            mImageViewPullMe.setImageDrawable(getContext().getDrawable(R.drawable.pull_me_animated_reverse));
            ((Animatable) mImageViewPullMe.getDrawable()).start();
        }
    }
}
