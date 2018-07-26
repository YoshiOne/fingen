package com.yoshione.fingen.widgets;

import java.math.BigDecimal;

public interface OnAmountChangeListener {
    void OnAmountChange(BigDecimal newAmount, int newType);
}
