package com.example.studex.fragments;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.NavDirections;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.studex.Authentication;
import com.example.studex.ChatData;
import com.example.studex.ChatMessagesAdapter;
import com.example.studex.ListingData;
import com.example.studex.MainActivity;
import com.example.studex.MessageData;
import com.example.studex.R;
import com.example.studex.UserData;
import com.example.studex.databinding.FragmentChatBinding;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ChatFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ChatFragment extends Fragment {
    private Context context;
    private ChatMessagesAdapter messagesAdapter;
    private RecyclerView messagesAreaRecycler;
    private ChatFragmentArgs arguments;
    private String listingId;
    private String chatId;
    private ChatData chat;
    private UserData buyerUserData;
    private UserData sellerUserData;
    private ListingData listingData;
    private boolean isSeller;
    private List<MessageData> messageDataList = new ArrayList<>();
    private int dataLoadedCounter = 0;
    private boolean isDataLoaded = false;
    private boolean messageIsSending = false;
    private Handler mHandler;
    private static final int UPDATE_INTERVAL = 2000; // Interval in milliseconds

    public ChatFragment() {
        // Required empty public constructor
    }

    public static ChatFragment newInstance() {
        ChatFragment fragment = new ChatFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    FragmentChatBinding binding;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentChatBinding.inflate(inflater, container, false);
        context = getContext();

        dataLoadedCounter = 0;
        isDataLoaded = false;
        messagesAreaRecycler = binding.messagesAreaRecycler;
        messagesAreaRecycler.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));

        arguments = ChatFragmentArgs.fromBundle(getArguments());
        chat = arguments.getChatData();

        chatId = chat.getId();
        listingId = chat.getListing_id();

        mHandler = new Handler();
        startMessageUpdateTimer();

        initializeView();

        return binding.getRoot();
    }

    private void updateMessages() {
        RequestQueue messagesQueue = Volley.newRequestQueue(context);
        String messagesUrl = MainActivity.getBaseURL() + "messages/" + chatId;
        StringRequest messagesRequest = new StringRequest(Request.Method.GET, messagesUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Gson gson = new Gson();
                        MessageData[] messageData = gson.fromJson(response.toString(), MessageData[].class);
                        if (messageData.length != messageDataList.size()) {
                            messageDataList = Arrays.asList(messageData);
                            if (!isDataLoaded) { dataLoadedCounter++; }
                            checkDataLoaded();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        String message = MainActivity.getErrorMessage(error);
                        Log.i("Error", message);
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
        messagesQueue.add(messagesRequest);
    }

    private void checkDataLoaded() {
        // Check if all data requests have been completed
        if (dataLoadedCounter == 4 && messageDataList != null && buyerUserData != null && sellerUserData != null && listingData != null) {
            isDataLoaded = true;

            // Set chat data
            String titleText = getString(R.string.title) + listingData.getTitle();
            String priceText = getString(R.string.price) + listingData.getPrice() + " kr";
            binding.titleText.setText(titleText);
            binding.priceText.setText(priceText);

            if (listingData.getImage() != null) {
                // Convert image string to bitmap using Base64
                byte[] decodedString = Base64.decode(listingData.getImage(), Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                binding.chatListingImage.setImageBitmap(bitmap);
            }

            // Set seller
            if (isSeller) {
                binding.userText.setText(R.string.your_listing);
                messagesAdapter = new ChatMessagesAdapter(messageDataList, context, buyerUserData);
            } else {
                String userText = "Seller: " + sellerUserData.getUsername().
                        substring(0, 1).toUpperCase() + sellerUserData.getUsername().substring(1);
                binding.userText.setText(userText);
                messagesAdapter = new ChatMessagesAdapter(messageDataList, context, sellerUserData);
            }

            // Set messages
            messagesAreaRecycler.setAdapter(messagesAdapter);
            messagesAdapter.notifyDataSetChanged();

            // Scroll to bottom
            messagesAreaRecycler.scrollToPosition(messageDataList.size() - 1);
        }
    }

    private void initializeView() {
        dataLoadedCounter = 0;
        isDataLoaded = false;
        messagesAreaRecycler = binding.messagesAreaRecycler;
        messagesAreaRecycler.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));

        try {
            arguments = ChatFragmentArgs.fromBundle(getArguments());
            chat = arguments.getChatData();

            chatId = chat.getId();
            listingId = chat.getListing_id();
            if (chat.getBuyer_id().equals(Authentication.getId())) {
                isSeller = false;
            } else {
                isSeller = true;
            }
        } catch (RuntimeException e) {
            e.printStackTrace();
        }

        // Get listing data
        RequestQueue listingQueue = Volley.newRequestQueue(context);
        String listingUrl = MainActivity.getBaseURL() + "listing/" + listingId;
        JsonObjectRequest listingRequest = new JsonObjectRequest(Request.Method.GET, listingUrl, null,
                new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Gson gson = new Gson();
                listingData = gson.fromJson(response.toString(), ListingData.class);
                dataLoadedCounter++;
                checkDataLoaded();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                String message = MainActivity.getErrorMessage(error);
                Log.i("Error", message);
                Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
            }
        });
        listingQueue.add(listingRequest);


        // Get buyer data
        RequestQueue buyerQueue = Volley.newRequestQueue(context);
        String buyerUrl = MainActivity.getBaseURL() + "profile/" + chat.getBuyer_id();
        JsonObjectRequest buyerRequest = new JsonObjectRequest(Request.Method.GET, buyerUrl, null,
                new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Gson gson = new Gson();
                buyerUserData = gson.fromJson(response.toString(), UserData.class);
                dataLoadedCounter++;
                checkDataLoaded();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                String message = MainActivity.getErrorMessage(error);
                Log.i("Error", message);
                Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
            }
        });
        buyerQueue.add(buyerRequest);


        // Get seller data
        RequestQueue sellerQueue = Volley.newRequestQueue(context);
        String sellerUrl = MainActivity.getBaseURL() + "profile/" + chat.getSeller_id();
        JsonObjectRequest sellerRequest = new JsonObjectRequest(Request.Method.GET, sellerUrl, null,
                new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                Gson gson = new Gson();
                sellerUserData = gson.fromJson(response.toString(), UserData.class);
                dataLoadedCounter++;
                checkDataLoaded();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                String message = MainActivity.getErrorMessage(error);
                Log.i("Error", message);
                Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
            }
        });
        sellerQueue.add(sellerRequest);

        // Get messages
        updateMessages();

        View sendButton = binding.buttonSendMessage;

        // listener for send button click
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!messageIsSending) {
                    String message = binding.messageInputTextview.getText().toString();

                    if (message.isEmpty()) {
                        Toast.makeText(context, "Message is empty!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String sender_id = Authentication.getId();

                    // Create JsonObject to send
                    MessageData messageData = new MessageData(message, sender_id, chatId);

                    Gson gson = new Gson();
                    String json = gson.toJson(messageData);

                    JSONObject jsonRequest= null;
                    try {
                        jsonRequest = new JSONObject(json);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    String sendMessageUrl = MainActivity.getBaseURL() + "messages/" + chatId + "/send";
                    messageIsSending = true;

                    // Create request
                    RequestQueue sendMessageQueue = Volley.newRequestQueue(context);
                    JsonRequest sendMessageRequest = new JsonObjectRequest(Request.Method.POST, sendMessageUrl, jsonRequest,
                            new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject response) {
                                    messageIsSending = false;
                                    binding.messageInputTextview.setText("");
                                    updateMessages();

                                    // Scroll to bottom
                                    messagesAreaRecycler.scrollToPosition(messageDataList.size() - 1);
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
                    sendMessageQueue.add(sendMessageRequest);
                } else {
                    Toast.makeText(context, "Message is sending", Toast.LENGTH_LONG).show();
                }
            }
        });

        View listingButton = binding.constraintLayoutChat;
        listingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listingData != null) {
                    NavController navController = MainActivity.getNavController();
                    NavDestination currentDestination = navController.getCurrentDestination();

                    NavDirections action = null;
                    if (currentDestination.getId() == R.id.chatFragment) {
                        action = ChatFragmentDirections.actionChatFragmentToListingFragment(listingData);
                    }

                    MainActivity.getNavController().navigate(action);
                } else {
                    Toast.makeText(context, "Listing data is not loaded", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void stopMessageUpdateTimer() {
        mHandler.removeCallbacksAndMessages(null); // Stop the timer
    }

    private void startMessageUpdateTimer() {
        mHandler = new Handler();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                updateMessages(); // Call the method to update messages
                mHandler.postDelayed(this, UPDATE_INTERVAL); // Schedule the next update
            }
        }, UPDATE_INTERVAL);
    }

    @Override
    public void onResume() {
        super.onResume();
        updateMessages();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        stopMessageUpdateTimer(); // Stop the timer
    }

}