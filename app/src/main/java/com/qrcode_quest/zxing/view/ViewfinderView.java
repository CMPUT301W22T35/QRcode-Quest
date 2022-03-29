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

package com.qrcode_quest.zxing.view;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.google.zxing.ResultPoint;
import com.qrcode_quest.R;
import com.qrcode_quest.zxing.camera.CameraManager;

import java.util.ArrayList;
import java.util.List;


public final class ViewfinderView extends View {
	private static final int CURRENT_POINT_OPACITY = 0xA0;
	private static final int MAX_RESULT_POINTS = 20;
	private final float density;
	private CameraManager cameraManager;
	private Paint paint, scanLinePaint, reactPaint, frameLinePaint;
	private Bitmap resultBitmap;
	private int maskColor; 
	private int resultColor;
	private int reactColor;
	private int scanLineColor;
	private int frameLineColor = -1;


	private List<ResultPoint> possibleResultPoints;
	
	private int scanLineTop;

	private ValueAnimator valueAnimator;
	private Rect frame;
	private String strHint;
	private float strHintWidth;
	private Paint hintPaint;


	public ViewfinderView(Context context) {
		this(context, null);

	}

	public ViewfinderView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);


	}





	public ViewfinderView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		reactColor = ContextCompat.getColor(getContext(), R.color.color_A545E6);
		density = context.getResources().getDisplayMetrics().density;
		frameLineColor =-1;

		scanLineColor = ContextCompat.getColor(getContext(), R.color.color_A545E6);
		initPaint();
		maskColor = ContextCompat.getColor(getContext(), R.color.viewfinder_mask);
		resultColor = ContextCompat.getColor(getContext(), R.color.result_view);
		possibleResultPoints = new ArrayList<>(10);

	}

	private void initPaint() {
		paint = new Paint(Paint.ANTI_ALIAS_FLAG);

		
		reactPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		reactPaint.setColor(reactColor);
		reactPaint.setStyle(Paint.Style.FILL);
		reactPaint.setStrokeWidth(dp2px(1));

		

		if (frameLineColor != -1) {
			frameLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
			frameLinePaint.setColor(frameLineColor);
			frameLinePaint.setStrokeWidth(dp2px(1));
			frameLinePaint.setStyle(Paint.Style.STROKE);
		}


		scanLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		scanLinePaint.setStrokeWidth(dp2px(2));
		scanLinePaint.setStyle(Paint.Style.FILL);
		scanLinePaint.setDither(true);
		scanLinePaint.setColor(scanLineColor);

		strHint = getResources().getString(R.string.scan_text);
		hintPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		hintPaint.setTextSize(14*density);
		hintPaint.setColor(Color.WHITE);
		hintPaint.setTypeface(Typeface.DEFAULT);
		strHintWidth = hintPaint.measureText(strHint);

	}

	private void initAnimator() {

		if (valueAnimator == null) {
			valueAnimator = ValueAnimator.ofInt(frame.top, frame.bottom);
			valueAnimator.setDuration(3000);
			valueAnimator.setInterpolator(new DecelerateInterpolator());
			valueAnimator.setRepeatMode(ValueAnimator.RESTART);
			valueAnimator.setRepeatCount(ValueAnimator.INFINITE);
			valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
				@Override
				public void onAnimationUpdate(ValueAnimator animation) {

					scanLineTop = (int) animation.getAnimatedValue();
					invalidate();

				}
			});

			valueAnimator.start();
		}


	}

	public void setCameraManager(CameraManager cameraManager) {
		this.cameraManager = cameraManager;


	}

	public void stopAnimator() {
		if (valueAnimator != null) {
			valueAnimator.end();
			valueAnimator.cancel();
			valueAnimator = null;
		}

	}

	@SuppressLint("DrawAllocation")
	@Override
	public void onDraw(Canvas canvas) {

		if (cameraManager == null) {
			return;
		}

		
		frame = cameraManager.getFramingRect();
		Rect previewFrame = cameraManager.getFramingRectInPreview();
		if (frame == null || previewFrame == null) {
			return;
		}
		initAnimator();

		int width = canvas.getWidth();
		int height = canvas.getHeight();

	
		drawMaskView(canvas, frame, width, height);

		
		drawFrameBounds(canvas, frame);

		if (resultBitmap != null) {
			paint.setAlpha(CURRENT_POINT_OPACITY);
			canvas.drawBitmap(resultBitmap, null, frame, paint);
		} else {

			drawScanLight(canvas, frame);
		}

		strHintWidth = hintPaint.measureText(strHint);
		canvas.drawText(strHint,(width-strHintWidth)/2,(frame.bottom+32*density),hintPaint);
	}


	private void drawMaskView(Canvas canvas, Rect frame, int width, int height) {
		// Draw the exterior (i.e. outside the framing rect) darkened
		
		paint.setColor(resultBitmap != null ? resultColor : maskColor);
		
		canvas.drawRect(0, 0, width, frame.top, paint);
		
		canvas.drawRect(0, frame.top, frame.left, frame.bottom + 1, paint);
	
		canvas.drawRect(frame.right + 1, frame.top, width, frame.bottom + 1,
				paint);
		
		canvas.drawRect(0, frame.bottom + 1, width, height, paint);


	}


	/**
	 *
	 * @param canvas
	 * @param frame
	 */
	private void drawFrameBounds(Canvas canvas, Rect frame) {

		
		if (frameLineColor != -1) {
			canvas.drawRect(frame, frameLinePaint);
		}


		
		int width = frame.width();
		int corLength = (int) (width * 0.07);
		int corWidth = (int) (corLength * 0.2);

		corWidth = corWidth > 15 ? 15 : corWidth;


		
		canvas.drawRect(frame.left - corWidth, frame.top, frame.left, frame.top
				+ corLength, reactPaint);
		canvas.drawRect(frame.left - corWidth, frame.top - corWidth, frame.left
				+ corLength, frame.top, reactPaint);
		
		canvas.drawRect(frame.right, frame.top, frame.right + corWidth,
				frame.top + corLength, reactPaint);
		canvas.drawRect(frame.right - corLength, frame.top - corWidth,
				frame.right + corWidth, frame.top, reactPaint);
		
		canvas.drawRect(frame.left - corWidth, frame.bottom - corLength,
				frame.left, frame.bottom, reactPaint);
		canvas.drawRect(frame.left - corWidth, frame.bottom, frame.left
				+ corLength, frame.bottom + corWidth, reactPaint);
		
		canvas.drawRect(frame.right, frame.bottom - corLength, frame.right
				+ corWidth, frame.bottom, reactPaint);
		canvas.drawRect(frame.right - corLength, frame.bottom, frame.right
				+ corWidth, frame.bottom + corWidth, reactPaint);
	}


	/**
	 *
	 * @param canvas
	 * @param frame
	 */
	private void drawScanLight(Canvas canvas, Rect frame) {

		canvas.drawLine(frame.left, scanLineTop, frame.right, scanLineTop, scanLinePaint);


	}

	public void drawViewfinder() {
		Bitmap resultBitmap = this.resultBitmap;
		this.resultBitmap = null;
		if (resultBitmap != null) {
			resultBitmap.recycle();
		}
		invalidate();
	}

	public void addPossibleResultPoint(ResultPoint point) {
		List<ResultPoint> points = possibleResultPoints;
		synchronized (points) {
			points.add(point);
			int size = points.size();
			if (size > MAX_RESULT_POINTS) {
				// trim it
				points.subList(0, size - MAX_RESULT_POINTS / 2).clear();
			}
		}
	}


	private int dp2px(int dp) {
		return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());

	}

	public void initTextHint(String strhint_res) {
		strHint =strhint_res;
	}
}
