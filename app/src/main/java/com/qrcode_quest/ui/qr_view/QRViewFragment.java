package com.qrcode_quest.ui.qr_view;

import static java.util.Objects.requireNonNull;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.qrcode_quest.MainViewModel;
import com.qrcode_quest.R;
import com.qrcode_quest.database.QRManager;
import com.qrcode_quest.databinding.FragmentPlayerQrShotsBinding;
import com.qrcode_quest.databinding.FragmentQrViewBinding;
import com.qrcode_quest.entities.PlayerAccount;
import com.qrcode_quest.entities.QRCode;
import com.qrcode_quest.entities.QRShot;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * A fragment for displaying a QR code's data.
 *
 * @author jdumouch
 * @version 1.0
 */
public class QRViewFragment extends Fragment {
    private FragmentQrViewBinding binding;

    /** A tag used for logging */
    private static final String CLASS_TAG = "QRViewFragment";

    // Fragment arguments
    private static final String SHOT_HASH_PARAM = "shot_hash";
    private static final String SHOT_OWNER_PARAM = "shot_owner";

    /** The name of the player who captured the QRShot (fragment parameter) */
    private String shotOwner;
    /** The hash of the QRShot captured (fragment parameter) */
    private String shotHash;

    /** Required empty constructor */
    public QRViewFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        assert getArguments() != null; // Arguments are non-nullable

        shotOwner = getArguments().getString(SHOT_OWNER_PARAM);
        shotHash = getArguments().getString(SHOT_HASH_PARAM);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Grab the action bar
        AppCompatActivity main = (AppCompatActivity) this.getActivity();
        ActionBar actionBar = requireNonNull((requireNonNull(main)).getSupportActionBar());
        // Hide the broken garbage stupid back arrow
        actionBar.setDisplayHomeAsUpEnabled(false);

        QRViewModel viewModel = new ViewModelProvider(this).get(QRViewModel.class);
        MainViewModel mainViewModel =
                new ViewModelProvider(this.getActivity()).get(MainViewModel.class);

        binding = FragmentQrViewBinding.inflate(inflater, container, false);

        // Default to the loading spinner screen
        binding.qrviewMainLayout.setVisibility(View.INVISIBLE);
        binding.qrviewLoadingLayout.setVisibility(View.VISIBLE);

        // Fetch and load the data into the fragment
        mainViewModel.getCurrentPlayer().observe(getViewLifecycleOwner(), player -> {
            if (player == null) { return; }

            viewModel.getCodes().observe(getViewLifecycleOwner(), codes -> {
                if (codes == null) {
                    return;
                }
                assert codes.containsKey(shotHash);
                viewModel.getShots().observe(getViewLifecycleOwner(), shots -> {
                    if (shots == null) { return; }

                    viewModel.getQRShot(shotOwner, shotHash).observe(getViewLifecycleOwner(), qrShot -> {
                        // Welcome to the 7th circle of callback hell.
                        if (qrShot == null) {
                            return;
                        }

                        // Load the data into the fragment
                        loadQRShot(qrShot, codes, shots, player);

                        // Make the fragment visible
                        binding.qrviewMainLayout.setVisibility(View.VISIBLE);
                        binding.qrviewLoadingLayout.setVisibility(View.INVISIBLE);
                    });
                });
            });
        });

        binding.qrviewEditButton.setOnClickListener(view ->{
            // TODO implement Edit
            Toast.makeText(this.getActivity(), "Edit not implemented", Toast.LENGTH_SHORT).show();
        });
        binding.qrviewOtherScansButton.setOnClickListener(view ->{
            // TODO implement View Players
            Toast.makeText(this.getActivity(), "View players not implemented", Toast.LENGTH_SHORT).show();
        });
        binding.qrviewCommentsButton.setOnClickListener(view ->{
            // TODO implement View comments
            Toast.makeText(this.getActivity(), "View comments not implemented", Toast.LENGTH_SHORT).show();
        });

        return binding.getRoot();

    }

    /**
     * Loads QRCode/QRShot data into the view
     * @param shot The QRShot to load
     * @param codes A HashMap of all the QRCodes
     * @param shots A list of all the QRShots
     * @param authedPlayer The player viewing the QRCode
     */
    @SuppressLint("DefaultLocale")
    private void loadQRShot(QRShot shot, HashMap<String, QRCode> codes,
                            ArrayList<QRShot> shots, PlayerAccount authedPlayer){
        AppCompatActivity main = requireNonNull((AppCompatActivity) this.getActivity());

        if (shot == null || !codes.containsKey(shot.getCodeHash())) {
            Log.e(CLASS_TAG, "Failed to populate as QRCode was null.");
            return;
        }

        QRCode thisCode = requireNonNull(codes.get(shot.getCodeHash()));

        // TODO use QR code name instead of hash
        binding.qrviewName.setText(shot.getCodeHash());
        binding.qrviewScore.setText(String.format("%d", thisCode.getScore()));

        int timesCaptured = 0;
        for (QRShot testShot : shots){
            if (testShot.getCodeHash().equals(shot.getCodeHash())){
                timesCaptured++;
            }
        }
        binding.qrviewOtherScans.setText(String.format("%d", timesCaptured));

        // Try to load the image
        if (shot.getPhoto() != null){
            //binding.qrviewPhoto.setImageBitmap(shot.getPhoto());
            binding.qrviewPhoto.setVisibility(View.VISIBLE);
        }

        // Try to load the geolocation
        if (shot.getLocation() != null ){
            //binding.qrviewGeoloc.setText(shot.getLocation().toString());
            binding.qrviewGeolocContainer.setVisibility(View.VISIBLE);
        }

        // Show the owner buttons if privileged
        if (authedPlayer.getUsername().equals(shot.getOwnerName()) || authedPlayer.isOwner()){
            binding.qrviewOwnerButtonContainer.setVisibility(View.VISIBLE);
            binding.qrviewDeleteButton.setOnClickListener(view -> {
                binding.qrviewMainLayout.setVisibility(View.INVISIBLE);
                binding.qrviewLoadingLayout.setVisibility(View.VISIBLE);
                this.deleteQR();
            });
        }
    }

    /**
     * Deletes the loaded QR code from the database and return to last screen.
     * This will occur even if the delete failed.
     */
    private void deleteQR(){
        new QRManager().removeQRCode(shotHash, result ->{
            if (!result.isSuccess()){
                Log.e(CLASS_TAG, "Failed to delete QRShot.");
                Toast.makeText(this.getActivity(), "Failed to delete.", Toast.LENGTH_LONG)
                        .show();
            }

            NavController navController = NavHostFragment.findNavController(this);
            navController.popBackStack();
        });
    }
}