package com.dbobrov.android.weather.models;

import java.io.Serializable;

public class WeatherData implements Serializable {
    public CurrentConditions currentConditions;
    public Forecast[] forecasts;

    public WeatherData(CurrentConditions currentConditions, Forecast[] forecasts) {
        this.currentConditions = currentConditions;
        this.forecasts = forecasts;
    }
}
