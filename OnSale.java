package com.example.asmid.pricetag;

import android.app.Activity;
import android.app.Fragment;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.Exchanger;

import static android.app.Activity.RESULT_OK;

/**
 * Created by asmid on 9/20/2016.
 */

public class OnSale extends Fragment {

    ArrayList<ListObject> itemDetails;
    private ExpandableListView elv;
    saleListAdapter adapter;

    public static OnSale newInstance() {
        OnSale fragment = new OnSale();
        return fragment;
    }

    public OnSale() {
    }

    TextView tv;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.onsale_tab, container, false);
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle state) {
        super.onActivityCreated(state);
        tv = (TextView) getView().findViewById(R.id.onsale_title);
        elv = (ExpandableListView) this.getActivity().findViewById(R.id.onsale_list);

        new getSaleItemsTask().execute(SellerHome.getUsername());

        FloatingActionButton newItem = (FloatingActionButton) getView().findViewById(R.id.button_new_item);
        newItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), sell_new_item.class);
                intent.putExtra("username", SellerHome.getUsername());
                startActivityForResult(intent, 100);
            }
        });
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(requestCode == 100 && resultCode == RESULT_OK && null!= data){

            new getSaleItemsTask().execute(SellerHome.getUsername());
        }
    }

    public ArrayList<ListObject>  getItems(String username) {
        ArrayList<ListObject> result = new ArrayList<ListObject>();

        InputStream imageStream = null;
        String url="http://pricetagbackend-env.us-west-1.elasticbeanstalk.com/getSaleItems.php/?username="+username;
        try{
            HttpClient httpClient = new DefaultHttpClient();
            HttpGet httpGet = new HttpGet(url);
            HttpResponse httpResponse = httpClient.execute(httpGet);
            HttpEntity entity = httpResponse.getEntity();
            InputStream stream = entity.getContent();
            BufferedReader bf = new BufferedReader(new InputStreamReader(stream));
            StringBuilder sb = new StringBuilder();
            String string = null;
            while ((string = bf.readLine()) != null) {
                sb.append(string);
            }
            String data = sb.toString();

            JSONArray array = new JSONArray(data);

            if(array != null) {
                for(int k=0; k<array.length(); k++) {
                    ArrayList<BuyerListObject> buyerListObjectList = new ArrayList<BuyerListObject>();
                    JSONObject json = array.getJSONObject(k);
                    JSONObject item = json.getJSONObject("item");
                    if(json.has("buyers")){
                        JSONArray buyers = json.getJSONArray("buyers");
                        if(buyers != null) {
                            for(int x=0; x<buyers.length(); x++) {
                                JSONObject buyer = buyers.getJSONObject(x);
                                BuyerListObject bListObj = new BuyerListObject(buyer.getString("username"),Double.parseDouble(buyer.getString("offer_price")),Integer.parseInt(buyer.getString("buyer_id")), Double.parseDouble(buyer.getString("distance")),buyer.getString("email"));
                                buyerListObjectList.add(bListObj);
                            }
                        }
                    }

                    String imageUrl = item.getString("image_url");
                    int id = Integer.parseInt(item.getString("item_id"));
                    imageUrl = imageUrl.replaceAll("\\/","/");
                    String item_name = item.getString("name");
                    Double price = Double.parseDouble(item.getString("cost_price"));


                    HttpGet httpRequest = null;
                    httpRequest = new HttpGet(imageUrl);
                    HttpClient httpclient = new DefaultHttpClient();
                    HttpResponse response = (HttpResponse) httpclient.execute(httpRequest);
                    HttpEntity ent = response.getEntity();
                    BufferedHttpEntity bufHttpEntity = new BufferedHttpEntity(ent);
                    imageStream = bufHttpEntity.getContent();

                    BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
                    bitmapOptions.inJustDecodeBounds = true;
                    bitmapOptions.inSampleSize = 1;
                    Bitmap bitmap = BitmapFactory.decodeStream(imageStream);
                    ListObject temp = new ListObject(imageStream, item_name, bitmap, id, price, buyerListObjectList, buyerListObjectList.size());

                    result.add(temp);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        catch (Exception e){

        }
        return result;
    }

    public class getSaleItemsTask extends AsyncTask<String, Void, String>{

        @Override
        protected String doInBackground(String... params) {
            String username = params[0];
            //get ArrayList of ArrayList of Strings - itemname, imageUrl from sever
            itemDetails = getItems(username);
            return "";
        }

        @Override
        protected void onPostExecute(String result){
            processList();

        }

    }

    private void processList() {

        adapter = new saleListAdapter(getActivity(),itemDetails);
        elv.setAdapter(adapter);
        elv.setClickable(true);
        elv.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(final ExpandableListView parent, final View v, final int groupPosition, int childPosition, long id) {

                final ListObject item = adapter.getGroup(groupPosition);
                final BuyerListObject buyer = adapter.getChild(groupPosition,childPosition);

                AlertDialog alertDialog = new AlertDialog.Builder(getContext())
                        .setTitle("Confirm")
                        .setMessage("What would you like to do?")
                        .create();

                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Accept Bid", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int id) {

                        JSONObject jsonObject = new JSONObject();
                        try {
                            jsonObject.accumulate("itemId", item.getItemId());
                            jsonObject.accumulate("buyerId", buyer.getBuyerId());
                            jsonObject.accumulate("offerPrice", buyer.getBuyerPrice());
                            jsonObject.accumulate("username",SellerHome.getUsername());
                            //jsonObject.accumulate("index", adapter.getItemId(groupPosition));
                             parent.collapseGroup(groupPosition);
                            adapter.results.remove(groupPosition);
                            adapter.notifyDataSetChanged();
                            new acceptOfferAsyncTask().execute(jsonObject);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }catch (Exception e){
                            e.printStackTrace();
                        }

                    } });

                alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int id) {

                        //...

                    }});

                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Send Email", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int id) {

                        String email = buyer.getEmail();
                        Intent emailIntent = new Intent(Intent.ACTION_SEND);
                        String subject = SellerHome.getUsername() + " wants to sell" + item.getItemName() + " to you on PriceTag";
                        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{email});
                        emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
                        emailIntent.putExtra(Intent.EXTRA_TEXT, "");
                        emailIntent.setType("message/rfc822");
                        try {
                            startActivity(Intent.createChooser(emailIntent, "choose an email client"));

                        }catch (android.content.ActivityNotFoundException ex) {
                            Toast.makeText(getActivity(), "There is no email client installed.", Toast.LENGTH_SHORT).show();
                        }

                    }});
                alertDialog.show();

                return false;
            }
        });

    }

    public class acceptOfferAsyncTask extends AsyncTask<JSONObject, Void, String> {

        @Override
        protected String doInBackground(JSONObject... params) {
            String response = acceptOffer(params[0]);
            if(response.equals("\"Success\"")){
                JSONObject json = params[0];
                try {

                    return json.getString("index");
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

        if(!response.equals("\"Success\"")){

            Toast.makeText(getActivity().getBaseContext(), "Item Sold!", Toast.LENGTH_SHORT).show();
           // adapter.notifyDataSetChanged();
            try {
                long itemIndex =  Integer.parseInt(response);
                /*adapter.results.remove(itemIndex);
                adapter.notifyDataSetChanged();*/
            } catch (Exception e) {
                e.printStackTrace();
            }
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
