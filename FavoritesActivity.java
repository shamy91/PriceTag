package com.example.asmid.pricetag;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

/**
 * Created by asmid on 11/22/2016.
 */

public class FavoritesActivity extends AppCompatActivity{

    private String username;
    private TextView textView;
    private ListView listView;
    private SharedPreferences sharedPrefs;
    ArrayList<SoldListObject> favItems;
    FavoritesAdapter favoritesAdapter;
    String userLocation;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);
        textView = (TextView) findViewById(R.id.fav_title);
        listView = (ListView) findViewById(R.id.fav_list_view);

        Intent intent = getIntent();
        username = intent.getStringExtra("username");

        new getFavoriteTask().execute();

    }

    public class getFavoriteTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {

            sharedPrefs = getSharedPreferences(username, 0);
            Map<String,?> keys = sharedPrefs.getAll();

            ArrayList<Integer> favItemList = new ArrayList<Integer>();
            for(Map.Entry<String,?> entry : keys.entrySet()){
                if(!entry.getKey().contentEquals("location"))
                    favItemList.add(Integer.parseInt(entry.getKey()));
            }

            favItems = getFavorites(favItemList);
            userLocation = sharedPrefs.getString("location","");
            return "";
        }

        @Override
        protected void onPostExecute(String result){

            processResult();

        }
    }

    public void processResult() {
        favoritesAdapter = new FavoritesAdapter(this,favItems, userLocation, username, sharedPrefs);
        listView.setAdapter(favoritesAdapter);
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

    public ArrayList<SoldListObject> getFavorites(ArrayList<Integer> favList) {

        ArrayList<SoldListObject> resultList = new ArrayList<>();
        String json = favList.toString().replaceAll("\\s+","");
        InputStream inputStream = null;
        String result = "";
        String url="http://pricetagbackend-env.us-west-1.elasticbeanstalk.com/getFavorites.php/?items="+json.substring(1, json.length()-1);
        try {

            HttpClient httpclient = new DefaultHttpClient();
            HttpGet httpGet = new HttpGet(url);
            HttpResponse httpResponse = httpclient.execute(httpGet);
            inputStream = httpResponse.getEntity().getContent();
            if(inputStream != null){
                result = convertInputStreamToString(inputStream);
                JSONArray jArray = new JSONArray(result);
                for(int k=0; k<jArray.length(); k++) {
                    JSONObject item = jArray.getJSONObject(k);
                    String imageUrl = item.getString("image_url");
                    imageUrl = imageUrl.replaceAll("\\/","/");
                    int id = Integer.parseInt(item.getString("item_id"));
                    String item_name = item.getString("name");
                    String item_location = item.getString("item_location");

                    HttpGet httpRequest = null;
                    httpRequest = new HttpGet(imageUrl);
                    HttpClient httpClient = new DefaultHttpClient();
                    HttpResponse response = (HttpResponse) httpClient.execute(httpRequest);
                    HttpEntity ent = response.getEntity();
                    BufferedHttpEntity bufHttpEntity = new BufferedHttpEntity(ent);
                    inputStream = bufHttpEntity.getContent();

                    BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
                    bitmapOptions.inJustDecodeBounds = true;
                    bitmapOptions.inSampleSize = 1;
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

                    SoldListObject temp = new SoldListObject(inputStream, item_name, bitmap, id,item_location);
                    resultList.add(temp);
                }
                return resultList;
            }
            else{
                Log.d("Error", "Error");
                return null;
            }

        } catch (Exception e) {
            Log.d("InputStream", e.getLocalizedMessage());
        }
        return resultList;
    }


}
