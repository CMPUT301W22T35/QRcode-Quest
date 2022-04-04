package com.qrcode_quest.ui.account;

import static com.qrcode_quest.ui.leaderboard.PlayerListFragmentDirections.actionLeaderboardToPlayerqrs;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import com.qrcode_quest.R;
import com.qrcode_quest.ui.capture.CaptureFragment;
import com.qrcode_quest.MainViewModel;
import com.qrcode_quest.application.AppContainer;
import com.qrcode_quest.application.QRCodeQuestApp;
import com.qrcode_quest.database.QRManager;
import com.qrcode_quest.databinding.FragmentAccountBinding;
import com.qrcode_quest.entities.GPSLocationLiveData;
import com.qrcode_quest.entities.Geolocation;
import com.qrcode_quest.entities.PlayerAccount;
import com.qrcode_quest.entities.QRCode;
import com.qrcode_quest.entities.QRShot;
import com.qrcode_quest.entities.QRStringConverter;
import com.qrcode_quest.entities.RawQRCode;
import com.qrcode_quest.ui.leaderboard.PlayerListFragmentDirections;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

public class AccountFragment extends Fragment {
    private PlayerAccount account;
    private FragmentAccountBinding binding;
    private AccountViewModel accountViewModel;
    private MainViewModel mainViewModel;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        mainViewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
        accountViewModel = new ViewModelProvider(this).get(AccountViewModel.class);

        binding = FragmentAccountBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mainViewModel.getCurrentPlayer().observe(getViewLifecycleOwner(), playerAccount -> {
            Log.d(AccountFragment.class.getSimpleName(),"onChanged");
            if (playerAccount != null) {
                account = playerAccount;
                accountViewModel.createQRImage(
                        QRStringConverter.getLoginQRString(account.getUsername()),300,300);
            }
        });

        accountViewModel.getBitmapLivedata().observe(getViewLifecycleOwner(),
                bitmap -> binding.ivCode.setImageBitmap(bitmap));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}