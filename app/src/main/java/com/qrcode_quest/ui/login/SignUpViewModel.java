package com.qrcode_quest.ui.login;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.qrcode_quest.database.PlayerManager;
import com.qrcode_quest.entities.PlayerAccount;

import java.util.ArrayList;

public class SignUpViewModel extends ViewModel {
    /** A tag used for logging */
    private static final String CLASS_TAG = "SignUpViewModel";


    /**
     * Retrieves the player list from the view model.
     */
    public LiveData<ArrayList<PlayerAccount>> getPlayers(){
        if (players == null){
            players = new MutableLiveData<>();
            loadPlayers();
        }

        return players;
    }
    private MutableLiveData<ArrayList<PlayerAccount>> players;


    /**
     * Loads the player list from the database into the view model
     */
    private void loadPlayers(){
        new PlayerManager().getPlayerList(result -> {
            if (!result.isSuccess()){
                Log.e(CLASS_TAG, "Failed to load player list.");
                return;
            }

            players.setValue(result.unwrap());
        });
    }
}

