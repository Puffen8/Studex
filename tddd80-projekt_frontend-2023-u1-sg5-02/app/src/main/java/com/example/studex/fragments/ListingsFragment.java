package com.example.studex.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.example.studex.Authentication;
import com.example.studex.ListingAdapter;
import com.example.studex.ListingData;
import com.example.studex.MainActivity;
import com.example.studex.R;
import com.google.gson.Gson;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ListingsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ListingsFragment extends Fragment {
    private RecyclerView recyclerView;
    private ListingAdapter listingAdapter;

    private static int showListingType = 0;

    public ListingsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment ListingsFragment.
     */
    public static ListingsFragment newInstance() {
        ListingsFragment fragment = new ListingsFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_listings, container, false);

        initializeViews(view);

        return view;
    }

    public static void setShowListingType(int value) {
        showListingType = value;
    }

    public static int getShowListingType() {
        return showListingType;
    }

    public void initializeViews(View view) {
        recyclerView = view.findViewById(R.id.listings_view);
        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 3));
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 2);
        recyclerView.setLayoutManager(gridLayoutManager);

        if (showListingType == 0) {
            String listingsUrl = MainActivity.getBaseURL() + "/listings/user/" + Authentication.getUsername();
            RequestQueue listingsQueue = Volley.newRequestQueue(getContext());
            JsonArrayRequest listingsRequest = new JsonArrayRequest(Request.Method.GET, listingsUrl, null,
                    new Response.Listener<JSONArray>() {
                        @Override
                        public void onResponse(JSONArray response) {
                            Gson gson = new Gson();
                            ListingData[] data = gson.fromJson(response.toString(), ListingData[].class);

                            List<ListingData> listings = new ArrayList<>();

                            // Setting all of the ListingData to the listings list
                            Collections.addAll(listings, data);
                            listingAdapter = new ListingAdapter(listings);

                            recyclerView.setAdapter(listingAdapter);
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
            );
            listingsQueue.add(listingsRequest);

        } else if (showListingType == 1) {
            String favoritesUrl = MainActivity.getBaseURL() + "/listings/favorites";
            RequestQueue favoritesQueue = Volley.newRequestQueue(getContext());
            JsonArrayRequest favoritesRequest = new JsonArrayRequest(Request.Method.GET, favoritesUrl, null,
                    new Response.Listener<JSONArray>() {
                        @Override
                        public void onResponse(JSONArray response) {
                            Gson gson = new Gson();
                            ListingData[] data = gson.fromJson(response.toString(), ListingData[].class);

                            List<ListingData> listings = new ArrayList<>();

                            // Setting all of the listingData to the listings list
                            Collections.addAll(listings, data);
                            listingAdapter = new ListingAdapter(listings);

                            recyclerView.setAdapter(listingAdapter);
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
            ){
                @Override
                public Map<String, String> getHeaders() {
                    HashMap<String, String> headers = new HashMap<>();
                    headers.put("Authorization", "Bearer " + Authentication.getAccessTokenKey());
                    return headers;
                }
            };
            favoritesQueue.add(favoritesRequest);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        initializeViews(getView());
    }
}