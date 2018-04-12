package com.yoshione.fingen;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Animatable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import android.util.Log;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.yoshione.fingen.adapter.AdapterBudget;
import com.yoshione.fingen.classes.ListSumsByCabbage;
import com.yoshione.fingen.classes.SumsByCabbage;
import com.yoshione.fingen.dao.AccountsDAO;
import com.yoshione.fingen.dao.BudgetCreditsDAO;
import com.yoshione.fingen.dao.BudgetDAO;
import com.yoshione.fingen.dao.CabbagesDAO;
import com.yoshione.fingen.dao.CategoriesDAO;
import com.yoshione.fingen.dao.CreditsDAO;
import com.yoshione.fingen.dao.TransactionsDAO;
import com.yoshione.fingen.filters.AbstractFilter;
import com.yoshione.fingen.filters.AccountFilter;
import com.yoshione.fingen.filters.DateRangeFilter;
import com.yoshione.fingen.filters.FilterListHelper;
import com.yoshione.fingen.interfaces.IAbstractModel;
import com.yoshione.fingen.managers.CategoryManager;
import com.yoshione.fingen.managers.DebtsManager;
import com.yoshione.fingen.managers.FilterManager;
import com.yoshione.fingen.managers.SumsManager;
import com.yoshione.fingen.model.Account;
import com.yoshione.fingen.model.BudgetForCategory;
import com.yoshione.fingen.model.Cabbage;
import com.yoshione.fingen.model.Category;
import com.yoshione.fingen.model.Credit;
import com.yoshione.fingen.utils.CNode;
import com.yoshione.fingen.utils.IconGenerator;
import com.yoshione.fingen.utils.RequestCodes;
import com.yoshione.fingen.utils.ScreenUtils;
import com.yoshione.fingen.widgets.ContextMenuRecyclerView;
import com.yoshione.fingen.widgets.ToolbarActivity;

import java.lang.ref.WeakReference;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

import static android.app.Activity.RESULT_OK;

/**
 * Created by Leonid on 12.03.2016.
 * FragmentBudget
 */
public class FragmentBudget extends Fragment implements AdapterBudget.IOnItemClickListener {
    private static final String TAG = "FragmentBudget";
    protected Handler handler;
    @BindView(R.id.recycler_view)
    ContextMenuRecyclerView recyclerView;
    AdapterBudget adapter;
    @BindView(R.id.recycler_view_summary)
    RecyclerView recyclerViewSummary;
    @BindView(R.id.layoutSummaryTable)
    TableLayout layoutSumTable;
//    @BindView(R.id.imageViewFilterSumIcon)
//    ImageView imageViewFilterSumIcon;
    @BindView(R.id.fabAddModel)
    FloatingActionButton fabAddCategory;
    @BindView(R.id.fabAddDebt)
    FloatingActionButton fabAddDebt;
    @BindView(R.id.fabCopyBudget)
    FloatingActionButton fabCopyBudget;
    @BindView(R.id.fabCreateFromFact)
    FloatingActionButton fabCreateFromFact;
    @BindView(R.id.fabClearBudget)
    FloatingActionButton fabClearBudget;
    @BindView(R.id.fabMenu)
    FloatingActionMenu fabMenu;
    @BindView(R.id.sliding_layout)
    SlidingUpPanelLayout mSlidingUpPanelLayout;
    @BindView(R.id.imageViewPullMe)
    ImageView mImageViewPullMe;
    private AdapterBudget adapterSummary;
    private ListSumsByCabbage ioSums;
    private boolean isIoSumsLoaded;
    private UpdateRwHandler updateRwHandler;
    private boolean isUpdating = false;
    private int mYear;
    private int mMonth;
    private Unbinder unbinder;
//    private static final AdapterBudget[] adapterBudgetArr = new AdapterBudget[]{null, null};

    public static FragmentBudget newInstance(int year, int month) {
        FragmentBudget frag = new FragmentBudget();
        Bundle args = new Bundle();
        args.putInt("year", year);
        args.putInt("month", month);
        frag.setArguments(args);

        return frag;
    }

    int getmYear() {
        return mYear;
    }

    int getmMonth() {
        return mMonth;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_budget, container, false);
        unbinder = ButterKnife.bind(this, view);

        mYear = getArguments().getInt("year", Calendar.getInstance().get(Calendar.YEAR));
        mMonth = getArguments().getInt("month", Calendar.getInstance().get(Calendar.MONTH));

        handler = new Handler();
        updateRwHandler = new UpdateRwHandler(this);

        adapter = new AdapterBudget(getActivity());
        adapter.setHasStableIds(true);
        adapter.setOnItemClickListener(this);
        adapter.setmYear(mYear);
        adapter.setmMonth(mMonth);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        registerForContextMenu(recyclerView);

        adapterSummary = new AdapterBudget(getActivity());
        adapterSummary.setHasStableIds(true);
        adapterSummary.setOnItemClickListener(this);
        adapterSummary.setmYear(mYear);
        adapterSummary.setmMonth(mMonth);

        LinearLayoutManager layoutManagerSummary = new LinearLayoutManager(getActivity());
        recyclerViewSummary.setLayoutManager(layoutManagerSummary);
        recyclerViewSummary.setAdapter(adapterSummary);

        initFabMenu();

        fullUpdate();

        layoutSumTable.getViewTreeObserver().addOnGlobalLayoutListener(
                new SumsTableOnGlobalLayoutListener(getActivity(), layoutSumTable, mSlidingUpPanelLayout));

//        if (getActivity() instanceof ActivityBudget) {
//            ((ActivityBudget) getActivity()).mAppBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
//                @Override
//                public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
//                    int verticalOffsetDp = Math.round(ScreenUtils.PxToDp(getActivity(), verticalOffset));
//                    if (verticalOffsetDp == -48) {
//                        animatePullMe();
//                    } else {
//                        animatePullMeReverse();
//                    }
//                }
//            });
//        }

        return view;
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

    private void initFabMenu() {
        Context context = getActivity();
        fabMenu.getMenuIconView().setImageDrawable(getActivity().getDrawable(R.drawable.ic_menu_white));
        fabAddCategory.setImageDrawable(context.getDrawable(R.drawable.ic_category_white));
        fabAddDebt.setImageDrawable(getActivity().getDrawable(R.drawable.ic_debt_white));
        fabCopyBudget.setImageDrawable(getActivity().getDrawable(R.drawable.ic_copy_white));
        fabCreateFromFact.setImageDrawable(getActivity().getDrawable(R.drawable.ic_create_budget_from_fact_white));
        fabClearBudget.setImageDrawable(getActivity().getDrawable(R.drawable.ic_trash_white));

        FabMenuItemClickListener fabMenuItemClickListener = new FabMenuItemClickListener();
        fabAddCategory.setOnClickListener(fabMenuItemClickListener);
        fabAddDebt.setOnClickListener(fabMenuItemClickListener);
        fabCopyBudget.setOnClickListener(fabMenuItemClickListener);
        fabCreateFromFact.setOnClickListener(fabMenuItemClickListener);
        fabClearBudget.setOnClickListener(fabMenuItemClickListener);

        fabMenu.setClosedOnTouchOutside(true);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (Math.abs(dy) > 4) {
                    if (dy > 0) {
                        fabMenu.hideMenu(true);
                    } else {
                        fabMenu.showMenu(true);
                    }
                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == RequestCodes.REQUEST_CODE_SELECT_MODEL && data != null) {
            IAbstractModel model = data.getParcelableExtra("model");
            switch (model.getModelType()) {
                case IAbstractModel.MODEL_TYPE_CATEGORY:
                    onItemClick((Category) model, mYear, mMonth, -1, -1, BigDecimal.ZERO);
                    break;
                case IAbstractModel.MODEL_TYPE_CREDIT:
                    Credit credit = (Credit) model;
                    Account account = AccountsDAO.getInstance(getActivity()).getAccountByID(credit.getAccountID());
                    Category category = new Category();

                    category.setID(AdapterBudget.BUDGET_ITEM_DEBTS_ROOT - credit.getID());
                    category.setName(DebtsManager.getAccount(credit, getActivity()).getName());
                    category.setColor(0);
                    category.setParentID(AdapterBudget.BUDGET_ITEM_DEBTS_ROOT);
                    category.setOrderNum(0);
                    category.setBudget(new BudgetForCategory(new ListSumsByCabbage(), AdapterBudget.INFO_TYPE_ALL_TOTAL));
                    onItemClick(category, mYear, mMonth, account.getCabbageId(), -1, BigDecimal.ZERO);
                    break;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onResume() {
        super.onResume();
        registerForContextMenu(recyclerView);
    }

    @Override
    public void onPause() {
        unregisterForContextMenu(recyclerView);
        super.onPause();
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (getUserVisibleHint()) {
            ActivityBudget activityBudget = (ActivityBudget) getActivity();
            final ContextMenuRecyclerView.RecyclerContextMenuInfo info = (ContextMenuRecyclerView.RecyclerContextMenuInfo) item.getMenuInfo();
            switch (item.getItemId()) {
                case R.id.action_copy_plan: {
                    String title = getActivity().getResources().getString(R.string.act_copy_budget);
                    int position = activityBudget.viewPager.getCurrentItem() - 1;
                    FragmentCopyBudget alertDialog = FragmentCopyBudget.newInstance(title,
                            activityBudget.convertPosToYear(position),
                            activityBudget.convertPosToMonth(position),
                            false);
                    alertDialog.setCopyBudgetDialogListener(new FragmentCopyBudget.ICopyBudgetDialogListener() {
                        @Override
                        public void onOkClick(int srcYear, int srcMonth, boolean replace) {
                            BudgetDAO.getInstance(FragmentBudget.this.getActivity())
                                    .copyBudget(srcYear, srcMonth, mYear, mMonth, replace, info.id);
                            long creditId = (info.id - AdapterBudget.BUDGET_ITEM_DEBTS_ROOT) * -1;
                            BudgetCreditsDAO.getInstance(FragmentBudget.this.getActivity())
                                    .copyBudget(srcYear, srcMonth, mYear, mMonth, replace, creditId);
                            FragmentBudget.this.fullUpdate();
                        }
                    });
                    alertDialog.show(activityBudget.getSupportFragmentManager(), "fragment_copy_budget");
                    break;
                }
                case R.id.action_create_from_fact: {
                    String titleCreateFrmFact = getActivity().getResources().getString(R.string.act_copy_budget);
                    int position1 = activityBudget.viewPager.getCurrentItem() - 1;
                    FragmentCopyBudget alertDialog1 = FragmentCopyBudget.newInstance(titleCreateFrmFact,
                            activityBudget.convertPosToYear(position1),
                            activityBudget.convertPosToMonth(position1),
                            false);
                    alertDialog1.setCopyBudgetDialogListener(new FragmentCopyBudget.ICopyBudgetDialogListener() {
                        @Override
                        public void onOkClick(int srcYear, int srcMonth, boolean replace) {
                            FragmentBudget.this.createBudgetFromFact(srcYear, srcMonth, replace, info.id);
                        }
                    });
                    alertDialog1.show(activityBudget.getSupportFragmentManager(), "fragment_copy_budget");
                    break;
                }
                case R.id.action_show_transactions:
                    if (BuildConfig.DEBUG) {
                        Toast.makeText(getActivity(), String.valueOf(mMonth), Toast.LENGTH_SHORT).show();
                    }
                    Intent intent = new Intent(getActivity(), ActivityTransactions.class);
                    String caption;
                    ArrayList<AbstractFilter> filters;
                    if (info.id >= -1) {
                        filters = FilterManager.createFilterList(IAbstractModel.MODEL_TYPE_CATEGORY, info.id);
                        caption = CategoriesDAO.getInstance(getActivity()).getCategoryByID(info.id).getName();
                    } else {
                        long creditId = (info.id - AdapterBudget.BUDGET_ITEM_DEBTS_ROOT) * -1;
                        if (creditId < 1) return false;
                        Account account = AccountsDAO.getInstance(getActivity()).getAccountByID(CreditsDAO.getInstance(getActivity()).getCreditByID(creditId).getAccountID());
                        caption = account.getName();
                        filters = FilterManager.createFilterList(IAbstractModel.MODEL_TYPE_ACCOUNT, account.getID());
                    }
                    DateRangeFilter dateRangeFilter = new DateRangeFilter(0, getActivity());
                    dateRangeFilter.setRangeByMonth(mYear, mMonth, getActivity());
                    filters.add(dateRangeFilter);
                    intent.putParcelableArrayListExtra("filter_list", filters);
                    intent.putExtra("caption", caption);
                    intent.putExtra(FgConst.HIDE_FAB, true);
                    intent.putExtra(FgConst.LOCK_SLIDINGUP_PANEL, true);
                    startActivity(intent);
                    break;
            }
            return true;
        } else
            return false;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        MenuInflater menuInflater = getActivity().getMenuInflater();
        menuInflater.inflate(R.menu.context_menu_budget_item, menu);
    }

    @Override
    public void onItemClick(final Category category, final int year, final int month, long cabbageId, final int position, BigDecimal amount) {
        long cid;
        if (cabbageId >= 0) {
            cid = cabbageId;
        } else {
            cid = PreferenceManager.getDefaultSharedPreferences(getActivity()).getLong(FgConst.PREF_LAST_BUDGET_CURRENCY, -1);
        }
        Cabbage cabbage = CabbagesDAO.getInstance(getActivity()).getCabbageByID(cid);
        FragmentAmountEdit alertDialog = FragmentAmountEdit.newInstance(category.getName(), amount, cabbage, category.getID());
        alertDialog.setOnComplete(new IOnAmountEditComplete() {
            @Override
            public void onComplete(BigDecimal amount, Cabbage cabbage) {
                onAmountEdit(category, year, month, position, amount, cabbage);
            }
        });
        alertDialog.setOnAmountEditDialogDeleteListener(new OnAmountEditDialogDeleteListener(category, year, month, position, cabbageId));
        alertDialog.show(getActivity().getSupportFragmentManager(), "fragment_amount_edit");


//        builder.setPositiveButton("OK", new OnAmountEditDialogOkListener(category, year, month, position, input));

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    private void updateData() throws Exception {
        ioSums = getIoSumsFromTree(adapter.getmTree().getFlatChildrenList());
        isIoSumsLoaded = true;

        adapterSummary.getmTree().getNodeById(AdapterBudget.BUDGET_ITEM_INCOME).getmCategory().getBudget().setmSums(ioSums);
        adapterSummary.getmTree().getNodeById(AdapterBudget.BUDGET_ITEM_OUTCOME).getmCategory().getBudget().setmSums(ioSums);
        adapterSummary.getmTree().getNodeById(AdapterBudget.BUDGET_ITEM_TOTAL_IO).getmCategory().getBudget().setmSums(ioSums);

        CategoryManager.updatePlanAndFactForParents(adapter.getmTree());

        updateRwHandler.sendMessage(updateRwHandler.obtainMessage(0, 0, 0));
        isUpdating = false;
    }

    @SuppressLint("DefaultLocale")
    private void loadData() throws Exception {
        if (getActivity() == null) return;

//        Debug.startMethodTracing("BudgetLoadData");
//        Log.d(TAG, String.format("Start loading %d %d...", mMonth, mYear));
        long totalTime = System.currentTimeMillis();
        long time = System.currentTimeMillis();
        while (!((ActivityBudget) getActivity()).canAccessDb) {
            SystemClock.sleep(50);
        }
        Log.d(TAG, "Wait for db " + String.valueOf(System.currentTimeMillis() - time));
        ((ActivityBudget) getActivity()).canAccessDb = false;


        time = System.currentTimeMillis();
        adapter.setmTree(
                CategoryManager.convertListToTree(CategoriesDAO.getInstance(getActivity())
                        .getAllCategoriesWithPlanFact(mYear, mMonth))
                        .addDebtsCategories(getActivity(), mYear, mMonth)
                        .removeEmptySums()
        );
        Log.d(TAG, "Load plan and fact for categories " + String.valueOf(System.currentTimeMillis() - time));

        time = System.currentTimeMillis();
        CategoryManager.updatePlanAndFactForParents(adapter.getmTree());
        Log.d(TAG, "Update plan and fact for parents " + String.valueOf(System.currentTimeMillis() - time));

        updateRwHandler.sendMessage(updateRwHandler.obtainMessage(0, 0, 0));

        time = System.currentTimeMillis();
        ioSums = getIoSumsFromTree(adapter.getmTree().getFlatChildrenList());
        isIoSumsLoaded = true;
        Log.d(TAG, "Get IO sums from categories tree " + String.valueOf(System.currentTimeMillis() - time));

        adapterSummary.setmTree(new CNode(new Category(), null));
        int ind = 0;

        CNode newNode;
        Category categoryTotalIO = new Category(AdapterBudget.BUDGET_ITEM_TOTAL_IO,
                getActivity().getString(R.string.ent_budget_total_io_for_month), new Category(), 0, true);
        newNode = adapterSummary.getmTree().insertChild(ind++, categoryTotalIO);
        newNode.getmCategory().setBudget(new BudgetForCategory(ioSums, AdapterBudget.INFO_TYPE_TR_TOTAL));

        newNode = adapterSummary.getmTree().addChild(new Category(AdapterBudget.BUDGET_ITEM_INCOME,
                getActivity().getString(R.string.ent_budget_total_income), categoryTotalIO, 0, true));
        newNode.getmCategory().setBudget(new BudgetForCategory(ioSums, AdapterBudget.INFO_TYPE_TR_INCOME));

        newNode = adapterSummary.getmTree().addChild(new Category(AdapterBudget.BUDGET_ITEM_OUTCOME,
                getActivity().getString(R.string.ent_budget_total_outcome), categoryTotalIO, 0, true));
        newNode.getmCategory().setBudget(new BudgetForCategory(ioSums, AdapterBudget.INFO_TYPE_TR_OUTCOME));


        time = System.currentTimeMillis();
        if (CreditsDAO.getInstance(getActivity()).getAllDebts().size() > 0) {
            Category categoryTotalDebts = new Category(AdapterBudget.BUDGET_ITEM_TOTAL_DEBTS,
                    getActivity().getString(R.string.ent_budget_total_credits), new Category(), 0, true);
            ListSumsByCabbage creditSums = getDebtSums();

            newNode = adapterSummary.getmTree().insertChild(ind++, categoryTotalDebts);
            newNode.getmCategory().setBudget(new BudgetForCategory(creditSums, AdapterBudget.INFO_TYPE_ALL_TOTAL));

            newNode = adapterSummary.getmTree().addChild(new Category(AdapterBudget.BUDGET_ITEM_BORROW,
                    getActivity().getString(R.string.ent_budget_total_borrow), categoryTotalDebts, 0, true));
            newNode.getmCategory().setBudget(new BudgetForCategory(creditSums, AdapterBudget.INFO_TYPE_OUTCOME_TOTAL));

            newNode = adapterSummary.getmTree().addChild(new Category(AdapterBudget.BUDGET_ITEM_REPAY,
                    getActivity().getString(R.string.ent_budget_total_repay), categoryTotalDebts, 0, true));
            newNode.getmCategory().setBudget(new BudgetForCategory(creditSums, AdapterBudget.INFO_TYPE_INCOME_TOTAL));
        }
        Log.d(TAG, "Get debts from DB " + String.valueOf(System.currentTimeMillis() - time));

        Log.d(TAG, String.format("Total time %d %d - %d ms", mYear, mMonth, System.currentTimeMillis() - totalTime));
        updateRwHandler.sendMessage(updateRwHandler.obtainMessage(0, 0, 0));


        isUpdating = false;
        ((ActivityBudget) getActivity()).canAccessDb = true;
//        Debug.stopMethodTracing();
    }

    private void fullUpdate() {
        if (!isUpdating) {
            isUpdating = true;
            isIoSumsLoaded = false;
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        FragmentBudget.this.loadData();
                    } catch (Exception e) {
                        //get items error
                    }
                }
            });
            t.start();
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    FragmentBudget.this.updateTotalSums();
                }
            });
        }
    }

    private void nodeUpdate() {
        if (!isUpdating) {
            isUpdating = true;
            isIoSumsLoaded = false;
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    try {
                        FragmentBudget.this.updateData();
                    } catch (Exception e) {
                        //get items error
                    }
                }
            });

            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    FragmentBudget.this.updateTotalSums();
                }
            });
        }
    }

    private void updateTotalSums() {
        while (!isIoSumsLoaded) {
            SystemClock.sleep(100);
//            Log.d(TAG, "Wait 100 ms");
        }
        ListSumsByCabbage listSumsDif = new ListSumsByCabbage();
        for (SumsByCabbage sumsIo : ioSums.getmList()) {
            listSumsDif.getmList().add(new SumsByCabbage(sumsIo.getCabbageId(),
                    sumsIo.getInTrSum().subtract(sumsIo.getInPlan()),
                    sumsIo.getOutTrSum().subtract(sumsIo.getOutPlan())));
        }
//        String captions[] = new String[]{getResources().getString(R.string.title_total),
//                getResources().getString(R.string.budget_item_income), getResources().getString(R.string.budget_item_outcome)};
        SumsManager.updateSummaryTable(getActivity(), layoutSumTable, false, listSumsDif, CabbagesDAO.getInstance(getActivity()).getCabbagesMap(), null);
    }

    @SuppressWarnings("unchecked")
    private ListSumsByCabbage getIoSumsFromTree(List<CNode> nodes) throws Exception {
        List<Cabbage> cabbages = (List<Cabbage>) CabbagesDAO.getInstance(getActivity()).getAllModels();
        ListSumsByCabbage result = new ListSumsByCabbage();

        for (Cabbage cabbage : cabbages) {
            SumsByCabbage totalSums = new SumsByCabbage(cabbage.getID(), BigDecimal.ZERO, BigDecimal.ZERO);
            for (CNode node : nodes) {
                long id = node.getmCategory().getID();
                if ((id >= 0 | id < AdapterBudget.BUDGET_ITEM_DEBTS_ROOT) & node.getFlatChildrenList().size() == 0) {
                    SumsByCabbage sumsByCabbage = node.getmCategory().getBudget().getmSums().getSumsByCabbageId(cabbage.getID());
                    if (sumsByCabbage != null) {
                        totalSums.setInPlan(totalSums.getInPlan().add(sumsByCabbage.getInPlan()));
                        totalSums.setOutPlan(totalSums.getOutPlan().add(sumsByCabbage.getOutPlan()));
                        totalSums.setInTrSum(totalSums.getInTrSum().add(sumsByCabbage.getInTrSum()));
                        totalSums.setOutTrSum(totalSums.getOutTrSum().add(sumsByCabbage.getOutTrSum()));
                    }
                }
            }
            result.getmList().add(totalSums);
        }


        return result;
    }

    private ListSumsByCabbage getDebtSums() throws Exception {
        List<AbstractFilter> filters = new ArrayList<>();

        DateRangeFilter dateRangeFilter = new DateRangeFilter(0, getActivity());
        dateRangeFilter.setRangeByMonth(mYear, mMonth, getActivity());
        filters.add(dateRangeFilter);

        AccountFilter accountFilter = new AccountFilter(1);
        for (Credit credit : CreditsDAO.getInstance(getActivity()).getAllDebts()) {
            accountFilter.addAccount(credit.getAccountID());
        }
        filters.add(accountFilter);

        TransactionsDAO transactionsDAO = TransactionsDAO.getInstance(getActivity());

        return transactionsDAO.getGroupedSums(new FilterListHelper(filters, "", getActivity()), false, null, getActivity());
    }

    private void createBudgetFromFact(int srcYear, int srcMonth, boolean replace, long id) {
        List<Category> categories = CategoriesDAO.getInstance(getActivity()).getAllCategoriesWithPlanFact(srcYear, srcMonth);
        BudgetDAO budgetDAO = BudgetDAO.getInstance(getActivity());
        for (Category category : categories) {
            if ((id == -1) ^ (id == category.getID())) {
                if (!category.getBudget().getmSums().isEmpty()) {
                    for (SumsByCabbage sumsByCabbage : category.getBudget().getmSums().getmList()) {
                        if (!sumsByCabbage.isEmpty(false)) {
                            if (replace) {
                                try {
                                    budgetDAO.createBudget(mYear, mMonth, category.getID(),
                                            sumsByCabbage.getInTrSum().add(sumsByCabbage.getOutTrSum()),
                                            sumsByCabbage.getCabbageId());
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            } else {
                                if (!budgetDAO.budgetExists(mYear, mMonth, category.getID())) {
                                    try {
                                        budgetDAO.createBudget(mYear, mMonth, category.getID(),
                                                sumsByCabbage.getInTrSum().add(sumsByCabbage.getOutTrSum()),
                                                sumsByCabbage.getCabbageId());
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        categories = CreditsDAO.getInstance(getActivity()).getDebtsAsCategoriesWithPlanFact(srcYear, srcMonth, getActivity());
        BudgetCreditsDAO budgetCreditsDAO = BudgetCreditsDAO.getInstance(getActivity());
        long creditId;
        for (Category category : categories) {
            if ((id == -1) ^ (id == category.getID())) {
                creditId = (category.getID() - AdapterBudget.BUDGET_ITEM_DEBTS_ROOT) * -1;
                if (!category.getBudget().getmSums().isEmpty()) {
                    for (SumsByCabbage sumsByCabbage : category.getBudget().getmSums().getmList()) {
                        if (!sumsByCabbage.isEmpty(false)) {
                            if (replace) {
                                try {
                                    budgetCreditsDAO.createBudget(mYear, mMonth, creditId,
                                            sumsByCabbage.getInTrSum().add(sumsByCabbage.getOutTrSum()));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            } else {
                                if (!budgetCreditsDAO.budgetExists(mYear, mMonth, creditId)) {
                                    try {
                                        budgetCreditsDAO.createBudget(mYear, mMonth, creditId,
                                                sumsByCabbage.getInTrSum().add(sumsByCabbage.getOutTrSum()));
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        fullUpdate();
    }

    private static class UpdateRwHandler extends Handler {

        WeakReference<FragmentBudget> mFrag;

        UpdateRwHandler(FragmentBudget aFragment) {
            mFrag = new WeakReference<>(aFragment);
        }

        @Override
        public void handleMessage(Message msg) {
            FragmentBudget fragmentBudget = mFrag.get();
            fragmentBudget.adapter.notifyDataSetChanged();
            fragmentBudget.adapterSummary.notifyDataSetChanged();
        }
    }

    private class FabMenuItemClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            ActivityBudget activityBudget = (ActivityBudget) getActivity();
            switch (v.getId()) {
                case R.id.fabAddModel:
                    Intent intentCategory = new Intent(getActivity(), ActivityList.class);
                    intentCategory.putExtra("showHomeButton", false);
                    intentCategory.putExtra("model", new Category());
                    intentCategory.putExtra("requestCode", RequestCodes.REQUEST_CODE_SELECT_MODEL);
                    startActivityForResult(intentCategory, RequestCodes.REQUEST_CODE_SELECT_MODEL);
                    break;
                case R.id.fabAddDebt:
                    Intent intent = new Intent(getActivity(), ActivityModelList.class);
                    intent.putExtra("showHomeButton", false);
                    intent.putExtra("model", new Credit());
                    intent.putExtra("requestCode", RequestCodes.REQUEST_CODE_SELECT_MODEL);
                    startActivityForResult(intent, RequestCodes.REQUEST_CODE_SELECT_MODEL);
                    break;
                case R.id.fabCopyBudget:
                    String title = getActivity().getResources().getString(R.string.act_copy_budget);
                    int position = activityBudget.viewPager.getCurrentItem() - 1;
                    FragmentCopyBudget alertDialog = FragmentCopyBudget.newInstance(title,
                            activityBudget.convertPosToYear(position),
                            activityBudget.convertPosToMonth(position),
                            true);
                    alertDialog.setCopyBudgetDialogListener(new FragmentCopyBudget.ICopyBudgetDialogListener() {
                        @Override
                        public void onOkClick(int srcYear, int srcMonth, boolean replace) {
                            BudgetDAO.getInstance(getActivity())
                                    .copyBudget(srcYear, srcMonth, mYear, mMonth, replace, -1);
                            BudgetCreditsDAO.getInstance(getActivity())
                                    .copyBudget(srcYear, srcMonth, mYear, mMonth, replace, -1);
                            fullUpdate();
                        }
                    });
                    alertDialog.show(activityBudget.getSupportFragmentManager(), "fragment_copy_budget");
                    break;
                case R.id.fabCreateFromFact:
                    String titleCreateFrmFact = getActivity().getResources().getString(R.string.act_create_from_fact);
                    int position1 = activityBudget.viewPager.getCurrentItem() - 1;
                    FragmentCopyBudget alertDialog1 = FragmentCopyBudget.newInstance(titleCreateFrmFact,
                            activityBudget.convertPosToYear(position1),
                            activityBudget.convertPosToMonth(position1),
                            true);
                    alertDialog1.setCopyBudgetDialogListener(new FragmentCopyBudget.ICopyBudgetDialogListener() {
                        @Override
                        public void onOkClick(int srcYear, int srcMonth, boolean replace) {
                            createBudgetFromFact(srcYear, srcMonth, replace, -1);
                        }
                    });
                    alertDialog1.show(activityBudget.getSupportFragmentManager(), "fragment_copy_budget");
                    break;
                case R.id.fabClearBudget:
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle(R.string.ttl_confirm_action);
                    builder.setMessage(R.string.msg_dialog_clear_budget);

                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog1, int which1) {
                            BudgetDAO.getInstance(getActivity()).clearBudget(mYear, mMonth);
                            BudgetCreditsDAO.getInstance(getActivity()).clearBudget(mYear, mMonth);
                            fullUpdate();
                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });

                    builder.show();
                    break;
            }
            fabMenu.close(false);
        }
    }

    public class OnAmountEditDialogDeleteListener implements DialogInterface.OnClickListener {
        final Category category;
        final int year;
        final int month;
        final int position;
        final long cabbageID;

        OnAmountEditDialogDeleteListener(Category category, int year, int month, int position, long cabbageID) {
            this.category = category;
            this.year = year;
            this.month = month;
            this.position = position;
            this.cabbageID = cabbageID;
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            CNode node = adapter.getmTree().getChildrenAtFlatPos(position);
            if (node != null) {
                node.deleteBudget(getActivity(), year, month, cabbageID);
                adapter.getmTree().removeEmptySums();
                nodeUpdate();
            }
        }
    }

    private void onAmountEdit(Category category, int year, int month, int position, BigDecimal amount, Cabbage cabbage) {
        long cabbageId = cabbage.getID();
        PreferenceManager.getDefaultSharedPreferences(getActivity()).edit().putLong(FgConst.PREF_LAST_BUDGET_CURRENCY, cabbageId).apply();
        if (category.getID() >= 0) {
            try {
                BudgetDAO.getInstance(getActivity()).createBudget(year, month, category.getID(), amount, cabbageId);
            } catch (Exception e) {
                Toast.makeText(getActivity(), R.string.msg_error_on_write_to_db, Toast.LENGTH_SHORT).show();
                return;
            }
        } else {
            long creditId = (category.getID() - AdapterBudget.BUDGET_ITEM_DEBTS_ROOT) * -1;
            if (creditId > 0) {
                try {
                    BudgetCreditsDAO.getInstance(getActivity()).createBudget(year, month, creditId, amount);
                } catch (Exception e) {
                    Toast.makeText(getActivity(), R.string.msg_error_on_write_to_db, Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        }
        if (position < 0) {
            fullUpdate();
        } else {
            CNode node = adapter.getmTree().getChildrenAtFlatPos(position);
            if (node != null) {
                SumsByCabbage sumsByCabbage = node.getmCategory().getBudget().getmSums().getSumsByCabbageId(cabbageId);
                boolean fullUpdt = false;
                if (sumsByCabbage == null) {
                    sumsByCabbage = new SumsByCabbage(cabbageId, BigDecimal.ZERO, BigDecimal.ZERO);
                    fullUpdt = true;
                }
                if (amount.compareTo(BigDecimal.ZERO) > 0) {
                    sumsByCabbage.setInPlan(amount);
                    sumsByCabbage.setOutPlan(BigDecimal.ZERO);
                } else {
                    sumsByCabbage.setInPlan(BigDecimal.ZERO);
                    sumsByCabbage.setOutPlan(amount);
                }
                if (fullUpdt) {
                    fullUpdate();
                } else {
                    nodeUpdate(/*adapter.getmTree().getChildrenAtFlatPos(position)*/);
                }
            } else {
                fullUpdate();
            }
        }
    }

    public interface IOnAmountEditComplete {
        void onComplete(BigDecimal amount, Cabbage cabbage);
    }

}
