package com.example.studex.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

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
import com.android.volley.toolbox.Volley;
import com.example.studex.MainActivity;
import com.example.studex.UserData;
import com.example.studex.databinding.FragmentRegistrationBinding;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link RegistrationFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RegistrationFragment extends Fragment {
    public RegistrationFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment RegistrationFragment.
     */

    public static RegistrationFragment newInstance() {
        RegistrationFragment fragment = new RegistrationFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    Button registerButton;
    Button cancelButton;
    FragmentRegistrationBinding binding;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentRegistrationBinding.inflate(inflater, container, false);
        registerButton = binding.regButton;

        // Register button click listener
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the text from the EditTexts
                String userName = binding.usernameInput.getText().toString();
                String email = binding.emailInput.getText().toString();
                String password = binding.passwordInput.getText().toString();
                String firstName = binding.firstNameInput.getText().toString();
                String lastName = binding.lastNameInput.getText().toString();
                String phoneNumber = binding.phoneNumberInput.getText().toString();

                // Create a new User object
                UserData user = new UserData(userName, email, password, firstName, lastName, phoneNumber, null, null);

                // Convert the User object to JSON
                Gson gson = new Gson();
                String json = gson.toJson(user);

                JSONObject jsonRequest= null;
                try {
                    jsonRequest = new JSONObject(json);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                // Create a new StringRequest
                String signupUrl = MainActivity.getBaseURL() + "signup";
                RequestQueue signupQueue = Volley.newRequestQueue(getContext());
                JsonObjectRequest signupRequest = new JsonObjectRequest(Request.Method.POST, signupUrl, jsonRequest,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                try {
                                    Toast.makeText(getContext(), response.getString("message"), Toast.LENGTH_LONG).show();
                                } catch (JSONException e) {
                                    throw new RuntimeException(e);
                                }
                                MainActivity.getNavController().navigateUp();
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                String message = MainActivity.getErrorMessage(error);
                                Log.i("Error", message);
                                Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
                            }
                        });

                signupQueue.add(signupRequest);
            }
        });
        cancelButton = binding.regCancelButton;
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.getNavController().navigateUp();
            }});

        return binding.getRoot();
    }
}