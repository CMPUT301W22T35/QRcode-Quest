package com.qrcode_quest.ui.playerQR;

import androidx.lifecycle.ViewModelProvider;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.qrcode_quest.MainViewModel;
import com.qrcode_quest.database.QRManager;
import com.qrcode_quest.databinding.FragmentPlayerQrShotsBinding;
import com.qrcode_quest.entities.PlayerAccount;

import java.util.ArrayList;


/**
 * A view for displaying the QR codes a player has captured.
 *
 * @author jdumouch
 * @version 1.0
 */
public class PlayerQRListFragment extends Fragment {
    private static final String CLASS_TAG = "PlayerQRListFragment";

    private FragmentPlayerQrShotsBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        PlayerAccount player = PlayerQRListFragmentArgs.fromBundle(getArguments()).getPlayer();

        PlayerQRListViewModel viewModel =
                new ViewModelProvider(this).get(PlayerQRListViewModel.class);

        MainViewModel mainViewModel =
                new ViewModelProvider(this.requireActivity()).get(MainViewModel.class);

        binding = FragmentPlayerQrShotsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        RecyclerView recyclerView = binding.playerQrlistRecyclerview;

        // Set the adapter
        Context context = recyclerView.getContext();
        recyclerView.setLayoutManager(new LinearLayoutManager(context));

        viewModel.getPlayerShots(player.getUsername()).observe(getViewLifecycleOwner(), shots ->{
            mainViewModel.getCodes().observe(getViewLifecycleOwner(), codes -> {
                recyclerView.setAdapter(new PlayerQRShotViewAdapter(shots, codes));
            });
        });

        return root;
    }



    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}