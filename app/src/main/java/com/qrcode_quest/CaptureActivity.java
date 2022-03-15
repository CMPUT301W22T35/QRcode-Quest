package com.qrcode_quest;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;

import com.google.zxing.Result;
import com.qrcode_quest.zxing.Constant;
import com.qrcode_quest.zxing.camera.CameraManager;
import com.qrcode_quest.zxing.decoding.CaptureActivityHandler;
import com.qrcode_quest.zxing.decoding.InactivityTimer;
import com.qrcode_quest.zxing.view.ViewfinderView;

import java.io.IOException;

public class CaptureActivity extends AppCompatActivity implements SurfaceHolder.Callback  {

    private static final float BEEP_VOLUME = 0.10f;
    protected static final int BACK_PREVIEW = 1000;
    private static long curTime;
    protected CaptureActivityHandler handler;
    private ViewfinderView viewfinderView;
    private TextView txt_nonet_hint;
    private boolean hasSurface;
    private InactivityTimer inactivityTimer;
    private MediaPlayer mediaPlayer;
    private boolean playBeep;
    private boolean vibrate;
    protected String verify_code;
    protected String strhint_res ="Scan";;
    public static final int REQUEST_CODE_GET_PIC_URI=100;
    private SurfaceHolder surfaceHolder;
    private SurfaceView surfaceView;
    private CameraManager cameraManager;
    private static final int REQUEST_SD=1002;


    public CameraManager getCameraManager() {
        return cameraManager;
    }
    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView( R.layout.activity_capture);
        initView();
        initData();

    }


    protected void initData() {
        inactivityTimer = new InactivityTimer(this);
    }

    protected void initView() {
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        Toolbar toolbar =  findViewById(R.id.toolbar);
        if (toolbar==null){
            return;
        }
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setNavigationIcon(R.mipmap.nav_leftbai);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               finish();
            }
        });
        TextView mTitle =  toolbar.findViewById(R.id.tv_title);
        mTitle.setText("Scan");
        hasSurface = false;
        surfaceView = (SurfaceView) findViewById(R.id.preview_view);
        viewfinderView = (ViewfinderView) findViewById(R.id.viewfinder_view);
        viewfinderView.initTextHint(strhint_res);
    }








    @Override
    protected void onResume() {
        super.onResume();
        cameraManager = new CameraManager(getApplication());

        viewfinderView.setCameraManager(cameraManager);
        handler = null;
        playBeep = true;
        surfaceHolder = surfaceView.getHolder();
        if (hasSurface) {

            initCamera(surfaceHolder);
        } else {
            surfaceHolder.addCallback(this);
        }

        inactivityTimer.onResume();

        AudioManager audioService = (AudioManager) getSystemService(AUDIO_SERVICE);
        if (audioService.getRingerMode() != AudioManager.RINGER_MODE_NORMAL) {
            playBeep = false;
        }
        initBeepSound();
        vibrate = true;




    }

    @Override
    protected void onPause() {

        super.onPause();

    }

    @Override
    protected void onDestroy() {

        if (handler != null) {
            handler.quitSynchronously();
            handler = null;
        }
        inactivityTimer.onPause();
        cameraManager.closeDriver();

        if (!hasSurface) {

            surfaceHolder.removeCallback(this);
        }
        inactivityTimer.shutdown();
        viewfinderView.stopAnimator();
        super.onDestroy();
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_CAMERA:
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }
    public static boolean checkTime(long timeoffset) {
        long time = System.currentTimeMillis();
        if(time-curTime<timeoffset) {
            return false;
        }
        curTime = time;
        return true;
    }
    public void handleDecode(Result result) {
        if(!checkTime(1000))
            return;

        inactivityTimer.onActivity();
        playBeepSoundAndVibrate();
        verify_code = result.getText();
        if(TextUtils.isEmpty(verify_code)) {

            handler.sendEmptyMessageDelayed(Constant.RESTART_PREVIEW,BACK_PREVIEW);
            return;
        }
        if(!doAfterScan(verify_code)){
            handler.sendEmptyMessageDelayed(Constant.RESTART_PREVIEW,BACK_PREVIEW);
        }


    }

    private boolean doAfterScan(String verify_code) {

        //TODO
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
                handler = new CaptureActivityHandler(this, cameraManager);
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
        return viewfinderView;
    }

    public Handler getHandler() {
        return handler;
    }

    public void drawViewfinder() {
        viewfinderView.drawViewfinder();

    }

    private void initBeepSound() {
        if (playBeep && mediaPlayer == null) {
            // The volume on STREAM_SYSTEM is not adjustable, and users found it
            // too loud,
            // so we now play on the music stream.
            setVolumeControlStream(AudioManager.STREAM_MUSIC);
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



    private static final long VIBRATE_DURATION = 200L;

    private void playBeepSoundAndVibrate() {
        if (playBeep && mediaPlayer != null) {
            mediaPlayer.start();
        }
        if (vibrate) {
            Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
            vibrator.vibrate(VIBRATE_DURATION);
        }
    }

    /**
     * When the beep has finished playing, rewind to queue up another one.
     */
    private final MediaPlayer.OnCompletionListener beepListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mediaPlayer) {
            mediaPlayer.seekTo(0);
        }
    };

    public void continuePreview() {
        if (handler != null){
            handler.sendEmptyMessageDelayed(R.id.restart_preview,BACK_PREVIEW);
        }
    }



}
