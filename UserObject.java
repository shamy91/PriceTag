package com.example.asmid.pricetag;

import android.renderscript.ScriptGroup;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * Created by asmid on 10/13/2016.
 */

public class UserObject {

    String image_url;
    String username;
    String item_name;
    String item_desc;
    double item_lat;
    double item_long;
    float price;
    String category;

    public UserObject(String url, String username, String itemName, String itemDesc, Float price, double latitude, double longitude, String category) {
        this.image_url = url;
        this.username = username;
        this.item_name = itemName;
        this.item_desc = itemDesc;
        this.price = price;
        this.item_lat = latitude;
        this.item_long = longitude;
        this.category = category;
    }

    public String getUrl() {
        return image_url;
    }

    public void setInputStream(InputStream inputStream) {
        this.image_url = image_url;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getItem_name() {
        return item_name;
    }

    public void setItem_name(String item_name) {
        this.item_name = item_name;
    }

    public String getItem_desc() {
        return item_desc;
    }

    public void setItem_desc(String item_desc) {
        this.item_desc = item_desc;
    }

    public double getItem_lat() {
        return item_lat;
    }

    public void setItem_lat(double item_lat) {
        this.item_lat = item_lat;
    }

    public double getItem_long() {
        return item_long;
    }

    public void setItem_long(double item_long) {
        this.item_long = item_long;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }
}
