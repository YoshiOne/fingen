
package com.yoshione.fingen.fts.models;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Receipt {

    @SerializedName("operationType")
    @Expose
    private long operationType;
    @SerializedName("user")
    @Expose
    private String user;
    @SerializedName("kktRegId")
    @Expose
    private String kktRegId;
    @SerializedName("userInn")
    @Expose
    private String userInn;
    @SerializedName("cashTotalSum")
    @Expose
    private long cashTotalSum;
    @SerializedName("taxationType")
    @Expose
    private long taxationType;
    @SerializedName("dateTime")
    @Expose
    private String dateTime;
    @SerializedName("rawData")
    @Expose
    private String rawData;
    @SerializedName("totalSum")
    @Expose
    private long totalSum;
    @SerializedName("shiftNumber")
    @Expose
    private long shiftNumber;
    @SerializedName("stornoItems")
    @Expose
    private List<Object> stornoItems = null;
    @SerializedName("items")
    @Expose
    private List<Item> items = null;
    @SerializedName("ecashTotalSum")
    @Expose
    private long ecashTotalSum;
    @SerializedName("fiscalDriveNumber")
    @Expose
    private String fiscalDriveNumber;
    @SerializedName("fiscalDocumentNumber")
    @Expose
    private long fiscalDocumentNumber;
    @SerializedName("fiscalSign")
    @Expose
    private long fiscalSign;
    @SerializedName("nds18")
    @Expose
    private long nds18;
    @SerializedName("requestNumber")
    @Expose
    private long requestNumber;
    @SerializedName("retailPlaceAddress")
    @Expose
    private String retailPlaceAddress;
    @SerializedName("operator")
    @Expose
    private String operator;
    @SerializedName("nds10")
    @Expose
    private long nds10;
    @SerializedName("receiptCode")
    @Expose
    private long receiptCode;
    @SerializedName("modifiers")
    @Expose
    private List<Object> modifiers = null;

    public long getOperationType() {
        return operationType;
    }

    public void setOperationType(long operationType) {
        this.operationType = operationType;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getKktRegId() {
        return kktRegId;
    }

    public void setKktRegId(String kktRegId) {
        this.kktRegId = kktRegId;
    }

    public String getUserInn() {
        return userInn;
    }

    public void setUserInn(String userInn) {
        this.userInn = userInn;
    }

    public long getCashTotalSum() {
        return cashTotalSum;
    }

    public void setCashTotalSum(long cashTotalSum) {
        this.cashTotalSum = cashTotalSum;
    }

    public long getTaxationType() {
        return taxationType;
    }

    public void setTaxationType(long taxationType) {
        this.taxationType = taxationType;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public String getRawData() {
        return rawData;
    }

    public void setRawData(String rawData) {
        this.rawData = rawData;
    }

    public long getTotalSum() {
        return totalSum;
    }

    public void setTotalSum(long totalSum) {
        this.totalSum = totalSum;
    }

    public long getShiftNumber() {
        return shiftNumber;
    }

    public void setShiftNumber(long shiftNumber) {
        this.shiftNumber = shiftNumber;
    }

    public List<Object> getStornoItems() {
        return stornoItems;
    }

    public void setStornoItems(List<Object> stornoItems) {
        this.stornoItems = stornoItems;
    }

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

    public long getEcashTotalSum() {
        return ecashTotalSum;
    }

    public void setEcashTotalSum(long ecashTotalSum) {
        this.ecashTotalSum = ecashTotalSum;
    }

    public String getFiscalDriveNumber() {
        return fiscalDriveNumber;
    }

    public void setFiscalDriveNumber(String fiscalDriveNumber) {
        this.fiscalDriveNumber = fiscalDriveNumber;
    }

    public long getFiscalDocumentNumber() {
        return fiscalDocumentNumber;
    }

    public void setFiscalDocumentNumber(long fiscalDocumentNumber) {
        this.fiscalDocumentNumber = fiscalDocumentNumber;
    }

    public long getFiscalSign() {
        return fiscalSign;
    }

    public void setFiscalSign(long fiscalSign) {
        this.fiscalSign = fiscalSign;
    }

    public long getNds18() {
        return nds18;
    }

    public void setNds18(long nds18) {
        this.nds18 = nds18;
    }

    public long getRequestNumber() {
        return requestNumber;
    }

    public void setRequestNumber(long requestNumber) {
        this.requestNumber = requestNumber;
    }

    public String getRetailPlaceAddress() {
        return retailPlaceAddress;
    }

    public void setRetailPlaceAddress(String retailPlaceAddress) {
        this.retailPlaceAddress = retailPlaceAddress;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public long getNds10() {
        return nds10;
    }

    public void setNds10(long nds10) {
        this.nds10 = nds10;
    }

    public long getReceiptCode() {
        return receiptCode;
    }

    public void setReceiptCode(long receiptCode) {
        this.receiptCode = receiptCode;
    }

    public List<Object> getModifiers() {
        return modifiers;
    }

    public void setModifiers(List<Object> modifiers) {
        this.modifiers = modifiers;
    }

}
