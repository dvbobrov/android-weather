package com.dbobrov.android.weather.network;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.util.Pair;
import android.widget.ImageView;

import java.io.IOException;
import java.io.InputStream;
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
    private static final String TAG = "com.dbobrov.android.IconGetter";
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
                InputStream response = connection.getInputStream();
                Bitmap bitmap = BitmapFactory.decodeStream(response);

                publishProgress(new Pair<ImageView, Bitmap>(item.second, bitmap));

            } catch (MalformedURLException e) {
                Log.e(TAG, "Invalid URL");
            } catch (IOException e) {
//                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                Log.e(TAG, "Cannot download image");
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
