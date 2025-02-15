package com.example.studex.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;


import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.studex.Authentication;
import com.example.studex.MainActivity;
import com.example.studex.R;
import com.example.studex.UserData;
import com.example.studex.databinding.FragmentProfileBinding;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProfileFragment extends Fragment {
    public ProfileFragment() {
        // Required empty public constructor

    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment ProfileFragment.
     */

    public static ProfileFragment newInstance() {
        ProfileFragment fragment = new ProfileFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    FragmentProfileBinding binding;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentProfileBinding.inflate(inflater, container, false);

        binding.credentialsButton.setOnClickListener(view2 ->{
            NavController navController = Navigation.findNavController(getActivity(), R.id.navHostFragment2);
            NavDestination currentDestination = navController.getCurrentDestination();

            if (currentDestination.getId() == R.id.noDisplay) {
                navController.navigate(R.id.action_noDisplay_to_credentialsFragment);
            } else if (currentDestination.getId() == R.id.credentialsFragment) {

            } else {
                navController.navigate(R.id.action_listingsFragment_to_credentialsFragment);
            }
        });

        binding.listingsButton.setOnClickListener(view2 ->{
            NavController navController = Navigation.findNavController(getActivity(), R.id.navHostFragment2);
            NavDestination currentDestination = navController.getCurrentDestination();


                if (currentDestination.getId() == R.id.noDisplay) {
                    ListingsFragment.setShowListingType(0);
                    navController.navigate(R.id.action_noDisplay_to_listingsFragment);
                } else if (currentDestination.getId() == R.id.listingsFragment && ListingsFragment.getShowListingType() == 0) {

                } else if (currentDestination.getId() == R.id.listingsFragment && ListingsFragment.getShowListingType() == 1) {
                    ListingsFragment.setShowListingType(0);
                    navController.navigate(R.id.action_listingsFragment_self);
                } else {
                    navController.navigate(R.id.action_credentialsFragment_to_listingsFragment);
                }
        });

        binding.favoritesButton.setOnClickListener(view2 ->{
            NavController navController = Navigation.findNavController(getActivity(), R.id.navHostFragment2);
            NavDestination currentDestination = navController.getCurrentDestination();


            if (currentDestination.getId() == R.id.noDisplay) {
                ListingsFragment.setShowListingType(1);
                navController.navigate(R.id.action_noDisplay_to_listingsFragment);
            } else if (currentDestination.getId() == R.id.listingsFragment && ListingsFragment.getShowListingType() == 0) {
                ListingsFragment.setShowListingType(1);
                navController.navigate(R.id.action_listingsFragment_self);
            } else if (currentDestination.getId() == R.id.listingsFragment && ListingsFragment.getShowListingType() == 1) {

            } else {
                ListingsFragment.setShowListingType(1);
                navController.navigate(R.id.action_credentialsFragment_to_listingsFragment);
            }
        });

        binding.logOutButton.setOnClickListener(view2 ->{
            MainActivity.revokeToken();
        });

        String profileUrl = MainActivity.getBaseURL() + "profile";
        RequestQueue profileQueue = Volley.newRequestQueue(getContext());
        StringRequest profileRequest = new StringRequest(Request.Method.GET, profileUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Gson gson = new Gson();
                        UserData data = gson.fromJson(response, UserData.class);
                        String profileText = data.getUsername().substring(0,1).toUpperCase() + data.getUsername().substring(1) + "'s Profile";
                        binding.profileText.setText(profileText);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        String message = MainActivity.getErrorMessage(error);
                        Log.i("Error", message);
                        Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + Authentication.getAccessTokenKey());
                return headers;
            }
        };
        profileQueue.add(profileRequest);

        return binding.getRoot();
    }
}