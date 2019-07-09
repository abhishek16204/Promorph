package com.example.android.promorph;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;

public class WorkProgress extends AppCompatActivity {
    TabLayout tabLayout;
    ViewPager viewPager;
    WorkProgressAdapter adapter;
    TodayWorkProgressFragment frag1;
    MyWorkProgressFragment frag2;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_work_progress);
        tabLayout=findViewById(R.id.tablayout);
        getSupportActionBar().setTitle(Html.fromHtml("<font color='#ffffff'>Work Progress</font>"));

        viewPager=findViewById(R.id.viewpager);
        viewPager.setOffscreenPageLimit(1);
        setupViewPager(viewPager);
        tabLayout.setupWithViewPager(viewPager);


        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int i) {
              adapter.notifyDataSetChanged();
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });



    }

    @Override
    protected void onResume() {
        super.onResume();
        viewPager.setOffscreenPageLimit(1);
    }

    private void setupViewPager(ViewPager viewPager) {
        adapter = new WorkProgressAdapter( getSupportFragmentManager());
        frag1 = new TodayWorkProgressFragment();
        frag2 = new MyWorkProgressFragment();
        adapter.addFragment(frag1,"TODAY WORK PROGRESS");
        adapter.addFragment(frag2,"MY WORK PROGRESS");
        viewPager.setAdapter(adapter);


    }
}
