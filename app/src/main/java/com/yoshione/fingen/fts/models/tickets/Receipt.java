
package com.yoshione.fingen.fts.models.tickets;

import java.util.List;

public class Receipt {

    private String user;
    private String userInn;
    private String operator;
    private Long totalSum;
    private Long cashTotalSum;
    private Long ecashTotalSum;
    private List<Item> items;

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getUserInn() {
        return userInn;
    }

    public void setUserInn(String userInn) {
        this.userInn = userInn;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public Long getTotalSum() {
        return totalSum;
    }

    public void setTotalSum(Long totalSum) {
        this.totalSum = totalSum;
    }

    public Long getCashTotalSum() {
        return cashTotalSum;
    }

    public void setCashTotalSum(Long cashTotalSum) {
        this.cashTotalSum = cashTotalSum;
    }

    public Long getEcashTotalSum() {
        return ecashTotalSum;
    }

    public void setEcashTotalSum(Long ecashTotalSum) {
        this.ecashTotalSum = ecashTotalSum;
    }

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

    @Override
    public String toString() {
        return "Receipt{" +
                "user='" + user + '\'' +
                ", userInn='" + userInn + '\'' +
                ", operator='" + operator + '\'' +
                ", totalSum=" + totalSum +
                ", cashTotalSum=" + cashTotalSum +
                ", ecashTotalSum=" + ecashTotalSum +
                ", items=" + items +
                '}';
    }
}
