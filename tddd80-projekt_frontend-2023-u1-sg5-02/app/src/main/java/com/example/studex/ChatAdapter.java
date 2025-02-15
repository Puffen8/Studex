package com.example.studex;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.NavDirections;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {
    private List<ChatData> chats;
    private String otherUserId;
    private Context context;

    public ChatAdapter(List<ChatData> chats, Context context) {
        this.chats = chats;
        this.context = context;
    }

    @Override
    public ChatViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_small_chat, parent, false);

        return new ChatViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ChatViewHolder holder, int position) {
        ChatData chat = chats.get(position);

        if (chat.getSeller_id().equals(Authentication.getId())) {
            otherUserId = chat.getBuyer_id();
        } else {
            otherUserId = chat.getSeller_id();
        }

        RequestQueue otherUserQueue = Volley.newRequestQueue(context);
        String otherUserUrl = MainActivity.getBaseURL() + "profile/" + otherUserId;
        JsonObjectRequest otherUserRequest = new JsonObjectRequest(Request.Method.GET, otherUserUrl, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Gson gson = new Gson();
                UserData otheruser = gson.fromJson(response.toString(), UserData.class);
                String userText = "Correspondent: " + otheruser.getUsername().
                        substring(0, 1).toUpperCase() + otheruser.getUsername().substring(1);
                holder.userTextView.setText(userText);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                String message = MainActivity.getErrorMessage(error);
                Log.i("Error", message);
            }
        });
        otherUserQueue.add(otherUserRequest);


        RequestQueue listingQueue = Volley.newRequestQueue(context);
        String listingUrl = MainActivity.getBaseURL() + "listing/" + chat.getListing_id();
        JsonObjectRequest listingRequest = new JsonObjectRequest(Request.Method.GET, listingUrl, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Gson gson = new Gson();
                ListingData listingData = gson.fromJson(response.toString(), ListingData.class);
                holder.listingTitleTextView.setText("Listing Title: " + listingData.getTitle());

                if (listingData.getImage() != null) {
                    String imageString = listingData.getImage();
                    byte[] imageBytes = Base64.decode(imageString, Base64.DEFAULT);
                    Bitmap decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);

                    holder.listingImageView.setImageBitmap(decodedImage);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                String message = MainActivity.getErrorMessage(error);
                Log.i("Error", message);

            }
        });
        listingQueue.add(listingRequest);


        RequestQueue messagesQueue = Volley.newRequestQueue(context);
        String messagesUrl = MainActivity.getBaseURL() + "messages/" + chat.getId();
        StringRequest messagesRequest = new StringRequest(Request.Method.GET, messagesUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Gson gson = new Gson();
                        MessageData[] messageData = gson.fromJson(response.toString(), MessageData[].class);
                        List<MessageData> messages = Arrays.asList(messageData);
                        MessageData lastMessage = messages.get(messages.size() - 1);
                        if (messages.size() > 0) {
                            String lastMessageText = "Last message: \"" + lastMessage.getMessage() + "\"";
                            String timeStampText = lastMessage.getTimestamp().substring(0, 11) + " - " + lastMessage.getTimestamp().substring(17, 22);
                            holder.lastMessageTextView.setText(lastMessageText);
                            holder.timestampTextView.setText(timeStampText);
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


        holder.userTextView.setText(chat.getBuyer_id());

        holder.itemView.setOnClickListener(view2 ->{
            NavController navController = MainActivity.getNavController();
            NavDestination currentDestination = navController.getCurrentDestination();

            if (currentDestination.getId() == R.id.chatsFragment) {
                NavDirections action = com.example.studex.fragments.ChatsFragmentDirections.actionChatsFragmentToChatFragment(chat);
                MainActivity.getNavController().navigate(action);
            }
        });
    }


    @Override
    public int getItemCount() {
        return chats.size();
    }

    public static class ChatViewHolder extends RecyclerView.ViewHolder {
        public TextView userTextView;
        public TextView listingTitleTextView;
        public ImageView listingImageView;
        public TextView lastMessageTextView;
        public TextView timestampTextView;
        public TextView totalChats;
        public ChatViewHolder(View itemView) {
            super(itemView);
            userTextView = itemView.findViewById(R.id.preview_user_text);
            listingTitleTextView = itemView.findViewById(R.id.preview_listing_title_text);
            lastMessageTextView = itemView.findViewById(R.id.last_message);
            listingImageView = itemView.findViewById(R.id.small_listing_image_chat);
            timestampTextView = itemView.findViewById(R.id.chat_last_message_time);

            View smallListing = itemView.findViewById(R.id.frameLayout7);
            smallListing.setOnClickListener(view2 ->{
                MainActivity.getNavController().navigate(R.id.chatFragment);

            });
        }
    }
}
