package com.yoshione.fingen.fts.models.tickets;

public class TicketQrCodeRequest {
    private String qr;

    public TicketQrCodeRequest(String qr) {
        this.qr = qr;
    }

    public String getQr() {
        return qr;
    }

    public void setQr(String qr) {
        this.qr = qr;
    }

    @Override
    public String toString() {
        return "TicketQrCodeRequest{" +
                "qr='" + qr + '\'' +
                '}';
    }
}
