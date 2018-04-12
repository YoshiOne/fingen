/*
 * Copyright (c) 2015.
 */

package com.yoshione.fingen.managers;

import com.yoshione.fingen.classes.ListSumsByCabbage;
import com.yoshione.fingen.model.BudgetForCategory;
import com.yoshione.fingen.model.Category;
import com.yoshione.fingen.utils.CNode;

import java.util.HashSet;
import java.util.List;

/**
 * Created by Leonid on 22.11.2015.
 * a
 */
public class CategoryManager {


    public static CNode convertListToTree(List<Category> categoryList) {
        CNode tree = new CNode(new Category(), null);

        HashSet<Long> processedIds = new HashSet<>();

        int lastIdCount = 0;
        while (categoryList.size() != processedIds.size()) {
            for (Category category : categoryList) {
                if (!processedIds.contains(category.getID())) {
                    if (tree.addChild(category) != null) {
                        processedIds.add(category.getID());
                    }
                }
            }
            if (processedIds.size() == lastIdCount) {
                break;
            }
            lastIdCount = processedIds.size();
        }

        return tree;
    }

    public static void updatePlanAndFactForParents(CNode root) {
        BudgetForCategory budget = root.getmCategory().getBudget();
        for (CNode node : root.getmChildren()) {
            if (node.isHasChildren()) {
                node.getmCategory().getBudget().setmSums(new ListSumsByCabbage());
                updatePlanAndFactForParents(node);
            }
            if (budget != null) {
                budget.getmSums().appendSumsFact(node.getmCategory().getBudget().getmSums());
                budget.getmSums().appendSumsPlan(node.getmCategory().getBudget().getmSums());
            }
        }
    }
}
