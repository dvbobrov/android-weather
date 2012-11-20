package com.dbobrov.android.weather.models;

import java.io.Serializable;

public class Forecast implements Serializable {
    public String date;
    public int tempMax, tempMin;
    public int windSpeed;
    public String windDir;
    public String iconName;

    public Forecast() {

    }

    public Forecast(String date, int tempMax, int tempMin, int windSpeed, String iconName, String windDir) {
        this.iconName = iconName;
        this.tempMin = tempMin;
        this.windSpeed = windSpeed;
        this.windDir = windDir;
        this.date = date;
        this.tempMax = tempMax;
    }
}
