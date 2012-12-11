package com.dbobrov.android.weather.database;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.util.Log;
import com.dbobrov.android.weather.models.Forecast;

public class DataLayer {
    private static final String TAG = "com.dbobrov.android.WeatherDBAdapter";
    public static final String TBL_CITY = "City";
    public static final String TBL_CUR_CONDITIONS = "CurrentConditions";
    public static final String TBL_FORECAST = "Forecast";

    private final Context context;
//    private final DatabaseHelper dbHelper;
//    private SQLiteDatabase db;

    public DataLayer(Context context) {
        this.context = context;
//        dbHelper = new DatabaseHelper(context);
    }

    public DataLayer open() {
//        this.db = dbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
//        db.close();
//        db = null;
    }

    public Cursor getCities() {
//        return db.query(TBL_CITY, null, null, null, null, null, null);
        return context.getContentResolver().query(WeatherProvider.CONTENT_CITY_URI, null, null, null, null);
    }

    public Cursor getCityForecast(long cityId) {
//        return db.query(TBL_FORECAST, null, "cityId=" + cityId, null, null, null, null);
        return context.getContentResolver().query(Uri.withAppendedPath(WeatherProvider.CONTENT_FORECAST_URI,
                String.valueOf(cityId)), null, null, null, null);
    }

    public void removeCity(long cityId) {
//        db.delete(TBL_FORECAST, "cityId=" + cityId, null);
//        db.delete(TBL_CUR_CONDITIONS, "cityId=" + cityId, null);
//        db.delete(TBL_CITY, "_id=" + cityId, null);
        context.getContentResolver().delete(Uri.withAppendedPath(WeatherProvider.CONTENT_CITY_URI,
                String.valueOf(cityId)), null, null);
    }

    public Cursor getCurrentConditions(long cityId) {
//        return db.query(TBL_CUR_CONDITIONS, null, "cityId=" + cityId, null, null, null, null);
        return context.getContentResolver().query(Uri.withAppendedPath(WeatherProvider.CONTENT_CUR_CONDITIONS_URI,
                String.valueOf(cityId)), null, null, null, null);
    }

    public Cursor getCity(long cityId) {
        return context.getContentResolver().query(Uri.withAppendedPath(WeatherProvider.CONTENT_CITY_URI,
                String.valueOf(cityId)), null, null, null, null);
    }

    public long searchCity(String name, String country) {
//        Cursor cursor = db.query(TBL_CITY, new String[]{"_id"}, "name=? AND country=?", new String[]{name, country}, null, null, null);
        Cursor cursor = context.getContentResolver().query(WeatherProvider.CONTENT_CITY_URI, null,
                "name=? AND country=?", new String[]{name, country}, null);
        if (!cursor.moveToFirst()) {
            cursor.close();
            return -1;
        }
        long id = cursor.getLong(0);
        cursor.close();
        return id;
    }

    public long addCity(String name, String country) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("name", name);
        contentValues.put("country", country);
//        long id = db.insert(TBL_CITY, null, contentValues);
        Uri uri = context.getContentResolver().insert(WeatherProvider.CONTENT_CITY_URI, contentValues);
        if (uri != null)
            return Long.parseLong(uri.getLastPathSegment());
        return -1;
    }

    public boolean updateCurrentConditions(long cityId, String temperature, String pressure, String windDir, String windSpeed,
                                           String humidity, String observationTime, String iconName) {
        ContentValues contentValues = new ContentValues();

        contentValues.put("temperature", temperature);
        contentValues.put("pressure", pressure);
        contentValues.put("windDir", windDir);
        contentValues.put("windSpeed", windSpeed);
        contentValues.put("humidity", humidity);
        contentValues.put("observationTime", observationTime);
        contentValues.put("iconName", iconName);
//        int affected = db.update(TBL_CUR_CONDITIONS, contentValues, "cityId=" + cityId, null);
        int affected = context.getContentResolver().update(Uri.withAppendedPath(
                WeatherProvider.CONTENT_CUR_CONDITIONS_URI, String.valueOf(cityId)), contentValues, null, null);
        return affected != 0;
    }

    public void updateForecast(long cityId, Forecast[] forecasts) {
//        db.delete(TBL_FORECAST, "cityId=" + cityId, null);
        Uri uri = Uri.withAppendedPath(WeatherProvider.CONTENT_FORECAST_URI,
                String.valueOf(cityId));
        context.getContentResolver().delete(uri, null, null);
        for (Forecast forecast : forecasts) {
            ContentValues contentValues = new ContentValues();
            contentValues.put("cityId", cityId);
            contentValues.put("date", forecast.date);
            contentValues.put("tempMax", forecast.tempMax);
            contentValues.put("tempMin", forecast.tempMin);
            contentValues.put("windDir", forecast.windDir);
            contentValues.put("windSpeed", forecast.windSpeed);
            contentValues.put("iconName", forecast.iconName);
//            db.insert(TBL_FORECAST, null, contentValues);
            context.getContentResolver().insert(uri, contentValues);
        }
    }
}
