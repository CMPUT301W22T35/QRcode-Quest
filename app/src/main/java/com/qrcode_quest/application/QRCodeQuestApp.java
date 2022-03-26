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
    private final AppContainer container;

    public QRCodeQuestApp() {
        container = new AppContainer();
    }

    public AppContainer getContainer() {
        return container;
    }
}
