package com.example.studex.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.studex.Authentication;
import com.example.studex.ChatAdapter;
import com.example.studex.ChatData;
import com.example.studex.MainActivity;
import com.example.studex.R;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ChatsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ChatsFragment extends Fragment {
    private RecyclerView recyclerView;
    private ChatAdapter chatAdapter;
    private RequestQueue chatsQueue;

    public ChatsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment ChatsFragment.
     */

    public static ChatsFragment newInstance() {
        ChatsFragment fragment = new ChatsFragment();
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
        View view = inflater.inflate(R.layout.fragment_chats, container, false);

        initializeViews(view);

        return view;
    }

    private void initializeViews(View view) {
        // Create and set the adapter for the RecyclerView
        recyclerView = view.findViewById(R.id.chats_view);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 1, GridLayoutManager.VERTICAL, true);
        recyclerView.setLayoutManager(gridLayoutManager);

        String chatsUrl = MainActivity.getBaseURL() + "chats/all";
        chatsQueue = Volley.newRequestQueue(getContext());
        StringRequest chatsRequest = new StringRequest(Request.Method.GET, chatsUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Gson gson = new Gson();
                        ChatData[] data = gson.fromJson(response, ChatData[].class);
                        List<ChatData> chats = new ArrayList<>();

                        // Setting all of the chatData objects to the chats list
                        Collections.addAll(chats, data);

                        chatAdapter = new ChatAdapter(chats, getContext());
                        recyclerView.setAdapter(chatAdapter);
                        recyclerView.scrollToPosition(chatAdapter.getItemCount() - 1);
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
        chatsQueue.add(chatsRequest);
    }

    @Override
    public void onResume() {
        super.onResume();
        initializeViews(getView());
    }

}