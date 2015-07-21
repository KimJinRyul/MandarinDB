package jrkim.mandarindb.DB;

/**
 * Created by Jinryul on 15. 7. 17..
 */
public class DBConsts {
    public final static String DB_NAME = "jrkim_mandarindb";

    public final static String _ID = "_id";
    public final static String _GANJA = "_ganja";
    public final static String _BUNJA = "_bunja";
    public final static String _YAKJA = "_yakja";
    public final static String _PINYIN = "_pinyin";
    public final static String _RAWPINYIN = "_rawpinyin";           // 성조를 뺀 병음
    public final static String _KOREAN = "_korean";
    public final static String _JAPANESE = "_japanese";
    public final static String _MEANING = "_meaning";
    public final static String _TESTED  = "_tested";
    public final static String _ANSWERED = "_answered";
    public final static String _LEVEL_HANJA   = "_level_hanja";     // 8(50), 7(150), 6(300), 5(500), 4(1000), 3(1800), 2(2355), 1(3500), 특급(5978)
    public final static String _LEVEL_HSK    = "_level_hsk";        // hsk1, hsk2, hsk3, hsk4, hsk5, hsk6
    public final static String _LEVEL_JLPT  = "_level_jlpt";        // n5, n4, n3, n2, n1
    public final static String _LEARNING = "_learning_state";       // 0 : 익힌적 없음, 1 : 익히는 도중, 5: 완료

    // table for Mandarin, for character
    public final static String _TABLE_MANDARIN = "_table_mandarin";
    public final static String _CREATE_MANDARIN =
            "create table " + _TABLE_MANDARIN + "(" + _ID + " integer primary key autoincrement, " +
                    _GANJA + " text not null, " +
                    _BUNJA + " text not null, " +
                    _YAKJA + " text not null, " +
                    _PINYIN + " text not null, " +
                    _RAWPINYIN + " text not null, " +
                    _KOREAN + " text not null, " +
                    _JAPANESE + " text not null, " +
                    _MEANING + " text not null, " +
                    _LEVEL_HANJA + " integer not null, " +
                    _LEVEL_HSK + " integer not null, " +
                    _LEVEL_JLPT + " integer not null, " +
                    _LEARNING + " integer not null, " +
                    _TESTED + " text not null, " +
                    _ANSWERED + " text not null);";

    // table for Chinese Words
    public final static String _TABLE_CHINESE = "_table_chinese";
    public final static String _CREATE_CHINESE =
            "create table " + _TABLE_CHINESE + "(" + _ID + " integer primary key autoincrement, " +
                    _GANJA + " text not null, " +
                    _PINYIN + " text not null, " +
                    _RAWPINYIN + " text not null, " +
                    _MEANING + " text not null, " +
                    _LEVEL_HSK + " integer not null, " +
                    _LEARNING + " integer not null, " +
                    _TESTED + " text not null, " +
                    _ANSWERED + " text not null);";

    // table for Japanese Words
    public final static String _TABLE_JAPANESE = "_table_japanese";
    public final static String _CREATE_JAPANESE =
            "create table " + _TABLE_JAPANESE + "(" + _ID + " integer primary key autoincrement, " +
                    _YAKJA + " text not null, " +
                    _JAPANESE + " text not null, " +
                    _MEANING + " text not null, " +
                    _LEVEL_JLPT + " integer not null, " +
                    _LEARNING + " integer not null, " +
                    _TESTED + " text not null, " +
                    _ANSWERED + " text not null);";

    /*
    // table for User Custom Hanja Dictionary
    public final static String _TABLE_USERMANDARIN = "_table_usermandarin";
    public final static String _CREATE_USERMANDARIN =
            "create table " + _TABLE_USERMANDARIN + "(" + _ID + " integer primary key autoincrement, " +
                    _BUNJA + " text not null, " +
                    _KOREAN + " text not null, " +
                    _MEANING + " text not null, " +
                    _LEVEL_HANJA + " integer not null, " +
                    _LEARNING + " integer not null, " +
                    _TESTED + " text not null, " +
                    _ANSWERED + " text not null);";

    // table for User Custom Chinese Dictionary
    public final static String _TABLE_USERCHINESE = "_table_userchinese";
    public final static String _CREATE_USERCHINESE =
            "create table " + _TABLE_USERCHINESE + "(" + _ID + " integer primary key autoincrement, " +
                    _GANJA + " text not null, " +
                    _PINYIN + " text not null, " +
                    _RAWPINYIN + " text not null, " +
                    _MEANING + " text not null, " +
                    _LEVEL_HSK + " integer not null, " +
                    _LEARNING + " integer not null, " +
                    _TESTED + " text not null, " +
                    _ANSWERED + " text not null);";

    // table for User Custom Japanese Dictionary
    public final static String _TABLE_USERJAPANESE = "_table_userjapanese";
    public final static String _CREATE_USERJAPANESE =
            "create table " + _TABLE_USERJAPANESE + "(" + _ID + " integer primary key autoincrement, " +
                    _YAKJA + " text not null, " +
                    _JAPANESE + " text not null, " +
                    _MEANING + " text not null, " +
                    _LEVEL_JLPT + " integer not null, " +
                    _LEARNING + " integer not null, " +
                    _TESTED + " text not null, " +
                    _ANSWERED + " text not null);";
    */
}
