package com.example.asmid.pricetag;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

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
import java.util.ArrayList;
import android.os.AsyncTask;
import android.widget.Toast;

import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import com.google.android.gms.maps.MapView;


public class SignupForm extends AppCompatActivity implements OnMapReadyCallback, LocationListener{

    private static final int ACCESS_LOCATION = 0;
    GoogleMap finalMap;
    Marker marker;
    private MapView locationMap;
    ArrayList<String> userdetails = new ArrayList<String>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup_form);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitleTextColor(Color.WHITE);

        locationMap = (MapView) findViewById(R.id.mapView);
        locationMap.onCreate(savedInstanceState);
        locationMap.getMapAsync(this);

        FloatingActionButton submit = (FloatingActionButton) findViewById(R.id.submit);

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                TextView tv;
                tv =(TextView) findViewById(R.id.text_first_name);
                TextView tv1 = (TextView) findViewById(R.id.text_last_name);
                String firstname  = String.valueOf(tv.getText());
                String lastname = String.valueOf(tv1.getText());
                tv =((TextView) findViewById(R.id.text_username));
                String username = String.valueOf(tv.getText());
                String email = String.valueOf(((TextView) findViewById(R.id.text_email)).getText());
                String phone =  String.valueOf(((TextView) findViewById(R.id.text_phone)).getText());
                String password =  String.valueOf(((TextView) findViewById(R.id.text_password)).getText());
                Location loc = finalMap.getMyLocation();
                double latitude = loc.getLatitude();
                double longitude = loc.getLongitude();


                try{
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.accumulate("fname", firstname);
                    jsonObject.accumulate("lname", lastname);
                    jsonObject.accumulate("email", email);
                    jsonObject.accumulate("uname", username);
                    jsonObject.accumulate("phone", phone);
                    jsonObject.accumulate("password", password);
                    jsonObject.accumulate("latitude", latitude);
                    jsonObject.accumulate("longitude", longitude);
                    String userdata = jsonObject.toString();
                    new myasynctask().execute(userdata);

                }
                catch (Exception e){

                    Log.d("Exception", e.getMessage());
                }
            }
        });
    }


    @Override
    public void onLocationChanged(Location location) {

        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        LatLng latLng = new LatLng(latitude, longitude);
        marker = finalMap.addMarker(new MarkerOptions().position(latLng));
        finalMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        finalMap.animateCamera(CameraUpdateFactory.zoomTo(15));

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
        marker = map.addMarker(new MarkerOptions().position(new LatLng(-34, 151)).title("Sydney"));
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
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        locationMap.onSaveInstanceState(outState);
    }



    public static String createUser(String userdata){

        InputStream inputStream = null;
        String result = "";
        String url="http://pricetagbackend-env.us-west-1.elasticbeanstalk.com/signup.php";
        try {

            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(url);
            StringEntity se = new StringEntity(userdata);
            httpPost.setEntity(se);
            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-type", "application/json");
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

    private static String convertInputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while((line = bufferedReader.readLine()) != null)
            result += line;
        inputStream.close();
        return result;
    }

    public void showToastMessage(String response){

        if(response.equals("Success")){

            Toast.makeText(getApplicationContext(),"Congrats! Your profile was created.",Toast.LENGTH_LONG).show();
            Intent intent = new Intent(SignupForm.this, LoginActivity.class);
            startActivity(intent);
        }
        else if(response.equals("Failure")){

            Toast.makeText(getApplicationContext(),"Oops! This username is already taken. Try again.",Toast.LENGTH_LONG).show();
        }
        else if(response.equals("Error")){

            Toast.makeText(getApplicationContext(),"Error creating profile!",Toast.LENGTH_LONG).show();
        }
        else{

            Toast.makeText(getApplicationContext(),"Server error",Toast.LENGTH_LONG).show();
        }
    }
    public class myasynctask extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute(){

            try{
            }
            catch (Exception e){
                Log.w("Progress",e.getMessage());
            }
        }
        @Override
        protected String doInBackground(String... params) {

            String response = createUser(params[0]);
            return response;
        }

        @Override
        protected void onPostExecute(String str) {
            super.onPostExecute(str);
            showToastMessage(str);
        }
    }
}
