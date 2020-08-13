package com.yoshione.fingen.fts.models.tickets;

public class Seller {
    private String inn;
    private String logo;
    private String name;

    public String getInn() {
        return inn;
    }

    public void setInn(String inn) {
        this.inn = inn;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Seller{" +
                "inn='" + inn + '\'' +
                ", logo='" + logo + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
