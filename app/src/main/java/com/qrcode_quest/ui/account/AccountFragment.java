package com.qrcode_quest.ui.account;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.qrcode_quest.CaptureActivity;
import com.qrcode_quest.MainViewModel;
import com.qrcode_quest.databinding.FragmentAccountBinding;
import com.qrcode_quest.entities.PlayerAccount;
import com.qrcode_quest.entities.RawQRCode;
import com.qrcode_quest.ui.playerQR.PlayerQRListFragmentArgs;

public class AccountFragment extends Fragment {

    private MainViewModel mainViewModel;
    private FragmentAccountBinding binding;
    private PlayerAccount player;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        // Load the player argument
        player = PlayerQRListFragmentArgs.fromBundle(getArguments()).getPlayer();

        binding.playerUsername.setText(player.getUsername());

        if (player.getEmail() != null) {
            binding.playerEmail.setText(player.getEmail());
        }
        else{
            binding.playerEmail.setText("Not Provided");
        }

        binding.qrgenerateProfileButton.setOnClickListener(v->{
            generateProfileCode();
        });

        binding.qrgenerateLoginButton.setOnClickListener(v->{
            generateLoginCode();
        });

        AccountViewModel accountViewModel =
                new ViewModelProvider(this).get(AccountViewModel.class);

        binding = FragmentAccountBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public RawQRCode generateProfileCode(){
        return new RawQRCode(player.getUsername() + "Profile");
    }

    public RawQRCode generateLoginCode(){
        return new RawQRCode(player.getUsername() + "Login");
    }
}