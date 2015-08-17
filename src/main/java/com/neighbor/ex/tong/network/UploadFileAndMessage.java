package com.neighbor.ex.tong.network;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

import com.neighbor.ex.tong.CONST;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLEncoder;



public class UploadFileAndMessage extends AsyncTask<Void, Void, Void> {
//    private final String UPLAOD_URL = "http://211.189.132.184:8080/Tong/pushMsgByCarNum.do?senderMemberCarNum=";
    private final String UPLAOD_URL = "http://61.97.129.99:9000/OCRCarNoDetect";

    private String          path;
    private String          message;
    private ProgressDialog mProgressDialog;
    private SharedPreferences prefs;
    private Context context;

    public UploadFileAndMessage(String filePath, String Msg , Context cont){
        this.path = filePath;
        this.message =  Msg;
        this.context = cont;
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    @Override
    protected Void doInBackground(Void... voids) {
        try {

            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
            builder.addTextBody("message", URLEncoder.encode(message, "UTF-8"),
                    ContentType.create("Multipart/related", "UTF-8"));
            builder.addPart("image", new FileBody(new File(path)));

            InputStream inputStream = null;
            HttpClient httpClient = AndroidHttpClient.newInstance("Android");

            String carNo = prefs.getString(CONST.ACCOUNT_LICENSE, "");
            String encodeCarNo="";

            if (false == carNo.equals("")) {
                encodeCarNo = URLEncoder.encode(carNo, "UTF-8");
            }
            HttpPost httpPost = new HttpPost(UPLAOD_URL+ encodeCarNo);
            httpPost.setEntity(builder.build());
            HttpResponse httpResponse = httpClient.execute(httpPost);
            HttpEntity httpEntity = httpResponse.getEntity();
            inputStream = httpEntity.getContent();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
            StringBuilder stringBuilder = new StringBuilder();
            String line = null;

            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line + "\n");
            }
            inputStream.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPreExecute() {
        mProgressDialog = new ProgressDialog(context);
        mProgressDialog.setMessage("전송 중 입니다.");
        mProgressDialog.show();
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        mProgressDialog.dismiss();
        super.onPostExecute(aVoid);
//        ((Activity)context).finish();
    }
}
