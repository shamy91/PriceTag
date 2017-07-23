package com.example.asmid.pricetag;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Created by asmid on 11/22/2016.
 */

public class PendingListAdapter extends ArrayAdapter<SoldListObject> {
    ArrayList<SoldListObject> results;
    private Context context;
    String username;

    public PendingListAdapter(Context context, ArrayList<SoldListObject> objects, String username) {
        super(context, -1, objects);
        this.results = objects;
        this.context = context;
        this.username = username;
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

                LayoutInflater inflater = ((Activity) context).getLayoutInflater();
                convertView = inflater.inflate(R.layout.pending_list, parent, false);

                final SoldListObject item = getItem(position);
                TextView itemName = (TextView) convertView.findViewById(R.id.pending_item_name);
                itemName.setText(item.getItemName());

                ImageView iView = (ImageView)convertView.findViewById(R.id.pending_item_image);
                Bitmap bitmap = item.getBitmap();
                iView.setImageBitmap(bitmap);
                TextView dayCount = (TextView) convertView.findViewById(R.id.pending_day_count);
                dayCount.setText(item.getDays()+" day(s) ago");

                final Button withDrawButton = (Button)convertView.findViewById(R.id.withdraw_offer);
                withDrawButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        new removeOfferAsyncTask().execute(item.getItemId());
                        withDrawButton.setEnabled(false);
                    }
                });

            }
        }
        catch (Exception e){
            e.printStackTrace();
        }

        return convertView;
    }

    public class removeOfferAsyncTask extends AsyncTask<Integer, Void, String> {

        @Override
        protected String doInBackground(Integer... params) {
            String response = removeOffer(params[0]);
            return response;
        }

        @Override
        protected void onPostExecute(String response) {
            super.onPostExecute(response);
            processOffer(response);
        }
    }

    private void processOffer(String response) {


    }

    private String removeOffer(Integer param) {
        InputStream inputStream = null;
        String result = "";
        String url="";
        try {

            url = "http://pricetagbackend-env.us-west-1.elasticbeanstalk.com/removeOffer.php/?itemId="+param+"&buyerName="+username;
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


}
