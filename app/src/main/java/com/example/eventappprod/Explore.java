package com.example.eventappprod;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TableLayout;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.tabs.TabLayout;

public class Explore extends AppCompatActivity {

    private SectionPagerAdapter mSectionPagerAdapter;

    private ViewPager mViewPager;

    //Initialize and properly set the contentView (View)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        //Initialize and assign variable
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        // The navigation text/symbols will change color when you are on that page
        bottomNavigationView.setSelectedItemId(R.id.dashboard);
        int[] colors = new int[] {
                Color.LTGRAY,
                Color.WHITE,
        };

        int [][] states = new int [][]{
                new int[] { android.R.attr.state_enabled, -android.R.attr.state_checked},
                new int[] {android.R.attr.state_enabled, android.R.attr.state_checked}
        };

        //Visually update to color states as specified (View)
        bottomNavigationView.setItemTextColor(new ColorStateList(states, colors));
        bottomNavigationView.setItemIconTintList(new ColorStateList(states, colors));

        //Set search as selected
        bottomNavigationView.setSelectedItemId(R.id.explore);

        //Perform ItemSelectedListener (Controller)
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.dashboard:
                        startActivity(new Intent(getApplicationContext()
                                ,DashBoard.class));
                        overridePendingTransition(0,0);
                        return true;
                    case R.id.profile:
                        startActivity(new Intent(getApplicationContext()
                                ,Profile.class));
                        overridePendingTransition(0,0);
                        return true;
                    case R.id.explore:
                        return true;
                }
                return false;
            }
        });

        mSectionPagerAdapter = new SectionPagerAdapter(getSupportFragmentManager());

        mViewPager = (ViewPager) findViewById(R.id.container);
        setupViewPager(mViewPager);

        //Set the visual variables (View)
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        tabLayout.getTabAt(0).setIcon(R.drawable.ic_find_friends);
        tabLayout.getTabAt(1).setIcon(R.drawable.ic_dashboard);
    }

    //Initalize the view pager (Controller)
    private void setupViewPager(ViewPager viewPager) {
        SectionPagerAdapter adapter = new SectionPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new FindFriendsFrag());
        adapter.addFragment(new FindEventsFrag());
        viewPager.setAdapter(adapter);
    }

}
