package com.labelprinter.android.DBManager;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.labelprinter.android.Models.PrinterInfo;
import com.labelprinter.android.Models.TicketInfo;
import com.labelprinter.android.Models.TicketModel;
import com.labelprinter.android.Models.TicketType;
import com.labelprinter.android.Models.User;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import static com.labelprinter.android.Common.Common.cm;

/**
 * Queryを管理するクラス
 */

public class Queries {

    /** SQLiteDatabase object */
    private SQLiteDatabase db;

    /** DbHelper object */
    private DbHelper dbHelper;

    /**
     *
     * Construct Function
     *
     * @param db Database object
     * @param dbHelper DbHelper object
     */
    public Queries(SQLiteDatabase db, DbHelper dbHelper) {
        this.db = db;
        this.dbHelper = dbHelper;
    }

    public boolean getDBInfo() {
        User user = null;
        db = dbHelper.getReadableDatabase();
        Cursor mCursor = db.rawQuery("select * from mst_user", null);
        mCursor.moveToFirst();
        if (!mCursor.isAfterLast()) {
            do {
                user = formatUser(mCursor);
            } while (mCursor.moveToNext());
        }
        mCursor.close();
        if (user==null)
            initUserInfo();
        return true;
//        return user==null?false:true;
    }

    public User getUserInfo(String userId, String pass) {
        User user = null;

        db = dbHelper.getReadableDatabase();
        Cursor mCursor = db.rawQuery("select * from mst_user where userid='" + userId + "' and password='" + pass + "'", null);
        mCursor.moveToFirst();
        if (!mCursor.isAfterLast()) {
            do {
                user = formatUser(mCursor);
            } while (mCursor.moveToNext());
        }
        mCursor.close();

        return user;
    }

    public void addUserInfo(User user) {
        db = dbHelper.getWritableDatabase();
        User isExist = getUserInfo(user.getId(), user.getPassword());
        ContentValues values = new ContentValues();
        values.put("userid", user.getId());
        values.put("username", user.getName());
        values.put("password", user.getPassword());
        values.put("kengengrp", "2");
        if (isExist != null) {
            db.update("mst_user", values, "userid = '" + user.getId() +
                    "' and password = '" + user.getPassword() + "'", null);
        }else {
            db.insert("mst_user", null, values);
        }
    }

    public ArrayList<TicketModel> getTicketModels() {
        ArrayList<TicketModel> list = new ArrayList<>();

        db = dbHelper.getReadableDatabase();
        Cursor mCursor = db.rawQuery("select * from mst_ticket", null);
        mCursor.moveToFirst();
        if (!mCursor.isAfterLast()) {
            do {
                TicketModel model = formatTicketModel(mCursor);
                list.add(model);
            } while (mCursor.moveToNext());
        }
        mCursor.close();

        return list;
    }

    public ArrayList<PrinterInfo> getPrinterInfos() {
        ArrayList<PrinterInfo> list = new ArrayList<>();

        db = dbHelper.getReadableDatabase();
        Cursor mCursor = db.rawQuery("select * from mst_ticketstyle", null);
        mCursor.moveToFirst();
        if (!mCursor.isAfterLast()) {
            do {
                PrinterInfo model = formatPrinterInfo(mCursor);
                list.add(model);
            } while (mCursor.moveToNext());
        }
        mCursor.close();

        return list;
    }

    public ArrayList<TicketType> getTicketTypes() {
        ArrayList<TicketType> list = new ArrayList<>();

        db = dbHelper.getReadableDatabase();
        Cursor mCursor = db.rawQuery("select * from mst_ui_tab", null);
        mCursor.moveToFirst();
        if (!mCursor.isAfterLast()) {
            do {
                TicketType model = formatTicketType(mCursor);
                list.add(model);
            } while (mCursor.moveToNext());
        }
        mCursor.close();

        return list;
    }

    public HashMap getDeviceInfo() {
        HashMap map = null;
        db = dbHelper.getReadableDatabase();
        Cursor mCursor = db.rawQuery("select * from mst_tanmatsu", null);
        mCursor.moveToFirst();
        if (!mCursor.isAfterLast()) {
            do {
                map = new HashMap();
                map.put("tanmatsumei", mCursor.getString(mCursor.getColumnIndex("tanmatsumei")));
                map.put("tanmatsuno", mCursor.getInt(mCursor.getColumnIndex("tanmatsuno")));
                map.put("hanbaibasho", mCursor.getString(mCursor.getColumnIndex("hanbaibasho")));
                map.put("sakuseiuserid", mCursor.getString(mCursor.getColumnIndex("sakuseiuserid")));
                map.put("sakuseinichiji", mCursor.getDouble(mCursor.getColumnIndex("sakuseinichiji")));
                map.put("koshinuserid", mCursor.getString(mCursor.getColumnIndex("koshinuserid")));
                map.put("koshinnichiji", mCursor.getDouble(mCursor.getColumnIndex("koshinnichiji")));
            } while (mCursor.moveToNext());
        }
        mCursor.close();
        return map;
    }

    public ArrayList<String> getSentionWhitName(String kbtype) {
        ArrayList<String> list = new ArrayList<>();
        db = dbHelper.getReadableDatabase();
        Cursor mCursor = db.rawQuery("select * from mst_kbn where kbtype = '" + kbtype + "'", null);
        mCursor.moveToFirst();
        if (!mCursor.isAfterLast()) {
            do {
                String value = mCursor.getString(mCursor.getColumnIndex("kbcd"));
                list.add(value);
            } while (mCursor.moveToNext());
        }
        mCursor.close();
        return list;
    }

    public int getEndNumberWithSection(String sectionName) {
        int num = 0;
        db = dbHelper.getReadableDatabase();
        Cursor mCursor = db.rawQuery("select * from mst_counter where saibankb = '" + sectionName + "' and genzaino = (SELECT max(genzaino) FROM mst_counter)", null);
        mCursor.moveToFirst();
        if (!mCursor.isAfterLast()) {
            do {
                num = mCursor.getInt(mCursor.getColumnIndex("genzaino"));
            } while (mCursor.moveToNext());
        }
        mCursor.close();
        return num;
    }

    public int addNumberWithSection(String sectionName) {
        int num = getEndNumberWithSection(sectionName) + 1;
        db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("saibankb", sectionName);
        HashMap map = getDeviceInfo();
        if (map != null) {
            values.put("saibancd", String.valueOf(map.get("tanmatsumei")));
        }else {
            values.put("saibancd", "Android");
        }
        values.put("genzaino", num);
        values.put("sakuseiuserid", cm.me.getId());
        values.put("sakuseinichiji", cm.convertToMilisecondsFromDate(new Date()));
        values.put("koshinuserid", cm.me.getId());
        values.put("koshinnichiji", cm.convertToMilisecondsFromDate(new Date()));
        db.insert("mst_counter", null, values);
        return num;
    }

    public ArrayList<HashMap> getNumberData(Calendar calendar) {
        ArrayList<HashMap> list = new ArrayList<>();
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        try {
            Date c_date = dateFormatter.parse(calendar.get(Calendar.YEAR)+"-"+(calendar.get(Calendar.MONTH)+1)+"-"+calendar.get(Calendar.DAY_OF_MONTH)+" 00:00");
            calendar.setTime(c_date);
            long startTime = calendar.getTimeInMillis();
            c_date = dateFormatter.parse(calendar.get(Calendar.YEAR)+"-"+(calendar.get(Calendar.MONTH)+1)+"-"+calendar.get(Calendar.DAY_OF_MONTH)+" 23:59");
            calendar.setTime(c_date);
            long endTime = calendar.getTimeInMillis();

            db = dbHelper.getWritableDatabase();
            Cursor mCursor = db.rawQuery("select * from mst_counter where koshinnichiji >= " + startTime + " and koshinnichiji <= " + endTime, null);
            mCursor.moveToFirst();
            if (!mCursor.isAfterLast()) {
                do {
                    HashMap map = new HashMap();
                    map.put("saibankb", mCursor.getString(mCursor.getColumnIndex("saibankb")));
                    map.put("saibancd", mCursor.getString(mCursor.getColumnIndex("saibancd")));
                    map.put("genzaino", mCursor.getInt(mCursor.getColumnIndex("genzaino")));
                    map.put("sakuseiuserid", mCursor.getString(mCursor.getColumnIndex("sakuseiuserid")));
                    map.put("sakuseinichiji", cm.converToDateTimeFormatFromTime((long) mCursor.getDouble(mCursor.getColumnIndex("sakuseinichiji"))));
                    map.put("koshinuserid", mCursor.getString(mCursor.getColumnIndex("koshinuserid")));
                    map.put("koshinnichiji", cm.converToDateTimeFormatFromTime((long) mCursor.getDouble(mCursor.getColumnIndex("koshinnichiji"))));
                    list.add(map);
                } while (mCursor.moveToNext());
            }
            mCursor.close();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return list;
    }

    public void addSellInfoWithData(ArrayList<TicketInfo> list, int payType) {
        if (list.size() > 0) {
            db = dbHelper.getWritableDatabase();
            int ind = 0;
            for (TicketInfo info : list) {
                ContentValues values = new ContentValues();
                int num = addNumberWithSection("URIAGE");
                values.put("uriageno", num);
                values.put("gyono", ind);
                HashMap map = getDeviceInfo();
                if (map != null) {
                    values.put("tanmatsumei", String.valueOf(map.get("tanmatsumei")));
                    values.put("hanbaibasho", String.valueOf(map.get("hanbaibasho")));
                }else {
                    values.put("tanmatsumei", "Android");
                    values.put("hanbaibasho", "センターハウス2F発券場所1");
                }
                values.put("uriagenichiji", cm.convertToMilisecondsFromDate(new Date()));
                values.put("ticketid", info.getModel().getId());
                values.put("uriagesuryo", info.getNum());
                values.put("hanbaitanka", info.getModel().getPrice());
                values.put("hanbaikingaku", info.getNum() * info.getModel().getPrice());
                values.put("shohizeiritsu", info.getModel().getTaxRatio());
                values.put("shohizeigaku", info.getModel().getTax());
                values.put("tickettypecd", info.getType().getName());
                values.put("meisho", info.getModel().getName());
                if (payType == 5) {
                    values.put("haraimodoshikb", 1);
                    values.put("uriagekb", String.valueOf(payType));
                }else {
                    values.put("haraimodoshikb", 0);
                    values.put("uriagekb", String.valueOf(payType));
                }
                values.put("shimeno", "0");
                values.put("sakuseiuserid", cm.me.getId());
                values.put("sakuseinichiji", cm.convertToMilisecondsFromDate(new Date()));
                values.put("koshinuserid", cm.me.getId());
                values.put("koshinnichiji", cm.convertToMilisecondsFromDate(new Date()));
                db.insert("dat_record", null, values);
                ind ++;
            }
        }
    }

    public ArrayList<HashMap> getSellData(Calendar calendar) {
        ArrayList<HashMap> list = new ArrayList<>();
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        try {
            Date c_date = dateFormatter.parse(calendar.get(Calendar.YEAR)+"-"+(calendar.get(Calendar.MONTH)+1)+"-"+calendar.get(Calendar.DAY_OF_MONTH)+" 00:00");
            calendar.setTime(c_date);
            long startTime = calendar.getTimeInMillis();
            c_date = dateFormatter.parse(calendar.get(Calendar.YEAR)+"-"+(calendar.get(Calendar.MONTH)+1)+"-"+calendar.get(Calendar.DAY_OF_MONTH)+" 23:59");
            calendar.setTime(c_date);
            long endTime = calendar.getTimeInMillis();

            db = dbHelper.getWritableDatabase();
            Cursor mCursor = db.rawQuery("select * from dat_record where koshinnichiji >= " + startTime + " and koshinnichiji <= " + endTime, null);
            mCursor.moveToFirst();
            if (!mCursor.isAfterLast()) {
                do {
                    HashMap map = new HashMap();
                    map.put("uriageno", mCursor.getInt(mCursor.getColumnIndex("uriageno")));
                    map.put("gyono", mCursor.getInt(mCursor.getColumnIndex("gyono")));
                    map.put("tanmatsumei", mCursor.getString(mCursor.getColumnIndex("tanmatsumei")));
                    map.put("hanbaibasho", mCursor.getString(mCursor.getColumnIndex("hanbaibasho")));
                    map.put("uriagenichiji", cm.converToDateTimeFormatFromTime((long) mCursor.getDouble(mCursor.getColumnIndex("uriagenichiji"))));
                    map.put("ticketid", mCursor.getInt(mCursor.getColumnIndex("ticketid")));
                    map.put("uriagesuryo", mCursor.getInt(mCursor.getColumnIndex("uriagesuryo")));
                    map.put("hanbaitanka", mCursor.getInt(mCursor.getColumnIndex("hanbaitanka")));
                    map.put("hanbaikingaku", mCursor.getInt(mCursor.getColumnIndex("hanbaikingaku")));
                    map.put("shohizeiritsu", mCursor.getString(mCursor.getColumnIndex("shohizeiritsu")));
                    map.put("shohizeigaku", mCursor.getInt(mCursor.getColumnIndex("shohizeigaku")));
                    map.put("tickettypecd", mCursor.getString(mCursor.getColumnIndex("tickettypecd")));
                    map.put("meisho", mCursor.getString(mCursor.getColumnIndex("meisho")));
                    map.put("haraimodoshikb", mCursor.getString(mCursor.getColumnIndex("haraimodoshikb")));
                    map.put("uriagekb", mCursor.getInt(mCursor.getColumnIndex("uriagekb")));
                    map.put("shimeno", mCursor.getInt(mCursor.getColumnIndex("shimeno")));
                    map.put("sakuseiuserid", mCursor.getString(mCursor.getColumnIndex("sakuseiuserid")));
                    map.put("sakuseinichiji", cm.converToDateTimeFormatFromTime((long) mCursor.getDouble(mCursor.getColumnIndex("sakuseinichiji"))));
                    map.put("koshinuserid", mCursor.getString(mCursor.getColumnIndex("koshinuserid")));
                    map.put("koshinnichiji", cm.converToDateTimeFormatFromTime((long) mCursor.getDouble(mCursor.getColumnIndex("koshinnichiji"))));
                    list.add(map);
                } while (mCursor.moveToNext());
            }
            mCursor.close();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return list;
    }

    public ArrayList<HashMap> getSellDataByGroup(Calendar calendar) {
        ArrayList<HashMap> list = new ArrayList<>();
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        try {
            Date c_date = dateFormatter.parse(calendar.get(Calendar.YEAR)+"-"+(calendar.get(Calendar.MONTH)+1)+"-"+calendar.get(Calendar.DAY_OF_MONTH)+" 00:00");
            calendar.setTime(c_date);
            long startTime = calendar.getTimeInMillis();
            c_date = dateFormatter.parse(calendar.get(Calendar.YEAR)+"-"+(calendar.get(Calendar.MONTH)+1)+"-"+calendar.get(Calendar.DAY_OF_MONTH)+" 23:59");
            calendar.setTime(c_date);
            long endTime = calendar.getTimeInMillis();

            db = dbHelper.getWritableDatabase();
            Cursor mCursor = db.rawQuery("select ticketid, meisho, hanbaitanka, haraimodoshikb, SUM(uriagesuryo) uriagesuryo from dat_record where koshinnichiji >= " +
                    startTime + " and koshinnichiji <= " + endTime + " group by meisho, haraimodoshikb order by ticketid", null);
            mCursor.moveToFirst();
            if (!mCursor.isAfterLast()) {
                do {
                    HashMap map = new HashMap();
                    map.put("ticketid", mCursor.getInt(mCursor.getColumnIndex("ticketid")));
                    map.put("meisho", mCursor.getString(mCursor.getColumnIndex("meisho")));
                    map.put("hanbaitanka", mCursor.getInt(mCursor.getColumnIndex("hanbaitanka")));
                    map.put("haraimodoshikb", mCursor.getString(mCursor.getColumnIndex("haraimodoshikb")));
                    map.put("uriagesuryo", mCursor.getInt(mCursor.getColumnIndex("uriagesuryo")));
                    list.add(map);
                } while (mCursor.moveToNext());
            }
            mCursor.close();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return list;
    }

    public void addInvoiceInfoWithData(int invoiceMoney, String onlyStr, int payType) {
        db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        int num = addNumberWithSection("RYOSHUSHO");
        values.put("ryoshuno", num);
        HashMap map = getDeviceInfo();
        if (map != null) {
            values.put("tanmatsumei", String.valueOf(map.get("tanmatsumei")));
            values.put("hanbaibasho", String.valueOf(map.get("hanbaibasho")));
        }else {
            values.put("tanmatsumei", "Android");
            values.put("hanbaibasho", "センターハウス2F発券場所1");
        }
        values.put("hakkonichiji", cm.convertToMilisecondsFromDate(new Date()));
        values.put("ryoshukingaku", invoiceMoney);
        values.put("tadashigaki", onlyStr);
        values.put("uriageno", payType);
        values.put("sakuseiuserid", cm.me.getId());
        values.put("sakuseinichiji", cm.convertToMilisecondsFromDate(new Date()));
        values.put("koshinuserid", cm.me.getId());
        values.put("koshinnichiji", cm.convertToMilisecondsFromDate(new Date()));
        db.insert("dat_ryoshu", null, values);
    }

    public ArrayList<HashMap> getInvoiceData(Calendar calendar) {
        ArrayList<HashMap> list = new ArrayList<>();
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        try {
            Date c_date = dateFormatter.parse(calendar.get(Calendar.YEAR)+"-"+(calendar.get(Calendar.MONTH)+1)+"-"+calendar.get(Calendar.DAY_OF_MONTH)+" 00:00");
            calendar.setTime(c_date);
            long startTime = calendar.getTimeInMillis();
            c_date = dateFormatter.parse(calendar.get(Calendar.YEAR)+"-"+(calendar.get(Calendar.MONTH)+1)+"-"+calendar.get(Calendar.DAY_OF_MONTH)+" 23:59");
            calendar.setTime(c_date);
            long endTime = calendar.getTimeInMillis();

            db = dbHelper.getWritableDatabase();
            Cursor mCursor = db.rawQuery("select * from dat_ryoshu where koshinnichiji >= " + startTime + " and koshinnichiji <= " + endTime, null);
            mCursor.moveToFirst();
            if (!mCursor.isAfterLast()) {
                do {
                    HashMap map = new HashMap();
                    map.put("ryoshuno", mCursor.getInt(mCursor.getColumnIndex("ryoshuno")));
                    map.put("tanmatsumei", mCursor.getString(mCursor.getColumnIndex("tanmatsumei")));
                    map.put("hanbaibasho", mCursor.getString(mCursor.getColumnIndex("hanbaibasho")));
                    map.put("hakkonichiji", cm.converToDateTimeFormatFromTime((long) mCursor.getDouble(mCursor.getColumnIndex("hakkonichiji"))));
                    map.put("ryoshukingaku", mCursor.getInt(mCursor.getColumnIndex("ryoshukingaku")));
                    map.put("tadashigaki", mCursor.getString(mCursor.getColumnIndex("tadashigaki")));
                    map.put("uriageno", mCursor.getInt(mCursor.getColumnIndex("uriageno")));

                    map.put("sakuseiuserid", mCursor.getString(mCursor.getColumnIndex("sakuseiuserid")));
                    map.put("sakuseinichiji", cm.converToDateTimeFormatFromTime((long) mCursor.getDouble(mCursor.getColumnIndex("sakuseinichiji"))));
                    map.put("koshinuserid", mCursor.getString(mCursor.getColumnIndex("koshinuserid")));
                    map.put("koshinnichiji", cm.converToDateTimeFormatFromTime((long) mCursor.getDouble(mCursor.getColumnIndex("koshinnichiji"))));
                    list.add(map);
                } while (mCursor.moveToNext());
            }
            mCursor.close();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return list;
    }

    public void addSettlementInfoWithData(int payType, int refundType, int totalNum, int totalValue, int totalTax) {
        db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        int num = addNumberWithSection("SEISAN");
        values.put("shimeno", num);
        HashMap map = getDeviceInfo();
        if (map != null) {
            values.put("tanmatsumei", String.valueOf(map.get("tanmatsumei")));
            values.put("hanbaibasho", String.valueOf(map.get("hanbaibasho")));
        }else {
            values.put("tanmatsumei", "Android");
            values.put("hanbaibasho", "センターハウス2F発券場所1");
        }
        values.put("shimebi", cm.convertToMilisecondsFromDate(new Date()));
        values.put("uriagekb", payType);
        values.put("haraimodoshikb", refundType);
        if (refundType == 1) {
            values.put("suryo", totalNum * (-1));
            values.put("kingaku", totalValue * (-1));
            values.put("shohizei", totalTax * (-1));
        }else {
            values.put("suryo", totalNum);
            values.put("kingaku", totalValue);
            values.put("shohizei", totalTax);
        }
        values.put("sakuseiuserid", cm.me.getId());
        values.put("sakuseinichiji", cm.convertToMilisecondsFromDate(new Date()));
        values.put("koshinuserid", cm.me.getId());
        values.put("koshinnichiji", cm.convertToMilisecondsFromDate(new Date()));
        db.insert("dat_expense", null, values);
    }

    public ArrayList<HashMap> getSettlementData(Calendar calendar) {
        ArrayList<HashMap> list = new ArrayList<>();
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        try {
            Date c_date = dateFormatter.parse(calendar.get(Calendar.YEAR)+"-"+(calendar.get(Calendar.MONTH)+1)+"-"+calendar.get(Calendar.DAY_OF_MONTH)+" 00:00");
            calendar.setTime(c_date);
            long startTime = calendar.getTimeInMillis();
            c_date = dateFormatter.parse(calendar.get(Calendar.YEAR)+"-"+(calendar.get(Calendar.MONTH)+1)+"-"+calendar.get(Calendar.DAY_OF_MONTH)+" 23:59");
            calendar.setTime(c_date);
            long endTime = calendar.getTimeInMillis();

            db = dbHelper.getWritableDatabase();
            Cursor mCursor = db.rawQuery("select * from dat_expense where koshinnichiji >= " + startTime + " and koshinnichiji <= " + endTime + " order by uriagekb", null);
            mCursor.moveToFirst();
            if (!mCursor.isAfterLast()) {
                do {
                    HashMap map = new HashMap();
                    map.put("shimeno", mCursor.getInt(mCursor.getColumnIndex("shimeno")));
                    map.put("tanmatsumei", mCursor.getString(mCursor.getColumnIndex("tanmatsumei")));
                    map.put("hanbaibasho", mCursor.getString(mCursor.getColumnIndex("hanbaibasho")));
                    map.put("shimebi", cm.converToDateTimeFormatFromTime((long) mCursor.getDouble(mCursor.getColumnIndex("shimebi"))));
                    map.put("uriagekb", mCursor.getInt(mCursor.getColumnIndex("uriagekb")));
                    map.put("haraimodoshikb", mCursor.getString(mCursor.getColumnIndex("haraimodoshikb")));
                    map.put("suryo", mCursor.getInt(mCursor.getColumnIndex("suryo")));
                    map.put("kingaku", mCursor.getInt(mCursor.getColumnIndex("kingaku")));
                    map.put("shohizei", mCursor.getInt(mCursor.getColumnIndex("shohizei")));

                    map.put("sakuseiuserid", mCursor.getString(mCursor.getColumnIndex("sakuseiuserid")));
                    map.put("sakuseinichiji", cm.converToDateTimeFormatFromTime((long) mCursor.getDouble(mCursor.getColumnIndex("sakuseinichiji"))));
                    map.put("koshinuserid", mCursor.getString(mCursor.getColumnIndex("koshinuserid")));
                    map.put("koshinnichiji", cm.converToDateTimeFormatFromTime((long) mCursor.getDouble(mCursor.getColumnIndex("koshinnichiji"))));
                    list.add(map);
                } while (mCursor.moveToNext());
            }
            mCursor.close();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return list;
    }

    public void addReadXMLdata(String fname, String content) {
        db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("tekiyonichiji", cm.convertToMilisecondsFromDate(new Date()));
        HashMap map = getDeviceInfo();
        if (map != null) {
            values.put("tanmatsumei", String.valueOf(map.get("tanmatsumei")));
        }else {
            values.put("tanmatsumei", "Android");
        }
        values.put("filemei", fname);
        values.put("filenaiyo", content);
        values.put("sakuseiuserid", cm.me.getId());
        values.put("sakuseinichiji", cm.convertToMilisecondsFromDate(new Date()));
        values.put("koshinuserid", cm.me.getId());
        values.put("koshinnichiji", cm.convertToMilisecondsFromDate(new Date()));
        db.insert("dat_history", null, values);
    }

    public ArrayList<HashMap> getReadXMLData(Calendar calendar) {
        ArrayList<HashMap> list = new ArrayList<>();
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        try {
            Date c_date = dateFormatter.parse(calendar.get(Calendar.YEAR)+"-"+(calendar.get(Calendar.MONTH)+1)+"-"+calendar.get(Calendar.DAY_OF_MONTH)+" 00:00");
            calendar.setTime(c_date);
            long startTime = calendar.getTimeInMillis();
            c_date = dateFormatter.parse(calendar.get(Calendar.YEAR)+"-"+(calendar.get(Calendar.MONTH)+1)+"-"+calendar.get(Calendar.DAY_OF_MONTH)+" 23:59");
            calendar.setTime(c_date);
            long endTime = calendar.getTimeInMillis();

            db = dbHelper.getWritableDatabase();
            Cursor mCursor = db.rawQuery("select * from dat_history where koshinnichiji >= " + startTime + " and koshinnichiji <= " + endTime, null);
            mCursor.moveToFirst();
            if (!mCursor.isAfterLast()) {
                do {
                    HashMap map = new HashMap();
                    map.put("tekiyonichiji", cm.converToDateTimeFormatFromTime((long) mCursor.getDouble(mCursor.getColumnIndex("tekiyonichiji"))));
                    map.put("tanmatsumei", mCursor.getString(mCursor.getColumnIndex("tanmatsumei")));
                    map.put("filemei", mCursor.getString(mCursor.getColumnIndex("filemei")));
                    map.put("filenaiyo", mCursor.getString(mCursor.getColumnIndex("filenaiyo")));

                    map.put("sakuseiuserid", mCursor.getString(mCursor.getColumnIndex("sakuseiuserid")));
                    map.put("sakuseinichiji", cm.converToDateTimeFormatFromTime((long) mCursor.getDouble(mCursor.getColumnIndex("sakuseinichiji"))));
                    map.put("koshinuserid", mCursor.getString(mCursor.getColumnIndex("koshinuserid")));
                    map.put("koshinnichiji", cm.converToDateTimeFormatFromTime((long) mCursor.getDouble(mCursor.getColumnIndex("koshinnichiji"))));
                    list.add(map);
                } while (mCursor.moveToNext());
            }
            mCursor.close();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return list;
    }

    public void addDeviceInfo(ContentValues values) {
        db = dbHelper.getWritableDatabase();
        db.execSQL("DELETE FROM mst_tanmatsu");
        db.insert("mst_tanmatsu", null, values);
    }

    public void addTableFromServer(String tbname, ArrayList<ContentValues> list) {
        db = dbHelper.getWritableDatabase();
        db.execSQL("DELETE FROM " + tbname);
        if (list.size() > 0) {
            for (ContentValues model : list) {
                db.insert(tbname, null, model);
            }
        }
    }

    public boolean saveByDayEndData (Calendar calendar) {
        boolean isSuccess = true;

        ArrayList<String> list = getSentionWhitName("URIAGEKB");
        if (list != null) {
            for (String value : list) {
                db = dbHelper.getReadableDatabase();
                Cursor mCursor = db.rawQuery("select * from dat_record where " +
                        "haraimodoshikb = '0'" +
                        " and uriagekb = '" + value + "'" +
                        " and shimeno = 0", null);
                int totalNum = 0;
                int totalMoney = 0;
                int totalTax = 0;
                mCursor.moveToFirst();
                if (!mCursor.isAfterLast()) {
                    do {
                        totalNum += mCursor.getInt(mCursor.getColumnIndex("uriagesuryo"));
                        totalMoney += mCursor.getInt(mCursor.getColumnIndex("hanbaikingaku"));
                        totalTax += mCursor.getInt(mCursor.getColumnIndex("shohizeigaku"));
                        db = dbHelper.getWritableDatabase();
                        ContentValues values = new ContentValues();
                        values.put("shimeno", 1);
                        values.put("koshinuserid", cm.me.getId());
                        values.put("koshinnichiji", cm.convertToMilisecondsFromDate(new Date()));
                        db.update("dat_record", values, "uriageno = " + mCursor.getInt(mCursor.getColumnIndex("uriageno")), null);
                    } while (mCursor.moveToNext());
                }
                mCursor.close();
                addSettlementInfoWithData(cm.parseInteger(value), 0, totalNum, totalMoney, totalTax);
            }
        }
        db = dbHelper.getReadableDatabase();
        Cursor mCursor = db.rawQuery("select * from dat_record where " +
                "haraimodoshikb = '1'" +
                " and shimeno = 0", null);
        int totalNum = 0;
        int totalMoney = 0;
        int totalTax = 0;
        mCursor.moveToFirst();
        if (!mCursor.isAfterLast()) {
            do {
                totalNum += mCursor.getInt(mCursor.getColumnIndex("uriagesuryo"));
                totalMoney += mCursor.getInt(mCursor.getColumnIndex("hanbaikingaku"));
                totalTax += mCursor.getInt(mCursor.getColumnIndex("shohizeigaku"));
                db = dbHelper.getWritableDatabase();
                ContentValues values = new ContentValues();
                values.put("shimeno", 1);
                values.put("koshinuserid", cm.me.getId());
                values.put("koshinnichiji", cm.convertToMilisecondsFromDate(new Date()));
                db.update("dat_record", values, "uriageno = " + mCursor.getInt(mCursor.getColumnIndex("uriageno")), null);
            } while (mCursor.moveToNext());
        }
        mCursor.close();
        addSettlementInfoWithData(5, 1, totalNum, totalMoney, totalTax);

        return isSuccess;
    }

    public boolean deleteByDayEndData (Calendar calendar) {
        boolean isSuccess = true;
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        try {
            Date c_date = dateFormatter.parse(calendar.get(Calendar.YEAR)+"-"+(calendar.get(Calendar.MONTH)+1)+"-"+calendar.get(Calendar.DAY_OF_MONTH)+" 00:00");
            calendar.setTime(c_date);
            long startTime = calendar.getTimeInMillis();
            c_date = dateFormatter.parse(calendar.get(Calendar.YEAR)+"-"+(calendar.get(Calendar.MONTH)+1)+"-"+calendar.get(Calendar.DAY_OF_MONTH)+" 23:59");
            calendar.setTime(c_date);
            long endTime = calendar.getTimeInMillis();

            db = dbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("shimeno", 0);
            values.put("koshinuserid", cm.me.getId());
            values.put("koshinnichiji", cm.convertToMilisecondsFromDate(new Date()));
            db.update("dat_record", values, "koshinnichiji >= " + startTime, null);
            db.delete("mst_counter", "saibankb = 'SEISAN' and koshinnichiji >= " + startTime + " and koshinnichiji <= " + endTime, null);
            db.delete("dat_expense", "koshinnichiji >= " + startTime, null);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return isSuccess;
    }

    public ArrayList<TicketInfo> getByDayEndData (Calendar calendar) {
        ArrayList<TicketInfo> list = new ArrayList<>();
        db = dbHelper.getReadableDatabase();
        Cursor mCursor = db.rawQuery("select * from mst_user", null);
        mCursor.moveToFirst();
        if (!mCursor.isAfterLast()) {
            do {
                TicketInfo info = new TicketInfo();
                TicketModel model = formatTicketModel(mCursor);
                info.setModel(model);
                info.setNum(mCursor.getInt(mCursor.getColumnIndex("num")));
                list.add(info);
            } while (mCursor.moveToNext());
        }
        mCursor.close();
        return list;
    }

    /**
     * クエリ結果からチケット定義情報に変換する
     * from mst_ticket table
     *
     * @param mCursor クエリ結果
     */
    public TicketModel formatTicketModel(Cursor mCursor) {
        TicketModel model = new TicketModel();

        model.setId(String.valueOf(mCursor.getInt(mCursor.getColumnIndex("ticketid"))));
//        model.setType(String.valueOf(mCursor.getInt(mCursor.getColumnIndex("ticketid"))));
        model.setName(mCursor.getString(mCursor.getColumnIndex("kenshumei")));
        model.setPrice(mCursor.getInt(mCursor.getColumnIndex("kakaku")));
        model.setTaxRatio(Float.valueOf(mCursor.getString(mCursor.getColumnIndex("shohizeiritsu"))));
        model.setTax(mCursor.getInt(mCursor.getColumnIndex("shohizeigaku")));
        db = dbHelper.getReadableDatabase();
        Cursor mc = db.rawQuery("select * from mst_ui_tabticket where ticketid = " + mCursor.getInt(mCursor.getColumnIndex("ticketid")), null);
        mc.moveToFirst();
        if (!mc.isAfterLast()) {
            do {
                model.setRowPos(mc.getInt(mc.getColumnIndex("xpos")));
                model.setColPos(mc.getInt(mc.getColumnIndex("ypos")));
            } while (mc.moveToNext());
        }
        mc.close();
        model.setFgColor(mCursor.getString(mCursor.getColumnIndex("mojishoku")));
        model.setBgColor(mCursor.getString(mCursor.getColumnIndex("haikeishoku")));
        model.setOrder(mCursor.getInt(mCursor.getColumnIndex("seisansortno")));
        model.setEndDays(mCursor.getInt(mCursor.getColumnIndex("yukokikanbi")));

        return model;
    }

    /**
     * チケットや領収書の印字レイアウト定義
     * from mst_ticketstyle table
     *
     * @param mCursor クエリ結果
     */
    public PrinterInfo formatPrinterInfo(Cursor mCursor) {
        PrinterInfo info = new PrinterInfo();

        info.setType(mCursor.getString(mCursor.getColumnIndex("chohyokb")));
        info.setPrinterNum(mCursor.getInt(mCursor.getColumnIndex("hyojikb")));
        info.setPrinterType(mCursor.getString(mCursor.getColumnIndex("injishubetsu")));
        info.setIsShown(String.valueOf(mCursor.getInt(mCursor.getColumnIndex("areano"))));
        info.setFont(mCursor.getString(mCursor.getColumnIndex("fontmei")));
        info.setFontSize(mCursor.getInt(mCursor.getColumnIndex("fontsize")));
        info.setIsItalic(mCursor.getInt(mCursor.getColumnIndex("fontstyle_shatai")));
        info.setIsBold(mCursor.getInt(mCursor.getColumnIndex("fontstyle_bold")));
        info.setFormat(mCursor.getString(mCursor.getColumnIndex("shoshiki")));
        info.setStartX(mCursor.getInt(mCursor.getColumnIndex("xposfrom")));
        info.setStartY(mCursor.getInt(mCursor.getColumnIndex("yposfrom")));
        info.setEndX(mCursor.getInt(mCursor.getColumnIndex("xposto")));
        info.setEndY(mCursor.getInt(mCursor.getColumnIndex("yposto")));
        info.setFileName(mCursor.getString(mCursor.getColumnIndex("filemei")));
        info.setImgData(mCursor.getBlob(mCursor.getColumnIndex("img")));
        info.setBarcodeType(mCursor.getInt(mCursor.getColumnIndex("barcodetype")));
        info.setBarcodeHeight(mCursor.getInt(mCursor.getColumnIndex("barcodeheight")));
//        info.setBarcode(String.valueOf(mCursor.getInt(mCursor.getColumnIndex("barcode"))));

        return info;
    }

    /**
     * 画面タブ表示
     * from mst_ui_tab table
     *
     * @param mCursor クエリ結果
     */
    public TicketType formatTicketType(Cursor mCursor) {
        TicketType model = new TicketType();

//        model.setType(String.valueOf(mCursor.getInt(mCursor.getColumnIndex("ticketid"))));
        model.setName(mCursor.getString(mCursor.getColumnIndex("tickettypecd")));
        model.setOrder(mCursor.getInt(mCursor.getColumnIndex("sortno")));
        model.setIsShown(mCursor.getString(mCursor.getColumnIndex("hyojikb")));

        return model;
    }

    /**
     * クエリ結果からUserに変換する
     *
     * @param mCursor クエリ結果
     */
    public User formatUser(Cursor mCursor) {
        User user = new User();

        user.setId(mCursor.getString(mCursor.getColumnIndex("userid")));
        user.setName(mCursor.getString(mCursor.getColumnIndex("username")));
        user.setPassword(mCursor.getString(mCursor.getColumnIndex("password")));
        user.setRoll(mCursor.getString(mCursor.getColumnIndex("kengengrp")));
        return user;
    }

    public void initUserInfo() {
        db = dbHelper.getWritableDatabase();
        db.execSQL("DELETE FROM mst_user");
        ContentValues values = new ContentValues();
        values.put("userid", "user");
        values.put("password", "bcb15f821479b4d5772bd0ca866c00ad5f926e3580720659cc80d39c9d09802a");
        values.put("username", "ユーザー");
        values.put("kengengrp", "2");
        values.put("sakuseiuserid", "1");
        values.put("sakuseinichiji", cm.convertToMilisecondsFromDate(new Date()));
        values.put("koshinuserid", "1");
        values.put("koshinnichiji", cm.convertToMilisecondsFromDate(new Date()));
        db.insert("mst_user", null, values);
    }

    public void setTestData() {
        db = dbHelper.getWritableDatabase();
        db.execSQL("DELETE FROM mst_kbn");
        ContentValues values = new ContentValues();
        values.put("kbtype", "TICKETTYPE");
        values.put("kbcd", "1");
        values.put("kbmei", "平日");
        values.put("biko", "");
        values.put("sakuseiuserid", cm.me.getId());
        values.put("sakuseinichiji", cm.convertToMilisecondsFromDate(new Date()));
        values.put("koshinuserid", cm.me.getId());
        values.put("koshinnichiji", cm.convertToMilisecondsFromDate(new Date()));
        db.insert("mst_kbn", null, values);
        values.clear();
        values.put("kbtype", "TICKETTYPE");
        values.put("kbcd", "2");
        values.put("kbmei", "休日");
        values.put("biko", "");
        values.put("sakuseiuserid", cm.me.getId());
        values.put("sakuseinichiji", cm.convertToMilisecondsFromDate(new Date()));
        values.put("koshinuserid", cm.me.getId());
        values.put("koshinnichiji", cm.convertToMilisecondsFromDate(new Date()));
        db.insert("mst_kbn", null, values);
        values.clear();
        values.put("kbtype", "TICKETTYPE");
        values.put("kbcd", "3");
        values.put("kbmei", "休日");
        values.put("biko", "");
        values.put("sakuseiuserid", cm.me.getId());
        values.put("sakuseinichiji", cm.convertToMilisecondsFromDate(new Date()));
        values.put("koshinuserid", cm.me.getId());
        values.put("koshinnichiji", cm.convertToMilisecondsFromDate(new Date()));
        db.insert("mst_kbn", null, values);
        values.clear();
        values.put("kbtype", "TICKETTYPE");
        values.put("kbcd", "4");
        values.put("kbmei", "休日");
        values.put("biko", "");
        values.put("sakuseiuserid", cm.me.getId());
        values.put("sakuseinichiji", cm.convertToMilisecondsFromDate(new Date()));
        values.put("koshinuserid", cm.me.getId());
        values.put("koshinnichiji", cm.convertToMilisecondsFromDate(new Date()));
        db.insert("mst_kbn", null, values);
        values.clear();
        values.put("kbtype", "HYOJIKB");
        values.put("kbcd", "1");
        values.put("kbmei", "");
        values.put("biko", "");
        values.put("sakuseiuserid", cm.me.getId());
        values.put("sakuseinichiji", cm.convertToMilisecondsFromDate(new Date()));
        values.put("koshinuserid", cm.me.getId());
        values.put("koshinnichiji", cm.convertToMilisecondsFromDate(new Date()));
        db.insert("mst_kbn", null, values);
        values.clear();
        values.put("kbtype", "HYOJIKB");
        values.put("kbcd", "2");
        values.put("kbmei", "");
        values.put("biko", "");
        values.put("sakuseiuserid", cm.me.getId());
        values.put("sakuseinichiji", cm.convertToMilisecondsFromDate(new Date()));
        values.put("koshinuserid", cm.me.getId());
        values.put("koshinnichiji", cm.convertToMilisecondsFromDate(new Date()));
        db.insert("mst_kbn", null, values);
        values.clear();
        values.put("kbtype", "INJIKB");
        values.put("kbcd", "1");
        values.put("kbmei", "");
        values.put("biko", "");
        values.put("sakuseiuserid", cm.me.getId());
        values.put("sakuseinichiji", cm.convertToMilisecondsFromDate(new Date()));
        values.put("koshinuserid", cm.me.getId());
        values.put("koshinnichiji", cm.convertToMilisecondsFromDate(new Date()));
        db.insert("mst_kbn", null, values);
        values.clear();
        values.put("kbtype", "INJIKB");
        values.put("kbcd", "2");
        values.put("kbmei", "");
        values.put("biko", "");
        values.put("sakuseiuserid", cm.me.getId());
        values.put("sakuseinichiji", cm.convertToMilisecondsFromDate(new Date()));
        values.put("koshinuserid", cm.me.getId());
        values.put("koshinnichiji", cm.convertToMilisecondsFromDate(new Date()));
        db.insert("mst_kbn", null, values);
        values.clear();
        values.put("kbtype", "INJIZOKUSEI");
        values.put("kbcd", "HAKKENBI");
        values.put("kbmei", "");
        values.put("biko", "");
        values.put("sakuseiuserid", cm.me.getId());
        values.put("sakuseinichiji", cm.convertToMilisecondsFromDate(new Date()));
        values.put("koshinuserid", cm.me.getId());
        values.put("koshinnichiji", cm.convertToMilisecondsFromDate(new Date()));
        db.insert("mst_kbn", null, values);
        values.clear();
        values.put("kbtype", "INJIZOKUSEI");
        values.put("kbcd", "YUKOKIGEN");
        values.put("kbmei", "");
        values.put("biko", "");
        values.put("sakuseiuserid", cm.me.getId());
        values.put("sakuseinichiji", cm.convertToMilisecondsFromDate(new Date()));
        values.put("koshinuserid", cm.me.getId());
        values.put("koshinnichiji", cm.convertToMilisecondsFromDate(new Date()));
        db.insert("mst_kbn", null, values);
        values.clear();
        values.put("kbtype", "INJIZOKUSEI");
        values.put("kbcd", "KENSHUMEI");
        values.put("kbmei", "");
        values.put("biko", "");
        values.put("sakuseiuserid", cm.me.getId());
        values.put("sakuseinichiji", cm.convertToMilisecondsFromDate(new Date()));
        values.put("koshinuserid", cm.me.getId());
        values.put("koshinnichiji", cm.convertToMilisecondsFromDate(new Date()));
        db.insert("mst_kbn", null, values);
        values.clear();
        values.put("kbtype", "INJIZOKUSEI");
        values.put("kbcd", "KAKAKU");
        values.put("kbmei", "");
        values.put("biko", "");
        values.put("sakuseiuserid", cm.me.getId());
        values.put("sakuseinichiji", cm.convertToMilisecondsFromDate(new Date()));
        values.put("koshinuserid", cm.me.getId());
        values.put("koshinnichiji", cm.convertToMilisecondsFromDate(new Date()));
        db.insert("mst_kbn", null, values);
        values.clear();
        values.put("kbtype", "INJIZOKUSEI");
        values.put("kbcd", "GENZAINICHIJI");
        values.put("kbmei", "");
        values.put("biko", "");
        values.put("sakuseiuserid", cm.me.getId());
        values.put("sakuseinichiji", cm.convertToMilisecondsFromDate(new Date()));
        values.put("koshinuserid", cm.me.getId());
        values.put("koshinnichiji", cm.convertToMilisecondsFromDate(new Date()));
        db.insert("mst_kbn", null, values);
        values.clear();
        values.put("kbtype", "INJISHUBETSU");
        values.put("kbcd", "TEXT");
        values.put("kbmei", "");
        values.put("biko", "");
        values.put("sakuseiuserid", cm.me.getId());
        values.put("sakuseinichiji", cm.convertToMilisecondsFromDate(new Date()));
        values.put("koshinuserid", cm.me.getId());
        values.put("koshinnichiji", cm.convertToMilisecondsFromDate(new Date()));
        db.insert("mst_kbn", null, values);
        values.clear();
        values.put("kbtype", "INJISHUBETSU");
        values.put("kbcd", "IMAGE");
        values.put("kbmei", "");
        values.put("biko", "");
        values.put("sakuseiuserid", cm.me.getId());
        values.put("sakuseinichiji", cm.convertToMilisecondsFromDate(new Date()));
        values.put("koshinuserid", cm.me.getId());
        values.put("koshinnichiji", cm.convertToMilisecondsFromDate(new Date()));
        db.insert("mst_kbn", null, values);
        values.clear();
        values.put("kbtype", "INJISHUBETSU");
        values.put("kbcd", "BARCODE");
        values.put("kbmei", "");
        values.put("biko", "");
        values.put("sakuseiuserid", cm.me.getId());
        values.put("sakuseinichiji", cm.convertToMilisecondsFromDate(new Date()));
        values.put("koshinuserid", cm.me.getId());
        values.put("koshinnichiji", cm.convertToMilisecondsFromDate(new Date()));
        db.insert("mst_kbn", null, values);
        values.clear();
        values.put("kbtype", "INJISHUBETSU");
        values.put("kbcd", "LINE");
        values.put("kbmei", "");
        values.put("biko", "");
        values.put("sakuseiuserid", cm.me.getId());
        values.put("sakuseinichiji", cm.convertToMilisecondsFromDate(new Date()));
        values.put("koshinuserid", cm.me.getId());
        values.put("koshinnichiji", cm.convertToMilisecondsFromDate(new Date()));
        db.insert("mst_kbn", null, values);
        values.clear();
        values.put("kbtype", "USERKENGEN");
        values.put("kbcd", "1");
        values.put("kbmei", "");
        values.put("biko", "");
        values.put("sakuseiuserid", cm.me.getId());
        values.put("sakuseinichiji", cm.convertToMilisecondsFromDate(new Date()));
        values.put("koshinuserid", cm.me.getId());
        values.put("koshinnichiji", cm.convertToMilisecondsFromDate(new Date()));
        db.insert("mst_kbn", null, values);
        values.clear();
        values.put("kbtype", "USERKENGEN");
        values.put("kbcd", "2");
        values.put("kbmei", "");
        values.put("biko", "");
        values.put("sakuseiuserid", cm.me.getId());
        values.put("sakuseinichiji", cm.convertToMilisecondsFromDate(new Date()));
        values.put("koshinuserid", cm.me.getId());
        values.put("koshinnichiji", cm.convertToMilisecondsFromDate(new Date()));
        db.insert("mst_kbn", null, values);
        values.clear();
        values.put("kbtype", "URIAGEKB");
        values.put("kbcd", "1");
        values.put("kbmei", "");
        values.put("biko", "");
        values.put("sakuseiuserid", cm.me.getId());
        values.put("sakuseinichiji", cm.convertToMilisecondsFromDate(new Date()));
        values.put("koshinuserid", cm.me.getId());
        values.put("koshinnichiji", cm.convertToMilisecondsFromDate(new Date()));
        db.insert("mst_kbn", null, values);
        values.clear();
        values.put("kbtype", "URIAGEKB");
        values.put("kbcd", "2");
        values.put("kbmei", "");
        values.put("biko", "");
        values.put("sakuseiuserid", cm.me.getId());
        values.put("sakuseinichiji", cm.convertToMilisecondsFromDate(new Date()));
        values.put("koshinuserid", cm.me.getId());
        values.put("koshinnichiji", cm.convertToMilisecondsFromDate(new Date()));
        db.insert("mst_kbn", null, values);
        values.clear();
        values.put("kbtype", "URIAGEKB");
        values.put("kbcd", "3");
        values.put("kbmei", "");
        values.put("biko", "");
        values.put("sakuseiuserid", cm.me.getId());
        values.put("sakuseinichiji", cm.convertToMilisecondsFromDate(new Date()));
        values.put("koshinuserid", cm.me.getId());
        values.put("koshinnichiji", cm.convertToMilisecondsFromDate(new Date()));
        db.insert("mst_kbn", null, values);
        values.clear();
        values.put("kbtype", "URIAGEKB");
        values.put("kbcd", "4");
        values.put("kbmei", "");
        values.put("biko", "");
        values.put("sakuseiuserid", cm.me.getId());
        values.put("sakuseinichiji", cm.convertToMilisecondsFromDate(new Date()));
        values.put("koshinuserid", cm.me.getId());
        values.put("koshinnichiji", cm.convertToMilisecondsFromDate(new Date()));
        db.insert("mst_kbn", null, values);
        values.clear();
        values.put("kbtype", "HANBAIBASHO");
        values.put("kbcd", "1");
        values.put("kbmei", "センターハウス事務所");
        values.put("biko", "");
        values.put("sakuseiuserid", cm.me.getId());
        values.put("sakuseinichiji", cm.convertToMilisecondsFromDate(new Date()));
        values.put("koshinuserid", cm.me.getId());
        values.put("koshinnichiji", cm.convertToMilisecondsFromDate(new Date()));
        db.insert("mst_kbn", null, values);
        values.clear();
        values.put("kbtype", "HANBAIBASHO");
        values.put("kbcd", "2");
        values.put("kbmei", "センターハウス2F発券場所1");
        values.put("biko", "");
        values.put("sakuseiuserid", cm.me.getId());
        values.put("sakuseinichiji", cm.convertToMilisecondsFromDate(new Date()));
        values.put("koshinuserid", cm.me.getId());
        values.put("koshinnichiji", cm.convertToMilisecondsFromDate(new Date()));
        db.insert("mst_kbn", null, values);
        values.clear();
        values.put("kbtype", "HANBAIBASHO");
        values.put("kbcd", "3");
        values.put("kbmei", "センターハウス2F発券場所2");
        values.put("biko", "");
        values.put("sakuseiuserid", cm.me.getId());
        values.put("sakuseinichiji", cm.convertToMilisecondsFromDate(new Date()));
        values.put("koshinuserid", cm.me.getId());
        values.put("koshinnichiji", cm.convertToMilisecondsFromDate(new Date()));
        db.insert("mst_kbn", null, values);
        values.clear();
        values.put("kbtype", "HANBAIBASHO");
        values.put("kbcd", "4");
        values.put("kbmei", "チケット販売所1");
        values.put("biko", "");
        values.put("sakuseiuserid", cm.me.getId());
        values.put("sakuseinichiji", cm.convertToMilisecondsFromDate(new Date()));
        values.put("koshinuserid", cm.me.getId());
        values.put("koshinnichiji", cm.convertToMilisecondsFromDate(new Date()));
        db.insert("mst_kbn", null, values);
        values.clear();
        values.put("kbtype", "HANBAIBASHO");
        values.put("kbcd", "5");
        values.put("kbmei", "チケット販売所2");
        values.put("biko", "");
        values.put("sakuseiuserid", cm.me.getId());
        values.put("sakuseinichiji", cm.convertToMilisecondsFromDate(new Date()));
        values.put("koshinuserid", cm.me.getId());
        values.put("koshinnichiji", cm.convertToMilisecondsFromDate(new Date()));
        db.insert("mst_kbn", null, values);
        values.clear();
        values.put("kbtype", "HANBAIBASHO");
        values.put("kbcd", "6");
        values.put("kbmei", "レンタルスキーハウス");
        values.put("biko", "");
        values.put("sakuseiuserid", cm.me.getId());
        values.put("sakuseinichiji", cm.convertToMilisecondsFromDate(new Date()));
        values.put("koshinuserid", cm.me.getId());
        values.put("koshinnichiji", cm.convertToMilisecondsFromDate(new Date()));
        db.insert("mst_kbn", null, values);
        values.clear();
        values.put("kbtype", "HARAIMODOSHIKB");
        values.put("kbcd", "0");
        values.put("kbmei", "");
        values.put("biko", "");
        values.put("sakuseiuserid", cm.me.getId());
        values.put("sakuseinichiji", cm.convertToMilisecondsFromDate(new Date()));
        values.put("koshinuserid", cm.me.getId());
        values.put("koshinnichiji", cm.convertToMilisecondsFromDate(new Date()));
        db.insert("mst_kbn", null, values);
        values.clear();
        values.put("kbtype", "HARAIMODOSHIKB");
        values.put("kbcd", "1");
        values.put("kbmei", "");
        values.put("biko", "");
        values.put("sakuseiuserid", cm.me.getId());
        values.put("sakuseinichiji", cm.convertToMilisecondsFromDate(new Date()));
        values.put("koshinuserid", cm.me.getId());
        values.put("koshinnichiji", cm.convertToMilisecondsFromDate(new Date()));
        db.insert("mst_kbn", null, values);
        values.clear();
        values.put("kbtype", "CHOHYOKB");
        values.put("kbcd", "TICKET");
        values.put("kbmei", "チケット");
        values.put("biko", "");
        values.put("sakuseiuserid", cm.me.getId());
        values.put("sakuseinichiji", cm.convertToMilisecondsFromDate(new Date()));
        values.put("koshinuserid", cm.me.getId());
        values.put("koshinnichiji", cm.convertToMilisecondsFromDate(new Date()));
        db.insert("mst_kbn", null, values);
        values.clear();
        values.put("kbtype", "CHOHYOKB");
        values.put("kbcd", "RYOSHUSHO");
        values.put("kbmei", "領収書");
        values.put("biko", "");
        values.put("sakuseiuserid", cm.me.getId());
        values.put("sakuseinichiji", cm.convertToMilisecondsFromDate(new Date()));
        values.put("koshinuserid", cm.me.getId());
        values.put("koshinnichiji", cm.convertToMilisecondsFromDate(new Date()));
        db.insert("mst_kbn", null, values);
    }

}