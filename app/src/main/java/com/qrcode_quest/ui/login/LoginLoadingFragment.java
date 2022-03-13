package com.qrcode_quest.ui.login;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.qrcode_quest.R;

/**
 * A view to show the user that a login is in progress
 * @author jdumouch
 * @version 1.0
 */
public class LoginLoadingFragment extends Fragment {
    public LoginLoadingFragment() {}

    /**
     * @return A new instance of fragment LoginLoadingFragment.
     */
    public static LoginLoadingFragment newInstance() {
        LoginLoadingFragment fragment = new LoginLoadingFragment();
        fragment.setArguments(new Bundle());
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_login_loading, container, false);
    }
}