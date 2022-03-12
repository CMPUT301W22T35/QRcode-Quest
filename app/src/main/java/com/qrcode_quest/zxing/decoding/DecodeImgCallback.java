package com.qrcode_quest.zxing.decoding;

import com.google.zxing.Result;


public interface DecodeImgCallback {
    void onImageDecodeSuccess(Result result);

    void onImageDecodeFailed();
}
