/*
 * Copyright (c) 2015.
 */

package com.yoshione.fingen.managers;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.yoshione.fingen.R;
import com.yoshione.fingen.dao.CategoriesDAO;
import com.yoshione.fingen.dao.PayeesDAO;
import com.yoshione.fingen.interfaces.IAbstractModel;
import com.yoshione.fingen.model.Category;
import com.yoshione.fingen.model.Payee;
import com.yoshione.fingen.utils.BaseNode;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Leonid on 22.11.2015.
 * f
 */
public class PayeeManager {

    public static Category getDefCategory(Payee payee, Context context) {
        if (payee.getDefCategoryID() < 0) {
            return new Category();
        } else {
            CategoriesDAO categoriesDAO = CategoriesDAO.getInstance(context);
            return categoriesDAO.getCategoryByID(payee.getDefCategoryID());
        }
    }

    public static void showEditDialog(final Payee payee, final Activity activity) {

        String title;
        if (payee.getID() < 0) {
            title = activity.getResources().getString(R.string.ttl_new_payee);
        } else {
            title = activity.getResources().getString(R.string.ttl_edit_payee);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(title);
        final EditText input = (EditText) activity.getLayoutInflater().inflate(R.layout.template_edittext, null);
        input.setText(payee.getName());
        builder.setView(input);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String newName = input.getText().toString();
                if (!newName.isEmpty()) {
                    PayeesDAO payeesDAO = PayeesDAO.getInstance(activity);
                    payee.setName(input.getText().toString());
                    try {
                        payeesDAO.createModel(payee);
                    } catch (Exception e) {
                        Toast.makeText(activity, R.string.msg_error_on_write_to_db, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        builder.show();
        input.requestFocus();
    }

    public static void ShowSelectPayeeDialog(Activity activity, final OnSelectPayeeListener selectPayeeListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(activity.getResources().getString(R.string.ttl_select_payee));

        ArrayAdapter<Payee> arrayAdapter = new ArrayAdapter<>(activity, android.R.layout.simple_list_item_1);
        List<Payee> payees = null;
        try {
            payees = PayeesDAO.getInstance(activity).getAllPayees();
        } catch (Exception e) {
            payees = new ArrayList<>();
        }
        arrayAdapter.addAll(payees);

        builder.setNegativeButton(
                activity.getResources().getString(android.R.string.cancel),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        builder.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ListView listView = ((AlertDialog) dialog).getListView();
                Payee payee = (Payee) listView.getAdapter().getItem(which);
                selectPayeeListener.OnSelectPayee(payee);
            }
        });
        builder.show();
    }

    public static long checkPayeeAndCreateIfNecessary(long payeeID, String text, Context context) throws Exception {
        String fullName = PayeesDAO.getInstance(context).getPayeeByID(payeeID).getFullName();
        if (text.isEmpty()) {
            return -1;
        } else if (fullName.toLowerCase().equals(text.toLowerCase())) {
            return payeeID;
        } else {
            List<BaseNode> payeesTree = AbstractModelManager.createModelWithFullName(text, IAbstractModel.MODEL_TYPE_PAYEE, context).getFlatChildrenList();
            int treeSize = payeesTree.size();
            PayeesDAO payeesDAO = PayeesDAO.getInstance(context);
            long parentID = -1;
            for (BaseNode node : payeesTree) {
                if (node.getModel().getID() < 0) {
                    Payee payee = (Payee) node.getModel();
                    payee.setParentID(parentID);
                    payee = (Payee) payeesDAO.createModel(payee);
                    parentID = payee.getID();
                    node.setModel(payee);
                } else {
                    parentID = node.getModel().getID();
                }
            }
            return payeesTree.get(treeSize - 1).getModel().getID();
        }
    }

    public interface OnSelectPayeeListener {
        void OnSelectPayee(Payee selectedPayee);
    }

}
