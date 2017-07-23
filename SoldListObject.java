package com.example.asmid.pricetag;

import android.graphics.Bitmap;

import java.io.InputStream;
import java.util.ArrayList;

/**
 * Created by asmid on 11/22/2016.
 */

public class SoldListObject {

    InputStream imageStream;
    String itemName;
    Bitmap bitmap;
    int itemId;
    int buyerId;
    Double salePrice;
    String itemLocation;
    long days;
    String status;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public SoldListObject(InputStream imageStream, String itemName, Bitmap bitmap, int itemId, Double salePrice, int buyerId) {
        this.imageStream = imageStream;
        this.itemName = itemName;
        this.bitmap = bitmap;
        this.itemId = itemId;
        this.salePrice = salePrice;
        this.buyerId = buyerId;

    }

    public SoldListObject(InputStream imageStream, String itemName, Bitmap bitmap, int itemId, String itemLocation){
        this.imageStream = imageStream;
        this.itemName = itemName;
        this.bitmap = bitmap;
        this.itemId = itemId;
        this.itemLocation = itemLocation;
    }

    public SoldListObject(InputStream imageStream, String itemName, Bitmap bitmap, int itemId, long days){
        this.imageStream = imageStream;
        this.itemName = itemName;
        this.bitmap = bitmap;
        this.itemId = itemId;
        this.days = days;
    }

    public long getDays() {
        return days;
    }

    public void setDays(int days) {
        this.days = days;
    }

    public String getItemLocation() {
        return itemLocation;
    }

    public void setItemLocation(String itemLocation) {
        this.itemLocation = itemLocation;
    }

    public Double getItemPrice() {
        return salePrice;
    }

    public void setItemPrice(Double salePrice) {
        this.salePrice = salePrice;
    }

    public int getItemId() {
        return itemId;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public InputStream getImageStream() {
        return imageStream;
    }

    public void setImageStream(InputStream imageStream) {
        this.imageStream = imageStream;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public int getBuyerId() {
        return buyerId;
    }

    public void setBuyerId(int buyerId) {
        this.buyerId = buyerId;
    }

    public Double getSalePrice() {
        return salePrice;
    }

    public void setSalePrice(Double salePrice) {
        this.salePrice = salePrice;
    }
}
