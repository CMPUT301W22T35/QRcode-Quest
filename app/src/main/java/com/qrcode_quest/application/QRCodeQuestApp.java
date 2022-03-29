package com.qrcode_quest.application;

import android.app.Application;

/**
 * inherits Application class but adds a container to carry out dependency injection
 */
public class QRCodeQuestApp extends Application {

    // The purpose of this container attribute is to inject dependency to activity classes
    // contains the variables for real objects when running the app, for mock objects when running
    // the unit tests
    // refer to https://developer.android.com/training/dependency-injection/manual
    private AppContainer container;

    /** initialize the Application instance and the AppContainer */
    public QRCodeQuestApp() {
        resetContainer();
    }

    /**
     * get AppContainer that can provided dependency to other parts of the program
     * @return an AppContainer for dependencies
     */
    public AppContainer getContainer() {
        return container;
    }

    /** used in tests to reset the container; we can do this because Espresso tests run sequentially */
    public void resetContainer() { container = new AppContainer(this); }
}
