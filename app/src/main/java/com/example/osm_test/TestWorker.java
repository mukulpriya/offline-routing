package com.example.osm_test;

import android.content.Context;

import com.example.osm_test.MyGraph.*;


import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class TestWorker extends Worker {
    public TestWorker(
            @NonNull Context appContext,
            @NonNull WorkerParameters workerParams) {
        super(appContext, workerParams);
    }

    private MyGraph sample_graph = new MyGraph();

    @NonNull
    @Override
    public Result doWork() {

        Context applicationContext = getApplicationContext();

        try {

            sample_graph.buildGraph(applicationContext);
            sample_graph.generatePathDataFile(applicationContext);

            return Result.success();
        } catch (Throwable throwable) {

            // Technically WorkManager will return Result.failure()
            // but it's best to be explicit about it.
            // Thus if there were errors, we're return FAILURE


            return Result.failure();
        }
    }
}
