package com.qrcode_quest.ui.capture;

import static android.content.Context.AUDIO_SERVICE;
import static android.content.Context.VIBRATOR_SERVICE;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
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
import com.qrcode_quest.ui.account.AccountFragment;
import com.qrcode_quest.ui.account.AccountFragmentDirections;
import com.qrcode_quest.ui.account.AccountViewModel;
import com.qrcode_quest.zxing.Constant;
import com.qrcode_quest.zxing.camera.CameraManager;
import com.qrcode_quest.zxing.decoding.CaptureFragmentHandler;
import com.qrcode_quest.zxing.view.ViewfinderView;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

public class CaptureFragment extends Fragment implements SurfaceHolder.Callback  {

    private static final float BEEP_VOLUME = 0.10f;
    protected static final int BACK_PREVIEW = 1000;
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
    private SurfaceHolder surfaceHolder;
    private CameraManager cameraManager;
    private static final int REQUEST_SD=1002;
    private static final long VIBRATE_DURATION = 200L;

    MainViewModel mainViewModel;
    CaptureViewModel captureViewModel;
    FragmentCaptureBinding binding;
    FirebaseFirestore db;
    PhotoStorage storage;

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
    public void onDestroy() {

        if (handler != null) {
            handler.quitSynchronously();
            handler = null;
        }
        cameraManager.closeDriver();

        if (!hasSurface) {
            surfaceHolder.removeCallback(this);
        }
        binding.viewfinderView.stopAnimator();
        super.onDestroy();
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
        if(!checkTime(1000))
            return;
        playBeepSoundAndVibrate();
        verify_code = result.getText();
        if(TextUtils.isEmpty(verify_code)) {
            handler.sendEmptyMessageDelayed(Constant.RESTART_PREVIEW, BACK_PREVIEW);
            return;
        }
        if(!doAfterScan(verify_code)){
            handler.sendEmptyMessageDelayed(Constant.RESTART_PREVIEW, BACK_PREVIEW);
        }
    }

    private boolean doAfterScan(String verify_code) {
        Log.d("CAPTURE_VERIFY_CODE" ,verify_code);
        Intent intent = new Intent();
        intent.putExtra("verify_code",verify_code);

        if (!TextUtils.isEmpty(verify_code)){
            Log.d(AccountFragment.class.getSimpleName(),"code:"+verify_code);

            String playerName = QRStringConverter.getPlayerNameFromLoginQRString(verify_code);
            if (playerName != null) {
                // handles login qr code
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
                return false;
            }
            playerName = QRStringConverter.getPlayerNameFromProfileQRString(verify_code);
            if (playerName != null) {
                // handles profile qr code
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
                return false;
            }

            // otherwise this is a normal qr code for score
            RawQRCode rawCode = new RawQRCode(verify_code);
            String path = requireActivity().getCacheDir() + "/images/qr.png";
            Bitmap bitmap = BitmapFactory.decodeFile(path);
            try {
                QRCode qrCode = new QRCode(rawCode);
                // TODO: ask if location/photo is to be recorded
                QRShot qrShot = new QRShot(captureViewModel.getCurrentPlayer().getUsername(),
                        qrCode.getHashCode(), bitmap, captureViewModel.getCurrentLocation());

                new QRManager(db, storage).createQRShot(qrShot, result -> { }, result -> {
                    if (result.isSuccess()) {
                        goToPlayerQRListFragment(captureViewModel.getCurrentPlayer());
                        mainViewModel.loadQRCodesAndShots();
                    }
                });

            } catch (UnsupportedEncodingException | NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
        }

        return false;
    }


    private void initCamera(SurfaceHolder surfaceHolder) {
        if (surfaceHolder == null) {
            throw new IllegalStateException("No SurfaceHolder provided");
        }
        if (cameraManager.isOpen()) {
            return;
        }
        try {
            cameraManager.openDriver(surfaceHolder);
            if (handler == null) {
                handler = new CaptureFragmentHandler(this, cameraManager);
            }
        } catch (IOException ioe) {
        } catch (RuntimeException e) {
            return;
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {

    }

    @Override
    public void surfaceCreated(final SurfaceHolder holder) {
        if (!hasSurface) {
            hasSurface = true;
            initCamera(holder);
        }

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        hasSurface = false;

    }

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
            // The volume on STREAM_SYSTEM is not adjustable, and users found it
            // too loud,
            // so we now play on the music stream.
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
}
