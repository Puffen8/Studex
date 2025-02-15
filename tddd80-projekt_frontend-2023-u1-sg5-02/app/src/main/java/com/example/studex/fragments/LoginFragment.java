package com.example.studex.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.studex.Authentication;
import com.example.studex.MainActivity;
import com.example.studex.R;
import com.example.studex.UserData;
import com.example.studex.databinding.FragmentLoginBinding;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link LoginFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LoginFragment extends Fragment {
    private String username;
    private Button loginButton;
    private Button registerButton;
    private Context context;

    public LoginFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment LoginFragment.
     */

    public static LoginFragment newInstance() {
        LoginFragment fragment = new LoginFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    FragmentLoginBinding binding;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentLoginBinding.inflate(inflater, container, false);

        context = getContext();

        registerButton = binding.createAccountButton;
        loginButton = binding.buttonSignIn;

        loginButton.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);

            // Hide the keyboard
            inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);

            username = binding.usernameField.getText().toString();
            String password = binding.passwordField.getText().toString();

            // Create a new User object
            UserData user = new UserData(username, password);

            // Convert the User object to JSON
            Gson gson = new Gson();
            String json = gson.toJson(user);

            JSONObject jsonRequest;
            try {
                jsonRequest = new JSONObject(json);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }

            // Create a new StringRequest
            String loginUrl = MainActivity.getBaseURL() + "login";
            RequestQueue loginQueue = Volley.newRequestQueue(context);

            // Send the request
            JsonObjectRequest loginRequest = new JsonObjectRequest(Request.Method.POST, loginUrl, jsonRequest,
                    new Response.Listener<JSONObject>() {
                        @SuppressLint("ResourceType")
                        @Override
                        public void onResponse(JSONObject response) {
                            // Handle the response
                            try {
                                String token = response.getString("token");
                                String id = response.getString("id");
                                Authentication.setAccessTokenKey(token);
                                Authentication.setUsername(username);
                                Authentication.setId(id);

                                try {
                                    Toast.makeText(getContext(), response.getString("message"), Toast.LENGTH_LONG).show();
                                } catch (JSONException e) {
                                    throw new RuntimeException(e);
                                }

                                MainActivity.getNavController().navigate(MainActivity.getCurrentBottomNavBarItem());


                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    String message = MainActivity.getErrorMessage(error);
                    Log.i("Error", message);
                    Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
                }
            });
            loginQueue.add(loginRequest);
        }
        });

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.getNavController().navigate(R.id.action_loginFragment_to_registrationFragment);
            }
        });

        return binding.getRoot();
    }
}
