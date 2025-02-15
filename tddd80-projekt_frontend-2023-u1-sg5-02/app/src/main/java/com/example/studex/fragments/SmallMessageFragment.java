package com.example.studex.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.studex.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SmallMessageFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SmallMessageFragment extends Fragment {

    public SmallMessageFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment SmallMessageFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SmallMessageFragment newInstance(String param1, String param2) {
        SmallMessageFragment fragment = new SmallMessageFragment();
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
        return inflater.inflate(R.layout.fragment_small_message, container, false);
    }
}