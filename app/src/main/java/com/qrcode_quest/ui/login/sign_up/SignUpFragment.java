package com.qrcode_quest.ui.login.sign_up;


import static android.content.Context.MODE_PRIVATE;
import static com.qrcode_quest.Constants.*;

import static java.util.Objects.requireNonNull;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.qrcode_quest.R;
import com.qrcode_quest.database.PlayerManager;
import com.qrcode_quest.entities.PlayerAccount;

import java.util.UUID;

/**
 * A view to enable the user to either log in using a QR code or register a new user.
 *
 * @author jdumouch
 * @version 1.0
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

    private TextInputEditText usernameField;
    private TextInputLayout usernameLayout;
    private TextInputEditText emailField;
    private TextInputLayout emailLayout;
    private Button scanLoginButton;
    private Button registerButton;

    private PlayerManager playerManager;

    private SharedPreferences sharedPrefs;

    public SignUpFragment(){}
    public static SignUpFragment newInstance() {
        return new SignUpFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        playerManager = new PlayerManager();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_sign_up, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        assert this.getActivity() != null;
        sharedPrefs = this.getActivity().getApplicationContext()
                .getSharedPreferences(SHARED_PREF_PATH, MODE_PRIVATE);

        // Grab the view objects from the newly created fragment view
        usernameField = view.findViewById(R.id.login_signup_username);
        usernameLayout = view.findViewById(R.id.login_signup_username_layout);
        emailField = view.findViewById(R.id.login_signup_email);
        emailLayout = view.findViewById(R.id.login_signup_email_layout);
        scanLoginButton = view.findViewById(R.id.login_signup_scan_button);
        registerButton = view.findViewById(R.id.login_signup_register_button);

        // Set a click listener for scan to login button
        scanLoginButton.setOnClickListener(v -> {
            // TODO Implement scanning login codes
            Toast.makeText(this.getContext(), "Feature not implemented.", Toast.LENGTH_SHORT)
                    .show();
        });

        // Set a click listener for register button
        registerButton.setOnClickListener(v->this.onRegisterClicked());

        // Add a listener to the username field to clear errors
        usernameField.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            public void afterTextChanged(Editable editable) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                usernameLayout.setErrorEnabled(false);
            }
        });

        // Add a listener to email field to clear errors
        emailField.addTextChangedListener(new TextWatcher() {
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
        // Ensure that if the user entered an email it is valid
        String chosenEmail = requireNonNull(emailField.getText()).toString().trim();
        if (!chosenEmail.isEmpty() && !Patterns.EMAIL_ADDRESS.matcher(chosenEmail).matches()){
            emailLayout.setError("Invalid email address");
        }
        else {
            emailLayout.setErrorEnabled(false);
        }

        // Check the user actually entered a username
        String chosenUsername = requireNonNull(usernameField.getText()).toString();
        if (chosenUsername.trim().isEmpty()) {
            usernameLayout.setError("Username is required");
        }
        else if (!chosenUsername.trim().equals(chosenUsername)){
            usernameLayout.setError("No leading/trailing whitespace");
        }

        // Prevent a database request while a username error exists
        if (usernameLayout.isErrorEnabled()) { return; }

        // Prevent the user from spamming calls
        registerButton.setEnabled(false);

        playerManager.checkUserExists(chosenUsername, existsResult -> {
            // Unlock the button again
            registerButton.setEnabled(true);

            // Check for database call errors
            if (!existsResult.isSuccess()){
                Log.e(CLASS_TAG, "Failed to check username existence");
                Toast.makeText(this.getActivity(), "Database call failed.", Toast.LENGTH_SHORT).show();
                return;
            }

            Boolean isTaken = existsResult.unwrap();
            assert isTaken != null;

            if (!isTaken){
                // Prevent registration while there are errors
                if (usernameLayout.isErrorEnabled() || emailLayout.isErrorEnabled()){ return; }

                // Username is free, we can add the user
                PlayerAccount newPlayer = new PlayerAccount(chosenUsername, chosenEmail, "");
                playerManager.addPlayer(newPlayer, addResult -> {
                    if (!addResult.isSuccess()){
                        Log.e(CLASS_TAG, "Failed to add user.");
                        Toast.makeText(this.getActivity(), "Database call failed.", Toast.LENGTH_SHORT).show();
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
            else {
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
}