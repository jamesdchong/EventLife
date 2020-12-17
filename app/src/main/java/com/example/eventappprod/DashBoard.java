/*
    PERSONALIZED DASHBOARD OF EVENTS
 */

package com.example.eventappprod;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.tabs.TabLayout;


public class DashBoard extends AppCompatActivity {

    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dash_board);

        //Navigation Bar
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        //The navigation text/symbols will change color when the user is on that page
        bottomNavigationView.setSelectedItemId(R.id.dashboard);
        int[] colors = new int[]{
                Color.LTGRAY,
                Color.WHITE,
        };

        //Indicates which page the user is currently on
        int[][] states = new int[][]{
                new int[]{android.R.attr.state_enabled, -android.R.attr.state_checked},
                new int[]{android.R.attr.state_enabled, android.R.attr.state_checked}
        };

        //Creates the navigation bar
        bottomNavigationView.setItemTextColor(new ColorStateList(states, colors));
        bottomNavigationView.setItemIconTintList(new ColorStateList(states, colors));

        //Set dashboard as selected
        bottomNavigationView.setSelectedItemId(R.id.dashboard);

        //Perform ItemSelectedListener
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    //If dashboard button clicked, stay on dashboard
                    case R.id.dashboard:
                        return true;

                    //If profile button clicked, go to profile
                    case R.id.profile:
                        startActivity(new Intent(getApplicationContext(), Profile.class));
                        overridePendingTransition(0, 0);
                        return true;

                    //If explore button clicked, go to explore
                    case R.id.explore:
                        startActivity(new Intent(getApplicationContext(), Explore.class));
                        overridePendingTransition(0, 0);
                        return true;
                }
                return false;
            }
        });

        //Starts the dashboard fragment
        mViewPager = (ViewPager) findViewById(R.id.container);
        setupViewPager(mViewPager);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);
    }

    //Sets up dashboard page
    private void setupViewPager(ViewPager viewPager) {
        SectionPagerAdapter adapter = new SectionPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new DashBoardFrag());
        viewPager.setAdapter(adapter);
    }
}