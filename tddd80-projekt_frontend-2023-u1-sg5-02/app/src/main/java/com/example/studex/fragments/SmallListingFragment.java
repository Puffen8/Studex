package com.example.studex.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.studex.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SmallListingFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SmallListingFragment extends Fragment {

    public SmallListingFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment SmallListingFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SmallListingFragment newInstance(String param1, String param2) {
        SmallListingFragment fragment = new SmallListingFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_small_listing, container, false);
    }
}