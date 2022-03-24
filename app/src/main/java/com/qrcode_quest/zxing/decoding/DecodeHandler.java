/*
 * Copyright (C) 2010 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.qrcode_quest.zxing.decoding;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.PlanarYUVLuminanceSource;
import com.google.zxing.ReaderException;
import com.google.zxing.Result;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.qrcode_quest.CaptureActivity;
import com.qrcode_quest.zxing.Constant;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;

final class DecodeHandler extends Handler {

  private static final String TAG = DecodeHandler.class.getSimpleName();

  private final CaptureActivity activity;
  private final MultiFormatReader multiFormatReader;
  private boolean running = true;

  DecodeHandler(CaptureActivity activity, Map<DecodeHintType, Object> hints) {
    multiFormatReader = new MultiFormatReader();
    multiFormatReader.setHints(hints);
    this.activity = activity;
  }

  @Override
  public void handleMessage(Message message) {
    if (!running) {
      return;
    }
    switch (message.what) {
      case Constant.DECODE:

        decode((byte[]) message.obj, message.arg1, message.arg2);
        break;
      case Constant.QUIT:
        running = false;
        Looper.myLooper().quit();
        break;
    }
  }

  private void decode(byte[] data, int width, int height) {
    byte[] orginData = data;
    Result rawResult = null;

    byte[] rotatedData = new byte[data.length];
    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width; x++) {
        rotatedData[x * height + height - y - 1] = data[x + y * width];
      }
    }
    int tmp = width; // Here we are swapping, that's the difference to #11
    width = height;
    height = tmp;
    data = rotatedData;

    PlanarYUVLuminanceSource source = activity.getCameraManager()
            .buildLuminanceSource(data, width, height);


    if (source != null) {
      BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

      try {
        rawResult = multiFormatReader.decodeWithState(bitmap);
      } catch (ReaderException re) {


      } finally {
        multiFormatReader.reset();
      }
    }



    Handler handler = activity.getHandler();
    if (rawResult != null) {

      if (handler != null) {
        Bitmap bitmap = getBitmapFromByte(orginData,width,height);
        if (bitmap!=null){
          Log.e("zzz",bitmap.toString());
          saveBitmap(bitmap);
        }

        Message message = Message.obtain(handler,
                Constant.DECODE_SUCCEEDED, rawResult);
        message.sendToTarget();
      }
    } else {
      if (handler != null) {
        Message message = Message.obtain(handler, Constant.DECODE_FAILED);
        message.sendToTarget();
      }
    }
  }
  public Bitmap getBitmapFromByte(byte[] temp, int width, int height){
    if(temp != null){
      BitmapFactory.Options options = new BitmapFactory.Options();
      options.inJustDecodeBounds = true;
      YuvImage yuvImage = new YuvImage(temp, ImageFormat.NV21,width,height,null);
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      yuvImage.compressToJpeg(new Rect(0,0,width,height),100,baos);
      byte[] datas = baos.toByteArray();
      BitmapFactory.Options options2 = new BitmapFactory.Options();
      options2.inPreferredConfig = Bitmap.Config.RGB_565;
      Bitmap bitmap = BitmapFactory.decodeByteArray(datas, 0, datas.length,options2);
      return bitmap;
    }else{
      return null;
    }
  }
   void saveBitmap(Bitmap bm) {
    String path = activity.getCacheDir() + "/images/";
     File saveFile = new File(path, "qr.png");
     File file=new File(path);
     if (!file.exists()){
       file.mkdirs();

     }
     Log.e("zzz",file.getAbsolutePath());
     try {
       FileOutputStream saveImgOut = new FileOutputStream(saveFile);
       bm.compress(Bitmap.CompressFormat.JPEG, 80, saveImgOut);
       saveImgOut.flush();
       saveImgOut.close();
     } catch (IOException ex) {
       ex.printStackTrace();
     }
  }

}
