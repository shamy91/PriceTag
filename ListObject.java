package com.example.asmid.pricetag;

import android.graphics.Bitmap;

import java.io.InputStream;
import java.util.ArrayList;

/**
 * Created by asmid on 10/14/2016.
 */

public class ListObject {

    InputStream imageStream;
    String itemName;
    Bitmap bitmap;
    int itemId;
    Double itemPrice;
    public ArrayList<BuyerListObject> buyerObjectList = new ArrayList<BuyerListObject>();
    int offer_num;

    public ListObject(InputStream imageStream, String itemName, Bitmap bitmap, int itemId, Double itemPrice, ArrayList<BuyerListObject> buyerObjectList, int offers) {
        this.imageStream = imageStream;
        this.itemName = itemName;
        this.bitmap = bitmap;
        this.itemId = itemId;
        this.itemPrice = itemPrice;
        this.buyerObjectList = buyerObjectList;
        this.offer_num = offers;
    }

    public int getOffer_num() {
        return offer_num;
    }

    public void setOffer_num(int offer_num) {
        this.offer_num = offer_num;
    }

    public Double getItemPrice() {
        return itemPrice;
    }

    public void setItemPrice(Double itemPrice) {
        this.itemPrice = itemPrice;
    }

    public ArrayList<BuyerListObject> getBuyerObjectList() {
        return buyerObjectList;
    }

    public void setBuyerObjectList(ArrayList<BuyerListObject> buyerObjectList) {
        this.buyerObjectList = buyerObjectList;
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


}
