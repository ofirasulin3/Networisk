//package com.example.networisk;
//
//
//import android.content.Context;
//import android.net.wifi.WifiManager;
//import android.widget.Toast;
//
//import androidx.annotation.NonNull;
//import androidx.work.PeriodicWorkRequest;
//import androidx.work.WorkRequest;
//import androidx.work.Worker;
//import androidx.work.WorkerParameters;
//
//import java.util.concurrent.TimeUnit;
//
//public class UploadWorker extends Worker {
//    private final WifiManager wifiManager;
//
//    public UploadWorker(
//            @NonNull Context context,
//            @NonNull WorkerParameters params,
//            WifiManager wifiManager) {
//        super(context, params);
//        this.wifiManager = wifiManager;
//    }
//
//    @Override
//    public Result doWork() {
//
//        // Do the work here--in this case, upload the files.
//        uploadImages();
//
//        // Indicate whether the work finished successfully with the Result
//        return Result.success();
//    }
//
//    private void uploadImages() {
//        if(MainActivity.getInside()==1) {
//            Toast.makeText(MainActivity.this, "scanning", Toast.LENGTH_SHORT).show();
//            wifiManager.startScan();
//        }
//    }
//}
//
//
