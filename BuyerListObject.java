package com.example.asmid.pricetag;

/**
 * Created by asmid on 10/27/2016.
 */

public class BuyerListObject {
    String buyerName;
    double buyerPrice;
    int buyerId;
    Double buyerDistance;
    String email;

    public BuyerListObject(String buyerName, double buyerPrice, int buyerId, Double buyerDistance, String email) {
        this.buyerName = buyerName;
        this.buyerPrice = buyerPrice;
        this.buyerId = buyerId;
        this.buyerDistance = buyerDistance;
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Double getBuyerDistance() {
        return buyerDistance;
    }

    public void setBuyerDistance(Double buyerDistance) {
        this.buyerDistance = buyerDistance;
    }

    public int getBuyerId() {
        return buyerId;
    }

    public void setBuyerId(int buyerId) {
        this.buyerId = buyerId;
    }

    public double getBuyerPrice() {
        return buyerPrice;
    }

    public void setBuyerPrice(double buyerPrice) {
        this.buyerPrice = buyerPrice;
    }

    public String getBuyerName() {
        return buyerName;
    }

    public void setBuyerName(String buyerName) {
        this.buyerName = buyerName;
    }


}
