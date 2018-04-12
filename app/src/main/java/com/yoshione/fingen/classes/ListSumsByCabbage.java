package com.yoshione.fingen.classes;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by slv on 22.03.2016.
 *
 */
public class ListSumsByCabbage {
    private final List<SumsByCabbage> mList;

    public ListSumsByCabbage() {
        mList = new ArrayList<>();
    }

    public ListSumsByCabbage(List<SumsByCabbage> mList) {
        this.mList = mList;
    }

    public List<SumsByCabbage> getmList() {
        return mList;
    }

    public SumsByCabbage getSumsByCabbageId(long cabbageId) {
        SumsByCabbage result = null;
        for (SumsByCabbage sumsByCabbage : mList) {
            if (sumsByCabbage.getCabbageId() == cabbageId) {
                result = sumsByCabbage;
            }
        }
        return result;
    }

    private void appendSumFact(SumsByCabbage input) {
        SumsByCabbage sumsByCabbage = getSumsByCabbageId(input.getCabbageId());
        if (sumsByCabbage == null) {
            mList.add(new SumsByCabbage(input.getCabbageId(), input.getInTrSum(), input.getOutTrSum()));
        } else {
            sumsByCabbage.setInTrSum(sumsByCabbage.getInTrSum().add(input.getInTrSum()));
            sumsByCabbage.setOutTrSum(sumsByCabbage.getOutTrSum().add(input.getOutTrSum()));
        } 
    }

    private void appendSumPlan(SumsByCabbage input) {
        SumsByCabbage sumsByCabbage = getSumsByCabbageId(input.getCabbageId());
        if (sumsByCabbage == null) {
            SumsByCabbage newSums = new SumsByCabbage(input.getCabbageId(), input.getInTrSum(), input.getOutTrSum());
            newSums.setInPlan(input.getInPlan());
            newSums.setOutPlan(input.getInPlan());
            mList.add(newSums);
        } else {
            sumsByCabbage.setInPlan(sumsByCabbage.getInPlan().add(input.getInPlan()));
            sumsByCabbage.setOutPlan(sumsByCabbage.getOutPlan().add(input.getOutPlan()));
        }
    }

    public void appendSumsFact(ListSumsByCabbage input) {
        for (SumsByCabbage sumsByCabbage : input.getmList()) {
            appendSumFact(sumsByCabbage);
        }
    }

    public void appendSumsPlan(ListSumsByCabbage input) {
        for (SumsByCabbage sumsByCabbage : input.getmList()) {
            appendSumPlan(sumsByCabbage);
        }
    }

    public int size() {
        return mList.size();
    }

    public SumsByCabbage get(int location) {
        return mList.get(location);
    }

    public boolean isEmpty() {
        boolean result = true;
        for (SumsByCabbage sum : mList) {
            result = result & (sum.getInPlan()
                    .add(sum.getOutPlan())
                    .add(sum.getInTrSum())
                    .add(sum.getOutTrSum())
                    .compareTo(BigDecimal.ZERO) == 0);
        }
        return result;
    }
}
