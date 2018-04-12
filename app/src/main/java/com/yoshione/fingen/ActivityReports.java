package com.yoshione.fingen;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.TabLayout;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.yoshione.fingen.dao.CabbagesDAO;
import com.yoshione.fingen.filters.AbstractFilter;
import com.yoshione.fingen.filters.AccountFilter;
import com.yoshione.fingen.managers.AccountsSetManager;
import com.yoshione.fingen.model.AccountsSet;
import com.yoshione.fingen.model.Cabbage;
import com.yoshione.fingen.utils.ColorUtils;
import com.yoshione.fingen.utils.DateTimeFormatter;
import com.yoshione.fingen.widgets.ToolbarActivity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ActivityReports extends ToolbarActivity {

    private static final int FRAGMENT_PIE_CHART = 0;
    private static final int FRAGMENT_BAR_CHART = 1;
    private static final int FRAGMENT_TIME_CHART = 2;
    private static final int FRAGMENTS_COUNT = 3;

    @BindView(R.id.textInputLayoutData)
    TextInputLayout mTextInputLayoutData;
    @BindView(R.id.textInputLayoutDateRange)
    TextInputLayout mTextInputLayoutDateRange;
    @BindView(R.id.pager)
    ViewPager viewPager;
    @BindView(R.id.tabLayout)
    TabLayout tabLayout;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.textViewDateRange)
    TextView mTextViewDateRange;
    @BindView(R.id.editTextCabbage)
    EditText mEditTextCabbage;
    @BindView(R.id.editTextData)
    EditText mEditTextData;
    @BindView(R.id.editTextShow)
    EditText mEditTextShow;
    @BindView(R.id.editTextDateRange)
    EditText mEditTextDateRange;
    ReportBuilder mReportBuilder;
    private int mCurrentFragment;
    private MenuItem mMenuItemTogglePercents;
    private MenuItem mMenuItemToggleLines;
    private Menu mMenu;

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_reports;
    }

    @Override
    protected String getLayoutTitle() {
        return getString(R.string.ent_reports);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.bind(this);

        mCurrentFragment = 0;

        mReportBuilder = ReportBuilder.newInstance(this);

        List<AbstractFilter> filters = getIntent().getParcelableArrayListExtra("filter_list");
        AccountsSet currentAccountsSet = AccountsSetManager.getInstance().getCurrentAccountSet(this);
        List<Long> accountsIDs = currentAccountsSet.getAccountsIDsList();
        if (accountsIDs.size() > 0) {
            AccountFilter accountFilter = new AccountFilter(0);
            accountFilter.addList(accountsIDs);
            accountFilter.setSystem(true);
            filters.add(0, accountFilter);
        }
        mReportBuilder.setFilters(filters);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("current_fragment", viewPager.getCurrentItem());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mCurrentFragment = savedInstanceState.getInt("current_fragment", FRAGMENT_PIE_CHART);
        if (mCurrentFragment == FRAGMENT_TIME_CHART) {
            mTextInputLayoutDateRange.setVisibility(View.VISIBLE);
            mTextInputLayoutData.setVisibility(View.GONE);
        } else {
            mTextInputLayoutDateRange.setVisibility(View.GONE);
            mTextInputLayoutData.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        setupFragments();
        updateUI();
        updateSpinnersListeners();
        loadData();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_reports, menu);
        super.onCreateOptionsMenu(menu);
        mMenu = menu;
        mMenuItemTogglePercents = menu.findItem(R.id.action_toggle_percents);
        mMenuItemToggleLines = menu.findItem(R.id.action_toggle_lines);

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        setPieMenuItemsVisibility(viewPager.getCurrentItem() == FRAGMENT_PIE_CHART);
        return super.onPrepareOptionsMenu(menu);
    }

    private void setPieMenuItemsVisibility(boolean visible) {
        mMenuItemTogglePercents.setVisible(visible);
        mMenuItemToggleLines.setVisible(visible);
        ActivityReports.this.invalidateOptionsMenu();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        SharedPreferences preferences = android.preference.PreferenceManager.getDefaultSharedPreferences(this);
        switch (item.getItemId()) {
            case R.id.action_toggle_shrink_values:
                boolean showShrinkLabels = preferences.getBoolean(FgConst.PREF_SHRINK_CHART_LABELS, true);
                preferences.edit().putBoolean(FgConst.PREF_SHRINK_CHART_LABELS, !showShrinkLabels).apply();
                updateCharts();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void loadData() {
        mReportBuilder.loadEntitiesDataset(this);
        mReportBuilder.loadDateDataset(this);
    }

    private void updateUI() {
        updateCabbageListState();
        updateShowListState();


        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.getInstance(this);
        String s = "";
        for (Pair<Date, Date> datePair : mReportBuilder.getDates()) {
            s = s + String.format("%s - %s; ", dateTimeFormatter.getDateMediumString(datePair.first), dateTimeFormatter.getDateMediumString(datePair.second));
        }
        mTextViewDateRange.setText(s);

        mEditTextData.setText(mReportBuilder.getActiveDataCaption());
        mEditTextDateRange.setText(mReportBuilder.getActiveDateRangeCaption());
    }

    private Fragment getFragment(Class fragmentClass) {
        FragmentManager fm = getSupportFragmentManager();

        for (Fragment f : fm.getFragments()) { // to loop through fragments and checking their type
            if (f.getClass().equals(fragmentClass)) {
                return f;
            }
        }
        return null;
    }

    private void setupFragments() {
        FragmentPieChart fragmentPieChart = new FragmentPieChart();
        FragmentBarChart fragmentBarChart = new FragmentBarChart();
        FragmentTimeBarChart fragmentTimeBarChart = new FragmentTimeBarChart();

        final List<Fragment> fragments = new ArrayList<>();
        fragments.add(FRAGMENT_PIE_CHART, fragmentPieChart);
        fragments.add(FRAGMENT_BAR_CHART, fragmentBarChart);
        fragments.add(FRAGMENT_TIME_CHART, fragmentTimeBarChart);

        FragmentPagerAdapter mFragmentPagerAdapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public int getCount() {
                return FRAGMENTS_COUNT;
            }

            @Override
            public Fragment getItem(final int position) {
                return fragments.get(position);
            }

            @Override
            public CharSequence getPageTitle(final int position) {
                return "";
            }
        };
        viewPager.setAdapter(mFragmentPagerAdapter);
        viewPager.setOffscreenPageLimit(10);
        tabLayout.setupWithViewPager(viewPager);
        viewPager.setCurrentItem(mCurrentFragment);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (position == FRAGMENT_TIME_CHART) {
                    mTextInputLayoutDateRange.setVisibility(View.VISIBLE);
                    mTextInputLayoutData.setVisibility(View.GONE);
                } else {
                    mTextInputLayoutDateRange.setVisibility(View.GONE);
                    mTextInputLayoutData.setVisibility(View.VISIBLE);
                }
                setTabLayoutIcons();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        setTabLayoutIcons();
    }

    private void setTabLayoutIcons() {
        TabLayout.Tab tab = tabLayout.getTabAt(FRAGMENT_PIE_CHART);
        if (tab != null) {
            tab.setIcon(getDrawable(R.drawable.ic_chart_white));
        }
        tab = tabLayout.getTabAt(FRAGMENT_BAR_CHART);
        if (tab != null) {
            tab.setIcon(getDrawable(R.drawable.ic_bar_chart_white));
        }
        tab = tabLayout.getTabAt(FRAGMENT_TIME_CHART);
        if (tab != null) {
            tab.setIcon(getDrawable(R.drawable.ic_clock_white));
        }
    }

    @SuppressWarnings("unchecked")
    private void updateCabbageListState() {
        Cabbage cabbage;
        try {
            cabbage = mReportBuilder.getActiveCabbage();
        } catch (Exception e) {
            cabbage = new Cabbage();
        }
        mEditTextCabbage.setText(cabbage.getCode());
    }

    private void updateShowListState() {
        mEditTextShow.setText(mReportBuilder.getActiveShowParam());
    }

    private void updateCharts() {
        FragmentPieChart fragmentPieChart = (FragmentPieChart) getFragment(FragmentPieChart.class);
        if (fragmentPieChart != null) {
            fragmentPieChart.updateChart(true);
        }
        FragmentBarChart fragmentBarChart = (FragmentBarChart) getFragment(FragmentBarChart.class);
        if (fragmentBarChart != null) {
            fragmentBarChart.updateChart();
        }
        FragmentTimeBarChart fragmentTimeBarChart = (FragmentTimeBarChart) getFragment(FragmentTimeBarChart.class);
        if (fragmentTimeBarChart != null) {
            fragmentTimeBarChart.updateChart();
        }
    }

    @Override
    public void onBackPressed() {
        if (mReportBuilder.getParentID() >= 0 & viewPager.getCurrentItem() != FRAGMENT_TIME_CHART) {
            mReportBuilder.levelUp(this);
            updateCharts();
        } else {
            super.onBackPressed();
        }
    }

    private void updateSpinnersListeners() {
        SpinnersOnClickListeners onClickListeners = new SpinnersOnClickListeners();
        mEditTextCabbage.setOnClickListener(onClickListeners);
        mEditTextData.setOnClickListener(onClickListeners);
        mEditTextDateRange.setOnClickListener(onClickListeners);
        mEditTextShow.setOnClickListener(onClickListeners);
    }

    @SuppressWarnings("unchecked")
    private class SpinnersOnClickListeners implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            String caption;
            AlertDialog.Builder builderSingle = new AlertDialog.Builder(ActivityReports.this);
            final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(ActivityReports.this);
            switch (view.getId()) {
                case R.id.editTextCabbage:
                    caption = getString(R.string.ent_currency);

                    final ArrayAdapter<Cabbage> arrayAdapterCabbages = new ArrayAdapter<>(ActivityReports.this, android.R.layout.select_dialog_singlechoice);
                    CabbagesDAO cabbagesDAO = CabbagesDAO.getInstance(ActivityReports.this);
                    List<Cabbage> cabbages;
                    try {
                        cabbages = (List<Cabbage>) cabbagesDAO.getAllModels();
                    } catch (Exception e) {
                        cabbages = new ArrayList<>();
                    }

                    int index = 0;
                    Cabbage currentCabbage;
                    try {
                        currentCabbage= mReportBuilder.getActiveCabbage();
                    } catch (Exception e) {
                        currentCabbage = new Cabbage();
                    }
                    for (int i = 0; i < cabbages.size(); i++) {
                        if (cabbages.get(i).getID() == currentCabbage.getID()) {
                            index = i;
                        }
                    }

                    arrayAdapterCabbages.addAll(cabbages);

                    builderSingle.setSingleChoiceItems(arrayAdapterCabbages, index,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(final DialogInterface dialog, final int which) {
                                    new Handler().postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            dialog.dismiss();
                                            ListView lw = ((AlertDialog) dialog).getListView();
                                            Cabbage cabbage = (Cabbage) lw.getAdapter().getItem(which);
                                            preferences.edit().putLong("report_cabbage_id", cabbage.getID()).apply();
                                            updateCabbageListState();
                                            updateCharts();
                                        }
                                    }, 200);

                                }
                            });
                    break;
                case R.id.editTextData:
                    mReportBuilder.setParentID(-1);
                    caption = getString(R.string.ent_data);

                    final ArrayAdapter<String> arrayAdapterData = new ArrayAdapter<>(ActivityReports.this, android.R.layout.select_dialog_singlechoice);
                    arrayAdapterData.addAll(mReportBuilder.getDataCaptions());

                    builderSingle.setSingleChoiceItems(arrayAdapterData, preferences.getInt("report_data", 0),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(final DialogInterface dialog, int which) {
                                    preferences.edit().putInt("report_data", which).apply();
                                    new Handler().postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            dialog.dismiss();
                                            mReportBuilder.loadEntitiesDataset(ActivityReports.this);
                                            updateUI();
                                            updateCharts();
                                        }
                                    }, 200);
                                }
                            });
                    break;
                case R.id.editTextDateRange:
                    caption = getString(R.string.ent_date_range);

                    final ArrayAdapter<String> arrayAdapterDateRange = new ArrayAdapter<>(ActivityReports.this, android.R.layout.select_dialog_singlechoice);
                    arrayAdapterDateRange.addAll(mReportBuilder.getDateRangeCaptions());

                    builderSingle.setSingleChoiceItems(arrayAdapterDateRange, preferences.getInt("report_date_range", 0),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(final DialogInterface dialog, int which) {
                                    preferences.edit().putInt("report_date_range", which).apply();
                                    new Handler().postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            dialog.dismiss();
                                            mReportBuilder.loadDateDataset(ActivityReports.this);
                                            updateUI();
                                            updateCharts();
                                        }
                                    }, 200);
                                }
                            });
                    break;
                case R.id.editTextShow:
                    caption = getString(R.string.ent_show);

                    final ArrayAdapter<String> arrayAdapterShow = new ArrayAdapter<>(ActivityReports.this, android.R.layout.select_dialog_singlechoice);
                    arrayAdapterShow.addAll(mReportBuilder.getShowCaptions());

                    builderSingle.setSingleChoiceItems(arrayAdapterShow, mReportBuilder.getActiveShowIndex(),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(final DialogInterface dialog, final int which) {
                                    new Handler().postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            dialog.dismiss();
                                            preferences.edit().putInt("report_show", which).apply();
                                            updateShowListState();
                                            mReportBuilder.sortDataset();
                                            updateCharts();
                                        }
                                    }, 200);
                                }
                            });
                    break;
                default:
                    return;
            }

            builderSingle.setTitle(caption);

            builderSingle.setNegativeButton(
                    getResources().getString(android.R.string.cancel),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            builderSingle.show();
        }
    }
}
