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
import android.widget.Toast;

import com.qrcode_quest.application.AppContainer;
import com.qrcode_quest.application.QRCodeQuestApp;
import com.qrcode_quest.database.PlayerManager;
import com.qrcode_quest.databinding.FragmentSignUpBinding;
import com.qrcode_quest.entities.PlayerAccount;

import java.util.Objects;
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

    private SignUpViewModel viewModel;
    private FragmentSignUpBinding binding;
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

        binding = FragmentSignUpBinding.inflate(inflater, container, false);

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

        // Inflate the layout for this fragment
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        assert this.getActivity() != null;
        AppContainer container = ((QRCodeQuestApp) getActivity().getApplication()).getContainer();
        sharedPrefs = container.getPrivateDevicePrefs();


        // Set a click listener for scan to login button
        binding.loginSignupScanButton.setOnClickListener(v -> {
            // TODO Implement scanning login codes
            Toast.makeText(this.getContext(), "Feature not implemented.", Toast.LENGTH_SHORT)
                    .show();
        });

        // Set a click listener for register button
        binding.loginSignupRegisterButton.setOnClickListener(v->this.onRegisterClicked());

        // Add a listener to the username field to clear errors
        binding.loginSignupUsername.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            public void afterTextChanged(Editable editable) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                binding.loginSignupUsernameLayout.setErrorEnabled(false);
            }
        });

        // Add a listener to email field to clear errors
        binding.loginSignupEmail.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2){}
            public void afterTextChanged(Editable editable){}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String email = charSequence.toString();
                if (binding.loginSignupEmailLayout.isErrorEnabled()){
                    if (Patterns.EMAIL_ADDRESS.matcher(email).matches() || email.trim().isEmpty()) {
                        binding.loginSignupEmailLayout.setErrorEnabled(false);
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
        String chosenEmail = requireNonNull(binding.loginSignupEmail.getText()).toString().trim();
        if (!chosenEmail.isEmpty() && !Patterns.EMAIL_ADDRESS.matcher(chosenEmail).matches()){
            binding.loginSignupEmailLayout.setError("Invalid email address");
        }
        else {
            binding.loginSignupEmailLayout.setErrorEnabled(false);
        }

        // Check the user actually entered a username
        String chosenUsername = requireNonNull(binding.loginSignupUsername.getText()).toString();
        if (chosenUsername.trim().isEmpty()) {
            binding.loginSignupUsernameLayout.setError("Username is required");
        }
        else if (chosenUsername.contains(" ")){
            binding.loginSignupUsernameLayout.setError("No whitespace allowed");
        }

        // Prevent a database request while a username error exists
        if (binding.loginSignupUsernameLayout.isErrorEnabled()) { return; }

        // Prevent the user from spamming calls
        binding.loginSignupRegisterButton.setEnabled(false);
        binding.loginSignupMainContainer.setVisibility(View.GONE);
        binding.loginSignupProgress.setVisibility(View.VISIBLE);

        viewModel.getPlayers().observe(getViewLifecycleOwner(), players-> {
            // Unlock the button again
            binding.loginSignupRegisterButton.setEnabled(true);
            showLoading(true);

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
                if (binding.loginSignupUsernameLayout.isErrorEnabled() ||
                    binding.loginSignupEmailLayout.isErrorEnabled()) { return; }

                // Username is free, we can add the user
                PlayerAccount newPlayer = new PlayerAccount(chosenUsername, chosenEmail, "");
                playerManager.addPlayer(newPlayer, addResult -> {
                    if (!addResult.isSuccess()){
                        Log.e(CLASS_TAG, "Failed to add user.");
                        Toast.makeText(this.getActivity(), addResult.getError().getMessage(), Toast.LENGTH_SHORT).show();
                        showLoading(false);
                        return;
                    }
                    Log.i(CLASS_TAG, "New user registered: " + chosenUsername);

                    // Store user credentials on device
                    String deviceUID = generateDevicePlayerPair(chosenUsername);

                    // Create a device session for them
                    playerManager.createPlayerSession(deviceUID, chosenUsername, sessionResult ->{
                        if (!sessionResult.isSuccess()){
                            Log.e(CLASS_TAG, "Failed to add session.");
                            Toast.makeText(this.getActivity(), sessionResult.getError().getMessage(), Toast.LENGTH_SHORT).show();
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
                showLoading(false);
                binding.loginSignupUsernameLayout.setError("Username taken");
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

    private void showLoading(boolean state){
        if (state){
            binding.loginSignupMainContainer.setVisibility(View.GONE);
            binding.loginSignupProgress.setVisibility(View.VISIBLE);
        }
        else{
            binding.loginSignupMainContainer.setVisibility(View.VISIBLE);
            binding.loginSignupProgress.setVisibility(View.GONE);
        }
    }
}