package com.qrcode_quest.ui.leaderboard;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.qrcode_quest.databinding.PlayerItemViewBinding;
import com.qrcode_quest.entities.PlayerAccount;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link PlayerAccount}.
 *
 * @author jdumouch
 * @version 1.0
 */
public class PlayerViewAdapter extends RecyclerView.Adapter<PlayerViewAdapter.ViewHolder>
    implements Filterable {
    /**
     * Provides a callback interface for an item press event
     */
    public interface ItemClickHandler {
        /**
         * A handler for a user tapping an item.
         *
         * @param username The username of the player item clicked
         */
        void onItemClick(String username);
    }

    private final ItemClickHandler onClickListener;
    private PlayerFilter filter;
    private List<PlayerViewItem> items;
    public List<PlayerViewItem> filteredItems;

    /**
     * Create a new ViewAdapter using the passed lists to build item data.
     *
     * @param items The items to display in the list
     * @param onClickListener The listener to handle on click events
     */
    public PlayerViewAdapter(List<PlayerViewItem> items, ItemClickHandler onClickListener) {
        this.items = items;
        this.filteredItems = items;
        this.onClickListener = onClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(PlayerItemViewBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false));
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        PlayerViewItem item = items.get(position);

        // Bind the data to the ViewHolder
        holder.player = item;
        holder.nameText.setText(item.username);
        holder.scoreText.setText(String.format("%d", item.score));

        // Handle the user pressing the item
        holder.itemView.setOnClickListener(v -> {
            onClickListener.onItemClick(item.username);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }


    /** ViewHolders serve as the binder between the View and the data. */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView nameText;
        public final TextView scoreText;
        public PlayerViewItem player;

        /** Constructs a ViewHolder and bind the members to the view */
        public ViewHolder(@NonNull PlayerItemViewBinding binding) {
            super(binding.getRoot());
            nameText = binding.playerlistContentName;
            scoreText = binding.playerlistContentScore;
        }
    }


    /**
     * Returns the view adapters filter class.
     * This can be used to modify the displayed results.
     */
    @Override
    public Filter getFilter() {
        if (this.filter == null){
            this.filter = new PlayerFilter();
        }
        return this.filter;
    }

    /** Filter class implementation to sort by username */
    public class PlayerFilter extends Filter {
        /**
         * Filters the player usernames case insensitively, retaining only usernames
         * that contain the filter string.
         */
        @Override
        protected FilterResults performFiltering(CharSequence filter) {
            FilterResults results = new FilterResults();
            // Handle empty filter query
            if (filter == null || filter.length() == 0){
                results.values = filteredItems;
                results.count = filteredItems.size();
                return results;
            }

            ArrayList<PlayerViewItem> filtered = new ArrayList<>();
            for (PlayerViewItem player : filteredItems){
                // Check if the player's name matches the filter (case insensitive)
                if (player.username.toLowerCase().contains(filter.toString().toLowerCase())){
                    filtered.add(player);
                }
            }
            results.values = filtered;
            results.count = filtered.size();
            return results;
        }


        /** Updates the list on a filter call. */
        @Override
        @SuppressLint("NotifyDataSetChanged")
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            items = (ArrayList<PlayerViewItem>) filterResults.values;
            notifyDataSetChanged();
        }
    }
}