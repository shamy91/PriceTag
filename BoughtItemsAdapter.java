package com.example.asmid.pricetag;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by asmid on 11/23/2016.
 */

public class BoughtItemsAdapter extends ArrayAdapter<SoldListObject> {

    ArrayList<SoldListObject> results;
    private Context context;
    String username;

    public BoughtItemsAdapter(Context context, ArrayList<SoldListObject> objects) {
        super(context, -1, objects);
        this.results = objects;
        this.context = context;
        this.username = username;
    }

    @Override
    public int getCount() {
        return results.size();
    }

    @Override
    public SoldListObject getItem(int index) {
        return results.get(index);
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    public void removeElementAtPosition(int position){
        this.results.remove(position);
        notifyDataSetChanged();
    }


    public View getView(final int position, View convertView, ViewGroup parent) {

        try {

            if (convertView == null) {

                LayoutInflater inflater = ((Activity) context).getLayoutInflater();
                convertView = inflater.inflate(R.layout.bought_list, parent, false);

                final SoldListObject item = getItem(position);
                TextView itemName = (TextView) convertView.findViewById(R.id.bought_item_name);
                itemName.setText(item.getItemName());

                ImageView iView = (ImageView)convertView.findViewById(R.id.bought_item_image);
                Bitmap bitmap = item.getBitmap();
                iView.setImageBitmap(bitmap);
                TextView dayCount = (TextView) convertView.findViewById(R.id.bought_day_count);
                dayCount.setText(item.getDays()+" day(s) ago");

            }
        }
        catch (Exception e){
            e.printStackTrace();
        }

        return convertView;
    }



}
