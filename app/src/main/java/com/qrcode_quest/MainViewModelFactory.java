package com.qrcode_quest;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.google.firebase.firestore.FirebaseFirestore;
import com.qrcode_quest.database.PhotoStorage;

import java.util.Objects;

public class MainViewModelFactory implements ViewModelProvider.Factory {

    Application application;
    FirebaseFirestore db;
    PhotoStorage storage;
    public MainViewModelFactory(Application application) {
        this.application = application;
        db = FirebaseFirestore.getInstance();
        storage = new PhotoStorage();
    }

    public MainViewModelFactory(Application application,
                                FirebaseFirestore db, PhotoStorage storage) {
        this.application = application;
        this.db = db;
        this.storage = storage;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> aClass) {
        if (aClass.isAssignableFrom(MainViewModel.class))
            return Objects.requireNonNull(aClass.cast(new MainViewModel(application, db, storage)));
        else
            throw new IllegalArgumentException("Unexpected ViewModelClass type request received by the factory!");
    }
}
