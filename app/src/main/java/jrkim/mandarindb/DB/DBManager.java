package jrkim.mandarindb.DB;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Handler;

import jrkim.mandarindb.LogInfo;
import jrkim.mandarindb.MainActivity;

/**
 * Created by Jinryul on 15. 7. 17..
 */
public class DBManager {
    private static DBManager mInstance = null;
    private static SQLiteDatabase mDB = null;
    private static DBHelper mHelper = null;
    private Context mContext = null;

    private final static int DB_VERSION = 1;

    public final static int TABLE_MANDARIN      = 1;
    public final static int TABLE_CHINESE       = 2;
    public final static int TABLE_JAPANESE      = 3;


    // 검색
    public final static int TYPE_GANJA          = 10;   // 간자체
    public final static int TYPE_BUNJA          = 11;   // 번자체
    public final static int TYPE_YAKJA          = 14;   // 약자체
    public final static int TYPE_PINYIN         = 15;   // 병음
    public final static int TYPE_RAWPINYIN      = 16;   // 병음 (성조 무시)
    public final static int TYPE_KOREAN         = 17;   // 한국어 음
    public final static int TYPE_JAPANESE       = 18;   // 일본어
    public final static int TYPE_LEARNING       = 19;   // 학습진행 상황

    private class DBHelper extends SQLiteOpenHelper {
        public DBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, factory, version);
        }
        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(DBConsts._CREATE_MANDARIN);
            db.execSQL(DBConsts._CREATE_CHINESE);
            db.execSQL(DBConsts._CREATE_JAPANESE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXIST " + DBConsts._TABLE_MANDARIN);
            db.execSQL("DROP TABLE IF EXIST " + DBConsts._CREATE_CHINESE);
            db.execSQL("DROP TABLE IF EXIST " + DBConsts._CREATE_JAPANESE);
            onCreate(db);
        }
    }

    public static DBManager getInstance(Context context) {
        synchronized (DBManager.class) {
            if(mInstance == null)           mInstance = new DBManager(context);
        }
        return mInstance;
    }

    private DBManager(Context context) {
        mContext = context;
    }

    public void open() throws SQLException {
        if(mDB == null) {
            mHelper = new DBHelper(mContext, DBConsts.DB_NAME, null, DB_VERSION);
            mDB = mHelper.getWritableDatabase();
        }
    }

    public void close() {
        if(mDB != null) {
            mDB.close();
            mDB = null;
        }
    }

    private String getTableName(int table) {
        String strTableName = null;
        switch(table) {
            case TABLE_MANDARIN:        strTableName = DBConsts._TABLE_MANDARIN;        break;
            case TABLE_CHINESE:         strTableName = DBConsts._TABLE_CHINESE;         break;
            case TABLE_JAPANESE:        strTableName = DBConsts._TABLE_JAPANESE;        break;
        }
        return strTableName;
    }

    private String getTypeName(int type) {
        String strTypeName = null;
        switch(type) {
            case TYPE_GANJA:        strTypeName = DBConsts._GANJA;      break;
            case TYPE_BUNJA:        strTypeName = DBConsts._BUNJA;      break;
            case TYPE_YAKJA:        strTypeName = DBConsts._YAKJA;      break;
            case TYPE_PINYIN:       strTypeName = DBConsts._PINYIN;     break;
            case TYPE_RAWPINYIN:    strTypeName = DBConsts._RAWPINYIN;  break;
            case TYPE_KOREAN:       strTypeName = DBConsts._KOREAN;     break;
            case TYPE_JAPANESE:     strTypeName = DBConsts._JAPANESE;   break;
            case TYPE_LEARNING:     strTypeName = DBConsts._LEARNING;   break;
        }
        return strTypeName;
    }

    public Cursor get(int table, int type, String [] item) {
        return mDB.rawQuery("SELECT * FROM " + getTableName(table) + " WHERE " + getTypeName(type) + " = ?", item);
    }

    public Cursor getAll(int table) {
        return mDB.query(getTableName(table), null, null, null, null, null, null);
    }

    public boolean isExistInDB(int table, int type, String [] item) {
        boolean bRet = false;
        Cursor cursor = get(table, type, item);
        if(cursor.getCount() > 0) bRet = true;
        cursor.close();
        return bRet;
    }

    public boolean insertMandarin(Handler handler, String ganja, String bunja, String yakja,
                                  String pinyin, String rawpinyin, String korean, String japanese, String meaning,
                                  int lvHanja, int lvHSK, int lvJLPT) {
        if(isExistInDB(TABLE_MANDARIN, TYPE_BUNJA, new String [] {bunja})) {
            handler.sendMessage(handler.obtainMessage(MainActivity.MESSAGE_LOG, LogInfo.TYPE_ERROR, 0, "한자 입력 중... \"" + bunja + "\"는 이미 DB에 있습니다."));
            return false;
        }

        ContentValues values = new ContentValues();
        values.put(DBConsts._GANJA, ganja);
        values.put(DBConsts._BUNJA, bunja);
        values.put(DBConsts._YAKJA, yakja);
        values.put(DBConsts._PINYIN, pinyin);
        values.put(DBConsts._RAWPINYIN, rawpinyin);
        values.put(DBConsts._KOREAN, korean);
        values.put(DBConsts._JAPANESE, japanese);
        values.put(DBConsts._MEANING, meaning);
        values.put(DBConsts._LEVEL_HANJA, lvHanja);
        values.put(DBConsts._LEVEL_HSK, lvHSK);
        values.put(DBConsts._LEVEL_JLPT, lvJLPT);
        values.put(DBConsts._LEARNING, 0);
        values.put(DBConsts._ANSWERED, "");
        values.put(DBConsts._TESTED, "");
        mDB.insert(DBConsts._TABLE_MANDARIN, null, values);
        return true;
    }

    public boolean insertChinese(Handler handler, String ganja, String pinyin, String rawpinyin, String meaning, int lvHSK) {
        if(isExistInDB(TABLE_CHINESE, TYPE_GANJA, new String [] {ganja})) {
            handler.sendMessage(handler.obtainMessage(MainActivity.MESSAGE_LOG, LogInfo.TYPE_ERROR, 0, "중국어 입력 중... \"" + ganja + "\"는 이미 DB에 있습니다."));
            return false;
        }

        String ch = "";
        Cursor cursor = null;
        int lv = 0;
        String dbPinyin = "";
        for(int i = 0; i < ganja.length(); i++) {
            ch = "" + ganja.charAt(i);
            cursor = get(TABLE_MANDARIN, TYPE_GANJA, new String[] {ch});
            if(cursor.getCount() == 0) {
                cursor.close();
                handler.sendMessage(handler.obtainMessage(MainActivity.MESSAGE_LOG, LogInfo.TYPE_ERROR, 0, "중국어 입력 중... \"" + ch + "\"자는 DB에 없습니다."));
                return false;
            } else {
                while(cursor.moveToNext()) {
                    lv = cursor.getInt(cursor.getColumnIndex(DBConsts._LEVEL_HSK));
                    if(lv != lvHSK) {
                        if(lvHSK < lv) {
                            handler.sendMessage(handler.obtainMessage(MainActivity.MESSAGE_LOG, LogInfo.TYPE_ERROR, 0, "입력하려는 HSK:" + lvHSK + ", 한자 DB에" + ch + "의 레벨은 " + lv + "으로 서로 다릅니다."));
                        } else {
                            handler.sendMessage(handler.obtainMessage(MainActivity.MESSAGE_LOG, LogInfo.TYPE_WARNING, 0, "입력하려는 HSK:" + lvHSK + ", 한자 DB에" + ch + "의 레벨은 " + lv + "으로 서로 다릅니다."));
                        }
                    }

                    // TODO , 병음도 비교해 보자. 우선 db_tbl에서 각 단어별로 병음을 구분해 주어야 한다.
                    //dbPinyin = cursor.getString(cursor.getColumnIndex(DBConsts._PINYIN));

                }
                cursor.close();
            }
        }

        ContentValues values = new ContentValues();
        values.put(DBConsts._GANJA, ganja);
        values.put(DBConsts._PINYIN, pinyin);
        values.put(DBConsts._RAWPINYIN, rawpinyin);
        values.put(DBConsts._MEANING, meaning);
        values.put(DBConsts._LEVEL_HSK, lvHSK);
        values.put(DBConsts._LEARNING, 0);
        values.put(DBConsts._TESTED, "");
        values.put(DBConsts._ANSWERED, "");
        mDB.insert(DBConsts._TABLE_CHINESE, null, values);
        return true;
    }


    public boolean insertJapanese(Handler handler, String yakja, String japanese, String meaning, int lvJLPT) {
        if(isExistInDB(TABLE_JAPANESE, TYPE_YAKJA, new String [] {yakja})) {
            handler.sendMessage(handler.obtainMessage(MainActivity.MESSAGE_LOG, LogInfo.TYPE_ERROR, 0, "일본어 \"" + yakja + "\"는 이미 DB에 있습니다."));
            return false;
        }

        String ch = "";
        Cursor cursor = null;
        int lv = 0;
        for(int i = 0; i < yakja.length(); i++) {
            ch = "" + yakja.charAt(i);
            cursor = get(TABLE_MANDARIN, TYPE_YAKJA, new String[] {ch});
            if(cursor.getCount() == 0) {
                cursor.close();
                handler.sendMessage(handler.obtainMessage(MainActivity.MESSAGE_LOG, LogInfo.TYPE_ERROR, 0, "일본어 입력 중... \"" + ch + "\"자는 DB에 없습니다."));
                return false;
            } else {
                while(cursor.moveToNext()) {
                    lv = cursor.getInt(cursor.getColumnIndex(DBConsts._LEVEL_JLPT));
                    if(lv != lvJLPT) {
                        if(lv > lvJLPT)
                            handler.sendMessage(handler.obtainMessage(MainActivity.MESSAGE_LOG, LogInfo.TYPE_ERROR, 0, "입력하려는 JLPT:" + lvJLPT + ", 한자 DB에" + ch + "의 레벨은 " + lv + "으로 서로 다릅니다."));
                        else
                            handler.sendMessage(handler.obtainMessage(MainActivity.MESSAGE_LOG, LogInfo.TYPE_WARNING, 0, "입력하려는 JLPT:" + lvJLPT + ", 한자 DB에" + ch + "의 레벨은 " + lv + "으로 서로 다릅니다."));
                    }
                }
                cursor.close();
            }
        }

        ContentValues values = new ContentValues();
        values.put(DBConsts._YAKJA, yakja);
        values.put(DBConsts._JAPANESE, japanese);
        values.put(DBConsts._MEANING, meaning);
        values.put(DBConsts._LEVEL_JLPT, lvJLPT);
        values.put(DBConsts._LEARNING, 0);
        values.put(DBConsts._TESTED, "");
        values.put(DBConsts._ANSWERED, "");
        mDB.insert(DBConsts._TABLE_JAPANESE, null, values);
        return true;
    }

    public boolean delete(int table, int type, String [] item) {
        return mDB.delete(getTableName(table), getTypeName(type) + " = ?", item) > 0;
    }
}
