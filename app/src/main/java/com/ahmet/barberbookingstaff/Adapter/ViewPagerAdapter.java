package com.ahmet.barberbookingstaff.Adapter;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.ahmet.barberbookingstaff.Fragments.AddBarberFragment;
import com.ahmet.barberbookingstaff.Fragments.AddSalonFragment;
import com.ahmet.barberbookingstaff.Fragments.EmailFragment;

public class ViewPagerAdapter extends FragmentPagerAdapter {


    public ViewPagerAdapter(FragmentManager fragmentManager) {
        super(fragmentManager);
    }

    @Override
    public Fragment getItem(int position) {

        switch (position){
            case 0:
                return EmailFragment.getInstance();
            case 1:
                return AddSalonFragment.getInstance();
            case 2:
                return AddBarberFragment.getInstance();
        }
        return null;
    }

    @Override
    public int getCount() {
        return 3;
    }
}