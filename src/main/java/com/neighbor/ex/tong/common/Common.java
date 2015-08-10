package com.neighbor.ex.tong.common;

import android.content.ContentUris;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.ContactsContract.Contacts;

import java.io.InputStream;

public class Common {
    public static final String GCM_PROJECT_ID = "437587055114";

    public static final String PARSER_ID_SENSERINFO = "SenserInfo";
    public static final String PARSER_ID_EXERCISEINFO = "ExerciseInfo";
    // url
    private static final String SERVER_URL = "http://211.189.132.184:8080/";
    private static final String URL_BASE = SERVER_URL + "Tong/api/";

    public static final String REG_TRAFFICCENTE_RREPORT = "http://211.189.132.184:8080/Tong/regTrafficCenterReport.do?memberGmail=%s&comment=%s&commentType=%s&sender=%s&gpsLat=%s&gpsLong=%s";
    /**
     * 폰에서 운영자에게 제보하기
     */
    public static final String REGIST_ACCIDENT = URL_BASE + "RegistAccident.do?msg=%s&gmail=%s&gpsLong=%s&gpsLat=%s";

    /**
     * 내 그룹 수정
     */
    public static final String URL_UPDATE_TONG_INFO = "http://211.189.132.184:8080/Tong/api/updateTongInfo.do?roomId=%s&roomDesc=%s";


    // 메뉴
    public static final int MENU_HOME = 0;
    public static final int MENU_NOTICE = 1;
    public static final int MENU_LIFE_LOG = 2;
    public static final int MENU_ACTIVITY = 4;
    public static final int MENU_SLEEP = 3;
    //    public static final int MENU_GUARDIAN = 6;
    public static final int MENU_SYMPOTOM = 6;
    public static final int MENU_PRIVACY = 7;
    public static final int MENU_TELEMETERING = 8;

    // handle
    public static final int HANDLE_SUCCESS_LOGIN = 10000;
    public static final int HANDLE_SUCCESS_REGIST_ACCIDENT = HANDLE_SUCCESS_LOGIN + 1;
    public static final int HANDLE_FAIL_REGIST_ACCIDENT = HANDLE_SUCCESS_REGIST_ACCIDENT + 1;

    public static final int HANDLE_SUCCESS_GET_ROOM_TONG_MEMBERINFO = HANDLE_FAIL_REGIST_ACCIDENT + 1;
    public static final int HANDLE_FAIL_GET_ROOM_TONG_MEMBERINFO = HANDLE_SUCCESS_GET_ROOM_TONG_MEMBERINFO + 1;

    public static final int HANDLE_SUCCESS_JOIN_ACCEPT = HANDLE_FAIL_GET_ROOM_TONG_MEMBERINFO + 1;
    public static final int HANDLE_FAIL_JOIN_ACCEPT = HANDLE_SUCCESS_JOIN_ACCEPT + 1;





    /**
     * 그룹 목록
     */
    public static final String ROOM_ID = "ROOM_ID";
    public static final String ROOM_NAME = "ROOM_NAME";
    public static final String ROOM_OWNER = "ROOM_MASTER_GMAIL";
    public static final String ROOM_DESC = "ROOM_DESC";
    public static final String MEMBER_COUNT = "MEMBER_COUNT";
    public static final String REG_DATE = "REG_DATE";
    public static final String USE_FLAG = "USE_FLAG";
    /**
     * push noti 메시지
     */
    public static final String PUSH_NOIT = "pushNoit";
    public static final String ROOM_MASTER_CAR_NUM = "ROOM_MASTER_CAR_NUM";


    /**
     * 연락처 사진 id를 가지고 사진에 들어갈 bitmap을 생성.
     *
     * @param contactId 연락처 사진 ID
     * @return bitmap 연락처 사진
     */
    public static Bitmap openPhoto(Context context, long contactId) {
        Uri contactUri = ContentUris.withAppendedId(Contacts.CONTENT_URI,
                contactId);
        InputStream input = Contacts
                .openContactPhotoInputStream(context.getContentResolver(),
                        contactUri);
        if (input != null) {
            return BitmapFactory.decodeStream(input);
        }
        return null;
    }
}
