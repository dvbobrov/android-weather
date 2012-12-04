package com.dbobrov.android.weather.views;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import com.dbobrov.android.weather.R;
import com.dbobrov.android.weather.database.DataLayer;
import com.dbobrov.android.weather.network.ApiClient;

/**
 * Created with IntelliJ IDEA.
 * User: blackhawk
 * Date: 04.12.12
 * Time: 14:11
 */
public class WeatherFragment extends Fragment implements View.OnClickListener {
    private final int cityId;
    private final String city, country;
    private View cityView;
    private final Context context;

    // Data containers
    private TextView temperature, pressure, humidity, windDir, windSpeed, observationTime, cityName;
    private ListView forecast;
    private Button refresh;

    public WeatherFragment(int cityId, String city, String country, Context context) {
        this.cityId = cityId;
        this.city = city;
        this.country = country;
        this.context = context;
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

        cityName = getTextView(R.id.cityName);
        cityName.setText(this.city);
        return cityView;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnRefresh:
                //TODO update weather
                break;
        }
    }


    private void updateView() {
        DataLayer dataLayer = new DataLayer(context);
        dataLayer.open();
        Cursor cursor = dataLayer.getCurrentConditions(cityId);
        temperature.setText(cursor.getString(1));
        pressure.setText(cursor.getString(2));
        windDir.setText(ApiClient.windDir16PointToResourceString(cursor.getString(3)));
        windSpeed.setText(cursor.getString(4));
        humidity.setText(cursor.getString(5));
        observationTime.setText(cursor.getString(6));
        String iconName = cursor.getString(7);
        cursor.close();

    }
}
