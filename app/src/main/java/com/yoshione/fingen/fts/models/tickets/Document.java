
package com.yoshione.fingen.fts.models.tickets;

public class Document {
    private Receipt receipt;

    public Receipt getReceipt() {
        return receipt;
    }

    public void setReceipt(Receipt receipt) {
        this.receipt = receipt;
    }

    @Override
    public String toString() {
        return "Document{" +
                "receipt=" + receipt +
                '}';
    }
}
