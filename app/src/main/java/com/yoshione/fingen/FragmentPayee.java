package com.yoshione.fingen;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v7.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Filter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.yoshione.fingen.dao.PayeesDAO;
import com.yoshione.fingen.managers.TreeManager;
import com.yoshione.fingen.interfaces.IAbstractModel;
import com.yoshione.fingen.model.AutocompleteItem;
import com.yoshione.fingen.model.Payee;
import com.yoshione.fingen.utils.BaseNode;
import com.yoshione.fingen.utils.IconGenerator;
import com.yoshione.fingen.utils.Lg;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Leonid on 27.09.2015.
 * a
 */
public class FragmentPayee extends Fragment {

    private static final String TAG = "FragmentPayee";
    @BindView(R.id.editTextPayee)
    AutoCompleteTextView edPayee;
    @BindView(R.id.textInputLayoutPayee)
    TextInputLayout mTextInputLayoutPayee;
    @BindView(R.id.imageButtonDeletePayee)
    ImageButton mImageButtonDeletePayee;
    private String mHint;
    private PayeeTextChangeListener payeeTextChangeListener;
    private String mPayeeName = "";
    private Boolean mShowKeyboard = true;

    public void setPayeeOnClickListener(View.OnClickListener payeeOnClickListener) {
        mPayeeOnClickListener = payeeOnClickListener;
    }

    private View.OnClickListener mPayeeOnClickListener;

    public void setShowKeyboard(Boolean showKeyboard) {
        mShowKeyboard = showKeyboard;
    }

    void setPayeeTextChangeListener(PayeeTextChangeListener payeeTextChangeListener) {
        this.payeeTextChangeListener = payeeTextChangeListener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.frg_te_payee, container, false);
        ButterKnife.bind(this, view);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        int payeeSelectionStyle = Integer.valueOf(PreferenceManager.getDefaultSharedPreferences(getActivity()).getString("payee_selection_style", "0"));

        edPayee.setText(mPayeeName, TextView.BufferType.EDITABLE);

        if (payeeSelectionStyle == 0 && getActivity().getClass().equals(ActivityEditTransaction.class)) {
            setAutocompleteAdapter();

            edPayee.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    if (payeeTextChangeListener != null) {
                        payeeTextChangeListener.OnPayeeTyping(charSequence.toString());
                    }
                }

                @Override
                public void afterTextChanged(Editable editable) {

                }
            });

            edPayee.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view1, int position, long id) {
                    Lg.d(TAG, "FragmentPayee.edPayee.setOnItemClickListener");
                    if (payeeTextChangeListener != null) {
                        long selectedID = ((AutocompleteItem) edPayee.getAdapter().getItem(position)).getID();
                        Payee payee = PayeesDAO.getInstance(getActivity()).getPayeeByID(selectedID);
                        payeeTextChangeListener.OnPayeeItemClick(payee);
                    }
                }
            });

            edPayee.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(final View v, boolean hasFocus) {
                    if (mShowKeyboard) {
                        if (hasFocus) {
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    InputMethodManager imm = (InputMethodManager) FragmentPayee.this.getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                                    imm.showSoftInput(v, InputMethodManager.SHOW_IMPLICIT);
                                }
                            }, 200);
                        }
                    }
                }
            });
        } else {
            edPayee.setFocusable(false);
            edPayee.setClickable(true);
            edPayee.setOnClickListener(mPayeeOnClickListener);
        }

        edPayee.setHint("");
        mTextInputLayoutPayee.setHint(mHint);

//        mImageButtonDeletePayee.setImageDrawable(IconGenerator.getInstance(getActivity()).getDeleteIcon(getActivity()));
        mImageButtonDeletePayee.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                payeeTextChangeListener.OnClearPayee();
            }
        });
    }

    @SuppressWarnings("unchecked")
    void setAutocompleteAdapter() {
        PayeesDAO payeesDAO = PayeesDAO.getInstance(getActivity());
        NestedItemFullNameAdapter mAutoCompleteAdapterPayee;
        List<IAbstractModel> payees;
        List<AutocompleteItem> autocompleteItems = new ArrayList<>();
        try {
            payees = (List<IAbstractModel>) payeesDAO.getAllModels();
        } catch (Exception e) {
            payees = new ArrayList<>();
        }

        BaseNode tree = TreeManager.convertListToTree(payees, IAbstractModel.MODEL_TYPE_PAYEE);
        for (BaseNode node : tree.getFlatChildrenList()) {
            autocompleteItems.add(new AutocompleteItem(node.getModel().getID(), node.getModel().getFullName()));
        }

        mAutoCompleteAdapterPayee = new NestedItemFullNameAdapter(getActivity(), android.R.layout.simple_spinner_dropdown_item, autocompleteItems);
        edPayee.setAdapter(mAutoCompleteAdapterPayee);
    }

    void setPayeeName(String payeeName) {
        mPayeeName = payeeName;
        if (edPayee != null) {
            edPayee.setText(payeeName, TextView.BufferType.EDITABLE);
        }
    }

    public void setHint(String hint) {
        mHint = hint;
        if (edPayee != null) {
            edPayee.setHint("");
            mTextInputLayoutPayee.setHint(mHint);
        }
    }

    interface PayeeTextChangeListener {
        void OnPayeeItemClick(Payee payee);

        void OnPayeeTyping(String payeeName);

        void OnClearPayee();
    }

    private class NestedItemFullNameAdapter extends ArrayAdapter<AutocompleteItem> {
        private List<AutocompleteItem> suggestions;
        private List<AutocompleteItem> itemsAll;
        Filter nameFilter = new Filter() {
            @Override
            public String convertResultToString(Object resultValue) {
                return ((AutocompleteItem) (resultValue)).getFullName();
            }

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                if (constraint != null) {
                    suggestions.clear();
                    for (AutocompleteItem item : itemsAll) {
                        if (item.getFullName().toLowerCase().contains(constraint.toString().toLowerCase())) {
                            suggestions.add(item);
                        }
                    }
                    FilterResults filterResults = new FilterResults();
                    filterResults.values = suggestions;
                    filterResults.count = suggestions.size();
                    return filterResults;
                } else {
                    return new FilterResults();
                }
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults results) {
                List<AutocompleteItem> filteredList = (List<AutocompleteItem>) results.values;
                if (results != null && results.count > 0) {
                    clear();
                    for (AutocompleteItem c : filteredList) {
                        add(c);
                    }
                    notifyDataSetChanged();
                }
            }
        };

        NestedItemFullNameAdapter(Context context, int textViewResourceId, List<AutocompleteItem> items) {
            super(context, textViewResourceId, items);
            itemsAll = new ArrayList<>();
            itemsAll.addAll(items);
            suggestions = new ArrayList<>();
        }

        @NonNull
        @Override
        public Filter getFilter() {
            return nameFilter;
        }
    }

}
