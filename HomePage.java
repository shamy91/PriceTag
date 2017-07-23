package com.example.asmid.pricetag;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;

public class HomePage extends AppCompatActivity {

    Intent oldIntent;
    SharedPreferences sharedPreferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        Button buy = (Button) findViewById(R.id.button_buy);
        Button sell = (Button) findViewById(R.id.button_sell);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitleTextColor(Color.WHITE);
        oldIntent = getIntent();
        sharedPreferences = getSharedPreferences(oldIntent.getStringExtra("username"), 0);

        buy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(HomePage.this, BuyerHome.class);
                intent.putExtra("username", oldIntent.getStringExtra("username"));
                startActivity(intent);

            }
        });

        sell.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                Intent intent = new Intent(HomePage.this, SellerHome.class);
                intent.putExtra("username", oldIntent.getStringExtra("username"));
                startActivity(intent);
            }
        });
    }
}
