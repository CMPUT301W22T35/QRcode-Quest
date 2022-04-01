package com.qrcode_quest.userStoriesTests;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.qrcode_quest.MainActivity;
import com.qrcode_quest.MockInstances;
import com.qrcode_quest.application.AppContainer;
import com.qrcode_quest.application.QRCodeQuestApp;
import com.qrcode_quest.entities.PlayerAccount;

import org.junit.Rule;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class PlayerTests {

    public ActivityScenarioRule<MainActivity> rule;

    @Rule
    public ActivityScenarioRule<MainActivity> setupRule() {
            // get the application object so we can provide mock db and other dependencies to it
        QRCodeQuestApp app = ApplicationProvider.getApplicationContext();
        app.resetContainer();
        PlayerAccount testPlayer = new PlayerAccount("testPlayerName", "testplayer@gmail.com",
                "123-456-7890", false, true);
        String deviceID = "";  // authentication is not important for MainActivity test, so leave blank

        AppContainer container = app.getContainer();
        container.setDb(MockInstances.createSingerPlayerDb(testPlayer, deviceID));
        container.setStorage(MockInstances.createEmptyPhotoStorage());
        container.setPrivateDevicePrefs(MockInstances.createEmptySharedPreferences());

        rule = new ActivityScenarioRule<>(MainActivity.class);
        return rule;
    }

    //US 01.01.01
    //As a player, I want to add new QR codes to my account.
    public void addQRTest() {


    }




}
