package com.yoshione.fingen;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Animatable;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.yoshione.fingen.adapter.AdapterAccounts;
import com.yoshione.fingen.adapter.AdapterAccountsSets;
import com.yoshione.fingen.adapter.helper.OnStartDragListener;
import com.yoshione.fingen.adapter.helper.SimpleItemTouchHelperCallback;
import com.yoshione.fingen.dao.AccountsDAO;
import com.yoshione.fingen.dao.CabbagesDAO;
import com.yoshione.fingen.dao.CreditsDAO;
import com.yoshione.fingen.dao.TransactionsDAO;
import com.yoshione.fingen.filters.AbstractFilter;
import com.yoshione.fingen.filters.AccountFilter;
import com.yoshione.fingen.filters.FilterListHelper;
import com.yoshione.fingen.iab.BillingService;
import com.yoshione.fingen.interfaces.IUpdateCallback;
import com.yoshione.fingen.managers.AccountManager;
import com.yoshione.fingen.managers.AccountsSetManager;
import com.yoshione.fingen.managers.SumsManager;
import com.yoshione.fingen.model.Account;
import com.yoshione.fingen.model.AccountsSet;
import com.yoshione.fingen.model.AccountsSetRef;
import com.yoshione.fingen.model.Cabbage;
import com.yoshione.fingen.model.Transaction;
import com.yoshione.fingen.utils.PrefUtils;
import com.yoshione.fingen.utils.RequestCodes;
import com.yoshione.fingen.utils.ScreenUtils;
import com.yoshione.fingen.widgets.AmountEditor;
import com.yoshione.fingen.widgets.ContextMenuRecyclerView;
import com.yoshione.fingen.widgets.FgLinearLayoutManager;
import com.yoshione.fingen.widgets.ToolbarActivity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

import javax.inject.Inject;

import butterknife.BindView;
import dagger.Lazy;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by Leonid on 16.08.2015.
 * a
 */
public class FragmentAccounts extends BaseListFragment implements OnStartDragListener {

    public static final String TAG = "FragmentAccountsRW";
    private static final int CONTEXT_MENU_ACCOUNTS = 0;
    private static final int CONTEXT_MENU_SETS = 1;

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
    @BindView(R.id.textview_pull_to_create_account)
    TextView mTextViewPullToCreateAccount;
    @BindView(R.id.layout_pull_me)
    LinearLayout mLayoutPullMe;

    public AdapterAccounts adapter;
    private AdapterAccountsSets mAdapterAccountsSets;
    private AccountEventListener mAccountEventListener;
    private ItemTouchHelper mItemTouchHelper;
    private IUpdateCallback mFullUpdateCallback;
    private int contextMenuTarget = -1;

    @Inject
    Lazy<BillingService> mBillingService;
    @Inject
    Lazy<SharedPreferences> mPreferences;
    @Inject
    Lazy<AccountsDAO> mAccountsDAO;
    @Inject
    Lazy<CreditsDAO> mCreditsDAO;
    @Inject
    Lazy<TransactionsDAO> mTransactionsDAO;
    @Inject
    Lazy<CabbagesDAO> mCabbagesDAO;

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
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
//        setUpdateListsEvents(this);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        FGApplication.getAppComponent().inject(this);

        adapter = new AdapterAccounts((ToolbarActivity) getActivity(), this);
        adapter.setHasStableIds(true);
        recyclerView.setAdapter(adapter);

        adapter.setmOnAccountItemClickListener(account -> {
            if (mAccountEventListener != null) {
                mAccountEventListener.OnItemClick(account);
            }
        });

        initAccountsSets();

        mButtonAddAccount.setOnClickListener(v -> {
            int counter = mPreferences.get().getInt(FgConst.PREF_NEW_ACCOUNT_BUTTON_COUNTER, 0);
            mPreferences.get().edit().putInt(FgConst.PREF_NEW_ACCOUNT_BUTTON_COUNTER, counter + 1).apply();
            Intent intent = new Intent(FragmentAccounts.this.getActivity(), ActivityEditAccount.class);
            intent.putExtra("account", new Account());
            if (getActivity() != null) {
                getActivity().startActivityForResult(intent, RequestCodes.REQUEST_CODE_EDIT_ACCOUNT);
            }
        });

        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(adapter);
        mItemTouchHelper = new ItemTouchHelper(callback);
        mItemTouchHelper.attachToRecyclerView(recyclerView);

        mLayoutSumTable.getViewTreeObserver().addOnGlobalLayoutListener(
                    new SumsTableOnGlobalLayoutListener(getActivity(), mLayoutSumTable, mSlidingUpPanelLayout));

        if (getActivity() instanceof ActivityMain) {
            ((ActivityMain) getActivity()).mAppBarLayout.addOnOffsetChangedListener((appBarLayout, verticalOffset) -> {
                if (getActivity() != null) {
                    int verticalOffsetDp = Math.round(ScreenUtils.PxToDp(getActivity(), verticalOffset));
                    if (verticalOffsetDp == -48) {
                        animatePullMe();
                    } else {
                        animatePullMeReverse();
                    }
                }
            });
        }

        return view;
    }

    public void setFullUpdateCallback(IUpdateCallback fullUpdateCallback) {
        mFullUpdateCallback = fullUpdateCallback;
    }

    private void initAccountsSets() {
        mButtonAddAccountSet.setOnClickListener(view -> AccountsSetManager.getInstance().createAccountSet(getActivity(), this::fragmentUpdate));
        mAdapterAccountsSets = new AdapterAccountsSets(model -> {
            if (getActivity() != null) {
                mPreferences.get().edit().putLong(FgConst.PREF_CURRENT_ACCOUNT_SET, model.getID()).apply();
            }
            fragmentUpdate();
        });
        mAdapterAccountsSets.setHasStableIds(true);
        FgLinearLayoutManager layoutManagerFilters = new FgLinearLayoutManager(getActivity());
        layoutManagerFilters.setItemPrefetchEnabled(false);
        mRecyclerViewAccountsSets.setLayoutManager(layoutManagerFilters);
        mRecyclerViewAccountsSets.setAdapter(mAdapterAccountsSets);
    }

    private void loadAccountsSets() {
        ToolbarActivity activity = (ToolbarActivity) getActivity();
        Objects.requireNonNull(activity).unsubscribeOnDestroy(
                Single.fromCallable(() -> {
                    List<AccountsSet> accountsSetList = AccountsSetManager.getInstance().getAcoountsSets(getActivity());
                    accountsSetList.add(0, new AccountsSet(new AccountsSetRef(-1, getString(R.string.ent_all_accounts)), new HashSet<>()));
                    accountsSetList.add(1, new AccountsSet(new AccountsSetRef(-2, getString(R.string.ent_all_closed_accounts)), new HashSet<>()));
                    long currentSetID = getActivity() != null ? mPreferences.get().getLong(FgConst.PREF_CURRENT_ACCOUNT_SET, -1) : -1;
                    for (AccountsSet accountsSet : accountsSetList) {
                        accountsSet.setSelected(accountsSet.getAccountsSetRef().getID() == currentSetID);
                    }
                    return accountsSetList;
                })
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(accountsSets -> {
                            mAdapterAccountsSets.setList(accountsSets);
                            mAdapterAccountsSets.notifyDataSetChanged();
                        })
        );
    }

    @Override
    public void onStart() {
        super.onStart();
        registerForContextMenu(mRecyclerViewAccountsSets);
        loadAccountsSets();
        if (mAccountsDAO.get().getModelCount() == 0) {
            mSlidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.ANCHORED);
        }
    }

    @Override
    public void onCreateContextMenu(@NonNull ContextMenu menu, @NonNull View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if (getActivity() != null) {
            MenuInflater menuInflater = getActivity().getMenuInflater();
            switch (v.getId()) {
                case R.id.recycler_view:
                    contextMenuTarget = CONTEXT_MENU_ACCOUNTS;
                    menuInflater.inflate(R.menu.context_menu_accounts, menu);
                    ContextMenuRecyclerView.RecyclerContextMenuInfo info = (ContextMenuRecyclerView.RecyclerContextMenuInfo) menuInfo;
                    Account account = adapter.getAccountByPosition(info.position);
                    if (account.getIsClosed())
                        menu.findItem(R.id.action_toggle_close_account).setTitle(R.string.ent_open_account);
                    break;
                case R.id.recycler_view_accounts_sets:
                    contextMenuTarget = CONTEXT_MENU_SETS;
                    menuInflater.inflate(R.menu.context_menu_accounts_sets, menu);
                    break;
            }
            final MenuItem item = menu.findItem(R.id.action_show_report);
            if (item != null) {
                item.setVisible(false);
                ((ToolbarActivity) getActivity()).unsubscribeOnDestroy(
                        mBillingService.get().getReportsIapInfo()
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(
                                        skuDetailsWrapper -> {
                                            if (skuDetailsWrapper.isPurchased()) {
                                                item.setVisible(true);
                                            } else {
                                                item.setVisible(false);
                                            }
                                        },
                                        throwable -> item.setVisible(false)));
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
                AccountsSetManager.getInstance().editName(accountsSet, getActivity(), accountsSet1 -> AccountsSetManager.getInstance().writeAccountsSet(accountsSet1, getActivity(), this::fragmentUpdate));
                break;
            case R.id.action_edit_accounts:
                AccountsSetManager.getInstance().editAccounts(accountsSet, getActivity(), accountsSet2 -> AccountsSetManager.getInstance().writeAccountsSet(accountsSet2, getActivity(), this::fragmentUpdate));
                break;
            case R.id.action_delete:
                if (getActivity() != null) {
                    OnDeleteAccountSetDialogOkClickListener clickListener = new OnDeleteAccountSetDialogOkClickListener(accountsSet);

                    new AlertDialog.Builder(getActivity())
                            .setTitle(R.string.ttl_confirm_action)
                            .setMessage(String.format(getString(R.string.msg_delete_account_set_confirmation), accountsSet.getAccountsSetRef().getName()))
                            .setPositiveButton(android.R.string.ok, clickListener)
                            .setNegativeButton(android.R.string.cancel, (dialog, which) -> dialog.cancel())
                            .show();
                }
                break;
        }
    }

    @SuppressLint("StringFormatInvalid")
    public void initContextMenuAccounts(MenuItem item) {
        ContextMenuRecyclerView.RecyclerContextMenuInfo info = (ContextMenuRecyclerView.RecyclerContextMenuInfo) item.getMenuInfo();
        Account account = adapter.getAccountByPosition(info.position);
        switch (item.getItemId()) {
            case R.id.action_add: {
                Intent intent = new Intent(getActivity(), ActivityEditAccount.class);
                intent.putExtra("account", new Account());
                Objects.requireNonNull(getActivity()).startActivityForResult(intent, RequestCodes.REQUEST_CODE_EDIT_ACCOUNT);
                break;
            }
            case R.id.action_edit: {
                Intent intent = new Intent(getActivity(), ActivityEditAccount.class);
                intent.putExtra("account", account);
                Objects.requireNonNull(getActivity()).startActivityForResult(intent, RequestCodes.REQUEST_CODE_EDIT_ACCOUNT);
                break;
            }
            case R.id.action_delete: {
                OnDeleteAccountDialogOkClickListener clickListener = new OnDeleteAccountDialogOkClickListener(account);

                new AlertDialog.Builder(Objects.requireNonNull(getActivity()))
                        .setTitle(R.string.ttl_confirm_action)
                        .setMessage(String.format(getString(R.string.msg_delete_account_confirmation), account.getName()))
                        .setPositiveButton(android.R.string.ok, clickListener)
                        .setNegativeButton(android.R.string.cancel, (dialog, which) -> dialog.cancel())
                        .show();
                break;
            }
            case R.id.action_show_transactions: {
                if (Objects.requireNonNull(getActivity()).getClass().equals(ActivityMain.class)) {
                    ActivityMain activityMain = (ActivityMain) getActivity();
                    if (activityMain.fragmentTransactions.adapterF != null) {
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
            case R.id.action_show_report: {
                AccountFilter filter = new AccountFilter(0);
                filter.addAccount(account.getID());
                ArrayList<AbstractFilter> filters = new ArrayList<>();
                filters.add(filter);
                Intent intent = new Intent(getActivity(), ActivityReports.class);
                intent.putParcelableArrayListExtra("filter_list", filters);
                startActivity(intent);
                break;
            }
            case R.id.action_balance_adjustment: {
                Cabbage cabbage = AccountManager.getCabbage(account, getActivity());
                final AmountEditor amountEditor = new AmountEditor(getActivity(), (newAmount, newType) -> {},
                        (account.getCurrentBalance().compareTo(BigDecimal.ZERO) <= 0) ? Transaction.TRANSACTION_TYPE_EXPENSE : Transaction.TRANSACTION_TYPE_INCOME,
                        cabbage.getDecimalCount(), getActivity());
                amountEditor.setAmount(account.getCurrentBalance());

                new AlertDialog.Builder(Objects.requireNonNull(getActivity()))
                        .setTitle(getString(R.string.ttl_enter_actual_balance))
                        .setView(amountEditor)
                        .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                            BigDecimal amount = amountEditor.getAmount().multiply(new BigDecimal((amountEditor.getType() > 0) ? 1 : -1));
                            BigDecimal adjustment = amount.subtract(account.getCurrentBalance());
                            Transaction transaction = new Transaction(PrefUtils.getDefDepID(getActivity()));
                            transaction.setAccountID(account.getID());
                            transaction.setAmount(adjustment, (adjustment.compareTo(BigDecimal.ZERO) <= 0) ? Transaction.TRANSACTION_TYPE_EXPENSE : Transaction.TRANSACTION_TYPE_INCOME);
                            Intent intent1 = new Intent(getActivity(), ActivityEditTransaction.class);
                            intent1.putExtra("transaction", transaction);
                            getActivity().startActivityForResult(intent1, RequestCodes.REQUEST_CODE_EDIT_TRANSACTION);
                        })
                        .show();
                break;
            }
            case R.id.action_toggle_close_account:
                new AlertDialog.Builder(Objects.requireNonNull(getActivity()))
                    .setTitle(R.string.ttl_confirm_action)
                    .setMessage(String.format(getString(account.getIsClosed() ? R.string.msg_open_account_confirmation : R.string.msg_close_account_confirmation), account.getName()))
                    .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                        try {
                            account.setIsClosed(!account.getIsClosed());
                            mAccountsDAO.get().createModelWithoutEvent(account);
                            fragmentUpdate();
                        } catch (Exception e) {
                            Toast.makeText(getActivity(), "Error", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, (dialog, which) -> dialog.cancel())
                    .show();
                break;
            case R.id.action_sort: {
                AccountManager.showSortDialog(Objects.requireNonNull(getActivity()).getSupportFragmentManager(), getActivity());
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
    public void loadData(long itemID) {
//        if (Looper.myLooper() == Looper.getMainLooper()) {
//            Log.d(TAG, "Main thread!!!");
//        }
        boolean isOnlyClosed = mPreferences.get().getLong(FgConst.PREF_CURRENT_ACCOUNT_SET, -1) == -2;
        boolean showClosed = isOnlyClosed || mPreferences.get().getBoolean(FgConst.PREF_SHOW_CLOSED_ACCOUNTS, true);
        int sortType = mPreferences.get().getInt("accounts_sort_type", 0);
        int sortOrder = mPreferences.get().getInt("accounts_sort_order", 0);

        ToolbarActivity activity = (ToolbarActivity) getActivity();
        Objects.requireNonNull(activity).unsubscribeOnDestroy(
            mAccountsDAO.get().getAllAccountsRx(showClosed, isOnlyClosed)
                    .subscribeOn(Schedulers.newThread())
                    .map(accounts -> {
                        if (!mPreferences.get().getBoolean("show_debt_accounts", true)) {
                            for (int i = accounts.size() - 1; i >= 0; i--) {
                                if (mCreditsDAO.get().isAccountBindToDebt(accounts.get(i).getID())) {
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
                        return accounts;
                    })
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(accounts -> {
                        adapter.notifyDataSetChanged();
                        if (mPreferences.get().getInt(FgConst.PREF_NEW_ACCOUNT_BUTTON_COUNTER, 0) < 3) {
                            mTextViewPullToCreateAccount.setVisibility(View.VISIBLE);
                        } else {
                            mTextViewPullToCreateAccount.setVisibility(View.GONE);
                        }
                        loadSums();
                    })
        );
    }

    public void loadSums() {
        List<Long> accountsIDs = AccountsSetManager.getInstance().getCurrentAccountSet(getContext()).getAccountsIDsList();
        AccountFilter accountFilter = new AccountFilter(0);
        accountFilter.addList(accountsIDs);
        List<AbstractFilter> filters = new ArrayList<>();
        filters.add(accountFilter);

        ToolbarActivity activity = (ToolbarActivity) getActivity();
        HashMap<Long, Cabbage> cabbages = mCabbagesDAO.get().getCabbagesMap();
        Objects.requireNonNull(activity).unsubscribeOnDestroy(
                mTransactionsDAO.get().getGroupedSumsRx(new FilterListHelper(filters, "", activity, mPreferences.get(), true), true, null, getActivity())
                        .map(listSumsByCabbage -> SumsManager.formatSums(listSumsByCabbage, cabbages, true))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(listSumsByCabbage -> SumsManager.updateSummaryTableWithFormattedStrings(getActivity(),
                                mLayoutSumTable, true, listSumsByCabbage, null))
        );
    }

    private void fragmentUpdate() {
        loadAccountsSets();
        if (mFullUpdateCallback != null) {
            mFullUpdateCallback.update();
        } else {
            fullUpdate(-1);
        }
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
            mPreferences.get().edit().putLong(FgConst.PREF_CURRENT_ACCOUNT_SET, -1).apply();
            fragmentUpdate();
        }
    }

    private class OnDeleteAccountDialogOkClickListener implements DialogInterface.OnClickListener {
        private final Account mAccount;

        OnDeleteAccountDialogOkClickListener(Account account) {
            mAccount = account;
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            mAccountsDAO.get().deleteModel(mAccount, true, getActivity());
        }
    }

    private boolean mPullMeBended = false;

    private void animatePullMe() {
        if (!mPullMeBended) {
            mPullMeBended = true;
            mImageViewPullMe.setImageDrawable(Objects.requireNonNull(getContext()).getDrawable(R.drawable.pull_me_animated));
            ((Animatable) mImageViewPullMe.getDrawable()).start();
        }
    }

    private void animatePullMeReverse() {
        if (mPullMeBended) {
            mPullMeBended = false;
            mImageViewPullMe.setImageDrawable(Objects.requireNonNull(getContext()).getDrawable(R.drawable.pull_me_animated_reverse));
            ((Animatable) mImageViewPullMe.getDrawable()).start();
        }
    }
}
