package com.qrcode_quest.ui.playerQR;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import com.qrcode_quest.databinding.FragmentQrshotItemViewBinding;
import com.qrcode_quest.entities.QRCode;
import com.qrcode_quest.entities.QRShot;

import java.util.HashMap;
import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link QRShot}.
 *
 * @author jdumouch
 * @version 1.0
 */
public class PlayerQRShotViewAdapter extends RecyclerView.Adapter<PlayerQRShotViewAdapter.ViewHolder> {

    private final List<QRShot> shots;
    private final HashMap<String, QRCode> codes;

    public PlayerQRShotViewAdapter(List<QRShot> items, HashMap<String, QRCode> codes) {
        this.shots = items;
        this.codes = codes;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(
                FragmentQrshotItemViewBinding.inflate(
                    LayoutInflater.from(parent.getContext()),
                    parent,
                    false)
        );
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        QRShot shot = shots.get(position);
        if (shot == null) { return; }
        holder.shot = shot;
        holder.qrName.setText(shot.getCodeHash());
        holder.qrScore.setText(""+codes.get(shot.getCodeHash()).getScore());
    }

    @Override
    public int getItemCount() {
        return shots.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView qrName;
        public final TextView qrScore;

        public QRShot shot;

        public ViewHolder(FragmentQrshotItemViewBinding binding) {
            super(binding.getRoot());
            qrName = binding.playerQrlistContentName;
            qrScore = binding.playerQrlistContentScore;
        }
    }
}