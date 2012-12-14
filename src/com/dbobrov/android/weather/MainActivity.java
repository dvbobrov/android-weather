package com.dbobrov.android.weather;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.*;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import com.dbobrov.android.weather.database.WeatherProvider;
import com.dbobrov.android.weather.models.City;
import com.dbobrov.android.weather.network.ApiClient;
import com.dbobrov.android.weather.views.WeatherFragment;
import com.dbobrov.android.weather.views.WeatherPagerAdapter;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends FragmentActivity implements DialogInterface.OnClickListener, ViewPager.OnPageChangeListener {

    private ViewPager viewPager;
    private LinearLayout left, right;

    //    private DataLayer dataLayer;
    private List<Fragment> fragments;

    private BroadcastReceiver receiver;
//    private IService service;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        long start = System.nanoTime();
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
//        bindService(intent, this, BIND_NOT_FOREGROUND);
        long end = System.nanoTime();
        Log.i("com.dbobrov.android.weather", "Service bind: " + (end - start) / 1000000);
        start = end;
//        dataLayer = new DataLayer(this).open();
        viewPager = (ViewPager) findViewById(R.id.pager);
        fragments = new ArrayList<Fragment>();

//        Cursor cursor = dataLayer.getCities();
        Cursor cursor = managedQuery(WeatherProvider.CONTENT_CITY_URI, null, null, null, null);
        end = System.nanoTime();
        Log.i("com.dbobrov.android.weather", "Query completed: " + (end - start) / 1000000);
        start = end;
        left = (LinearLayout) findViewById(R.id.indicatorLeft);
        right = (LinearLayout) findViewById(R.id.indicatorRight);
        boolean addCircles = false;
        while (cursor.moveToNext()) {
            fragments.add(new WeatherFragment(cursor.getLong(0), cursor.getString(1), this));
            if (addCircles) {
                ImageView v = new ImageView(this);
                v.setPadding(2, 2, 2, 2);
                v.setImageResource(R.drawable.circle);
                right.addView(v);
            }
            addCircles = true;
        }
        cursor.close();
//        dataLayer.close();
        viewPager.setAdapter(new WeatherPagerAdapter(getSupportFragmentManager(), fragments));
        viewPager.setOnPageChangeListener(this);
        end = System.nanoTime();
        Log.i("com.dbobrov.android.weather", "Fragments displayed: " + (end - start) / 1000000);
    }

   /* @Override
    public void onServiceConnected(ComponentName componentName, IBinder binder) {
        service = IService.Stub.asInterface(binder);
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        service = null;
    }*/

    private static final int M_ADD_CITY = 0, M_DEL_CITY = 1, M_CHANGE_INTERVAL = 2;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(0, M_ADD_CITY, 0, R.string.add_city).setIcon(android.R.drawable.ic_menu_add);
        menu.add(0, M_DEL_CITY, 0, R.string.del_city).setIcon(android.R.drawable.ic_menu_delete);
        menu.add(0, M_CHANGE_INTERVAL, 0, R.string.change_interval).setIcon(android.R.drawable.ic_menu_edit);
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
                getContentResolver().delete(Uri.withAppendedPath(WeatherProvider.CONTENT_CITY_URI, String.valueOf(id)), null, null);
                Toast.makeText(this, R.string.deleted, Toast.LENGTH_SHORT).show();
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
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        switch (id) {
            case D_ADD_CITY:
                if (dialogText == null) {
                    dialogText = new EditText(this);
                    dialogText.setHint(R.string.enter_city);
                }
                LinearLayout.LayoutParams params =
                        new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT,
                                LinearLayout.LayoutParams.FILL_PARENT);
                dialogText.setLayoutParams(params);
                builder.setView(dialogText);
                builder.setPositiveButton(android.R.string.ok, this)
                        .setNegativeButton(android.R.string.cancel, this);
                return builder.create();
            case D_CHANGE_INTERVAL:
                builder.setItems(R.array.intervals, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which) {
                        long interval = getResources().getIntArray(R.array.interval_values)[which];
                        interval *= 60000L;
                        SharedPreferences.Editor editor = PreferenceManager
                                .getDefaultSharedPreferences(MainActivity.this).edit();
                        editor.putLong("updateInterval", interval);
                        editor.commit();
                        Intent svc = new Intent(MainActivity.this, WeatherService.class);
                        stopService(svc);
                        startService(svc);
                    }
                });
                return builder.create();
        }
        return super.onCreateDialog(id);
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int which) {
        switch (which) {
            case DialogInterface.BUTTON_POSITIVE:
                String query = dialogText.getText().toString();
                new CitySearcher().execute(query);
                break;
            case DialogInterface.BUTTON_NEGATIVE:
                break;
        }
        dialogText = null;
    }

    @Override
    public void onPageScrolled(int i, float v, int i2) {
    }

    @Override
    public void onPageSelected(int pos) {
        int leftCount = left.getChildCount();
        int rightCount = right.getChildCount();
        if (leftCount == pos - 1) {
            // Scrolled right
            View v = right.getChildAt(rightCount - 1);
            right.removeViewAt(rightCount - 1);
            left.addView(v);
        } else if (leftCount == pos + 1) {
            // Scrolled right
            View v = left.getChildAt(leftCount - 1);
            left.removeViewAt(leftCount - 1);
            right.addView(v);
        }
    }

    @Override
    public void onPageScrollStateChanged(int i) {

    }

    private class CitySearcher extends AsyncTask<String, Void, String[]> {

        ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            dialog = ProgressDialog.show(MainActivity.this, "Loading", null);
            dialog.show();
        }

        @Override
        protected String[] doInBackground(String... param) {
            ApiClient client = new ApiClient(MainActivity.this);
            List<String> result = client.searchCity(param[0]);
            return result.toArray(new String[result.size()]);
        }

        @Override
        protected void onPostExecute(final String[] result) {
            if (dialog != null && dialog.isShowing()) {
                dialog.dismiss();
            }
            if (result == null) {
                Toast.makeText(MainActivity.this, "Nothing found", Toast.LENGTH_SHORT).show();
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setItems(result, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which) {
                        new AddCity().execute(result[which]);
                    }
                });
                builder.create().show();
            }


        }
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
                Fragment fragment = new WeatherFragment(city.id, city.name, MainActivity.this);
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
                ImageView v = new ImageView(MainActivity.this);
                v.setImageResource(R.drawable.circle);
                v.setPadding(2, 2, 2, 2);
                right.addView(v);
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
    }
}
