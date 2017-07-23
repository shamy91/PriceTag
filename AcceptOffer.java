package com.example.asmid.pricetag;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class AcceptOffer extends AppCompatActivity {

    Intent intent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accept_offer);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        /*FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/


        intent = getIntent();
        ((TextView)findViewById(R.id.text_itemName)).setText(intent.getStringExtra("itemName"));
        ((TextView)findViewById(R.id.text_itemPrice)).setText(intent.getStringExtra("itemPrice"));
        ((TextView)findViewById(R.id.text_buyerName)).setText(intent.getStringExtra("buyerName"));
        ((TextView)findViewById(R.id.text_offerPrice)).setText(intent.getStringExtra("buyerPrice"));
        ((TextView)findViewById(R.id.text_itemName)).setText(intent.getStringExtra("distance"));

        Button acceptOffer = (Button)findViewById(R.id.button_makeOffer);
        acceptOffer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


            }
        });
    }

    public class acceptOfferAsyncTask extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... params) {
            String response = acceptOffer(params[0]);
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

            Toast.makeText(getApplicationContext(), "Item Sold!", Toast.LENGTH_SHORT).show();
            //need to remove from onSale list, put in on Sold tab list
            //also need to remove from all buyer's pending items
        }
    }

    private String acceptOffer(Void param) {

        InputStream inputStream = null;
        String result = "";
        String  url = "http://pricetagbackend-env.us-west-1.elasticbeanstalk.com/acceptOffer.php";
        try {

            String username = SellerHome.getUsername();
            String buyerId = intent.getStringExtra("buyerId");
            String itemId = intent.getStringExtra("itemId");
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(url);

            JSONObject jsonObject = new JSONObject();
            jsonObject.accumulate("itemId", itemId);
            jsonObject.accumulate("username", username);
            jsonObject.accumulate("buyerId", buyerId);
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
