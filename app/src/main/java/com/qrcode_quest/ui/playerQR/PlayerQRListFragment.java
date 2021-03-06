package com.qrcode_quest.ui.playerQR;

import static com.qrcode_quest.ui.playerQR.PlayerQRListFragmentDirections.actionPlayerqrsToQrview;
import static java.util.Objects.requireNonNull;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.qrcode_quest.MainViewModel;
import com.qrcode_quest.application.AppContainer;
import com.qrcode_quest.application.QRCodeQuestApp;
import com.qrcode_quest.database.PlayerManager;
import com.qrcode_quest.database.QRManager;
import com.qrcode_quest.databinding.FragmentPlayerQrShotsBinding;
import com.qrcode_quest.entities.PlayerAccount;
import com.qrcode_quest.entities.QRShot;
import com.qrcode_quest.entities.RawQRCode;
import com.qrcode_quest.ui.playerQR.PlayerQRListFragmentDirections.ActionPlayerqrsToQrview;

import java.util.ArrayList;
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

    private MainViewModel mainViewModel;
    private FragmentPlayerQrShotsBinding binding;
    private PlayerAccount player;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        // Load the player argument
        player = PlayerQRListFragmentArgs.fromBundle(getArguments()).getPlayer();

        // Grab the action bar from MainActivity
        AppCompatActivity main = (AppCompatActivity) this.getActivity();
        ActionBar actionBar = requireNonNull((requireNonNull(main)).getSupportActionBar());
        // Set custom title
        actionBar.setTitle(String.format("%s's Captures", player.getUsername()));

        AppContainer appContainer = ((QRCodeQuestApp) getActivity().getApplication()).getContainer();
        ViewModelProvider.Factory playerQRListViewModelFactory = new ViewModelProvider.Factory() {
            @NonNull
            @Override
            public <T extends ViewModel> T create(@NonNull Class<T> aClass) {
                if (aClass.isAssignableFrom(PlayerQRListViewModel.class))
                    return Objects.requireNonNull(aClass.cast(new PlayerQRListViewModel(
                            new QRManager(appContainer.getDb(), appContainer.getStorage()))));
                else
                    throw new IllegalArgumentException("Unexpected ViewModelClass type request received by the factory!");
            }
        };

        // Load the view models
        PlayerQRListViewModel viewModel =
                new ViewModelProvider(this, playerQRListViewModelFactory).get(PlayerQRListViewModel.class);

        mainViewModel =
                new ViewModelProvider(requireActivity()).get(MainViewModel.class);

        // Grab the view binding
        binding = FragmentPlayerQrShotsBinding.inflate(inflater, container, false);
        binding.playerQrlistProgress.setVisibility(View.VISIBLE);
        binding.playerQrlistRecyclerview.setVisibility(View.GONE);

        // Set up the RecyclerView
        RecyclerView recyclerView = binding.playerQrlistRecyclerview;
        Context context = recyclerView.getContext();
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(
                new PlayerQRShotViewAdapter(new ArrayList<>(), s->{})
        );
        setStats(null);

        // Load QRShot/QRCode data into the RecyclerView
        viewModel.getPlayerShots(player.getUsername()).observe(getViewLifecycleOwner(), shots ->{
            recyclerView.setAdapter(new PlayerQRShotViewAdapter(shots, this::transitionTo));
            // Use the data to load the stats card
            setStats(shots);

            // Hide the loading spinner and display the List (or no capture label
            binding.playerQrlistProgress.setVisibility(View.GONE);
            binding.playerQrlistNocaptures.setVisibility(shots.size() > 0 ? View.GONE : View.VISIBLE);
            binding.playerQrlistRecyclerview.setVisibility(shots.size() == 0 ? View.GONE : View.VISIBLE);
        });

        // Enable the delete user button for privileged users
        mainViewModel.getCurrentPlayer().observe(getViewLifecycleOwner(), authedUser -> {
            if (authedUser.isOwner()){
                binding.playerQrlistDeleteplayerButton.setVisibility(View.VISIBLE);
                binding.playerQrlistDeleteplayerButton.setOnClickListener(v->{
                    if (!authedUser.getUsername().equals(player.getUsername())){
                        deleteSelectedUser();
                    }
                    else {
                        Toast.makeText(getContext(), "Cannot delete yourself!", Toast.LENGTH_LONG).show();
                    }
                });
            }
        });

        return binding.getRoot();
    }

    /** delete the user corresponding to the current page */
    private void deleteSelectedUser(){
        AppContainer container = ((QRCodeQuestApp) requireActivity().getApplication()).getContainer();
        binding.playerQrlistProgress.setVisibility(View.VISIBLE);
        new PlayerManager(container.getDb()).setDeletedPlayer(player.getUsername(), true, result ->{
            if (!result.isSuccess()){
                Toast.makeText(getContext(), "Failed to delete player", Toast.LENGTH_SHORT).show();
                Log.e(CLASS_TAG, "Player delete call failed: " + result.getError().getMessage());
                binding.playerQrlistProgress.setVisibility(View.GONE);
                return;
            }

            // Force a reload on players so that the view model reflects changes
            mainViewModel.loadPlayers();

            NavController navController = NavHostFragment.findNavController(this);
            navController.popBackStack();
        });
    }

    /**
     * Takes the QRShot information to build user stats
     * @param shots The QRShots for the player
     */
    @SuppressLint("DefaultLocale")
    private void setStats(ArrayList<QRShot> shots){
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
            int codeScore = RawQRCode.getScoreFromHash(shot.getCodeHash());

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