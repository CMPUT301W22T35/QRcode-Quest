package com.qrcode_quest.ui.player_profile;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class PlayerProfileViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public PlayerProfileViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is player profile fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}