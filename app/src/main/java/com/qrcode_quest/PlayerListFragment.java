package com.qrcode_quest;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.qrcode_quest.entities.PlayerAccount;

import java.util.ArrayList;

/**
 * A fragment representing a list of Players.
 */
public class PlayerListFragment extends Fragment {
    ListView playerList;
    ArrayAdapter<PlayerAccount> playerAdapter;
    ArrayList<PlayerAccount> playerDataList;

    private static final String ARG_PLAYERS = "players";
    private String[] players;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public PlayerListFragment() {

    }

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

        if (getArguments() != null) {
            players = getArguments().getStringArray(ARG_PLAYERS);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_player_list, container, false);

        playerList = view.findViewById(R.id.player_list);
        playerDataList = new ArrayList<>();

        for (int i=0;i<players.length;i++){
            playerDataList.add(new PlayerAccount(players[i]));
        }

        playerAdapter = new CustomPlayerList(this.getContext(), playerDataList);
        playerList.setAdapter(playerAdapter);

        return view;
    }
}