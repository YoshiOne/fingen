package com.yoshione.fingen;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.yoshione.fingen.dao.CategoriesDAO;
import com.yoshione.fingen.dao.ProductsDAO;
import com.yoshione.fingen.dao.ProjectsDAO;
import com.yoshione.fingen.interfaces.IAbstractModel;
import com.yoshione.fingen.model.Product;
import com.yoshione.fingen.model.ProductEntry;
import com.yoshione.fingen.widgets.AmountEditor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

import static android.app.Activity.RESULT_OK;
import static com.yoshione.fingen.utils.RequestCodes.REQUEST_CODE_SELECT_MODEL;

/**
 * Created by slv on 05.02.2018.
 *
 */

public class FragmentProductEntryEdit extends DialogFragment {

    @BindView(R.id.textViewProduct)
    AutoCompleteTextView mTextViewProduct;
    @BindView(R.id.textInputLayoutProduct)
    TextInputLayout mTextInputLayoutProduct;
    @BindView(R.id.imageButtonDeleteProduct)
    ImageButton mImageButtonDeleteProduct;
    @BindView(R.id.textViewCategory)
    EditText mTextViewCategory;
    @BindView(R.id.textInputLayoutCategory)
    TextInputLayout mTextInputLayoutCategory;
    @BindView(R.id.imageButtonDeleteCategory)
    ImageButton mImageButtonDeleteCategory;
    @BindView(R.id.textViewProject)
    EditText mTextViewProject;
    @BindView(R.id.textInputLayoutProject)
    TextInputLayout mTextInputLayoutProject;
    @BindView(R.id.imageButtonDeleteProject)
    ImageButton mImageButtonDeleteProject;
    @BindView(R.id.amount_editor)
    AmountEditor mAmountEditor;
    @BindView(R.id.textViewQuantity)
    EditText mTextViewQuantity;
    @BindView(R.id.textInputLayoutQuantity)
    TextInputLayout mTextInputLayoutQuantity;
    @BindView(R.id.imageButtonMore)
    ImageButton mImageButtonMore;
    @BindView(R.id.imageButtonLess)
    ImageButton mImageButtonLess;
    Unbinder unbinder;
    ProductEntry mProductEntry;

    public void setEntryEditListener(IProductEntryEditListener entryEditListener) {
        mEntryEditListener = entryEditListener;
    }

    IProductEntryEditListener mEntryEditListener;

    public FragmentProductEntryEdit() {
    }

    public static FragmentProductEntryEdit newInstance(String title, ProductEntry productEntry) {
        FragmentProductEntryEdit frag = new FragmentProductEntryEdit();
        Bundle args = new Bundle();
        args.putString("title", title);
        args.putParcelable("product_entry", productEntry);
        frag.setArguments(args);

        return frag;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String title = getArguments().getString("title");
        mProductEntry = getArguments().getParcelable("product_entry");
        View view = getActivity().getLayoutInflater().inflate(R.layout.fragment_product_entry_edit, null);
        unbinder = ButterKnife.bind(this, view);
        mAmountEditor.setActivity(getActivity());

        if (mProductEntry != null) {

            Product product = ProductsDAO.getInstance(getActivity()).getProductByID(mProductEntry.getProductID());
            mTextViewProduct.setText(product.getName());
            setProductsAutocompleteAdapter(getActivity());
            mImageButtonDeleteProduct.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mProductEntry.setProductID(-1);
                    mTextViewProduct.setText("");
                }
            });

            mTextViewCategory.setText(CategoriesDAO.getInstance(getActivity()).getCategoryByID(mProductEntry.getCategoryID()).getFullName());
            mTextViewCategory.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(), ActivityList.class);
                    intent.putExtra("showHomeButton", false);
                    intent.putExtra("model", CategoriesDAO.getInstance(getActivity()).getCategoryByID(mProductEntry.getCategoryID()));
                    intent.putExtra("requestCode", REQUEST_CODE_SELECT_MODEL);
                    startActivityForResult(intent, REQUEST_CODE_SELECT_MODEL);
                }
            });
            mImageButtonDeleteCategory.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mProductEntry.setCategoryID(-1);
                    mTextViewCategory.setText("");
                }
            });

            mTextViewProject.setText(ProjectsDAO.getInstance(getActivity()).getProjectByID(mProductEntry.getProjectID()).getFullName());
            mTextViewProject.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(), ActivityList.class);
                    intent.putExtra("showHomeButton", false);
                    intent.putExtra("model", ProjectsDAO.getInstance(getActivity()).getProjectByID(mProductEntry.getProjectID()));
                    intent.putExtra("requestCode", REQUEST_CODE_SELECT_MODEL);
                    startActivityForResult(intent, REQUEST_CODE_SELECT_MODEL);
                }
            });
            mImageButtonDeleteProject.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mProductEntry.setProjectID(-1);
                    mTextViewProject.setText("");
                }
            });

            mAmountEditor.setAmount(mProductEntry.getPrice());
            mAmountEditor.setType(mProductEntry.getPrice().compareTo(BigDecimal.ZERO));
            mAmountEditor.setHint(getString(R.string.ent_price));
            mAmountEditor.mOnAmountChangeListener = new AmountEditor.OnAmountChangeListener() {
                @Override
                public void OnAmountChange(BigDecimal newAmount, int newType) {
                    mProductEntry.setPrice(newAmount.multiply(new BigDecimal((newType > 0) ? 1 : -1)));
                }
            };

            mTextViewQuantity.setText(String.valueOf(mProductEntry.getQuantity()));
            mTextViewQuantity.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    BigDecimal quantity;
                    try {
                        quantity = new BigDecimal(mTextViewQuantity.getText().toString());
                    } catch (Exception e) {
                        quantity = BigDecimal.ONE;
                    }
                    mProductEntry.setQuantity(quantity);
                }

                @Override
                public void afterTextChanged(Editable editable) {

                }
            });
            View.OnClickListener listener = new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    BigDecimal quantity = mProductEntry.getQuantity();
                    switch (view.getId()) {
                        case R.id.imageButtonMore:
                            mProductEntry.setQuantity(quantity.add(BigDecimal.ONE));
                            break;
                        case R.id.imageButtonLess:
                            mProductEntry.setQuantity(quantity.subtract(BigDecimal.ONE));
                            break;
                    }
                    mTextViewQuantity.setText(String.valueOf(mProductEntry.getQuantity()));
                }
            };
            mImageButtonMore.setOnClickListener(listener);
            mImageButtonLess.setOnClickListener(listener);
        }

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setView(view);
        alertDialogBuilder.setTitle(title);
        alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        alertDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });


        return alertDialogBuilder.create();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == REQUEST_CODE_SELECT_MODEL && data != null) {
            IAbstractModel model = data.getParcelableExtra("model");
            switch (model.getModelType()) {
                case IAbstractModel.MODEL_TYPE_CATEGORY:
                    mProductEntry.setCategoryID(model.getID());
                    mTextViewCategory.setText(model.getFullName());
                    break;
                case IAbstractModel.MODEL_TYPE_PROJECT:
                    mProductEntry.setProjectID(model.getID());
                    mTextViewProject.setText(model.getFullName());
                    break;
            }
        }
    }

    void setProductsAutocompleteAdapter(Context context) {
        ProductsDAO productsDAO = ProductsDAO.getInstance(context);
        List<Product> products;
        try {
            products = productsDAO.getAllProducts();
        } catch (Exception e) {
            products = new ArrayList<>();
        }

        ArrayAdapter<Product> productArrayAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_dropdown_item, products);

        mTextViewProduct.setAdapter(productArrayAdapter);
        mTextViewProduct.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Product product = (Product) mTextViewProduct.getAdapter().getItem(i);
                mProductEntry.setProductID(product.getID());
            }
        });
    }

    public interface IProductEntryEditListener {
        void onProductEntryEdited(ProductEntry productEntry);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @Override
    public void onStart() {
        super.onStart();
        AlertDialog d = (AlertDialog) getDialog();
        if (d != null) {
            Button positiveButton = d.getButton(Dialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(new OnOkListener());
        }
        Dialog dialog = getDialog();
        if (dialog != null) {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.WRAP_CONTENT;
            dialog.getWindow().setLayout(width, height);
        }
    }

    private class OnOkListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            if (mTextViewProduct.getText().toString().isEmpty()) {
                mTextInputLayoutProduct.setError(getString(R.string.err_set_product));
                return;
            }
            ProductsDAO productsDAO = ProductsDAO.getInstance(getActivity());
            Product product = productsDAO.getProductByID(mProductEntry.getProductID());
            String name = mTextViewProduct.getText().toString();
            if (!product.getName().toLowerCase().equals(name.toLowerCase())) {
                try {
                    product = (Product) productsDAO.getModelByName(name);
                } catch (Exception e) {
                    return;
                }
                if (product.getID() < 0) {
                    product.setName(name);
                    try {
                        product = (Product) productsDAO.createModel(product);
                    } catch (Exception e) {
                        return;
                    }
                }
                mProductEntry.setProductID(product.getID());
            }

            mEntryEditListener.onProductEntryEdited(mProductEntry);
            dismiss();
        }
    }
}
