package com.qrcode_quest.ui.leaderboard;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

/**
 * @author ageolleg
 * @version 0.1
 */
public class LeaderboardViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public LeaderboardViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is leaderboard fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}