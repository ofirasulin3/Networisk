package com.example.networisk;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
//import kotlinx.android.synthetic.main.activity_main.*;




public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        buttonOn.setOnClickListener(){
//            switchWIFI(true);
//        }
//        buttonOn.setOnClickListener(OnClickListener(){
//            @Override
//            public void onClick(View v) {
//                // TODO Auto-generated method stub
//
//            }
//        });
//
//        buttonOFF.setOnClickListener{
//            switchWIFI(false);
//        }
    }

//    Button buttonOn = (Button) findViewById(R.id.buttonOn);
//    Button buttonOff = (Button) findViewById(R.id.buttonOFF);

//    buttonOn.setOnClickListener( new OnClickListener() {
//        @Override
//        public void onClick(View v) {
//            // TODO Auto-generated method stub
//                ***Do what you want with the click here***
//        }
//    });

    public void switchWIFI(Boolean isON) {
        // if it is Android Q and above go for the newer approach
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Intent panelIntent = new Intent(Settings.Panel.ACTION_WIFI);
            startActivityForResult(panelIntent, 1);
        } else {
            WifiManager wifi_mngr =
                    (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            wifi_mngr.setWifiEnabled(isON);
        }
    }

    public void switchWifiOn(View view) {
        switchWIFI(true);
    }
    public void switchWifiOff(View view) {
        switchWIFI(false);
    }

}