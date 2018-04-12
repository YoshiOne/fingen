package com.yoshione.fingen.managers;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Toast;

import com.yoshione.fingen.R;
import com.yoshione.fingen.dao.AbstractDAO;
import com.yoshione.fingen.dao.BaseDAO;
import com.yoshione.fingen.dao.CategoriesDAO;
import com.yoshione.fingen.dao.ProjectsDAO;
import com.yoshione.fingen.interfaces.IAbstractModel;
import com.yoshione.fingen.model.BaseModel;
import com.yoshione.fingen.model.Category;
import com.yoshione.fingen.model.Project;
import com.yoshione.fingen.utils.BaseNode;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Leonid on 25.10.2016.
 * a
 */

public class AbstractModelManager {

    public static void editNestedModelNameViaDialog(final IAbstractModel model, final Activity activity, final EditDialogEventsListener eventsListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(R.string.ttl_enter_new_name);
//        final EditText input = new EditText(activity, null, R.style.EditableEditText);
        final EditText input = new EditText(activity);
        input.setText(model.getName());
        input.setId(R.id.text);
        input.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        builder.setView(input);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String text = input.getText().toString();
                if (text.length() > 0) {
                    model.setName(text);
                    int modelType = (model).getModelType();
                    try {
                        if (modelType == IAbstractModel.MODEL_TYPE_CATEGORY) {
                            CategoriesDAO.getInstance(activity).createCategory((Category) model, activity);
                        } else if (modelType == IAbstractModel.MODEL_TYPE_PROJECT) {
                            ProjectsDAO.getInstance(activity).createProject((Project) model, activity);
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
            }
        });
        builder.setNegativeButton(activity.getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
        input.requestFocus();
    }

    public interface EditDialogEventsListener {
        void OnOkClick(IAbstractModel model);
    }

    @SuppressWarnings("unchecked")
    public static void ShowSelectNestedModelDialog(String title,
                                                   final Activity activity,
                                                   BaseNode tree,
                                                   final BaseNode excludeNode,
                                                   final EditDialogEventsListener eventsListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        final List<BaseNode> nodes = tree.getFlatChildrenListWOChildrenOfGivenOne(excludeNode);
        builder.setTitle(title)
                .setNegativeButton(activity.getResources().getString(android.R.string.cancel),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                .setAdapter(createTreeArrayAdapter(activity, nodes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (eventsListener != null) {
                            eventsListener.OnOkClick(nodes.get(which).getModel());
                        }
                    }
                })
                .show();
    }

    private static ArrayAdapter<String> createTreeArrayAdapter(Activity activity, List<BaseNode> nodes) {
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(activity, android.R.layout.simple_list_item_1);
        for (BaseNode node : nodes) {
            String prefix = "";
            for (int i = 0; i < node.getLevel() - 2; i++) {
                prefix = prefix + "\t\t";
            }
            if (!prefix.isEmpty()) {
                if (node.isLastChildren()) {
                    prefix = prefix + "└ ";
                } else {
                    prefix = prefix + "├ ";
                }
            }
            arrayAdapter.add(prefix + node.getModel().getName());
        }
        return arrayAdapter;
    }

    /*public static String getFullName(IAbstractModel model, Context context) {
        return getFullName(model.getID(), model.getModelType(), context);
    }

    @SuppressWarnings("unchecked")
    public static String getFullName(long modelID, int modelType, Context context) {
        BaseNode tree;
        AbstractDAO dao = BaseDAO.getDAO(modelType, context);
        if (dao == null) return "";
        try {
            tree = TreeManager.convertListToTree((List<IAbstractModel>) dao.getAllModels(), modelType);
        } catch (Exception e) {
            tree = new BaseNode(BaseModel.createModelByType(modelType), null);
        }
        return AbstractModelManager.getFullName(modelID, tree);
    }

    public static String getFullName(long modelID, BaseNode tree) {
        if (modelID < 0) return "";
        BaseNode node = null;
        try {
            node = tree.getNodeById(modelID);
        } catch (Exception e) {
            return "";
        }
        String name = node.getModel().getName();
        if (node.getParent() == null) {
            node.getParent();
            return name;
        }
        while (node.getParent().getModel().getID() >= 0) {
            node = node.getParent();
            name = node.getModel().getName() + "\\" + name;
        }
        return name;
    }*/

    /*@SuppressWarnings("unchecked")
    public static IAbstractModel getModelByFullName(String fullName, int modelType, AbstractDAO abstractDAO) {
        BaseNode tree;
        try {
            tree = TreeManager.convertListToTree((List<IAbstractModel>) abstractDAO.getAllModels(), modelType);
        } catch (Exception e) {
            tree = new BaseNode(BaseModel.createModelByType(modelType), null);
        }

        BaseNode node = tree.getChildByFullName(fullName);
        if (node != null) {
            return node.getModel();
        } else {
            return BaseModel.createModelByType(modelType);
        }
    }*/

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
        BaseNode root = new BaseNode(parent, null);
        List<IAbstractModel> models;
        AbstractDAO dao = BaseDAO.getDAO(parent.getModelType(), context);
        if (dao == null) return new ArrayList<>();
        try {
            models = (List<IAbstractModel>) dao.getAllModels();
        } catch (Exception e) {
            models = new ArrayList<>();
        }
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
