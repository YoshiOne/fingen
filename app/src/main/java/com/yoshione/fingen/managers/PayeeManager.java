package com.yoshione.fingen.managers;

import android.app.Activity;
import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.yoshione.fingen.R;
import com.yoshione.fingen.dao.CategoriesDAO;
import com.yoshione.fingen.dao.PayeesDAO;
import com.yoshione.fingen.interfaces.IAbstractModel;
import com.yoshione.fingen.model.Category;
import com.yoshione.fingen.model.Payee;
import com.yoshione.fingen.utils.BaseNode;

import java.util.List;

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
        final EditText input = (EditText) activity.getLayoutInflater().inflate(R.layout.template_edittext, null);
        input.setText(payee.getName());

        String title = payee.getID() < 0 ? activity.getResources().getString(R.string.ttl_new_payee) : activity.getResources().getString(R.string.ttl_edit_payee);

        new AlertDialog.Builder(activity).setTitle(title)
                .setView(input)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
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
                })
                .show();

        input.requestFocus();
    }

    public static void ShowSelectPayeeDialog(Activity activity, final OnSelectPayeeListener selectPayeeListener) {
        List<Payee> payees = PayeesDAO.getInstance(activity).getAllModels();
        ArrayAdapter<Payee> arrayAdapter = new ArrayAdapter<>(activity, android.R.layout.simple_list_item_1, payees);

        new AlertDialog.Builder(activity)
                .setTitle(activity.getResources().getString(R.string.ttl_select_payee))
                .setNegativeButton(
                    activity.getResources().getString(android.R.string.cancel),
                    (dialog, which) -> dialog.dismiss())
                .setAdapter(arrayAdapter, (dialog, which) -> {
                    ListView listView = ((AlertDialog) dialog).getListView();
                    Payee payee = (Payee) listView.getAdapter().getItem(which);
                    selectPayeeListener.OnSelectPayee(payee);
                })
                .show();
    }

    public static long checkPayeeAndCreateIfNecessary(long payeeID, String text, Context context) throws Exception {
        String fullName = PayeesDAO.getInstance(context).getPayeeByID(payeeID).getFullName();
        if (text.isEmpty()) {
            return -1;
        } else if (fullName.toLowerCase().equals(text.toLowerCase())) {
            return payeeID;
        } else {
            BaseNode rootTree = AbstractModelManager.createModelWithFullName(text, IAbstractModel.MODEL_TYPE_PAYEE, context);
            List<BaseNode> payeesTree = rootTree.getFlatChildrenList();
            int treeSize = payeesTree.size();
            if (treeSize == 0 && rootTree.getModel().getID() != -1) {
                return rootTree.getModel().getID();
            }
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
