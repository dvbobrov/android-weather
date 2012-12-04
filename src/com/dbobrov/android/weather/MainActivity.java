package com.dbobrov.android.weather;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.view.ViewPager;

public class MainActivity extends Activity {
    /**
     * Called when the activity is first created.
     */

    private ViewPager viewPager;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
    }
}
