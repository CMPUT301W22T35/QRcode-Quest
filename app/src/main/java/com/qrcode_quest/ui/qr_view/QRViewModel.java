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
 * currently unused after replacing calls to this view model by calls to MainViewModel
 *
 * @author jdumouch, tianming
 * @version 1.1
 */
public class QRViewModel extends ViewModel {
    /** A class tag used for logging */
    private final static String CLASS_TAG = "QRViewModel";

    private String shotOwner;
    private String shotHash;
}
