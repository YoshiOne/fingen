package com.yoshione.fingen;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;

import com.yoshione.fingen.interfaces.IAbstractModel;
import com.yoshione.fingen.model.Cabbage;
import com.yoshione.fingen.model.Events;
import com.yoshione.fingen.utils.LocaleUtils;
import com.yoshione.fingen.utils.RequestCodes;
import com.yoshione.fingen.widgets.ToolbarActivity;

import org.greenrobot.eventbus.EventBus;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by slv on 07.03.2016.
 *
 */
public class ActivityBudget extends ToolbarActivity {
//    private static final String TAG = "ActivityBudget";

    private static final int YEAR_ZERO = 2000;
    private static final int POS_ZERO = Integer.MAX_VALUE / 2;

    @BindView(R.id.viewPager)
    ViewPager viewPager;
    private List<FragmentBudget> fragments;

    boolean canAccessDb = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.bind(this);

        fragments = new ArrayList<>();

        Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);

        FragmentPagerAdapter fragmentPagerAdapter = new FragmentPagerAdapter(getSupportFragmentManager()) {

            @Override
            public int getCount() {
                return Integer.MAX_VALUE;
            }

            @Override
            public Fragment getItem(final int position) {
                return findFragment(position);
            }

            @Override
            public CharSequence getPageTitle(final int position) {
                return "";
            }

        };
        viewPager.setAdapter(fragmentPagerAdapter);

        viewPager.setOffscreenPageLimit(1);
        viewPager.setCurrentItem(convertYMtoPos(year,month),false);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getTitle(viewPager.getCurrentItem()));
        }

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @SuppressLint("DefaultLocale")
            @Override
            public void onPageSelected(int position) {
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setTitle(getTitle(position));
                    Log.d(TAG, String.format("%d %d %d", position, convertPosToYear(position), convertPosToMonth(position)));
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

    }

    private FragmentBudget findFragment(int position) {
        int year = convertPosToYear(position);
        int month = convertPosToMonth(position);
        for (FragmentBudget fragment : fragments) {
            if (fragment.getmYear() == year & fragment.getmMonth() == month) {
                return fragment;
            }
        }
        FragmentBudget fragmentBudget = FragmentBudget.newInstance(year, month);
        fragments.add(fragmentBudget);
        return fragmentBudget;
    }

    private int convertYMtoPos(int year, int month) {
        return year * 12 + month - YEAR_ZERO *12 + POS_ZERO;
    }

    int convertPosToYear(int pos) {
        return (pos - POS_ZERO) / 12 + YEAR_ZERO;
    }

    int convertPosToMonth(int pos) {
        return (pos - POS_ZERO) % 12;
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_budget;
    }

    private String getTitle(int position) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("LLLL yyyy", LocaleUtils.getLocale(this));
        Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR,convertPosToYear(position));
        c.set(Calendar.MONTH,convertPosToMonth(position));
        c.set(Calendar.DAY_OF_MONTH,1);
        CharSequence date = dateFormat.format(c.getTime());
        return String.valueOf(date);
    }

    @Override
    protected String getLayoutTitle() {
        return "";
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == RequestCodes.REQUEST_CODE_SELECT_MODEL && data != null) {
            IAbstractModel model = data.getParcelableExtra("model");
            switch (model.getModelType()) {
                case IAbstractModel.MODEL_TYPE_CABBAGE:
                    EventBus.getDefault().postSticky(new Events.EventOnChangeCabbageInAmountEditor((Cabbage) model));
                    break;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
