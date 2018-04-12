package com.yoshione.fingen.interfaces;

import com.yoshione.fingen.classes.ListSumsByCabbage;
import com.yoshione.fingen.utils.UpdateMainListsRwHandler;

/**
 * Created by slv on 08.11.2016.
 */
public interface IUpdateMainListsEvents {
    void loadData(UpdateMainListsRwHandler handler, long itemID);

    void loadSums(UpdateMainListsRwHandler handler);

    void updateLists(long itemID);

    void updateSums(ListSumsByCabbage listSumsByCabbage);
}
