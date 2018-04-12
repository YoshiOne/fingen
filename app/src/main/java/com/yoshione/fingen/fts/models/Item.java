
package com.yoshione.fingen.fts.models;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Item {

    @SerializedName("quantity")
    @Expose
    private double quantity;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("sum")
    @Expose
    private long sum;
    @SerializedName("price")
    @Expose
    private long price;
    @SerializedName("nds18")
    @Expose
    private long nds18;
    @SerializedName("modifiers")
    @Expose
    private List<Modifier> modifiers = null;
    @SerializedName("nds10")
    @Expose
    private long nds10;

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getSum() {
        return sum;
    }

    public void setSum(long sum) {
        this.sum = sum;
    }

    public long getPrice() {
        return price;
    }

    public void setPrice(long price) {
        this.price = price;
    }

    public long getNds18() {
        return nds18;
    }

    public void setNds18(long nds18) {
        this.nds18 = nds18;
    }

    public List<Modifier> getModifiers() {
        return modifiers;
    }

    public void setModifiers(List<Modifier> modifiers) {
        this.modifiers = modifiers;
    }

    public long getNds10() {
        return nds10;
    }

    public void setNds10(long nds10) {
        this.nds10 = nds10;
    }

}
