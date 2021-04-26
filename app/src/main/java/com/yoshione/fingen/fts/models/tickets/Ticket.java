package com.yoshione.fingen.fts.models.tickets;

public class Ticket {
    private String id;
    private String qr;
    private Seller seller;
    private TicketQuery query;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getQr() {
        return qr;
    }

    public void setQr(String qr) {
        this.qr = qr;
    }

    public Seller getSeller() {
        return seller;
    }

    public void setSeller(Seller seller) {
        this.seller = seller;
    }

    public TicketQuery getQuery() {
        return query;
    }

    public void setQuery(TicketQuery query) {
        this.query = query;
    }

    @Override
    public String toString() {
        return "Ticket{" +
                "id='" + id + '\'' +
                ", qr='" + qr + '\'' +
                ", seller=" + seller +
                ", query=" + query +
                '}';
    }
}
