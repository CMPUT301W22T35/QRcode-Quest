package com.qrcode_quest.ui.playerQR;

import androidx.lifecycle.ViewModelProvider;

import android.annotation.SuppressLint;
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
import com.qrcode_quest.entities.QRCode;
import com.qrcode_quest.entities.QRShot;

import java.util.ArrayList;
import java.util.HashMap;


/**
 * A view for displaying the QR codes a player has captured.
 *
 * @author jdumouch
 * @version 1.0
 */
public class PlayerQRListFragment extends Fragment {
    /** A tag used in logging */
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

        binding.playerQrlistProgress.setVisibility(View.VISIBLE);
        binding.playerQrlistRecyclerview.setVisibility(View.INVISIBLE);

        // Set the adapter
        RecyclerView recyclerView = binding.playerQrlistRecyclerview;
        Context context = recyclerView.getContext();
        recyclerView.setLayoutManager(new LinearLayoutManager(context));

        // Load QRShot/QRCode data into the RecyclerView
        viewModel.getPlayerShots(player.getUsername()).observe(getViewLifecycleOwner(), shots ->{
            mainViewModel.getCodes().observe(getViewLifecycleOwner(), codes -> {
                recyclerView.setAdapter(new PlayerQRShotViewAdapter(shots, codes));
                setStats(shots, codes);

                binding.playerQrlistProgress.setVisibility(View.INVISIBLE);
                binding.playerQrlistRecyclerview.setVisibility(View.VISIBLE);
            });
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @SuppressLint("DefaultLocale")
    private void setStats(ArrayList<QRShot> shots, HashMap<String, QRCode> codes){
        // Handle empty stats
        if (shots.size() == 0){
            binding.playerQrlistLowest.setText("--");
            binding.playerQrlistHighest.setText("--");
            binding.playerQrlistScore.setText("--");
            binding.playerQrlistTotal.setText("0");
            return;
        }

        int score = 0; int max = 0; int min = Integer.MAX_VALUE;
        for (QRShot shot : shots) {
            QRCode qrCode = codes.get(shot.getCodeHash());
            if (qrCode == null) { continue; }
            int codeScore = qrCode.getScore();

            score += codeScore;
            max = Math.max(max, codeScore);
            min = Math.min(min, codeScore);
        }

        binding.playerQrlistLowest.setText(String.format("%d", min));
        binding.playerQrlistHighest.setText(String.format("%d", max));
        binding.playerQrlistScore.setText(String.format("%d", score));
        binding.playerQrlistTotal.setText(String.format("%d", shots.size()));
    }

}