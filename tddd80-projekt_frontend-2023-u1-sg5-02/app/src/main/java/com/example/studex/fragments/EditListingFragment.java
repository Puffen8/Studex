package com.example.studex.fragments;

import static android.content.Context.LOCATION_SERVICE;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.NavDirections;

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
import com.example.studex.databinding.FragmentEditListingBinding;
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
 * Use the {@link EditListingFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class EditListingFragment extends Fragment {
    private Button getLocationBtn;
    private String listingId;
    private ImageView imageView;
    private TextInputEditText locationField;
    private static final int LOCATION_REQUEST_CODE = 0;
    private static final int CAMERA_REQUEST_CODE = 1;
    private static final int GALLERY_REQUEST_CODE = 2;
    private boolean customImage = false;
    private Geocoder geocoder;
    private LocationManager locationManager;
    private LocationListener locationListener;

    private Intent openCamera;
    private Intent openGallery;
    private Context context;

    public EditListingFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment EditListingFragment.
     */

    public static EditListingFragment newInstance() {
        EditListingFragment fragment = new EditListingFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    FragmentEditListingBinding binding;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        context = getContext();

        binding = FragmentEditListingBinding.inflate(inflater, container, false);
        getLocationBtn = binding.getLocationButton;
        imageView = binding.imageView;

        locationField = binding.locationField;

        locationManager = (LocationManager) requireContext().getSystemService(LOCATION_SERVICE);
        geocoder = new Geocoder(getContext(), Locale.getDefault());

        openCamera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        openGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        EditListingFragmentArgs arguments = EditListingFragmentArgs.fromBundle(getArguments());
        ListingData listing = arguments.getListingData();
        binding.titleText.getEditText().setText(listing.getTitle());
        binding.priceText.getEditText().setText(listing.getPrice());
        binding.locationText.getEditText().setText(listing.getLocation());
        binding.descriptionText.getEditText().setText(listing.getDescription());

        if (listing.getImage() != null) {
            byte[] decodedString = Base64.decode(listing.getImage(), Base64.DEFAULT);

            Bitmap bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            imageView.setImageBitmap(bitmap);
        } else {
            imageView.setImageResource(R.drawable.ic_no_image);
        }
        listingId = listing.getId();

        binding.openCameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestPermissions(new String[] { Manifest.permission.CAMERA }, CAMERA_REQUEST_CODE);
            }
        });

        binding.addPictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestPermissions(new String[] { Manifest.permission.READ_MEDIA_IMAGES}, GALLERY_REQUEST_CODE);
            }
        });

        Button saveButton = binding.saveButton;
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = binding.titleText.getEditText().getText().toString();
                String price = binding.priceText.getEditText().getText().toString();
                String location = binding.locationText.getEditText().getText().toString();
                String description = binding.descriptionText.getEditText().getText().toString();
                String seller = Authentication.getId();
                String image = null;
                if (customImage) {

                    Bitmap imageBitmap = ((BitmapDrawable) binding.imageView.getDrawable()).getBitmap();

                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                    byte[] byteArray = stream.toByteArray();
                    image = Base64.encodeToString(byteArray, Base64.DEFAULT);
                }

                // Create a new User object
                ListingData listing = new ListingData(title, Float.parseFloat(price), description, location, seller, image, null);

                // Convert the User object to JSON
                Gson gson = new Gson();
                String json = gson.toJson(listing);

                JSONObject jsonRequest= null;
                try {
                    jsonRequest = new JSONObject(json);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                String editListingUrl = MainActivity.getBaseURL() + "listing/edit/" + listingId;
                RequestQueue editListingQueue = Volley.newRequestQueue(getContext());
                JsonObjectRequest editListingRequest = new JsonObjectRequest(Request.Method.PUT, editListingUrl, jsonRequest,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                try {
                                    Toast.makeText(context, response.getString("message"), Toast.LENGTH_LONG);
                                } catch (JSONException e) {
                                    throw new RuntimeException(e);
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

                editListingQueue.add(editListingRequest);

                NavController navController = MainActivity.getNavController();
                NavDestination currentDestination = navController.getCurrentDestination();

                NavDirections action = null;
                if (currentDestination.getId() == R.id.editListingFragment) {
                    String listingImage = null;
                    if (listing.getImage() != null) {
                        Bitmap imageBitmap = ((BitmapDrawable) binding.imageView.getDrawable()).getBitmap();

                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                        byte[] byteArray = stream.toByteArray();
                        listingImage = Base64.encodeToString(byteArray, Base64.DEFAULT);
                    }
                    ListingData listingArg = new ListingData(title, Float.parseFloat(price), description, location, seller, listingImage, null);
                    action = EditListingFragmentDirections.actionEditListingFragmentToListingFragment(listingArg);
                }

                MainActivity.getNavController().navigate(action);

            }
        });

        Button discardButton = binding.discardButton;
        discardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavController navController = MainActivity.getNavController();
                NavDestination currentDestination = navController.getCurrentDestination();

                if (currentDestination.getId() == R.id.editListingFragment) {
                    NavDirections action = EditListingFragmentDirections.actionEditListingFragmentToListingFragment(listing);
                    MainActivity.getNavController().navigate(action);
                }
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
                try {
                    String address = geocoder.getFromLocation(location.getLatitude(),
                            location.getLongitude(), 1).get(0).getAddressLine(0);
                    locationField.setText(address);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }};

        return binding.getRoot();
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                        0, 0, locationListener);
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
                try {

                    Bitmap bitmap = (Bitmap) data.getExtras().get("data");
                    imageView.setImageBitmap(bitmap);

                    customImage = true;

                } catch (RuntimeException e) {
                    e.printStackTrace();
                }
                break;

            case GALLERY_REQUEST_CODE:
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