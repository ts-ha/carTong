/*
[현대자동차 블루링크TnB APP] version [1.0]

Copyright ⓒ [2014] kt corp. All rights reserved.

This is a proprietary software of kt corp, and you may not use this file except in compliance with license agreement with kt corp. 
Any redistribution or use of this software, with or without modification shall be strictly prohibited without prior written approval of kt corp, and the copyright notice above does not evidence any actual or intended publication of such software.
 */

package com.neighbor.ex.tong.ui.activity;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.util.AttributeSet;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;

public class OilCameraPreview extends SurfaceView implements SurfaceHolder.Callback {// ,
    public SurfaceHolder mHolder;
    public Camera mCamera = null;
    public Camera.Parameters mParameters;
    Size mPreviewSize;
    List<Size> mSupportedPreviewSizes;
    // OnGestureListener
    // {
    private Context mContext;

    public OilCameraPreview(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        try {
            if (mCamera == null)
                mCamera = Camera.open();

            mHolder = getHolder();

            mHolder.addCallback(this);
            mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

            mSupportedPreviewSizes = mCamera.getParameters()
                    .getSupportedPreviewSizes();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void close() {
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public void surfaceCreated(SurfaceHolder holder) {
        try {
            if (mCamera == null) {
                mCamera = Camera.open();
            }

            if (mCamera != null) {
                // mSupportedPreviewSizes =
                // mCamera.getParameters().getSupportedPreviewSizes();

                try {
                    mCamera.setPreviewDisplay(mHolder);
                    Camera.Parameters parameters = mCamera.getParameters();
                    mCamera.setParameters(parameters);
                    mCamera.startPreview();
                } catch (IOException exception) {
                    exception.printStackTrace();
                    mCamera.release();
                    mCamera = null;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        Camera.Parameters parameters = mCamera.getParameters();
        // parameters.setPreviewSize(parameters.getPictureSize().width,
        // parameters.getPictureSize().height);
        // parameters.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
        // mCamera.setParameters(parameters);

        if (mSupportedPreviewSizes != null) {
            mPreviewSize = getOptimalPreviewSize(mSupportedPreviewSizes, w, h);
            // mPreviewSize = getBestPreviewSize(w, h);
        }

        parameters.setPreviewSize(mPreviewSize.width, mPreviewSize.height);

        try {
            mCamera.setParameters(parameters);
        } catch (RuntimeException e) {
            // Log.d("SseOk", "RuntimeException : " + e.getMessage());
        }
    }

    private Size getBestPreviewSize(int width, int height) {
        Size result = null;
        Camera.Parameters p = mCamera.getParameters();
        for (Size size : p.getSupportedPreviewSizes()) {
            if (size.width <= width && size.height <= height) {
                if (result == null) {
                    result = size;
                } else {
                    int resultArea = result.width * result.height;
                    int newArea = size.width * size.height;

                    if (newArea > resultArea) {
                        result = size;
                    }
                }
            }
        }
        return result;

    }

    public int setCameraDisplayOrientation() {
        if (mCamera == null) {
            return 0;
        }

        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(0, info);

        WindowManager winManager = (WindowManager) mContext
                .getSystemService(Context.WINDOW_SERVICE);
        int rotation = winManager.getDefaultDisplay().getRotation();

        int degrees = 0;

        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }
        return degrees;
    }

    public Camera.Parameters getCameraParameters() {
        return mCamera.getParameters();
    }

    private Size getOptimalPreviewSize(List<Size> sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio = (double) w / h;
        if (sizes == null)
            return null;

        Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = h;

        // Try to find an size match aspect ratio and size
        for (Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE)
                continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        // Cannot find the one match the aspect ratio, ignore the requirement
        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;
    }

}
