package jrkim.mandarindb;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

import jrkim.mandarindb.DB.DBConsts;
import jrkim.mandarindb.DB.DBManager;
import jrkim.mandarindb.automata.AutoMataPinyin;

public class MainActivity extends Activity implements View.OnClickListener{

    private final static int KOREAN = 0;
    private final static int CHINESE = 1;
    private final static int JAPANESE = 2;

    public final static int MESSAGE_COMPLETE    = 1000;
    public final static int MESSAGE_LOG         = 1001;
    public final static int MESSAGE_MAKE_NEWDB  = 1002;

    private ListView mListView = null;
    private LogAdapter mAdapter = null;
    private ArrayList<LogInfo> mLogs = new ArrayList<LogInfo>();

    public static MakeDBHandler mHandler = null;
    private static class MakeDBHandler extends Handler {
        private static WeakReference<MainActivity> mActivity = null;
        public MakeDBHandler (MainActivity activity) {
            mActivity = new WeakReference<MainActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            MainActivity activity = mActivity.get();
            if(activity != null)
                activity.handleMessage(msg);
        }
    }

    public void handleMessage(Message msg) {
        switch(msg.what) {
            case MESSAGE_MAKE_NEWDB:
                makeNewDB();
                break;
            case MESSAGE_COMPLETE:
            findViewById(R.id.btnMakeDB).setEnabled(true);
            findViewById(R.id.btnGetDB).setEnabled(true);
                updateText();
            break;
            case MESSAGE_LOG:
                mLogs.add(new LogInfo((String) msg.obj, msg.arg1));
                mAdapter.notifyDataSetChanged();
                mListView.setSelection(mLogs.size() - 1);
                switch(msg.arg1) {
                    case LogInfo.TYPE_DEFAULT:
                    case LogInfo.TYPE_INFORMATION:
                        Log.i("jrkim", (String)msg.obj);
                        break;
                    case LogInfo.TYPE_WARNING:
                        Log.w("jrkim", (String)msg.obj);
                        break;
                    case LogInfo.TYPE_ERROR:
                        Log.e("jrkim", (String)msg.obj);
                        break;
                }

                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mHandler = new MakeDBHandler(this);

        findViewById(R.id.btnMakeDB).setOnClickListener(this);
        findViewById(R.id.btnGetDB).setOnClickListener(this);
        findViewById(R.id.btnDeleteLog).setOnClickListener(this);

        mListView = (ListView)findViewById(R.id.lvLogs);
        mAdapter = new LogAdapter(getApplicationContext(), mLogs);
        mListView.setAdapter(mAdapter);

        updateText();
    }

    private void updateText() {
        DBManager db = DBManager.getInstance(this);
        db.open();

        Cursor mandarinCursor = db.getAll(DBManager.TABLE_MANDARIN);
        Cursor chineseCursor = db.getAll(DBManager.TABLE_CHINESE);
        Cursor japaneseCursor = db.getAll(DBManager.TABLE_JAPANESE);

        ((TextView)findViewById(R.id.tvHanjaCnt)).setText(String.format(getString(R.string.HANJA_CNT), mandarinCursor.getCount()));
        ((TextView)findViewById(R.id.tvChineseCnt)).setText(String.format(getString(R.string.HSK_CNT), chineseCursor.getCount()));
        ((TextView)findViewById(R.id.tvJapaneseCnt)).setText(String.format(getString(R.string.JLPT_CNT), japaneseCursor.getCount()));

        mandarinCursor.close();
        chineseCursor.close();
        japaneseCursor.close();

        db.close();
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.btnDeleteLog:
                mLogs.clear();
                mAdapter.notifyDataSetChanged();
                break;
            case R.id.btnMakeDB:
                mHandler.sendEmptyMessage(MESSAGE_MAKE_NEWDB);
                break;
            case R.id.btnGetDB:
                try {
                    File dbFile = getDatabasePath(DBConsts.DB_NAME);
                    if (dbFile.exists()) {
                        mHandler.sendMessage(mHandler.obtainMessage(MESSAGE_LOG, LogInfo.TYPE_DEFAULT, 0, "생성된 DB 존재."));
                        mHandler.sendMessage(mHandler.obtainMessage(MESSAGE_LOG, LogInfo.TYPE_INFORMATION, 0, "size : " + dbFile.length()));

                        File fDir = Environment.getExternalStorageDirectory();
                        File outputDB = new File(fDir.getAbsolutePath() + "/" + DBConsts.DB_NAME);
                        if(!outputDB.exists())
                            outputDB.createNewFile();

                        mHandler.sendMessage(mHandler.obtainMessage(MESSAGE_LOG, LogInfo.TYPE_DEFAULT, 0, "DB 출력 : " + outputDB.getPath()));

                        FileInputStream fis = new FileInputStream(dbFile);
                        FileOutputStream fos = new FileOutputStream(outputDB);
                        byte [] buffer = new byte[1024];
                        int read = 0;
                        while((read = fis.read(buffer)) > 0) {
                            fos.write(buffer, 0, read);
                        }

                        fis.close();
                        fos.close();
                        mHandler.sendMessage(mHandler.obtainMessage(MESSAGE_LOG, LogInfo.TYPE_DEFAULT, 0, "DB 출력 완료"));

                    } else {
                        mHandler.sendMessage(mHandler.obtainMessage(MESSAGE_LOG, LogInfo.TYPE_ERROR, 0, "생성된 DB가 없음."));
                    }
                    break;
                } catch (Exception e) {
                    e.printStackTrace();
                }
        }
    }

    private void makeNewDB() {
        File fp = getDatabasePath(DBConsts.DB_NAME);
        if(fp.exists()) {
            fp.delete();
        }

        findViewById(R.id.btnMakeDB).setEnabled(false);
        findViewById(R.id.btnGetDB).setEnabled(false);

        new Thread() {
            @Override
            public void run() {
                try {
                    mHandler.sendMessage(mHandler.obtainMessage(MESSAGE_LOG, LogInfo.TYPE_INFORMATION, 0, "한자 DB 생성 시작.."));
                    AutoMataPinyin amPinyin = AutoMataPinyin.getInstance(MainActivity.this);
                    DBManager db = DBManager.getInstance(MainActivity.this);
                    db.open();

                    BufferedReader br = new BufferedReader(new InputStreamReader(getAssets().open("dbinsert.tbl")));
                    String line = "";
                    int count = 0;
                    String ganja, bunja, yakja, pinyin, rawpinyin, korea, japan, meaning;
                    int lvHanja, lvHSK, lvJLPT;
                    String [] languages;
                    while((line = br.readLine()) != null) {

                        line = line.trim();
                        if(line.indexOf("#") >= 0)      line = line.substring(0, line.indexOf("#")).trim();
                        if(line.length() <= 10)          continue;      // 10자 미만이면 머가 됐든 이상한 값임

                        languages = line.split(":");
                        if(languages.length != 3) {
                            mHandler.sendMessage(mHandler.obtainMessage(MESSAGE_LOG, LogInfo.TYPE_ERROR, 0, line + "에 3가지 언어가 모두 포함되어 있지 않은 것 같습니다."));
                            break;
                        } else {
                            String [] korean = languages[KOREAN].trim().split("\\|");
                            if(korean.length != 3) {
                                mHandler.sendMessage(mHandler.obtainMessage(MESSAGE_LOG, LogInfo.TYPE_ERROR, 0, "번자 정보가 3가지 모두 포함되어 있지 않습니다." + languages[KOREAN]));
                                break;
                            }

                            String [] chinese = languages[CHINESE].trim().split("\\|");
                            if(korean.length != 3) {
                                mHandler.sendMessage(mHandler.obtainMessage(MESSAGE_LOG, LogInfo.TYPE_ERROR, 0, "간자 정보가 3가지 모두 포함되어 있지 않습니다." + languages[CHINESE]));
                                break;
                            }

                            String [] japanese = languages[JAPANESE].trim().split("\\|");
                            if(korean.length != 3) {
                                mHandler.sendMessage(mHandler.obtainMessage(MESSAGE_LOG, LogInfo.TYPE_ERROR, 0, "약자 정보가 3가지 모두 포함되어 있지 않습니다." + languages[JAPANESE]));
                                break;
                            }

                            ganja = chinese[0];
                            bunja = korean[0];
                            yakja = japanese[0];
                            pinyin = chinese[1];

                            if(pinyin.indexOf(",") > 0) {
                                String [] pinyins = pinyin.split(",");
                                pinyin = "";
                                for(int i = 0; i < pinyins.length; i++) {
                                    pinyin += amPinyin.change(pinyins[i]);
                                    if(i < pinyins.length - 1) {
                                        pinyin += ",";
                                    }
                                }
                            } else {
                                pinyin = amPinyin.change(pinyin);
                            }

                            rawpinyin = amPinyin.getRawPinyin(pinyin);

                            korea = korean[1].split("/")[1];
                            japan = japanese[1];
                            meaning = korean[1].split("/")[0];
                            lvHanja = Integer.parseInt(korean[2]);
                            lvHSK = Integer.parseInt(chinese[2]);
                            lvJLPT = Integer.parseInt(japanese[2]);

                            mHandler.sendMessage(mHandler.obtainMessage(MESSAGE_LOG, LogInfo.TYPE_DEFAULT, 0, "DB입력:" + bunja + ganja + yakja + ":" + pinyin + "(" + rawpinyin + ")" + ":" + lvHanja + lvHSK + lvJLPT + ":" + meaning + "_" + korea));
                            db.insertMandarin(mHandler, ganja, bunja, yakja, pinyin, rawpinyin, korea, japan, meaning, lvHanja, lvHSK, lvJLPT);
                        }
                    }

                    br.close();
                    mHandler.sendMessage(mHandler.obtainMessage(MESSAGE_LOG, LogInfo.TYPE_INFORMATION, 0, "한자 DB 생성 완료.."));

                    br = new BufferedReader(new InputStreamReader(getAssets().open("dbinsert_chinese.tbl")));
                    mHandler.sendMessage(mHandler.obtainMessage(MESSAGE_LOG, LogInfo.TYPE_INFORMATION, 0, "중국어 DB 생성 완료.."));
                    int level = 1;
                    String [] pinyins;
                    while ((line = br.readLine()) != null) {
                        line = line.trim();
                        int index = line.indexOf("#");
                        if (index >= 0)         line = line.substring(0, index).trim();
                        if (line.length() <= 0) continue;

                        if (line.charAt(0) == '!') {
                            level = Integer.parseInt("" + line.charAt(1));
                            continue;
                        }

                        languages = line.split("\\|");
                        if (languages.length != 3) {
                            mHandler.sendMessage(mHandler.obtainMessage(MESSAGE_LOG, LogInfo.TYPE_ERROR, 0, line + " is not have 3 items"));
                            break;
                        }

                        ganja = languages[0];
                        pinyins = languages[1].split(",");
                        pinyin = "";
                        for(String item : pinyins) {
                            pinyin += amPinyin.change(item);
                        }

                        rawpinyin = amPinyin.getRawPinyin(pinyin);
                        meaning = languages[2];

                        mHandler.sendMessage(mHandler.obtainMessage(MESSAGE_LOG, LogInfo.TYPE_DEFAULT, 0, "DB입력:" + ganja + "-" + pinyin + "(" + rawpinyin + ")" + meaning + "-Lv" + level));
                        db.insertChinese(mHandler, ganja, pinyin, rawpinyin, meaning, level);
                    }
                    br.close();
                    db.close();

                    mHandler.sendMessage(mHandler.obtainMessage(MESSAGE_LOG, LogInfo.TYPE_INFORMATION, 0, "중국어 DB 생성 완료.."));
                } catch (Exception e) {
                    mHandler.sendMessage(mHandler.obtainMessage(MESSAGE_LOG, LogInfo.TYPE_ERROR, 0, e.getMessage()));
                    e.printStackTrace();
                } finally {
                    mHandler.sendEmptyMessage(MESSAGE_COMPLETE);
                }
            }
        }.start();
    }
}
