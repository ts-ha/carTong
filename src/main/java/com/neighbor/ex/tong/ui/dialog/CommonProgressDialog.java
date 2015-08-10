package com.neighbor.ex.tong.ui.dialog;

import android.app.ProgressDialog;
import android.content.Context;

/**
 * Created by tsha on 2014-11-14.
 */
public class CommonProgressDialog {
    private static CommonProgressDialog instance;
    private static ProgressDialog mProgressDialog;

    private CommonProgressDialog(Context context) {

    }

    public static CommonProgressDialog getInstance(Context context) {
        if (instance == null) {
            instance = new CommonProgressDialog(context);
        }
        return instance;
    }


    public static void showProgressDialog(Context context) {
        if (mProgressDialog == null && context != null) {
            mProgressDialog = new ProgressDialog(context);
            mProgressDialog.setCanceledOnTouchOutside(false);
            mProgressDialog.setMessage("잠시만 기다려 주세요");
            mProgressDialog.show();
        }
    }

    public static synchronized void hideProgress() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }
}
