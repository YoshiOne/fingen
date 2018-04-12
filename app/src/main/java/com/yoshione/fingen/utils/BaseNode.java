package com.yoshione.fingen.utils;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Pair;

import com.yoshione.fingen.interfaces.IAbstractNode;
import com.yoshione.fingen.interfaces.IAbstractModel;
import com.yoshione.fingen.model.BaseModel;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by slv on 25.10.2016.
 * a
 */

public class BaseNode implements IAbstractNode, Comparable<BaseNode> {
    private final List<BaseNode> mChildren;
    private IAbstractModel mModel;
    private BaseNode mParent;
    private BigDecimal mIncome;
    private BigDecimal mExpense;

    public BaseNode(IAbstractModel model, BaseNode parent) {
        mModel = model;
        mParent = parent;
        mChildren = new ArrayList<>();
    }

    public IAbstractModel getModel() {
        return mModel;
    }

    public void setModel(IAbstractModel model) {
        mModel = model;
    }

    public BaseNode getParent() {
        return mParent;
    }

    public List<BaseNode> getChildren() {
        return mChildren;
    }

    public BigDecimal getIncome() {
        return mIncome;
    }

    public void setIncome(BigDecimal income) {
        mIncome = income;
    }

    public BigDecimal getExpense() {
        return mExpense;
    }

    public void setExpense(BigDecimal expense) {
        mExpense = expense;
    }

    public BaseNode addChild(IAbstractModel model) {
        BaseNode newNode;
        //если id вставляемой категории = id категории текущего узла - добавляем в чилдрены
        if (model != null) {
            if (model.getParentID() == getModel().getID()) {
                newNode = new BaseNode(model, this);
//                if (model.getParentID() < 0) {
//                    newNode.setFullName(model.getName());
//                } else {
//                    newNode.setFullName(mFullName +"\\" + model.getName());
//                }
                mChildren.add(newNode);
                return newNode;
            }

            //если id вставляемой категории = id категории родительского узла - добавляем в чилдрены к родителю
            if (mParent != null) {
                if (mParent.getModel() != null) {
                    if (model.getParentID() == mParent.getModel().getID()) {
                        newNode = new BaseNode(model, this);
//                        if (model.getParentID() < 0) {
//                            newNode.setFullName(model.getName());
//                        } else {
//                            newNode.setFullName(mParent.getFullName() +"\\" + model.getName());
//                        }
                        mParent.mChildren.add(newNode);
                        return newNode;
                    }
                }
            }

            //если категория все еще не вставлена, запускаем рекурсивный обход всех чилдренов и пытаемся вставить в них
            for (BaseNode node : mChildren) {
                newNode = node.addChild(model);
                if (newNode != null) {
                    return newNode;
                }
            }
        }

        return null;
    }

    private boolean isParentOf(BaseNode possibleChild) {
        for (BaseNode node : getFlatChildrenList()) {
            if (node.getModel().getID() == possibleChild.getModel().getID()) {
                return true;
            }
        }
        return false;
    }

    public List<BaseNode> getFlatChildrenList() {
        List<BaseNode> list = new ArrayList<>();
        try {
            for (BaseNode child : this.mChildren) {
                list.add(child);
                if (child.getModel().isExpanded()) {
                    list.addAll(child.getFlatChildrenList());
                }
            }
        } catch (Exception e) {

        }
        return list;
    }

    public int getLevel() {
        int level = 1;
        if (mParent != null) {
            level += mParent.getLevel();
        }
        return level;
    }

    public boolean isLastChildren() {
        return mParent != null && mParent.mChildren.indexOf(this) == mParent.mChildren.size() - 1;
    }

    public
    @Nullable
    BaseNode getChildrenAtFlatPos(int position) {
        List<BaseNode> flatChildrenList = this.getFlatChildrenList();
        if (position >= 0 && position < flatChildrenList.size()) {
            return flatChildrenList.get(position);
        } else {
            return null;
        }
    }

    public int getNumberOfChildren() {
        return getFlatChildrenList().size();
    }

    public List<Pair<Long, Integer>> getOrderList() {
        List<Pair<Long, Integer>> pairs = new ArrayList<>();

        int ind = 0;
        for (BaseNode node : getFlatChildrenList()) {
            pairs.add(new Pair<>(node.getModel().getID(), ind));
            ind++;
        }

        return pairs;
    }

    public int getFlatPos(BaseNode node) {
        return this.getFlatChildrenList().indexOf(node);
    }

    public IAbstractModel moveItemFromToFlatPos(int from, int to) {
        if (from == to) return null;
        BaseNode node = getChildrenAtFlatPos(from);
        if (node == null) return null;
        BaseNode firstParent = node.mParent;
        BaseNode toNode = getChildrenAtFlatPos(to);
        if (toNode == null) return null;
        BaseNode toParent = toNode.mParent;

        if (node.isParentOf(toNode)) {
            return null;
        }

        firstParent.mChildren.remove(node);
        int toRelativeIndex = toParent.mChildren.indexOf(toNode);
        if (to > from) {
            toRelativeIndex++;
        }
        toParent.mChildren.add(toRelativeIndex, node);
        node.mParent = toParent;
        long parentId = (toNode.mParent.getModel() == null) ? -1 : toNode.mParent.getModel().getID();
        node.getModel().setParentID(parentId);
        return node.getModel();
    }

    public BaseNode getNodeById(long id) throws Exception {
        for (BaseNode node : getFlatChildrenList()) {
            if (node.getModel().getID() == id) {
                return node;
            }
        }
        throw new Exception();
    }

    private void filterFromChildToParent(String filter) {

        if (mChildren.size() == 0) {
            if (!getModel().getName().toLowerCase().contains(filter.toLowerCase())) {
                mParent.mChildren.remove(this);
            }
        }

        if (mParent == null) return;

        if (mParent.getModel() != null) {
            mParent.filterFromChildToParent(filter);
        }
    }

    public void applyFilter(String filter) {
        boolean clear = true;
        for (BaseNode node : getFlatChildrenList()) {
            if (node.getModel().getName().toLowerCase().contains(filter.toLowerCase())) {
                clear = false;
                break;
            }
        }
        if (clear) {
            clear();
        } else {
            for (BaseNode node : getFlatChildrenList()) {
                if (node.mChildren.size() == 0) {
                    node.filterFromChildToParent(filter);
                }
            }
        }
    }

    public void clear() {
        mChildren.clear();
    }

    public List<BaseNode> getFlatChildrenListWOChildrenOfGivenOne(BaseNode node) {
        List<BaseNode> list = new ArrayList<>();
        if (node != null) {
            for (BaseNode child : this.mChildren) {
                if (!child.equals(node) & !node.isParentOf(child)) {
                    list.add(child);
                    if (child.getModel().isExpanded()) {
                        list.addAll(child.getFlatChildrenListWOChildrenOfGivenOne(node));
                    }

                }
            }
        } else {
            list = getFlatChildrenList();
        }
        return list;
    }

    public void sort() {
        Collections.sort(mChildren);
        for (BaseNode node : mChildren) {
            node.sort();
        }
    }

    @Override
    public int compareTo(@NonNull BaseNode baseNode) {
        return mModel.compareTo(baseNode.getModel());
    }

    private
    @Nullable
    BaseNode getChildByModelName(String name) {
        for (BaseNode node : mChildren) {
            if (node.getModel().getName().toLowerCase().equals(name.toLowerCase())) {
                return node;
            }
        }
        return null;
    }

    public BaseNode getChildByFullName(String fullName) {
        if (fullName.isEmpty()) {
            return null;
        }
        String path[] = fullName.split("\\\\");
        return getChildByPath(this, Arrays.asList(path));
    }

    private BaseNode getChildByPath(BaseNode currentNode, List<String> path) {
        if (path.isEmpty() && currentNode.mChildren.isEmpty()) {
            return currentNode;
        } else if (path.isEmpty() && !currentNode.getChildren().isEmpty()) {
            return currentNode;
        } else {
            for (BaseNode node : currentNode.getChildren()) {
                if (node.getModel().getName().toLowerCase().equals(path.get(0).toLowerCase())) {
                    List<String> newPath = new ArrayList<>();
                    newPath.addAll(path);
                    newPath.remove(0);
                    return getChildByPath(node, newPath);
                }
            }
        }
        return new BaseNode(BaseModel.createModelByType(getModel().getModelType()), null);
    }

    public
    @Nullable
    BaseNode createTreeByFullName(String fullName) {
        if (fullName.isEmpty()) {
            return null;
        }
        String path[] = fullName.split("\\\\");
        IAbstractModel rootModel = BaseModel.createModelByType(getModel().getModelType());
        return createTreeByPath(this, new BaseNode(rootModel, null) , Arrays.asList(path));
    }

    private BaseNode createTreeByPath(BaseNode currentNode, BaseNode newTree, List<String> path) {
        if (path.isEmpty() && currentNode.mChildren.isEmpty()) {
            return currentNode;
        } else if (path.isEmpty() && !currentNode.getChildren().isEmpty()) {
            return currentNode;
        } else {
            for (BaseNode node : currentNode.getChildren()) {
                if (node.getModel().getName().toLowerCase().equals(path.get(0).toLowerCase())) {
                    newTree.addChildToDeepestChild(node.getModel());
                    List<String> newPath = new ArrayList<>();
                    newPath.addAll(path);
                    newPath.remove(0);
                    return createTreeByPath(node, newTree, newPath);
                }
            }
            IAbstractModel model = BaseModel.createModelByType(getModel().getModelType());
            model.setName(path.get(0));
            model.setParentID(newTree.getModel().getID());
            newTree.addChildToDeepestChild(model);
            List<String> newPath = new ArrayList<>();
            newPath.addAll(path);
            newPath.remove(0);
            if (newPath.size() > 0) {
                return createTreeByPath(new BaseNode(BaseModel.createModelByType(getModel().getModelType()), null), newTree, newPath);
            } else {
                return newTree;
            }
        }
    }

    private void addChildToDeepestChild(IAbstractModel model) {
        BaseNode node;
        if (getFlatChildrenList().isEmpty()) {
            node = new BaseNode(model, this);
            mChildren.add(node);
        } else {
            int size = getFlatChildrenList().size();
            BaseNode deepestNode = getFlatChildrenList().get(size - 1);
            node = new BaseNode(model, deepestNode);
            deepestNode.getChildren().add(node);
        }
    }

    public ArrayList<Long> getSelectedModelsIDs() {
        ArrayList<Long> list = new ArrayList<>();
        for (BaseNode node : getFlatChildrenList()) {
            if (node.getModel().isSelected()) {
                list.add(node.getModel().getID());
            }
        }
        return list;
    }
}
