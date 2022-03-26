package com.qrcode_quest.ui.qr_view;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.common.collect.ArrayTable;
import com.qrcode_quest.database.QRManager;
import com.qrcode_quest.entities.QRCode;
import com.qrcode_quest.entities.QRShot;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Fetches and caches data for QRViewFragment usages.
 *
 * @author jdumouch, tianming
 * @version 1.0
 */
public class QRViewModel extends ViewModel {
    /** A class tag used for logging */
    private final static String CLASS_TAG = "QRViewModel";

    private String shotOwner;
    private String shotHash;
    private MutableLiveData<QRShot> qrShot;
    private QRManager qrManager;

    /**
     * Fetches a QRShot for a specific user.
     * @param owner The owner of the QRShot to fetch
     * @param hash The hash of the QRCode the shot references
     */
    public LiveData<QRShot> getQRShot(String owner, String hash){
        if (qrShot == null){
            qrShot = new MutableLiveData<>();
            loadQRShot(owner, hash);
        }
        return qrShot;
    }

    /**
     * Loads a specific QRShot using an owner name and a hash value.
     * @param owner The owner of the qr shot to load
     * @param hash The hash value of the QRShot
     */
    private void loadQRShot(String owner, String hash){
        Log.d(CLASS_TAG, String.format("Loading QRShot [%s, %s]...", owner, hash));
        qrManager.getPlayerShotByHash(owner, hash, result -> {
            if (!result.isSuccess()){
                Log.e(CLASS_TAG, "Failed to load player QRShots");
                return;
            }

            if (result.unwrap() != null){
                shotOwner = owner;
                shotHash = hash;
                qrShot.setValue(result.unwrap());
                Log.d(CLASS_TAG, String.format("Loading QRShot [%s, %s]...done", owner, hash));

            }
            else{
                Log.d(CLASS_TAG, String.format("Loading QRShot [%s, %s]...failed (does not exist)", owner, hash));
            }
        });
    }

    /**
     * Gets a list containing all of the QRShots
     */
    public LiveData<ArrayList<QRShot>> getShots(){
        if (qrShots == null){
            qrShots = new MutableLiveData<>();
            updateQRShots();
        }
        return qrShots;
    }
    private MutableLiveData<ArrayList<QRShot>> qrShots;

    /**
     * Forces a refresh of the QRShots
     */
    public void updateQRShots(){
        Log.d("MainViewModel", "Loading QRShots...");
        qrManager.getAllQRShots(result ->{
            if (!result.isSuccess()){
                Log.e(CLASS_TAG, "Failed to load QRShots");
                return;
            }

            Log.d(CLASS_TAG, "Loading QRShots...done.");
            qrShots.setValue(result.unwrap());
        });
    }

    /**
     * Gets a list containing all of the QRCodes
     */
    public LiveData<HashMap<String, QRCode>> getCodes(){
        if (qrCodes == null){
            qrCodes = new MutableLiveData<>();
            updateQRCodes();
        }

        return qrCodes;
    }
    private MutableLiveData<HashMap<String, QRCode>> qrCodes;

    /**
     * Forces a refresh of the QRCodes
     */
    public void updateQRCodes(){
        Log.d(CLASS_TAG, "Loading QRCodes...");
        qrManager.getAllQRCodesAsMap(result ->{
            if (!result.isSuccess()){
                Log.e(CLASS_TAG, "Failed to load QR codes");
                return;
            }

            Log.d(CLASS_TAG, "Loading QRCodes...done.");
            qrCodes.setValue(result.unwrap());
        });
    }
}
