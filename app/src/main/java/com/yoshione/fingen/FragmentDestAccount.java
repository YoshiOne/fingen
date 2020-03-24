package com.yoshione.fingen;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Leonid on 27.09.2015.
 *
 */
public class FragmentDestAccount extends Fragment {

    @BindView(R.id.textViewDestAccount)
    EditText textViewDestAccount;
    @BindView(R.id.imageButtonInvertTransferDirection)
    ImageButton imageButtonInvertTransferDirection;

    FragmentDestAccountListener mCallback;

    public interface FragmentDestAccountListener {
        void destAccountTextViewClick();

        void InvertTransferDirectionClick();

        String getDestAccountName();
    }

    public static FragmentDestAccount newInstance() {
        return new FragmentDestAccount();
    }

    public View onCreateView(@NonNull final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frg_te_dest_account, container, false);
        ButterKnife.bind(this, view);

        ActivityEditTransaction activityEditTransaction = (ActivityEditTransaction) getActivity();

        if (activityEditTransaction != null) {
            textViewDestAccount.setOnClickListener(view12 -> {
                if (mCallback != null) {
                    mCallback.destAccountTextViewClick();
                }
            });

            imageButtonInvertTransferDirection.setOnClickListener(view1 -> {
                if (mCallback != null) {
                    mCallback.InvertTransferDirectionClick();
                    textViewDestAccount.setText(mCallback.getDestAccountName());
                }
            });
        }

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mCallback != null) {
            textViewDestAccount.setText(mCallback.getDestAccountName());
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mCallback = (FragmentDestAccountListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement FragmentDestAccountListener");
        }
    }
}
