package com.qrcode_quest.ui.login;


import static com.qrcode_quest.Constants.*;

import static java.util.Objects.requireNonNull;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;
import com.qrcode_quest.R;
import com.qrcode_quest.application.AppContainer;
import com.qrcode_quest.application.QRCodeQuestApp;
import com.qrcode_quest.database.PlayerManager;
import com.qrcode_quest.entities.PlayerAccount;

import java.util.Objects;
import java.util.UUID;

/**
 * A view to enable the user to either log in using a QR code or register a new user.
 *
 * @author jdumouch
 * @version 1.1
 */
public class SignUpFragment extends Fragment {
    /**
     * Provides a handler for a successful registration.
     */
    public interface RegisterHandler {
        /**
         * Called on a successful registration.
         * @param deviceUID The unique device ID used to register a session
         * @param username The username of the registered player
         */
        void onRegistered(String deviceUID, String username);
    }

    /** A tag constant used for logging */
    private static final String CLASS_TAG = "SignUpFragment";

    private SignUpViewModel viewModel;
    private SharedPreferences sharedPrefs;


    public SignUpFragment(){}
    public static SignUpFragment newInstance() {
        return new SignUpFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Preload the players list for fast username testing
        AppContainer appContainer = ((QRCodeQuestApp) requireActivity().getApplication()).getContainer();
        ViewModelProvider.Factory signUpViewModelFactory = new ViewModelProvider.Factory() {
            @NonNull
            @Override
            public <T extends ViewModel> T create(@NonNull Class<T> aClass) {
                if (aClass.isAssignableFrom(SignUpViewModel.class))
                    return Objects.requireNonNull(aClass.cast(new SignUpViewModel(
                            new PlayerManager(appContainer.getDb()))));
                else
                    throw new IllegalArgumentException("Unexpected ViewModelClass type request received by the factory!");
            }
        };
        viewModel =  new ViewModelProvider(this, signUpViewModelFactory).get(SignUpViewModel.class);
        viewModel.getPlayers();

        return inflater.inflate(R.layout.fragment_sign_up, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        assert this.getActivity() != null;
        AppContainer container = ((QRCodeQuestApp) getActivity().getApplication()).getContainer();
        sharedPrefs = container.getPrivateDevicePrefs();

        // Set a click listener for scan to login button
        view.findViewById(R.id.login_signup_scan_button).setOnClickListener(v -> {
            // TODO Implement scanning login codes
            Toast.makeText(this.getContext(), "Feature not implemented.", Toast.LENGTH_SHORT)
                    .show();
        });

        // Set a click listener for register button
        view.findViewById(R.id.login_signup_register_button)
                .setOnClickListener(v->this.onRegisterClicked());

        // Add a listener to the username field to clear errors
        TextView usernameText = view.findViewById(R.id.login_signup_username);
        TextInputLayout usernameLayout = view.findViewById(R.id.login_signup_username_layout);
        usernameText.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            public void afterTextChanged(Editable editable) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                usernameLayout.setErrorEnabled(false);
            }
        });

        // Add a listener to email field to clear errors
        TextView emailText = view.findViewById(R.id.login_signup_email);
        TextInputLayout emailLayout = view.findViewById(R.id.login_signup_email_layout);
        emailText.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2){}
            public void afterTextChanged(Editable editable){}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String email = charSequence.toString();
                if (emailLayout.isErrorEnabled()){
                    if (Patterns.EMAIL_ADDRESS.matcher(email).matches() || email.trim().isEmpty()) {
                        emailLayout.setErrorEnabled(false);
                    }
                }
            }
        });
    }

    /**
     * Validates user input and creates a new player/session.
     */
    void onRegisterClicked() {
        AppContainer container = ((QRCodeQuestApp) requireActivity().getApplication()).getContainer();
        PlayerManager playerManager = new PlayerManager(container.getDb());

        // Ensure that if the user entered an email it is valid
        TextView emailText = requireView().findViewById(R.id.login_signup_email);
        TextInputLayout emailLayout = requireView().findViewById(R.id.login_signup_email_layout);
        String chosenEmail = requireNonNull(emailText.getText()).toString().trim();
        if (!chosenEmail.isEmpty() && !Patterns.EMAIL_ADDRESS.matcher(chosenEmail).matches()){
            emailLayout.setError("Invalid email address");
        }
        else {
            emailLayout.setErrorEnabled(false);
        }

        // Check the user actually entered a username
        TextView usernameText = requireView().findViewById(R.id.login_signup_email);
        TextInputLayout usernameLayout = requireView().findViewById(R.id.login_signup_email_layout);
        String chosenUsername = requireNonNull(usernameText.getText()).toString();
        if (chosenUsername.trim().isEmpty()) {
            usernameLayout.setError("Username is required");
        }
        else if (!chosenUsername.trim().equals(chosenUsername)){
            usernameLayout.setError("No leading/trailing whitespace");
        }

        // Prevent a database request while a username error exists
        if (usernameLayout.isErrorEnabled()) { return; }

        // Prevent the user from spamming calls
        View registerButton = requireView().findViewById(R.id.login_signup_register_button);
        setLoading(true);

        viewModel.getPlayers().observe(getViewLifecycleOwner(), players-> {
            // Unlock the button again
            registerButton.setEnabled(true);
            setLoading(true);

            // Scan players to see if username is taken
            boolean isTaken = false;
            for (PlayerAccount p : players){
                if (p.getUsername().equals(chosenUsername)){
                    isTaken = true;
                    break;
                }
            }

            // Allow user creation
            if (!isTaken){
                // Prevent registration while there are errors
                if (usernameLayout.isErrorEnabled() ||
                    emailLayout.isErrorEnabled()) { return; }

                // Username is free, we can add the user
                PlayerAccount newPlayer = new PlayerAccount(chosenUsername, chosenEmail, "");
                playerManager.addPlayer(newPlayer, addResult -> {
                    if (!addResult.isSuccess()){
                        Log.e(CLASS_TAG, "Failed to add user.");
                        Toast.makeText(this.getActivity(), "Database call failed.", Toast.LENGTH_SHORT).show();
                        setLoading(false);
                        return;
                    }
                    Log.i(CLASS_TAG, "New user registered: " + chosenUsername);

                    // Store user credentials on device
                    String deviceUID = generateDevicePlayerPair(chosenUsername);

                    // Create a device session for them
                    playerManager.createPlayerSession(deviceUID, chosenUsername, sessionResult ->{
                        if (!sessionResult.isSuccess()){
                            Log.e(CLASS_TAG, "Failed to add session.");
                            Toast.makeText(this.getActivity(), "Database call failed.", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // Call the parent to authenticate new user
                        try {
                            RegisterHandler listener = (RegisterHandler) getActivity();
                            assert listener != null;
                            listener.onRegistered(deviceUID, chosenUsername);
                        }
                        catch (ClassCastException castException) {
                            Log.e(CLASS_TAG, "Host activity does not implement RegisterHandler.");
                        }
                    });
                });
            }
            // Handle username taken
            else {
                setLoading(false);
                usernameLayout.setError("Username taken");
            }
        });
    }

    /**
     * Generates a new UID and stores to user prefs
     * @param username The username to generate the pair with
     * @return The UID generated.
     */
    private String generateDevicePlayerPair(String username){
        String deviceUID = UUID.randomUUID().toString();
        SharedPreferences.Editor prefEditor = sharedPrefs.edit();
        prefEditor.putString(DEVICE_UID_PREF, deviceUID);
        prefEditor.putString(AUTHED_USERNAME_PREF, username);
        prefEditor.apply();

        return deviceUID;
    }

    private void setLoading(boolean state){
        View mainContainer = requireView().findViewById(R.id.login_signup_main_container);
        View progress = requireView().findViewById(R.id.login_signup_progress);
        if (state){
            mainContainer.setVisibility(View.GONE);
            progress.setVisibility(View.VISIBLE);
        }
        else{
            mainContainer.setVisibility(View.VISIBLE);
            progress.setVisibility(View.GONE);
        }
    }
}