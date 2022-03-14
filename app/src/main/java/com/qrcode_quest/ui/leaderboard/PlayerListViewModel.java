package com.qrcode_quest.ui.leaderboard;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;


/**
 * @author tianming
 * @version 0.1
 */
public class PlayerListViewModel extends ViewModel {

    private final MutableLiveData<ArrayList<CustomPlayerList.PlayerScore>> playerScoreList;

    public PlayerListViewModel() {
        playerScoreList = new MutableLiveData<>();
    }

    public MutableLiveData<ArrayList<CustomPlayerList.PlayerScore>> getPlayerScoreList() {
        return playerScoreList;
    }
}