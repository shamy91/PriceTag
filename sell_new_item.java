package com.example.asmid.pricetag;

import android.*;
import android.Manifest;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.internal.http.multipart.MultipartEntity;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class sell_new_item extends AppCompatActivity {

    private static final int CAMERA_REQUEST = 1888;
    private static final int RESULT_LOAD_IMAGE = 1;
    private static final int RESULT_UPLOADED = 2404;
    private ImageView imageView;
    private static String username;
    private static  File photoFile;
    private static final int EXTERNAL_STORAGE = 0;
    Cloudinary cloudinary;
    Map result;
    Bitmap photo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sell_new_item);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitleTextColor(Color.WHITE);

        HashMap config = new HashMap();
        config.put("cloud_name", "pricetag");
        config.put("api_key", "679297912494948");
        config.put("api_secret", "F04pIOgXGADgjIW7_FXQY-9yl6E");
        cloudinary = new Cloudinary(config);
        Intent thisIntent = getIntent();
        username = thisIntent.getStringExtra("username");
        this.imageView = (ImageView)this.findViewById(R.id.camera_imageView);
        final Button photoButton = (Button) this.findViewById(R.id.button_camera);
        photoButton.setOnClickListener(new View.OnClickListener() {


            @Override
            public void onClick(View v) {
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_REQUEST);
            }
        });

        Button galleryButton = (Button) this.findViewById(R.id.button_gallery);
        galleryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(galleryIntent, RESULT_LOAD_IMAGE);
            }
        });

        FloatingActionButton uploadButton = (FloatingActionButton) findViewById(R.id.button_upload);
        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ImageView iv = (ImageView) findViewById(R.id.camera_imageView);
                photo = ((BitmapDrawable)iv.getDrawable()).getBitmap();
                if(photo==null){
                    Toast.makeText(getApplicationContext(),"No image selected!",Toast.LENGTH_SHORT).show();
                }
                else{
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    photo.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    uploadImagetask uploadTask = new uploadImagetask(cloudinary);
                    uploadTask.execute("");
                }

            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == EXTERNAL_STORAGE) {
            if (permissions.length == 1 &&
                    permissions[0] == Manifest.permission.WRITE_EXTERNAL_STORAGE &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            } else {
                // Permission was denied. Display an error message.
            }
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
       // super.onActivityResult(requestCode,resultCode,data);
        if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            Bitmap scaledPhoto = Bitmap.createScaledBitmap(photo, 300,300,false);
            imageView.setImageBitmap(scaledPhoto);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            photo.compress(Bitmap.CompressFormat.JPEG, 100, baos); //bitmap is required image which have to send  in Bitmap form


            String path = MediaStore.Images.Media.insertImage(getApplicationContext().getContentResolver(), photo, "title", null);
            Uri uri = Uri.parse(path);
            photoFile = new File(getRealPath(uri));
        }

        else if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            photoFile = new File(getRealPath(selectedImage));
            ImageView imageView = (ImageView) findViewById(R.id.camera_imageView);
            try {

                Bitmap photo = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImage);
                Bitmap scaledPhoto = Bitmap.createScaledBitmap(photo, 300,300,false);
                imageView.setImageBitmap(scaledPhoto);
            }
            catch (Exception e){
                e.printStackTrace();
            }

        }
        else {

            Log.d("success", data.getStringExtra("username"));
            Intent returnIntent = new Intent(sell_new_item.this, OnSale.class);
            returnIntent.putExtra("blah", "blah");
            setResult(RESULT_OK, returnIntent);
            finish();
        }
    }


    public String getRealPath(Uri uri){

        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        cursor.moveToFirst();
        int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
        return  cursor.getString(idx);
    }

    public void sendImageUrl(String url){

        Intent intent = new Intent(sell_new_item.this, SellerItemD.class);
        intent.putExtra("image", photo);
        intent.putExtra("url", url);
        intent.putExtra("username", username);
        startActivityForResult(intent, RESULT_UPLOADED);

    }
    public class uploadImagetask extends AsyncTask<String, Void, String>{

        Cloudinary mCloudinary;

        public uploadImagetask(Cloudinary cloudinary){
            super();
            mCloudinary = cloudinary;
        }

        @Override
        protected String doInBackground(String... params) {
            String response = "";

            try{

                if(photoFile!=null){
                    result = mCloudinary.uploader().upload(photoFile, ObjectUtils.emptyMap());
                    response = (String)result.get("url");

                }

            }
            catch(Exception e){

                e.printStackTrace();
            }
            return response;
        }

        @Override
        protected void onPostExecute(String result){

            sendImageUrl(result);
            Log.w("done", "done");
        }
    }
}
