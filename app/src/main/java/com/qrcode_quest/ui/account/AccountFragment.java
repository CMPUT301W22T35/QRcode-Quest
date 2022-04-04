package com.qrcode_quest.ui.account;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.qrcode_quest.MainViewModel;
import com.qrcode_quest.databinding.FragmentAccountBinding;
import com.qrcode_quest.entities.PlayerAccount;
import com.qrcode_quest.entities.QRStringConverter;

public class AccountFragment extends Fragment {
    private PlayerAccount account;
    private FragmentAccountBinding binding;
    private AccountViewModel accountViewModel;
    private MainViewModel mainViewModel;

    static private final int QR_WIDTH = 300;
    static private final int QR_HEIGHT = 300;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        mainViewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
        accountViewModel = new ViewModelProvider(this).get(AccountViewModel.class);

        accountViewModel =
                new ViewModelProvider(this).get(AccountViewModel.class);
        binding = FragmentAccountBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // after account is loaded, clicking on profile/login qr generates qr codes for them
        binding.qrgenerateLoginButton.setOnClickListener(view1 -> {
            if (account != null) {
                accountViewModel.createQRImage(QRStringConverter.getLoginQRString(
                        account.getUsername()), QR_WIDTH, QR_HEIGHT);
            }
        });
        binding.qrgenerateProfileButton.setOnClickListener(view1 -> {
            if (account != null) {
                accountViewModel.createQRImage(QRStringConverter.getProfileQRString(
                        account.getUsername()), QR_WIDTH, QR_HEIGHT);
            }
        });

        // fetch the current player account
        mainViewModel.getCurrentPlayer().observe(getViewLifecycleOwner(), playerAccount -> {
            Log.e(AccountFragment.class.getSimpleName(), "onChanged");
            if (playerAccount != null) {
                account = playerAccount;
                binding.playerUsername.setText(playerAccount.getUsername());
                binding.playerEmail.setText(playerAccount.getEmail());
                accountViewModel.hideQRImage(QR_WIDTH, QR_HEIGHT);
            }
        });

        accountViewModel.getBitmapLivedata().observe(getViewLifecycleOwner(),
                bitmap -> binding.ivCode.setImageBitmap(bitmap)
        );
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}