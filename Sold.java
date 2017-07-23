package com.example.asmid.pricetag;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.TextView;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import static android.app.Activity.RESULT_OK;

/**
 * Created by asmid on 9/20/2016.
 */

public class Sold extends Fragment {


    ArrayList<SoldListObject> soldItems;
    private ListView sold_listview;
    soldListAdapter adapter;
    TextView textView;


    public static Sold newInstance() {
        Sold fragment = new Sold();
        return fragment;
    }

    public Sold() {
    }

    TextView tv;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.sold_tab, container, false);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        textView = (TextView) getView().findViewById(R.id.sold_title);
        sold_listview = (ListView) this.getActivity().findViewById(R.id.sold_listview);
        new getSoldItemsTask().execute(SellerHome.getUsername());
    }


    public void process(){
        adapter = new soldListAdapter(getActivity(),soldItems);
        sold_listview.setAdapter(adapter);
    }

    private ArrayList<SoldListObject> getSoldItems(String username) {


        ArrayList<SoldListObject> result = new ArrayList<SoldListObject>();
        InputStream imageStream = null;
        String url="http://pricetagbackend-env.us-west-1.elasticbeanstalk.com/getSoldItems.php/?username="+username;
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
                for (int k = 0; k < array.length(); k++) {

                    JSONObject item = array.getJSONObject(k);
                    String imageUrl = item.getString("image_url");
                    int id = Integer.parseInt(item.getString("item_id"));
                    imageUrl = imageUrl.replaceAll("\\/", "/");
                    String item_name = item.getString("name");
                    Double price = Double.parseDouble(item.getString("sale_price"));
                    int buyer_id = 0;

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
                    SoldListObject temp = new SoldListObject(imageStream, item_name, bitmap, id, price, buyer_id);
                    result.add(temp);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }

    public class getSoldItemsTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String username = params[0];
            soldItems = getSoldItems(username);
            return "";
        }

        @Override
        protected void onPostExecute(String result){
            process();
        }

    }

}
