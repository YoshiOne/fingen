package com.yoshione.fingen.managers;

import android.app.Activity;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.widget.EditText;
import android.widget.Toast;

import com.yoshione.fingen.R;
import com.yoshione.fingen.dao.ProductsDAO;
import com.yoshione.fingen.model.Product;

/**
 * Created by slv on 06.02.2018.
 *
 */

public class ProductManager {

    public static void showEditDialog(final Product product, final Activity activity) {

        String title;
        if (product.getID() < 0) {
            title = activity.getResources().getString(R.string.ttl_new_product);
        } else {
            title = activity.getResources().getString(R.string.ttl_edit_product);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(title);
        final EditText input = (EditText) activity.getLayoutInflater().inflate(R.layout.template_edittext, null);
        input.setText(product.getName());
        builder.setView(input);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
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
            }
        });

        builder.show();
        input.requestFocus();
    }
}
