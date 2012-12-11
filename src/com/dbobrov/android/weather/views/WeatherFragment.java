package com.dbobrov.android.weather.views;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import com.dbobrov.android.weather.R;
import com.dbobrov.android.weather.database.DataLayer;
import com.dbobrov.android.weather.models.Forecast;
import com.dbobrov.android.weather.network.ApiClient;
import com.dbobrov.android.weather.network.IconGetter;

import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: blackhawk
 * Date: 04.12.12
 * Time: 14:11
 */
public class WeatherFragment extends Fragment implements View.OnClickListener {
    private final long cityId;
    private final String city, country;
    private View cityView;
    private final Context context;

    private static final Pattern TIME_PATTERN = Pattern.compile("^(\\d+):(\\d+) (AM|PM)$");



    public class UpdateCurrentWeather extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {
            ApiClient client = new ApiClient(context);
            return client.updateWeather(cityId);
        }

        @Override
        protected void onPostExecute(Boolean result) {
            refresh.setEnabled(true);
            if (result) {
                updateView();
            }
        }
    }


    // Data containers
    private TextView temperature, pressure, humidity, windDir, windSpeed, observationTime, cityName;
    private ImageView weatherIcon;
    private ListView forecast;
    private Button refresh;

    public WeatherFragment(long cityId, String city, String country, Context context) {
        this.cityId = cityId;
        this.city = city;
        this.country = country;
        this.context = context;
    }


    public long getCityId() {
        return cityId;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private TextView getTextView(int id) {
        return (TextView) cityView.findViewById(id);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.cityView = inflater.inflate(R.layout.city_weather, null);
        temperature = getTextView(R.id.temperature);
        pressure = getTextView(R.id.pressure);
        humidity = getTextView(R.id.humidity);
        windDir = getTextView(R.id.windDir);
        windSpeed = getTextView(R.id.windSpeed);
        observationTime = getTextView(R.id.observationTime);
        forecast = (ListView) cityView.findViewById(R.id.forecast);
        refresh = (Button) cityView.findViewById(R.id.btnRefresh);
        refresh.setOnClickListener(this);
        weatherIcon = (ImageView) cityView.findViewById(R.id.weatherIcon);
        cityName = getTextView(R.id.cityName);
        cityName.setText(this.city);
        updateView();
        return cityView;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnRefresh:
                refresh.setEnabled(false);
                cityView.findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
                new UpdateCurrentWeather().execute(null);
                break;
        }
    }


    public void updateView() {
        if (cityView == null) return;
        DataLayer dataLayer = new DataLayer(context);
        dataLayer.open();
        Cursor cursor = dataLayer.getCurrentConditions(cityId);
        if (cursor.moveToFirst()) {
            temperature.setText(cursor.getString(1));
            pressure.setText(cursor.getString(2));
            windDir.setText(ApiClient.windDir16PointToResourceString(cursor.getString(3)));
            windSpeed.setText(cursor.getString(4));
            humidity.setText(cursor.getString(5));
            String time = cursor.getString(6);
            observationTime.setText(timeOffset(time));
            String iconName = cursor.getString(7);
            IconGetter.addElement(iconName, weatherIcon);
        }
        cursor.close();
        cursor = dataLayer.getCityForecast(cityId);
        List<Forecast> forecasts = new ArrayList<Forecast>();
        while (cursor.moveToNext()) {
            forecasts.add(new Forecast(cursor.getString(1),
                    cursor.getString(2),
                    cursor.getString(3),
                    cursor.getString(5),
                    cursor.getString(6),
                    getString(ApiClient.windDir16PointToResourceString(cursor.getString(4)))));
        }
        cursor.close();
        dataLayer.close();
        ForecastListAdapter adapter = new ForecastListAdapter((Activity) context, forecasts);
        forecast.setAdapter(adapter);
        cityView.findViewById(R.id.progressBar).setVisibility(View.GONE);
    }

    private static String timeOffset(String time) {
        Matcher matcher = TIME_PATTERN.matcher(time);
        if (!matcher.find()) return time;
        int hours = Integer.parseInt(matcher.group(1));
        int minutes = Integer.parseInt(matcher.group(2));
        String ampm = matcher.group(3);
        int offset = TimeZone.getDefault().getRawOffset() / 60000;
        minutes += offset;
        hours += offset / 60;
        minutes %= 60;
        if (hours / 12 > 0) {
            hours %= 12;
            ampm = ampm.equals("AM") ? "PM" : "AM";
        }
        return String.format("%d:%d %s", hours, minutes, ampm);
    }

    public void setRefreshDisabled() {
        if (cityView != null) {
            refresh.setEnabled(false);
        }
    }
}
