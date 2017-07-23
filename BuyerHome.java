package com.example.asmid.pricetag;

import android.content.ClipData;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.internal.zzc;
import com.google.android.gms.nearby.messages.Distance;
import com.google.maps.android.ui.IconGenerator;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static android.R.attr.fillColor;
import static android.R.attr.strokeColor;
import static com.example.asmid.pricetag.R.id.icon;
import static com.example.asmid.pricetag.R.id.map;
import static com.example.asmid.pricetag.R.id.seekBar;

public class BuyerHome extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback, GoogleMap.OnMarkerClickListener, LocationListener {

    private static final int ACCESS_LOCATION = 0;
    GoogleMap finalMap;
    private MapView locationMap;
    Marker userMarker;
    int seekDistance;
    int seekPrice;
    HashMap<Marker, Integer> markerMap;
    private String username;
    private String category;
    SharedPreferences sharedPrefs;
    SharedPreferences.Editor sharedPrefEditor;
    boolean windowQuery;
    List<LatLng> polygonMarkers;
    int pointCount;
    Polygon mapPolygon;
    HashMap<Marker, Integer> polyMap;
    private Circle circle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buyer_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(toolbar);
        Intent intent = getIntent();
        username = intent.getStringExtra("username");

        pointCount=0;
        markerMap = new HashMap<>();
        polyMap=new HashMap<>();
        polygonMarkers = new ArrayList<>();
        locationMap = (MapView) findViewById(R.id.buyer_mapView);
        locationMap.onCreate(savedInstanceState);
        locationMap.getMapAsync(this);
        windowQuery=false;

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        final SeekBar DistanceSeekBar  = (SeekBar) findViewById(R.id.seekBar);
        final SeekBar PriceSeekBar = (SeekBar) findViewById(R.id.seekBar2);

        final TextView distV = (TextView)findViewById(R.id.distanceValue);
        final TextView priceV = (TextView)findViewById(R.id.priceValue);


        DistanceSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                distV.setText(""+progress);
                if(circle!=null) {
                    circle.setRadius(progress * 1609.34);
                    circle.setStrokeColor(Color.BLUE);
                    circle.setStrokeWidth(5);
                    circle.setCenter(userMarker.getPosition());
                }
                else {
                    circle = finalMap.addCircle(new CircleOptions()
                            .center(userMarker.getPosition())
                            .radius(progress*1609.34).strokeWidth(5).strokeColor(Color.BLUE).fillColor(Color.TRANSPARENT));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


        PriceSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                priceV.setText(""+progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {


            }
        });

        final FloatingActionButton floatingActionButton = (FloatingActionButton) findViewById(R.id.fab);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                LatLng latlng = userMarker.getPosition();
                double buyerLatitude = latlng.latitude;
                double buyerLongitude = latlng.longitude;
                ItemObject itemObject = new ItemObject(buyerLatitude, buyerLongitude,0);
                seekDistance = DistanceSeekBar.getProgress();
                seekPrice = PriceSeekBar.getProgress();
                new mapAsyncTask().execute(itemObject);
            }
        });


        FloatingActionButton windowButton = (FloatingActionButton) findViewById(R.id.fab_window);
        windowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                windowQuery=true;
                pointCount=0;
                if(circle!=null)
                    circle.setRadius(0);
            }
        });


        FloatingActionButton clearWindow = (FloatingActionButton) findViewById(R.id.clear_window);
        clearWindow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                windowQuery=false;
                category=null;
                pointCount=0;
                DistanceSeekBar.setProgress(0);
                PriceSeekBar.setProgress(0);
                if(circle!=null)
                    circle.setRadius(0);
                if(mapPolygon!=null)
                    mapPolygon.remove();
                for(Map.Entry<Marker, Integer> entry: markerMap.entrySet()){
                    Marker m = entry.getKey();
                    m.remove();
                }
                for(Map.Entry<Marker, Integer> entry: polyMap.entrySet()){
                    Marker m = entry.getKey();
                    m.remove();
                }
                if(markerMap.size()>0)
                 markerMap.clear();
                if(polyMap.size()>0)
                    polyMap.clear();
                if(polygonMarkers.size()>0)
                    polygonMarkers.clear();

            }
        });

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.buyer_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.electronics || id==R.id.home|| id==R.id.clothes ) {
            return true;
        }
        else{

            category = String.valueOf(item.getTitle());
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_fav) {
            Intent intent = new Intent(BuyerHome.this, FavoritesActivity.class);
            intent.putExtra("username",username);
            startActivity(intent);
        } else if (id == R.id.nav_pending) {
            Intent intent = new Intent(BuyerHome.this, PendingItemsActivity.class);
            intent.putExtra("username",username);
            startActivity(intent);
        } else if (id == R.id.nav_bought) {
            Intent intent = new Intent(BuyerHome.this, BoughtItemsActivity.class);
            intent.putExtra("username",username);
            startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onLocationChanged(Location location) {

        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        LatLng latLng = new LatLng(latitude, longitude);
        userMarker = finalMap.addMarker(new MarkerOptions().position(latLng));
        if(circle!=null) {
            circle.setRadius(0);
            circle.setStrokeColor(Color.BLUE);
            circle.setStrokeWidth(5);
        }
        circle = finalMap.addCircle(new CircleOptions()
                .center(userMarker.getPosition())
                .radius(1000).strokeWidth(5).strokeColor(Color.BLUE).fillColor(Color.TRANSPARENT));

        sharedPrefs = getSharedPreferences(username,0);
        sharedPrefEditor = sharedPrefs.edit();
        String latlng = String.valueOf(userMarker.getPosition());
        latlng = latlng.split(":")[1];
        sharedPrefEditor.putString("location",latlng);
        sharedPrefEditor.commit();
        finalMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        finalMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 12.0f));
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {


        if (checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            googleMap.setMyLocationEnabled(true);
            finalMap = googleMap;
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
        googleMap.setMyLocationEnabled(true);
        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if(windowQuery==false){

                    if(userMarker!=null){
                        userMarker.remove();
                    }
                    userMarker = finalMap.addMarker(new MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
                    if(circle!=null)
                        circle.setRadius(0);
                    circle = finalMap.addCircle(new CircleOptions()
                            .center(userMarker.getPosition())
                            .radius(1000));
                    sharedPrefs = getSharedPreferences(username,0);
                    sharedPrefEditor = sharedPrefs.edit();
                    sharedPrefEditor.putString("location",String.valueOf(userMarker.getPosition()));
                    sharedPrefEditor.commit();
                }
                else {

                    Marker newPoint = finalMap.addMarker(new MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE)));
                    polygonMarkers.add(pointCount,newPoint.getPosition());
                    polyMap.put(newPoint, pointCount);
                    pointCount++;
                    if(pointCount>2){

                        Iterable<LatLng> iter = polygonMarkers;
                        if(mapPolygon!=null)
                            mapPolygon.remove();
                        mapPolygon = finalMap.addPolygon(new PolygonOptions()
                                .addAll(iter).add(polygonMarkers.get(0))
                                .fillColor(Color.TRANSPARENT)
                                .strokeColor(Color.BLUE)
                                .strokeWidth(5));
                        }
                }
            }
        });
        googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {

                if(marker.getSnippet()!=null){

                    try {
                        //call Activity
                        return true;
                    }catch (Exception e){
                        Log.d("Exception", e.getMessage());
                    }

                }
                return false;
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
    protected void onResume() {
        super.onResume();
        locationMap.onResume();
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

    public void addItemMarkers(List<ItemObject> itemObjectList){

        for(ItemObject itemObject: itemObjectList){

            LatLng latLng = new LatLng(itemObject.getLatitude(),itemObject.getLongitude());
            TextView textView = new TextView(getApplicationContext());
            textView.setText(itemObject.getItemName() + "\n$" + itemObject.getItemPrice());
            textView.setTextColor(Color.BLACK);
            textView.setPadding(10,10,10,10);
            textView.setTypeface(Typeface.DEFAULT_BOLD);

            IconGenerator iconGenerator = new IconGenerator(getApplicationContext());
            iconGenerator.setBackground(getApplicationContext().getDrawable(R.drawable.bubble_shadow));
            iconGenerator.setColor(Color.LTGRAY);

            iconGenerator.setContentView(textView);
            Bitmap icon = iconGenerator.makeIcon();
            Marker itemMarker = finalMap.addMarker(new MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.fromBitmap(icon)));
            finalMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(Marker marker) {

                    if(markerMap.containsKey(marker)){

                        int itemId = markerMap.get(marker);
                        LatLng userLoc = userMarker.getPosition();
                        LatLng itemLoc = marker.getPosition();
                        ItemMarkerObject itemMarkerObject = new ItemMarkerObject(itemId, userLoc, itemLoc);
                        new markerAsyncTask().execute(itemMarkerObject);
                    }
                    return false;
                }
            });
            JSONObject jsonObject = new JSONObject();
            try {
                itemMarker.setSnippet("$"+String.valueOf(itemObject.getItemPrice()));
                itemMarker.showInfoWindow();


            }
            catch (Exception e){
                Log.d("Err", e.getMessage());
            }
            markerMap.put(itemMarker, itemObject.getItemId());
        }
    }


    public List<ItemObject> getWindowItems(ItemObject itemObject){

        InputStream inputStream = null;
        String result = "";
        List<ItemObject> items = new ArrayList<ItemObject>();

        String url="http://pricetagbackend-env.us-west-1.elasticbeanstalk.com/getWindowItems.php";
        try {

            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(url);
            JSONObject jsonObject = new JSONObject();
            jsonObject.accumulate("username", username);
            if(category!=null)
                jsonObject.accumulate("category", category);
            if(seekPrice!=0)
                jsonObject.accumulate("price", seekPrice);
            List<String> points = new ArrayList<>();
            for(LatLng latLng: polygonMarkers){

                points.add(latLng.toString().split(":")[1]);
            }
            points.add(polygonMarkers.get(0).toString().split(":")[1]);
            JSONArray jsonAraay = new JSONArray(points);
            jsonObject.accumulate("points",jsonAraay);
            String json = jsonObject.toString();
            StringEntity se = new StringEntity(json);
            httpPost.setEntity(se);
            HttpResponse httpResponse = httpclient.execute(httpPost);
            inputStream = httpResponse.getEntity().getContent();
            if(inputStream != null){
                result = convertInputStreamToString(inputStream);
                JSONArray jsonArray = new JSONArray(result);
                for(int k=0; k<jsonArray.length(); k++) {
                    JSONObject jsonObject1 = jsonArray.getJSONObject(k);
                    String item_id = (String)jsonObject1.get("item_id");
                    String item_location  = (String)jsonObject1.get("item_location");
                    String loc = item_location.substring(1, item_location.length()-1);
                    String[] lvalues = loc.split(",");
                    String itemName = (String)jsonObject1.getString("name");
                    String price = (String)jsonObject1.getString("cost_price");
                    ItemObject itemObject1;
                    itemObject1 = new ItemObject(Double.parseDouble(lvalues[0]),Double.parseDouble(lvalues[1]),Integer.parseInt(item_id), itemName, Float.parseFloat(price));
                    items.add(itemObject1);

                }
                return items;
            }
            else{
                Log.d("Error", "Error");
                return null;
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        return items;
    }

    public List<ItemObject> getLocationItems(ItemObject itemObject){


        InputStream inputStream = null;
        String result = "";
        List<ItemObject> items = new ArrayList<ItemObject>();

        String url="http://pricetagbackend-env.us-west-1.elasticbeanstalk.com/getLocationItems.php";

        try {

            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(url);
            JSONObject jsonObject = new JSONObject();
            jsonObject.accumulate("username", username);
            jsonObject.accumulate("latitude", itemObject.getLatitude());
            jsonObject.accumulate("longitude", itemObject.getLongitude());
            if(seekDistance!=0)
                jsonObject.accumulate("distance", seekDistance);
            if(seekPrice!=0)
                jsonObject.accumulate("price", seekPrice);
            if(category!=null)
                jsonObject.accumulate("category", category);
            String json = jsonObject.toString();
            StringEntity se = new StringEntity(json);
            httpPost.setEntity(se);
            HttpResponse httpResponse = httpclient.execute(httpPost);
            inputStream = httpResponse.getEntity().getContent();
            if(inputStream != null){
                result = convertInputStreamToString(inputStream);
                JSONArray jsonArray = new JSONArray(result);
                for(int k=0; k<jsonArray.length(); k++) {
                    JSONObject jsonObject1 = jsonArray.getJSONObject(k);
                    String item_id = (String)jsonObject1.get("item_id");
                    String item_location  = (String)jsonObject1.get("item_location");
                    String loc = item_location.substring(1, item_location.length()-1);
                    String[] lvalues = loc.split(",");
                    String itemName = (String)jsonObject1.getString("name");
                    String price = (String)jsonObject1.getString("cost_price");
                    ItemObject itemObject1;
                    itemObject1 = new ItemObject(Double.parseDouble(lvalues[0]),Double.parseDouble(lvalues[1]),Integer.parseInt(item_id), itemName, Float.parseFloat(price));
                    items.add(itemObject1);

                }
                return items;
            }
            else{
                Log.d("Error", "Error");
                return null;
            }

        } catch (Exception e) {
            Log.d("InputStream", e.getLocalizedMessage());
        }
        return items;
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

    @Override
    public boolean onMarkerClick(Marker marker) {

        return false;
    }

    public class mapAsyncTask extends AsyncTask<ItemObject, Void, List<ItemObject>> {

        @Override
        protected List<ItemObject> doInBackground(ItemObject... params) {
            ItemObject itemObject = params[0];
            List<ItemObject>itemList;
            if(windowQuery==true && polygonMarkers.size()>2){
               itemList = getWindowItems(itemObject);
            }
            else {
                itemList = getLocationItems(itemObject);
            }
            return itemList;
        }

        @Override
        protected void onPreExecute(){

            try{
            }
            catch (Exception e){
                Log.w("Progress",e.getMessage());
            }
        }

        @Override
        protected void onPostExecute(List<ItemObject> itemObjects) {
            super.onPostExecute(itemObjects);
            addItemMarkers(itemObjects);
        }
    }

    private void showItemDetails(String response) throws JSONException {


        JSONObject json = new JSONObject(response);
        JSONObject item = json.getJSONObject("item");
        JSONArray images = json.getJSONArray("images");
        String distance = json.getString("miles");
        String seller = json.getString("seller");
        Intent intent = new Intent(this, ItemDetails.class);
        intent.putExtra("itemId", item.getString("item_id"));
        intent.putExtra("itemName", item.getString("name"));
        intent.putExtra("itemPrice", item.getString("cost_price"));
        intent.putExtra("itemDescription", item.getString("description"));
        intent.putExtra("distance", distance);
        intent.putExtra("username", username);
        intent.putExtra("seller", seller);
        for(int i=0; i<images.length(); i++){

            JSONObject img = images.getJSONObject(i);
            String imgURL = img.getString("image_url");
            intent.putExtra("url"+i, imgURL);
        }

        startActivity(intent);

    }

    private String getItemDetails(ItemMarkerObject imo) {

        InputStream inputStream = null;
        String result = "";
        String url="http://pricetagbackend-env.us-west-1.elasticbeanstalk.com/getBuyerItem.php";
        try {

            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(url);
            JSONObject jsonObject = new JSONObject();
            jsonObject.accumulate("userLat", imo.getUserLoc().latitude);
            jsonObject.accumulate("userLong", imo.getUserLoc().longitude);
            jsonObject.accumulate("itemLat", imo.getItemLoc().latitude);
            jsonObject.accumulate("itemLong", imo.getItemLoc().longitude);
            jsonObject.accumulate("itemId", imo.getItemId());

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
                Log.d("Error", "Error");
                return null;
            }

        } catch (Exception e) {
            Log.d("InputStream", e.getLocalizedMessage());
        }

        return null;
    }

    public class markerAsyncTask extends AsyncTask<ItemMarkerObject, Void, String> {

        @Override
        protected String doInBackground(ItemMarkerObject... params) {
            ItemMarkerObject imo = params[0];
            String response = getItemDetails(imo);

            return response;
        }

        @Override
        protected void onPreExecute(){

            try{
            }
            catch (Exception e){
                Log.w("Progress",e.getMessage());
            }
        }

        @Override
        protected void onPostExecute(String response) {
            super.onPostExecute(response);
            try {
                showItemDetails(response);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

}
