package com.qrcode_quest.ui.account;

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
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

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

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

public class AccountFragment extends Fragment {
    private String name;
    private FragmentAccountBinding binding;
    private AccountViewModel accountViewModel;
    private MainViewModel mainViewModel;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        mainViewModel =
                new ViewModelProvider(requireActivity()).get(MainViewModel.class);

        AppContainer appContainer = ((QRCodeQuestApp) requireActivity().getApplication()).getContainer();
        ViewModelProvider.Factory accountViewModelFactory = new ViewModelProvider.Factory() {
            @NonNull
            @Override
            public <T extends ViewModel> T create(@NonNull Class<T> aClass) {
                if (aClass.isAssignableFrom(AccountViewModel.class))
                    return Objects.requireNonNull(aClass.cast(new AccountViewModel(
                            new QRManager(appContainer.getDb(), appContainer.getStorage()))));
                else
                    throw new IllegalArgumentException("Unexpected ViewModelClass type request received by the factory!");
            }
        };
        accountViewModel =
                new ViewModelProvider(this, accountViewModelFactory).get(AccountViewModel.class);
        binding = FragmentAccountBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        getGps();
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.test.setOnClickListener(view1 -> goCapture());
        mainViewModel.getCurrentPlayer().observe(getViewLifecycleOwner(), playerAccount -> {
            Log.d(AccountFragment.class.getSimpleName(),"onChanged");
            if (playerAccount != null) {
                name = playerAccount.getUsername();
                accountViewModel.createQRImage(
                        QRStringConverter.getLoginQRString(name),300,300);
            }
        });
        accountViewModel.getBitmapLivedata().observe(getViewLifecycleOwner(), new Observer<Bitmap>() {
            @Override
            public void onChanged(Bitmap bitmap) {
                binding.ivCode.setImageBitmap(bitmap);
            }
        });
    }



    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==100&&resultCode== Activity.RESULT_OK){
           String code = data.getStringExtra("verify_code");
           if (!TextUtils.isEmpty(code)){
               Log.d(AccountFragment.class.getSimpleName(),"code:"+code);

               String playerName = QRStringConverter.getPlayerNameFromLoginQRString(code);
               if (playerName != null) {
                   // handles login qr code
                   Log.d("LOGIN_SCAN", playerName);
                   // TODO: (after moving this piece of code into capture fragment)
                   // 1) transition back to account fragment with a message containing player's name
                   // 2) account fragment updates database to first check if the given player name
                   // exists, and if so insert a playerName-deviceId pair into PlayerDevice relation
                   return;
               }
               playerName = QRStringConverter.getPlayerNameFromProfileQRString(code);
               if (playerName != null) {
                   // handles profile qr code
                   Log.d("PROFILE_SCAN", playerName);
                   // TODO: (after moving this piece of code into capture fragment)
                   // 1) transition to playerQRListFragment with player's name as parameter
                   return;
               }

               // otherwise this is a normal qr code for score
               RawQRCode rawCode = new RawQRCode(code);
               String path = requireActivity().getCacheDir() + "/images/qr.png";
               Bitmap bitmap = BitmapFactory.decodeFile(path);
               try {
                   QRCode qrCode = new QRCode(rawCode);
                   QRShot qrShot = new QRShot(name, qrCode.getHashCode(), bitmap, location);
                   accountViewModel.uploadQrCode(qrShot);
                   // TODO: (after moving this piece of code into capture fragment)
                   // 1) after scanning, call mainViewModel.loadQRCodesAndShots() to force an update
               } catch (UnsupportedEncodingException | NoSuchAlgorithmException e) {
                   e.printStackTrace();
               }
           }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
    Geolocation location = null;
    private void getGps(){
        QRCodeQuestApp app = (QRCodeQuestApp) requireActivity().getApplication();
        AppContainer appContainer = app.getContainer();
        GPSLocationLiveData liveData = new GPSLocationLiveData(app.getApplicationContext(),
                appContainer.getLocationManager());
        if (liveData.isPermissionGranted()){
            liveData.observe(getViewLifecycleOwner(), new Observer<Location>() {
                @Override
                public void onChanged(Location geolocation) {
                    if (geolocation != null) {
                        location = new Geolocation(geolocation.getLatitude(),geolocation.getLongitude());
                    }
                }
            });
        }else{
            ActivityCompat.requestPermissions(requireActivity(), new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION}, 2);
        }
    }
    private void goCapture() {
        if (ActivityCompat.checkSelfPermission(requireActivity(), Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(),new String[]{Manifest.permission.CAMERA}, 1);
        } else {
            Navigation.findNavController(requireView()).navigate(R.id.action_navigation_account_to_captureFragment);
        }
    }

}