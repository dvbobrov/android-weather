package com.dbobrov.android.weather.network;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;
import android.util.Pair;
import android.widget.ImageView;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.ConcurrentLinkedQueue;


/**
 * Created with IntelliJ IDEA.
 * User: blackhawk
 * Date: 04.12.12
 * Time: 15:51
 */
public class IconGetter extends AsyncTask<Void, Pair<ImageView, Bitmap>, Void> {
    private static final String TAG = "IconGetter";
    private static final String ROOT_URL = "http://www.worldweatheronline.com/images/wsymbols01_png_64/";
    private static volatile IconGetter instance = null;

    private static final ConcurrentLinkedQueue<Pair<String, ImageView>> queue = new ConcurrentLinkedQueue<Pair<String, ImageView>>();


    public static void executeIfNeeded() {
        if (instance == null || !instance.getStatus().equals(Status.RUNNING)) {
            instance = new IconGetter();
            instance.execute((Void) null);
        }
    }

    public static void addElement(String iconName, ImageView view) {
        queue.add(new Pair<String, ImageView>(iconName, view));
        executeIfNeeded();
    }


    @Override
    protected Void doInBackground(Void... params) {
        while (!queue.isEmpty()) {
            Pair<String, ImageView> item = queue.poll();
            try {
                URL url = new URL(ROOT_URL + item.first);
                URLConnection connection = url.openConnection();
                connection.setUseCaches(true);
                Object response = connection.getContent();
                if (response instanceof Bitmap) {
                    publishProgress(new Pair<ImageView, Bitmap>(item.second, (Bitmap) response));
                }
            } catch (MalformedURLException e) {
                Log.e(TAG, "Invalid URL");
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(Pair<ImageView, Bitmap>... progress) {
        for (Pair<ImageView, Bitmap> pair : progress) {
            pair.first.setImageBitmap(pair.second);
        }
    }

}
