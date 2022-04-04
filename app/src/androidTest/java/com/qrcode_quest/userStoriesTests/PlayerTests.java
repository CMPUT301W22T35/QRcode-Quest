package com.qrcode_quest.userStoriesTests;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.firebase.firestore.FirebaseFirestore;
import com.qrcode_quest.EspressoHelper;
import com.qrcode_quest.MainActivity;
import com.qrcode_quest.MockDb;
import com.qrcode_quest.MockInstances;
import com.qrcode_quest.R;
import com.qrcode_quest.application.AppContainer;
import com.qrcode_quest.application.QRCodeQuestApp;
import com.qrcode_quest.entities.PlayerAccount;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;

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
        String deviceID = "";

        AppContainer container = app.getContainer();
        container.setDb(MockInstances.createSinglePlayerDb(testPlayer, deviceID));
        container.setStorage(MockInstances.createEmptyPhotoStorage());
        container.setPrivateDevicePrefs(MockInstances.createEmptySharedPreferences());

        FirebaseFirestore db = MockDb.createMockDatabase(new HashMap<>());
        container.setDb(db);


        rule = new ActivityScenarioRule<>(MainActivity.class);
        return rule;
    }

/**
    //US 01.01.01
    //As a player, I want to add new QR codes to my account.
    @Test
    public void addQRTest() {

        ActivityScenario scenario = rule.getScenario();





    }

    //US 01.0X.01
    //As a player, I want to see my highest and lowest scoring QR codes.
    @Test
    public void maxMinScoringTest(){

        ActivityScenario scenario = rule.getScenario();

        onView(withId(R.id.navigation_leaderboard)).perform(click());
        onView(isRoot()).perform(EspressoHelper.waitFor(1000));



    }

*/


}
