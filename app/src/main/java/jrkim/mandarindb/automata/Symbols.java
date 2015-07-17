package jrkim.mandarindb.automata;

import android.util.Log;

import java.util.ArrayList;

/**
 * Created by Jinryul on 15. 7. 6..
 */
public class Symbols {
    public final static String BEGIN = "^";
    public final static String END = "$";
    public ArrayList<Symbol> arrSymbols = new ArrayList<Symbol>();
    private class Symbol {
        private char symbol;
        private ArrayList<Character> arrElements = new ArrayList<Character>();
        public Symbol(String str) {
            symbol = str.charAt(0);
            str = str.substring(2);
            for(int i = 0; i < str.length(); i++) {
                arrElements.add(str.charAt(i));
            }
        }
    }

    public void encode(String str) {
        arrSymbols.add(new Symbol(str));
    }

    /**
     * ch가 symbol의 element인지 확인한다.
     * @param symbol
     * @param ch
     * @return
     */
    public boolean isElementOfSymbol(char symbol, char ch) {
        for(int i = 0; i < arrSymbols.size(); i++) {
            if(arrSymbols.get(i).symbol == symbol) {
                for(int j = 0; j < arrSymbols.get(i).arrElements.size(); j++) {
                    if(arrSymbols.get(i).arrElements.get(j) == ch)
                        return true;
                }
            }
        }
        return false;
    }

    /**
     * symbol이 실제 symbol중 하나인지 체크
     * @param symbol
     * @return
     */
    public boolean isSymbol(char symbol) {
        for(int i = 0; i < arrSymbols.size(); i++) {
            if(arrSymbols.get(i).symbol == symbol)
                return true;
        }
        return false;
    }
}
