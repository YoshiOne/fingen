package com.yoshione.fingen.classes;

import java.math.BigDecimal;

/**
 * Created by slv on 07.03.2016.
 *
 */
public class SumsByCabbage {
    private long mCabbageId = -1;
    private BigDecimal inTrSum;
    private BigDecimal outTrSum;
    private BigDecimal inPlan;
    private BigDecimal outPlan;
    private BigDecimal startBalance;

    public boolean isEmpty(boolean isAddStartBalance) {
        boolean isEmpty = inPlan.compareTo(BigDecimal.ZERO) == 0 &
                outPlan.compareTo(BigDecimal.ZERO) == 0 &
                inTrSum.compareTo(BigDecimal.ZERO) == 0 &
                outTrSum.compareTo(BigDecimal.ZERO) == 0;
        if (isAddStartBalance) {
            isEmpty = startBalance.compareTo(BigDecimal.ZERO) == 0 & isEmpty;
        }
        return isEmpty;
    }

    public BigDecimal getStartBalance() {
        return startBalance;
    }

    public void setStartBalance(BigDecimal startBalance) {
        this.startBalance = startBalance;
    }

    public BigDecimal getInPlan() {
        return inPlan;
    }

    public void setInPlan(BigDecimal inPlan) {
        this.inPlan = inPlan;
    }

    public BigDecimal getOutPlan() {
        return outPlan;
    }

    public void setOutPlan(BigDecimal outPlan) {
        this.outPlan = outPlan;
    }

    public long getCabbageId() {
        return mCabbageId;
    }

    public BigDecimal getInTrSum() {
        return inTrSum;
    }

    public void setInTrSum(BigDecimal inTrSum) {
        this.inTrSum = inTrSum;
    }

    public BigDecimal getOutTrSum() {
        return outTrSum;
    }

    public void setOutTrSum(BigDecimal outTrSum) {
        this.outTrSum = outTrSum;
    }

    public SumsByCabbage(long mCabbageId, BigDecimal inTrSum, BigDecimal outTrSum) {
        this.mCabbageId = mCabbageId;
        this.inTrSum = inTrSum;
        this.outTrSum = outTrSum;
        inPlan = BigDecimal.ZERO;
        outPlan = BigDecimal.ZERO;
        startBalance = BigDecimal.ZERO;
    }
}
