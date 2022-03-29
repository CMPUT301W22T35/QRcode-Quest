package com.qrcode_quest.ui.qr_view;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.qrcode_quest.databinding.UsernameItemViewBinding;

import java.util.ArrayList;

/**
 * A ViewAdapter for displaying player username strings as a list.
 * This provides very minimal functionality with no click handling.
 *
 * @author jdumouch
 * @version 1.0
 */
public class UsernameViewAdapter extends RecyclerView.Adapter<UsernameViewAdapter.ViewHolder> {

    private ArrayList<String> items;

    /** Creates a new UsernameViewAdapter using the provided list */
    public UsernameViewAdapter(ArrayList<String> items){
        this.items = items;
    }

    /**
     * Gets the stored list of items
     * @return The list of items
     */
    public ArrayList<String> getItems(){ return items; }

    /** Handles the event that a new needs to be ViewHolder created */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(UsernameItemViewBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false));
    }


    /** Binds a new ViewHolder to its respective item data. */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String username = items.get(position);

        holder.username = username;
        holder.usernameTextView.setText(username);
    }

    /** Returns the number of items displayed */
    @Override
    public int getItemCount() {
        return this.items.size();
    }

    /** A very basic view holder for displaying player usernames */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView usernameTextView;
        private String username;

        public ViewHolder(@NonNull UsernameItemViewBinding binding) {
            super(binding.getRoot());
            usernameTextView = binding.usernameViewText;
        }
    }
}
