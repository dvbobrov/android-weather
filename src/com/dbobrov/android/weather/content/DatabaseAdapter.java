package com.dbobrov.android.weather.content;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created with IntelliJ IDEA.
 * User: blackhawk
 * Date: 18.11.12
 * Time: 18:07
 */
public class DatabaseAdapter {

    private static final String DB_NAME = "Weather";
    private static final int DB_VERSION = 1;
    private static final String TAG = "WeatherDBAdapter";
    private static final String TBL_CITY = "City";
    private static final String TBL_CUR_CONDITIONS = "CurrentConditions";
    private static final String TBL_FORECAST = "Forecast";
    private static final String[] DB_CREATE = new String[]{
            "CREATE TABLE " + TBL_CITY + " (_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "title TEXT NOT NULL, " +
                    "country TEXT NOT NULL, " +
                    "latitude REAL, " +
                    "longitude REAL);",
            "CREATE TABLE " + TBL_CUR_CONDITIONS + " (city_id INTEGER, " +
                    "temperature INTEGER NOT NULL, " +
                    "pressure INTEGER, " +
                    "wind_dir INTEGER, " +
                    "wind_speed INTEGER, " +
                    "humidity INTEGER, " +
                    "observation_time TEXT NOT NULL, " +
                    "icon_name TEXT," +
                    "FOREIGN KEY(city_id) REFERENCES " + TBL_CITY + "(_id));",
            "CREATE TABLE " + TBL_FORECAST + " (city_id INTEGER, " +
                    "date TEXT NOT NULL, " +
                    "temp_max INTEGER NOT NULL, " +
                    "temp_min INTEGER NOT NULL, " +
                    "wind_dir INTEGER, " +
                    "wind_speed INTEGER, " +
                    "icon_name TEXT," +
                    "FOREIGN KEY(city_id) REFERENCES " + TBL_CITY + "(_id));"
    };

    private final Context context;
    private final DatabaseHelper dbHelper;
    private SQLiteDatabase db;

    public DatabaseAdapter(Context context) {
        this.context = context;
        dbHelper = new DatabaseHelper(context);
    }

    public DatabaseAdapter open() {
        this.db = dbHelper.getWritableDatabase();
        return this;
    }


    private static class DatabaseHelper extends SQLiteOpenHelper {

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
    }
}
