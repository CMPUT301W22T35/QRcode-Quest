package com.qrcode_quest.ui.leaderboard;

import static android.content.Context.MODE_PRIVATE;
import static com.qrcode_quest.Constants.AUTHED_USERNAME_PREF;
import static com.qrcode_quest.Constants.SHARED_PREF_PATH;
import static com.qrcode_quest.ui.leaderboard.PlayerListFragmentDirections.actionLeaderboardToPlayerqrs;

import static java.util.Objects.requireNonNull;

import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;

import android.annotation.SuppressLint;
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
import android.widget.SearchView;

import com.qrcode_quest.MainViewModel;
import com.qrcode_quest.database.SchemaResultHelper;
<<<<<<< HEAD
import com.qrcode_quest.databinding.FragmentPlayerListBinding;
=======
>>>>>>> main
import com.qrcode_quest.entities.PlayerAccount;
import com.qrcode_quest.entities.QRCode;
import com.qrcode_quest.entities.QRShot;

import java.util.ArrayList;
import java.util.HashMap;

<<<<<<< HEAD
=======
import com.qrcode_quest.databinding.FragmentPlayerListBinding;
>>>>>>> main
import com.qrcode_quest.ui.leaderboard.PlayerListFragmentDirections.ActionLeaderboardToPlayerqrs;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;

/**
 * A view to display the global leaderboard
 *
 * @author jdumouch
 * @version 1.0
 */
public class PlayerListFragment extends Fragment {
    private static final String ARG_PLAYERS = "players";

    /** A tag used for logging */
    private static final String CLASS_TAG = "PlayerListFragment";

    private FragmentPlayerListBinding binding;
    private MainViewModel mainViewModel;
    private PlayerViewAdapter viewAdapter;

    /** Empty constructor for android to use */
    public PlayerListFragment() {}

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

<<<<<<< HEAD
        // Connect the LiveData sources
        mainViewModel = new ViewModelProvider(this.requireActivity()).get(MainViewModel.class);

        LifecycleOwner owner = getViewLifecycleOwner();
        LiveData<ArrayList<PlayerAccount>> allAccounts = mainViewModel.getPlayers();
        LiveData<ArrayList<QRShot>> allShots = mainViewModel.getQRShots();

        // define update callback
        PlayerViewAdapter.SourceUpdateHandler updateHandler = () -> {
            ArrayList<PlayerAccount> players = allAccounts.getValue();
            ArrayList<QRShot> shots = allShots.getValue();
            HashMap<String, PlayerStats> stats = calculatePlayerScores(players, shots);
            setRanking(stats);

            // Use those scores to build a displayable list
            ArrayList<PlayerViewItem> listItems = new ArrayList<>();
            for (String username : stats.keySet()){
                int score = requireNonNull(stats.get(username)).totalScore;
                listItems.add(new PlayerViewItem(username, score));
            }
            Collections.sort(listItems, (a,b)->b.score-a.score);

            // Load the list into the View
            viewAdapter = new PlayerViewAdapter(listItems, this::transitionToQRList);
            viewAdapter.getFilter().filter(binding.playerlistSearchview.getQuery());
            recyclerView.setAdapter(viewAdapter);

            binding.playerlistLoadingContainer.setVisibility(View.GONE);
            binding.playerlistMainContainer.setVisibility(View.VISIBLE);
        };

        // When any of the sources change, update the leaderboard
        allAccounts.observe(owner, playerAccounts -> updateHandler.onSourceUpdate());
        allShots.observe(owner, playerAccounts -> updateHandler.onSourceUpdate());

        // Handle search queries
        binding.playerlistSearchview.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                viewAdapter.getFilter().filter(s);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                if (s.isEmpty() && viewAdapter != null){
                    viewAdapter.getFilter().filter(null);
                }
                return false;
            }
        });
        // Handle closing the search view
        binding.playerlistSearchview.setOnCloseListener(() -> {
            viewAdapter.getFilter().filter(null);
            return false;
        });

        return binding.getRoot();
    }

    /**
     * Transitions the view to a player's QR list
     * @param username The player's username to view QR codes of
     */
    private void transitionToQRList(String username) {
        mainViewModel.getPlayers().observe(getViewLifecycleOwner(), players->{
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
    private void setRanking(HashMap<String, PlayerStats> stats){
        // Grab the loaded user
        SharedPreferences prefs = this.requireActivity().getApplicationContext()
                .getSharedPreferences(SHARED_PREF_PATH, MODE_PRIVATE);
        String currentUser = prefs.getString(AUTHED_USERNAME_PREF, "");

        // Grab the loaded users stats
        PlayerStats userStats = stats.get(currentUser);
        // In case the currentUser and global user list is not in sync
        if (userStats == null) { return; }

        List<PlayerStats> statList = new ArrayList<>(stats.values());

        // Create a hash set for each category (to account for ties)
        HashSet<Integer> totalCodeSet = new HashSet<>();
        HashSet<Integer> totalScoreSet = new HashSet<>();
        HashSet<Integer> bestCaptureSet = new HashSet<>();
        for (PlayerStats stat : stats.values()){
            totalCodeSet.add(stat.totalCodes);
            totalScoreSet.add(stat.totalScore);
            bestCaptureSet.add(stat.highestCode);
        }

        // Rank by total code count
        List<Integer> totalCodes = new ArrayList<>(totalCodeSet);
        Collections.sort(totalCodes, (a,b)->b-a);
        for (int i = 0; i < totalCodes.size(); i++){
            if (totalCodes.get(i) == userStats.totalCodes){
                binding.playerlistTotalcaptures.setText(
                        String.format("%d%s", i+1, getOrdinalAffix(i+1)));
                break;
            }
        }

        // Rank by total score
        List<Integer> totalScore = new ArrayList<>(totalScoreSet);
        Collections.sort(totalScore, (a,b)->b-a);
        for (int i = 0; i < totalScore.size(); i++){
            if (totalScore.get(i) == userStats.totalScore){
                binding.playerlistTotalscore.setText(
                        String.format("%d%s", i+1, getOrdinalAffix(i+1)));
                break;
            }
        }

        // Rank by best capture
        List<Integer> bestCapture = new ArrayList<>(bestCaptureSet);
        Collections.sort(bestCapture, (a,b)->b-a);
        for (int i = 0; i < bestCapture.size(); i++){
            if (bestCapture.get(i) == userStats.highestCode){
                binding.playerlistBestcapture.setText(
                        String.format("%d%s", i+1, getOrdinalAffix(i+1)));
                break;
            }
        }
    }

    /**
     * Builds a HashMap containing calculated player stats
     * @param players The list of players
     * @param shots The list of QRShots
     * @return The calculated stats
     */
    private HashMap<String, PlayerStats> calculatePlayerScores(ArrayList<PlayerAccount> players,
                                                               ArrayList<QRShot> shots) {
        HashMap<String, ArrayList<QRCode>> playerToCodes =
                SchemaResultHelper.getOwnerNameToCodeArrayMapFromJoin(players, shots);

        HashMap<String, PlayerStats> stats = new HashMap<>();
        for (PlayerAccount player : players){
            String username = player.getUsername();
            PlayerStats current = new PlayerStats(username);  // Create a zeroed score list of stats

            ArrayList<QRCode> ownedCodes = playerToCodes.get(username);
            assert ownedCodes != null;
            for(QRCode code: ownedCodes) {
                // Update stats
                current.totalScore += code.getScore();
                current.totalCodes++;
                current.highestCode = Math.max(current.highestCode, code.getScore());
            }

            // Store back into the hashmap
            stats.put(username , current);
        }

        return stats;
    }

    /**
     * Finds the correct ordinal affix (#st, #nd, #rd) for english numerals
     * @param number The number to find the affix for
     * @return The ordinal affix as a string
     */
    private String getOrdinalAffix(int number){
        int lastDigit = number % 10;
        switch (lastDigit){
            case 3:
                return "rd";
            case 2:
                return "nd";
            default:
                return "st";
        }
    }
}