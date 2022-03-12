package com.qrcode_quest.ui.playerQR;

import androidx.lifecycle.ViewModelProvider;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.qrcode_quest.MainViewModel;
import com.qrcode_quest.databinding.FragmentPlayerQrListBinding;


/**
 * A view for displaying the QR codes a player has captured.
 */
public class PlayerQRListFragment extends Fragment {

    private FragmentPlayerQrListBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        PlayerQRListViewModel viewModel =
                new ViewModelProvider(this).get(PlayerQRListViewModel.class);

        MainViewModel mainViewModel =
                new ViewModelProvider(this.requireActivity()).get(MainViewModel.class);

        binding = FragmentPlayerQrListBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}