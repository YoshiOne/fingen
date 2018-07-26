package com.yoshione.fingen.managers;

import com.yoshione.fingen.model.Transaction;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class TransferManager {
//    public static final int EXRATE_DIRECTION_1SRC_XDEST = 0;
//    public static final int EXRATE_DIRECTION_1DEST_XSRC = 1;

    public static BigDecimal getDestAmount(Transaction transaction) {
        if (transaction.getExchangeRate().compareTo(BigDecimal.ONE) == 0) {
            return transaction.getAmount().abs();
        } else {
            return transaction.getAmount().multiply(transaction.getExchangeRate()).abs();
        }
    }

    public static BigDecimal getExRate(Transaction transaction, BigDecimal destAmount) {
        if (transaction.getAmount().compareTo(BigDecimal.ZERO) != 0 && transaction.getAccountID() != transaction.getDestAccountID()) {
            BigDecimal untrim = destAmount.divide(transaction.getAmount(), 15, BigDecimal.ROUND_HALF_EVEN);
            return trimExRate(untrim, transaction.getAmount(), destAmount);
        } else {
            return BigDecimal.ONE;
        }
    }


    private static BigDecimal trimExRate(BigDecimal untrim, BigDecimal src, BigDecimal dst) {
        BigDecimal cur;
        BigDecimal prev = new BigDecimal(String.valueOf(untrim.doubleValue()));
        BigDecimal trim = new BigDecimal(String.valueOf(untrim.doubleValue()));
        int scale = untrim.scale();
        while (scale >= 0) {
            trim = untrim.setScale(scale, RoundingMode.HALF_EVEN);
            cur = src.multiply(trim).setScale(2, RoundingMode.HALF_EVEN);
            if (cur.compareTo(dst) != 0) {
                trim = prev;
                break;
            }
//            prev = new BigDecimal(trim.doubleValue());
            prev = trim;
            scale--;
        }
        return trim;
    }
}

