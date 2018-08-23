package com.yoshione.fingen.interfaces;

import com.yoshione.fingen.model.Transaction;

public interface ITransactionItemEventListener {
    void onTransactionItemClick(Transaction transaction);

    void onSelectionChange(int selectedCount);
}
