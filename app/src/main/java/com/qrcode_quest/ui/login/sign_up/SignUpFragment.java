package com.qrcode_quest.ui.login.sign_up;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;
import com.qrcode_quest.manager.PlayerManager;
import com.qrcode_quest.R;

public class SignUpFragment extends Fragment {

    private EditText usernameField;
    private TextInputLayout usernameLayout;
    private EditText emailField;
    private TextInputLayout emailLayout;
    private PlayerManager playerManager;

    public SignUpFragment(){}

    public static SignUpFragment newInstance() {
        return new SignUpFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        playerManager = PlayerManager.getInstance();
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

        // Grab the view objects from the newly created fragment view
        usernameField = view.findViewById(R.id.login_signup_username);
        usernameLayout = view.findViewById(R.id.login_signup_username_layout);
        emailField = view.findViewById(R.id.login_signup_email);
        emailLayout = view.findViewById(R.id.login_signup_email_layout);
        Button scanLoginButton = view.findViewById(R.id.login_signup_scan_button);
        Button registerButton = view.findViewById(R.id.login_signup_register_button);

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
            public void afterTextChanged(Editable editable) {}

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

        // Check for username uniqueness
        playerManager.checkUsernameTaken(chosenUsername,
                taken -> {
                    // Username is available
                    if (!taken) {
                        // TODO Register the user and authenticate
                        Toast.makeText(this.getContext(), "Account creation not implemented.",
                                Toast.LENGTH_SHORT).show();
                    }
                    // Username was taken
                    else {
                        usernameLayout.setError("Username taken");
                    }
                },
                // onError
                () -> {
                    Toast.makeText(this.getContext(), "Error communicating with Firebase",
                            Toast.LENGTH_SHORT).show();
                });
    }
}