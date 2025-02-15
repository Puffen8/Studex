package com.example.studex.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.studex.Authentication;
import com.example.studex.MainActivity;
import com.example.studex.UserData;
import com.example.studex.databinding.FragmentCredentialsBinding;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CredentialsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CredentialsFragment extends Fragment {
    private UserData userData;

    public CredentialsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment CredentialsFragment.
     */

    public static CredentialsFragment newInstance(String param1, String param2) {
        CredentialsFragment fragment = new CredentialsFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    FragmentCredentialsBinding binding;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        binding = FragmentCredentialsBinding.inflate(inflater, container, false);
        binding.saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setUserData();
            }
        });

        binding.discardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    binding.firstNameText.getEditText().setText(userData.getFirst_name());
                    binding.lastNameText.getEditText().setText(userData.getLast_name());
                    binding.phoneNumberText.getEditText().setText(userData.getPhone_number());
                } catch (RuntimeException e) {
                    e.printStackTrace();
                }

            }
        });
        getUserData();

        return binding.getRoot();
    }

    public void setUserData() {
        String userName = binding.firstNameText.getEditText().getText().toString();
        String email = binding.lastNameText.getEditText().getText().toString();
        String password = binding.phoneNumberText.getEditText().getText().toString();

        // Create a new User object
        UserData user = new UserData(userName, email, password);

        // Convert the User object to JSON
        Gson gson = new Gson();
        String json = gson.toJson(user);

        JSONObject jsonRequest= null;
        try {
            jsonRequest = new JSONObject(json);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String profileUrl = MainActivity.getBaseURL() + "profile";
        RequestQueue profileQueue = Volley.newRequestQueue(getContext());
        JsonObjectRequest profileRequest = new JsonObjectRequest(Request.Method.PUT, profileUrl, jsonRequest,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        userData = user;
                        try {
                            Toast.makeText(getContext(), response.getString("message"), Toast.LENGTH_SHORT).show();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
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
    }

    public void getUserData() {
        String userUrl = MainActivity.getBaseURL() + "profile";
        RequestQueue userQueue = Volley.newRequestQueue(getContext());
        StringRequest userRequest = new StringRequest(Request.Method.GET, userUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Gson gson = new Gson();
                        userData = gson.fromJson(response, UserData.class);
                        try {
                            binding.firstNameText.getEditText().setText(userData.getFirst_name());
                            binding.lastNameText.getEditText().setText(userData.getLast_name());
                            binding.phoneNumberText.getEditText().setText(userData.getPhone_number());
                            String accountCreationText = "Account created: " + userData.getCreated_at();
                            binding.accountCreationDateText.setText(accountCreationText);
                        } catch (RuntimeException e) {
                            e.printStackTrace();
                        }

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

        userQueue.add(userRequest);
    }
}