package com.anonymous.makanga.makanga;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class CustomerLoginActivity extends AppCompatActivity {
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private ViewPagerAdapter viewPagerAdapter;
    private List<String> tabList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_registration);

        tabLayout = findViewById(R.id.tabs);
        viewPager = findViewById(R.id.viewpager);

        tabList = new ArrayList<>();
        tabList.add("Log In");
        tabList.add("Register");

        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), tabList);
        viewPager.setAdapter(viewPagerAdapter);
        tabLayout.setupWithViewPager(viewPager);
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {

        private List<String> mFragmentTitleList;

        public ViewPagerAdapter(FragmentManager manager, List<String> tabList) {
            super(manager);
            this.mFragmentTitleList = tabList;
        }

        @Override
        public Fragment getItem(int position) {
            Bundle bundle;
            switch (position) {

                case 0:
                    LoginFragment loginFragment = new LoginFragment();
                    bundle = new Bundle();
                    bundle.putString("userType", "Customers");
                    loginFragment.setArguments(bundle);
                    return loginFragment;

                case 1:

                    SignUpFragment signUpFragment = new SignUpFragment();
                    bundle = new Bundle();
                    bundle.putString("userType", "Customers");
                    signUpFragment.setArguments(bundle);
                    return signUpFragment;
            }

            return null;
        }

        @Override
        public int getCount() {
            return mFragmentTitleList.size();
        }


        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }

}
