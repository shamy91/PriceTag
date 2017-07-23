package com.example.asmid.pricetag;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by asmid on 11/22/2016.
 */

public class soldListAdapter extends ArrayAdapter<SoldListObject> {


    ArrayList<SoldListObject> results;
    private Context context;
    LinkedHashMap<SoldListObject, Integer> mIdmap;

    public soldListAdapter(Context context, ArrayList<SoldListObject> objects) {
        super(context, -1, objects);
        this.context=context;
        this.results = objects;
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

                LayoutInflater inflater = ((MainActivity) context).getLayoutInflater();
                convertView = inflater.inflate(R.layout.sold_list, parent, false);

                final SoldListObject item = getItem(position);
                TextView itemName = (TextView) convertView.findViewById(R.id.soldItemName);
                itemName.setText(item.getItemName());
                ImageView iView = (ImageView)convertView.findViewById(R.id.soldItemImage);
                Bitmap bitmap = item.getBitmap();
                iView.setImageBitmap(bitmap);

                Button deleteItem = (Button) convertView.findViewById(R.id.deletSoldItem);
                deleteItem.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        new deleteAsyncTask().execute(item.getItemId());
                        removeElementAtPosition(position);
                        notifyDataSetChanged();
                    }
                });

            }
        }
        catch (Exception e){
           e.printStackTrace();
        }

        return convertView;
    }


    private class deleteAsyncTask extends AsyncTask<Integer, Void, String> {

        @Override
        protected String doInBackground(Integer... params) {

            String response = deleteSoldItem(params[0]);
            if(response.equals("\"Success\"")){


            }

            return response;
        }

        @Override
        protected void onPostExecute(String response) {
            super.onPostExecute(response);
            processResponse(response);
        }

    }

    private void processResponse(String response) {
    }

    private String deleteSoldItem(Integer param) {

        return null;
    }
}
