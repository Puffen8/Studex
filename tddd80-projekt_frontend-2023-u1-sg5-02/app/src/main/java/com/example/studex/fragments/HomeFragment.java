package com.example.studex.fragments;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

public class HomeFragment extends Fragment {
    private RecyclerView recyclerViewForYou;
    private RecyclerView recyclerViewNewPosts;
    private RecyclerView recyclerViewFavoritePosts;
    private RecyclerView recyclerViewUnviewedPosts;
    private RecyclerView recyclerViewViewedPosts;
    private ListingAdapter listingAdapterForYou;
    private ListingAdapter listingAdapterNewPosts;
    private ListingAdapter listingAdapterFavoritePosts;
    private ListingAdapter listingAdapterUnviewedPosts;
    private ListingAdapter listingAdapterViewedPosts;
    private List<ListingData> newlistings;
    private RequestQueue listingsQueue;
    private RequestQueue favoritesQueue;
    private RequestQueue unviewedQueue;
    private RequestQueue viewedQueue;
    private static boolean isListingsRequesting = false;
    private static boolean isFavoritesRequesting = false;
    private static boolean isUnviewedRequesting = false;
    private static boolean isViewedRequesting = false;
    private View view;
    private Context context;

    public HomeFragment() {
        // Required empty public constructor
    }

    public static HomeFragment newInstance() {
        return new HomeFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_home, container, false);
        context = view.getContext();
        setVisibilityGone();
        initializeViews();
        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
        setVisibilityGone();
        cancelRequests();
    }

    private void initializeViews() {
        recyclerViewForYou = view.findViewById(R.id.for_you_content);
        recyclerViewForYou.setLayoutManager(new GridLayoutManager(getActivity(), 2));
        recyclerViewNewPosts = view.findViewById(R.id.new_posts_content);
        recyclerViewNewPosts.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, true));

        if (Authentication.getId() != null) {
            recyclerViewFavoritePosts = view.findViewById(R.id.favorite_posts_content);
            recyclerViewFavoritePosts.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
            recyclerViewUnviewedPosts = view.findViewById(R.id.unviewed_posts_content);
            recyclerViewUnviewedPosts.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
            recyclerViewViewedPosts = view.findViewById(R.id.viewed_posts_content);
            recyclerViewViewedPosts.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        }
        if (!(isListingsRequesting && isUnviewedRequesting && isViewedRequesting && isFavoritesRequesting)) {
            sendListingsRequest();
        }
    }

    private void sendListingsRequest() {

        if (!isListingsRequesting) {
            isListingsRequesting = true;
            String listingsUrl = MainActivity.getBaseURL() + "listings";
            listingsQueue = Volley.newRequestQueue(context);
            JsonArrayRequest listingsRequest = new JsonArrayRequest(Request.Method.GET, listingsUrl, null,
                    new Response.Listener<JSONArray>() {
                        @Override
                        public void onResponse(JSONArray response) {
                            Gson gson = new Gson();
                            ListingData[] data = gson.fromJson(response.toString(), ListingData[].class);

                            newlistings = new ArrayList<>();
                            Collections.addAll(newlistings, data);

                            listingAdapterNewPosts = new ListingAdapter(newlistings);
                            recyclerViewNewPosts.setAdapter(listingAdapterNewPosts);
                            recyclerViewNewPosts.scrollToPosition(newlistings.size() - 1);

                            listingAdapterForYou = new ListingAdapter(newlistings);
                            recyclerViewForYou.setAdapter(listingAdapterForYou);
                            recyclerViewForYou.stopNestedScroll();

                            setVisibilityAfterRequest();
                            isListingsRequesting = false;
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            String message = MainActivity.getErrorMessage(error);
                            Log.i("Error", message);
                            isListingsRequesting = false;

                            cancelRequests();
                            sendListingsRequest();

                        }
                    }
            );
            listingsRequest.setTag("listings");
            listingsQueue.add(listingsRequest);
        }

        if (Authentication.getId() != null) {
            if (!isFavoritesRequesting) {
                isFavoritesRequesting = true;
                String favoritesUrl = MainActivity.getBaseURL() + "listings/favorites";
                favoritesQueue = Volley.newRequestQueue(context);
                JsonArrayRequest favoritesRequest = new JsonArrayRequest(Request.Method.GET, favoritesUrl, null,
                        new Response.Listener<JSONArray>() {
                            @Override
                            public void onResponse(JSONArray response) {
                                Gson gson = new Gson();
                                ListingData[] data = gson.fromJson(response.toString(), ListingData[].class);

                                List<ListingData> favoriteListings = new ArrayList<>();
                                Collections.addAll(favoriteListings, data);
                                listingAdapterFavoritePosts = new ListingAdapter(favoriteListings);
                                recyclerViewFavoritePosts.setAdapter(listingAdapterFavoritePosts);

                                if (favoriteListings.size() > 0) {
                                    view.findViewById(R.id.Favorite_posts).setVisibility(View.VISIBLE);
                                    view.findViewById(R.id.favorite_posts_content).setVisibility(View.VISIBLE);
                                }
                                isFavoritesRequesting = false;
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                String message = MainActivity.getErrorMessage(error);
                                Log.i("Error", message);
                                isFavoritesRequesting = false;

                                cancelRequests();
                                sendListingsRequest();

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
                favoritesRequest.setTag("favorites");
                favoritesQueue.add(favoritesRequest);
            }

            if (!isUnviewedRequesting) {
                isUnviewedRequesting = true;
                String unviewedUrl = MainActivity.getBaseURL() + "listings/unviewed";
                unviewedQueue = Volley.newRequestQueue(context);
                JsonArrayRequest unviewedRequest = new JsonArrayRequest(Request.Method.GET, unviewedUrl, null,
                        new Response.Listener<JSONArray>() {
                            @Override
                            public void onResponse(JSONArray response) {
                                Gson gson = new Gson();
                                ListingData[] data = gson.fromJson(response.toString(), ListingData[].class);

                                List<ListingData> unviewedListings = new ArrayList<>();
                                Collections.addAll(unviewedListings, data);
                                listingAdapterUnviewedPosts = new ListingAdapter(unviewedListings);
                                recyclerViewUnviewedPosts.setAdapter(listingAdapterUnviewedPosts);

                                if (unviewedListings.size() > 0) {
                                    view.findViewById(R.id.unviewed_posts).setVisibility(View.VISIBLE);
                                    view.findViewById(R.id.unviewed_posts_content).setVisibility(View.VISIBLE);
                                }
                                isUnviewedRequesting = false;
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                String message = MainActivity.getErrorMessage(error);
                                Log.i("Error", message);
                                isUnviewedRequesting = false;

                                cancelRequests();
                                sendListingsRequest();

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
                unviewedRequest.setTag("unviewed");
                unviewedQueue.add(unviewedRequest);
            }

            if (!isViewedRequesting) {
                isViewedRequesting = true;
                String viewedUrl = MainActivity.getBaseURL() + "listings/viewed";
                viewedQueue = Volley.newRequestQueue(context);
                JsonArrayRequest viewedRequest = new JsonArrayRequest(Request.Method.GET, viewedUrl, null,
                        new Response.Listener<JSONArray>() {
                            @Override
                            public void onResponse(JSONArray response) {
                                Gson gson = new Gson();
                                ListingData[] data = gson.fromJson(response.toString(), ListingData[].class);

                                List<ListingData> viewedListings = new ArrayList<>();
                                Collections.addAll(viewedListings, data);
                                listingAdapterViewedPosts = new ListingAdapter(viewedListings);
                                recyclerViewViewedPosts.setAdapter(listingAdapterViewedPosts);

                                if (viewedListings.size() > 0) {
                                    view.findViewById(R.id.viewed_posts).setVisibility(View.VISIBLE);
                                    view.findViewById(R.id.viewed_posts_content).setVisibility(View.VISIBLE);
                                }
                                isViewedRequesting = false;
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                String message = MainActivity.getErrorMessage(error);
                                Log.i("Error", message);
                                isViewedRequesting = false;

                                cancelRequests();
                                sendListingsRequest();

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
                viewedRequest.setTag("viewed");
                viewedQueue.add(viewedRequest);
            }
        }
    }

    private void setVisibilityAfterRequest() {
        View view = getView();
        if (view != null) {
            view.findViewById(R.id.new_posts).setVisibility(View.VISIBLE);
            view.findViewById(R.id.new_posts_content).setVisibility(View.VISIBLE);
            view.findViewById(R.id.for_you).setVisibility(View.VISIBLE);
            view.findViewById(R.id.for_you_content).setVisibility(View.VISIBLE);
        }
    }

    private void setVisibilityGone() {
        if (Authentication.getId() == null) {
            view.findViewById(R.id.Favorite_posts).setVisibility(View.GONE);
            view.findViewById(R.id.favorite_posts_content).setVisibility(View.GONE);
            view.findViewById(R.id.unviewed_posts).setVisibility(View.GONE);
            view.findViewById(R.id.unviewed_posts_content).setVisibility(View.GONE);
            view.findViewById(R.id.new_posts).setVisibility(View.GONE);
            view.findViewById(R.id.new_posts_content).setVisibility(View.GONE);
            view.findViewById(R.id.for_you).setVisibility(View.GONE);
            view.findViewById(R.id.for_you_content).setVisibility(View.GONE);
            view.findViewById(R.id.viewed_posts).setVisibility(View.GONE);
            view.findViewById(R.id.viewed_posts_content).setVisibility(View.GONE);
        }
    }

    private void cancelRequests() {
        isListingsRequesting = false;
        isFavoritesRequesting = false;
        isUnviewedRequesting = false;
        if (listingsQueue != null) {
            listingsQueue.cancelAll("listings");
        } if (favoritesQueue != null) {
            favoritesQueue.cancelAll("favorites");
        } if (unviewedQueue != null) {
            unviewedQueue.cancelAll("unviewed");
        }
    }
}
