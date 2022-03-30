package com.qrcode_quest;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.allOf;

import android.Manifest;
import android.util.Log;
import android.view.View;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.UiController;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.GrantPermissionRule;

import com.qrcode_quest.application.AppContainer;
import com.qrcode_quest.application.QRCodeQuestApp;
import com.qrcode_quest.entities.PlayerAccount;

import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class MainActivityTest {

    @Rule
    public GrantPermissionRule grantPermissionRule = GrantPermissionRule.grant(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.INTERNET
    );

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

    @Before
    public void setupBeforeTests() {
    }

    @Test
    public void testMainActivity() {
        // stackoverflow by gosr
        // url:https://stackoverflow.com/questions/61953249/how-to-access-activity-from-activityscenariorule
        ActivityScenario<MainActivity> scenario = rule.getScenario();

        // for more about how to use Espresso
        // see doc: https://developer.android.com/training/testing/espresso/basics
        onView(withId(R.id.navigation_leaderboard)).perform(click());
        onView(isRoot()).perform(EspressoHelper.waitFor(1000));  // example of wait
        onData(allOf(is(instanceOf(String.class)), is("This is home fragment")));
        onView(withId(R.id.playerlist_content_name))
                .check(matches(withText(containsString("testPlayerName"))));
        scenario.onActivity(new ActivityScenario.ActivityAction<MainActivity>() {
            @Override
            public void perform(MainActivity activity) {
            }
        });
    }
    @Test
    public void testMainActivity2() {
        ActivityScenario<MainActivity> scenario = rule.getScenario();
        onView(withId(R.id.navigation_leaderboard)).perform(click());
        scenario.onActivity(new ActivityScenario.ActivityAction<MainActivity>() {
            @Override
            public void perform(MainActivity activity) {
            }
        });
    }
}
