package com.beesham.beerac.service;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.net.URL;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Beesham on 1/30/2017.
 */

public class BeerDetailsAsyncTask extends AsyncTask<Uri, Integer, String> {

    public AsyncResponse delegate = null;

    public interface AsyncResponse{
        void processFinish(String results);
    }

    public BeerDetailsAsyncTask(AsyncResponse delegate) {
        this.delegate = delegate;
    }

    @Override
    protected String doInBackground(Uri... urls) {
        String responseStr = null;
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(urls[0].toString())
                .build();

        try {
            Response response = client.newCall(request).execute();
            responseStr = response.body().string();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return responseStr;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
    }

    @Override
    protected void onPostExecute(String results) {
        delegate.processFinish(results);
    }
}
