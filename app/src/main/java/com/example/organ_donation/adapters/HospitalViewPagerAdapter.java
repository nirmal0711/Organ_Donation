package com.example.organ_donation.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import com.example.organ_donation.fragments.HospitalRequestsFragment;
import com.example.organ_donation.fragments.HospitalDonationsFragment;

public class HospitalViewPagerAdapter extends FragmentStateAdapter {
    public HospitalViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull @Override
    public Fragment createFragment(int position) {
        return position == 0 ? new HospitalRequestsFragment() : new HospitalDonationsFragment();
    }

    @Override public int getItemCount() { return 2; }
}