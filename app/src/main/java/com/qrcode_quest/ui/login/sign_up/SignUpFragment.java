package com.qrcode_quest.ui.login.sign_up;


import static android.content.Context.MODE_PRIVATE;

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
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;
import com.qrcode_quest.R;
import com.qrcode_quest.database.PlayerManager;
import com.qrcode_quest.entities.PlayerAccount;

import java.util.UUID;

public class SignUpFragment extends Fragment {
    public interface RegisterHandler {
        void onRegistered(String deviceUID, String username);
    }

    /** A tag constant used for logging */
    private static final String CLASS_TAG = "SignUpFragment";

    // TODO move this to a global location
    private static final String PREF_PATH = "qrcode_quest";
    private static final String AUTHED_USERNAME_PREF = "authed_username";
    private static final String DEVICE_UID_PREF = "device_UID";

    private EditText usernameField;
    private TextInputLayout usernameLayout;
    private EditText emailField;
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
                .getSharedPreferences(PREF_PATH, MODE_PRIVATE);

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
     * Validates the input and submits an addPlayer request.
     */
    void onRegisterClicked() {
        // Ensure that if the user entered an email it is valid
        String chosenEmail = emailField.getText().toString();
        if (!chosenEmail.isEmpty() && !Patterns.EMAIL_ADDRESS.matcher(chosenEmail).matches()){
            emailLayout.setError("Invalid email address");
        }
        else {
            emailLayout.setErrorEnabled(false);
        }

        // Check the user actually entered a username
        String chosenUsername = usernameField.getText().toString();
        if (chosenUsername.trim().isEmpty()) {
            usernameLayout.setError("Username is required");
            return;
        }

        // Prevent the user from spamming calls
        registerButton.setEnabled(false);

        playerManager.checkUserExists(chosenUsername, existsResult -> {
            // Unlock the button again
            registerButton.setEnabled(true);

            // Check for database call errors
            if (!existsResult.isSuccess()){
                Log.e(CLASS_TAG, "Failed to check username existence");
                Toast.makeText(this.getActivity(), "Database call failed.", Toast.LENGTH_SHORT).show();
            }

            Boolean isTaken = existsResult.unwrap();
            assert isTaken != null;

            if (!isTaken){
                // Username is free, we can add the user
                PlayerAccount newPlayer = new PlayerAccount(chosenUsername, chosenEmail, "");
                playerManager.addPlayer(newPlayer, addResult -> {
                    if (!addResult.isSuccess()){
                        Log.e(CLASS_TAG, "Failed to add user.");
                        Toast.makeText(this.getActivity(), "Database call failed.", Toast.LENGTH_SHORT).show();
                    }
                    Log.i(CLASS_TAG, "New user registered: " + chosenUsername);

                    // Store user credentials on device
                    String deviceUID = generateDevicePlayerPair(chosenUsername);

                    // Create a device session for them
                    playerManager.createPlayerSession(deviceUID, chosenUsername, sessionResult ->{
                        if (!sessionResult.isSuccess()){
                            Log.e(CLASS_TAG, "Failed to add session.");
                            Toast.makeText(this.getActivity(), "Database call failed.", Toast.LENGTH_SHORT).show();
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