package com.example.asmid.pricetag;

/**
 * Created by asmid on 10/26/2016.
 */

public class ItemObject {


    double latitude;
    double longitude;
    int itemId;
    String itemName;
    Float itemPrice;
    String category;

    public ItemObject(double latitude, double longitude, int itemId, String itemName, Float itemPrice) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.itemId = itemId;
        this.itemName = itemName;
        this.itemPrice = itemPrice;
    }

    public ItemObject(double latitude, double longitude, int itemId, String itemName, Float itemPrice, String category) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.itemId = itemId;
        this.itemName = itemName;
        this.itemPrice = itemPrice;
        this.category = category;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public ItemObject(double latitude, double longitude, int itemId) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.itemId = itemId;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public Float getItemPrice() {
        return itemPrice;
    }

    public void setItemPrice(Float itemPrice) {
        this.itemPrice = itemPrice;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public int getItemId() {
        return itemId;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }
}
