package com.example.asmid.pricetag;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.ExpandableListAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cloudinary.Cloudinary;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by asmid on 10/14/2016.
 */

public class saleListAdapter extends BaseExpandableListAdapter implements ExpandableListAdapter{

    ArrayList<ListObject> results;
    private Context context;
    LinkedHashMap<ListObject, Integer> mIdmap;

    public saleListAdapter(Context context, ArrayList<ListObject> objects) {
        this.context=context;
        this.results=objects;

        mIdmap = new LinkedHashMap<>();
        for(int i=0; i<results.size();i++)
            mIdmap.put(results.get(i), i);

    }


    private static class ViewHolder
    {
        TextView name;
        TextView offernum;
        TextView id;
        ImageView image;
        ImageButton viewbuyers;
    }

    private static class ChilHolder
    {
        TextView name;
        TextView price;
        TextView id;

    }


    public int getItemId(int position){

        return position;
    }
    @Override
    public int getGroupCount() {
        return results.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        int count = results.get(groupPosition).buyerObjectList.size();
        return count;
    }

    @Override
    public ListObject getGroup(int groupPosition) {
        return results.get(groupPosition);
    }

    @Override
    public BuyerListObject getChild(int groupPosition, int childPosition) {
        return results.get(groupPosition).buyerObjectList.get(childPosition);
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {

        return childPosition;
    }


    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        final ViewHolder Vholder;
        try {

            if (convertView == null) {

                LayoutInflater inflater = ((Activity)context).getLayoutInflater();
                convertView = inflater.inflate(R.layout.sale_list, parent, false);
                ViewHolder holder = new ViewHolder();
                convertView.setTag(holder);
            }

            Vholder = (ViewHolder)convertView.getTag();
            ListObject itemData = getGroup(groupPosition);
            Vholder.name = (TextView) convertView.findViewById(R.id.item_name);
            Vholder.name.setText(itemData.getItemName());
            String offers = String.valueOf(itemData.getOffer_num());
            Vholder.offernum = (TextView)convertView.findViewById(R.id.text_numBuyers);
            Vholder.offernum.setText(offers+ " Bid(s)");
            Vholder.image = (ImageView)convertView.findViewById(R.id.saleItemImage);
            Bitmap bitmap = itemData.getBitmap();
            Vholder.image.setImageBitmap(bitmap);
            String itemId = String.valueOf(itemData.getItemId());
            Vholder.id  = (TextView) convertView.findViewById(R.id.item_id);
            Vholder.id .setText(itemId);
            Vholder.id .setVisibility(View.GONE);
            Vholder.viewbuyers  = (ImageButton) convertView.findViewById(R.id.viewBuyersButton);
            Vholder.viewbuyers.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
            Vholder.viewbuyers .setFocusable(false);
            if(offers.equals("0")){
                Vholder.viewbuyers.setEnabled(false);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return convertView;
    }



    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        final ChilHolder holder;
        try {

            if (convertView == null) {

                LayoutInflater inflater = ((Activity)context).getLayoutInflater();
                convertView = inflater.inflate(R.layout.buyer_list, parent, false);
                ChilHolder viewHolder = new ChilHolder();
                convertView.setTag(viewHolder);
            }

            holder = (ChilHolder) convertView.getTag();
            BuyerListObject itemData = getChild(groupPosition, childPosition);
            holder.name = (TextView) convertView.findViewById(R.id.buyer_name);
            holder.name.setText(itemData.getBuyerName());
            holder.price = (TextView) convertView.findViewById(R.id.buyer_price);
            holder.price.setText("$"+String.valueOf(itemData.getBuyerPrice()));
            holder.id = (TextView) convertView.findViewById(R.id.buyer_id);
            holder.id.setText(String.valueOf(itemData.getBuyerId()));
            holder.id.setVisibility(View.GONE);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    public class acceptOfferAsyncTask extends AsyncTask<JSONObject, Void, String> {

        @Override
        protected String doInBackground(JSONObject... params) {
            String response = acceptOffer(params[0]);
            if(response.equals("\"Success\"")){
                JSONObject json = params[0];

                try {
                    long itemIndex = Integer.parseInt(json.getString("index"));
                    results.remove(itemIndex);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            return response;
        }

        @Override
        protected void onPostExecute(String response) {
            super.onPostExecute(response);
            processOffer(response);
        }
    }

    private void processOffer(String response) {

        if(response.equals("\"Success\"")){

            Toast.makeText(context, "Item Sold!", Toast.LENGTH_SHORT).show();
            saleListAdapter.this.notifyDataSetChanged();
        }
    }

    private String acceptOffer(JSONObject param) {

        InputStream inputStream = null;
        String result = "";
        String  url = "http://pricetagbackend-env.us-west-1.elasticbeanstalk.com/acceptOffer.php";
        try {

            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(url);
            String json = param.toString();
            StringEntity se = new StringEntity(json);
            httpPost.setEntity(se);
            HttpResponse httpResponse = httpclient.execute(httpPost);
            inputStream = httpResponse.getEntity().getContent();
            if(inputStream != null){
                result = convertInputStreamToString(inputStream);
                return result;
            }
            else{
                return "Error connecting to server!";
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

}
