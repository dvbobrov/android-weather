package com.dbobrov.android.weather;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.dbobrov.android.weather.network.ApiClient;

/**
 * Created with IntelliJ IDEA.
 * User: blackhawk
 * Date: 04.12.12
 * Time: 17:36
 */
public class RepeatingAlarmService extends BroadcastReceiver {
    public static final int B_ALL_WEATHER = 0;
    public void onReceive(Context context, Intent intent) {
            updateAllCities(context);
    }

    private void updateAllCities(Context context) {
        ApiClient apiClient = new ApiClient(context);
        Log.i(WeatherService.TAG, "Updating all cities");
        if (apiClient.updateWeather()) {
            Intent intent = new Intent(WeatherService.TAG);
            intent.putExtra("Data", B_ALL_WEATHER);
            context.sendBroadcast(intent);
        }
    }
}
