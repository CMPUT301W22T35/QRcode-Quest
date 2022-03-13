package com.qrcode_quest.ui.playerQR;

import static com.qrcode_quest.ui.playerQR.PlayerQRListFragmentDirections.actionPlayerQrlistToQrshot;

import static java.util.Objects.requireNonNull;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.qrcode_quest.MainViewModel;
import com.qrcode_quest.databinding.FragmentPlayerQrShotsBinding;
import com.qrcode_quest.entities.PlayerAccount;
import com.qrcode_quest.entities.QRCode;
import com.qrcode_quest.entities.QRShot;
import com.qrcode_quest.ui.playerQR.PlayerQRListFragmentDirections.ActionPlayerQrlistToQrshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;


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
        recyclerView.setAdapter(
                new PlayerQRShotViewAdapter(new ArrayList<>(), new HashMap<>(), s->{})
        );
        setStats(null, null);

        // Load QRShot/QRCode data into the RecyclerView
        viewModel.getPlayerShots(player.getUsername()).observe(getViewLifecycleOwner(), shots ->{
            mainViewModel.getCodes().observe(getViewLifecycleOwner(), codes -> {
                recyclerView.setAdapter(new PlayerQRShotViewAdapter(shots, codes, this::transitionTo));

                setStats(shots, codes);

                binding.playerQrlistProgress.setVisibility(View.INVISIBLE);
                binding.playerQrlistRecyclerview.setVisibility(View.VISIBLE);
            });
        });

        // Grab the action bar from MainActivity
        AppCompatActivity main = (AppCompatActivity) this.getActivity();
        ActionBar actionBar = requireNonNull((requireNonNull(main)).getSupportActionBar());

        // Hide the back arrow because its broken garbage juice
        actionBar.setDisplayHomeAsUpEnabled(false);
        // Set custom title
        actionBar.setTitle(String.format("%s's Captures", player.getUsername()));

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
        if (shots == null || shots.size() == 0){
            binding.playerQrlistLowest.setText("--");
            binding.playerQrlistHighest.setText("--");
            binding.playerQrlistScore.setText("--");
            binding.playerQrlistTotal.setText("--");
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

    /**
     * Transitions from the player's QRShot list to the QRView of a specific shot.
     * @param shot The QRShot to view
     */
    public void transitionTo(QRShot shot){
        // Navigate to the QRView of the clicked shot
        NavController navController = NavHostFragment.findNavController(this);
        ActionPlayerQrlistToQrshot action =
                actionPlayerQrlistToQrshot(shot.getOwnerName(), shot.getCodeHash());
        navController.navigate(action);
    }
}