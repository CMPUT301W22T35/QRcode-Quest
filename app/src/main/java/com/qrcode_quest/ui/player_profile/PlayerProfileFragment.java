package com.qrcode_quest.ui.player_profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.qrcode_quest.databinding.FragmentPlayerProfileBinding;

public class PlayerProfileFragment extends Fragment {

    private FragmentPlayerProfileBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        PlayerProfileViewModel playerProfileViewModel =
                new ViewModelProvider(this).get(PlayerProfileViewModel.class);

        binding = FragmentPlayerProfileBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textPlayerProfile;
        playerProfileViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);

        TextView emailView = binding.textEmail;
        emailView.setText("qrhunter@gmail.com"); // to be replaced

        TextView usernameView = binding.textUsername;
        usernameView.setText("qrhunter"); // to be replaced

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}