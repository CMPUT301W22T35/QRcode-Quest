package com.qrcode_quest.ui.leaderboard;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.qrcode_quest.databinding.PlayerItemViewBinding;
import com.qrcode_quest.databinding.QrshotItemViewBinding;
import com.qrcode_quest.entities.PlayerAccount;

import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link PlayerAccount}.
 *
 * @author jdumouch
 * @version 1.0
 */
public class PlayerViewAdapter extends RecyclerView.Adapter<PlayerViewAdapter.ViewHolder> {
    /**
     * A data model class for displaying a PlayerAccount in the list
     */
    public static class PlayerItem {
        public final String username;
        public final int score;

        public PlayerItem(String username, int score){
            this.username = username;
            this.score = score;
        }
    }

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

    private final List<PlayerItem> items;
    private final ItemClickHandler onClickListener;

    /**
     * Create a new ViewAdapter using the passed lists to build item data.
     *
     * @param items The items to display in the list
     * @param onClickListener The listener to handle on click events
     */
    public PlayerViewAdapter(List<PlayerItem> items, ItemClickHandler onClickListener) {
        this.items = items;
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
        PlayerItem item = items.get(position);

        holder.player = item;
        holder.nameText.setText(item.username);
        holder.scoreText.setText(String.format("%d", item.score));

        holder.itemView.setOnClickListener(v -> {
            onClickListener.onItemClick(item.username);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    /**
     * ViewHolders serve as the binder between the View and the data.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView nameText;
        public final TextView scoreText;
        public PlayerItem player;

        /**
         * Constructs a ViewHolder and binds the View to the data
         */
        public ViewHolder(@NonNull PlayerItemViewBinding binding) {
            super(binding.getRoot());
            nameText = binding.playerlistContentName;
            scoreText = binding.playerlistContentScore;
        }
    }
}