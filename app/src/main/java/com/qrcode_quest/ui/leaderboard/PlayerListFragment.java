package com.qrcode_quest.ui.leaderboard;


import static com.qrcode_quest.ui.leaderboard.LeaderboardFragmentDirections.*;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;
import com.qrcode_quest.MainViewModel;
import com.qrcode_quest.R;
import com.qrcode_quest.database.ManagerResult;
import com.qrcode_quest.database.PlayerManager;
import com.qrcode_quest.database.QRManager;
import com.qrcode_quest.database.Result;
import com.qrcode_quest.entities.PlayerAccount;
import com.qrcode_quest.entities.QRCode;
import com.qrcode_quest.entities.QRShot;
import com.qrcode_quest.ui.leaderboard.LeaderboardFragmentDirections.ActionNavigationLeaderboardToNavigationPlayerQrlist;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

/**
 * A fragment representing a list of Players.
 *
 * @author ageolleg
 * @version 0.1
 */
public class PlayerListFragment extends Fragment {
    ListView playerList;
    CustomPlayerList playerAdapter;

    private static final String ARG_PLAYERS = "players";

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public PlayerListFragment() {}

    public static PlayerListFragment newInstance(String[] players) {
        PlayerListFragment fragment = new PlayerListFragment();
        Bundle args = new Bundle();
        args.putStringArray(ARG_PLAYERS, players);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {}
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_player_list, container, false);

        playerList = view.findViewById(R.id.player_list);
        // first create a list of placeholder data, then when the data are loaded they are replaced
        // with the actual data
        playerAdapter = CustomPlayerList.getInstanceWithPlaceholderData(this.getContext());
        playerList.setAdapter(playerAdapter);

        // pass data to the adapter
        MainViewModel mainViewModel =
                new ViewModelProvider(this.getActivity()).get(MainViewModel.class);
        playerAdapter.setDataSources(getViewLifecycleOwner(),
                mainViewModel.getPlayers(), mainViewModel.getQRShots());

        // retrieve player accounts, qr codes and scores then perform aggregation to compute the player-score pairs
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        new PlayerManager(db).getPlayerList(result -> {
            if (result.isSuccess()) {
                new QRManager(db).getAllQRShots(result1 -> {
                    if(result.isSuccess()) {
                        new QRManager(db).getAllQRCodesAsMap(new ManagerResult.Listener<HashMap<String, QRCode>>() {
                            @Override
                            public void onResult(Result<HashMap<String, QRCode>> result2) {
                                if(result2.isSuccess()) {
                                    ArrayList<PlayerAccount> accounts = result.unwrap();
                                    ArrayList<QRShot> shots = result1.unwrap();
                                    HashMap<String, ArrayList<QRShot>> shotMap = new HashMap<>();
                                    for (PlayerAccount account: accounts) {
                                        shotMap.put(account.getUsername(), new ArrayList<>());
                                    }
                                    for(QRShot shot: shots) {
                                        if(shotMap.containsKey(shot.getOwnerName())) {
                                            Objects.requireNonNull(shotMap.get(shot.getOwnerName())).add(shot);
                                        }
                                    }
                                    HashMap<String, QRCode> codes = result2.unwrap();

                                    ArrayList<PlayerScore> playerScores = new ArrayList<>();
                                    for (PlayerAccount account: accounts) {
                                        int score = 0;
                                        for (QRShot shot: Objects.requireNonNull(shotMap.get(account.getUsername()))) {
                                            if (codes.containsKey(shot.getCodeHash())) {
                                                score += Objects.requireNonNull(codes.get(shot.getCodeHash())).getScore();
                                            }
                                        }
                                        PlayerScore ps = new PlayerScore(
                                                account, score);
                                        playerScores.add(ps);
                                    }

                                    // setup listener
                                    setupListener();
                                } else {
                                    Toast toast = Toast.makeText(getActivity(),
                                            "Retrieve qr codes failed with error " + result2.getError().getMessage(),
                                            Toast.LENGTH_SHORT);
                                    toast.show();
                                }
                            }
                        });
                    } else {
                        Toast toast = Toast.makeText(getActivity(),
                                "Retrieve scores failed with error " + result1.getError().getMessage(),
                                Toast.LENGTH_SHORT);
                        toast.show();
                    }
                });
            } else {
                Toast toast = Toast.makeText(getActivity(),
                        "Retrieve player list failed with error " + result.getError().getMessage(),
                        Toast.LENGTH_SHORT);
                toast.show();
            }
        });

        return view;
    }

    void setupListener() {
        playerList.setOnItemClickListener((adapterView, itemView, i, l) -> {
            if (!playerAdapter.isDataPlaceHolder()) {
                PlayerScore playerScore = (PlayerScore) adapterView.getItemAtPosition(i);
                PlayerAccount player = playerScore.m_account;

                NavController navController = NavHostFragment.findNavController(this);
                ActionNavigationLeaderboardToNavigationPlayerQrlist action =
                        actionNavigationLeaderboardToNavigationPlayerQrlist(player);
                navController.navigate(action);
            }
        });
    }
}