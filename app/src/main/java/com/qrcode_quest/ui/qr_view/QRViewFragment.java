package com.qrcode_quest.ui.qr_view;

import static java.util.Objects.requireNonNull;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.qrcode_quest.R;
import com.qrcode_quest.databinding.FragmentPlayerQrShotsBinding;
import com.qrcode_quest.databinding.FragmentQrViewBinding;
import com.qrcode_quest.entities.QRShot;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link QRViewFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class QRViewFragment extends Fragment {
    private FragmentQrViewBinding binding;

    private static final String SHOT_HASH_PARAM = "shot_hash";
    private static final String SHOT_OWNER_PARAM = "shot_owner";

    private String shotOwner;
    private String shotHash;

    public QRViewFragment() {}

    public static QRViewFragment newInstance(String shotHash, String shotOwner) {
        QRViewFragment fragment = new QRViewFragment();
        Bundle args = new Bundle();
        args.putString(SHOT_HASH_PARAM, shotHash);
        args.putString(SHOT_OWNER_PARAM, shotOwner);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            shotOwner = getArguments().getString(SHOT_OWNER_PARAM);
            shotHash = getArguments().getString(SHOT_HASH_PARAM);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        QRViewModel viewModel = new ViewModelProvider(this).get(QRViewModel.class);

        binding = FragmentQrViewBinding.inflate(inflater, container, false);
        binding.qrviewMainLayout.setVisibility(View.INVISIBLE);
        binding.qrviewLoadingLayout.setVisibility(View.VISIBLE);

        // Fetch and load the QRShot into the fragment
        viewModel.getQRShot(shotOwner, shotHash).observe(getViewLifecycleOwner(), qrShot ->{
            loadQRShot(qrShot);
            binding.qrviewMainLayout.setVisibility(View.VISIBLE);
            binding.qrviewLoadingLayout.setVisibility(View.INVISIBLE);
        });

        // Grab the action bar
        AppCompatActivity main = (AppCompatActivity) this.getActivity();
        ActionBar actionBar = requireNonNull((requireNonNull(main)).getSupportActionBar());
        // Hide the broken garbage stupid back arrow
        actionBar.setDisplayHomeAsUpEnabled(false);

        return binding.getRoot();

    }

    private void loadQRShot(QRShot shot){

    }
}