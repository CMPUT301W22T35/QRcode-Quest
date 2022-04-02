package com.qrcode_quest.ui.map;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import com.qrcode_quest.databinding.MapListItemViewBinding;
import com.qrcode_quest.ui.map.MapListContent.MapListItem;

import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display {@link MapListContent}.
 *
 * @author ageolleg
 * @version 1.0
 */
public class MapListViewAdapter extends RecyclerView.Adapter<MapListViewAdapter.ViewHolder> {

    private final List<MapListItem> items;

    public MapListViewAdapter(List<MapListItem> items) {
        this.items = items;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

    return new ViewHolder(MapListItemViewBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));

    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        MapListItem item = items.get(position);

        holder.mapListViewItem = item;
        holder.score.setText(String.format("%d pts", item.score));
        holder.distance.setText(String.format("%.2fm away", item.distance));
        holder.latLon.setText(String.format("Latitude: %.5f Longitude: %.5f", item.latitude, item.longitude));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView distance;
        public final TextView latLon;
        public final TextView score;
        public MapListItem mapListViewItem;

    public ViewHolder(@NonNull MapListItemViewBinding binding) {
      super(binding.getRoot());
        distance = binding.mapListDistance;
        latLon = binding.mapListLatLon;
        score = binding.mapListScore;
    }

    }
}