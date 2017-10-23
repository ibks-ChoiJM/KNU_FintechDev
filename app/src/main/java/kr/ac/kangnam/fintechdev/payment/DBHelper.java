package kr.ac.kangnam.fintechdev.payment;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.google.gson.Gson;

import java.util.ArrayList;

import kr.ac.kangnam.fintechdev.data.PaymentDto;
import kr.ac.kangnam.fintechdev.data.PrdInfoDto;
import kr.ac.kangnam.fintechdev.data.SellerInfoDto;

/**
 * Created by choi on 2017. 9. 20..
 */

public class DBHelper extends SQLiteOpenHelper {


    // DBHelper 생성자로 관리할 DB 이름과 버전 정보를 받음
    public DBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    // DB를 새로 생성할 때 호출되는 함수
    @Override
    public void onCreate(SQLiteDatabase db) {
        // 새로운 테이블 생성
        db.execSQL("CREATE TABLE SELLERINFO " +
                "(seller_id INTEGER PRIMARY KEY, " +
                "created_at DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                "updated_at DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                "seller_name TEXT, " +
                "phone_num TEXT, " +
                "qr_id TEXT);");

        db.execSQL("CREATE TABLE PRDINFO " +
                "(prd_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "seller_id INTEGER , " +
                "seller_name TEXT, " +
                "qr_id TEXT , " +
                "created_at DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                "updated_at DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                "prd_name TEXT, " +
                "prd_price INTEGER);");

        db.execSQL("CREATE TABLE PAYMENTINFO " +
                "(no INTEGER PRIMARY KEY AUTOINCREMENT," +
                "seller_id INTEGER , " +
                "qr_id TEXT , " +
                "payment_at DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                "payment_type INTEGER, " +
                "buyer_account_num TEXT, " +
                "prd_id INTEGER, " +
                "prd_name TEXT, " +
                "prd_price INTEGER);");
    }

    // DB 업그레이드를 위해 버전이 변경될 때 호출되는 함수
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void insertSellerInfo(int nSellerId, String sSellerName, String sPhoneNum, String sQRid)
    {
        insertSellerInfoDB(nSellerId, sSellerName, sPhoneNum, sQRid);
    }

    private void insertSellerInfoDB(Object... values)
    {
        // 읽고 쓰기가 가능하게 DB 열기
        SQLiteDatabase db = getWritableDatabase();
        // DB에 입력한 값으로 행 추가
        db.execSQL("INSERT INTO SELLERINFO VALUES(?,datetime('now','localtime'),datetime('now','localtime'),?,?,?);", values);
        db.close();
    }

    public String selectSellerInfo() {
        // 읽기가 가능하게 DB 열기
        SQLiteDatabase db = getReadableDatabase();
        String result = "";

        SellerInfoDto info = new SellerInfoDto();

        // DB에 있는 데이터를 쉽게 처리하기 위해 Cursor를 사용하여 테이블에 있는 모든 데이터 출력
        Cursor cursor = db.rawQuery("SELECT * FROM SELLERINFO LIMIT 1", null);
        if (cursor.moveToNext()) {

            Log.d("test", "cursor.getString(1) : " + cursor.getString(1));

            info.setSellerId(cursor.getInt(0));
            info.setCreatedAt(cursor.getString(1));
            info.setUpdatedAt(cursor.getString(2));
            info.setSellerName(cursor.getString(3));
            info.setPhoneNum(cursor.getString(4));
            info.setQrId(cursor.getString(5));


        }

        Gson gson = new Gson();
        result = gson.toJson(info);

        return result;
    }

    public void insertPrdInfo(int nSellerId, String sSellerName, String sQRid, String sPrdName, long sPrdPrice )
    {
        insertPrdInfoDB(nSellerId, sSellerName, sQRid, sPrdName, sPrdPrice);
    }

    private void insertPrdInfoDB(Object... values)
    {
        // 읽고 쓰기가 가능하게 DB 열기
        SQLiteDatabase db = getWritableDatabase();
        // DB에 입력한 값으로 행 추가
        db.execSQL("INSERT INTO PRDINFO VALUES(null,?,?,?,datetime('now','localtime'),datetime('now','localtime'),?,?);", values);
        db.close();
    }

    public String selectPrdInfo(String sQRid) {
        // 읽기가 가능하게 DB 열기
        SQLiteDatabase db = getReadableDatabase();
        String result = "";
        PrdInfoDto info = new PrdInfoDto();

        String[] selections = new String[1];
        selections[0] = sQRid;

        // DB에 있는 데이터를 쉽게 처리하기 위해 Cursor를 사용하여 테이블에 있는 모든 데이터 출력
        Cursor cursor = db.rawQuery("SELECT * FROM PRDINFO WHERE qr_id=? LIMIT 1", selections);
        if (cursor.moveToNext()) {

            info.setPrdId(cursor.getInt(0));
            info.setSellerId(cursor.getInt(1));
            info.setSellerName(cursor.getString(2));
            info.setQrId(cursor.getString(3));
            info.setCreatedAt(cursor.getString(4));
            info.setUpdatedAt(cursor.getString(5));
            info.setPrdName(cursor.getString(6));
            info.setPrdPrice(cursor.getInt(7));

        }

        Gson gson = new Gson();
        result = gson.toJson(info);

        return result;
    }

    public void insertPayment(int nSellerId, String sQRid, int nPaymentType, String sBuyerAccountNum, int nPrdId, String sPrdName, long sPrdPrice )
    {
        insertPaymentDB(nSellerId, sQRid, nPaymentType, sBuyerAccountNum, nPrdId, sPrdName, sPrdPrice);
    }

    private void insertPaymentDB(Object... values)
    {
        // 읽고 쓰기가 가능하게 DB 열기
        SQLiteDatabase db = getWritableDatabase();
        // DB에 입력한 값으로 행 추가
        db.execSQL("INSERT INTO PAYMENTINFO VALUES(null,?,?,datetime('now','localtime'),?,?,?,?,?);", values);
        db.close();
    }

    public long selectTotalPrice() {
        // 읽기가 가능하게 DB 열기
        SQLiteDatabase db = getReadableDatabase();

        Cursor cur = db.rawQuery("SELECT SUM(prd_price) FROM PAYMENTINFO", null);
        if(cur.moveToFirst())
        {
            return cur.getInt(0);
        }

        return 0;
    }

    public String selectPaymentList(String sDate) {
        // 읽기가 가능하게 DB 열기
        SQLiteDatabase db = getReadableDatabase();

        String[] selections = new String[2];
        selections[0] = sDate;
        selections[1] = sDate;

        ArrayList<PaymentDto> arrauPayment = new ArrayList<>();

        // DB에 있는 데이터를 쉽게 처리하기 위해 Cursor를 사용하여 테이블에 있는 모든 데이터 출력
        Cursor cursor = db.rawQuery("SELECT * FROM PAYMENTINFO WHERE payment_at BETWEEN ? AND date(?, '+1 day')", selections);
        while (cursor.moveToNext()) {

            PaymentDto paymentDto = new PaymentDto();
            paymentDto.setNo(cursor.getInt(0));
            paymentDto.setSellerId(cursor.getInt(1));
            paymentDto.setQrId(cursor.getString(2));
            paymentDto.setPaymentAt(cursor.getString(3));
            paymentDto.setPaymentType(cursor.getInt(4));
            paymentDto.setBuyerAccountNum(cursor.getString(5));
            paymentDto.setPrdId(cursor.getInt(6));
            paymentDto.setPrdName(cursor.getString(7));
            paymentDto.setPrdPrice(cursor.getInt(8));

            arrauPayment.add(paymentDto);
        }

        Gson gson = new Gson();
        return gson.toJson(arrauPayment);
    }


}
