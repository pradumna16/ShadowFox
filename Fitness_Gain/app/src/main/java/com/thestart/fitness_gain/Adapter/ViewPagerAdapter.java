package com.thestart.fitness_gain.Adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.thestart.fitness_gain.DayFragment;
import com.thestart.fitness_gain.MonthFragment;
import com.thestart.fitness_gain.WeekFragment;

public class ViewPagerAdapter extends FragmentStateAdapter {
    public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new DayFragment();
            case 1:
                return new WeekFragment();
            case 2:
                return new MonthFragment();
            default:
                return new DayFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}
