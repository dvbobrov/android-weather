package com.dbobrov.android.weather;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.*;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.dbobrov.android.weather.database.DataLayer;
import com.dbobrov.android.weather.models.City;
import com.dbobrov.android.weather.network.ApiClient;
import com.dbobrov.android.weather.views.WeatherFragment;
import com.dbobrov.android.weather.views.WeatherPagerAdapter;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends FragmentActivity implements ServiceConnection, DialogInterface.OnClickListener {

    private ViewPager viewPager;

    private DataLayer dataLayer;
    private List<Fragment> fragments;

    private BroadcastReceiver receiver;
    private IService service;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        IntentFilter filter = new IntentFilter();
        filter.addAction(WeatherService.TAG);
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(WeatherService.TAG)) {
                    int data = intent.getIntExtra("Data", -1);
                    if (data == RepeatingAlarmService.B_ALL_WEATHER) {
                        for (Fragment fragment : fragments) {
                            if (fragment instanceof WeatherFragment && fragment.isVisible()) {
                                ((WeatherFragment) fragment).updateView();
                            }
                        }
                    }
                }
            }
        };
        registerReceiver(receiver, filter);
        Intent intent = new Intent(this, WeatherService.class);
        startService(intent);
        bindService(intent, this, BIND_NOT_FOREGROUND);

        dataLayer = new DataLayer(this).open();
        viewPager = (ViewPager) findViewById(R.id.pager);
        fragments = new ArrayList<Fragment>();

        Cursor cursor = dataLayer.getCities();
        while (cursor.moveToNext()) {
            fragments.add(new WeatherFragment(cursor.getLong(0), cursor.getString(1), cursor.getString(2), this));
        }
        cursor.close();
        dataLayer.close();
        viewPager.setAdapter(new WeatherPagerAdapter(getSupportFragmentManager(), fragments));
    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder binder) {
        service = IService.Stub.asInterface(binder);
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        service = null;
    }

    private static final int M_ADD_CITY = 0, M_DEL_CITY = 1, M_CHANGE_INTERVAL = 2;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(0, M_ADD_CITY, 0, R.string.add_city);
        menu.add(0, M_DEL_CITY, 0, R.string.del_city);
        menu.add(0, M_CHANGE_INTERVAL, 0, R.string.change_interval);
        return false;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch (item.getItemId()) {
            case M_ADD_CITY:
                showDialog(D_ADD_CITY);
                break;
            case M_DEL_CITY:
                WeatherFragment fragment = (WeatherFragment) fragments.get(viewPager.getCurrentItem());
                long id = fragment.getCityId();
                dataLayer.open().removeCity(id);
                dataLayer.close();
                Toast.makeText(this, R.string.deleted, Toast.LENGTH_SHORT).show();
//                fragment.setRefreshDisabled();
                fragments.remove(fragment);
                viewPager.getAdapter().notifyDataSetChanged();
                break;
            case M_CHANGE_INTERVAL:
                showDialog(D_CHANGE_INTERVAL);
                break;
        }
        return true;
    }

    private static final int D_ADD_CITY = 0, D_CHANGE_INTERVAL = 1;
    private EditText dialogText;

    @Override
    public Dialog onCreateDialog(int id) {

        switch (id) {
            case D_ADD_CITY:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                dialogText = new EditText(this);
                LinearLayout.LayoutParams params =
                        new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT,
                                LinearLayout.LayoutParams.FILL_PARENT);
                dialogText.setLayoutParams(params);
                builder.setView(dialogText);
                builder.setPositiveButton("Ok", this)
                        .setNegativeButton("Cancel", this);
                return builder.create();
            case D_CHANGE_INTERVAL:
                AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
                dialogText = new EditText(this);

                LinearLayout.LayoutParams params1 =
                        new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT,
                                LinearLayout.LayoutParams.FILL_PARENT);
                dialogText.setLayoutParams(params1);
                dialogText.setHint(R.string.interval_hint);
                builder1.setView(dialogText);
                DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        switch (i) {
                            case DialogInterface.BUTTON_POSITIVE:
                                int intervalMin = -1;
                                try {
                                    intervalMin = Integer.parseInt(dialogText.getText().toString());
                                } catch (NumberFormatException ignore) {
                                }
                                if (intervalMin < 0) {
                                    Toast.makeText(MainActivity.this,
                                            "Please, enter a positive number", Toast.LENGTH_SHORT).show();
                                    break;
                                }
                                long newInterval = intervalMin * 1000L * 60L;

                                SharedPreferences.Editor editor = PreferenceManager
                                        .getDefaultSharedPreferences(MainActivity.this).edit();

                                editor.putLong("updateInterval", newInterval);
                                editor.commit();
                                break;
                            case DialogInterface.BUTTON_NEGATIVE:
                                break;
                        }
                        dialogText = null;
                    }
                };
                builder1.setPositiveButton("Ok", listener)
                        .setNegativeButton("Cancel", listener);
                return builder1.create();
        }
        return super.onCreateDialog(id);
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int which) {
        switch (which) {
            case DialogInterface.BUTTON_POSITIVE:
                String query = dialogText.getText().toString();
                new AddCity().execute(query);
                break;
            case DialogInterface.BUTTON_NEGATIVE:
                break;
        }
        dialogText = null;
    }

    private class AddCity extends AsyncTask<String, Void, Boolean> {
        ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            dialog = ProgressDialog.show(MainActivity.this, "Loading", null);
            dialog.show();
        }

        @Override
        protected Boolean doInBackground(String... param) {
            String query = param[0];
            ApiClient client = new ApiClient(MainActivity.this);
            City city = client.tryAddCity(query);
            if (city != null) {
                Fragment fragment = new WeatherFragment(city.id, city.name, city.country, MainActivity.this);
                fragments.add(fragment);
                return true;
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean result) {

            if (dialog != null && dialog.isShowing()) {
                dialog.dismiss();
            }
            if (result) {
                PagerAdapter adapter = viewPager.getAdapter();
                adapter.notifyDataSetChanged();
//                viewPager.setAdapter(adapter);
                Toast.makeText(MainActivity.this, R.string.city_added, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this, R.string.error, Toast.LENGTH_SHORT).show();
            }

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (receiver != null) {
            unregisterReceiver(receiver);
        }
        unbindService(this);
    }
}
