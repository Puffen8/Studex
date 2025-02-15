package com.example.studex.fragments;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.NavDirections;

import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.studex.Authentication;
import com.example.studex.ChatData;
import com.example.studex.ListingData;
import com.example.studex.MainActivity;
import com.example.studex.R;
import com.example.studex.UserData;
import com.example.studex.databinding.FragmentListingBinding;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ListingFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ListingFragment extends Fragment {
    private static boolean listingInFavorites = false;
    private ListingData listing;
    private Context context;
    private boolean existingChatFound = false;

    public ListingFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment ListingFragment.
     */

    public static ListingFragment newInstance() {
        ListingFragment fragment = new ListingFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    FragmentListingBinding binding;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        listingInFavorites = false;
        binding = FragmentListingBinding.inflate(inflater, container, false);
        context = getContext();

        ListingFragmentArgs arguments = ListingFragmentArgs.fromBundle(getArguments());
        this.listing = arguments.getListingData();
        String titleText = listing.getTitle();
        String priceText = "Price: " + listing.getPrice() + " kr";
        String locationtext = "Location: " + listing.getLocation();
        String descriptionText = "Description: \n" + listing.getDescription();

        binding.titleText.setText(titleText);
        binding.priceText.setText(priceText);
        binding.locationText.setText(locationtext);
        binding.descriptionText.setText(descriptionText);

        if (Authentication.getId() != null) {
            RequestQueue viewedQueue = Volley.newRequestQueue(context);
            String viewedUrl = MainActivity.getBaseURL() + "/listing/" + listing.getId() + "/view";
            StringRequest viewedRequest = new StringRequest(Request.Method.POST, viewedUrl, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
//                    Log.i("Success", "Successfully viewed listing");
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                }
            })  {
                @Override
                public Map<String, String> getHeaders() {
                    HashMap<String, String> headers = new HashMap<>();
                    headers.put("Authorization", "Bearer " + Authentication.getAccessTokenKey());
                    return headers;
                }
            };
            viewedQueue.add(viewedRequest);
        }


        RequestQueue ownerQueue = Volley.newRequestQueue(context);
        String ownerUrl = MainActivity.getBaseURL() + "profile/" + listing.getOwner_id();
        JsonObjectRequest ownerRequest = new JsonObjectRequest(Request.Method.GET, ownerUrl, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Gson gson = new Gson();
                UserData seller = gson.fromJson(response.toString(), UserData.class);
                String sellerText = "Seller: " + seller.getUsername().substring(0, 1).toUpperCase()
                        + seller.getUsername().substring(1);
                binding.sellerText.setText(sellerText);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                String message = MainActivity.getErrorMessage(error);
                Log.i("Error", message);
                Toast.makeText(context, message, Toast.LENGTH_LONG).show();
            }
        });
        ownerQueue.add(ownerRequest);


        if (listing.getImage() != null) {
            byte[] decodedString = Base64.decode(listing.getImage(), Base64.DEFAULT);

            Bitmap bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            binding.listingImage.setImageBitmap(bitmap);
        } else {
            binding.listingImage.setImageResource(R.drawable.ic_no_image);
        }

        if (!Authentication.isLoggedIn()) {
            binding.editOrSendButton.setVisibility(View.GONE);
        }
        else if (listing.getOwner_id().equals(Authentication.getId())) {
            binding.editOrSendButton.setText(R.string.editListing);
            Button sendMessageButton = binding.editOrSendButton;
            sendMessageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    NavController navController = MainActivity.getNavController();
                    NavDestination currentDestination = navController.getCurrentDestination();
                    if (currentDestination.getId() == R.id.listingFragment) {
                        NavDirections action = ListingFragmentDirections.actionListingFragmentToEditListingFragment(listing);
                        MainActivity.getNavController().navigate(action);
                    }
                }
            });
        } else {
            binding.editOrSendButton.setText("Send Message");
            Button editListingButton = binding.editOrSendButton;
            editListingButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String chatsUrl = MainActivity.getBaseURL() + "chats/all";
                    RequestQueue chatsQueue = Volley.newRequestQueue(context);
                    StringRequest chatsRequest = new StringRequest(Request.Method.GET, chatsUrl,
                            new Response.Listener<String>() {
                                @Override
                                public void onResponse(String response) {
                                    Gson gson = new Gson();
                                    ChatData[] data = gson.fromJson(response, ChatData[].class);

                                    for (ChatData chatData : data) {
                                        if (chatData.getListing_id().equals(listing.getId())) {
                                            // Existing chat found, navigate to the chat fragment
                                            NavController navController = MainActivity.getNavController();
                                            NavDestination currentDestination = navController.getCurrentDestination();

                                            if (currentDestination.getId() == R.id.listingFragment) {
                                                NavDirections action = ListingFragmentDirections.actionListingFragmentToChatFragment(chatData);
                                                MainActivity.getNavController().navigate(action);
                                            }
                                            existingChatFound = true;
                                            break;
                                        }
                                    }
                                    if (!existingChatFound) {
                                        createNewChat();
                                    }
                                }
                            },
                            new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    String message = MainActivity.getErrorMessage(error);
                                    Log.i("Error", message);
                                    createNewChat();
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
                    chatsQueue.add(chatsRequest);
                }
            });
        }

        if (Authentication.getId() != null) {
            binding.favoriteButton.setVisibility(View.VISIBLE);

        }

        String favoriteUrl = MainActivity.getBaseURL() + "listings/favorites";
        RequestQueue favoriteQueue = Volley.newRequestQueue(context);
        StringRequest favoriteRequest = new StringRequest(Request.Method.GET, favoriteUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Gson gson = new Gson();
                        ListingData[] data = gson.fromJson(response.toString(), ListingData[].class);

                        for (ListingData listingData : data) {
                            if (listingData.getId().equals(listing.getId())) {
                                listingInFavorites = true;
                                break;
                            }
                        }

                        switchFavoriteButton(listingInFavorites);


                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        String message = MainActivity.getErrorMessage(error);
                        Log.i("Error", message);
                        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
                        switchFavoriteButton(false);

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
        favoriteQueue.add(favoriteRequest);

        return binding.getRoot();
    }

    public void switchFavoriteButton(Boolean isFavorited) {
        listingInFavorites = false;

        if (!Authentication.isLoggedIn()) {
            binding.favoriteButton.setVisibility(View.GONE);

        } else if (isFavorited) {
            binding.favoriteButton.setText(R.string.unfavorite);
            binding.favoriteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    binding.favoriteButton.setOnClickListener(null);
                    String setFavoriteUrl = MainActivity.getBaseURL() + "listing/" + listing.getId() + "/favorite";
                    RequestQueue setFavoriteQueue = Volley.newRequestQueue(context);
                    StringRequest setFavoriteRequest = new StringRequest(Request.Method.DELETE, setFavoriteUrl,
                            new Response.Listener<String>() {
                                @Override
                                public void onResponse(String response) {
                                    Toast.makeText(context, "Listing Unfavorited", Toast.LENGTH_SHORT).show();
                                    binding.favoriteButton.setText(R.string.favorite);

                                    switchFavoriteButton(false);
                                }
                            },
                            new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    String message = MainActivity.getErrorMessage(error);
                                    Log.i("Error", message);
                                    Toast.makeText(context, message, Toast.LENGTH_LONG).show();

                                    switchFavoriteButton(false);
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

                    setFavoriteQueue.add(setFavoriteRequest);
                }
            });


        } else {
            binding.favoriteButton.setText(R.string.favorite);
            binding.favoriteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    binding.favoriteButton.setOnClickListener(null);
                    String url = MainActivity.getBaseURL() + "listing/" + listing.getId() + "/favorite";
                    RequestQueue queue = Volley.newRequestQueue(context);
                    StringRequest request = new StringRequest(Request.Method.POST, url,
                            new Response.Listener<String>() {
                                @Override
                                public void onResponse(String response) {
                                    Toast.makeText(context, "Listing Favorited", Toast.LENGTH_SHORT).show();
                                    binding.favoriteButton.setText(R.string.unfavorite);

                                    switchFavoriteButton(true);
                                }
                            },
                            new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    String message = MainActivity.getErrorMessage(error);
                                    Log.i("Error", message);
                                    Toast.makeText(context, message, Toast.LENGTH_LONG).show();

                                    switchFavoriteButton(true);
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

                    queue.add(request);
                }
            });
        }
    }
    public void createNewChat() {
        String newChatUrl = MainActivity.getBaseURL() + "listing/" + listing.getId() + "/new_chat";
        RequestQueue newChatQueue = Volley.newRequestQueue(context);
        StringRequest newChatRequest = new StringRequest(Request.Method.POST, newChatUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Gson gson = new Gson();
                        ChatData chatData = gson.fromJson(response.toString(), ChatData.class);

                        chatData.setBuyer_id(Authentication.getId());
                        NavController navController = MainActivity.getNavController();
                        NavDestination currentDestination = navController.getCurrentDestination();

                        NavDirections action = null;
                        if (currentDestination.getId() == R.id.listingFragment) {
                            action = ListingFragmentDirections.actionListingFragmentToChatFragment(chatData);

                            MainActivity.getNavController().navigate(action);
                            Toast.makeText(context, "New chat created", Toast.LENGTH_SHORT).show();
                        }


                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        String message = MainActivity.getErrorMessage(error);
                        Log.i("Error", message);
                        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
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


        newChatQueue.add(newChatRequest);

    }

    @Override
    public void onResume() {
        super.onResume();
        ListingFragmentArgs arguments = ListingFragmentArgs.fromBundle(getArguments());
        this.listing = arguments.getListingData();


        String favoritesUrl = MainActivity.getBaseURL() + "listings/favorites";
        RequestQueue favoritesQueue = Volley.newRequestQueue(context);
        StringRequest favoritesRequest = new StringRequest(Request.Method.GET, favoritesUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Gson gson = new Gson();
                        ListingData[] data = gson.fromJson(response, ListingData[].class);

                        for (ListingData listingData : data) {
                            if (listingData.getId().equals(listing.getId())) {
                                listingInFavorites = true;
                                break;
                            }
                        }
                        switchFavoriteButton(listingInFavorites);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        String message = MainActivity.getErrorMessage(error);
                        Log.i("Error", message);
                        Toast.makeText(context, message, Toast.LENGTH_LONG).show();

                        switchFavoriteButton(false);
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

        favoritesQueue.add(favoritesRequest);
    }
}