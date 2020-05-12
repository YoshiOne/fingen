package com.yoshione.fingen;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Animatable;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.yoshione.fingen.adapter.AdapterSimpleDebts;
import com.yoshione.fingen.classes.ListSumsByCabbage;
import com.yoshione.fingen.classes.SumsByCabbage;
import com.yoshione.fingen.dao.AbstractDAO;
import com.yoshione.fingen.dao.AccountsDAO;
import com.yoshione.fingen.dao.BaseDAO;
import com.yoshione.fingen.dao.CabbagesDAO;
import com.yoshione.fingen.dao.SimpleDebtsDAO;
import com.yoshione.fingen.dao.TransactionsDAO;
import com.yoshione.fingen.iab.BillingService;
import com.yoshione.fingen.interfaces.IAbstractModel;
import com.yoshione.fingen.interfaces.IBaseModelEventListener;
import com.yoshione.fingen.managers.SumsManager;
import com.yoshione.fingen.model.BaseModel;
import com.yoshione.fingen.model.Cabbage;
import com.yoshione.fingen.model.SimpleDebt;
import com.yoshione.fingen.utils.FabMenuController;
import com.yoshione.fingen.utils.RequestCodes;
import com.yoshione.fingen.utils.ScreenUtils;
import com.yoshione.fingen.widgets.ContextMenuRecyclerView;
import com.yoshione.fingen.widgets.FgLinearLayoutManager;
import com.yoshione.fingen.widgets.ToolbarActivity;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

import static android.app.Activity.RESULT_OK;

public class FragmentSimpleDebts extends BaseListFragment
        implements
        IBaseModelEventListener
{

    private static final String TAG = "FragmentSimpleDebts";
    private static final int CONTEXT_MENU_DEBTS = 0;

    //<editor-fold desc="Bind views" defaultstate="collapsed">
    @BindView(R.id.layoutSummaryTable)
    TableLayout layoutSumTable;
    @BindView(R.id.switch_all_filters)
    SwitchCompat switchAllFilters;
    @BindView(R.id.fabSelectAll)
    FloatingActionButton mFabSelectAll;
    @BindView(R.id.fabUnselectAll)
    FloatingActionButton mFabUnselectAll;
    @BindView(R.id.sliding_layout_debts)
    SlidingUpPanelLayout mSlidingLayoutDebts;
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
    @BindView(R.id.fabSelectAllLayout)
    LinearLayout mFabSelectAllLayout;
    @BindView(R.id.fabUnselectAllLayout)
    LinearLayout mFabUnselectAllLayout;
    @BindView(R.id.fabMenuButtonRoot)
    FloatingActionButton mFabMenuButtonRoot;
    @BindView(R.id.fabMenuButtonRootLayout)
    LinearLayout mFabMenuButtonRootLayout;
    Unbinder unbinder;
    @BindView(R.id.fabBGLayout)
    View mFabBGLayout;
    //</editor-fold>

    private AdapterSimpleDebts adapterD;
    private int contextMenuTarget = -1;
    private boolean isInSelectionMode;
    FabMenuController mFabMenuController;
    private int mLastFirstVisiblePosition = 0;

    @Inject
    BillingService mBillingService;
    @Inject
    SharedPreferences mPreferences;
    @Inject
    Context mContext;
    @Inject
    SimpleDebtsDAO mSimpleDebtsDAO;
    @Inject
    TransactionsDAO mTransactionsDAO;
    @Inject
    AccountsDAO mAccountsDAO;
    @Inject
    CabbagesDAO mCabbagesDAO;

    public static FragmentSimpleDebts newInstance(String forceUpdateParam, int layoutID) {
        FragmentSimpleDebts fragment = new FragmentSimpleDebts();
        Bundle args = new Bundle();
        args.putString(FORCE_UPDATE_PARAM, forceUpdateParam);
        args.putInt(LAYOUT_NAME_PARAM, layoutID);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        FGApplication.getAppComponent().inject(this);

        if (getActivity() != null) {
            Intent intent = getActivity().getIntent();
            if (intent.getBooleanExtra(FgConst.LOCK_SLIDINGUP_PANEL, false) & !BuildConfig.DEBUG) {
                mSlidingLayoutDebts.setEnabled(false);
            }
        }

        adapterD = new AdapterSimpleDebts(this, (ToolbarActivity) getActivity());
        adapterD.setHasStableIds(true);

        final LinearLayoutManager linearLayoutManager = new FgLinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(adapterD);

        mSlidingLayoutDebts.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {
                float alpha = Math.max(0, Math.abs(1 - slideOffset * 25));
                mImageViewPullMe.setAlpha(alpha);
            }

            @Override
            public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {
                if (previousState != newState && newState == SlidingUpPanelLayout.PanelState.COLLAPSED) {
                    onSelectionChange(adapterD.getSelectedCount());
                }
            }
        });

        mEditTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapterD.setSearchString(s.toString());
                fullUpdate(-1);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mImageButtonClearSearchString.setOnClickListener(view1 -> {
            if (!mEditTextSearch.getText().toString().isEmpty()) {
                mEditTextSearch.setText("");
                fullUpdate(-1);
            } else {
                hideSearchView();
            }
        });

        layoutSumTable.getViewTreeObserver().addOnGlobalLayoutListener(
                new SumsTableOnGlobalLayoutListener(getActivity(), layoutSumTable, mSlidingLayoutDebts));

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

        if (view != null) {
            unbinder = ButterKnife.bind(this, view);
        }
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        initFabMenu();
        mTextViewSelectedCount.setVisibility(isInSelectionMode ? View.VISIBLE : View.GONE);

        recyclerView.getLayoutManager().scrollToPosition(mLastFirstVisiblePosition);
    }

    private void initFabMenu() {
        mFabMenuController = new FabMenuController(mFabMenuButtonRoot, mFabBGLayout, getActivity(),
                mFabUnselectAllLayout, mFabSelectAllLayout);
        mFabMenuButtonRootLayout.setVisibility(View.GONE);
        isInSelectionMode = false;

        FabMenuSelectionItemClickListener fabMenuSelectionItemClickListener = new FabMenuSelectionItemClickListener();
        mFabSelectAll.setOnClickListener(fabMenuSelectionItemClickListener);
        mFabUnselectAll.setOnClickListener(fabMenuSelectionItemClickListener);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RequestCodes.REQUEST_CODE_EDIT_TRANSACTION & resultCode == RESULT_OK & data != null) {
            Toast.makeText(mContext, R.string.ttl_transaction_splitted, Toast.LENGTH_SHORT).show();
        }
        if (data != null && resultCode == RESULT_OK && requestCode == RequestCodes.REQUEST_CODE_SELECT_MODEL) {
            IAbstractModel model = data.getParcelableExtra("model");
            if (adapterD.getSelectedCount() >= 0 & data.getStringArrayListExtra(FgConst.SELECTED_TRANSACTIONS_IDS) != null) {
                switch (model.getModelType()) {
                    case IAbstractModel.MODEL_TYPE_ACCOUNT:
                    case IAbstractModel.MODEL_TYPE_PAYEE:
                    case IAbstractModel.MODEL_TYPE_CATEGORY:
                    case IAbstractModel.MODEL_TYPE_PROJECT:
                    case IAbstractModel.MODEL_TYPE_LOCATION:
                    case IAbstractModel.MODEL_TYPE_DEPARTMENT:
                    case IAbstractModel.MODEL_TYPE_SIMPLEDEBT:
                        try {
                            mTransactionsDAO.bulkUpdateEntity(data.getStringArrayListExtra(FgConst.SELECTED_TRANSACTIONS_IDS), model, true);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                }
            }
        }
    }

    @Override
    public void onItemClick(BaseModel item) {
        Toast.makeText(getActivity(), "Not implemented yet", Toast.LENGTH_LONG).show();
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onSelectionChange(int selectedCount) {
        Log.e(TAG, "onSelectionChange " + selectedCount);
        if (selectedCount > 0) {
            new Handler().postDelayed(() -> mFabMenuButtonRootLayout.setVisibility(View.VISIBLE), 200);
            mTextViewSelectedCount.setVisibility(View.VISIBLE);
            mTextViewSelectedCount.setText(String.format("%d %s", selectedCount, getString(R.string.ttl_selected)));
        } else {
            mFabMenuButtonRootLayout.setVisibility(View.GONE);
            mTextViewSelectedCount.setVisibility(View.GONE);
            mTextViewSelectedCount.setText("");
        }
        isInSelectionMode = selectedCount > 0;

        ListSumsByCabbage listSumsByCabbage = new ListSumsByCabbage();
        for (SimpleDebt dept : adapterD.getSelectedDebts()) {
            boolean startAmountIsPositive = dept.getStartAmount().abs().equals(dept.getStartAmount());
            SumsByCabbage sumsByCabbage = new SumsByCabbage(dept.getCabbageID(),
                    dept.getOweMe().add(startAmountIsPositive ? dept.getStartAmount() : BigDecimal.ZERO),
                    BigDecimal.ZERO.subtract(dept.getAmount()).add(!startAmountIsPositive ? dept.getStartAmount() : BigDecimal.ZERO));
            listSumsByCabbage.appendSumFact(sumsByCabbage);
        }
        updateSums(listSumsByCabbage);
    }

    private void initSlidePanelButtons() {
        mButtonSearch.setOnClickListener(v -> showSearchView());

        mButtonAddFilter.setOnClickListener(v -> {
            if (getActivity() == null) return;
            Toast.makeText(getActivity(), "Not implemented yet", Toast.LENGTH_LONG).show();
        });
        mButtonReports.setOnClickListener(v -> {
            if (getActivity() == null) return;
            Toast.makeText(getActivity(), "Not implemented yet", Toast.LENGTH_LONG).show();
        });
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        initSlidePanelButtons();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onPause() {
        super.onPause();

        mLastFirstVisiblePosition = ((LinearLayoutManager)recyclerView.getLayoutManager()).findFirstCompletelyVisibleItemPosition();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        Activity activity = getActivity();
        if (activity != null) {
            MenuInflater menuInflater = activity.getMenuInflater();
            if (v.getId() == R.id.recycler_view) {
                contextMenuTarget = CONTEXT_MENU_DEBTS;
                menuInflater.inflate(R.menu.context_menu_simple_debts, menu);
            }
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (getUserVisibleHint()) {
            switch (contextMenuTarget) {
                case CONTEXT_MENU_DEBTS:
                    initContextMenuDebts(item);
                    break;
            }
            return true;
        } else
            return false;
    }

    @SuppressWarnings("WrongConstant")
    private void initContextMenuDebts(final MenuItem item) {
        final ContextMenuRecyclerView.RecyclerContextMenuInfo info = (ContextMenuRecyclerView.RecyclerContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case R.id.action_borrow:
            case R.id.action_repay:
            case R.id.action_grant_a_loan_to:
            case R.id.action_take_in_payment: {
                Toast.makeText(getActivity(), "Not implemented yet", Toast.LENGTH_SHORT).show();
                break;
            }
        }
    }

    private void showSearchView() {
        Toast.makeText(getActivity(), "Not implemented yet", Toast.LENGTH_LONG).show();
//        mCardViewSearch.setVisibility(View.VISIBLE);
//        mSlidingLayoutDebts.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
//        mEditTextSearch.requestFocus();
//        if (getActivity() != null) {
//            final InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
//            mEditTextSearch.postDelayed(
//                    () -> {
//                        if (imm != null) {
//                            imm.showSoftInput(mEditTextSearch, 0);
//                        }
//                    }, 300);
//        }
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
    public void loadData(long itemID) {
        if (recyclerView == null) return;

        Context context = FGApplication.getContext();

        adapterD.clearDebtList();

        mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SimpleDebtsDAO simpleDebtsDAO = SimpleDebtsDAO.getInstance(getActivity());
        boolean showClosed = mPreferences.getBoolean(FgConst.PREF_SHOW_CLOSED_DEBTS, true);
        try {
            List<SimpleDebt> models = simpleDebtsDAO.getAllSimpleDebts();
            long cabbageID = getActivity().getIntent().getLongExtra("cabbageID", -1);
            SimpleDebt debt;
            for (int i = models.size() - 1; i >= 0; i--) {
                debt = models.get(i);
                if ((cabbageID >= 0 && debt.getCabbageID() != cabbageID) || (!debt.isActive() && !showClosed)) {
                    models.remove(i);
                }
            }
            adapterD.addDebts(models, true);
        } catch (Exception ignored) { }

        adapterD.notifyDataSetChanged();

        loadSums();
    }

    public void loadSums() {
        ToolbarActivity activity = (ToolbarActivity) getActivity();
        HashMap<Long, Cabbage> cabbages = mCabbagesDAO.getCabbagesMap();
        Objects.requireNonNull(activity).unsubscribeOnDestroy(
            mSimpleDebtsDAO.getGroupedSumsRx(true, adapterD.getSelectedDebtsIDsAsLong(), getActivity())
                    .map(listSumsByCabbage -> SumsManager.formatSums(listSumsByCabbage, cabbages, false))
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
            .subscribe(listSumsByCabbage -> SumsManager.updateSummaryTableWithFormattedStrings(getActivity(),
                    layoutSumTable, false, listSumsByCabbage, null))
        );
    }

    public void updateLists(long itemID) {
        adapterD.notifyDataSetChanged();
        if (recyclerView != null && itemID >= 0) {
            FgLinearLayoutManager linearLayoutManager = (FgLinearLayoutManager) recyclerView.getLayoutManager();
            linearLayoutManager.scrollToPosition((int) itemID);
        }
    }

    public void updateSums(ListSumsByCabbage listSumsByCabbage) {
        SumsManager.updateSummaryTable(getActivity(), layoutSumTable, false, listSumsByCabbage, mCabbagesDAO.getCabbagesMap(), null);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    private class FabMenuSelectionItemClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.fabSelectAll: {
                    adapterD.selectAll();
                    break;
                }
                case R.id.fabUnselectAll: {
                    adapterD.unselectAll();
                    break;
                }
            }
            mFabMenuController.closeFABMenu();
        }
    }

    private boolean mPullMeBended = false;

    private void animatePullMe() {
        if (!mPullMeBended && mImageViewPullMe != null) {
            mPullMeBended = true;
            mImageViewPullMe.setImageDrawable(Objects.requireNonNull(getContext()).getDrawable(R.drawable.pull_me_animated));
            ((Animatable) mImageViewPullMe.getDrawable()).start();
        }
    }

    private void animatePullMeReverse() {
        if (mPullMeBended && mImageViewPullMe != null) {
            mPullMeBended = false;
            mImageViewPullMe.setImageDrawable(Objects.requireNonNull(getContext()).getDrawable(R.drawable.pull_me_animated_reverse));
            ((Animatable) mImageViewPullMe.getDrawable()).start();
        }
    }

}
