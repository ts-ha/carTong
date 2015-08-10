package com.neighbor.ex.tong.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.database.sqlite.SQLiteStatement;
import android.net.Uri;
import android.util.Log;

import com.neighbor.ex.tong.CONST;

import java.util.HashMap;

public class DataProvider extends ContentProvider {

    private static final int CODE_TONG_MESSAGE = 1;
    private static final int CODE_TONG_MESSAGE_ID = 2;

    private static final int CODE_TONG_EVENT = 3;
    private static final int CODE_TONG_PLATE = 4;

    private static final String MSG_URL = "content://" + CONST.CARTONG_AUTH + "/tongTable";
    private static final String EVENT_URL = "content://" + CONST.CARTONG_AUTH + "/eventTable";
    private static final String PLATE_URL = "content://" + CONST.CARTONG_AUTH + "/plateTable";

    public static final Uri TONG_URI = Uri.parse(MSG_URL);
    public static final Uri EVENT_URI = Uri.parse(EVENT_URL);
    public static final Uri PLATE_URI = Uri.parse(PLATE_URL);

    private static HashMap<String, String> TongMap;
    private static HashMap<String, String> EventMap;
    private static HashMap<String, String> PlateMap;

    public static final String MSG_TABLE = "tongTable";
    public static final String EVENT_TABLE = "eventTable";
    public static final String PLATE_TABLE = "plateTable";

    ContentHelper mDbhelper;
    SQLiteDatabase mDb;

    static final UriMatcher uriMatcher;

    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(CONST.CARTONG_AUTH, "tongTable", CODE_TONG_MESSAGE);
        uriMatcher.addURI(CONST.CARTONG_AUTH, "tongTable/#", CODE_TONG_MESSAGE_ID);
        uriMatcher.addURI(CONST.CARTONG_AUTH, "eventTable", CODE_TONG_EVENT);
        uriMatcher.addURI(CONST.CARTONG_AUTH, "plateTable", CODE_TONG_PLATE);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int cnt = 0;

        switch (uriMatcher.match(uri)) {
            case CODE_TONG_EVENT:
                cnt = mDb.delete(EVENT_TABLE, selection, selectionArgs);
                break;
            case CODE_TONG_PLATE:
                cnt = mDb.delete(PLATE_TABLE, selection, selectionArgs);
                break;
        }
        return cnt;
    }

    @Override
    public String getType(Uri uri) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        long row;
        Uri newUri = null;

        switch (uriMatcher.match(uri)) {
            case CODE_TONG_MESSAGE:
                row = mDb.insert(MSG_TABLE, "", values);
                if (row > 0) {
                    newUri = ContentUris.withAppendedId(TONG_URI, row);
                    getContext().getContentResolver().notifyChange(newUri, null);
                }
                break;
            case CODE_TONG_PLATE:
                row = mDb.insert(PLATE_TABLE, "", values);
                if (row > 0) {
                    newUri = ContentUris.withAppendedId(PLATE_URI, row);
                    getContext().getContentResolver().notifyChange(newUri, null);
                }
                break;

            default:
                throw new IllegalArgumentException("Unknown URI" + uri);
        }
        return newUri;
    }

    static String createInsert(final String tableName, final String[] columnNames) {
        if (tableName == null || columnNames == null || columnNames.length == 0) {
            throw new IllegalArgumentException();
        }
        final StringBuilder s = new StringBuilder();
        s.append("INSERT INTO ").append(tableName).append(" (");
        for (String column : columnNames) {
            s.append(column).append(" ,");
        }
        int length = s.length();
        s.delete(length - 2, length);
        s.append(") VALUES( ");
        for (int i = 0; i < columnNames.length; i++) {
            s.append(" ? ,");
        }
        length = s.length();
        s.delete(length - 2, length);
        s.append(")");
        return s.toString();
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {

        Log.d("DataProvider", "bulkInsert" + uri.toString());

        String insertQuery;

        SQLiteStatement statement;

        switch (uriMatcher.match(uri)) {

            case CODE_TONG_EVENT: {
                insertQuery = createInsert(EVENT_TABLE,
                        new String[]{
                                "TITLE",
                                "GPS_X",
                                "GPS_Y"});

                statement = mDb.compileStatement(insertQuery);
                mDb.beginTransaction();

                try {

                    for (ContentValues bean : values) {
                        statement.clearBindings();
                        statement
                                .bindString(1, (String) bean
                                        .getAsString("TITLE"));
                        statement.bindString(2, (String) bean
                                .getAsString("GPS_X"));
                        statement.bindString(3, (String) bean
                                .getAsString("GPS_Y"));
                        statement.execute();
                    }

                    mDb.setTransactionSuccessful();

                } finally {
                    mDb.endTransaction();
                    Log.d("DataProvider", "End insertion");
                    getContext().getContentResolver().notifyChange(uri, null);
                }
            }
            break;

            default:
                break;

        }

        return values.length;
    }

    @Override
    public boolean onCreate() {
        mDbhelper = new ContentHelper(getContext(), CONST.CARTONG_DB, null, CONST.CARTONG_DB_VER);
        mDb = mDbhelper.getWritableDatabase();
        if (mDb == null)
            return false;
        else
            return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

        switch (uriMatcher.match(uri)) {
            case CODE_TONG_MESSAGE:
                queryBuilder.setTables(MSG_TABLE);
                queryBuilder.setProjectionMap(TongMap);
                break;

            case CODE_TONG_EVENT:
                queryBuilder.setTables(EVENT_TABLE);
                queryBuilder.setProjectionMap(EventMap);
                break;

            case CODE_TONG_PLATE:
                queryBuilder.setTables(PLATE_TABLE);
                queryBuilder.setProjectionMap(PlateMap);
                break;

            default:
                throw new IllegalArgumentException("Unknown URI" + uri);
        }

        Cursor cursor = queryBuilder.query(mDb, projection, selection, selectionArgs,
                null, null, sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        int count = 0;
        switch (uriMatcher.match(uri)) {
            case CODE_TONG_MESSAGE:
                count = mDb.update(MSG_TABLE, values, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI" + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    class ContentHelper extends SQLiteOpenHelper {

        private static final String CREATE_MESSAGE = "create table tongTable (" +
                "_id Integer primary key autoincrement, CONTENTS TEXT, RECOMM_CNT INTEGER, " +
                "DoSend TEXT , RECEIVER TEXT, SENDER TEXT, TIME DATETIME, SEQ  TEXT);";

        private static final String CREATE_CARNUM = "create table plateTable (" +
                "_id Integer primary key autoincrement, ID TEXT, PLATE_NUMBER TEXT );";

        public ContentHelper(Context context, String name,
                             SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        @Override
        public void onCreate(SQLiteDatabase sqLiteDatabase) {
            sqLiteDatabase.execSQL(CREATE_MESSAGE);
            sqLiteDatabase.execSQL(CREATE_CARNUM);
        }

        @Override
        public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
            sqLiteDatabase.execSQL("DROP TABLE IF EXISTS tongTable");
            sqLiteDatabase.execSQL(CREATE_MESSAGE);
        }

        @Override
        public void onOpen(SQLiteDatabase db) {
            super.onOpen(db);
        }

        @Override
        public synchronized void close() {
            super.close();
        }
    }
}
