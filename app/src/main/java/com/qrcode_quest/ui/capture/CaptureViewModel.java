package com.qrcode_quest.ui.capture;

import androidx.lifecycle.ViewModel;

import com.qrcode_quest.database.ManagerResult;
import com.qrcode_quest.database.QRManager;
import com.qrcode_quest.database.Result;
import com.qrcode_quest.entities.Geolocation;
import com.qrcode_quest.entities.PlayerAccount;
import com.qrcode_quest.entities.QRShot;

public class CaptureViewModel extends ViewModel {
    private Geolocation currentLocation;
    private final PlayerAccount currentPlayer;

    public CaptureViewModel(PlayerAccount player) {
        this.currentLocation = null;
        this.currentPlayer = player;
    }

    public Geolocation getCurrentLocation() {
        return currentLocation;
    }

    public void setCurrentLocation(Geolocation currentLocation) {
        this.currentLocation = currentLocation;
    }

    public PlayerAccount getCurrentPlayer() {
        return currentPlayer;
    }
}
