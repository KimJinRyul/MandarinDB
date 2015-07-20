package jrkim.mandarindb;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
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
            case MESSAGE_COMPLETE:
            findViewById(R.id.btnMakeDB).setEnabled(true);
            findViewById(R.id.btnGetDB).setEnabled(true);
            break;
            case MESSAGE_LOG:
                mLogs.add(new LogInfo((String) msg.obj, msg.arg1));
                mAdapter.notifyDataSetChanged();
                mListView.setSelection(mLogs.size() - 1);
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

        mListView = (ListView)findViewById(R.id.lvLogs);
        mAdapter = new LogAdapter(getApplicationContext(), mLogs);
        mListView.setAdapter(mAdapter);
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.btnMakeDB:
                makeNewDB();
                break;
            case R.id.btnGetDB:
                Toast.makeText(this, "GET DB", Toast.LENGTH_SHORT).show();
                break;
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
                    mHandler.sendEmptyMessage(MESSAGE_COMPLETE);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }
}
