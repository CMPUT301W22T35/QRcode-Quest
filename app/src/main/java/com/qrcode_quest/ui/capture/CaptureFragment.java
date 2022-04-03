package com.qrcode_quest.ui.capture;

import static android.content.Context.AUDIO_SERVICE;
import static android.content.Context.VIBRATOR_SERVICE;

import android.content.Intent;
import android.content.res.AssetFileDescriptor;
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
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.zxing.Result;
import com.qrcode_quest.MainViewModel;
import com.qrcode_quest.R;
import com.qrcode_quest.application.AppContainer;
import com.qrcode_quest.application.QRCodeQuestApp;
import com.qrcode_quest.databinding.FragmentCaptureBinding;
import com.qrcode_quest.zxing.Constant;
import com.qrcode_quest.zxing.camera.CameraManager;
import com.qrcode_quest.zxing.decoding.CaptureFragmentHandler;
import com.qrcode_quest.zxing.view.ViewfinderView;

import java.io.IOException;

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
    protected String strhint_res ="Scan";;
    public static final int REQUEST_CODE_GET_PIC_URI=100;
    private SurfaceHolder surfaceHolder;
    private CameraManager cameraManager;
    private static final int REQUEST_SD=1002;
    private static final long VIBRATE_DURATION = 200L;

    MainViewModel mainViewModel;
    FragmentCaptureBinding binding;


    public CameraManager getCameraManager() {
        return cameraManager;
    }
    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                            ViewGroup container, Bundle savedInstanceState) {
        mainViewModel =
                new ViewModelProvider(requireActivity()).get(MainViewModel.class);
        AppContainer appContainer = ((QRCodeQuestApp) requireActivity().getApplication()).getContainer();

        binding = FragmentCaptureBinding.inflate(inflater, container, false);
        initView();
        return binding.getRoot();
    }

    protected void initView() {
        Window window = getActivity().getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
//        setSupportActionBar(binding.toolbar);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        getSupportActionBar().setDisplayShowTitleEnabled(false);
//        binding.toolbar.setNavigationIcon(R.mipmap.nav_leftbai);
//        binding.toolbar.setNavigationOnClickListener(view -> returnToAccountFragment());
//        binding.tvTitle.setText("Scan");
        hasSurface = false;
        binding.viewfinderView.initTextHint(strhint_res);
    }

    /** return the user to account fragment */
    public void returnToAccountFragment() {

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
        // TODO: deal with the scanned photo

        returnToAccountFragment();
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
            mediaPlayer.setOnCompletionListener(beepListener);

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

    /** When the beep has finished playing, rewind to queue up another one. */
    private final MediaPlayer.OnCompletionListener beepListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mediaPlayer) {
            mediaPlayer.seekTo(0);
        }
    };
}