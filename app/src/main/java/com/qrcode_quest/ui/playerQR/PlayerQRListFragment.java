package com.qrcode_quest.ui.playerQR;

import static com.qrcode_quest.ui.playerQR.PlayerQRListFragmentDirections.actionPlayerqrsToQrview;
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
import androidx.navigation.ui.AppBarConfiguration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.qrcode_quest.databinding.FragmentPlayerQrShotsBinding;
import com.qrcode_quest.entities.PlayerAccount;
import com.qrcode_quest.entities.QRCode;
import com.qrcode_quest.entities.QRShot;
import com.qrcode_quest.ui.playerQR.PlayerQRListFragmentDirections.ActionPlayerqrsToQrview;

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

        // Load the player argument
        PlayerAccount player = PlayerQRListFragmentArgs.fromBundle(getArguments()).getPlayer();

        // Grab the action bar from MainActivity
        AppCompatActivity main = (AppCompatActivity) this.getActivity();
        ActionBar actionBar = requireNonNull((requireNonNull(main)).getSupportActionBar());
        // Set custom title
        actionBar.setTitle(String.format("%s's Captures", player.getUsername()));

        // Load the view models
        PlayerQRListViewModel viewModel =
                new ViewModelProvider(this).get(PlayerQRListViewModel.class);

        // Grab the view binding
        binding = FragmentPlayerQrShotsBinding.inflate(inflater, container, false);

        // Display the loading spinner and hide the list
        binding.playerQrlistProgress.setVisibility(View.VISIBLE);
        binding.playerQrlistRecyclerview.setVisibility(View.INVISIBLE);

        // Set up the RecyclerView
        RecyclerView recyclerView = binding.playerQrlistRecyclerview;
        Context context = recyclerView.getContext();
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(
                new PlayerQRShotViewAdapter(new ArrayList<>(), new HashMap<>(), s->{})
        );
        setStats(null, null);

        // Load QRShot/QRCode data into the RecyclerView
        viewModel.getPlayerShots(player.getUsername()).observe(getViewLifecycleOwner(), shots ->{
            viewModel.getCodes().observe(getViewLifecycleOwner(), codes -> {
                recyclerView.setAdapter(new PlayerQRShotViewAdapter(shots, codes, this::transitionTo));
                // Use the data to load the stats card
                setStats(shots, codes);

                // Hide the loading spinner and display the List
                binding.playerQrlistProgress.setVisibility(View.INVISIBLE);
                binding.playerQrlistRecyclerview.setVisibility(View.VISIBLE);
            });
        });

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    /**
     * Takes the QRShot information to build user stats
     * @param shots The QRShots for the player
     * @param codes The list of QRCodes (containing at minimum the shots)
     */
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

        // Init stat variables
        int score = 0; int max = 0; int min = Integer.MAX_VALUE;

        // Iterate through the QRShots, grabbing the score from its relevant code
        for (QRShot shot : shots) {
            QRCode qrCode = codes.get(shot.getCodeHash());
            if (qrCode == null) { continue; }
            int codeScore = qrCode.getScore();

            // Update stats
            score += codeScore;
            max = Math.max(max, codeScore);
            min = Math.min(min, codeScore);
        }

        // Update the View with the newly calculated info
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
        ActionPlayerqrsToQrview action =
                actionPlayerqrsToQrview(shot.getOwnerName(), shot.getCodeHash());
        navController.navigate(action);
    }
}