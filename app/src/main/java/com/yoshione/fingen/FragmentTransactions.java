package com.yoshione.fingen;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.drawable.Animatable;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import android.util.Log;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.yoshione.fingen.adapter.AdapterFilters;
import com.yoshione.fingen.adapter.AdapterTransactions;
import com.yoshione.fingen.classes.ListSumsByCabbage;
import com.yoshione.fingen.dao.AccountsDAO;
import com.yoshione.fingen.dao.CabbagesDAO;
import com.yoshione.fingen.dao.TransactionsDAO;
import com.yoshione.fingen.filters.AbstractFilter;
import com.yoshione.fingen.filters.AccountFilter;
import com.yoshione.fingen.filters.AmountFilter;
import com.yoshione.fingen.filters.DateRangeFilter;
import com.yoshione.fingen.filters.FilterListHelper;
import com.yoshione.fingen.filters.FilterUtils;
import com.yoshione.fingen.filters.NestedModelFilter;
import com.yoshione.fingen.interfaces.IAbstractModel;
import com.yoshione.fingen.interfaces.ILoadMoreFinish;
import com.yoshione.fingen.interfaces.IOnLoadMore;
import com.yoshione.fingen.interfaces.IUpdateMainListsEvents;
import com.yoshione.fingen.managers.AccountsSetManager;
import com.yoshione.fingen.managers.FilterManager;
import com.yoshione.fingen.managers.SumsManager;
import com.yoshione.fingen.managers.TransactionManager;
import com.yoshione.fingen.model.Account;
import com.yoshione.fingen.model.AccountsSet;
import com.yoshione.fingen.model.BaseModel;
import com.yoshione.fingen.model.StringIntItem;
import com.yoshione.fingen.model.Template;
import com.yoshione.fingen.model.Transaction;
import com.yoshione.fingen.utils.PrefUtils;
import com.yoshione.fingen.utils.RequestCodes;
import com.yoshione.fingen.utils.ScreenUtils;
import com.yoshione.fingen.utils.UpdateMainListsRwHandler;
import com.yoshione.fingen.widgets.ContextMenuRecyclerView;
import com.yoshione.fingen.widgets.FgLinearLayoutManager;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import ca.barrenechea.widget.recyclerview.decoration.StickyHeaderDecoration;

import static android.app.Activity.RESULT_OK;
import static com.yoshione.fingen.utils.RequestCodes.REQUEST_CODE_SELECT_MODEL;

/**
 * Created by slv on 10.09.2015.
 * 1
 */

public class FragmentTransactions extends BaseListFragment implements AdapterFilters.OnFilterChangeListener,
        AdapterTransactions.OnTransactionItemEventListener, IUpdateMainListsEvents, IOnLoadMore {

    private static final String TAG = "FragmentTransactions";
    public static final int NUMBER_ITEMS_TO_BE_LOADED = 25;

    private static final int CONTEXT_MENU_TRANSACTIONS = 0;
    private static final int CONTEXT_MENU_FILTERS = 1;
    AdapterFilters adapterFilters;
    @BindView(R.id.layoutSummaryTable)
    TableLayout layoutSumTable;
    @BindView(R.id.switch_all_filters)
    SwitchCompat switchAllFilters;
    @BindView(R.id.recycler_view_filters)
    ContextMenuRecyclerView recyclerViewFilters;
    @BindView(R.id.fabMenuSelection)
    FloatingActionMenu fabMenuSelection;
    @BindView(R.id.fabGoTop)
    FloatingActionButton mFabGoTop;
    @BindView(R.id.fabSelectAll)
    FloatingActionButton mFabSelectAll;
    @BindView(R.id.fabUnselectAll)
    FloatingActionButton mFabUnselectAll;
    @BindView(R.id.fabEditSelected)
    FloatingActionButton mFabEditSelected;
    @BindView(R.id.fabExportSelected)
    FloatingActionButton mFabExportSelected;
    @BindView(R.id.fabDeleteSelected)
    FloatingActionButton mFabDeleteSelected;
    @BindView(R.id.sliding_layout_transactions)
    SlidingUpPanelLayout mSlidingLayoutTransactions;
    @BindView(R.id.editTextSearch)
    EditText mEditTextSearch;
    @BindView(R.id.imageButtonClearSearchString)
    ImageButton mImageButtonClearSearchString;
    @BindView(R.id.cardViewSearch)
    ConstraintLayout mCardViewSearch;
    @BindView(R.id.dragView)
    LinearLayout mDragView;
    @BindView(R.id.textViewSelectedCount)
    TextView mTextViewSelectedCount;
    @BindView(R.id.imageViewPullMe)
    ImageView mImageViewPullMe;
    @BindView(R.id.sliding_panel_header)
    View mSlidingPanelHeader;
    @BindView(R.id.buttonSearch)
    Button mButtonSearch;
    @BindView(R.id.buttonReports)
    Button mButtonReports;
    @BindView(R.id.buttonAddFilter)
    Button mButtonAddFilter;
    @BindView(R.id.buttonClearfilters)
    Button mButtonClearFilters;
    boolean isNewVersion = false;
    private AdapterTransactions adapter;
    private int contextMenuTarget = -1;
    private boolean isInSelectionMode;
    private TransactionsDAO mTransactionsDAO;
    private StickyHeaderDecoration stickyHeaderDecoration;
    private boolean mSumsLoaded;

    public static FragmentTransactions newInstance(String forceUpdateParam, int layoutID) {
        FragmentTransactions fragment = new FragmentTransactions();
        Bundle args = new Bundle();
        args.putString(FORCE_UPDATE_PARAM, forceUpdateParam);
        args.putInt(LAYOUT_NAME_PARAM, layoutID);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        setUpdateListsEvents(this);
    }

    public AdapterTransactions getAdapter() {
        return adapter;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        mSumsLoaded = false;
        Intent intent = getActivity().getIntent();
        if (intent.getBooleanExtra(FgConst.LOCK_SLIDINGUP_PANEL, false) & !BuildConfig.DEBUG) {
            mSlidingLayoutTransactions.setEnabled(false);
        }

        mTransactionsDAO = TransactionsDAO.getInstance(getActivity());

        FgLinearLayoutManager layoutManagerFilters = new FgLinearLayoutManager(getActivity());
        recyclerViewFilters.setLayoutManager(layoutManagerFilters);

        adapter = new AdapterTransactions(recyclerView, this, getActivity());
        adapter.setHasStableIds(true);
        adapter.setmOnTransactionItemClickListener(this);
        adapterFilters = new AdapterFilters(getActivity(), this, new AdapterFilters.IEditFilterListener() {
            @Override
            public void OnEditClick(AbstractFilter filter) {
                editFilter(filter);
            }
        });
        adapterFilters.setHasStableIds(true);

        stickyHeaderDecoration = new StickyHeaderDecoration(adapter);

        recyclerView.setAdapter(adapter);
        recyclerView.addItemDecoration(stickyHeaderDecoration);
        recyclerViewFilters.setAdapter(adapterFilters);

        switchAllFilters.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (AbstractFilter filter : adapterFilters.getFilterList()) {
                    filter.setEnabled(switchAllFilters.isChecked());
                }

                FragmentTransactions.this.onFilterChange(true);
            }
        });


        if (!CreateFilterListFromIntent()) {
            loadFilters();

            onFilterChange(true);
        }

        mSlidingLayoutTransactions.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {
                float alpha = Math.max(0, Math.abs(1 - slideOffset * 25));
                mImageViewPullMe.setAlpha(alpha);
            }

            @Override
            public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {
                if (previousState != newState && newState == SlidingUpPanelLayout.PanelState.COLLAPSED) {
                    onSelectionChange(adapter.getSelectedCount());
                }
//                if (previousState == SlidingUpPanelLayout.PanelState.HIDDEN & newState == SlidingUpPanelLayout.PanelState.COLLAPSED) {
//                    animatePullMe();
//                } else if (previousState == SlidingUpPanelLayout.PanelState.COLLAPSED & newState == SlidingUpPanelLayout.PanelState.HIDDEN) {
//                    animatePullMeReverse();
//                }
            }
        });

        mEditTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.setSearchString(s.toString());
                fullUpdate(-1);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mImageButtonClearSearchString.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mEditTextSearch.getText().toString().isEmpty()) {
                    mEditTextSearch.setText("");
                    fullUpdate(-1);
                } else {
                    hideSearchView();
                }
            }
        });

        layoutSumTable.getViewTreeObserver().addOnGlobalLayoutListener(
                new SumsTableOnGlobalLayoutListener(getActivity(), layoutSumTable, mSlidingLayoutTransactions));

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

    @Override
    public void onResume() {
        super.onResume();
        registerForContextMenu(recyclerViewFilters);

        initFabMenu();
        mTextViewSelectedCount.setVisibility(isInSelectionMode? View.VISIBLE : View.GONE);

    }

    private boolean isReportsPurchased() {
        if (getActivity() != null && getActivity() instanceof ActivityMain) {
            Log.d(TAG, "activity is not null");
            Log.d(TAG, "mReportsPurchased is " + String.valueOf(((ActivityMain) getActivity()).mReportsPurchased));
            return ((ActivityMain) getActivity()).mReportsPurchased;
        } else {
            return false;
        }
    }

    private void initFabMenu() {
        Context context = getActivity();
        mFabGoTop.setImageDrawable(getActivity().getDrawable(R.drawable.ic_arrow_up_white));
        fabMenuSelection.getMenuIconView().setImageDrawable(getActivity().getDrawable(R.drawable.ic_check_circle_white));
        fabMenuSelection.hideMenu(false);
        isInSelectionMode = false;

        FabMenuSelectionItemClickListener fabMenuSelectionItemClickListener = new FabMenuSelectionItemClickListener();
        mFabSelectAll.setOnClickListener(fabMenuSelectionItemClickListener);
        mFabUnselectAll.setOnClickListener(fabMenuSelectionItemClickListener);
        mFabDeleteSelected.setOnClickListener(fabMenuSelectionItemClickListener);
        mFabEditSelected.setOnClickListener(fabMenuSelectionItemClickListener);
        mFabExportSelected.setOnClickListener(fabMenuSelectionItemClickListener);

        mFabGoTop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FgLinearLayoutManager linearLayoutManager = (FgLinearLayoutManager) recyclerView.getLayoutManager();
                if (adapter.getTransactionList().size() > 0) {
                    linearLayoutManager.scrollToPositionWithOffset(0, 0);
                }
            }
        });

        fabMenuSelection.setClosedOnTouchOutside(true);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                LinearLayoutManager layoutManager = ((LinearLayoutManager) recyclerView.getLayoutManager());
                int firstVisiblePosition = layoutManager.findFirstVisibleItemPosition();
                if (firstVisiblePosition < 10) {
                    mFabGoTop.hide(true);
                } else {
                    if (PreferenceManager.getDefaultSharedPreferences(getActivity()).getBoolean("show_tr_go_top_button", true)) {
                        mFabGoTop.show(true);
                    }
                }
            }
        });

        mButtonSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSearchView();
            }
        });
    }

    private void saveFilters() {
        StringBuilder sb = new StringBuilder();
        for (AbstractFilter filter : adapterFilters.getFilterList()) {
            if (!filter.getClass().equals(AccountFilter.class) || !((AccountFilter) filter).isSystem()) {
                sb.append(filter.getId());
                sb.append("/");
                sb.append(filter.getModelType());
                sb.append("/");
                sb.append(filter.getEnabled());
                sb.append("/");
                sb.append(filter.saveToString());
                sb.append(";");
            }
        }
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        preferences.edit().putString("filters", sb.toString()).apply();
    }

    private boolean CreateFilterListFromIntent() {
        Intent intent = getActivity().getIntent();

        ArrayList<AbstractFilter> filters = intent.getParcelableArrayListExtra("filter_list");

        if (filters == null || filters.isEmpty()) {
            return false;
        } else {
            adapterFilters.setFilterList(filters);
            onFilterChange(false);
            return true;
        }
    }

    private void loadFilters() {
        adapterFilters.getFilterList().clear();
        adapterFilters.getFilterList().addAll(FilterManager.loadFiltersFromPreferences(getActivity()));
    }

    private void setAccountSetFilter() {
        if (adapterFilters.getFilterList().size() > 0
                && adapterFilters.getFilterList().get(0).getClass().equals(AccountFilter.class)
                && ((AccountFilter) adapterFilters.getFilterList().get(0)).isSystem()) {
            adapterFilters.getFilterList().remove(0);
        }

        //Если в списке фильтров уже есть фильтр по счету, то фильтр набора счетов не добавляем
        for (AbstractFilter filter : adapterFilters.getFilterList()) {
            if (filter.getClass().equals(AccountFilter.class)) {
                return;
            }
        }

        AccountsSet currentAccountsSet = AccountsSetManager.getInstance().getCurrentAccountSet(getContext());
        List<Long> accountsIDs = currentAccountsSet.getAccountsIDsList();
        if (accountsIDs.size() > 0) {
            AccountFilter accountFilter = new AccountFilter(0);
            accountFilter.addList(accountsIDs);
            accountFilter.setSystem(true);
            adapterFilters.getFilterList().add(0, accountFilter);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RequestCodes.REQUEST_CODE_EDIT_TRANSACTION & resultCode == RESULT_OK & data != null) {
            Toast.makeText(getActivity(), R.string.ttl_transaction_splitted, Toast.LENGTH_SHORT).show();
        }
        if (data != null && resultCode == RESULT_OK && requestCode == RequestCodes.REQUEST_CODE_SELECT_MODEL) {
            IAbstractModel model = data.getParcelableExtra("model");
            if (adapter.getSelectedCount() >= 0 & data.getStringArrayListExtra(FgConst.SELECTED_TRANSACTIONS_IDS) != null) {
                switch (model.getModelType()) {
                    case IAbstractModel.MODEL_TYPE_ACCOUNT:
                    case IAbstractModel.MODEL_TYPE_PAYEE:
                    case IAbstractModel.MODEL_TYPE_CATEGORY:
                    case IAbstractModel.MODEL_TYPE_PROJECT:
                    case IAbstractModel.MODEL_TYPE_LOCATION:
                    case IAbstractModel.MODEL_TYPE_DEPARTMENT:
                    case IAbstractModel.MODEL_TYPE_SIMPLEDEBT:
                        try {
                            TransactionsDAO.getInstance(getActivity()).bulkUpdateEntity(data.getStringArrayListExtra(FgConst.SELECTED_TRANSACTIONS_IDS),model, true);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                }
            }
        }
        if (requestCode == RequestCodes.REQUEST_CODE_BULK_SELECT_MODEL) {
            if (resultCode == RESULT_OK && data != null) {
                long filterID = data.getLongExtra("filterID", -1);
                ArrayList<Long> checkedIDs = (ArrayList<Long>) data.getSerializableExtra("checked_ids");

                NestedModelFilter filter = null;
                for (AbstractFilter f : adapterFilters.getFilterList()) {
                    if (f instanceof NestedModelFilter) {
                        if (f.getId() == filterID) {
                            filter = (NestedModelFilter) f;
                        }
                    }
                }

                if (!checkedIDs.isEmpty()) {
                    if (filter != null) {
                        filter.getIDsSet().clear();
                        filter.getIDsSet().addAll(checkedIDs);
                    }
                } else {
                    adapterFilters.getFilterList().remove(filter);
                }

                adapterFilters.notifyDataSetChanged();
                FragmentTransactions.this.onFilterChange(true);
            } else {
                for (int i = adapterFilters.getItemCount() - 1; i >= 0; i--) {
                    AbstractFilter filter = adapterFilters.getFilterList().get(i);
                    if (filter.getClass().equals(NestedModelFilter.class)) {
                        if ((filter).getIDsSet().isEmpty()) {
                            adapterFilters.getFilterList().remove(i);
                        }
                    }

                }
                adapterFilters.notifyDataSetChanged();
                FragmentTransactions.this.onFilterChange(true);
            }
        }
    }

    @Override
    public void onTransactionItemClick(Transaction transaction) {
        Intent intent = new Intent(getActivity(), ActivityEditTransaction.class);
        intent.putExtra("transaction", transaction);
        getActivity().startActivityForResult(intent, RequestCodes.REQUEST_CODE_EDIT_TRANSACTION);
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onSelectionChange(int selectedCount) {
        if (selectedCount > 0) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    fabMenuSelection.showMenu(true);
                }
            }, 200);
            mTextViewSelectedCount.setVisibility(View.VISIBLE);
            mTextViewSelectedCount.setText(String.format("%d %s", selectedCount,getString(R.string.ttl_selected)));
        } else {
            fabMenuSelection.hideMenu(true);
            mTextViewSelectedCount.setVisibility(View.GONE);
            mTextViewSelectedCount.setText("");
        }
        isInSelectionMode = selectedCount > 0;

        ListSumsByCabbage listSumsByCabbage;
        try {
            listSumsByCabbage = mTransactionsDAO.getGroupedSums(new FilterListHelper(adapterFilters.getFilterList(),
                    mEditTextSearch.getText().toString(), getActivity()), true, adapter.getSelectedTransactionsIDsAsLong(), getActivity());
        } catch (Exception e) {
            listSumsByCabbage = new ListSumsByCabbage();
        }
        updateSums(listSumsByCabbage);
    }

    private void initSlidePanelButtons() {
        mButtonAddFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builderSingle = new AlertDialog.Builder(getActivity());
                builderSingle.setTitle(getActivity().getResources().getString(R.string.ttl_new_filter));

                final ArrayAdapter<StringIntItem> arrayAdapter = new ArrayAdapter<>(
                        getActivity(),
                        android.R.layout.select_dialog_singlechoice);

                Resources res = getActivity().getResources();
                arrayAdapter.add(new StringIntItem(res.getString(R.string.ent_amount), IAbstractModel.MODEL_TYPE_AMOUNT_FILTER));
                arrayAdapter.add(new StringIntItem(res.getString(R.string.ent_date_range), IAbstractModel.MODEL_TYPE_DATE_RANGE));
                arrayAdapter.add(new StringIntItem(res.getString(R.string.ent_account), IAbstractModel.MODEL_TYPE_ACCOUNT));
                arrayAdapter.add(new StringIntItem(res.getString(R.string.ent_payee_or_payer), IAbstractModel.MODEL_TYPE_PAYEE));
                arrayAdapter.add(new StringIntItem(res.getString(R.string.ent_category), IAbstractModel.MODEL_TYPE_CATEGORY));
                arrayAdapter.add(new StringIntItem(res.getString(R.string.ent_project), IAbstractModel.MODEL_TYPE_PROJECT));
                arrayAdapter.add(new StringIntItem(res.getString(R.string.ent_location), IAbstractModel.MODEL_TYPE_LOCATION));
                arrayAdapter.add(new StringIntItem(res.getString(R.string.ent_department), IAbstractModel.MODEL_TYPE_DEPARTMENT));
                arrayAdapter.add(new StringIntItem(res.getString(R.string.ent_debt), IAbstractModel.MODEL_TYPE_SIMPLEDEBT));

                builderSingle.setNegativeButton(
                        getActivity().getResources().getString(android.R.string.cancel),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });

                builderSingle.setAdapter(
                        arrayAdapter,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ListView lw = ((AlertDialog) dialog).getListView();
                                StringIntItem checkedItem = (StringIntItem) lw.getAdapter().getItem(which);
                                addFilter(checkedItem.getID(), -1);
                            }
                        });
                builderSingle.show();
            }
        });
        mButtonClearFilters.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adapterFilters.clearData();
            }
        });
        mButtonReports.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent;
                if (isReportsPurchased()) {
                    intent = new Intent(getActivity(), ActivityReports.class);
                    intent.putParcelableArrayListExtra("filter_list", adapterFilters.getFilterList());
                    mSlidingLayoutTransactions.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                    startActivity(intent);
                } else {
                    intent = new Intent(getActivity(), ActivityPro.class);
                    startActivityForResult(intent, RequestCodes.REQUEST_CODE_OPEN_PRO);
                }
            }
        });
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        initSlidePanelButtons();

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

    }

//    void showSlidingPanelExample() {
//        if (isNewVersion) {
//            new Handler().postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    ActivityMain activityMain = (ActivityMain) getActivity();
//                    activityMain.mAppBarLayout.setExpanded(false, true);
//                    new Handler().postDelayed(new Runnable() {
//                        @Override
//                        public void run() {
//                            mSlidingLayoutTransactions.setAnchorPoint(0.3f);
//                            mSlidingLayoutTransactions.setPanelState(SlidingUpPanelLayout.PanelState.ANCHORED);
//
//                            new Handler().postDelayed(new Runnable() {
//                                @Override
//                                public void run() {
//                                    mSlidingLayoutTransactions.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
//                                    mSlidingLayoutTransactions.setAnchorPoint(0.5f);
//                                }
//                            }, 2000);
//                        }
//                    }, 500);
//                }
//            }, 1000);
//        }
//        isNewVersion = false;
//    }

    @Override
    public void onPause() {
        unregisterForContextMenu(recyclerViewFilters);
        super.onPause();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater menuInflater = getActivity().getMenuInflater();
        if (v.getId() == R.id.recycler_view) {
            contextMenuTarget = CONTEXT_MENU_TRANSACTIONS;
            menuInflater.inflate(R.menu.context_menu_transactions, menu);
        } else {
            contextMenuTarget = CONTEXT_MENU_FILTERS;
            menuInflater.inflate(R.menu.context_menu_filters, menu);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (getUserVisibleHint()) {
            switch (contextMenuTarget) {
                case CONTEXT_MENU_TRANSACTIONS:
                    initContextMenuTransactions(item);
                    break;
            }
            return true;
        } else
            return false;
    }

    private void editFilter(AbstractFilter filter) {
        switch (filter.getModelType()) {
            case IAbstractModel.MODEL_TYPE_ACCOUNT:
                editFilterAccounts((AccountFilter) filter);
                break;
            case IAbstractModel.MODEL_TYPE_PROJECT:
            case IAbstractModel.MODEL_TYPE_PAYEE:
            case IAbstractModel.MODEL_TYPE_LOCATION:
            case IAbstractModel.MODEL_TYPE_CATEGORY:
            case IAbstractModel.MODEL_TYPE_SIMPLEDEBT:
            case IAbstractModel.MODEL_TYPE_DEPARTMENT:
                editNestedModelFilter((NestedModelFilter) filter);
                break;
        }
    }

    private void editFilterAccounts(final AccountFilter filter) {
        AlertDialog dialog;
        final AccountsDAO accountsDAO = AccountsDAO.getInstance(getActivity());
        List<Account> accountList;
        try {
            accountList = accountsDAO.getAllAccounts(PreferenceManager.getDefaultSharedPreferences(getActivity()).getBoolean(FgConst.PREF_SHOW_CLOSED_ACCOUNTS, true));
        } catch (Exception e) {
            accountList = new ArrayList<>();
        }
        CharSequence[] items = new CharSequence[accountList.size()];
        boolean[] checkedItems = new boolean[accountList.size()];

        for (int i = 0; i < accountList.size(); i++) {
            checkedItems[i] = filter.getIDsSet().contains(accountList.get(i).getID());
            items[i] = accountList.get(i).getName();
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getActivity().getResources().getString(R.string.ttl_select_accounts));
        builder.setMultiChoiceItems(items, checkedItems,
                new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog1, int indexSelected, boolean isChecked) {

                    }
                })
                // Set the action buttons
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog1, int id) {
                        AlertDialog ad = (AlertDialog) dialog1;
                        String name;
                        long accId;
                        for (int i = 0; i < ad.getListView().getCount(); i++) {
                            name = ad.getListView().getAdapter().getItem(i).toString();
                            try {
                                accId = accountsDAO.getModelByName(name).getID();
                            } catch (Exception e) {
                                accId = -1;
                            }
                            if (ad.getListView().isItemChecked(i)) {
                                filter.addAccount(accId);
                            } else {
                                filter.removeAccount(accId);
                            }
                        }
                        adapterFilters.notifyDataSetChanged();
                        FragmentTransactions.this.onFilterChange(true);
                    }
                });

        dialog = builder.create();//AlertDialog dialog; create like this outside onClick
        dialog.show();
    }

    private void editNestedModelFilter(final NestedModelFilter filter) {
        Intent intent = new Intent(getActivity(), ActivityList.class);
        intent.putExtra("showHomeButton", false);
        intent.putExtra("model", BaseModel.createModelByType(filter.getModelType()));
        intent.putExtra("filterID", filter.getId());
        intent.putExtra("checked_ids", filter.getIDsSet());
        intent.putExtra("requestCode", RequestCodes.REQUEST_CODE_BULK_SELECT_MODEL);
        getActivity().startActivityForResult(intent, RequestCodes.REQUEST_CODE_BULK_SELECT_MODEL);
    }

    @SuppressWarnings("WrongConstant")
    private void initContextMenuTransactions(final MenuItem item) {
        final ContextMenuRecyclerView.RecyclerContextMenuInfo info = (ContextMenuRecyclerView.RecyclerContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case R.id.action_add: {
                Intent intent = new Intent(getActivity(), ActivityEditTransaction.class);
                intent.putExtra("transaction", new Transaction(PrefUtils.getDefDepID(getActivity())));
                getActivity().startActivityForResult(intent, RequestCodes.REQUEST_CODE_EDIT_TRANSACTION);
                break;
            }
            case R.id.action_filter_on_selected:
            case R.id.action_select_on_selected: {
                Transaction transaction = mTransactionsDAO.getTransactionByID(info.id);

                final AlertDialog.Builder builderSingle = new AlertDialog.Builder(getActivity());
                builderSingle.setTitle(getActivity().getResources().getString(R.string.ttl_new_filter));

                final ArrayAdapter<IAbstractModel> arrayAdapter = new ArrayAdapter<>(
                        getActivity(),
                        android.R.layout.select_dialog_singlechoice);
                arrayAdapter.addAll(FilterUtils.CreateModelsListFromTransaction(transaction, getActivity()));

                builderSingle.setNegativeButton(
                        getActivity().getResources().getString(android.R.string.cancel),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });

                builderSingle.setAdapter(
                        arrayAdapter,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ListView lw = ((AlertDialog) dialog).getListView();
                                IAbstractModel checkedModel = (IAbstractModel) lw.getAdapter().getItem(which);
                                switch (item.getItemId()) {
                                    case R.id.action_filter_on_selected:
                                        addFilter(checkedModel.getModelType(), checkedModel.getID());
                                        break;
                                    case R.id.action_select_on_selected:
                                        adapter.selectByModel(checkedModel);
                                        break;
                                }
                            }
                        });
                builderSingle.show();
                break;
            }
            case R.id.action_split: {
                Transaction srcTransaction = mTransactionsDAO.getTransactionByID(info.id);

                if (srcTransaction.getDestAccountID() >= 0) {
                    Toast.makeText(getActivity(), getString(R.string.err_split_of_transfers_not_supported), Toast.LENGTH_SHORT).show();
                } else {
                    Intent intent = new Intent(getActivity(), ActivityEditTransaction.class);
                    Transaction transaction = new Transaction(srcTransaction);
                    transaction.setAmount(BigDecimal.ZERO, srcTransaction.getTransactionType());
                    transaction.setCategoryID(-1);
                    intent.putExtra("transaction", transaction);
                    intent.putExtra("src_transaction", srcTransaction);
                    intent.putExtra("focus_to_category", true);
                    getActivity().startActivityForResult(intent, RequestCodes.REQUEST_CODE_EDIT_TRANSACTION);
                }
                break;
            }
            case R.id.action_dulpicate: {
                Transaction transaction = new Transaction(mTransactionsDAO.getTransactionByID(info.id));
                transaction.setDateTime(new Date());
                try {
                    mTransactionsDAO.createModel(transaction);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            }
            case R.id.action_create_template: {
                Template template = new Template();
                Transaction transaction = mTransactionsDAO.getTransactionByID(info.id);
                template.extractFromTransaction(transaction);
                Intent intent = new Intent(getActivity(), ActivityEditTransaction.class);
                intent.putExtra("template", template);
                intent.putExtra("transaction", TransactionManager.templateToTransaction(template, getActivity()));
                startActivity(intent);
                break;
            }
            case R.id.action_edit: {
                Intent intent = new Intent(getActivity(), ActivityEditTransaction.class);
                intent.putExtra("transaction", mTransactionsDAO.getTransactionByID(info.id));
                getActivity().startActivityForResult(intent, RequestCodes.REQUEST_CODE_EDIT_TRANSACTION);
                break;
            }
            case R.id.action_delete: {
                final List<IAbstractModel> transactionsToDelete = new ArrayList<>();
                transactionsToDelete.add(mTransactionsDAO.getTransactionByID(info.id));
                final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
                alertDialogBuilder
                        .setTitle(R.string.ttl_confirm_action)
                        .setMessage(R.string.msg_confirm_delete_transaction)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                TransactionsDAO.getInstance(getActivity()).bulkDeleteModel(transactionsToDelete, true);
                                onSelectionChange(0);
                            }
                        }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();
                break;
            }
        }
    }

    private void addFilter(int filtType, long modelID) {
        AbstractFilter filter = null;
        long id = adapterFilters.getFilterList().size();
        switch (filtType) {
            case IAbstractModel.MODEL_TYPE_AMOUNT_FILTER:
                filter = new AmountFilter(id);
                break;
            case IAbstractModel.MODEL_TYPE_DATE_RANGE:
                filter = new DateRangeFilter(id, getActivity());
                break;
            case IAbstractModel.MODEL_TYPE_ACCOUNT:
                filter = new AccountFilter(id);
                break;
            case IAbstractModel.MODEL_TYPE_PAYEE:
            case IAbstractModel.MODEL_TYPE_PROJECT:
            case IAbstractModel.MODEL_TYPE_DEPARTMENT:
            case IAbstractModel.MODEL_TYPE_LOCATION:
            case IAbstractModel.MODEL_TYPE_CATEGORY:
            case IAbstractModel.MODEL_TYPE_SIMPLEDEBT:
                filter = new NestedModelFilter(id, filtType);
                break;
        }
        if (filter != null) {
            if (modelID >= 0) {
                filter.getIDsSet().add(modelID);
            }
            adapterFilters.getFilterList().add(filter);
            adapterFilters.notifyItemInserted(adapterFilters.getFilterList().size() - 1);
            if (modelID < 0) {
                saveFilters();
                editFilter(filter);
            }
            onFilterChange(true);
        }

    }

    @Override
    public void onFilterChange(boolean save) {
        if (isFiltersAdded()) {
            switchAllFilters.setVisibility(View.VISIBLE);
        } else {
            switchAllFilters.setVisibility(View.GONE);
        }
        switchAllFilters.setChecked(isFiltersEnabled());
        if (save) {
            saveFilters();
        }
        fullUpdate(-1);
        onSelectionChange(adapter.getSelectedCount());
    }

    private boolean isFiltersEnabled() {
        boolean result = false;
        for (AbstractFilter filter : adapterFilters.getFilterList()) {
            if (!filter.getClass().equals(AccountFilter.class) || !((AccountFilter) filter).isSystem()) {
                result = result | filter.getEnabled();
            }
        }
        return result;
    }

    private boolean isFiltersAdded() {
        return adapterFilters.getFilterList().size() > 0;
    }

    private void showSearchView() {
        mCardViewSearch.setVisibility(View.VISIBLE);
        mSlidingLayoutTransactions.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        mEditTextSearch.requestFocus();
        if (getActivity() != null) {
            final InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            mEditTextSearch.postDelayed(
                    new Runnable() {
                        @Override
                        public void run() {
                            if (imm != null) {
                                imm.showSoftInput(mEditTextSearch, 0);
                            }
                        }
                    }, 300);
        }
    }

    void hideSearchView() {
        mCardViewSearch.setVisibility(View.GONE);
        mEditTextSearch.setText("");

        if (getActivity() != null) {
            View view = getActivity().getCurrentFocus();
            if (view != null) {
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
            }
        }
    }

    @Override
    public synchronized void loadData(UpdateMainListsRwHandler handler, long itemID) {

//        Debug.startMethodTracing("TransactionsLoadData");

        stickyHeaderDecoration.clearHeaderCache();
        setAccountSetFilter();

        if (recyclerView == null) {
            return;
        }

        FgLinearLayoutManager linearLayoutManager = (FgLinearLayoutManager) recyclerView.getLayoutManager();
        int currentItem = linearLayoutManager.findFirstVisibleItemPosition();

        int count = Math.max(adapter.getItemCount(), NUMBER_ITEMS_TO_BE_LOADED);
        List<Transaction> transactions;
        try {
            long curTime = System.currentTimeMillis();
            transactions = mTransactionsDAO.getRangeTransactions(0, count, new FilterListHelper(adapterFilters.getFilterList(), mEditTextSearch.getText().toString(), getActivity()), getActivity());
            curTime = System.currentTimeMillis() - curTime;
            Log.d(TAG, "Query length" + String.valueOf(curTime));
        } catch (Exception e) {
            transactions = new ArrayList<>();
        }
        adapter.clearCaches();
        adapter.setTransactionList(transactions);
        adapter.endOfList = false;
        adapter.setLoaded();
//        handler.sendMessage(handler.obtainMessage(UpdateMainListsRwHandler.UPDATE_LIST, (int) itemID, 0));

        if (itemID >= 0) {
            for (int i = 0; i < adapter.getTransactionList().size(); i++) {
                if (adapter.getTransactionList().get(i).getID() == itemID) {
                    handler.sendMessage(handler.obtainMessage(UpdateMainListsRwHandler.UPDATE_LIST, i, 0));
                }
            }
        } else {
            handler.sendMessage(handler.obtainMessage(UpdateMainListsRwHandler.UPDATE_LIST, currentItem, 0));
        }
//        Debug.stopMethodTracing();
    }

    @Override
    public void loadSums(UpdateMainListsRwHandler handler) {
//        Debug.startMethodTracing("TransactionsLoadSums");
        ListSumsByCabbage listSumsByCabbage;
        try {
            listSumsByCabbage = mTransactionsDAO.getGroupedSums(new FilterListHelper(adapterFilters.getFilterList(),
                    mEditTextSearch.getText().toString(), getActivity()), true, adapter.getSelectedTransactionsIDsAsLong(), getActivity());
        } catch (Exception e) {
            listSumsByCabbage = new ListSumsByCabbage();
        }
        handler.sendMessage(handler.obtainMessage(UpdateMainListsRwHandler.UPDATE_SUMS, listSumsByCabbage));
//        Debug.stopMethodTracing();
    }

    @Override
    public void updateLists(long itemID) {
        adapter.notifyDataSetChanged();
        adapterFilters.notifyDataSetChanged();
        if (recyclerView != null && itemID >= 0) {
            FgLinearLayoutManager linearLayoutManager = (FgLinearLayoutManager) recyclerView.getLayoutManager();
            linearLayoutManager.scrollToPosition((int) itemID);
        }
    }

    @Override
    public void updateSums(ListSumsByCabbage listSumsByCabbage) {
        SumsManager.updateSummaryTable(getActivity(), layoutSumTable, false, listSumsByCabbage, CabbagesDAO.getInstance(getActivity()).getCabbagesMap(), null);
        mSumsLoaded = true;
    }

    @Override
    public synchronized void loadMore(int numberItems, ILoadMoreFinish loadMoreFinish) {
        int start = adapter.getTransactionList().size();
        List<Transaction> rangeTransactions = new ArrayList<>();
        if (!adapter.endOfList) {
            try {
                long curTime = System.currentTimeMillis();
                rangeTransactions = TransactionsDAO.getInstance(getActivity()).getRangeTransactions(start, numberItems, new FilterListHelper(adapterFilters.getFilterList(), mEditTextSearch.getText().toString(), getActivity()), getActivity());
                curTime = System.currentTimeMillis() - curTime;
                Log.d(TAG, "Query length " + String.valueOf(curTime));
            } catch (Exception e) {
                rangeTransactions = new ArrayList<>();
            }
        }
        adapter.endOfList = rangeTransactions.size() < numberItems;
        adapter.addTransactions(rangeTransactions, false);
//        adapter.notifyDataSetChanged();
        loadMoreFinish.onLoadMoreFinish();
        adapter.setLoaded();
    }

    private class FabMenuSelectionItemClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.fabSelectAll: {
                    adapter.selectAll();
                    break;
                }
                case R.id.fabUnselectAll: {
                    adapter.unselectAll();
                    break;
                }
                case R.id.fabEditSelected: {
                    final AlertDialog.Builder builderSingle = new AlertDialog.Builder(getActivity());
                    builderSingle.setTitle(getActivity().getResources().getString(R.string.ttl_change_selected_param));

                    final ArrayAdapter<StringIntItem> arrayAdapter = new ArrayAdapter<>(
                            getActivity(),
                            android.R.layout.select_dialog_singlechoice);

                    Resources res = getActivity().getResources();
                    arrayAdapter.add(new StringIntItem(res.getString(R.string.ent_account), IAbstractModel.MODEL_TYPE_ACCOUNT));
                    arrayAdapter.add(new StringIntItem(res.getString(R.string.ent_payee_or_payer), IAbstractModel.MODEL_TYPE_PAYEE));
                    arrayAdapter.add(new StringIntItem(res.getString(R.string.ent_category), IAbstractModel.MODEL_TYPE_CATEGORY));
                    arrayAdapter.add(new StringIntItem(res.getString(R.string.ent_project), IAbstractModel.MODEL_TYPE_PROJECT));
                    arrayAdapter.add(new StringIntItem(res.getString(R.string.ent_location), IAbstractModel.MODEL_TYPE_LOCATION));
                    arrayAdapter.add(new StringIntItem(res.getString(R.string.ent_department), IAbstractModel.MODEL_TYPE_DEPARTMENT));
                    arrayAdapter.add(new StringIntItem(res.getString(R.string.ent_debt), IAbstractModel.MODEL_TYPE_SIMPLEDEBT));

                    builderSingle.setNegativeButton(
                            getActivity().getResources().getString(android.R.string.cancel),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });

                    builderSingle.setAdapter(
                            arrayAdapter,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    ListView lw = ((AlertDialog) dialog).getListView();
                                    StringIntItem checkedItem = (StringIntItem) lw.getAdapter().getItem(which);
                                    Intent intent;
                                    switch (checkedItem.getID()) {
                                        case IAbstractModel.MODEL_TYPE_ACCOUNT:
                                            intent = new Intent(getActivity(), ActivityAccounts.class);
                                            intent.putExtra("showHomeButton", false);
                                            intent.putExtra("model", new Account());
                                            intent.putExtra("destAccount", false);
                                            intent.putStringArrayListExtra(FgConst.SELECTED_TRANSACTIONS_IDS, adapter.getSelectedTransactionsIDs());
                                            startActivityForResult(intent, REQUEST_CODE_SELECT_MODEL);
                                            break;
                                        case IAbstractModel.MODEL_TYPE_PAYEE:
                                        case IAbstractModel.MODEL_TYPE_CATEGORY:
                                        case IAbstractModel.MODEL_TYPE_PROJECT:
                                        case IAbstractModel.MODEL_TYPE_LOCATION:
                                        case IAbstractModel.MODEL_TYPE_DEPARTMENT:
                                        case IAbstractModel.MODEL_TYPE_SIMPLEDEBT:
                                            intent = new Intent(getActivity(), ActivityList.class);
                                            intent.putExtra("showHomeButton", false);
                                            intent.putExtra("model", BaseModel.createModelByType(checkedItem.getID()));
                                            intent.putExtra("requestCode", REQUEST_CODE_SELECT_MODEL);
                                            intent.putStringArrayListExtra(FgConst.SELECTED_TRANSACTIONS_IDS, adapter.getSelectedTransactionsIDs());
                                            startActivityForResult(intent, REQUEST_CODE_SELECT_MODEL);
                                            break;
                                    }
                                }
                            });
                    builderSingle.show();
                    break;
                }
                case R.id.fabExportSelected: {
                    Intent intent = new Intent(getActivity(), ActivityExportCSV.class);
                    intent.putParcelableArrayListExtra("transactions", adapter.getSelectedTransactions());
                    startActivity(intent);
                    break;
                }
                case R.id.fabDeleteSelected: {
                    final List<IAbstractModel> transactionsToDelete = adapter.removeSelectedTransactions();
                    final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
                    alertDialogBuilder
                            .setTitle(R.string.ttl_confirm_action)
                            .setMessage(R.string.msg_confirm_delete_selected_transactions)
                            .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    TransactionsDAO.getInstance(getActivity()).bulkDeleteModel(transactionsToDelete, true);
                                    onSelectionChange(0);
                                }
                            }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).show();

                    break;
                }
            }
            fabMenuSelection.close(true);
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
