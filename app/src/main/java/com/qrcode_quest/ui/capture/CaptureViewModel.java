package com.qrcode_quest.ui.capture;

import androidx.lifecycle.ViewModel;

import com.qrcode_quest.entities.Geolocation;
import com.qrcode_quest.entities.PlayerAccount;

/**
 * contains the state for the capture fragment
 */
public class CaptureViewModel extends ViewModel {
    private Geolocation currentLocation;
    private final PlayerAccount currentPlayer;

    /**
     * creates the view model
     * @param player the player account of the player taking the capture
     */
    public CaptureViewModel(PlayerAccount player) {
        this.currentLocation = null;
        this.currentPlayer = player;
    }

    /**
     * most up to date location known; may be null
     * @return the current location
     */
    public Geolocation getCurrentLocation() {
        return currentLocation;
    }

    /**
     * update current location to the input
     * @param currentLocation input location
     */
    public void setCurrentLocation(Geolocation currentLocation) {
        this.currentLocation = currentLocation;
    }

    /**
     * obtain the current player
     * @return the account of the current player
     */
    public PlayerAccount getCurrentPlayer() {
        return currentPlayer;
    }
}
