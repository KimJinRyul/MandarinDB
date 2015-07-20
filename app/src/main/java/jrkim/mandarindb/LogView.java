package jrkim.mandarindb;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * Created by Jinryul on 15. 7. 20..
 */
public class LogView extends RelativeLayout {
    private Context mContext = null;

    public LogView(Context context) {
        super(context);
        mContext = context;
    }

    public void setLog(LogInfo loginfo) {

        removeAllViewsInLayout();

        LayoutInflater.from(mContext).inflate(R.layout.item_log, this);
        ((TextView)findViewById(R.id.tvLog)).setText(loginfo.log);
        switch(loginfo.type) {
            case LogInfo.TYPE_DEFAULT:
                ((TextView)findViewById(R.id.tvLog)).setTextColor(LogInfo.COLOR_DEFAULT);
                break;
            case LogInfo.TYPE_WARNING:
                ((TextView)findViewById(R.id.tvLog)).setTextColor(LogInfo.COLOR_WARNING);
                break;
            case LogInfo.TYPE_ERROR:
                ((TextView)findViewById(R.id.tvLog)).setTextColor(LogInfo.COLOR_ERROR);
                break;
            case LogInfo.TYPE_INFORMATION:
                ((TextView)findViewById(R.id.tvLog)).setTextColor(LogInfo.COLOR_INFORMATION);
                break;
        }
    }
}
