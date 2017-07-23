package com.example.asmid.pricetag;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;

/**
 * Created by asmid on 11/22/2016.
 */

public class FavoritesAdapter  extends ArrayAdapter<SoldListObject> {

    ArrayList<SoldListObject> results;
    private Context context;
    private String userLocation;
    private String username;
    SharedPreferences sharedPrefs;
    SharedPreferences.Editor editor;

    public FavoritesAdapter(Context context, ArrayList<SoldListObject> objects, String userLocation, String username, SharedPreferences sharedPreferences) {
        super(context, -1, objects);
        this.results = objects;
        this.context = context;
        this.userLocation = userLocation;
        this.username = username;
        this.sharedPrefs = sharedPreferences;
    }

    private static class ViewHolder
    {
        TextView name;
        ImageView image;
        FloatingActionButton offerbutton;
        Button favbutton;
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
        final ViewHolder viewHolder;
        try {

            if (convertView == null) {

                LayoutInflater inflater = ((Activity) context).getLayoutInflater();
                convertView = inflater.inflate(R.layout.fav_list, parent, false);
                ViewHolder vHolder = new ViewHolder();
                convertView.setTag(vHolder);
            }

                viewHolder = (ViewHolder)convertView.getTag();
                final SoldListObject item = getItem(position);
                viewHolder.name = (TextView) convertView.findViewById(R.id.fav_item_name);
                viewHolder.name.setText(item.getItemName());

                viewHolder.image = (ImageView)convertView.findViewById(R.id.fav_item_image);
                Bitmap bitmap = item.getBitmap();
                viewHolder.image.setImageBitmap(bitmap);

                viewHolder.offerbutton = (FloatingActionButton) convertView.findViewById(R.id.make_fav_offer);
                viewHolder.offerbutton.setOnClickListener(new View.OnClickListener() {
                                                       @Override
                                                       public void onClick(View v) {
                                                                new getBuyerItemTask().execute(item);
                                                       }
                                                   });

               /* viewHolder.favbutton = (Button) convertView.findViewById(R.id.fav_img);
                viewHolder.favbutton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Drawable drawable = viewHolder.favbutton.getBackground();
                        editor = sharedPrefs.edit();
                        String id = String.valueOf(item.getItemId());
                        if(drawable.getConstantState()==(context.getResources().getDrawable(R.drawable.ic_unfav).getConstantState())){
                            editor.putString(id,id);
                            editor.commit();
                            viewHolder.favbutton.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.ic_fav));
                        }
                        else {
                            editor.remove(id);
                            editor.commit();
                            results.remove(position);
                            notifyDataSetChanged();
                        }
                    }
                });*/

        }
        catch (Exception e){
            e.printStackTrace();
        }

        return convertView;
    }

    public class getBuyerItemTask extends AsyncTask<SoldListObject, Void, String> {

        @Override
        protected String doInBackground(SoldListObject... params) {
            String response = getBuyerDetails(params[0]);
            return response;
        }

        @Override
        protected void onPostExecute(String response) {
            super.onPostExecute(response);
            try {
                showItemDetails(response);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }

    public String getBuyerDetails(SoldListObject item) {
        InputStream inputStream = null;
        String result = "";
        String url="http://pricetagbackend-env.us-west-1.elasticbeanstalk.com/getBuyerItem.php";
        try {

            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(url);
            JSONObject jsonObject = new JSONObject();
            String userloc = userLocation.trim().substring(1, userLocation.length()-2);
            String[] location = userloc.split(",");
            Double ulat = Double.parseDouble(location[0]);
            Double ulong = Double.parseDouble(location[1]);

            String itemlocation = item.getItemLocation().substring(1,item.getItemLocation().length()-2);
            String[] ilocation = itemlocation.split(",");
            Double ilat = Double.parseDouble(ilocation[0]);
            Double ilong = Double.parseDouble(ilocation[1]);


            jsonObject.accumulate("userLat", ulat);
            jsonObject.accumulate("userLong", ulong);
            jsonObject.accumulate("itemLat", ilat);
            jsonObject.accumulate("itemLong", ilong);
            jsonObject.accumulate("itemId",item.getItemId());

            String json = jsonObject.toString();
            StringEntity se = new StringEntity(json);
            httpPost.setEntity(se);
            HttpResponse httpResponse = httpclient.execute(httpPost);
            inputStream = httpResponse.getEntity().getContent();
            if(inputStream != null){
                result = convertInputStreamToString(inputStream);
                return result;
            }
            else{
                Log.d("Error", "Error");
                return null;
            }

        } catch (Exception e) {
            Log.d("InputStream", e.getLocalizedMessage());
        }

        return result;
    }


    private static String convertInputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while((line = bufferedReader.readLine()) != null)
            result += line;
        inputStream.close();
        return result;
    }

    private void showItemDetails(String response) throws JSONException {


        JSONObject json = new JSONObject(response);
        JSONObject item = json.getJSONObject("item");
        JSONArray images = json.getJSONArray("images");
        String distance = json.getString("miles");

        Intent intent = new Intent(getContext(),ItemDetails.class);
        intent.putExtra("itemId", item.getString("item_id"));
        intent.putExtra("itemName", item.getString("name"));
        intent.putExtra("itemPrice", item.getString("cost_price"));
        intent.putExtra("itemDescription", item.getString("description"));
        intent.putExtra("distance", distance);
        intent.putExtra("username", username);
        String status = item.getString("status");
        if(status!=null && status.length()!=0 && !status.equals("null"))
            intent.putExtra("status", status);
        else
            intent.putExtra("status", "");
        for(int i=0; i<images.length(); i++){

            JSONObject img = images.getJSONObject(i);
            String imgURL = img.getString("image_url");
            intent.putExtra("url"+i, imgURL);
        }
        context.startActivity(intent);

    }
}
