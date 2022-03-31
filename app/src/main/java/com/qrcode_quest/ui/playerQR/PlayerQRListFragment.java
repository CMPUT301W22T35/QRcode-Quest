package com.qrcode_quest.ui.playerQR;

import static com.qrcode_quest.ui.playerQR.PlayerQRListFragmentDirections.actionPlayerqrsToQrview;
import static java.util.Objects.requireNonNull;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.qrcode_quest.MainViewModel;
import com.qrcode_quest.R;
import com.qrcode_quest.application.AppContainer;
import com.qrcode_quest.application.QRCodeQuestApp;
import com.qrcode_quest.database.QRManager;
import com.qrcode_quest.database.PlayerManager;
import com.qrcode_quest.entities.PlayerAccount;
import com.qrcode_quest.entities.QRShot;
import com.qrcode_quest.entities.RawQRCode;
import com.qrcode_quest.ui.playerQR.PlayerQRListFragmentDirections.ActionPlayerqrsToQrview;


import java.util.ArrayList;


/**
 * A view for displaying the QR codes a player has captured.
 *
 * @author jdumouch
 * @version 1.1
 */
public class PlayerQRListFragment extends Fragment {
    /** A tag used in logging */
    private static final String CLASS_TAG = "PlayerQRListFragment";

    private MainViewModel mainViewModel;
    private PlayerAccount player;
    private View thisView;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        thisView = inflater.inflate(R.layout.fragment_player_qr_shots, container, false);

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
                    return requireNonNull(aClass.cast(new PlayerQRListViewModel(
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

        // Grab the Views
        View progressView = thisView.findViewById(R.id.player_qrlist_progress);
        View noCapturesLabel = thisView.findViewById(R.id.player_qrlist_nocaptures);
        RecyclerView playerListRecycler = thisView.findViewById(R.id.player_qrlist_recyclerview);

        // Set the progress view as the default view
        progressView.setVisibility(View.VISIBLE);
        playerListRecycler.setVisibility(View.GONE);

        // Set up the RecyclerView
        Context context = playerListRecycler.getContext();
        playerListRecycler.setLayoutManager(new LinearLayoutManager(context));
        playerListRecycler.setAdapter(
                new PlayerQRShotViewAdapter(new ArrayList<>(), s->{})
        );
        setStats(null);

        // Load QRShot/QRCode data into the RecyclerView
        viewModel.getPlayerShots(player.getUsername()).observe(getViewLifecycleOwner(), shots ->{
            playerListRecycler.setAdapter(new PlayerQRShotViewAdapter(shots, this::transitionTo));
            // Use the data to load the stats card
            setStats(shots);

            // Hide the loading spinner and display the List (or no capture label
            progressView.setVisibility(View.GONE);
            noCapturesLabel.setVisibility(shots.size() > 0 ? View.GONE : View.VISIBLE);
            playerListRecycler.setVisibility(shots.size() == 0 ? View.GONE : View.VISIBLE);
        });

        // Enable the delete user button for privileged
        mainViewModel.getCurrentPlayer().observe(getViewLifecycleOwner(), authedUser -> {
            if (authedUser.isOwner()){
                Button deleteButton = thisView.findViewById(R.id.player_qrlist_deleteplayer_button);
                deleteButton.setVisibility(View.VISIBLE);
                deleteButton.setOnClickListener(v->{
                    if (!authedUser.getUsername().equals(player.getUsername())){
                        deleteSelectedUser();
                    }
                    else {
                        Toast.makeText(getContext(), "Cannot delete yourself!", Toast.LENGTH_LONG).show();
                    }
                });
            }
        });

        return thisView;
    }

    private void deleteSelectedUser(){
        AppContainer container = ((QRCodeQuestApp) requireActivity().getApplication()).getContainer();
        View progressView = thisView.findViewById(R.id.player_qrlist_progress);

        progressView.setVisibility(View.VISIBLE);
        new PlayerManager(container.getDb()).setDeletedPlayer(player.getUsername(), true, result ->{
            if (!result.isSuccess()){
                Toast.makeText(getContext(), "Failed to delete player", Toast.LENGTH_SHORT).show();
                Log.e(CLASS_TAG, "Player delete call failed: " + result.getError().getMessage());
                progressView.setVisibility(View.GONE);
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
        // Grab the needed views
        TextView lowestText = thisView.findViewById(R.id.player_qrlist_lowest);
        TextView highestText = thisView.findViewById(R.id.player_qrlist_highest);
        TextView scoreText = thisView.findViewById(R.id.player_qrlist_score);
        TextView totalText = thisView.findViewById(R.id.player_qrlist_total);

        // Handle empty stats
        if (shots == null || shots.size() == 0){
            lowestText.setText("--");
            highestText.setText("--");
            scoreText.setText("--");
            totalText.setText("--");
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
        lowestText.setText(String.format("%d", min));
        highestText.setText(String.format("%d", max));
        scoreText.setText(String.format("%d", score));
        totalText.setText(String.format("%d", shots.size()));
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