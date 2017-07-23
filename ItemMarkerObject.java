package com.example.asmid.pricetag;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by asmid on 10/28/2016.
 */

public class ItemMarkerObject {

    int itemId;
    LatLng userLoc;
    LatLng itemLoc;

    public ItemMarkerObject(int itemId, LatLng userLoc, LatLng itemLoc) {
        this.itemId = itemId;
        this.userLoc = userLoc;
        this.itemLoc = itemLoc;
    }

    public int getItemId() {
        return itemId;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    public LatLng getUserLoc() {
        return userLoc;
    }

    public void setUserLoc(LatLng userLoc) {
        this.userLoc = userLoc;
    }

    public LatLng getItemLoc() {
        return itemLoc;
    }

    public void setItemLoc(LatLng itemLoc) {
        this.itemLoc = itemLoc;
    }

    @Override
    public String toString() {
        return "ItemMarkerObject{" +
                "itemId=" + itemId +
                ", userLoc=" + userLoc +
                ", itemLoc=" + itemLoc +
                '}';
    }
}
