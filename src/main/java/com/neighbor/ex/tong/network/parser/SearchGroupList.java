package com.neighbor.ex.tong.network.parser;

import java.util.ArrayList;

/**
 * Created by ts.ha on 2015-08-06.
 */
public class SearchGroupList {
    String resultCode, resultMsg;
    ArrayList<Row> rows;

    public class Row {
        int RNUM;
        String SEQ, ROOM_ID, ROOM_MASTER_GMAIL, ROOM_MASTER_CAR_NUM, ROOM_NAME, USE_FLAG, REG_DATE, ROOM_DESC,
                MEMBER_COUNT;

        public int getRNUM() {
            return RNUM;
        }

        public String getSEQ() {
            return SEQ;
        }

        public String getROOM_ID() {
            return ROOM_ID;
        }

        public String getROOM_MASTER_GMAIL() {
            return ROOM_MASTER_GMAIL;
        }

        public String getROOM_MASTER_CAR_NUM() {
            return ROOM_MASTER_CAR_NUM;
        }

        public String getROOM_NAME() {
            return ROOM_NAME;
        }

        public String getUSE_FLAG() {
            return USE_FLAG;
        }

        public String getREG_DATE() {
            return REG_DATE;
        }

        public String getROOM_DESC() {
            return ROOM_DESC;
        }

        public String getMEMBER_COUNT() {
            return MEMBER_COUNT;
        }

        @Override
        public String toString() {
            return "Row{" +
                    "RNUM=" + RNUM +
                    ", SEQ='" + SEQ + '\'' +
                    ", ROOM_ID='" + ROOM_ID + '\'' +
                    ", ROOM_MASTER_GMAIL='" + ROOM_MASTER_GMAIL + '\'' +
                    ", ROOM_MASTER_CAR_NUM='" + ROOM_MASTER_CAR_NUM + '\'' +
                    ", ROOM_NAME='" + ROOM_NAME + '\'' +
                    ", USE_FLAG='" + USE_FLAG + '\'' +
                    ", REG_DATE='" + REG_DATE + '\'' +
                    ", ROOM_DESC='" + ROOM_DESC + '\'' +
                    ", MEMBER_COUNT='" + MEMBER_COUNT + '\'' +
                    '}';
        }
    }

    public String getResultCode() {
        return resultCode;
    }

    public String getResultMsg() {
        return resultMsg;
    }

    public ArrayList<Row> getRows() {
        return rows;
    }

    @Override
    public String toString() {
        return "SearchGroupList{" +
                "resultCode='" + resultCode + '\'' +
                ", resultMsg='" + resultMsg + '\'' +
                ", rows=" + rows +
                '}';
    }
}
