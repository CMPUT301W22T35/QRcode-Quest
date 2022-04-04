package com.qrcode_quest.ui.capture;

import static android.content.Context.AUDIO_SERVICE;
import static android.content.Context.VIBRATOR_SERVICE;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.zxing.Result;
import com.qrcode_quest.MainViewModel;
import com.qrcode_quest.R;
import com.qrcode_quest.application.AppContainer;
import com.qrcode_quest.application.QRCodeQuestApp;
import com.qrcode_quest.database.ManagerResult;
import com.qrcode_quest.database.PhotoStorage;
import com.qrcode_quest.database.PlayerManager;
import com.qrcode_quest.database.QRManager;
import com.qrcode_quest.databinding.FragmentCaptureBinding;
import com.qrcode_quest.entities.GPSLocationLiveData;
import com.qrcode_quest.entities.Geolocation;
import com.qrcode_quest.entities.PlayerAccount;
import com.qrcode_quest.entities.QRCode;
import com.qrcode_quest.entities.QRShot;
import com.qrcode_quest.entities.QRStringConverter;
import com.qrcode_quest.entities.RawQRCode;
import com.qrcode_quest.ui.login.LoginActivity;
import com.qrcode_quest.zxing.Constant;
import com.qrcode_quest.zxing.camera.CameraManager;
import com.qrcode_quest.zxing.decoding.CaptureFragmentHandler;
import com.qrcode_quest.zxing.view.ViewfinderView;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Objects;

public class CaptureFragment extends Fragment implements SurfaceHolder.Callback  {

    private static final float BEEP_VOLUME = 0.10f;
    protected static final int BACK_PREVIEW = 2500;
    private static long curTime;
    protected CaptureFragmentHandler handler;
    private TextView txt_nonet_hint;
    private boolean hasSurface;
    private MediaPlayer mediaPlayer;
    private boolean playBeep;
    private boolean vibrate;
    protected String verify_code;
    protected String strhint_res ="Find and scan a QR code";;
    public static final int REQUEST_CODE_GET_PIC_URI=100;
    protected SurfaceHolder surfaceHolder;
    protected CameraManager cameraManager;
    private static final int REQUEST_SD=1002;
    private static final long VIBRATE_DURATION = 200L;

    private MainViewModel mainViewModel;
    private CaptureViewModel captureViewModel;
    protected FragmentCaptureBinding binding;
    private FirebaseFirestore db;
    private PhotoStorage storage;
    // lock the scan after first scan so we don't have 2 scans asynchronously upload data simultaneously
    private boolean lockScan;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public CaptureFragment() {
    }

    public CameraManager getCameraManager() {
        return cameraManager;
    }
    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                            ViewGroup container, Bundle savedInstanceState) {
        mainViewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);

        lockScan = false;  // permit scan at start
        PlayerAccount player = CaptureFragmentArgs.fromBundle(getArguments()).getPlayer();
        AppContainer appContainer = ((QRCodeQuestApp) requireActivity().getApplication()).getContainer();
        db = appContainer.getDb();
        storage = appContainer.getStorage();
        ViewModelProvider.Factory captureViewModelFactory = new ViewModelProvider.Factory() {
            @NonNull
            @Override
            public <T extends ViewModel> T create(@NonNull Class<T> aClass) {
                if (aClass.isAssignableFrom(CaptureViewModel.class)) {
                    return Objects.requireNonNull(aClass.cast(new CaptureViewModel(player)));
                } else {
                    throw new IllegalArgumentException("Unexpected ViewModelClass type request received by the factory!");
                }
            }
        };
        captureViewModel =
                new ViewModelProvider(this, captureViewModelFactory).get(CaptureViewModel.class);

        binding = FragmentCaptureBinding.inflate(inflater, container, false);

        Window window = requireActivity().getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        hasSurface = false;
        binding.viewfinderView.initTextHint(strhint_res);

        startLocationUpdates();
        return binding.getRoot();
    }

    private void startLocationUpdates(){
        QRCodeQuestApp app = (QRCodeQuestApp) requireActivity().getApplication();
        AppContainer appContainer = app.getContainer();
        GPSLocationLiveData liveData = new GPSLocationLiveData(app.getApplicationContext(),
                appContainer.getLocationManager());
        if (liveData.isPermissionGranted()) {
            liveData.observe(getViewLifecycleOwner(), geolocation -> {
                if (geolocation != null) {
                    captureViewModel.setCurrentLocation(
                            new Geolocation(geolocation.getLatitude(), geolocation.getLongitude()));
                }
            });
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        // Workaround to let LoginCaptureFragment inherit
        if (requireActivity() instanceof LoginActivity) { return; }

        // handle camera and beeping
        cameraManager = new CameraManager(requireActivity().getApplication());

        binding.viewfinderView.setCameraManager(cameraManager);
        handler = null;
        playBeep = true;
        surfaceHolder = binding.previewView.getHolder();
        if (hasSurface) {
            initCamera(surfaceHolder);
        } else {
            surfaceHolder.addCallback(this);
        }

        AudioManager audioService = (AudioManager) requireActivity().getSystemService(AUDIO_SERVICE);
        if (audioService.getRingerMode() != AudioManager.RINGER_MODE_NORMAL) {
            playBeep = false;
        }
        initBeepSound();
        vibrate = true;
    }

    @Override
    public void onStop() {
        if (handler != null) {
            handler.quitSynchronously();
            handler = null;
        }

        if (!hasSurface) {
            surfaceHolder.removeCallback(this);
        }
        binding.viewfinderView.stopAnimator();
        super.onStop();
    }

    /**
     * check if the specified time has passed in milliseconds since the last time checkTime()
     * returns successfully
     * @param dt the minimum elapsed time required
     * @return true if given time has passed
     */
    public static boolean checkTime(long dt) {
        long time = System.currentTimeMillis();
        if(time - curTime < dt) {
            return false;
        }
        curTime = time;
        return true;
    }


    public void handleDecode(Result result) {
        if(!checkTime(BACK_PREVIEW))
            return;
        Log.d("RECORD_LOCK", Boolean.toString(lockScan));
        playBeepSoundAndVibrate();
        verify_code = result.getText();
        if (TextUtils.isEmpty(verify_code) || lockScan) {
            handler.sendEmptyMessageDelayed(Constant.RESTART_PREVIEW, BACK_PREVIEW);
            return;
        }
        if (!doAfterScan(verify_code)) {
            handler.sendEmptyMessageDelayed(Constant.RESTART_PREVIEW, BACK_PREVIEW);
        }
    }

    private boolean doAfterScan(String verify_code) {
        Log.d("CAPTURE_VERIFY_CODE" ,verify_code);
        Intent intent = new Intent();
        intent.putExtra("verify_code",verify_code);

        if (!TextUtils.isEmpty(verify_code)){
            // 3 cases: login, profile or other qr codes
            String playerName = QRStringConverter.getPlayerNameFromLoginQRString(verify_code);
            if (playerName != null) {
                onScanLoginQR(playerName);
                return false;
            }
            playerName = QRStringConverter.getPlayerNameFromProfileQRString(verify_code);
            if (playerName != null) {
                onScanProfileQR(playerName);
                return false;
            }
            onScanNormalQR();
        }

        return false;
    }

    private void onScanLoginQR(String playerName) {
        lockScan = true;
        Log.d("LOGIN_SCAN", playerName);
        QRCodeQuestApp app = (QRCodeQuestApp) requireActivity().getApplication();
        AppContainer appContainer = app.getContainer();
        mainViewModel.setCurrentPlayerByUsername(playerName, appContainer.getPrivateDevicePrefs(),
                result -> {
                    if (!result.isSuccess()) {
                        Log.e("LOGIN_SCAN", result.getError().getMessage());
                    } else if (result.unwrap() == null) {
                        Log.e("LOGIN_SCAN", "player does not exist");
                    } else {
                        // the player account has been updated
                        returnToAccountFragment();
                    }
                });
    }

    private void onScanProfileQR(String playerName) {
        lockScan = true;
        Log.d("PROFILE_SCAN", playerName);
        new PlayerManager(db).getPlayer(playerName, result -> {
            if (!result.isSuccess()) {
                Log.e("PROFILE_SCAN", result.getError().getMessage());
            } else if (result.unwrap() == null) {
                Log.e("PROFILE_SCAN", "player does not exist");
            } else {
                goToPlayerQRListFragment(result.unwrap());
            }
        });
    }

    private void onScanNormalQR() {
        lockScan = true;
        RawQRCode rawCode = new RawQRCode(verify_code);
        String path = requireActivity().getCacheDir() + "/images/qr.png";
        Bitmap bitmap = BitmapFactory.decodeFile(path);
        try {
            String qrHash = new QRCode(rawCode).getHashCode();
            onUploadingQRShot(new QRShot(captureViewModel.getCurrentPlayer().getUsername(),
                    qrHash, bitmap, captureViewModel.getCurrentLocation()));
        } catch (UnsupportedEncodingException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    private void onUploadingQRShot(QRShot shot) {
        Context context = requireActivity().getApplicationContext();
        ArrayList<QRShot> shots = mainViewModel.getQRShots().getValue();
        if (shots == null) {
            lockScan = false;
            return;
        }
        for (QRShot testShot : shots){
            if (testShot.getOwnerName().equals(shot.getOwnerName())
                && testShot.getCodeHash().equals(shot.getCodeHash())){
                Toast.makeText(requireContext(), "Already scanned", Toast.LENGTH_SHORT).show();
                lockScan = false;
                return;
            }
        }


        // StackOverflow, by Steve Haley
        // url: https://stackoverflow.com/questions/2478517/how-to-display-a-yes-no-dialog-box-on-android
        DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
            switch (which){
                case DialogInterface.BUTTON_POSITIVE:
                    break;

                case DialogInterface.BUTTON_NEGATIVE:
                    shot.setLocation(null);
                    break;
            }
            // show another dialog
            DialogInterface.OnClickListener dialogClickListener1 = (dialog1, which1) -> {
                switch (which1){
                    case DialogInterface.BUTTON_POSITIVE:
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        shot.setPhoto(null);
                        break;
                }
                // record stuffs and return
                ManagerResult.Listener<Void> listener = result -> {
                    if (result.isSuccess()) {
                        Log.d("RECORD_SHOT", "success");
                        goToQRShotFragment(shot.getOwnerName(), shot.getCodeHash());
                        mainViewModel.loadQRCodesAndShots();
                    } else {
                        // do nothing if the shot is already there
                        Log.d("RECORD_SHOT", "fail");
                        Toast toast = Toast.makeText(context, "QR code already recorded!",
                                Toast.LENGTH_SHORT);
                        toast.show();
                        lockScan = false;
                    }
                };
                new QRManager(db, storage).createQRShot(shot, listener, result -> { });
            };
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setMessage("Do you want to record photo?").setPositiveButton("Yes", dialogClickListener1)
                    .setNegativeButton("No", dialogClickListener1).show();
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage("Do you want to record location?").setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show();
    }


    private void initCamera(SurfaceHolder surfaceHolder) {
        if (surfaceHolder == null) {
            throw new IllegalStateException("No SurfaceHolder provided");
        }
        if (!cameraManager.isOpen()) {
            try {
                cameraManager.openDriver(surfaceHolder);
                if (handler == null) {
                    handler = new CaptureFragmentHandler(this, cameraManager);
                }
            } catch (IOException ignored){}
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {}

    @Override
    public void surfaceCreated(final SurfaceHolder holder) {
        if (!hasSurface) {
            hasSurface = true;
            initCamera(holder);
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) { hasSurface = false; }

    public ViewfinderView getViewfinderView() {
        return binding.viewfinderView;
    }

    public Handler getHandler() {
        return handler;
    }

    public void drawViewfinder() {
        binding.viewfinderView.drawViewfinder();
    }

    private void initBeepSound() {
        if (playBeep && mediaPlayer == null) {
            // https://github.com/oVirt/moVirt/commit/f4e0c03d28932c8cc97c7f71793fd51122951ff3
            // Author: Noise Doll
            // Date: 10 June, 2015
            // License: Apache 2.0
            requireActivity().setVolumeControlStream(AudioManager.STREAM_MUSIC);
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

            // When the beep has finished playing, rewind to queue up another one.
            mediaPlayer.setOnCompletionListener(mediaPlayer -> mediaPlayer.seekTo(0));

            AssetFileDescriptor file = getResources().openRawResourceFd(
                    R.raw.beep);
            try {
                mediaPlayer.setDataSource(file.getFileDescriptor(),
                        file.getStartOffset(), file.getLength());
                file.close();
                mediaPlayer.setVolume(BEEP_VOLUME, BEEP_VOLUME);
                mediaPlayer.prepare();
            } catch (IOException e) {
                mediaPlayer = null;
            }
        }
    }

    private void playBeepSoundAndVibrate() {
        if (playBeep && mediaPlayer != null) {
            mediaPlayer.start();
        }
        if (vibrate) {
            Vibrator vibrator = (Vibrator) requireActivity().getSystemService(VIBRATOR_SERVICE);
            vibrator.vibrate(VIBRATE_DURATION);
        }
    }

    /** return the user to account fragment */
    public void returnToAccountFragment() {
        NavHostFragment.findNavController(this).navigate(
                R.id.action_captureFragment_to_navigation_account);
    }

    /** go to player qr list fragment of the current player */
    public void goToPlayerQRListFragment(PlayerAccount player) {
        NavController navController = NavHostFragment.findNavController(this);
        CaptureFragmentDirections.ActionCaptureFragmentToNavigationPlayerQrlist action =
                CaptureFragmentDirections.actionCaptureFragmentToNavigationPlayerQrlist(player);
        navController.navigate(action);
    }

    /** go to qr view fragment for the newly captured qr shot */
    public void goToQRShotFragment(String ownerName, String qrHash) {
        // Navigate to the QRView of the clicked shot
        NavController navController = NavHostFragment.findNavController(this);
        CaptureFragmentDirections.ActionCaptureFragmentToNavigationQrshot action =
                CaptureFragmentDirections.actionCaptureFragmentToNavigationQrshot(ownerName, qrHash);
        navController.navigate(action);
    }
}
