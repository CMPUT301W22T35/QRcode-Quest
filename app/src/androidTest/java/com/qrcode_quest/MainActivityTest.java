package com.qrcode_quest;

import android.app.Activity;
import android.util.Log;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.Espresso;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.qrcode_quest.application.QRCodeQuestApp;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class MainActivityTest {

    public ActivityScenarioRule<MainActivity> rule;

    @Rule
    public ActivityScenarioRule<MainActivity> setupRule() {
        rule = new ActivityScenarioRule<>(MainActivity.class);
        return rule;
    }

    @Before
    public void setupBeforeTests() {
        // get the application object so we can provide mock db and other dependencies to it
        QRCodeQuestApp app = ApplicationProvider.getApplicationContext();
    }

    @Test
    public void testMainActivity() {
        // stackoverflow by gosr
        // url:https://stackoverflow.com/questions/61953249/how-to-access-activity-from-activityscenariorule
        ActivityScenario<MainActivity> scenario = rule.getScenario();
        Log.d("START TEST", scenario.getState().name());
        scenario.onActivity(new ActivityScenario.ActivityAction<MainActivity>() {
            @Override
            public void perform(MainActivity activity) {
                //Espresso.onView(R.id.);
            }
        });
    }
}
