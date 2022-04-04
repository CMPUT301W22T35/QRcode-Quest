/*
 * Copyright (C) 2008 ZXing authors
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

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;

import com.google.zxing.Result;
import com.qrcode_quest.ui.capture.CaptureFragment;
import com.qrcode_quest.zxing.Constant;
import com.qrcode_quest.zxing.camera.CameraManager;
import com.qrcode_quest.zxing.view.ViewfinderResultPointCallback;
/**
 * this part of code is from https://github.com/yipianfengye/android-zxingLibrary
 */
public final class CaptureFragmentHandler extends Handler {

  private static final String CLASS_TAG = CaptureFragmentHandler.class
          .getSimpleName();

  private final CaptureFragment fragment;
  private final DecodeThread decodeThread;
  private State state;
  private final CameraManager cameraManager;

  private enum State {
    PREVIEW, SUCCESS, DONE
  }

  public CaptureFragmentHandler(CaptureFragment fragment, CameraManager cameraManager) {
    this.fragment = fragment;
    decodeThread = new DecodeThread(fragment,  new ViewfinderResultPointCallback(
            fragment.getViewfinderView()));
    decodeThread.start();
    state = State.SUCCESS;

    // Start ourselves capturing previews and decoding.
    this.cameraManager = cameraManager;
    cameraManager.startPreview();
    restartPreviewAndDecode();
  }

  @Override
  public void handleMessage(Message message) {
    switch (message.what) {
      case Constant.RESTART_PREVIEW:
        restartPreviewAndDecode();
        break;

      case Constant.DECODE_SUCCEEDED:
        state = State.SUCCESS;
        fragment.handleDecode((Result) message.obj);
        break;

      case Constant.DECODE_FAILED:
        state = State.PREVIEW;
        cameraManager.requestPreviewFrame(decodeThread.getHandler(),
                Constant.DECODE);
        break;

      case Constant.RETURN_SCAN_RESULT:
        fragment.returnToAccountFragment();
        break;
    }
  }

  /**
   * Closes the decode thread and stop the camera
   */
  public void quitSynchronously() {
    state = State.DONE;

    cameraManager.stopPreview();
    Message quit = Message.obtain(decodeThread.getHandler(), Constant.QUIT);
    quit.sendToTarget();

    try {
      decodeThread.join(500L);
    } catch (InterruptedException ignored){}

    removeMessages(Constant.DECODE_SUCCEEDED);
    removeMessages(Constant.DECODE_FAILED);

    cameraManager.closeDriver();
  }

  public void restartPreviewAndDecode() {
    if (state == State.SUCCESS) {
      state = State.PREVIEW;
      cameraManager.requestPreviewFrame(decodeThread.getHandler(),
              Constant.DECODE);
      fragment.drawViewfinder();
    }
  }

}
