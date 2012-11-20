package com.dbobrov.android.weather.models;

import java.io.Serializable;

public class CurrentConditions implements Serializable {
    public int temperature;
    public int pressure;
    public String windDir;
    public int windSpeed;
    public int humidity;
    public String observationTime;
    public String iconName;

    public CurrentConditions() {}

    public CurrentConditions(int temperature, int pressure, int windSpeed, int humidity, String windDir,
                             String observationTime, String iconName) {
        this.humidity = humidity;
        this.iconName = iconName;
        this.observationTime = observationTime;
        this.pressure = pressure;
        this.temperature = temperature;
        this.windDir = windDir;
        this.windSpeed = windSpeed;
    }
}
