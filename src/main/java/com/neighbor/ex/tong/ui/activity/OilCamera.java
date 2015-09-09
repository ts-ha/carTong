/*
[현대자동차 블루링크TnB APP] version [1.0]

Copyright ⓒ [2014] kt corp. All rights reserved.

This is a proprietary software of kt corp, and you may not use this file except in compliance with license agreement with kt corp. 
Any redistribution or use of this software, with or without modification shall be strictly prohibited without prior written approval of kt corp, and the copyright notice above does not evidence any actual or intended publication of such software.
 */

package com.neighbor.ex.tong.ui.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.neighbor.ex.tong.R;
import com.neighbor.ex.tong.network.HttpURLConnectionMultipart;
import com.neighbor.ex.tong.ui.dialog.CommonProgressDialog;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.CookieManager;
import java.text.SimpleDateFormat;
import java.util.Date;


public class OilCamera extends Activity implements OnTouchListener {

    private final int SESSION_SUCCESS = 10001;
    private final int FAIL = 20001;
    private final int IMAGE_SUCCESS = 20004;
    private final int IMAGE_FAIL = 20005;

    private OilCameraPreview mPreview;
    // private PanAndZoomListener1 mZoomListener;

    private OrientationEventListener mOrientationEventListener;
    private int mOrientation = -1;

    private static final int ORIENTATION_PORTRAIT_NORMAL = 1;
    private static final int ORIENTATION_PORTRAIT_INVERTED = 2;
    private static final int ORIENTATION_LANDSCAPE_NORMAL = 3;
    private static final int ORIENTATION_LANDSCAPE_INVERTED = 4;

    private boolean mIsCaptureMode = false;

    //    private RelativeLayout mBackgroundLayout;
    private Bitmap mBitmap;
    private ImageView mImageView;
    private Button mCapureButton, mSaveButton;
    public CookieManager cookieManager;
    private Dialog showOilDialog;
    protected String accessToken;

    public static int mCurrentZoomLevel = 0, mMaxZoomLevel = 0;

    private final int CLICK = 0;
    private final int ZOOM = 2;
    private final int NONE = 3;
    private int mode = NONE;
    private float oldDist = 1f;
    private final float SPACE_ZOOM = 10f;

    private int mEndAnimationId;

    private String result;
    private String resultCarNo;
    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            super.handleMessage(msg);
            switch (msg.what) {
                case IMAGE_SUCCESS:
//                    UiProgress.hideProgress();
                    CommonProgressDialog.hideProgress();

                    XmlPullParserFactory factory = null;
                    try {
                        factory = XmlPullParserFactory.newInstance();

                        factory.setNamespaceAware(true);
                        XmlPullParser xpp = factory.newPullParser();

                        xpp.setInput(new StringReader((String) msg.obj));
                        int eventType = xpp.getEventType();
                        boolean isresultDesc = false;
                        boolean isCarNo = false;
                        while (eventType != XmlPullParser.END_DOCUMENT) {
                            if (eventType == XmlPullParser.START_DOCUMENT) {
                                System.out.println("Start document");
                            } else if (eventType == XmlPullParser.START_TAG) {
                                String tagName = xpp.getName();
                                if (tagName.equalsIgnoreCase("resultDesc")) {
                                    isresultDesc = true;
                                } else if (xpp.getName().equalsIgnoreCase("carNo")) {
                                    isCarNo = true;
                                }
                                System.out.println("Start tag " + xpp.getName());
                            } else if (eventType == XmlPullParser.END_TAG) {
                                System.out.println("End tag " + xpp.getName());
                                if (xpp.getName().equalsIgnoreCase("resultDesc")) {
                                    isresultDesc = false;
                                } else if (xpp.getName().equalsIgnoreCase("carNo")) {
                                    isCarNo = false;
                                }
                            } else if (eventType == XmlPullParser.TEXT) {
                                if (isresultDesc) {
                                    result = xpp.getText();
                                } else if (isCarNo) {
                                    Log.d("hts", " xpp.getText() : " + xpp.getText());
                                    resultCarNo = xpp.getText();
                                }
                            }
                            eventType = xpp.next();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (XmlPullParserException e) {
                        e.printStackTrace();
                    }
                    Intent intent = new Intent();
                    intent.putExtra("carNum", result);
//                    Log.d("hts", "resultCarNo L : "+ resultCarNo);
                    intent.putExtra("resultCarNo", resultCarNo);
                    OilCamera.this.setResult(RESULT_OK, intent);
                    OilCamera.this.finish();
                    File file = new File(imagePath);
                    file.delete();
                    break;

//                case IMAGE_SUCCESS:
////                    UiProgress.hideProgress();
//                    File file = new File(imagePath);
//                    file.delete();
//                    imagePath = "";
//                    showDialogUpload();
//                    break;

                case FAIL:
                    file = new File(imagePath);
                    file.delete();
//                    UiProgress.hideProgress();
//                    new BasicErrDialog(mContext, (String) msg.obj);
                    break;

                case IMAGE_FAIL:
//                    UiProgress.hideProgress();
//                    mSaveButton.setEnabled(true);
//                    new BasicErrDialog(mContext, (String) msg.obj);
                    break;
            }
        }

    };


    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera_preview);
        mContext = OilCamera.this;
        mPreview = (OilCameraPreview) findViewById(R.id.camera_preview);
        mImageView = (ImageView) findViewById(R.id.show_img);
        mCapureButton = (Button) findViewById(R.id.capture_btn);
        mSaveButton = (Button) findViewById(R.id.save_btn);
        mSaveButton.setEnabled(false);
//        mBackgroundLayout = (RelativeLayout) findViewById(R.id.camera_preview_parent);
        mCapureButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mSaveButton.setBackgroundResource(R.drawable.select_send);
                mCapureButton.setEnabled(false);
                if (!mIsCaptureMode) {
                    mPreview.mCamera.autoFocus(mAutoFocusCallback);
                } else {
                    mCurrentZoomLevel = 1;
                    mIsCaptureMode = !mIsCaptureMode;
                    showCapturedImage(mIsCaptureMode);
                }
            }
        });
        mSaveButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mSaveButton.setEnabled(false);
                showDialog();
            }
        });

        mPreview.setOnTouchListener(this);

        if (savedInstanceState != null) {
            mBitmap = savedInstanceState.getParcelable("mBitmap");
            mIsCaptureMode = savedInstanceState.getBoolean("mIsCaptureMode");
            imagePath = savedInstanceState.getString("imagePath");
            accessToken = savedInstanceState.getString("accessToken");
            showCapturedImage(mIsCaptureMode);
            mImageView.setImageBitmap(mBitmap);
        }

    }

    private void showDialog() {
        CommonProgressDialog.showProgressDialog(OilCamera.this);
        HttpURLConnectionMultipart multipartNetwork = new HttpURLConnectionMultipart(
                "http://61.97.129.99:9000/OCRCarNoDetect",
                imagePath, "FUEL_IMAGE", mHandler,
                IMAGE_SUCCESS, IMAGE_FAIL);
        new Thread(multipartNetwork).start();
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (mPreview.mCamera == null) {
            Toast.makeText(OilCamera.this, "카메라 권한이 없습니다.", Toast.LENGTH_SHORT).show();
            finish();
        }
        if (mOrientationEventListener == null) {
            mOrientationEventListener = new OrientationEventListener(this,
                    SensorManager.SENSOR_DELAY_NORMAL) {

                @Override
                public void onOrientationChanged(int orientation) {

                    if (orientation >= 315 || orientation < 45) {
                        if (mOrientation != ORIENTATION_PORTRAIT_NORMAL) {
                            mOrientation = ORIENTATION_PORTRAIT_NORMAL;
                        }
                    } else if (orientation < 315 && orientation >= 225) {
                        if (mOrientation != ORIENTATION_LANDSCAPE_NORMAL) {
                            mOrientation = ORIENTATION_LANDSCAPE_NORMAL;
                        }
                    } else if (orientation < 225 && orientation >= 135) {
                        if (mOrientation != ORIENTATION_PORTRAIT_INVERTED) {
                            mOrientation = ORIENTATION_PORTRAIT_INVERTED;
                        }
                    } else { // orientation <135 && orientation > 45
                        if (mOrientation != ORIENTATION_LANDSCAPE_INVERTED) {
                            mOrientation = ORIENTATION_LANDSCAPE_INVERTED;
                        }
                    }
                }
            };
        }
        if (mOrientationEventListener.canDetectOrientation()) {
            mOrientationEventListener.enable();
        }

        if (mIsCaptureMode) {
            mImageView.setVisibility(View.VISIBLE);
//            mBackgroundLayout.setVisibility(View.VISIBLE);
            // mPreview.mCamera.stopPreview();
            mPreview.setVisibility(View.GONE);
            mImageView.setImageBitmap(mBitmap);
        }
    }


    @Override
    public void finish() {
        super.finish();
        this.overridePendingTransition(0, mEndAnimationId);
        if (mPreview != null) {
            if (mPreview.mCamera != null) {
                mPreview.mCamera.stopPreview();
                mPreview.mCamera.release();
                mPreview.mCamera = null;
            }
        }
        if (mOrientationEventListener != null) {
            mOrientationEventListener.disable();
        }

    }

    private void showCapturedImage(boolean mode) {
        if (mIsCaptureMode) {
            // mImageView.setVisibility(View.VISIBLE);
//            mBackgroundLayout.setVisibility(View.VISIBLE);
            mPreview.mCamera.stopPreview();
            // mPreview.setVisibility(View.GONE);

            mSaveButton.setEnabled(true);
            // mSaveButton.setPressed(true);
            mSaveButton
                    .setBackgroundResource(R.drawable.select_send);
        } else {
            if (imagePath != null && imagePath.length() > 1) {
                File file = new File(imagePath);
                file.delete();
                imagePath = "";
            }
            mPreview.setVisibility(View.VISIBLE);
            mImageView.setVisibility(View.GONE);
            mPreview.mCamera.startPreview();
//            mBackgroundLayout.setVisibility(View.GONE);

            mSaveButton.setEnabled(false);
            mSaveButton.setBackgroundResource(R.drawable.btn_send);
        }
        mCapureButton.setEnabled(true);
    }

    Camera.AutoFocusCallback mAutoFocusCallback = new Camera.AutoFocusCallback() {
        @Override
        public void onAutoFocus(boolean success, Camera camera) {
            // if (success) {
            mIsCaptureMode = !mIsCaptureMode;
            mPreview.mCamera.takePicture(mShutterCallback, mPictureCallbackRaw,
                    mPictureCallbackJpeg);
            // }
        }
    };

    Camera.PictureCallback mPictureCallbackRaw = new Camera.PictureCallback() {
        public void onPictureTaken(byte[] data, Camera c) {
//			Log.i("SseOk", "mPictureCallbackRaw");
        }
    };
    protected String imagePath;

    @SuppressLint("SimpleDateFormat")
    Camera.PictureCallback mPictureCallbackJpeg = new Camera.PictureCallback() {
        public void onPictureTaken(byte[] data, Camera c) {
            // 처리중 다이얼로그 show

//            UiProgress.showProgress(mContext, R.string.progress_wait);

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = getImageCompressSize(data.length);
//            options.inSampleSize = 4;

            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length,
                    options);

            int exifDegree = exifOrientationToDegrees(mOrientation);
            bitmap = rotate(bitmap, exifDegree);

            mBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
                    bitmap.getHeight());

            File fileCacheItem = new File(Environment
                    .getExternalStorageDirectory().getAbsolutePath());
            OutputStream out = null;

            if (!fileCacheItem.exists()) {
                fileCacheItem.mkdirs();
            }

            try {
                fileCacheItem.createNewFile();
                Date date = new Date(System.currentTimeMillis());
                SimpleDateFormat sdf = new SimpleDateFormat(
                        "yyyy-MM-dd-hh-mm-ss");
                String filename = sdf.format(date);
                // Integer.toString(date.getDate()) + "/test.jpg");
                imagePath = fileCacheItem + "/" + filename + ".jpg";
                out = new FileOutputStream(imagePath);
                mBitmap.compress(CompressFormat.JPEG, 100, out);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            showCapturedImage(mIsCaptureMode);

//            UiProgress.hideProgress();
        }
    };

    public int exifOrientationToDegrees(int orientation) {
        int returnValue = 0;
        switch (orientation) {
            case ORIENTATION_PORTRAIT_NORMAL:
                returnValue = 90;
//			Log.v("CameraActivity", "Orientation = 90");
                break;
            case ORIENTATION_LANDSCAPE_NORMAL:
//			Log.v("CameraActivity", "Orientation = 0");
                break;
            case ORIENTATION_PORTRAIT_INVERTED:
                returnValue = 270;
//			Log.v("CameraActivity", "Orientation = 270");
                break;
            case ORIENTATION_LANDSCAPE_INVERTED:
                returnValue = 180;
//			Log.v("CameraActivity", "Orientation = 180");
                break;
        }

        return returnValue;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelable("mBitmap", mBitmap);
        outState.putBoolean("mIsCaptureMode", mIsCaptureMode);
        outState.putString("imagePath", imagePath);
        outState.putString("accessToken", accessToken);

        super.onSaveInstanceState(outState);
    }

    public Bitmap rotate(Bitmap bitmap, int degrees) {
        if (degrees != 0 && bitmap != null) {
            Matrix m = new Matrix();
            m.setRotate(degrees, (float) bitmap.getWidth() / 2,
                    (float) bitmap.getHeight() / 2);

            Bitmap converted = Bitmap.createBitmap(bitmap, 0, 0,
                    bitmap.getWidth(), bitmap.getHeight(), m, true);
            return converted;
        }
        return bitmap;
    }

    Camera.ShutterCallback mShutterCallback = new Camera.ShutterCallback() {
        public void onShutter() {
        }
    };

    private int getImageCompressSize(int len) {
//		Log.i("SseOk", "getImageCompressSize :: " + len);

        if (len <= 0)
            return -1;

        /**
         * 사진 용량 기준 1메가, 2메가, 3메가, 3메가 이상으로 처리
         */
        if (len > 0 && len <= 512384)
            return 1;
        if (len > 512384 && len <= 1024768)
            return 2;
        else if (len > 1024768 && len <= 2049536)
            return 4;
        else if (len > 2049536 && len <= 3074304)
            return 8;
        else
            return 10;

    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
//		Log.i("SseOk", "onTouch");
        if (mPreview == null) {
            return false;

        }
        Camera.Parameters parameters = mPreview.mCamera.getParameters();
        if (OilCamera.mMaxZoomLevel == 0) {
            OilCamera.mMaxZoomLevel = parameters.getMaxZoom();
        }

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
//			Log.i("SseOk", "ACTION_DOWN");
                mode = CLICK;
                break;

            case MotionEvent.ACTION_POINTER_DOWN:
                oldDist = spacing(event.getX(0), event.getY(0), event.getX(1),
                        event.getY(1));
                if (oldDist < SPACE_ZOOM) {
                    mode = NONE;
                } else {
                    mode = ZOOM;
                }
                break;

            case MotionEvent.ACTION_POINTER_UP:
//			Log.i("SseOk", "ACTION_POINTER_UP");
                mode = NONE;
                break;

            case MotionEvent.ACTION_UP:
//			Log.i("SseOk", "ACTION_UP");
                mode = NONE;
                break;

            case MotionEvent.ACTION_MOVE:
//			Log.i("SseOk", "ACTION_MOVE");
                if (mode == ZOOM) {
                    float newDist = spacing(event.getX(0), event.getY(0),
                            event.getX(1), event.getY(1));

                    float temp = oldDist - newDist;

                    if (newDist >= SPACE_ZOOM) {
                        if (temp < 0
                                && OilCamera.mCurrentZoomLevel < OilCamera.mMaxZoomLevel) {
                            OilCamera.mCurrentZoomLevel += 1;
                        } else if (temp >= 0 && OilCamera.mCurrentZoomLevel > 0) {
                            OilCamera.mCurrentZoomLevel -= 1;
                        }

                        parameters.setZoom(OilCamera.mCurrentZoomLevel);
                        mPreview.mCamera.setParameters(parameters);
                    }
                }
                break;
        }
        return true;
    }

    private float spacing(float x0, float y0, float x1, float y1) {
        float x = x0 - x1;
        float y = y0 - y1;
        return (float) Math.sqrt(x * x + y * y);
    }

}
