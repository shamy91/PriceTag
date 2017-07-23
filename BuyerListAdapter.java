package com.example.asmid.pricetag;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by asmid on 10/27/2016.
 */

public class BuyerListAdapter  extends ArrayAdapter<BuyerListObject> {

    ArrayList<BuyerListObject> results;
    private Context context;
    HashMap<BuyerListObject,Integer> mIdmap;

    public BuyerListAdapter(Context context, ArrayList<BuyerListObject> objects) {
        super(context,-1,objects);
        this.context=context;
        this.results=objects;

        mIdmap = new HashMap<BuyerListObject, Integer>();
        for(int i = 0; i<results.size(); i++){
            mIdmap.put(results.get(i),i);
        }
    }

    @Override
    public int getCount() {
        return results.size();
    }

    @Override
    public BuyerListObject getItem(int index) {

        return results.get(index);
    }

    @Override
    public long getItemId(int position) {
        BuyerListObject item = getItem(position);
        return mIdmap.get(item);
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    public void removeElementAtPosition(int position){
        this.results.remove(position);
        notifyDataSetChanged();
    }

    public View getView(int position, View convertView, ViewGroup parent) {

        try {

            if (convertView == null) {

                LayoutInflater inflater = ((Activity)context).getLayoutInflater();
                convertView = inflater.inflate(R.layout.buyer_list, parent, false);

                BuyerListObject itemData = getItem(position);
                ((TextView) convertView.findViewById(R.id.buyer_name)).setText(itemData.getBuyerName());
                ((TextView) convertView.findViewById(R.id.buyer_price)).setText(String.valueOf(itemData.getBuyerPrice()));
                TextView textView = ((TextView) convertView.findViewById(R.id.buyer_id));
                textView.setText(String.valueOf(itemData.getBuyerId()));
                textView.setVisibility(View.GONE);


            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return convertView;
    }


}
