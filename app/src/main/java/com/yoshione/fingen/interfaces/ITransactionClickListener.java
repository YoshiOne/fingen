package com.yoshione.fingen.interfaces;

import com.yoshione.fingen.model.Transaction;

public interface ITransactionClickListener {
    void onSelectButtonClick();

    void onTransactionItemClick(Transaction transaction);
}
