/*
[현대자동차 블루링크TnB APP] version [1.0]

Copyright ⓒ [2014] kt corp. All rights reserved.

This is a proprietary software of kt corp, and you may not use this file except in compliance with license agreement with kt corp. 
Any redistribution or use of this software, with or without modification shall be strictly prohibited without prior written approval of kt corp, and the copyright notice above does not evidence any actual or intended publication of such software.
 */

package com.neighbor.ex.tong.network;

import android.os.Handler;
import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class HttpURLConnectionMultipart implements Runnable {

    private String urlTo;
    private String filepath;
    private String filefield;
    private String post;
    private Handler mHandler;
    private int IMAGE_SUCCESS;
    private int IMAGE_FAIL;
    private HttpURLConnection mClient = null;
    private String accessToken;

    public HttpURLConnectionMultipart(String urlTo,
                                      String filepath, String filefield, Handler handler,
                                      int imageSuccess, int imageFail) {
        this.urlTo = urlTo;
        this.filepath = filepath;
        this.filefield = filefield;
        this.post = post;
        this.mHandler = handler;
        this.IMAGE_SUCCESS = imageSuccess;
        this.IMAGE_FAIL = imageFail;
        this.accessToken = accessToken;
    }

    private String convertStreamToString(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }

    @Override
    public void run() {
        // TODO Auto-generated method stub
        mClient = null;
        DataOutputStream outputStream = null;
        InputStream inputStream = null;

        String twoHyphens = "--";
        String boundary = "*****" + Long.toString(System.currentTimeMillis())
                + "*****";
        String lineEnd = "\r\n";

        String result = "";

        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1 * 1024 * 1024;

        String[] q = filepath.split("/");
        int idx = q.length - 1;

        try {
            File file = new File(filepath);
            FileInputStream fileInputStream = new FileInputStream(file);


            mClient = openClient(urlTo);
            mClient.setDoInput(true);
            mClient.setDoOutput(true);
            mClient.setDefaultUseCaches(false);
            mClient.setConnectTimeout(30000);

//			mClient.setRequestProperty("accessToken", accessToken);
//			if (!AppPreferences.getCookies().isEmpty()) {
//				mClient.setRequestProperty("Cookie",
//						AppPreferences.getCookies());
//			}

            mClient.setRequestProperty("User-Agent",
                    "Android Multipart HTTP Client 1.0");
            mClient.setRequestProperty("Content-Type",
                    "multipart/form-data; boundary=" + boundary);

            outputStream = new DataOutputStream(mClient.getOutputStream());
//            outputStream.writeBytes(twoHyphens + boundary + lineEnd);
            outputStream.writeBytes("--" + boundary + "\r\n");
            outputStream.writeBytes("Content-Disposition: form-data; name=\"datafile\"; filename=\"" + q[idx] + "\"" + lineEnd);
            Log.d("hts", "q[idx] ~~~~~~~~~~~~~~~~~~~~~~~~ : " + q[idx]);
            outputStream.writeBytes("Content-Type: image/jpeg" + lineEnd);
//            outputStream.writeBytes("Content-Transfer-Encoding: binary"
//                    + lineEnd);
            outputStream.writeBytes(lineEnd);

            bytesAvailable = fileInputStream.available();
            bufferSize = Math.min(bytesAvailable, maxBufferSize);
            buffer = new byte[bufferSize];

            bytesRead = fileInputStream.read(buffer, 0, bufferSize);
            while (bytesRead > 0) {
                outputStream.write(buffer, 0, bufferSize);
                bytesAvailable = fileInputStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);
            }

            outputStream.writeBytes(lineEnd);

            // Upload POST Data
//			String[] posts = post.split("&");
//			int max = posts.length;
//			for (int i = 0; i < max; i++) {
//				outputStream.writeBytes(twoHyphens + boundary + lineEnd);
//				String[] kv = posts[i].split("=");
//				outputStream
//						.writeBytes("Content-Disposition: form-data; name=\""
//								+ kv[0] + "\"" + lineEnd);
//				outputStream.writeBytes("Content-Type: text/plain" + lineEnd);
//				outputStream.writeBytes(lineEnd);
//				outputStream.writeBytes(kv[1]);
//				outputStream.writeBytes(lineEnd);
//			}

            outputStream.writeBytes(twoHyphens + boundary + twoHyphens
                    + lineEnd);

            inputStream = mClient.getInputStream();

            result = this.convertStreamToString(inputStream);

            Log.d("hts", "result ~~~~~~~~~~~~~~~~~ : " + result);
            fileInputStream.close();
            inputStream.close();
            outputStream.flush();
            outputStream.close();

//			Gson gson = new GsonBuilder().setPrettyPrinting().create();
//			CommonErrCheck errCheck = gson.fromJson((String) result,
//					CommonErrCheck.class);
//
//			String errMsg = errCheck.getErrMsg();
//
//			if (errMsg != null && errMsg.equalsIgnoreCase("Success")) {
            mHandler.sendMessage(mHandler.obtainMessage(IMAGE_SUCCESS,
                    result));
//			} else {
//				mHandler.sendMessage(mHandler.obtainMessage(IMAGE_FAIL, errMsg));
//			}

        } catch (Exception e) {
            e.printStackTrace();
            mHandler.sendEmptyMessage(IMAGE_FAIL);
        } finally {
            disconnect();
        }
    }

    public void disconnect() {
        if (mClient != null) {
            mClient.disconnect();
            mClient = null;
        }
    }

    private HttpURLConnection openClient(String url) {
        URL httpUrl = null;
        HttpURLConnection client = null;

        try {
            httpUrl = new URL(url);
            disableConnectionReuseIfNecessary();

//			if (httpUrl.getProtocol().toLowerCase().equals("https")) {
//				trustAllHosts();
//				HttpsURLConnection https = (HttpsURLConnection) httpUrl
//						.openConnection();
//				https.setHostnameVerifier(DO_NOT_VERIFY);
//				client = https;
//			} else {
            client = (HttpURLConnection) httpUrl.openConnection();
//			}
            client.setRequestMethod("POST");
        } catch (IOException e) {
            e.printStackTrace();
            mHandler.sendMessage(mHandler.obtainMessage(IMAGE_FAIL));
            return null;
        }

        return client;
    }

    private void disableConnectionReuseIfNecessary() {
        System.setProperty("http.keepAlive", "false");
    }

    private void trustAllHosts() {
        // Create a trust manager that does not validate certificate chains
        TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return new java.security.cert.X509Certificate[]{};
            }

            @Override
            public void checkClientTrusted(
                    java.security.cert.X509Certificate[] chain, String authType)
                    throws java.security.cert.CertificateException {
            }

            @Override
            public void checkServerTrusted(
                    java.security.cert.X509Certificate[] chain, String authType)
                    throws java.security.cert.CertificateException {
            }
        }};

        // Install the all-trusting trust manager
        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection
                    .setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private final HostnameVerifier DO_NOT_VERIFY = new HostnameVerifier() {
        @Override
        public boolean verify(String arg0, SSLSession arg1) {
            mClient = openClient(urlTo);
            return true;
        }
    };

}
