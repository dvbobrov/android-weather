package com.dbobrov.android.weather.network;

import android.content.Context;
import android.database.Cursor;
import android.location.*;
import android.util.Log;
import com.dbobrov.android.weather.R;
import com.dbobrov.android.weather.database.DataLayer;
import com.dbobrov.android.weather.models.Forecast;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.URLEncoder;
import java.util.List;
import java.util.Locale;


public class ApiClient {
    private static final String TAG = "ApiClient";
    private static final int DAY_COUNT = 3;
    private static final String API_KEY = "3c185a104f135532121811";
    private static final String WEATHER_URL = "http://free.worldweatheronline.com/feed/weather.ashx?" +
            "format=json&num_of_days=" + DAY_COUNT + "key=" + API_KEY + "&q=";

    private static final String CITY_SEARCH_URL = "http://www.worldweatheronline.com/feed/search.ashx?key=" +
            API_KEY +
            "&num_of_results=1" +
            "&format=json&popular=yes&query=";

    private final DataLayer dataLayer;
    private final Context context;

    public ApiClient(Context context) {
        dataLayer = new DataLayer(context);
        this.context = context;
    }

    private JSONObject getCurrentCityInfo() throws IOException, JSONException {
        Location location = getLocation();
        if (location == null) {
            Log.w(TAG, "Can't get current location");
            return null;
        }
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
        String city, country;
        if (addresses.size() > 0) {
            Address address = addresses.get(0);
            city = address.getLocality();
            country = address.getCountryName();
        } else {
            return null;
        }
        String requestUrl = CITY_SEARCH_URL + URLEncoder.encode(city + "," + country);
        String response = getHttpResponse(requestUrl);
        return new JSONObject(response).getJSONObject("search_api");
    }

    public long getCurrentCityId() {
        long id = -1;
        try {
            JSONObject json = getCurrentCityInfo();
            if (json != null) {
                json = json.getJSONArray("result").getJSONObject(0);
                String city = json.getJSONArray("areaName").getJSONObject(0).getString("value");
                String country = json.getJSONArray("country").getJSONObject(0).getString("value");
                dataLayer.open();
                id = dataLayer.searchCity(city, country);
                dataLayer.close();
            }
        } catch (JSONException e) {
            Log.e(TAG, "Can't download city info");
            e.printStackTrace();
        } catch (IOException e) {
            Log.e(TAG, "Error parsing city info");
            e.printStackTrace();
        }
        return id;
    }

    public boolean updateWeather() {
        dataLayer.open();
        Cursor cursor = dataLayer.getCities();
        boolean smthUpdated = false;
        while (cursor.moveToNext()) {
            smthUpdated = updateWeatherForCity(cursor);
        }
        cursor.close();
        dataLayer.close();
        return smthUpdated;
    }

    public boolean updateWeather(long cityId) {
        dataLayer.open();
        Cursor cursor = dataLayer.getCity(cityId);
        if (!cursor.moveToFirst()) {
            cursor.close();
            return false;
        }
        boolean updated = updateWeatherForCity(cursor);
        cursor.close();
        dataLayer.close();
        return updated;
    }

    public JSONObject downloadWeather(String query) throws IOException, JSONException {
        String url = WEATHER_URL + URLEncoder.encode(query);
        String httpResponse = getHttpResponse(url);
        return new JSONObject(httpResponse);
    }

    private String getHttpResponse(String url) throws IOException {
        HttpClient client = new DefaultHttpClient();
        HttpGet request = new HttpGet(url);
        HttpResponse response = client.execute(request);
        InputStream stream = response.getEntity().getContent();
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        StringBuilder builder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            builder.append(line);
        }
        stream.close();
        return builder.toString();
    }

    private Location getLocation() {
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.NO_REQUIREMENT);
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);
        criteria.setCostAllowed(true);
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        criteria.setSpeedRequired(true);
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        String provider = locationManager.getBestProvider(criteria, true);
        if (provider == null) {
            return null;
        }
        return locationManager.getLastKnownLocation(provider);
    }

    private boolean updateWeatherForCity(Cursor cursor) {
        long id = cursor.getLong(0);
        String name = cursor.getString(1);
        String country = cursor.getString(2);
        try {
            JSONObject json = downloadWeather(name + "," + country).getJSONObject("data");
            writeWeatherUpdate(id, json);
        } catch (IOException e) {
            Log.e(TAG, "Cannot download weather for " + name);
            e.printStackTrace();
            return false;
        } catch (JSONException e) {
            Log.e(TAG, "Cannot parse response for " + name);
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private void writeWeatherUpdate(long id, JSONObject json) throws JSONException {
        JSONObject currentConditions = json.getJSONArray("current_condition").getJSONObject(0);
        String iconName = currentConditions.getJSONArray("weatherIconUrl").getJSONObject(0).getString("value");
        for (int i = iconName.length() - 1; i >= 0; --i) {
            if (iconName.charAt(i) == '/') {
                iconName = iconName.substring(i + 1);
                break;
            }
        }
        dataLayer.updateCurrentConditions(id, currentConditions.getInt("temp_C"),
                currentConditions.getInt("pressure"), currentConditions.getString("winddir16Point"),
                currentConditions.getInt("windspeedKmph"), currentConditions.getInt("humidity"),
                currentConditions.getString("observation_time"), iconName);

        JSONArray forecastJson = json.getJSONArray("weather");
        Forecast[] forecasts = new Forecast[DAY_COUNT];
        for (int i = 0; i < Math.max(forecastJson.length(), DAY_COUNT); ++i) {
            JSONObject o = forecastJson.getJSONObject(i);
            iconName = o.getJSONArray("weatherIconUrl").getJSONObject(0).getString("value");
            for (int j = iconName.length() - 1; j >= 0; --j) {
                if (iconName.charAt(j) == '/') {
                    iconName = iconName.substring(j + 1);
                    break;
                }
            }
            forecasts[i] = new Forecast(o.getString("date"), o.getInt("tempMaxC"), o.getInt("tempMinC"),
                    o.getInt("windspeedKmph"), iconName, o.getString("winddir16Point"));
        }
    }

    public static int windDir16PointToResourceString(String windDir) {
        Class<R.string> stringClass = R.string.class;
        try {
            Field field = stringClass.getField("dir_" + windDir);
            return field.getInt(null);
        } catch (NoSuchFieldException e) {
            return R.string.unknown;
        } catch (IllegalAccessException e) {
            return R.string.unknown;
        }

        /*if ("N".equals(windDir))
            return R.string.dir_N;
        if ("E".equals(windDir))
            return R.string.dir_E;
        if ("W".equals(windDir))
            return R.string.dir_W;
        if ("S".equals(windDir))
            return R.string.dir_S;
        if ("NE".equals(windDir))
            return R.string.dir_NE;
        if ("NW".equals(windDir))
            return R.string.dir_NW;
        if ("SE".equals(windDir))
            return R.string.dir_SE;
        if ("SW".equals(windDir))
            return R.string.dir_SW;
        if ("NNE".equals(windDir))
            return R.string.dir_NNE;
        if ("NNW".equals(windDir))
            return R.string.dir_NNW;
        if ("NEE".equals(windDir))
            return R.string.dir_NEE;
        if ("NWW".equals(windDir))
            return R.string.dir_NWW;
        if ("SSE".equals(windDir))
            return R.string.dir_SSE;
        if ("SSW".equals(windDir))
            return R.string.dir_SSW;
        if ("SEE".equals(windDir))
            return R.string.dir_SEE;
        if ("SWW".equals(windDir))
            return R.string.dir_SWW;
        return R.string.unknown;  */
    }
}
