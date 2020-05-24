package com.yoshione.fingen.managers;

import android.app.Activity;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.yoshione.fingen.R;
import com.yoshione.fingen.dao.ProductsDAO;
import com.yoshione.fingen.model.Product;

public class ProductManager {

    public static void showEditDialog(final Product product, final Activity activity) {
        final EditText input = (EditText) activity.getLayoutInflater().inflate(R.layout.template_edittext, null);
        input.setText(product.getName());

        String title = product.getID() < 0 ? activity.getResources().getString(R.string.ttl_new_product) : activity.getResources().getString(R.string.ttl_edit_product);

        new AlertDialog.Builder(activity)
                .setTitle(title)
                .setView(input)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    String newName = input.getText().toString();
                    if (!newName.isEmpty()) {
                        ProductsDAO productsDAO = ProductsDAO.getInstance(activity);
                        product.setName(input.getText().toString());
                        try {
                            productsDAO.createModel(product);
                        } catch (Exception e) {
                            Toast.makeText(activity, R.string.msg_error_on_write_to_db, Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .show();

        input.requestFocus();
    }
}
