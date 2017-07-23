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
import java.util.Map;
import java.util.TimeZone;

/**
 * Created by asmid on 11/23/2016.
 */

public class BoughtItemsActivity  extends AppCompatActivity {


    private String username;
    private TextView textView;
    private ListView listView;
    private SharedPreferences sharedPrefs;
    ArrayList<SoldListObject> boughtItems;
    BoughtItemsAdapter boughtAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bought_items);
        textView = (TextView) findViewById(R.id.bought_title);
        listView = (ListView) findViewById(R.id.bought_list_view);

        Intent intent = getIntent();
        username = intent.getStringExtra("username");

        new getBoughtItemsTask().execute();

    }

    public class getBoughtItemsTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {

            boughtItems = getBoughtItems();
            return "";
        }

        @Override
        protected void onPostExecute(String result){
            processResult();
        }
    }

    public void processResult() {
        boughtAdapter = new BoughtItemsAdapter(this, boughtItems);
        listView.setAdapter(boughtAdapter);
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

    public ArrayList<SoldListObject> getBoughtItems() {

        ArrayList<SoldListObject> resultList = new ArrayList<>();
        InputStream inputStream = null;
        String result = "";
        String url="http://pricetagbackend-env.us-west-1.elasticbeanstalk.com/getBoughtItems.php/?username="+username;
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
                    String item_name = item.getString("name");
                    int id = Integer.parseInt(item.getString("item_id"));
                    String offer_time = item.getString("offer_time");
                    long days = daysBetween(offer_time);

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

                    SoldListObject temp = new SoldListObject(inputStream, item_name, bitmap, id, days);
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

}
