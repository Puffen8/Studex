package com.example.studex;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.annotation.NonNull;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.fragment.app.FragmentManager;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    /*private static final String baseURL = "https://studex.azurewebsites.net/";*/
    private static final String baseURL = "http://10.0.2.2:5000/";
//    private static final String baseURL = "http://192.168.0.101:5000/";
    private static final List<Integer> logInReqFragments = Arrays.asList(R.id.newListingFragment, R.id.chatsFragment, R.id.profileFragment);
    private static final List<Integer> mainFragments = Arrays.asList(R.id.homeFragment, R.id.searchFragment, R.id.newListingFragment, R.id.chatsFragment, R.id.profileFragment, R.id.loginFragment);

    private static int currentBottomNavBarItem;
    private static FragmentManager fragmentManager;
    private static NavHostFragment navHostFragment;
    private static NavController navController;
    private static BottomNavigationView bottomNavigationView;
    private static Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;
        bottomNavigationView = findViewById(R.id.bottomnav);
        fragmentManager = getSupportFragmentManager();
        navHostFragment = (NavHostFragment) fragmentManager.findFragmentById(R.id.navHostFragment);
        navController = navHostFragment.getNavController();

        NavigationUI.setupWithNavController(bottomNavigationView, navController);

        Set<Integer> mainFragmentSet = new HashSet<>(Arrays.asList(R.id.homeFragment,
                R.id.searchFragment, R.id.newListingFragment, R.id.chatsFragment, R.id.profileFragment));

        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(mainFragmentSet).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        navController.addOnDestinationChangedListener(new NavController.OnDestinationChangedListener() {
            @Override
            public void onDestinationChanged(@NonNull NavController navController,
                                             @NonNull NavDestination navDestination,
                                             @Nullable Bundle bundle) {
                if (logInReqFragments.contains(navDestination.getId())) {
                    if (!Authentication.isLoggedIn()) {
                        navController.navigate(R.id.loginFragment);
                        setCurrentBottomNavBarItem(navDestination.getId());
                    }
                }
                if (!mainFragments.contains(navDestination.getId())) {
                    hideBottomNav(true);
                }
                if (mainFragments.contains(navDestination.getId())) {
                    hideBottomNav(false);
                }
            }
        });
    }
    @Override
    public boolean onSupportNavigateUp() {
        return navController.navigateUp();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.top_nav, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.logout_button:
                revokeToken();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void hideBottomNav(boolean hide) {
        if (hide) {
            bottomNavigationView.setVisibility(View.GONE);
        } else {
            bottomNavigationView.setVisibility(View.VISIBLE);
        }
    }

    public static void revokeToken() {
        String logoutUrl = MainActivity.getBaseURL() + "logout";
        RequestQueue logoutQueue = Volley.newRequestQueue(navHostFragment.getContext());
        JsonObjectRequest logoutRequest = new JsonObjectRequest(Request.Method.POST, logoutUrl, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            Toast.makeText(context, response.getString("message"), Toast.LENGTH_SHORT).show();
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                        Authentication.clearAccessToken();
                        Authentication.clearUsername();
                        Authentication.clearId();
                        navController.popBackStack(navController.getGraph().getStartDestination(), false);
                        MainActivity.getNavController().navigate(R.id.homeFragment);
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
        logoutQueue.add(logoutRequest);

    }

    public static String getErrorMessage(VolleyError error) {
        String errorMessage = null;
        String message = "Unknown Error";
        NetworkResponse networkResponse = error.networkResponse;
        if (networkResponse != null && networkResponse.data != null) {
            try {
                errorMessage = new String(networkResponse.data, "UTF-8");

            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        try {
            if (errorMessage != null) {
                JSONObject errorJson = new JSONObject(errorMessage);
                message = errorJson.getString("message");
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return message;
    }

    public static String getBaseURL() {
        return baseURL;
    }
    public static NavHostFragment getNavHostFragment() {
        return navHostFragment;
    }
    public static NavController getNavController() {
        return navController;
    }

    public static int getCurrentBottomNavBarItem() {
        return currentBottomNavBarItem;
    }

    public static void setCurrentBottomNavBarItem(int currentBottomNavBarItem) {
        MainActivity.currentBottomNavBarItem = currentBottomNavBarItem;
    }
    public static FragmentManager getFragmentManager2() {
        return fragmentManager;
    }

    public Context getThisContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }
}
// Points:
// TODO: Server-labbar
// TODO: Android-labbar
// TODO: Nav graph fullt ut
// TODO: GPS
// TODO: Favoritea listings
// TODO: Följa andras aktivitet
// TODO: Extra rapport
// TODO: Safe args
// TODO: skriva chattar
// TODO: kamera

// Necessary:
// TODO: Bugg: kan inte edita en listing
// TODO: Lägga till kommentarer

// Extras:
// TODO: Delete funktion på accounts och listings
// TODO: Lägga till Login knapp i top menu
// TODO: lägga till en logout button längst ner i profile fragment
// TODO: Göra att en listing blir viewed när man går in på den
// TODO: (Set every longer text field (description for example) in a scroll view so the object will take up less space in the fragment)
// TODO: ConstraintLayouten i chatFragment ska ta en till listingen då man klickar på den
// TODO: Submit knappen i newListingFragment ska ta en till ListingFragment där man ser Listingen??? no need for it men sku va coolt
// TODO: Lägg till "categories" i listings + kanske search by price?
// TODO: Bugg: Kan inte adda en listing som kostar 0 kr. Ger error 400 i backend.
// TODO: Bugg: Ibland laddar inte hemfragmentet in listingsen ordentligt.
// TODO: Bugg: Ibland visas listings some är favorited inte som favorited o vice versa
// TODO: Byta ut datahämtning i vissa fragment mot safe args om det går.
// TODO: Confirm password when registering
// TODO: Kolla input validates i backend
// TODO: New posts i homeFragment fyller på bakifrån
// TODO: I homefragment, istället för continues updates ifall man sparar datan lokalt kan man lägga till en refresh funktion genom att scrolla upp i "taket".