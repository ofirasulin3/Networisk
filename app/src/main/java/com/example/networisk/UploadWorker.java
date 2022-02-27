package com.example.networisk;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import java.util.Calendar;
import java.util.Date;


public class UploadWorker extends Worker {
    private final Context con;
    private final WifiManager wifiManager;

    public UploadWorker(
            @NonNull Context context,
            @NonNull WorkerParameters params) {
        super(context, params);
        this.con = context;
        this.wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
    }

    @Override
    public Result doWork() {

        // Do the work here--in this case, upload the files.
        startScanWorker();

        // Indicate whether the work finished successfully with the Result
        return Result.success();
    }

    public void startScanWorker() {
        if(MainActivity.getInside()==1) {
//            Toast.makeText(this.con, "Periodic Scanning", Toast.LENGTH_SHORT).show();
            Date currentTime = Calendar.getInstance().getTime();
            Log.i("UploadWorker", "working: "+currentTime.toString());
            wifiManager.startScan();
        }
    }
}
