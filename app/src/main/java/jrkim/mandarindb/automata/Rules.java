package jrkim.mandarindb.automata;

import java.util.ArrayList;

/**
 * Created by Jinryul on 15. 7. 7..
 */
public class Rules {
    public ArrayList<Rule> arrRules = new ArrayList<Rule>();
    private final static String CHANGE = ">";
    private final static String DETERMINE = "/";
    public class Rule {
        public String strOrigin;
        public String strReplace;
        public String strRule;

        public Rule(String str) {
            strOrigin = str.substring(0, str.indexOf(CHANGE));
            str = str.substring(str.indexOf(CHANGE) + 1);
            strReplace = str.substring(0, str.indexOf(DETERMINE));
            str = str.substring(str.indexOf(DETERMINE) + 1);
            strRule = str;
        }
    }

    public void encode(String str) {
        arrRules.add(new Rule(str));
    }
}