package com.dbobrov.android.weather.database;

import android.content.*;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.util.Log;

/**
 * Created with IntelliJ IDEA.
 * User: blackhawk
 * Date: 11.12.12
 * Time: 18:27
 */
public class WeatherProvider extends ContentProvider {
    private static final String TAG = "WeatherProvider";

    private static String AUTHORITY = "com.dbobrov.android.weather.provider";
    public static Uri CONTENT_URI = Uri.parse(ContentResolver.SCHEME_CONTENT + "://" + AUTHORITY);

    private static UriMatcher MATCHER = new UriMatcher(UriMatcher.NO_MATCH);

    private static final int C_CITIES = 0, C_CITY = 1, C_FORECAST = 2,
            C_CUR_CONDITIONS = 3, C_CITY_SEARCH = 4,
            C_CUR_CONDITIONS_INS = 5, C_FORECAST_INS = 6;

    public static final String TBL_CITY = "City";
    public static final String TBL_CUR_CONDITIONS = "CurrentConditions";
    public static final String TBL_FORECAST = "Forecast";

    static {
        MATCHER.addURI(AUTHORITY, TBL_CITY, C_CITIES);
        MATCHER.addURI(AUTHORITY, TBL_CITY + "/#", C_CITY);
        MATCHER.addURI(AUTHORITY, TBL_CITY + "/search", C_CITY_SEARCH);
        MATCHER.addURI(AUTHORITY, TBL_CUR_CONDITIONS + "/#", C_CUR_CONDITIONS);
        MATCHER.addURI(AUTHORITY, TBL_CUR_CONDITIONS, C_CUR_CONDITIONS_INS);
        MATCHER.addURI(AUTHORITY, TBL_FORECAST + "/#", C_FORECAST);
        MATCHER.addURI(AUTHORITY, TBL_FORECAST, C_FORECAST_INS);
    }

    public static Uri CONTENT_CITY_URI = Uri.withAppendedPath(CONTENT_URI, TBL_CITY);
    public static Uri CONTENT_CUR_CONDITIONS_URI = Uri.withAppendedPath(CONTENT_URI, TBL_CUR_CONDITIONS);
    public static Uri CONTENT_FORECAST_URI = Uri.withAppendedPath(CONTENT_URI, TBL_FORECAST);

    private SQLiteOpenHelper helper;


    @Override
    public boolean onCreate() {
        helper = new DatabaseHelper(this.getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String order) {
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = null;
        switch (MATCHER.match(uri)) {
            case C_CITY:
                selection = "_id=?";
                selectionArgs = new String[]{uri.getLastPathSegment()};
            case C_CITIES:
                cursor = db.query(TBL_CITY, projection, selection, selectionArgs, null, null, order);
                cursor.setNotificationUri(getContext().getContentResolver(), CONTENT_CITY_URI);
                break;
            case C_CITY_SEARCH:
                cursor = db.query(TBL_CITY, projection, selection, selectionArgs, null, null, order);
                cursor.setNotificationUri(getContext().getContentResolver(), CONTENT_CITY_URI);
                break;
            case C_FORECAST:
                selection = "cityId=?";
                selectionArgs = new String[]{uri.getLastPathSegment()};
                cursor = db.query(TBL_FORECAST, projection, selection, selectionArgs, null, null, order);
                cursor.setNotificationUri(getContext().getContentResolver(), CONTENT_FORECAST_URI);
                break;
            case C_CUR_CONDITIONS:
                selection = "cityId=?";
                selectionArgs = new String[]{uri.getLastPathSegment()};
                cursor = db.query(TBL_CUR_CONDITIONS, projection, selection, selectionArgs, null, null, order);
                cursor.setNotificationUri(getContext().getContentResolver(), CONTENT_CUR_CONDITIONS_URI);
        }
        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        SQLiteDatabase db = helper.getWritableDatabase();
        switch (MATCHER.match(uri)) {
            case C_CITIES:
                long id = db.insert(TBL_CITY, null, contentValues);
                if (id == -1) {
                    return null;
                }
                getContext().getContentResolver().notifyChange(CONTENT_URI, null);
                return Uri.withAppendedPath(CONTENT_CITY_URI, Long.toString(id));
            case C_FORECAST:
                long id1 = db.insert(TBL_FORECAST, null, contentValues);
                return id1 != -1 ? Uri.withAppendedPath(CONTENT_FORECAST_URI, contentValues.getAsString("cityId")) : null;
        }
        return null;
    }

    @Override
    public int delete(Uri uri, String where, String[] whereArgs) {
        SQLiteDatabase db = helper.getWritableDatabase();
        whereArgs = new String[]{uri.getLastPathSegment()};
        switch (MATCHER.match(uri)) {
            case C_CITY:
                db.delete(TBL_FORECAST, "cityId=?", whereArgs);
                db.delete(TBL_CUR_CONDITIONS, "cityId=?", whereArgs);
                return db.delete(TBL_CITY, "_id=?", whereArgs);
            case C_FORECAST:
                db.delete(TBL_FORECAST, "cityId=?", whereArgs);
        }
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String where, String[] whereArgs) {
        SQLiteDatabase db = helper.getWritableDatabase();
        switch (MATCHER.match(uri)) {
            case C_CUR_CONDITIONS:
                long cityId = Long.parseLong(uri.getLastPathSegment());
                int affected = db.update(TBL_CUR_CONDITIONS, contentValues, "cityId=" + cityId, null);
                if (affected == 0) {
                    contentValues.put("cityId", cityId);
                    db.insert(TBL_CUR_CONDITIONS, null, contentValues);
                    return 1;
                }
                return affected;
        }
        return 0;
    }

    private static class DatabaseHelper extends SQLiteOpenHelper {
        private static final String DB_NAME = "Weather";
        private static final int DB_VERSION = 1;
        private static final String[] DB_CREATE = new String[]{
                "CREATE TABLE " + TBL_CITY + " (_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "name TEXT NOT NULL, " +
                        "country TEXT NOT NULL);",
                "CREATE TABLE " + TBL_CUR_CONDITIONS + " (cityId INTEGER, " +
                        "temperature TEXT NOT NULL, " +
                        "pressure TEXT, " +
                        "windDir TEXT, " +
                        "windSpeed TEXT, " +
                        "humidity TEXT, " +
                        "observationTime TEXT NOT NULL, " +
                        "iconName TEXT," +
                        "FOREIGN KEY(cityId) REFERENCES " + TBL_CITY + "(_id));",
                "CREATE TABLE " + TBL_FORECAST + " (cityId INTEGER, " +
                        "date TEXT NOT NULL, " +
                        "tempMax TEXT NOT NULL, " +
                        "tempMin TEXT NOT NULL, " +
                        "windDir TEXT, " +
                        "windSpeed TEXT, " +
                        "iconName TEXT," +
                        "FOREIGN KEY(cityId) REFERENCES " + TBL_CITY + "(_id));",
                "INSERT INTO " + TBL_CITY + " (name, country) VALUES ('Saint Petersburg', 'Russia');",
                "INSERT INTO " + TBL_CITY + " (name, country) VALUES ('Chelyabinsk', 'Russia');",
                "INSERT INTO " + TBL_CITY + " (name, country) VALUES ('Moscow', 'Russia');",
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
