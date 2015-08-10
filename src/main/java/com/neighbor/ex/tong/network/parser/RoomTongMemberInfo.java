package com.neighbor.ex.tong.network.parser;

import java.util.ArrayList;

/**
 * Created by ts.ha on 2015-08-01.
 */
public class RoomTongMemberInfo {
    ArrayList<Rows> rows = new ArrayList<Rows>();

   public class Rows {
        String MEMBER_GMAIL, MEMBER_CAR_NUM, USE_FLAG;

        public String getMEMBER_GMAIL() {
            return MEMBER_GMAIL;
        }

        public void setMEMBER_GMAIL(String MEMBER_GMAIL) {
            this.MEMBER_GMAIL = MEMBER_GMAIL;
        }

        public String getMEMBER_CAR_NUM() {
            return MEMBER_CAR_NUM;
        }

        public void setMEMBER_CAR_NUM(String MEMBER_CAR_NUM) {
            this.MEMBER_CAR_NUM = MEMBER_CAR_NUM;
        }

        public String getUSE_FLAG() {
            return USE_FLAG;
        }

        public void setUSE_FLAG(String USE_FLAG) {
            this.USE_FLAG = USE_FLAG;
        }
    }

    public ArrayList<Rows> getRowses() {
        return rows;
    }

}
