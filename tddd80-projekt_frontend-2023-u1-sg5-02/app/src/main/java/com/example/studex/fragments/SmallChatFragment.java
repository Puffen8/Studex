package com.example.studex.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.studex.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SmallChatFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SmallChatFragment extends Fragment {

    public SmallChatFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment SmallChatFragment.
     */

    public static SmallChatFragment newInstance() {
        SmallChatFragment fragment = new SmallChatFragment();
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
        return inflater.inflate(R.layout.fragment_small_chat, container, false);
    }
}