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
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.BufferedHttpEntity;
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
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

/**
 * Created by asmid on 11/22/2016.
 */

public class PendingItemsActivity extends AppCompatActivity {

    private String username;
    private TextView textView;
    private ListView listView;
    private SharedPreferences sharedPrefs;
    ArrayList<SoldListObject> pendingItems;
    PendingListAdapter pendingAdapter;
    String userLocation;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pending_items);
        textView = (TextView) findViewById(R.id.pending_title);
        listView = (ListView) findViewById(R.id.pending_list_view);

        Intent intent = getIntent();
        username = intent.getStringExtra("username");

        new getPendingItemsTask().execute();

    }

    public class getPendingItemsTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {

            pendingItems = getPendingItems();
            return "";
        }

        @Override
        protected void onPostExecute(String result){

            processResult();

        }
    }

    public void processResult() {
        pendingAdapter = new PendingListAdapter(this,pendingItems, username);
        listView.setAdapter(pendingAdapter);
        listView.setClickable(true);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                pendingAdapter.results.remove(position);
                pendingAdapter.notifyDataSetChanged();
                new getItemTask().execute(pendingAdapter.results.get(position));
            }
        });
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

    public ArrayList<SoldListObject> getPendingItems() {

        ArrayList<SoldListObject> resultList = new ArrayList<>();
        InputStream inputStream = null;
        String result = "";
        String url="http://pricetagbackend-env.us-west-1.elasticbeanstalk.com/getPendingItems.php/?username="+username;
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
                    String offer_time = item.getString("offer_time");

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

                    //number of days calculation
                    long diffDays = daysBetween(offer_time);

                    SoldListObject temp = new SoldListObject(inputStream, item_name, bitmap, id, diffDays);
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

    public long daysBetween(String offer_time){

        Calendar cal1 = Calendar.getInstance(TimeZone.getTimeZone("PST"));
        Calendar cal2 = Calendar.getInstance(TimeZone.getTimeZone("PST"));
        String[] dateFields = offer_time.split("\\s+")[0].split("-");
        cal1.set(Integer.parseInt(dateFields[0]), Integer.parseInt(dateFields[1]), Integer.parseInt(dateFields[2]));
        Date date2 = new Date();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String[] today = dateFormat.format(date2).split("-");
        cal2.set(Integer.parseInt(today[0]), Integer.parseInt(today[1]), Integer.parseInt(today[2]));
        Date start = cal1.getTime();
        Date end = cal2.getTime();
        long diffDays = Math.abs(((long)end.getTime() - (long)start.getTime())/(1000*60*60*24));

        return diffDays;
    }

    public class getItemTask extends AsyncTask<SoldListObject, Void, String> {

        @Override
        protected String doInBackground(SoldListObject... params) {
            String response = updateWithdrawal(params[0]);
            return response;
        }

        @Override
        protected void onPostExecute(String response) {
            super.onPostExecute(response);
            processResult(response);
        }
    }

    private void processResult(String response) {
        if(response.contentEquals("Success")) {
            Toast.makeText(getApplicationContext(),"Offer withdrawn",Toast.LENGTH_SHORT).show();
        }
    }

    public String updateWithdrawal(SoldListObject item) {

        InputStream inputStream = null;
        String result = "";
        String url="http://pricetagbackend-env.us-west-1.elasticbeanstalk.com/withdrawOffer.php/?username="+username+"&itemId="+item.getItemId();
        try {
            HttpClient httpclient = new DefaultHttpClient();
            HttpGet httpGet = new HttpGet(url);
            HttpResponse httpResponse = httpclient.execute(httpGet);
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

}
