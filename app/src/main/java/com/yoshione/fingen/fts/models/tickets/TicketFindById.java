package com.yoshione.fingen.fts.models.tickets;

public class TicketFindById {
    private Integer status;
    private String qr;
    private Seller seller;
    private TicketQuery query;
    private TicketData ticket;

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
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

    public TicketData getTicket() {
        return ticket;
    }

    public void setTicket(TicketData ticket) {
        this.ticket = ticket;
    }

    @Override
    public String toString() {
        return "TicketFindById{" +
                "status=" + status +
                ", qr='" + qr + '\'' +
                ", seller=" + seller +
                ", query=" + query +
                ", ticket=" + ticket +
                '}';
    }
}
