package com.dbobrov.android.weather;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.SystemClock;
import android.util.Log;
import com.dbobrov.android.weather.network.ApiClient;

import java.util.Timer;
import java.util.TimerTask;

public class WeatherService extends Service {
    public static final String TAG = "com.dbobrov.android.WeatherSvc";
    private static final int UPDATE_INTERVAL = 30 * 60 * 1000, // 30 min
            FIRST_RUN = 1000;


    private AlarmManager alarmManager;
    private static final int ALARM_CODE = 1;

    private Intent intent;

    @Override
    public void onCreate() {
        super.onCreate();
        intent = new Intent(this, RepeatingAlarmService.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, ALARM_CODE, intent, 0);
        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + FIRST_RUN,
                UPDATE_INTERVAL, pendingIntent);
        Log.i(TAG, "Service started");
    }

    @Override
    public void onDestroy() {
        if (alarmManager != null) {
            alarmManager.cancel(PendingIntent.getBroadcast(this, ALARM_CODE, intent, 0));
        }
        Log.i(TAG, "Service destroyed");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new IService.Stub() {

            @Override
            public boolean updateWeather(long cityId) throws RemoteException {
                ApiClient apiClient = new ApiClient(WeatherService.this);
                return apiClient.updateWeather(cityId);
            }
        };
    }


}
