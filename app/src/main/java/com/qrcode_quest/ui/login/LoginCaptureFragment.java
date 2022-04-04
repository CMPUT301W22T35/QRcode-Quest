package com.qrcode_quest.ui.login;

import static com.qrcode_quest.Constants.AUTHED_USERNAME_PREF;
import static com.qrcode_quest.Constants.DEVICE_UID_PREF;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.zxing.Result;
import com.qrcode_quest.application.AppContainer;
import com.qrcode_quest.application.QRCodeQuestApp;
import com.qrcode_quest.database.PlayerManager;
import com.qrcode_quest.databinding.FragmentCaptureBinding;
import com.qrcode_quest.entities.QRStringConverter;
import com.qrcode_quest.ui.capture.CaptureFragment;
import com.qrcode_quest.zxing.Constant;
import com.qrcode_quest.zxing.camera.CameraManager;
import com.qrcode_quest.zxing.decoding.CaptureFragmentHandler;

import java.io.IOException;
import java.util.UUID;

/**
 * Handles scanning login QR codes.
 *
 * This will ignore every QR code that wasn't specifically created for authentication.
 *
 * Largely a boiled down version of CaptureFragment
 *
 * @author jdumouch
 * @version 1.0
 */
public class LoginCaptureFragment extends CaptureFragment implements SurfaceHolder.Callback {
    private static final String CLASS_TAG = "LoginCaptureFragment";
    private static long curTime;
    private boolean lockScan;
    private boolean hasSurface;
    private Thread decodeThread;

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    public interface RegisterHandler {
        /**
         * Called on a successful scan.
         * @param deviceUID The unique device ID used to register a session
         * @param username The username of the registered player
         */
        void onRegistered(String deviceUID, String username);
    }

    // ------ SurfaceHolder.Callback ----------//
    @Override
    public void surfaceChanged(@NonNull SurfaceHolder surfaceHolder, int i, int i1, int i2) {}
    @Override
    public void surfaceCreated(@NonNull SurfaceHolder surfaceHolder) {
        if (!hasSurface){
            hasSurface = true;
            initCamera(surfaceHolder);
        }
    }
    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder surfaceHolder) { hasSurface=false; }


    /** Mandatory empty constructor */
    public LoginCaptureFragment(){}


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding =  FragmentCaptureBinding.inflate(inflater, container, false);

        Window window = requireActivity().getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        hasSurface= false;
        lockScan = false;

        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        cameraManager = new CameraManager(requireActivity().getApplication());
        binding.viewfinderView.setCameraManager(cameraManager);
        surfaceHolder = binding.previewView.getHolder();

        if (hasSurface) initCamera(surfaceHolder);
        else surfaceHolder.addCallback(this);
    }

    @Override
    public void handleDecode(Result result){
        if (!checkTime(1000)) return;
        verify_code = result.getText();
        Log.d(CLASS_TAG, "Read " + verify_code);
        if (verify_code.isEmpty() || lockScan){
            handler.sendEmptyMessageDelayed(Constant.RESTART_PREVIEW, BACK_PREVIEW);
            return;
        }

        String playerName = QRStringConverter.getPlayerNameFromLoginQRString(verify_code);
        if (playerName != null) {
            lockScan = true;
            registerPlayer(playerName);
        }
        else {
            Toast.makeText(requireContext(), "Invalid login code", Toast.LENGTH_SHORT).show();
            handler.sendEmptyMessageDelayed(Constant.RESTART_PREVIEW, BACK_PREVIEW);
        }
    }

    /**
     * Registers a player using the passed username.
     * Calls the listener on success.
     */
    private void registerPlayer(String playerName){
        AppContainer appContainer = ((QRCodeQuestApp) requireActivity().getApplication()).getContainer();
        FirebaseFirestore db = appContainer.getDb();
        SharedPreferences sharedPrefs = appContainer.getPrivateDevicePrefs();

        // Generate and store a deviceUID
        String deviceUID = UUID.randomUUID().toString();
        SharedPreferences.Editor prefEditor = sharedPrefs.edit();
        prefEditor.putString(DEVICE_UID_PREF, deviceUID);
        prefEditor.putString(AUTHED_USERNAME_PREF, playerName);
        prefEditor.apply();

        // Save the new session to the database
        new PlayerManager(db).createPlayerSession(deviceUID, playerName, result -> {
            if (!result.isSuccess()){
                Toast.makeText(getContext(), "Failed to create new player session.",
                        Toast.LENGTH_LONG).show();
                Log.e(CLASS_TAG, "Failed to create player session.");
                handler.sendEmptyMessageDelayed(Constant.RESTART_PREVIEW, BACK_PREVIEW);
                lockScan = false;
                return;
            }

            // Grab the login's onRegistered function and register using the session data
            RegisterHandler listener = (RegisterHandler) requireActivity();
            listener.onRegistered(deviceUID, playerName);
        });
    }

    /**
     * Attempts to re-open the camera and spawn a new camera handler.
     */
    private void initCamera(SurfaceHolder surfaceHolder){
        assert surfaceHolder != null;

        if (!cameraManager.isOpen()){
            try {
                cameraManager.openDriver(surfaceHolder);
                if (handler == null){
                    handler = new CaptureFragmentHandler(this, cameraManager);
                }
            } catch (IOException ignored){}
        }
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

}
