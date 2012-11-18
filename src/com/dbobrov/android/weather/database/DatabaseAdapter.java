package com.dbobrov.android.weather.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import com.dbobrov.android.weather.models.Forecast;

/**
 * Created with IntelliJ IDEA.
 * User: blackhawk
 * Date: 18.11.12
 * Time: 18:07
 */
public class DatabaseAdapter {
    private static final String TAG = "WeatherDBAdapter";
    private static final String TBL_CITY = "City";
    private static final String TBL_CUR_CONDITIONS = "CurrentConditions";
    private static final String TBL_FORECAST = "Forecast";

    //    private final Context context;
    private final DatabaseHelper dbHelper;
    private SQLiteDatabase db;

    public DatabaseAdapter(Context context) {
//        this.context = context;
        dbHelper = new DatabaseHelper(context);
    }

    public DatabaseAdapter open() {
        this.db = dbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        dbHelper.close();
        db.close();
    }

    public Cursor getCities() {
        return db.query(TBL_CITY, null, null, null, null, null, null);
    }

    public Cursor getCityForecast(long cityId) {
        return db.query(TBL_FORECAST, null, "cityId=" + cityId, null, null, null, null);
    }

    public Cursor getCurrentConditions(long cityId) {
        return db.query(TBL_CUR_CONDITIONS, null, "cityId=" + cityId, null, null, null, null);
    }

    public long addCity(String name, String country) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("name", name);
        contentValues.put("country", country);
        long id = db.insert(TBL_CITY, null, contentValues);
        return id;
    }

    public boolean updateCurrentConditions(long cityId, int temperature, int pressure, int windDir, int windSpeed,
                                           int humidity, String observationTime, String iconName) {
        ContentValues contentValues = new ContentValues();

        contentValues.put("temperature", temperature);
        contentValues.put("pressure", pressure);
        contentValues.put("windDir", windDir);
        contentValues.put("windSpeed", windSpeed);
        contentValues.put("humidity", humidity);
        contentValues.put("observationTime", observationTime);
        contentValues.put("iconName", iconName);
        int affected = db.update(TBL_CUR_CONDITIONS, contentValues, "cityId=" + cityId, null);
        if (affected == 0) {
            contentValues.put("cityId", cityId);
            long id = db.insert(TBL_CUR_CONDITIONS, null, contentValues);
            return id != -1;
        }
        return true;
    }

    public void updateForecast(long cityId, Forecast[] forecasts) {
        db.delete(TBL_FORECAST, "cityId=" + cityId, null);
        for (Forecast forecast: forecasts) {
            ContentValues contentValues = new ContentValues();
            contentValues.put("cityId", cityId);
            contentValues.put("date", forecast.date);
            contentValues.put("tempMax", forecast.tempMax);
            contentValues.put("tempMin", forecast.tempMin);
            contentValues.put("windDir", forecast.windDir);
            contentValues.put("windSpeed", forecast.windSpeed);
            contentValues.put("iconName", forecast.iconName);
            db.insert(TBL_FORECAST, null, contentValues);
        }
    }


    private static class DatabaseHelper extends SQLiteOpenHelper {
        private static final String DB_NAME = "Weather";
        private static final int DB_VERSION = 1;
        private static final String[] DB_CREATE = new String[]{
                "CREATE TABLE " + TBL_CITY + " (_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "name TEXT NOT NULL, " +
                        "country TEXT NOT NULL);",
                "CREATE TABLE " + TBL_CUR_CONDITIONS + " (cityId INTEGER, " +
                        "temperature INTEGER NOT NULL, " +
                        "pressure INTEGER, " +
                        "windDir INTEGER, " +
                        "windSpeed INTEGER, " +
                        "humidity INTEGER, " +
                        "observationTime TEXT NOT NULL, " +
                        "iconName TEXT," +
                        "FOREIGN KEY(cityId) REFERENCES " + TBL_CITY + "(_id));",
                "CREATE TABLE " + TBL_FORECAST + " (cityId INTEGER, " +
                        "date TEXT NOT NULL, " +
                        "tempMax INTEGER NOT NULL, " +
                        "tempMin INTEGER NOT NULL, " +
                        "windDir INTEGER, " +
                        "windSpeed INTEGER, " +
                        "iconName TEXT," +
                        "FOREIGN KEY(cityId) REFERENCES " + TBL_CITY + "(_id));"
        };


        public DatabaseHelper(Context context) {
            super(context, DB_NAME, null, DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            for (String q : DB_CREATE) {
                db.execSQL(q);
            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int from, int to) {
            Log.w(TAG, "Updating db from " + from + " to " + to);
            db.execSQL("DROP TABLE IF EXISTS " + TBL_CITY);
            db.execSQL("DROP TABLE IF EXISTS " + TBL_CUR_CONDITIONS);
            db.execSQL("DROP TABLE IF EXISTS " + TBL_FORECAST);
            onCreate(db);
        }

        @Override
        public void onOpen(SQLiteDatabase db) {
            super.onOpen(db);
            if (!db.isReadOnly()) {
                // Enable foreign key constraints
                db.execSQL("PRAGMA foreign_keys=ON;");
            }
        }
    }
}
