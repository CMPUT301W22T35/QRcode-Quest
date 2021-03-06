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

package com.qrcode_quest.zxing.camera;

import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.Handler;
import android.util.Log;
import android.view.SurfaceHolder;

import com.google.zxing.PlanarYUVLuminanceSource;
import com.qrcode_quest.R;

import java.io.IOException;
/**
 * this part of code is from https://github.com/yipianfengye/android-zxingLibrary
 *
 * modified by tianming: change getFramingRect
 */
public final class CameraManager {

  private static final String TAG = CameraManager.class.getSimpleName();

  private static CameraManager cameraManager;

  private final Context context;
  private final CameraConfigurationManager configManager;
  private Camera camera;
  private AutoFocusCallback autoFocusManager;
  private Rect framingRect;
  private Rect framingRectInPreview;
  private boolean initialized;
  private boolean previewing;
  private int requestedCameraId = -1;
  private int requestedFramingRectWidth;
  private int requestedFramingRectHeight;
  /**
   * Preview frames are delivered here, which we pass on to the registered
   * handler. Make sure to clear the handler so it will only receive one
   * message.
   */
  private final PreviewCallback previewCallback;

  public CameraManager(Context context) {
    this.context = context;
    this.configManager = new CameraConfigurationManager(context);
    previewCallback = new PreviewCallback(configManager);
  }

//    public static void init(Context context) {
//        if (cameraManager == null) {
//            cameraManager = new CameraManager(context, null);
//        }
//    }


  /**
   * Opens the camera driver and initializes the hardware parameters.
   *
   * @param holder The surface object which the camera will draw preview frames
   *               into.
   * @throws IOException Indicates the camera driver failed to open.
   */
  public synchronized void openDriver(SurfaceHolder holder)
          throws IOException {
    Camera theCamera = camera;

    if (theCamera == null) {

      if (requestedCameraId >= 0) {
        theCamera = OpenCameraInterface.open(requestedCameraId);
      } else {
        theCamera = OpenCameraInterface.open();
      }

      if (theCamera == null) {
        throw new IOException();
      }
      camera = theCamera;
    }
    theCamera.setPreviewDisplay(holder);

    if (!initialized) {
      initialized = true;
      configManager.initFromCameraParameters(theCamera);
      if (requestedFramingRectWidth > 0 && requestedFramingRectHeight > 0) {
        setManualFramingRect(requestedFramingRectWidth,
                requestedFramingRectHeight);
        requestedFramingRectWidth = 0;
        requestedFramingRectHeight = 0;
      }
    }

    Camera.Parameters parameters = theCamera.getParameters();
    String parametersFlattened = parameters == null ? null : parameters
            .flatten(); // Save these, temporarily
    try {
      configManager.setDesiredCameraParameters(theCamera);
    } catch (RuntimeException re) {
      // Driver failed
      Log.w(TAG,
              "Camera rejected parameters. Setting only minimal safe-mode parameters");
      Log.i(TAG, "Resetting to saved camera params: "
              + parametersFlattened);
      // Reset:
      if (parametersFlattened != null) {
        parameters = theCamera.getParameters();
        parameters.unflatten(parametersFlattened);
        try {
          theCamera.setParameters(parameters);
          configManager.setDesiredCameraParameters(theCamera);
        } catch (RuntimeException re2) {
          // Well, darn. Give up
          Log.w(TAG,
                  "Camera rejected even safe-mode parameters! No configuration");
        }
      }
    }

  }

  public synchronized boolean isOpen() {
    return camera != null;
  }

  /**
   * Closes the camera driver if still in use.
   */
  public synchronized void closeDriver() {
    if (camera != null) {
      camera.release();
      camera = null;
      // Make sure to clear these each time we close the camera, so that
      // any scanning rect
      // requested by intent is forgotten.
      framingRect = null;
      framingRectInPreview = null;
    }
  }




  /**
   * Asks the camera hardware to begin drawing preview frames to the screen.
   */
  public synchronized void startPreview() {
    Camera theCamera = camera;
    if (theCamera != null && !previewing) {
      theCamera.startPreview();
      previewing = true;
      autoFocusManager = new AutoFocusCallback(camera);
    }
  }

  /**
   * Tells the camera to stop drawing preview frames.
   */
  public synchronized void stopPreview() {
    if (autoFocusManager != null) {
      autoFocusManager.stop();
      autoFocusManager = null;
    }
    if (camera != null && previewing) {
      camera.stopPreview();
      previewCallback.setHandler(null, 0);
      previewing = false;
    }
  }

  /**
   */
  public synchronized void requestPreviewFrame(Handler handler, int message) {
    Camera theCamera = camera;
    if (theCamera != null && previewing) {
      previewCallback.setHandler(handler, message);
      theCamera.setOneShotPreviewCallback(previewCallback);
    }
  }

  private double cameraYResolutionMultiplier = 0.74;

  public void setCameraYResolutionMultiplier(double cameraYResolutionMultiplier) {
    this.cameraYResolutionMultiplier = cameraYResolutionMultiplier;
  }

  public synchronized Rect getFramingRect() {
    if (framingRect == null) {
      if (camera == null) {
        return null;
      }
      Point screenResolution = configManager.getScreenResolution();

      if (screenResolution == null) {
        // Called early, before init even finished
        return null;
      }

      int screenResolutionX = screenResolution.x;
      int screenResolutionY = screenResolution.y;
      int width = (int) (screenResolutionX * 0.74);
      int height = (int) (screenResolutionY * cameraYResolutionMultiplier);


      int leftOffset = (screenResolutionX - width) / 2;
      int topOffset = 0;
      Log.d("x", Integer.toString(leftOffset));
      Log.d("y", Integer.toString(topOffset));

      framingRect = new Rect(leftOffset, topOffset, leftOffset + width,
              topOffset + height);
      Log.d(TAG, "Calculated framing rect: " + framingRect);
    }
    return framingRect;
  }


  /**
   * Like {@link #getFramingRect} but coordinates are in terms of the preview
   */
  public synchronized Rect getFramingRectInPreview() {
    if (framingRectInPreview == null) {
      Rect framingRect = getFramingRect();
      if (framingRect == null) {
        return null;
      }
      Rect rect = new Rect(framingRect);
      Point cameraResolution = configManager.getCameraResolution();
      Point screenResolution = configManager.getScreenResolution();
      if (cameraResolution == null || screenResolution == null) {
        // Called early, before init even finished
        return null;
      }

      rect.left = rect.left * cameraResolution.y / screenResolution.x;
      rect.right = rect.right * cameraResolution.y / screenResolution.x;
      rect.top = rect.top * cameraResolution.x / screenResolution.y;
      rect.bottom = rect.bottom * cameraResolution.x / screenResolution.y;
      framingRectInPreview = rect;
    }
    return framingRectInPreview;
  }

  /**
   * Allows third party apps to specify the camera ID, rather than determine
   * it automatically based on available cameras and their orientation.
   *
   * @param cameraId camera ID of the camera to use. A negative value means
   *                 "no preference".
   */
  public synchronized void setManualCameraId(int cameraId) {
    requestedCameraId = cameraId;
  }

  /**
   * Allows third party apps to specify the scanning rectangle dimensions,
   * rather than determine them automatically based on screen resolution.
   *
   * @param width  The width in pixels to scan.
   * @param height The height in pixels to scan.
   */
  public synchronized void setManualFramingRect(int width, int height) {
    if (initialized) {
      Point screenResolution = configManager.getScreenResolution();
      if (width > screenResolution.x) {
        width = screenResolution.x;
      }
      if (height > screenResolution.y) {
        height = screenResolution.y;
      }
      int leftOffset = (screenResolution.x - width) / 2;
      int topOffset = (screenResolution.y - height) / 5;
      framingRect = new Rect(leftOffset, topOffset, leftOffset + width,
              topOffset + height);
      Log.d(TAG, "Calculated manual framing rect: " + framingRect);
      framingRectInPreview = null;
    } else {
      requestedFramingRectWidth = width;
      requestedFramingRectHeight = height;
    }
  }

  /**
   * A factory method to build the appropriate LuminanceSource object based on
   * the format of the preview buffers, as described by Camera.Parameters.
   *
   * @param data   A preview frame.
   * @param width  The width of the image.
   * @param height The height of the image.
   * @return A PlanarYUVLuminanceSource instance.
   */
  public PlanarYUVLuminanceSource buildLuminanceSource(byte[] data,
                                                       int width, int height) {
    Rect rect = getFramingRectInPreview();
    if (rect == null) {
      return null;
    }



    int actionbarHeight = context.getResources().getDimensionPixelSize(R.dimen.toolBarHeight);
    return new PlanarYUVLuminanceSource(data, width, height, rect.left,
            rect.top + actionbarHeight, rect.width(), rect.height(), false);
  }

  public static CameraManager get() {
    return cameraManager;
  }

}
