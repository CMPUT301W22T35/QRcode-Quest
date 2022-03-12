package com.qrcode_quest.ui.player_codes;

import android.app.ActionBar;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.qrcode_quest.R;
import com.qrcode_quest.databinding.FragmentPlayerCodesBinding;

//import com.qrcode_quest.databinding.FragmentPlayerCodesBinding;

public class PlayerCodesFragment extends Fragment {

    private FragmentPlayerCodesBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        PlayerCodesViewModel playerCodesViewModel =
                new ViewModelProvider(this).get(PlayerCodesViewModel.class);

        binding = FragmentPlayerCodesBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textPlayerCodes;
        playerCodesViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }


}
