package com.qrcode_quest.ui.leaderboard;

import static android.content.Context.MODE_PRIVATE;
import static com.qrcode_quest.Constants.AUTHED_USERNAME_PREF;
import static com.qrcode_quest.Constants.SHARED_PREF_PATH;
import static com.qrcode_quest.ui.leaderboard.PlayerListFragmentDirections.actionLeaderboardToPlayerqrs;

import static java.util.Objects.requireNonNull;

import androidx.lifecycle.ViewModelProvider;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.qrcode_quest.databinding.FragmentPlayerListBinding;
import com.qrcode_quest.entities.PlayerAccount;
import com.qrcode_quest.entities.QRCode;
import com.qrcode_quest.entities.QRShot;
import com.qrcode_quest.ui.leaderboard.PlayerListFragmentDirections.ActionLeaderboardToPlayerqrs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 * A view to display or select an arbitrary list of players
 *
 * @author jdumouch
 * @version 1.0
 */
public class PlayerListFragment extends Fragment {
    /** A tag used for logging */
    private static final String CLASS_TAG = "PlayerListFragment";

    /**
     * A data structure for storing player stats
     */
    private class Stats {
        public final String username;
        public int highestCode;
        public int totalScore;
        public int totalCodes;

        public Stats(String user) {
            this.username = user;
            this.highestCode = 0; this.totalCodes = 0; this.totalScore = 0;
        }
    }

    /**
     * States for a player list to display in
     */
    public enum ViewMode {
        LEADERBOARD,
    }

    private PlayerListViewModel viewModel;
    private FragmentPlayerListBinding binding;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(PlayerListViewModel.class);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Grab the view's bindings
        binding = FragmentPlayerListBinding.inflate(inflater, container, false);

        // Init the recycler view to be empty
        RecyclerView recyclerView = binding.playerlistRecycler;
        recyclerView.setLayoutManager(new LinearLayoutManager(recyclerView.getContext()));
        recyclerView.setAdapter(new PlayerViewAdapter(new ArrayList<>(), null));

        binding.playerlistLoadingContainer.setVisibility(View.VISIBLE);
        binding.playerlistMainContainer.setVisibility(View.GONE);

        // Load requisite data. This would be faster pipelined..
        viewModel.getCodes().observe(getViewLifecycleOwner(), codes->{
            viewModel.getShots().observe(getViewLifecycleOwner(), shots->{
                viewModel.getPlayers().observe(getViewLifecycleOwner(), players->{
                    // Calculate each player's scores
                    HashMap<String, Stats> stats = calculatePlayerScores(players,codes,shots);
                    setRanking(stats);

                    // Use those scores to build a displayable list
                    ArrayList<PlayerViewAdapter.PlayerItem> listItems = new ArrayList<>();
                    for (String username : stats.keySet()){
                        int score = requireNonNull(stats.get(username)).totalScore;
                        listItems.add(new PlayerViewAdapter.PlayerItem(username, score));
                    }
                    Collections.sort(listItems, (a,b)->b.score-a.score);

                    // Load the list into the View
                    recyclerView.setAdapter(new PlayerViewAdapter(listItems, this::transitionToQRList));

                    binding.playerlistLoadingContainer.setVisibility(View.GONE);
                    binding.playerlistMainContainer.setVisibility(View.VISIBLE);
                });
            });
        });

        return binding.getRoot();
    }

    /**
     * Transitions the view to a player's QR list
     * @param username The player's username to view QR codes of
     */
    private void transitionToQRList(String username) {
        viewModel.getPlayers().observe(getViewLifecycleOwner(), players->{
            for (PlayerAccount player : players){
                if (player.getUsername().equals(username)){
                    NavController navController = NavHostFragment.findNavController(this);
                    ActionLeaderboardToPlayerqrs action =
                            actionLeaderboardToPlayerqrs(player);
                    navController.navigate(action);
                    return;
                }
            }

            Log.e(CLASS_TAG, "Failed to find player " + username);
        });
    }

    /**
     * Calculates and displays the logged in users ranking relative to the other players.
     * @param stats The hashmap of user stats
     */
    @SuppressLint("DefaultLocale")
    private void setRanking(HashMap<String, Stats> stats){
        // Grab the loaded user
        SharedPreferences prefs = this.requireActivity().getApplicationContext()
                .getSharedPreferences(SHARED_PREF_PATH, MODE_PRIVATE);
        String currentUser = prefs.getString(AUTHED_USERNAME_PREF, "");

        // Grab the loaded users stats
        Stats userStats = requireNonNull(stats.get(currentUser));
        List<Stats> statList = new ArrayList<>(stats.values());

        // Create a hash set for each category (to account for ties)
        HashSet<Integer> totalCodeSet = new HashSet<>();
        HashSet<Integer> totalScoreSet = new HashSet<>();
        HashSet<Integer> bestCaptureSet = new HashSet<>();
        for (Stats stat : stats.values()){
            totalCodeSet.add(stat.totalCodes);
            totalScoreSet.add(stat.totalScore);
            bestCaptureSet.add(stat.highestCode);
        }

        // Rank by total code count
        List<Integer> totalCodes = new ArrayList<>(totalCodeSet);
        Collections.sort(totalCodes, (a,b)->b-a);
        for (int i = 0; i < totalCodes.size(); i++){
            if (totalCodes.get(i) == userStats.totalCodes){
                binding.playerlistTotalcaptures.setText(String.format("%d", i+1));
                break;
            }
        }

        // Rank by total score
        List<Integer> totalScore = new ArrayList<>(totalScoreSet);
        Collections.sort(totalScore, (a,b)->b-a);
        for (int i = 0; i < totalScore.size(); i++){
            if (totalScore.get(i) == userStats.totalScore){
                binding.playerlistTotalscore.setText(String.format("%d", i+1));
                break;
            }
        }

        // Rank by best capture
        List<Integer> bestCapture = new ArrayList<>(bestCaptureSet);
        Collections.sort(bestCapture, (a,b)->b-a);
        for (int i = 0; i < bestCapture.size(); i++){
            if (bestCapture.get(i) == userStats.highestCode){
                binding.playerlistBestcapture.setText(String.format("%d", i+1));
                break;
            }
        }
    }

    /**
     * Builds a HashMap containing calculated player stats
     * @param players The list of players
     * @param codes The hashmap of QRCodes
     * @param shots The list of QRShots
     * @return The calculated stats
     */
    private HashMap<String, Stats> calculatePlayerScores(ArrayList<PlayerAccount> players,
                                       HashMap<String, QRCode> codes,
                                       ArrayList<QRShot> shots) {

        // Create a zeroed score list
        HashMap<String, Stats> stats = new HashMap<>();
        for (PlayerAccount player : players){
            String username = player.getUsername();
            stats.put(username , new Stats(username));
        }

        // Sum all the scores off all available shots
        for (QRShot shot : shots){
            // Grab the required objects to calculate stats
            QRCode code = requireNonNull(codes.get(shot.getCodeHash()));
            Stats current = stats.get(shot.getOwnerName());

            // Skip codes of deleted players
            if (current == null) { continue; }

            // Update stats
            current.totalScore += code.getScore();
            current.totalCodes++;
            current.highestCode = Math.max(current.highestCode, code.getScore());
            // Store back into the hashmap
            stats.put(shot.getOwnerName(), current);
        }

        return stats;
    }
}