package com.yoshione.fingen.fts.models.tickets;

public class TicketQuery {
    private String date;
    private String documentId;
    private String fiscalSign;
    private String fsId;
    private String operationType;
    private String sum;

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public String getFiscalSign() {
        return fiscalSign;
    }

    public void setFiscalSign(String fiscalSign) {
        this.fiscalSign = fiscalSign;
    }

    public String getFsId() {
        return fsId;
    }

    public void setFsId(String fsId) {
        this.fsId = fsId;
    }

    public String getOperationType() {
        return operationType;
    }

    public void setOperationType(String operationType) {
        this.operationType = operationType;
    }

    public String getSum() {
        return sum;
    }

    public void setSum(String sum) {
        this.sum = sum;
    }

    @Override
    public String toString() {
        return "TicketQuery{" +
                "date='" + date + '\'' +
                ", documentId='" + documentId + '\'' +
                ", fiscalSign='" + fiscalSign + '\'' +
                ", fsId='" + fsId + '\'' +
                ", operationType='" + operationType + '\'' +
                ", sum='" + sum + '\'' +
                '}';
    }
}
