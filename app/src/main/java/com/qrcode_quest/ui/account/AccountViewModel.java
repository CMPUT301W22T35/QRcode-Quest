package com.qrcode_quest.ui.account;

import android.graphics.Bitmap;
import android.location.Location;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.util.HashMap;
import java.util.Map;

/**
 * view model that holds states for account fragment
 */
public class AccountViewModel extends ViewModel {

    private final MutableLiveData<String> mText;
    private HashMap<Object, Object> pathToPhotos;
    private  MutableLiveData<Bitmap> bitmapLivedata = new MutableLiveData<Bitmap>();
    MutableLiveData<Location> actualLocation;
    /** creates the view model */
    public AccountViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is account fragment");
        pathToPhotos = new HashMap<>();
        actualLocation = new MutableLiveData<>();
    }

    /** @return the live data that contains the QRCode to display (an empty image when hide) */
    public MutableLiveData<Bitmap> getBitmapLivedata() {
        return bitmapLivedata;
    }

    /** @return the text livedata */
    public LiveData<String> getText() {
        return mText;
    }

    /**
     * create an invisible QR image of the given size
     * @param widthPix width in pixel
     * @param heightPix height in pixel
     */
    public void hideQRImage(int widthPix, int heightPix) {
        Bitmap bitmap = Bitmap.createBitmap(widthPix, heightPix, Bitmap.Config.ARGB_8888);
        bitmapLivedata.postValue(bitmap);
    }

    /**
     * create a QR image with the given message
     * @param content the content qr message
     * @param widthPix width in pixel
     * @param heightPix height in pixel
     */
    public void createQRImage(String content, int widthPix, int heightPix) {
        try {
            if (content == null || "".equals(content)) {
                return;
            }
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
            hints.put(EncodeHintType.MARGIN, 2);
            BitMatrix bitMatrix = new QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, widthPix, heightPix, hints);
            int[] pixels = new int[widthPix * heightPix];
            for (int y = 0; y < heightPix; y++) {
                for (int x = 0; x < widthPix; x++) {
                    if (bitMatrix.get(x, y)) {
                        pixels[y * widthPix + x] = 0xff000000;
                    } else {
                        pixels[y * widthPix + x] = 0xffffffff;
                    }
                }
            }

            Bitmap bitmap = Bitmap.createBitmap(widthPix, heightPix, Bitmap.Config.ARGB_8888);
            bitmap.setPixels(pixels, 0, widthPix, 0, 0, widthPix, heightPix);
            bitmapLivedata.postValue(bitmap);
        } catch (WriterException e) {
            e.printStackTrace();
        }
    }
}