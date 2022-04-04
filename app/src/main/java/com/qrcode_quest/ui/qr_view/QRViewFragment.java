package com.qrcode_quest.ui.qr_view;

import static com.qrcode_quest.ui.qr_view.QRViewFragmentDirections.actionQrshotToComments;
import static java.util.Objects.requireNonNull;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.qrcode_quest.MainViewModel;
import com.qrcode_quest.application.AppContainer;
import com.qrcode_quest.application.QRCodeQuestApp;
import com.qrcode_quest.database.QRManager;
import com.qrcode_quest.databinding.FragmentQrViewBinding;
import com.qrcode_quest.entities.PlayerAccount;
import com.qrcode_quest.entities.QRShot;
import com.qrcode_quest.entities.RawQRCode;
import com.qrcode_quest.ui.qr_view.QRViewFragmentDirections.ActionQrshotToComments;

import java.util.ArrayList;

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

    private UsernameViewAdapter playerViewAdapter;
    private MainViewModel mainViewModel;

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

        mainViewModel = new ViewModelProvider(this.requireActivity()).get(MainViewModel.class);
        binding = FragmentQrViewBinding.inflate(inflater, container, false);

        // Set up the "other players" RecyclerView
        playerViewAdapter = new UsernameViewAdapter(new ArrayList<>());
        RecyclerView otherPlayersList = binding.qrviewPlayerlist;
        otherPlayersList.setAdapter(playerViewAdapter);
        otherPlayersList.setLayoutManager(new LinearLayoutManager(otherPlayersList.getContext()));

        // Default to the loading spinner screen
        binding.qrviewMainLayout.setVisibility(View.GONE);
        binding.qrviewLoadingLayout.setVisibility(View.VISIBLE);

        // Fetch and load the QRShot data into the fragment
        mainViewModel.getCurrentPlayer().observe(getViewLifecycleOwner(), player -> {
            mainViewModel.getQRShots().observe(getViewLifecycleOwner(), shots -> {
                    // Load the data into the fragment
                    loadQRShot(shots, player);

                    // Make the fragment visible
                    binding.qrviewMainLayout.setVisibility(View.VISIBLE);
                    binding.qrviewLoadingLayout.setVisibility(View.GONE);
            });
        });

        // Hook up button listeners
        binding.qrviewOtherScansButton.setOnClickListener(view -> {
            // Swap the visibility of the view button and the list
            binding.qrviewOtherScansButton.setVisibility(View.GONE);
            binding.qrviewPlayerlist.setVisibility(View.VISIBLE);
        });
        binding.qrviewCommentsButton.setOnClickListener(view -> transitionToComments(shotHash));

        return binding.getRoot();
    }

    /**
     * Loads QRCode/QRShot data into the view
     * @param shots A list of all the QRShots
     * @param authedPlayer The player viewing the QRCode
     */
    @SuppressLint({"DefaultLocale", "NotifyDataSetChanged"})
    private void loadQRShot(ArrayList<QRShot> shots, PlayerAccount authedPlayer){
        AppCompatActivity main = requireNonNull((AppCompatActivity) this.getActivity());
        ArrayList<String> usersWhoScanned = playerViewAdapter.getItems();
        usersWhoScanned.clear();

        // Find all users who scanned the code, as well as find our target shot
        QRShot shot = null;
        for (QRShot testShot : shots){
            // Check if this is a shared shot
            if (testShot.getCodeHash().equals(shotHash)){
                usersWhoScanned.add(testShot.getOwnerName());
                // Check if this is the exact shot
                if (testShot.getOwnerName().equals(shotOwner)){
                    shot = testShot;
                }
            }
        }
        playerViewAdapter.notifyDataSetChanged();

        // Ensure we actually found the shot (given database integrity is sound, it will)
        if (shot == null) {
            Log.e(CLASS_TAG, "Failed to find QRShot in shot list.");
            return;
        }

        // Calculate the score of the shot
        String hash = shot.getCodeHash();
        int score = RawQRCode.getScoreFromHash(hash);

        // Load given data
        binding.qrviewName.setText(hash.substring(hash.length()-5));
        binding.qrviewScore.setText(String.format("%d", score));
        binding.qrviewOtherScans.setText(String.format("%d", usersWhoScanned.size()));

        // Try to load the image
        if (shot.getPhoto() != null){
            binding.qrviewPhoto.setImageBitmap(shot.getPhoto());
            binding.qrviewPhoto.setVisibility(View.VISIBLE);
        }

        // Try to load the geolocation
        if (shot.getLocation() != null ){
            binding.qrviewGeoloc.setText(shot.getLocation().toString());
            binding.qrviewGeolocContainer.setVisibility(View.VISIBLE);
        }

        // Show the owner buttons if privileged
        if (authedPlayer.getUsername().equals(shot.getOwnerName()) || authedPlayer.isOwner()){
            binding.qrviewDeleteButton.setVisibility(View.VISIBLE);
            binding.qrviewDeleteButton.setOnClickListener(view -> {
                binding.qrviewMainLayout.setVisibility(View.GONE);
                binding.qrviewLoadingLayout.setVisibility(View.VISIBLE);
                this.deleteQR();
            });
        }
    }

    /**
     * Navigates to the comment view using the hash of the viewed shot
     * @param qrHash The hash of the QR code to view comments for
     */
    private void transitionToComments(String qrHash){
        NavController navController = NavHostFragment.findNavController(this);
        ActionQrshotToComments action = actionQrshotToComments(qrHash);
        navController.navigate(action);
    }

    /**
     * Deletes the loaded QR code from the database and return to last screen.
     * Note: This will occur even if the delete failed.
     */
    private void deleteQR(){
        AppContainer container = ((QRCodeQuestApp) requireActivity().getApplication()).getContainer();
        QRManager qrManager = new QRManager(container.getDb(), container.getStorage());
        qrManager.removeQRShot(shotOwner, shotHash, result ->{
            if (!result.isSuccess()){
                Log.e(CLASS_TAG, "Failed to delete QRShot.");
                Toast.makeText(this.getActivity(), "Failed to delete.", Toast.LENGTH_LONG)
                        .show();
            }

            // Force a refresh on the QR codes
            mainViewModel.loadQRCodesAndShots();

            // Navigate backwards
            NavController navController = NavHostFragment.findNavController(this);
            navController.popBackStack();
        });
    }
}