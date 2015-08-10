package com.neighbor.ex.tong;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by User on 2015-07-15.
 */
public class Utils {

    public static String getJSON(String url, int timeout) {
        HttpURLConnection c = null;
        try {
            URL u = new URL(url);
            c = (HttpURLConnection) u.openConnection();
            c.setRequestMethod("GET");
            c.setRequestProperty("Content-length", "0");
            c.setUseCaches(false);
            c.setAllowUserInteraction(false);
            c.setConnectTimeout(timeout);
            c.setReadTimeout(timeout);
            c.connect();
            int status = c.getResponseCode();
            switch (status) {
                case 200:
                case 201:
                    BufferedReader br = new BufferedReader(new InputStreamReader(c.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        sb.append(line + "\n");
                    }
                    br.close();
                    return sb.toString();
            }
        } catch (MalformedURLException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if (c != null) {
                try {
                    c.disconnect();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
        return null;
    }

    public synchronized static int GetExifOrientation(String filepath) {
        int degree = 0;
        ExifInterface exif = null;

        try {
            exif = new ExifInterface(filepath);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (exif != null) {
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, -1);

            if (orientation != -1) {
                // We only recognize a subset of orientation tag values.
                switch (orientation) {
                    case ExifInterface.ORIENTATION_ROTATE_90:
                        degree = 90;
                        break;

                    case ExifInterface.ORIENTATION_ROTATE_180:
                        degree = 180;
                        break;

                    case ExifInterface.ORIENTATION_ROTATE_270:
                        degree = 270;
                        break;
                }

            }
        }

        return degree;
    }

    public synchronized static Bitmap GetRotatedBitmap(Bitmap bitmap, int degrees) {
        if (degrees != 0 && bitmap != null) {
            Matrix m = new Matrix();
            m.setRotate(degrees, (float) bitmap.getWidth() / 2, (float) bitmap.getHeight() / 2);
            try {
                Bitmap b2 = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, true);
                if (bitmap != b2) {
                    bitmap.recycle();
                    bitmap = b2;
                }
            } catch (OutOfMemoryError ex) {
                // We have no memory to rotate. Return the original bitmap.
            }
        }
        return bitmap;
    }

    public static String getDateTime() {
        Date currentDate = Calendar.getInstance().getTime();
        java.text.SimpleDateFormat simpleDateFormat =
                new java.text.SimpleDateFormat("yyyyMMddHHmmss");
        return simpleDateFormat.format(currentDate);
    }

    public static double calDistance(double lat1, double lon1, double lat2, double lon2) {

        double theta, dist;
        theta = lon1 - lon2;
        dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1))
                * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);

        dist = dist * 60 * 1.1515;
        dist = dist * 1.609344;    // 단위 mile 에서 km 변환.
        dist = dist * 1000.0;      // 단위  km 에서 m 로 변환

        return dist;
    }

    // 주어진 도(degree) 값을 라디언으로 변환
    private static double deg2rad(double deg) {
        return (double) (deg * Math.PI / (double) 180d);
    }

    // 주어진 라디언(radian) 값을 도(degree) 값으로 변환
    private static double rad2deg(double rad) {
        return (double) (rad * (double) 180d / Math.PI);
    }

    public static int getEmoticonPathByMessage(String message, Context context) {

        Resources resources = context.getResources();
        if (message.contains(resources.getString(R.string.msg_opendoor))) {
            return R.drawable.icon_tong_msg_opendoor;
        } else if (message.contains(resources.getString(R.string.msg_trunk))) {
            return R.drawable.icon_tong_msg_opentrunk;
        } else if (message.contains(resources.getString(R.string.msg_tir))) {
            return R.drawable.icon_tong_msg_puncture;
        } else if (message.contains(resources.getString(R.string.msg_lamp))) {
            return R.drawable.icon_tong_msg_troublelamp;
        } else if (message.contains(resources.getString(R.string.msg_drop))) {
            return R.drawable.icon_tong_msg_luggage;
        } else if (message.contains(resources.getString(R.string.msg_smoke))) {
            return R.drawable.icon_tong_msg_fire;
        } else if (message.contains(resources.getString(R.string.msg_box))) {
            return R.drawable.icon_tong_msg_box;
        } else if (message.contains(resources.getString(R.string.msg_roadline))) {
            return R.drawable.icon_tong_msg_roadline;
        } else if (message.contains(resources.getString(R.string.msg_accident))) {
            return R.drawable.icon_tong_msg_accident;
        } else if (message.contains(resources.getString(R.string.msg_baby))) {
            return R.drawable.icon_tong_msg_babyincar;
        } else if (message.contains(resources.getString(R.string.msg_help))) {
            return R.drawable.icon_tong_msg_help;
        } else if (message.contains(".*차량을 옮겨주세요.*")) {
            return R.drawable.icon_tong_msg_moving;
        } else if (message.contains(resources.getString(R.string.msg_drop))) {
            return R.drawable.icon_tong_msg_luggage;
        } else if (message.contains(resources.getString(R.string.msg_light))) {
            return R.drawable.icon_tong_msg_inlight;
        } else if (message.contains(resources.getString(R.string.msg_tir))) {
            return R.drawable.icon_tong_msg_puncture;
        } else if (message.contains(resources.getString(R.string.msg_parking))) {
            return R.drawable.icon_tong_msg_parking;
        } else if (message.contains(resources.getString(R.string.msg_safe))) {
            return R.drawable.icon_tong_msg_safedriving;
        } else if (message.contains(resources.getString(R.string.msg_landslid))) {
            return R.drawable.icon_tong_msg_landslide;
        }


        return 0;
    }

}
