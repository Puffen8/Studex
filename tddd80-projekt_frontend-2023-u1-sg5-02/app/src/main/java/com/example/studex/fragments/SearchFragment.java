package com.example.studex.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.example.studex.ListingAdapter;
import com.example.studex.ListingData;
import com.example.studex.MainActivity;
import com.example.studex.R;
import com.google.gson.Gson;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SearchFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SearchFragment extends Fragment {
    private RecyclerView recyclerView;

    public SearchFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment SearchFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SearchFragment newInstance(String param1, String param2) {
        SearchFragment fragment = new SearchFragment();
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
        View view= inflater.inflate(R.layout.fragment_search, container, false);

        // Find and initialize the RecyclerView
        recyclerView = view.findViewById(R.id.searched_listings);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));

        initializeSearch(view);

        return view;
    }

    private void initializeSearch(View view) {
        // Find and initialize the search field
        EditText searchField = view.findViewById(R.id.search_bar);

        // Create a new RequestQueue
        RequestQueue searchQueue = Volley.newRequestQueue(getContext());

        // Set an onEditorActionListener for the search field to detect when the user hits the search button on their keyboard
        searchField.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    // Retrieve the search query entered by the user
                    String query = searchField.getText().toString().trim();

                    // Make a GET request to the search endpoint of your Flask backend API
                    String searchUrl = MainActivity.getBaseURL() + "/listings/search?query=" + query;
                    JsonArrayRequest searchRequest = new JsonArrayRequest(Request.Method.GET, searchUrl, null,
                            new Response.Listener<JSONArray>() {
                                @Override
                                public void onResponse(JSONArray response) {
                                    Gson gson = new Gson();
                                    ListingData[] data = gson.fromJson(response.toString(), ListingData[].class);

                                    // Convert the JSON response to a List of ListingData objects
                                    List<ListingData> listings = new ArrayList<>();
                                    for (ListingData listingData : data) {
                                        listings.add(listingData);
                                    }

                                    // Set up the RecyclerView adapter with the search results
                                    ListingAdapter adapter = new ListingAdapter(listings);
                                    recyclerView.setAdapter(adapter);
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

                    // Add the request to the queue
                    searchQueue.add(searchRequest);

                    // Clear the search field
                    searchField.setText("");

                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        initializeSearch(getView());
    }

}