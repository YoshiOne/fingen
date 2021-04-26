package com.yoshione.fingen.managers;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.yoshione.fingen.R;
import com.yoshione.fingen.dao.AbstractDAO;
import com.yoshione.fingen.dao.BaseDAO;
import com.yoshione.fingen.dao.CategoriesDAO;
import com.yoshione.fingen.dao.DepartmentsDAO;
import com.yoshione.fingen.dao.ProjectsDAO;
import com.yoshione.fingen.interfaces.IAbstractModel;
import com.yoshione.fingen.model.BaseModel;
import com.yoshione.fingen.model.Category;
import com.yoshione.fingen.model.Department;
import com.yoshione.fingen.model.Project;
import com.yoshione.fingen.utils.BaseNode;

import java.util.ArrayList;
import java.util.List;

public class AbstractModelManager {

    public static void editNestedModelNameViaDialog(final IAbstractModel model, final Activity activity, final EditDialogEventsListener eventsListener) {
        final EditText input = (EditText) activity.getLayoutInflater().inflate(R.layout.template_edittext, null);
        input.setText(model.getName());

        new AlertDialog.Builder(activity)
                .setTitle(R.string.ttl_enter_new_name)
                .setView(input)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    String text = input.getText().toString();
                    if (text.length() > 0) {
                        model.setName(text);
                        int modelType = (model).getModelType();
                        try {
                            if (modelType == IAbstractModel.MODEL_TYPE_CATEGORY) {
                                CategoriesDAO.getInstance(activity).createCategory((Category) model, activity);
                            } else if (modelType == IAbstractModel.MODEL_TYPE_PROJECT) {
                                ProjectsDAO.getInstance(activity).createProject((Project) model, activity);
                        } else if (modelType == IAbstractModel.MODEL_TYPE_DEPARTMENT) {
                            DepartmentsDAO.getInstance(activity).createDepartment((Department) model, activity);
                            } else {
                                AbstractDAO dao = BaseDAO.getDAO(modelType, activity);
                                if (dao != null) {
                                    dao.createModel(model);
                                }
                            }
                        } catch (Exception e) {
                            Toast.makeText(activity, R.string.msg_error_on_write_to_db, Toast.LENGTH_SHORT).show();
                        }
                        if (eventsListener != null) {
                            eventsListener.OnOkClick(model);
                        }
                    }
                })
                .setNegativeButton(activity.getString(android.R.string.cancel), (dialog, which) -> dialog.cancel())
                .show();

        input.requestFocus();
    }

    public interface EditDialogEventsListener {
        void OnOkClick(IAbstractModel model);
    }

    public static void ShowSelectNestedModelDialog(String title,
                                                   final Activity activity,
                                                   BaseNode tree,
                                                   final BaseNode excludeNode,
                                                   final EditDialogEventsListener eventsListener) {
        final List<BaseNode> nodes = tree.getFlatChildrenListWOChildrenOfGivenOne(excludeNode);

        new AlertDialog.Builder(activity).setTitle(title)
                .setNegativeButton(activity.getResources().getString(android.R.string.cancel),
                        (dialog, which) -> dialog.dismiss())
                .setAdapter(createTreeArrayAdapter(activity, nodes), (dialog, which) -> {
                    if (eventsListener != null) {
                        eventsListener.OnOkClick(nodes.get(which).getModel());
                    }
                })
                .show();
    }

    private static ArrayAdapter<String> createTreeArrayAdapter(Activity activity, List<BaseNode> nodes) {
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(activity, android.R.layout.simple_list_item_1);
        for (BaseNode node : nodes) {
            StringBuilder prefix = new StringBuilder();
            for (int i = 0; i < node.getLevel() - 2; i++)
                prefix.append("\t\t");
            if (prefix.length() > 0)
                prefix.append(node.isLastChildren() ? "└ " : "├ ");
            arrayAdapter.add(prefix + node.getModel().getName());
        }
        return arrayAdapter;
    }

    @SuppressWarnings("unchecked")
    static BaseNode createModelWithFullName(String fullName, int modelType, Context context) {
        BaseNode tree;
        AbstractDAO dao = BaseDAO.getDAO(modelType, context);
        if (dao == null) return new BaseNode(BaseModel.createModelByType(modelType), null);
        try {
            tree = TreeManager.convertListToTree((List<IAbstractModel>) dao.getAllModels(), modelType);
        } catch (Exception e) {
            tree = new BaseNode(BaseModel.createModelByType(modelType), null);
        }

        return tree.createTreeByFullName(fullName);
    }

    @SuppressWarnings("unchecked")
    public static List<IAbstractModel> getAllChildren(IAbstractModel parent, Context context) {
        AbstractDAO dao = BaseDAO.getDAO(parent.getModelType(), context);
        if (dao == null)
            return new ArrayList<>();

        BaseNode root = new BaseNode(parent, null);
        List<IAbstractModel> models = dao.getAllModels();
        List<Long> processedIds = new ArrayList<>();

        int lastIdCount = 0;
        while (models.size() != processedIds.size()) {
            for (IAbstractModel model : models) {
                if (processedIds.indexOf(model.getID()) < 0) {
                    if (root.addChild(model) != null) {
                        processedIds.add(model.getID());
                    }
                }
            }
            if (processedIds.size() == lastIdCount) {
                break;
            }
            lastIdCount = processedIds.size();
        }

        List<IAbstractModel> children = new ArrayList<>();
        for (BaseNode node : root.getFlatChildrenList()) {
            children.add(node.getModel());
        }

        return children;
    }
}
