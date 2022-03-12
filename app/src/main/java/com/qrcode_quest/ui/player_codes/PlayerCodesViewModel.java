package com.qrcode_quest.ui.player_codes;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class PlayerCodesViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public PlayerCodesViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is player codes fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}
