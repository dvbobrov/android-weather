package com.dbobrov.android.weather;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import com.dbobrov.android.weather.network.ApiClient;

import java.util.Timer;
import java.util.TimerTask;

public class WeatherService extends Service {
    private static final String TAG = "WeatherSvc";
    private static final int UPDATE_INTERVAL = 30 * 60 * 1000; // 30 min
    private static final int DELAY = 1000;
    private Timer timer;

    public static final int B_ALL_WEATHER = 0;

    private Intent intent;

    @Override
    public void onCreate() {
        super.onCreate();
        intent = new Intent(TAG);
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                updateAllCities();
            }
        }, DELAY, UPDATE_INTERVAL);
        Log.i(TAG, "Service started");
    }

    @Override
    public void onDestroy() {
        if (timer != null) {
            timer.cancel();
        }
        Log.i(TAG, "Service destroyed");
    }

    public IBinder onBind(Intent intent) {
        return new IService.Stub() {

            @Override
            public boolean updateWeather(long cityId) throws RemoteException {
                ApiClient apiClient = new ApiClient(WeatherService.this);
                return apiClient.updateWeather(cityId);
            }
        };
    }

    private void updateAllCities() {
        ApiClient apiClient = new ApiClient(this);
        if (apiClient.updateWeather()) {
            intent.putExtra("Data", B_ALL_WEATHER);
            sendBroadcast(intent);
        }
    }
}
