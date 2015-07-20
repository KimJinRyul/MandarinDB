package jrkim.mandarindb;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;

/**
 * Created by Jinryul on 15. 7. 20..
 */
public class LogAdapter extends BaseAdapter {

    private Context mContext = null;
    private ArrayList<LogInfo> mLogs = null;

    public LogAdapter(Context context, ArrayList<LogInfo> logs) {
        mContext = context;
        mLogs = logs;
    }

    @Override
    public int getCount() {
        if(mLogs != null)
            return mLogs.size();
        return 0;
    }

    @Override
    public Object getItem(int i) {
        if(mLogs != null && mLogs.size() > i)
            return mLogs.get(i);
        return null;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        LogInfo log = (LogInfo)getItem(i);
        if(log != null) {
            try {
                if(view == null) {
                    view = new LogView(mContext);
                }

                if(view != null) {
                    LogView lv = (LogView)view;
                    lv.setLog(log);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return view;
    }
}
