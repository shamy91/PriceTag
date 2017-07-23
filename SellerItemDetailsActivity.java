package com.example.asmid.pricetag;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class SellerItemDetailsActivity extends AppCompatActivity implements OnMapReadyCallback, LocationListener {

    private static final int ACCESS_LOCATION = 0;
    private GoogleMap finalMap;
    private MapView locationMap;
    Marker marker;
    Bundle userBundle;
    private double itemLatitude;
    private double itemLongitude;
    private String url;
    private String username;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seller_item_details);

        ImageView iView= (ImageView)findViewById(R.id.item_main_image);
        Intent intent = getIntent();
        userBundle = intent.getExtras();
        Bitmap image = (Bitmap) userBundle.get("image");
        username = userBundle.getString("username");
        url = userBundle.getString("url");
        iView.setImageBitmap(image);

        locationMap = (MapView)findViewById(R.id.item_mapView);
        locationMap.onCreate(savedInstanceState);
        locationMap.getMapAsync(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitleTextColor(Color.WHITE);

        Button saveButton = (Button) findViewById(R.id.button_item_save);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                TextView textView = (TextView) findViewById(R.id.item_name_text);
                String itemName = textView.getText().toString();
                textView = (TextView) findViewById(R.id.item_desc);
                String itemDescription = textView.getText().toString();
                textView = (TextView) findViewById(R.id.item_cost_price);
                String priceString = textView.getText().toString();
                Float itemPrice = 0f;
                if(priceString.length()!=0)
                    itemPrice = Float.parseFloat(priceString);
                LatLng latlng = marker.getPosition();
                itemLatitude = latlng.latitude;
                itemLongitude = latlng.longitude;
               // final UserObject userObject = new UserObject(url,username, itemName, itemDescription, itemPrice, itemLatitude, itemLongitude);
                //new uploadImageTask().execute(userObject);
            }
        });

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        locationMap.onSaveInstanceState(outState);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == ACCESS_LOCATION) {
            if (permissions.length == 1 &&
                    permissions[0] == android.Manifest.permission.ACCESS_COARSE_LOCATION &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //find a way to show map stuff here
            } else {
                // Permission was denied. Display an error message.
            }
        }
    }

    public static String sendImage(UserObject userObject){

        InputStream inputStream = null;
        String result = "";
        String url="http://pricetagbackend-env.us-west-1.elasticbeanstalk.com/storeimage.php";
        try {

            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(url);
            JSONObject jsonObject = new JSONObject();
            jsonObject.accumulate("image_url", userObject.getUrl());
            jsonObject.accumulate("username", userObject.getUsername());
            jsonObject.accumulate("description", userObject.getItem_desc());
            jsonObject.accumulate("latitude", userObject.getItem_lat());
            jsonObject.accumulate("longitude", userObject.getItem_long());
            jsonObject.accumulate("price", userObject.getPrice());
            jsonObject.accumulate("name", userObject.getItem_name());
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
        // 11. return result
        return "";
    }

    public void goBackToOnSale(){

        Intent data = new Intent();
        data.putExtra("username", userBundle.getString("username"));
        setResult(2404, data);
        finish();
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


    public class uploadImageTask extends AsyncTask<UserObject, Void,String> {

        @Override
        protected String doInBackground(UserObject... params) {

            sendImage(params[0]);
            return null;
        }

        @Override
        protected void onPostExecute(String str) {
            super.onPostExecute(str);
            goBackToOnSale();
        }
    }

    @Override
    protected void onPause() {
        locationMap.onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        locationMap.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        locationMap.onLowMemory();
    }

    @Override
    public void onLocationChanged(Location location) {

        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        LatLng latLng = new LatLng(latitude, longitude);
        marker = finalMap.addMarker(new MarkerOptions().position(latLng));
        finalMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        finalMap.animateCamera(CameraUpdateFactory.zoomTo(15));
        itemLatitude = latitude;
        itemLongitude = longitude;
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        locationMap.onResume();
    }


    @Override
    public void onMapReady(GoogleMap map) {

        if (checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            map.setMyLocationEnabled(true);
            finalMap = map;
            LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            Criteria criteria = new Criteria();
            String bestProvider = locationManager.getBestProvider(criteria, true);
            Location location = locationManager.getLastKnownLocation(bestProvider);

            //allow user to change location of marker

            if (location != null) {
                onLocationChanged(location);
            }
        }
        else {
            requestPermissions(new String[] {  android.Manifest.permission.ACCESS_COARSE_LOCATION  }, ACCESS_LOCATION );
        }

        //this happens by default, remove once permission request processing is complete
        map.setMyLocationEnabled(true);
        map.addMarker(new MarkerOptions().position(new LatLng(-34, 151)).title("Sydney"));
        map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if(marker!=null){
                    marker.remove();
                }
                marker = finalMap.addMarker(new MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
            }
        });
    }
}
