package com.qrcode_quest.ui.playerQR;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.qrcode_quest.databinding.QrshotItemViewBinding;
import com.qrcode_quest.entities.QRShot;
import com.qrcode_quest.entities.RawQRCode;

import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link QRShot}.
 *
 * @author jdumouch
 * @version 1.0
 */
public class PlayerQRShotViewAdapter extends RecyclerView.Adapter<PlayerQRShotViewAdapter.ViewHolder> {
    /**
     * Provides a callback interface for an item press event
     */
    public interface ItemClickHandler {
        /**
         * A handler for a user tapping an item.
         *
         * @param shot The QRShot that was pressed
         */
        void onItemClick(QRShot shot);
    }

    private final List<QRShot> shots;
    private final ItemClickHandler onClickListener;

    /**
     * Create a new ViewAdapter using the passed lists to build item data.
     * @param items           The QRShots to display
     * @param onClickListener The listener to handle on click events
     */
    public PlayerQRShotViewAdapter(List<QRShot> items, ItemClickHandler onClickListener) {
        this.shots = items;
        this.onClickListener = onClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(QrshotItemViewBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false));
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        // Try to get the underlying QR objects
        QRShot shot = shots.get(position);
        if (shot == null) {
            return;
        }

        // Grab relevant info
        String hash = shot.getCodeHash();
        int score = RawQRCode.getScoreFromHash(hash);

        // Load the data into the ViewHolder
        holder.shot = shot;
        holder.qrName.setText(hash.substring(hash.length()-5));
        holder.qrScore.setText(String.format("%d", score));

        holder.itemView.setOnClickListener(v -> {
            onClickListener.onItemClick(shot);
        });
    }

    @Override
    public int getItemCount() {
        return shots.size();
    }

    /**
     * ViewHolders serve as the binder between the View and the data.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView qrName;
        public final TextView qrScore;
        public QRShot shot;

        /**
         * Constructs a ViewHolder and binds the View to the data
         *
         * @param binding
         */
        public ViewHolder(@NonNull QrshotItemViewBinding binding) {
            super(binding.getRoot());
            qrName = binding.playerQrlistContentName;
            qrScore = binding.playerQrlistContentScore;
        }
    }
}