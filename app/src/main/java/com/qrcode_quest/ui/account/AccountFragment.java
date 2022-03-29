package com.qrcode_quest.ui.account;

import static com.qrcode_quest.Constants.AUTHED_USERNAME_PREF;
import static com.qrcode_quest.Constants.DEVICE_UID_PREF;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.qrcode_quest.CaptureActivity;
import com.qrcode_quest.MainViewModel;
import com.qrcode_quest.application.AppContainer;
import com.qrcode_quest.application.QRCodeQuestApp;
import com.qrcode_quest.database.QRManager;
import com.qrcode_quest.databinding.FragmentAccountBinding;
import com.qrcode_quest.entities.PlayerAccount;
import com.qrcode_quest.entities.QRShot;
import com.qrcode_quest.ui.playerQR.PlayerQRListViewModel;

import java.util.Objects;

public class AccountFragment extends Fragment {
    private String name;
    private FragmentAccountBinding binding;
    private AccountViewModel accountViewModel;
    private MainViewModel mainViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        mainViewModel =
                new ViewModelProvider(getActivity()).get(MainViewModel.class);

        AppContainer appContainer = ((QRCodeQuestApp) getActivity().getApplication()).getContainer();
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

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent =  new Intent(getActivity(), CaptureActivity.class);
                startActivityForResult(intent,100);
            }
        });
        mainViewModel.getCurrentPlayer().observe(getViewLifecycleOwner(), new Observer<PlayerAccount>() {


            @Override
            public void onChanged(PlayerAccount playerAccount) {

                Log.e(AccountFragment.class.getSimpleName(),"onChanged");
                if (playerAccount!=null){
                     name = playerAccount.getUsername();
                    String email = playerAccount.getEmail();
                    accountViewModel.createQRImage(name+"##"+email,300,300);

                }
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
               Log.e(AccountFragment.class.getSimpleName(),"code:"+code);
               String path = getActivity().getCacheDir() + "/images/qr.png";
               Bitmap bitmap = BitmapFactory.decodeFile(path);
               QRShot qrShot = new QRShot(name,code.hashCode()+"",bitmap,null);
               accountViewModel.uploadQrCode(qrShot);
           }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}