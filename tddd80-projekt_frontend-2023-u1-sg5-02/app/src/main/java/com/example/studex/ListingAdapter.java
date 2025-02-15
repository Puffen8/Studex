package com.example.studex;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.NavDirections;

import androidx.recyclerview.widget.RecyclerView;

import com.example.studex.fragments.HomeFragmentDirections;
import com.example.studex.fragments.ProfileFragmentDirections;
import com.example.studex.fragments.SearchFragmentDirections;

import java.util.List;

public class ListingAdapter extends RecyclerView.Adapter<ListingAdapter.ListingViewHolder> {
    private List<ListingData> listings;

    public ListingAdapter(List<ListingData> posts) {
        this.listings = posts;
    }

    @Override
    public ListingViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_small_listing, parent, false);

        return new ListingViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ListingViewHolder holder, int position) {
        ListingData listing = listings.get(position);
        holder.textTextView.setText(listing.getTitle());
        String priceText = listing.getPrice() + " kr";
        holder.priceTextView.setText(priceText);
        holder.itemView.setOnClickListener(view2 ->{
            // Why to use Safe Args:
            // Safe args are more relient and safer to use than regular arguments using Bundle.
            // Safe args cooperates with the Navigation graph which is benefitial when using a nav graph.
            // Safe args also generate type safe classes for handeling the arguments.
            // These classes are easy to use and eliminates the need to use Bundle objects.
            // They also updates automatically when you refactor any argument in your code.

            NavController navController = MainActivity.getNavController();
            NavDestination currentDestination = navController.getCurrentDestination();

            NavDirections action = null;
            if (currentDestination.getId() == R.id.homeFragment) {
                action = HomeFragmentDirections.actionHomeFragmentToListingFragment2(listing);
                MainActivity.getNavController().navigate(action);
            } else if (currentDestination.getId() == R.id.profileFragment) {
                action = ProfileFragmentDirections.actionProfileFragmentToListingFragment(listing);
                MainActivity.getNavController().navigate(action);
            }
            else if (currentDestination.getId() == R.id.searchFragment) {
                action = SearchFragmentDirections.actionSearchFragmentToListingFragment(listing);
                MainActivity.getNavController().navigate(action);
            }
            MainActivity.getNavController().popBackStack(R.id.listingFragment, false);

        });
        if (listing.getImage() != null) {
            byte[] decodedString = Base64.decode(listing.getImage(), Base64.DEFAULT);

            Bitmap bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

            holder.photoImageView.setImageBitmap(bitmap);
        } else {
            holder.photoImageView.setImageResource(R.drawable.ic_no_image);
        }
    }

    @Override
    public int getItemCount() {
        return listings.size();
    }

    public List<ListingData> getListings() {
        return listings;
    }

    public static class ListingViewHolder extends RecyclerView.ViewHolder {
        public TextView textTextView;
        public ImageView photoImageView;
        public TextView priceTextView;

        public ListingViewHolder(View itemView) {
            super(itemView);
            textTextView = itemView.findViewById(R.id.small_listing_title);
            photoImageView = itemView.findViewById(R.id.small_listing_image);
            priceTextView = itemView.findViewById(R.id.small_listing_price);


        }
    }
}
