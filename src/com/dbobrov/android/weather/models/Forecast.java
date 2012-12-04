package com.dbobrov.android.weather.models;

import java.io.Serializable;

public class Forecast implements Serializable {
    public String date;
    public String tempMax, tempMin;
    public String windSpeed;
    public String windDir;
    public String iconName;

    public Forecast() {

    }

    public Forecast(String date, String tempMax, String tempMin, String windSpeed, String iconName, String windDir) {
        this.iconName = iconName;
        this.tempMin = tempMin;
        this.windSpeed = windSpeed;
        this.windDir = windDir;
        this.date = date;
        this.tempMax = tempMax;
    }
}
