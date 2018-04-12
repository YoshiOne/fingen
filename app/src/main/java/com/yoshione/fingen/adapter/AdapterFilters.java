/*
 * Copyright (c) 2015.
 */

package com.yoshione.fingen.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.yoshione.fingen.ActivityMain;
import com.yoshione.fingen.FgConst;
import com.yoshione.fingen.FragmentDateFilterEdit;
import com.yoshione.fingen.R;
import com.yoshione.fingen.dao.AbstractDAO;
import com.yoshione.fingen.dao.AccountsDAO;
import com.yoshione.fingen.dao.BaseDAO;
import com.yoshione.fingen.filters.AbstractFilter;
import com.yoshione.fingen.filters.AccountFilter;
import com.yoshione.fingen.filters.AmountFilter;
import com.yoshione.fingen.filters.DateRangeFilter;
import com.yoshione.fingen.filters.NestedModelFilter;
import com.yoshione.fingen.filters.ViewHolderAbstractFilter;
import com.yoshione.fingen.interfaces.IAbstractModel;
import com.yoshione.fingen.managers.TreeManager;
import com.yoshione.fingen.model.BaseModel;
import com.yoshione.fingen.tag.OnTagClickListener;
import com.yoshione.fingen.tag.OnTagDeleteListener;
import com.yoshione.fingen.tag.Tag;
import com.yoshione.fingen.tag.TagView;
import com.yoshione.fingen.utils.BaseNode;
import com.yoshione.fingen.utils.ColorUtils;
import com.yoshione.fingen.utils.DateTimeFormatter;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Leonid on 05.11.2015.
 *
 */
public class AdapterFilters extends RecyclerView.Adapter {
    private final Activity mActivity;
    private final OnFilterChangeListener mOnFilterChangeListener;
    private ArrayList<AbstractFilter> mFilterList;
    private int mTextColor;
    private IEditFilterListener mEditFilterListener;

    public AdapterFilters(FragmentActivity activity, OnFilterChangeListener onFilterChangeListener, IEditFilterListener editFilterListener) {
        mFilterList = new ArrayList<>();
        mActivity = activity;
        mOnFilterChangeListener = onFilterChangeListener;
        mEditFilterListener = editFilterListener;
    }

    public ArrayList<AbstractFilter> getFilterList() {
        return mFilterList;
    }

    public void setFilterList(ArrayList<AbstractFilter> filterList) {
        mFilterList = filterList;
    }

    public int getTextColor() {
        return mTextColor;
    }

    public void setTextColor(int textColor) {
        mTextColor = textColor;
    }

    @Override
    public long getItemId(int position) {
        return mFilterList.get(position).getId();
    }

    @Override
    public int getItemViewType(int position) {
        return mFilterList.get(position).getModelType();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh = null;
        View v;
        switch (viewType) {
            case IAbstractModel.MODEL_TYPE_DATE_RANGE: {
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_filter_date_2, parent, false);
                vh = new DateRangeFilterViewHolder(v);
                break;
            }
//            case IAbstractModel.MODEL_TYPE_ACCOUNT: {
//                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_filter_tagview, parent, false);
//                vh = new AccountFilterViewHolder(v);
//                break;
//            }
            case IAbstractModel.MODEL_TYPE_AMOUNT_FILTER: {
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_filter_amount, parent, false);
                vh = new AmountFilterViewHolder(v);
                break;
            }
            case IAbstractModel.MODEL_TYPE_ACCOUNT:
            case IAbstractModel.MODEL_TYPE_PAYEE:
            case IAbstractModel.MODEL_TYPE_PROJECT:
            case IAbstractModel.MODEL_TYPE_SIMPLEDEBT:
            case IAbstractModel.MODEL_TYPE_DEPARTMENT:
            case IAbstractModel.MODEL_TYPE_LOCATION:
            case IAbstractModel.MODEL_TYPE_CATEGORY: {
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_filter_tagview, parent, false);
                vh = new NestedModelFilterViewHolder(v);
                break;
            }
        }


        return vh;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ((ViewHolderAbstractFilter) holder).setFilter(mFilterList.get(position));
        ((ViewHolderAbstractFilter) holder).bindViewHolder();
    }

    @Override
    public int getItemCount() {
        return mFilterList.size();
    }

    public void clearData() {
        int size = this.mFilterList.size();
        if (size > 0) {
            AbstractFilter filter;
            for (int i = size - 1; i >= 0; i--) {
                filter = mFilterList.get(i);
                if (!filter.getClass().equals(AccountFilter.class) || !((AccountFilter) filter).isSystem()) {
                    this.mFilterList.remove(i);
                }
            }

            this.notifyItemRangeRemoved(0, size);
            this.notifyItemRangeChanged(0, size);
        }

        mOnFilterChangeListener.onFilterChange(true);
    }

    public interface OnFilterChangeListener {
        void onFilterChange(boolean save);
    }

    private class OnFilterSwitchCheckedChangeListener implements CompoundButton.OnCheckedChangeListener {

        final AbstractFilter mFilter;

        OnFilterSwitchCheckedChangeListener(AbstractFilter mFilter) {
            this.mFilter = mFilter;
        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            mFilter.setEnabled(buttonView.isChecked());
            mOnFilterChangeListener.onFilterChange(true);
        }
    }

    class BaseViewHolder extends ViewHolderAbstractFilter {
        @BindView(R.id.caption)
        TextView textViewCaption;
        @BindView(R.id.switch_enabled)
        Switch switchEnabled;
        @BindView(R.id.buttonDelete)
        ImageButton mButtonDelete;
        @BindView(R.id.toggleButtonExclude)
        ToggleButton mToggleButtonExclude;
        AbstractFilter abstractFilter;

        BaseViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);

            itemView.setLongClickable(true);
        }

        @Override
        public AbstractFilter getFilter() {
            return abstractFilter;
        }

        @Override
        public void setFilter(AbstractFilter filter) {
            abstractFilter = filter;
        }

        @Override
        public void bindViewHolder() {

            Resources res = mActivity.getResources();
            String name;
            boolean systemFilter = false;

            switch (abstractFilter.getModelType()) {
                case IAbstractModel.MODEL_TYPE_ACCOUNT:
                    AccountFilter accountFilter = (AccountFilter) abstractFilter;
                    if (accountFilter.isSystem()) {
                        systemFilter = true;
                        name = res.getString(R.string.ent_active_account_set);
                    } else {
                        name = res.getString(R.string.ent_account);
                    }
                    break;
                case IAbstractModel.MODEL_TYPE_CATEGORY:
                    name = res.getString(R.string.ent_category);
                    break;
                case IAbstractModel.MODEL_TYPE_LOCATION:
                    name = res.getString(R.string.ent_location);
                    break;
                case IAbstractModel.MODEL_TYPE_PAYEE:
                    name = res.getString(R.string.ent_payee);
                    break;
                case IAbstractModel.MODEL_TYPE_PROJECT:
                    name = res.getString(R.string.ent_project);
                    break;
                case IAbstractModel.MODEL_TYPE_SIMPLEDEBT:
                    name = res.getString(R.string.ent_debt);
                    break;
                case IAbstractModel.MODEL_TYPE_DEPARTMENT:
                    name = res.getString(R.string.ent_department);
                    break;
                default:
                    name = "";
            }

            textViewCaption.setText(name);
            if (systemFilter) {
                switchEnabled.setVisibility(View.INVISIBLE);
            } else {
                switchEnabled.setVisibility(View.VISIBLE);
                switchEnabled.setChecked(abstractFilter.getEnabled());
                switchEnabled.setOnCheckedChangeListener(new OnFilterSwitchCheckedChangeListener(abstractFilter));
            }

            mToggleButtonExclude.setChecked(abstractFilter.isInverted());
            mToggleButtonExclude.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                    abstractFilter.setInverted(isChecked);
                    mOnFilterChangeListener.onFilterChange(true);
                }
            });

            mButtonDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mFilterList.remove(abstractFilter);
                    notifyDataSetChanged();
                    mOnFilterChangeListener.onFilterChange(true);
                }
            });
        }
    }

    class AmountFilterViewHolder extends BaseViewHolder {

        @BindView(R.id.check_box_outcome)
        AppCompatCheckBox checkBoxOutcome;
        @BindView(R.id.check_box_income)
        AppCompatCheckBox checkBoxIncome;
        @BindView(R.id.edit_text_min_amount)
        EditText editTextMin;
        @BindView(R.id.edit_text_max_amount)
        EditText editTextMax;
        @BindView(R.id.radio_group_type)
        RadioGroup radioGroupType;
        @BindView(R.id.radio_button_type_transaction)
        RadioButton radioButtonTypeTransaction;
        @BindView(R.id.radio_button_type_transfer)
        RadioButton radioButtonTypeTransfer;
        @BindView(R.id.radio_button_type_both)
        RadioButton radioButtonTypeBoth;
        private boolean mBinding;

        AmountFilterViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void bindViewHolder() {
            mBinding = true;
            super.bindViewHolder();
            final AmountFilter amountFilter = (AmountFilter) getFilter();
            textViewCaption.setText(mActivity.getResources().getString(R.string.ent_amount));
            TextWatcher watcherMin = new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (!mBinding) {
                        Double val;
                        try {
                            val = Double.valueOf(s.toString());
                            amountFilter.setmMinAmount(new BigDecimal(val));
                        } catch (NumberFormatException ignored) {
                            amountFilter.setmMinAmount(new BigDecimal(BigInteger.ZERO));
                        }
                        mOnFilterChangeListener.onFilterChange(true);
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {
                }
            };
            TextWatcher watcherMax = new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (!mBinding) {
                        Double val;
                        try {
                            val = Double.valueOf(s.toString());
                            amountFilter.setmMaxAmount(new BigDecimal(val));
                        } catch (NumberFormatException ignored) {
                            amountFilter.setmMaxAmount(new BigDecimal(Integer.MAX_VALUE));
                        }
                        mOnFilterChangeListener.onFilterChange(true);
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {
                }
            };
            if (amountFilter.getmMinAmount().compareTo(BigDecimal.ZERO) >= 0) {
                int pos = editTextMin.getSelectionStart();
                editTextMin.setText(amountFilter.getmMinAmount().toString());
                editTextMin.setSelection(Math.min(pos, editTextMin.getText().length()));
            } else {
                editTextMin.setText("");
            }
            if (amountFilter.getmMaxAmount().compareTo(new BigDecimal(Integer.MAX_VALUE)) < 0) {
                int pos = editTextMax.getSelectionStart();
                editTextMax.setText(((AmountFilter) getFilter()).getmMaxAmount().toString());
                editTextMax.setSelection(Math.min(pos, editTextMax.getText().length()));
            } else {
                editTextMax.setText("");
            }
            editTextMin.addTextChangedListener(watcherMin);
            editTextMax.addTextChangedListener(watcherMax);



            checkBoxOutcome.setChecked(((AmountFilter) getFilter()).ismOutcome());
            checkBoxIncome.setChecked(((AmountFilter) getFilter()).ismIncome());

            CompoundButton.OnCheckedChangeListener onCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (!mBinding) {
                        switch (buttonView.getId()) {
                            case R.id.check_box_income: {
                                ((AmountFilter) AmountFilterViewHolder.this.getFilter()).setmIncome(isChecked);
                                break;
                            }
                            case R.id.check_box_outcome: {
                                ((AmountFilter) AmountFilterViewHolder.this.getFilter()).setmOutcome(isChecked);
                                break;
                            }
                        }
                        mOnFilterChangeListener.onFilterChange(true);
                    }
                }
            };
            checkBoxIncome.setOnCheckedChangeListener(onCheckedChangeListener);
            checkBoxOutcome.setOnCheckedChangeListener(onCheckedChangeListener);

            switch (((AmountFilter) getFilter()).getTransfer()) {
                case AmountFilter.TRANSACTION_TYPE_TRANSACTION: {
                    radioButtonTypeTransaction.setChecked(true);
                    break;
                }
                case AmountFilter.TRANSACTION_TYPE_TRANSFER: {
                    radioButtonTypeTransfer.setChecked(true);
                    break;
                }
                case AmountFilter.TRANSACTION_TYPE_BOTH: {
                    radioButtonTypeBoth.setChecked(true);
                    break;
                }
            }

            radioGroupType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    if (!mBinding) {
                        switch (checkedId) {
                            case R.id.radio_button_type_transaction: {
                                ((AmountFilter) AmountFilterViewHolder.this.getFilter()).setmTransfer(AmountFilter.TRANSACTION_TYPE_TRANSACTION);
                                break;
                            }
                            case R.id.radio_button_type_transfer: {
                                ((AmountFilter) AmountFilterViewHolder.this.getFilter()).setmTransfer(AmountFilter.TRANSACTION_TYPE_TRANSFER);
                                break;
                            }
                            case R.id.radio_button_type_both: {
                                ((AmountFilter) AmountFilterViewHolder.this.getFilter()).setmTransfer(AmountFilter.TRANSACTION_TYPE_BOTH);
                                break;
                            }
                        }
                        mOnFilterChangeListener.onFilterChange(true);
                    }
                }
            });
            mBinding = false;
        }
    }

    class AccountFilterViewHolder extends BaseViewHolder {

        //        AccountFilter accountFilter;
        @BindView(R.id.tag_view)
        TagView tagView;

        AccountFilterViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);
        }

        @Override
        public void bindViewHolder() {
            super.bindViewHolder();
            initTagView();
        }

        private void initTagView() {
            ArrayList<Tag> tags = new ArrayList<>();
            Tag tag;

            final AccountsDAO accountsDAO = AccountsDAO.getInstance(mActivity);
            for (long id : getFilter().getIDsSet()) {
                tag = new Tag(new SpannableString(accountsDAO.getAccountByID(id).getName()));
                tag.radius = 10f;
                tag.layoutColor = ColorUtils.getColor(mActivity);
                tag.isDeletable = true;
                tags.add(tag);
            }
            tagView.setAlignEnd(false);
            tagView.addTags(tags);
            tagView.setOnTagDeleteListener(new OnTagDeleteListener() {
                @Override
                public void onTagDeleted(TagView view, Tag tag1, int position) {
                    long accId;
                    try {
                        accId = accountsDAO.getModelByName(tag1.text.toString()).getID();
                    } catch (Exception e) {
                        accId = -1;
                    }
                    AccountFilter filter = ((AccountFilter) AccountFilterViewHolder.this.getFilter());
                    filter.removeAccount(accId);
                    if (filter.getIDsSet().isEmpty()) {
                        mFilterList.remove(filter);
                    }
                    mOnFilterChangeListener.onFilterChange(true);
                    notifyDataSetChanged();
                }
            });
        }
    }

    class NestedModelFilterViewHolder extends BaseViewHolder {

        @BindView(R.id.tag_view)
        TagView tagView;

        NestedModelFilterViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);
        }

        @Override
        public void bindViewHolder() {
            super.bindViewHolder();

            initTagView();
        }

        @SuppressWarnings("unchecked")
        private void initTagView() {
            ArrayList<Tag> tags = new ArrayList<>();
            Tag tag;

            AbstractDAO dao = BaseDAO.getDAO(getFilter().getModelType(), mActivity);
            BaseNode tree;
            try {
                tree = TreeManager.convertListToTree((List<IAbstractModel>) dao.getAllModels(), getFilter().getModelType());
            } catch (Exception e) {
                tree = new BaseNode(BaseModel.createModelByType(getFilter().getModelType()), null);
            }

            BaseNode node;
            for (long id : getFilter().getIDsSet()) {
                try {
                    tag = new Tag(new SpannableString(tree.getNodeById(id).getModel().getFullName()));
                } catch (Exception e) {
                    tag = new Tag(new SpannableString(""));
                }
//                tag.tagTextColor = ColorUtils.getTextColor(mActivity);
                if (getFilter().getModelType() == IAbstractModel.MODEL_TYPE_CATEGORY) {
                    try {
                        node = tree.getNodeById(id);
                        tag.layoutColor = node.getModel().getColor();
                    } catch (Exception e) {
                        tag.layoutColor = Color.GRAY;
                    }
                } else {
                    tag.layoutColor = ColorUtils.getColor(mActivity);
                }
                tag.isDeletable = true;
                tag.setID(id);
                tags.add(tag);
            }

            tag = new Tag(new SpannableString("   "));
            tag.isDeletable = false;
            tag.setID(0);
            tag.background = ContextCompat.getDrawable(mActivity, R.drawable.ic_add_blue);
            tag.onTagClickListener = new OnTagClickListener() {
                @Override
                public void onTagClick(Tag tag, int position) {
                    mEditFilterListener.OnEditClick(abstractFilter);
                }
            };
            tags.add(tag);

            tagView.setAlignEnd(false);
            tagView.addTags(tags);
            tagView.setOnTagDeleteListener(new OnTagDeleteListener() {
                @Override
                public void onTagDeleted(TagView view, Tag tag1, int position) {
                    AbstractFilter filter = NestedModelFilterViewHolder.this.getFilter();
                    if (filter.getClass().equals(NestedModelFilter.class)) {
                        ((NestedModelFilter) filter).removeModel(tag1.getID());
                    } else {
                        ((AccountFilter) filter).removeAccount(tag1.getID());
                    }
                    if (filter.getIDsSet().isEmpty()) {
                        mFilterList.remove(filter);
                    }
                    mOnFilterChangeListener.onFilterChange(true);
                    notifyDataSetChanged();
                }
            });

        }
    }

    class DateRangeFilterViewHolder extends BaseViewHolder implements DatePickerDialog.OnDateSetListener {

        @BindView(R.id.edit_text_start_date)
        EditText buttonStartDate;
        @BindView(R.id.edit_text_end_date)
        EditText buttonEndDate;
        @BindView(R.id.button_more)
        ImageButton buttonMore;

        DateRangeFilterViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);

            buttonMore.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v1) {
                    final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mActivity);
                    int range = preferences.getInt(FgConst.PREF_DEF_DATE_RANGE, DateRangeFilter.DATE_RANGE_MONTH);
                    int modifier = preferences.getInt(FgConst.PREF_DEF_DATE_MODIFIER, DateRangeFilter.DATE_RANGE_MODIFIER_CURRENT);
                    FragmentDateFilterEdit alertDialog = FragmentDateFilterEdit.newInstance(range, modifier);
                    alertDialog.setOnComplete(new FragmentDateFilterEdit.IOnComplete() {
                        @Override
                        public void onComplete(int range, int modifier) {((DateRangeFilter) getFilter()).setRange(range, modifier, mActivity);
                            preferences.edit()
                                    .putInt(FgConst.PREF_DEF_DATE_RANGE, range)
                                    .putInt(FgConst.PREF_DEF_DATE_MODIFIER, modifier)
                                    .apply();
                            initDates();
                            mOnFilterChangeListener.onFilterChange(true);
                        }
                    });
                    alertDialog.show(mActivity.getFragmentManager(), "fragment_date_filter_edit");
                }
            });
        }

        @Override
        public void bindViewHolder() {
            super.bindViewHolder();
            textViewCaption.setText(mActivity.getResources().getString(R.string.ent_date_range));

            initDates();

            switchEnabled.setChecked(getFilter().getEnabled());
            switchEnabled.setOnCheckedChangeListener(new OnFilterSwitchCheckedChangeListener(getFilter()));
        }

        private void initDates() {
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.getInstance(mActivity);
            buttonStartDate.setText(dateTimeFormatter.getDateMediumString(((DateRangeFilter) getFilter()).getmStartDate()));
            buttonEndDate.setText(dateTimeFormatter.getDateMediumString(((DateRangeFilter) getFilter()).getmEndDate()));
            buttonStartDate.setOnClickListener(new OnDateFieldsClickListener(this));
            buttonEndDate.setOnClickListener(new OnDateFieldsClickListener(this));
        }

        @Override
        public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(((DateRangeFilter) getFilter()).getmStartDate());
            switch (view.getArguments().getInt("DateType")) {
                case 0:
                    calendar.set(year, monthOfYear, dayOfMonth, 0, 0, 0);
                    ((DateRangeFilter) getFilter()).setmStartDate(calendar.getTime());
                    break;
                case 1:
                    calendar.set(year, monthOfYear, dayOfMonth, 23, 59, 59);
                    ((DateRangeFilter) getFilter()).setmEndDate(calendar.getTime());
                    break;
            }
            mOnFilterChangeListener.onFilterChange(true);
            initDates();
        }

        private class OnDateFieldsClickListener implements View.OnClickListener {
            final DateRangeFilterViewHolder vh;

            OnDateFieldsClickListener(DateRangeFilterViewHolder vh) {
                this.vh = vh;
            }

            @Override
            public void onClick(View v) {
                int id = 0;
                Calendar calendar = Calendar.getInstance();
                switch (v.getId()) {
                    case R.id.edit_text_start_date:
                        calendar.setTime(((DateRangeFilter) getFilter()).getmStartDate());
                        id = 0;
                        break;
                    case R.id.edit_text_end_date:
                        calendar.setTime(((DateRangeFilter) getFilter()).getmEndDate());
                        id = 1;
                        break;
                }

                com.wdullaer.materialdatetimepicker.date.DatePickerDialog dpd = com.wdullaer.materialdatetimepicker.date.DatePickerDialog.newInstance(
                        vh,
                        calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)
                );
                int theme = Integer.valueOf(PreferenceManager.getDefaultSharedPreferences(mActivity).getString("theme", "0"));
                dpd.setThemeDark(theme == ActivityMain.THEME_DARK);
                dpd.vibrate(false);
                dpd.dismissOnPause(false);
                Bundle args = new Bundle();
                args.putInt("DateType", id);
                dpd.setArguments(args);

                dpd.show(mActivity.getFragmentManager(), "Datepickerdialog");
            }
        }

    }

    public interface IEditFilterListener {
        void OnEditClick(AbstractFilter filter);
    }

}
