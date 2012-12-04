package com.dbobrov.android.weather;

import android.content.*;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.View;
import com.dbobrov.android.weather.database.DataLayer;
import com.dbobrov.android.weather.network.ApiClient;
import com.dbobrov.android.weather.views.WeatherFragment;
import com.dbobrov.android.weather.views.WeatherPagerAdapter;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends FragmentActivity implements ServiceConnection {

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
                            if (fragment instanceof WeatherFragment) {
                                ((WeatherFragment) fragment).updateView();
                            }
                        }
                    }
                }
            }
        };
        registerReceiver(receiver, filter);
        bindService(new Intent(this, WeatherService.class), this, BIND_AUTO_CREATE);

        dataLayer = new DataLayer(this);
        viewPager = (ViewPager) findViewById(R.id.pager);
        fragments = new ArrayList<Fragment>();

        Cursor cursor = dataLayer.getCities();
        while (cursor.moveToNext()) {
            fragments.add(new WeatherFragment(cursor.getLong(0), cursor.getString(1), cursor.getString(2), this));
        }
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
}
