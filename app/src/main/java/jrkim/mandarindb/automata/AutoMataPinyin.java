package jrkim.mandarindb.automata;

import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.util.Log;

/**
 * Created by Jinryul on 15. 7. 7..
 */
public class AutoMataPinyin {

    private final static String RULE_FILE   = "pinyin.rule";
    private final static String COMMENT     = "!";
    private final static String SYMBOL      = ":";
    private final static String RULE        = "?";
    private static String [] pinyins = {"ā", "á", "ǎ", "à",
                                        "ē", "é", "ě", "è",
                                        "ī", "í", "ǐ", "ì",
                                        "ō", "ó", "ǒ", "ò",
                                        "ū", "ú", "ǔ", "ù",
                                        "ǖ", "ǘ", "ǚ", "ǜ"};
    private final static int A = 0;
    private final static int E = 4;
    private final static int I = 8;
    private final static int O = 12;
    private final static int U = 16;
    private final static int Y = 20;
    private static AutoMataPinyin mInstance = null;
    private Context mContext = null;
    private Symbols mSymbols = null;
    private Rules mRules = null;

    private AutoMataPinyin(Context context) {
        mContext = context;
        init();
    }

    public static AutoMataPinyin getInstance(Context context) {
        synchronized (AutoMataPinyin.class) {
            if(mInstance == null) {
                mInstance = new AutoMataPinyin(context);
            }
         }
        return mInstance;
    }

    private void init()  {
        try {
            mSymbols = new Symbols();
            mRules = new Rules();

            InputStream is = mContext.getAssets().open(RULE_FILE);
            int size = is.available();
            if(size > 0) {
                byte[] buffer = new byte[size];
                is.read(buffer);
                String rule = new String(buffer);
                String [] lines = rule.split(System.getProperty("line.separator"));
                int index;
                for(String line : lines) {
                    index = line.indexOf(COMMENT);
                    if(index >= 0) {
                        line = line.substring(0, line.indexOf(COMMENT));
                    }

                    if(line.length() > 0) {

                        if(SYMBOL.compareTo(line.substring(0, 1)) == 0) {
                            mSymbols.encode(line.substring(1));
                        } else if(RULE.compareTo(line.substring(0, 1)) == 0) {
                            mRules.encode(line.substring(1));
                        } else {

                        }
                    }
                }
            }
            is.close();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    private String removeSongju(String ch) {
        for(int i = 0; i < pinyins.length; i++) {
            if(pinyins[i].compareTo(ch) == 0) {
                if(i < 4)
                    return "a";
                else if(i < 8)
                    return "e";
                else if(i < 12)
                    return "i";
                else if(i < 16)
                    return "o";
                else if(i < 20)
                    return "u";
                else
                    return "ü";
            }
        }
        return ch;
    }

    public String getRawPinyin(String pinyin) {
        char ch = '\0';
        String rawPinyin = "";
        for(int i = 0; i < pinyin.length(); i++) {
            rawPinyin += removeSongju(""+pinyin.charAt(i));
        }
        return rawPinyin;
    }

    public String change(String input) {
        String output = input;

        // Step 1. 전처리, ^ + yu -> ü + $
        output = Symbols.BEGIN + output.replaceAll("yu", "ü") + Symbols.END;

        // Step 2. 성조가 붙은 모음과 성조를 찾아 둔다.
        char chSungjo = '\0';
        int nSungjo = 0;
        for(int i = 0; i < output.length(); i++) {
            if(Character.isDigit(output.charAt(i))) {
                chSungjo = output.charAt(i - 1);
                nSungjo = Integer.parseInt(""+output.charAt(i));
                output = output.substring(0, i) + output.substring(i+1);
                break;
            }
        }

        // Step 3. 룰 적용
        boolean bCompleted = false;      // 룰 발견 완료!
        String strOrigin, strReplace, strRule, strTemp;
        boolean bMatched = false;
        for(int i = 0; i < mRules.arrRules.size(); i++) {

            strOrigin = mRules.arrRules.get(i).strOrigin;
            strReplace = mRules.arrRules.get(i).strReplace;
            strRule = mRules.arrRules.get(i).strRule;
            strTemp = strRule.replaceAll("_", strOrigin);

            for(int j = 0; j <= output.length() - strTemp.length(); j++) {
                bMatched = false;
                if(mSymbols.isSymbol(strTemp.charAt(0)) == true) {
                    if(mSymbols.isElementOfSymbol(strTemp.charAt(0), output.charAt(j)) == true) {
                        bMatched = true;
                    }
                } else if(strTemp.charAt(0) == output.charAt(j)) {
                    bMatched = true;
                }

                if(bMatched == true) {
                    for(int k = 1; k < strTemp.length(); k++) {
                        if(mSymbols.isSymbol(strTemp.charAt(k)) == true) {
                            if(mSymbols.isElementOfSymbol(strTemp.charAt(k), output.charAt(j + k)) == false) {
                                bMatched = false;
                            }
                        } else if(strTemp.charAt(k) != output.charAt(j + k)) {
                            bMatched = false;
                        }
                    }
                    if(bMatched == true) { // 진짜 매칭~~
                        bCompleted = true;
                        output = output.replaceFirst(strOrigin, strReplace);
                    }
                }
                if(bCompleted == true)
                    break;
            }
            if(bCompleted == true)
                break;
        }

        // Step 4. 임시 기호 ^ $ 삭제
        output = output.substring(1);
        output = output.substring(0, output.length() - 1);

        // Step 5. 후처리(성조 적용)
        if(nSungjo > 0) {
            String newP = "";
            char chTemp = '\0';
            for (int i = 0; i < output.length(); i++) {
                chTemp = output.charAt(i);
                if(chTemp == chSungjo || (chSungjo == 'ü' && chTemp == 'u')) {
                    if (chTemp == 'a')           newP = pinyins[A + nSungjo - 1];
                    else if (chTemp == 'e')      newP = pinyins[E + nSungjo - 1];
                    else if (chTemp == 'i')      newP = pinyins[I + nSungjo - 1];
                    else if (chTemp == 'o')      newP = pinyins[O + nSungjo - 1];
                    else if (chTemp == 'u')      newP = pinyins[U + nSungjo - 1];
                    else if (chTemp == 'ü')      newP = pinyins[Y + nSungjo - 1];
                    else    Log.e("jrkim", input + " --> " + chTemp + " can not have shengzo:" + nSungjo);
                    output = output.substring(0, i) + newP + output.substring(i+1);
                    break;
                }
            }
        }

        return output;
    }
}
