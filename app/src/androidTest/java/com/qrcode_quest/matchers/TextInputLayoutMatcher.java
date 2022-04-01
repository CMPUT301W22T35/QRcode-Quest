package com.qrcode_quest.matchers;

import android.view.View;

import com.google.android.material.textfield.TextInputLayout;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;


/**
 * A class of {@link TextInputLayout} {@link Matcher} used in espresso tests.
 *
 * @author jdumouch
 * @version 1.0
 */
public class TextInputLayoutMatcher {
    // Custom matcher found:
    // https://stackoverflow.com/questions/38842034/how-to-test-textinputlayout-values-hint-error-etc-using-android-espresso
    // Author: piotrek1543
    // Date Posted: Aug 10, 2016
    // Date Accessed: March 31, 2020

    /**
     * Matches a {@link TextInputLayout} with the specified error text.
     * @param text The error text to match on
     */
    public static Matcher<View> hasErrorText(String text){
        return new TypeSafeMatcher<View>() {
            @Override
            protected boolean matchesSafely(View item) {
                if (!(item instanceof TextInputLayout)){
                    return false;
                }
                CharSequence error = ((TextInputLayout) item).getError();
                if (error == null) { return false; }

                return text.equals(error.toString());
            }
            @Override
            public void describeTo(Description description) {}
        };
    }

    /**
     * Matches a {@link TextInputLayout} that has no error text.
     */
    public static Matcher<View> hasNoError(){
        return new TypeSafeMatcher<View>() {
            @Override
            protected boolean matchesSafely(View item) {
                if (!(item instanceof TextInputLayout)){
                    return false;
                }
                CharSequence error = ((TextInputLayout) item).getError();
                return (error == null || error.toString().isEmpty());
            }

            @Override
            public void describeTo(Description description) {}
        };
    }
}
