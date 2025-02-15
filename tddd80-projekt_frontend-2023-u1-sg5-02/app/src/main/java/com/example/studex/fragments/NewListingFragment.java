package com.example.studex.fragments;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.LOCATION_SERVICE;
import static com.example.studex.Authentication.getAccessTokenKey;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.studex.Authentication;
import com.example.studex.ListingData;
import com.example.studex.MainActivity;
import com.example.studex.R;
import com.example.studex.databinding.FragmentNewListingBinding;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;



/**
 * A simple {@link Fragment} subclass.
 * Use the {@link NewListingFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class NewListingFragment extends Fragment {
    private String mParam1;
    private String mParam2;

    private TextInputEditText titleText;
    private TextInputEditText priceText;
    private TextInputEditText locationText;
    private TextInputEditText descriptionText;

    private Button getLocationBtn;
    private Button openCameraBtn;
    private Button openGalleryBtn;
    private Button SubmitBtn;
    private Button CancelBtn;
    private ImageView imageView;
    private static final int LOCATION_REQUEST_CODE = 0;
    private static final int CAMERA_REQUEST_CODE = 1;
    private static final int GALLERY_REQUEST_CODE = 2;
    private boolean customImage = false;



    private Geocoder geocoder;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private Intent openCamera;
    private Intent openGallery;

    public NewListingFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment NewListingFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static NewListingFragment newInstance() {
        NewListingFragment fragment = new NewListingFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    FragmentNewListingBinding binding;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentNewListingBinding.inflate(inflater, container, false);
        titleText = binding.titleField;
        priceText = binding.priceField;
        locationText = binding.locationField;
        descriptionText = binding.descriptionField;
        getLocationBtn = binding.getLocationButton;
        openCameraBtn = binding.openCameraButton;
        openGalleryBtn = binding.addPictureButton;
        SubmitBtn = binding.submitPostButton;
        CancelBtn = binding.cancelPostButton;
        imageView = binding.imageView;
        imageView.setImageResource(R.drawable.ic_no_image);


        locationManager = (LocationManager) requireContext().getSystemService(LOCATION_SERVICE);
        geocoder = new Geocoder(getContext(), Locale.getDefault());

        openCamera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        openGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        openCameraBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestPermissions(new String[] { Manifest.permission.CAMERA }, CAMERA_REQUEST_CODE);
            }
        });

        openGalleryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestPermissions(new String[] { Manifest.permission.READ_MEDIA_IMAGES}, GALLERY_REQUEST_CODE);
            }
        });

        SubmitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = binding.titleField.getText().toString();
                String location = binding.locationField.getText().toString();
                String description = binding.descriptionField.getText().toString();
                String price = binding.priceField.getText().toString();
                String seller = Authentication.getId();
                String image = null;

                if (customImage) {

                    Bitmap imageBitmap = ((BitmapDrawable) binding.imageView.getDrawable()).getBitmap();

                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                    byte[] byteArray = stream.toByteArray();
                    image = Base64.encodeToString(byteArray, Base64.DEFAULT);
                }

                if (price.contains(",")) {
                    price = price.replace(",", ".");
                }

                Float floatPrice = null;
                try {
                    floatPrice = Float.parseFloat(price);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }

                // Create a new listing object
                ListingData listing = new ListingData(title, floatPrice, description, location, seller, image, null);

                // Convert the User object to JSON
                Gson gson = new Gson();
                String json = gson.toJson(listing);

                JSONObject jsonRequest = null;
                try {
                    jsonRequest = new JSONObject(json);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }

                // Create a new StringRequest
                String addUrl = MainActivity.getBaseURL() + "listing/add";
                RequestQueue addQueue = Volley.newRequestQueue(getContext());

                // Send the request
                JsonObjectRequest addRequest = new JsonObjectRequest(Request.Method.POST, addUrl, jsonRequest,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                try {
                                    Toast.makeText(getContext(), response.getString("message"), Toast.LENGTH_SHORT).show();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                titleText.setText("");
                                priceText.setText("");
                                locationText.setText("");
                                descriptionText.setText("");
                                imageView.setImageResource(R.drawable.ic_no_image);
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        String message = MainActivity.getErrorMessage(error);
                        Log.i("Error", message);
                        Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
                    }
                }){
                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        Map<String, String> headers = new HashMap<>();
                        headers.put("Authorization", "Bearer " + getAccessTokenKey());
                        headers.put("Content-Type", "application/json");
                        return headers;
                    }};

                addQueue.add(addRequest);
            }
        });

        CancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                titleText.setText("");
                priceText.setText("");
                locationText.setText("");
                descriptionText.setText("");

            }
        });
        getLocationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
            }
        });

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                String address = null;
                try {
                    address = geocoder.getFromLocation(location.getLatitude(),
                            location.getLongitude(), 1).get(0).getAddressLine(0);
                    locationText.setText(address);
                    locationManager.removeUpdates(locationListener);

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }};






        return binding.getRoot();
    }

    /*@RequiresApi(api = Build.VERSION_CODES.TIRAMISU) // TODO: Kanske behövs*/
    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                        0, 0, locationListener);
                /*locationField.setText("Checking your location...");*/
                Toast.makeText(getContext(), "Checking your location", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getContext(),
                        "Location access is needed for the Get address function", Toast.LENGTH_LONG).show();
            }
        } else if (requestCode == CAMERA_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startActivityForResult(openCamera, CAMERA_REQUEST_CODE);
            } else {
                requestPermissions(new String[] {Manifest.permission.CAMERA }, CAMERA_REQUEST_CODE);
            }
        } else if (requestCode == GALLERY_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startActivityForResult(openGallery, GALLERY_REQUEST_CODE);
            } else {
                requestPermissions(new String[] {Manifest.permission.READ_MEDIA_IMAGES }, GALLERY_REQUEST_CODE);
            }
        }
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case CAMERA_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    try {
                        Bitmap bitmap = (Bitmap) data.getExtras().get("data");
                        imageView.setImageBitmap(bitmap);
                        customImage = true;

                    } catch (RuntimeException e) {
                        e.printStackTrace();
                    }
                    break;
                }

            case GALLERY_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    try {
                        Uri imageUri = data.getData();
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), imageUri);
                        imageView.setImageBitmap(bitmap);

                        customImage = true;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                }
        }
    }
}