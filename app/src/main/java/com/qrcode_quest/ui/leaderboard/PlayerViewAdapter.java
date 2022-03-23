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
     * Provides a callback interface for an item press event
     */
    public interface ItemClickHandler {
        /**
         * A handler for a user tapping an item.
         *
         * @param player The PlayerAccount pressed
         */
        void onItemClick(PlayerAccount player);
    }

    private final List<PlayerAccount> players;
    private final ItemClickHandler onClickListener;

    /**
     * Create a new ViewAdapter using the passed lists to build item data.
     *
     * @param onClickListener The listener to handle on click events
     */
    public PlayerViewAdapter(List<PlayerAccount> items, ItemClickHandler onClickListener) {
        this.players = items;
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
        PlayerAccount player = players.get(position);

        holder.player = player;
        holder.nameText.setText(player.getUsername());

        holder.itemView.setOnClickListener(v -> {
            onClickListener.onItemClick(player);
        });
    }

    @Override
    public int getItemCount() {
        return players.size();
    }

    /**
     * ViewHolders serve as the binder between the View and the data.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView nameText;
        public final TextView scoreText;
        public PlayerAccount player;

        /**
         * Constructs a ViewHolder and binds the View to the data
         *
         * @param binding
         */
        public ViewHolder(@NonNull PlayerItemViewBinding binding) {
            super(binding.getRoot());
            nameText = binding.playerlistContentName;
            scoreText = binding.playerlistContentScore;
        }
    }
}