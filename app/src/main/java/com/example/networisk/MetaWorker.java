package com.example.networisk;

import android.content.Context;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class MetaWorker extends Worker {
    private static int numWorker = 0;

    public MetaWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @Override
    public Result doWork() {
        // Do the work here--in this case, upload the files.
        startWorker();
        // Indicate whether the work finished successfully with the Result
        return Result.success();
    }

    private void startWorker() {
        PeriodicWorkRequest myUploadWork = new PeriodicWorkRequest.Builder(UploadWorker.class, 15, TimeUnit.MINUTES).build();
        WorkManager
                .getInstance(getApplicationContext())
                .enqueueUniquePeriodicWork(
                        "UploadWork"+String.valueOf(numWorker),
                        ExistingPeriodicWorkPolicy.REPLACE,
                        myUploadWork
                );
        numWorker++;
        numWorker %= 5;
    }



}
