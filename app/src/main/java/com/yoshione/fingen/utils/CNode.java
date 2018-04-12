package com.yoshione.fingen.utils;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.yoshione.fingen.R;
import com.yoshione.fingen.adapter.AdapterBudget;
import com.yoshione.fingen.classes.ListSumsByCabbage;
import com.yoshione.fingen.dao.BudgetDAO;
import com.yoshione.fingen.dao.CreditsDAO;
import com.yoshione.fingen.model.BudgetForCategory;
import com.yoshione.fingen.model.Category;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by slv on 25.12.2015.
 * a
 */

public class CNode implements Comparable<CNode> {
    private final Category mCategory;
    private BigDecimal mIncome;
    private BigDecimal mExpense;
    private CNode mParent;
    private final List<CNode> mChildren;

    public CNode(Category mCategory, CNode mParent) {
        this.mCategory = mCategory;
        this.mParent = mParent;
        mChildren = new ArrayList<>();
        mIncome = new BigDecimal(BigInteger.ZERO);
        mExpense = new BigDecimal(BigInteger.ZERO);
    }

    public BigDecimal getExpense() {
        return mExpense;
    }

    public void setExpense(BigDecimal expense) {
        mExpense = expense;
    }

    public BigDecimal getIncome() {
        return mIncome;
    }

    public void setIncome(BigDecimal income) {
        mIncome = income;
    }

    public CNode getmParent() {
        return mParent;
    }

    public List<CNode> getmChildren() {
        return mChildren;
    }

    public void clear() {
        mChildren.clear();
    }

    public Category getmCategory() {
        return mCategory;
    }

    public CNode insertChild(int location, Category category) {
        CNode newNode = new CNode(category, this);
        mChildren.add(location, newNode);
        return newNode;
    }

    public CNode addChild(Category category) {
        CNode newNode;
        //если id вставляемой категории = id категории текущего узла - добавляем в чилдрены
        if (mCategory != null) {
            if (category.getParentID() == mCategory.getID()) {
                newNode = new CNode(category, this);
                mChildren.add(newNode);
                return newNode;
            }
        }

        //если id вставляемой категории = id категории родительского узла - добавляем в чилдрены к родителю
        if (mParent != null) {
            if (mParent.getmCategory() != null) {
                if (category.getParentID() == mParent.getmCategory().getID()) {
                    newNode = new CNode(category, this);
                    mParent.mChildren.add(newNode);
                    return newNode;
                }
            }
        }

        //если категория все еще не вставлена, запускаем рекурсивный обход всех чилдренов и пытаемся вставить в них
        for (CNode node : mChildren) {
            newNode = node.addChild(category);
            if (newNode != null) {
                return newNode;
            }
        }

        return null;
    }

    public int getLevel() {
        int level = 1;
        if (mParent != null) {
            level += mParent.getLevel();
        }
        return level;
    }

    public boolean isHasChildren() {
        return getFlatChildrenList().size() > 0;
    }

    public int getNumberOfChildren() {
        return getFlatChildrenList().size();
    }

    public List<CNode> getFlatChildrenList() {
        List<CNode> list = new ArrayList<>();
        try {
            for (CNode child : this.mChildren) {
                list.add(child);
                if (child.mCategory.isExpanded()) {
                    list.addAll(child.getFlatChildrenList());
                }
            }
        } catch (Exception e) {
            Log.d("CNODE", "ConcurrentModificationException");
        }
        return list;
    }

    public
    @Nullable
    CNode getChildrenAtFlatPos(int position) {
        List<CNode> flatChildrenList = this.getFlatChildrenList();
        if (position >= 0 && position < flatChildrenList.size()) {
            return flatChildrenList.get(position);
        } else {
            return null;
        }
    }

    public boolean equals(CNode node) {
        return node.getmCategory() != null && mCategory.getID() == node.getmCategory().getID();
    }

    public CNode getNodeById(long id) {
        CNode result = null;
        for (CNode node : getFlatChildrenList()) {
            if (node.getmCategory().getID() == id) {
                result = node;
            }
        }
        return result;
    }

    public CNode removeEmptySums() {
        int count;
        do {
            count = getNumberOfChildren();
            for (CNode node : getFlatChildrenList()) {
                if (!node.isHasChildren() & node.getmCategory().getBudget().getmSums().isEmpty()) {
                    node.getmParent().mChildren.remove(node);
                }
            }
        }
        while (count != getNumberOfChildren());
        return this;
    }

    public void deleteBudget(Context context, int year, int month, long cabbageId) {
        ListSumsByCabbage listSumsByCabbage = mCategory.getBudget().getmSums();
        for (int i = 0; i < listSumsByCabbage.getmList().size(); i++) {
            if (listSumsByCabbage.getmList().get(i).getCabbageId() == cabbageId) {
                listSumsByCabbage.getmList().remove(i);
                break;
            }
        }

        BudgetDAO.getInstance(context).deleteBudget(year, month, mCategory.getID(), cabbageId);
    }

    public CNode addDebtsCategories(Context context, int year, int month) {
        Category rootCategory = new Category(AdapterBudget.BUDGET_ITEM_DEBTS_ROOT,
                context.getString(R.string.ent_credit), new Category(), -1, true);
        rootCategory.setBudget(new BudgetForCategory(new ListSumsByCabbage(), AdapterBudget.INFO_TYPE_ALL_TOTAL));
        CNode root = addChild(rootCategory);

        List<Category> creditCategories = CreditsDAO.getInstance(context).getDebtsAsCategoriesWithPlanFact(year, month, context);
        for (Category category : creditCategories) {
            root.addChild(category);
        }
        return this;
    }

    public void sort() {
        Collections.sort(mChildren);
        for (CNode node : mChildren) {
            node.sort();
        }
    }

    @Override
    public int compareTo(@NonNull CNode another) {
        return mCategory.compareTo(another.getmCategory());
    }
}