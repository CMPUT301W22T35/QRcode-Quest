package com.qrcode_quest.ui.leaderboard;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.qrcode_quest.entities.PlayerAccount;
import com.qrcode_quest.entities.QRCode;
import com.qrcode_quest.entities.QRShot;

import java.util.HashMap;
import java.util.List;

public class PlayerListViewModel extends ViewModel {
    private MutableLiveData<HashMap<PlayerAccount, List<QRCode>>> playerCodes;
    private MutableLiveData<PlayerAccount> players;
    private MutableLiveData<QRShot> shots;
    private MutableLiveData<QRCode> codes;
}