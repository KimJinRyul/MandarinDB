package jrkim.mandarindb;

import android.graphics.Color;

/**
 * Created by Jinryul on 15. 7. 20..
 */
public class LogInfo {

    public final static int TYPE_DEFAULT        = 1;
    public final static int TYPE_WARNING        = 2;
    public final static int TYPE_ERROR          = 3;
    public final static int TYPE_INFORMATION    = 4;

    public final static int COLOR_DEFAULT = Color.argb(255, 232, 232, 232);
    public final static int COLOR_WARNING = Color.argb(255, 230, 170, 101);
    public final static int COLOR_ERROR = Color.argb(255, 230, 76, 101);
    public final static int COLOR_INFORMATION = Color.argb(255, 18, 170, 235);

    public String log;
    public int type;

    public LogInfo(String log, int type) {
        this.log = log;
        this.type = type;
    }
}
