package com.yoshione.fingen.fts.models.tickets;

public class TicketData {
    private Document document;

    public Document getDocument() {
        return document;
    }

    public void setDocument(Document document) {
        this.document = document;
    }

    @Override
    public String toString() {
        return "TicketData{" +
                "document=" + document +
                '}';
    }
}
