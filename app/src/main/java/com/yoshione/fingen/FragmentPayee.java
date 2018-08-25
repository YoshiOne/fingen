package com.yoshione.fingen;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.TextView;

import com.yoshione.fingen.adapter.NestedItemFullNameAdapter;
import com.yoshione.fingen.model.AutocompleteItem;

import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Leonid on 27.09.2015.
 * a
 */
public class FragmentPayee extends Fragment {

    @BindView(R.id.editTextPayee)
    AutoCompleteTextView edPayee;
    @BindView(R.id.textInputLayoutPayee)
    TextInputLayout mTextInputLayoutPayee;
    @BindView(R.id.imageButtonDeletePayee)
    ImageButton mImageButtonDeletePayee;

    private FragmentPayeeListener mCallback;

    public interface FragmentPayeeListener {
        String getPayeeName();

        String getPayeeHint();

        void onPayeeTextViewClick();

        void onPayeeItemClick(long payeeID);

        void onPayeeTyping(String payeeName);

        void onClearPayee();

        int getPayeeSelectionStyle();

        boolean isShowKeyboard();

        NestedItemFullNameAdapter getPayeeNameAutocompleteAdapter();
    }

    public static FragmentPayee newInstance() {
        return new FragmentPayee();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mCallback = (FragmentPayeeListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement FragmentPayeeListener");
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.frg_te_payee, container, false);
        ButterKnife.bind(this, view);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        edPayee.setText(mCallback.getPayeeName(), TextView.BufferType.EDITABLE);
        mTextInputLayoutPayee.setHint(mCallback.getPayeeHint());

        if (mCallback.getPayeeSelectionStyle() == 0 && Objects.requireNonNull(getActivity()).getClass().equals(ActivityEditTransaction.class)) {
            setAutocompleteAdapter();

            edPayee.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    mCallback.onPayeeTyping(charSequence.toString());
                }

                @Override
                public void afterTextChanged(Editable editable) {

                }
            });

            edPayee.setOnItemClickListener((parent, view1, position, id) -> {
                long selectedPayeeID = ((AutocompleteItem) edPayee.getAdapter().getItem(position)).getID();
                mCallback.onPayeeItemClick(selectedPayeeID);
            });

            edPayee.setOnFocusChangeListener((v, hasFocus) -> {
                if (mCallback.isShowKeyboard()) {
                    if (hasFocus) {
                        new Handler().postDelayed(() -> {
                            Activity activity = FragmentPayee.this.getActivity();
                            if (activity != null) {
                                InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                                if (imm != null) {
                                    imm.showSoftInput(v, InputMethodManager.SHOW_IMPLICIT);
                                }
                            }
                        }, 200);
                    }
                }
            });
        } else {
            edPayee.setFocusable(false);
            edPayee.setClickable(true);
            edPayee.setOnClickListener(view -> mCallback.onPayeeTextViewClick());
        }

        mImageButtonDeletePayee.setOnClickListener(view -> mCallback.onClearPayee());
    }

    @SuppressWarnings("unchecked")
    void setAutocompleteAdapter() {
        edPayee.setAdapter(mCallback.getPayeeNameAutocompleteAdapter());
    }

    void setPayeeName(String payeeName) {
        if (edPayee != null) {
            edPayee.setText(payeeName, TextView.BufferType.EDITABLE);
        }
    }

    public void setHint(String hint) {
        if (edPayee != null) {
            mTextInputLayoutPayee.setHint(hint);
        }
    }

}
