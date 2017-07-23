package com.example.asmid.pricetag;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ItemDetails extends AppCompatActivity {

    private String username;
    private int itemId;
    private Button makeOfferButton;
    private SharedPreferences.Editor editor;
    private SharedPreferences sharedPrefs;
    private Button favoriteButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_details);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitleTextColor(Color.WHITE);
        toolbar.setTitle("Place a Bid");
        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        username = intent.getStringExtra("username");
        itemId = Integer.parseInt(intent.getStringExtra("itemId"));

        favoriteButton = (Button)findViewById(R.id.favImg);
        sharedPrefs = getSharedPreferences(username, 0);
        String id = String.valueOf(itemId);
        String check = sharedPrefs.getString(id,"");
        if(check != "") {
            favoriteButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.ic_fav));
        }
        else {
            favoriteButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.ic_unfav));
        }


        ((TextView)findViewById(R.id.text_itemName)).setText(intent.getStringExtra("itemName"));
        ((TextView)findViewById(R.id.text_itemPrice)).setText("$"+intent.getStringExtra("itemPrice"));
        ((TextView)findViewById(R.id.text_itemDesc)).setText(intent.getStringExtra("itemDescription"));
        ((TextView)findViewById(R.id.sellerName)).setText("Sold by: " + intent.getStringExtra("seller"));
        String dist = intent.getStringExtra("distance");
        ((TextView)findViewById(R.id.text_itemDist)).setText(dist.substring(0, 5) + " mi");
        String status = intent.getStringExtra("status");
        if(status!=null && status.length()!=0){
            if(status.equals("Pending")){
                TextView tv = (TextView)findViewById(R.id.button_makeOffer);
                tv.setText("Remove Bid");
                tv.setEnabled(true);
                ((EditText) findViewById(R.id.editText_offerPrice)).setEnabled(false);
            }
            else if(status.equals("Complete")) {
                TextView tv = (TextView)findViewById(R.id.button_makeOffer);
                tv.setText("Bid");
                tv.setEnabled(false);
                ((EditText) findViewById(R.id.editText_offerPrice)).setEnabled(false);
            }
        }
        else{

            TextView tv = (TextView)findViewById(R.id.button_makeOffer);
            tv.setText("Bid");
            tv.setEnabled(true);
            ((EditText) findViewById(R.id.editText_offerPrice)).setEnabled(true);
        }
        String url = intent.getStringExtra("url0");
        new imageAsyncTask().execute(url);

        favoriteButton = (Button)findViewById(R.id.favImg);
        favoriteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Drawable drawable = favoriteButton.getBackground();
                editor = sharedPrefs.edit();
                String id = String.valueOf(itemId);
                if(drawable.getConstantState()==(getResources().getDrawable(R.drawable.ic_unfav).getConstantState())){
                    editor.putString(id,id);
                    editor.commit();
                    favoriteButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.ic_fav));
                }
                else {
                    editor.remove(id);
                    editor.commit();
                    favoriteButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.ic_unfav));
                }
            }
        });

        makeOfferButton = (Button)findViewById(R.id.button_makeOffer);
        makeOfferButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText editText = (EditText)findViewById(R.id.editText_offerPrice);
                String price = editText.getText().toString();
                int offerPrice=0;
                if(price==null || price.length()==0)
                    offerPrice=0;
                else
                    offerPrice = Integer.parseInt(editText.getText().toString());
                new makeOfferAsyncTask().execute(offerPrice);
            }
        });
    }

    public class makeOfferAsyncTask extends AsyncTask<Integer, Void, String> {

        @Override
        protected String doInBackground(Integer... params) {
            String response = makeOffer(params[0]);
            return response;
        }

        @Override
        protected void onPostExecute(String response) {
            super.onPostExecute(response);
            processOffer(response);
        }
    }

    private void processOffer(String result) {
        if(result.equals("\"Success\"")) {
            if(makeOfferButton.getText().equals("Bid")) {
                Toast.makeText(getApplicationContext(), "Bid made successfully!", Toast.LENGTH_SHORT).show();
                makeOfferButton.setText("Remove Bid");
                ((EditText) findViewById(R.id.editText_offerPrice)).setEnabled(false);
            }
            else {
                Toast.makeText(getApplicationContext(), "Bid removed", Toast.LENGTH_SHORT).show();
                makeOfferButton.setText("Bid");
                ((EditText) findViewById(R.id.editText_offerPrice)).setEnabled(true);

            }
        }
    }

    private String makeOffer(int offerPrice) {

        InputStream inputStream = null;
        String result = "";
        String url="";
        try {
            if(makeOfferButton.getText().equals("Bid")) {
                url = "http://pricetagbackend-env.us-west-1.elasticbeanstalk.com/makeOffer.php/?offerPrice="+offerPrice+"&itemId="+itemId+"&buyerName="+username;
            }
            else {
                url = "http://pricetagbackend-env.us-west-1.elasticbeanstalk.com/removeOffer.php/?itemId="+itemId+"&buyerName="+username;
            }
            HttpClient httpclient = new DefaultHttpClient();
            HttpGet httpGet = new HttpGet(url);
            HttpResponse httpResponse = httpclient.execute(httpGet);
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
        // 11. return result
        return "";
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


    public class imageAsyncTask extends AsyncTask<String, Void, Bitmap>{

        @Override
        protected Bitmap doInBackground(String... params) {

            Bitmap b = displayImage(params[0]);
            return b;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            if(bitmap!=null)
                ((ImageView)findViewById(R.id.itemImage0)).setImageBitmap(bitmap);
        }
    }

    private Bitmap displayImage(String url) {

        try {
            HttpGet httpRequest = null;
            httpRequest = new HttpGet(url);
            HttpClient httpclient = new DefaultHttpClient();
            HttpResponse response = (HttpResponse) httpclient.execute(httpRequest);
            HttpEntity ent = response.getEntity();
            BufferedHttpEntity bufHttpEntity = new BufferedHttpEntity(ent);
            InputStream imageStream = bufHttpEntity.getContent();

            BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
            bitmapOptions.inJustDecodeBounds = true;
            bitmapOptions.inSampleSize = 1;
            Bitmap bitmap = BitmapFactory.decodeStream(imageStream);
            return bitmap;
        }
        catch (Exception e){
            Log.d("Itemerror", e.getMessage());
        }
        return null;
    }
}
