package net.gwerder.java.messagevortex.transport;

import java.util.Random;

/**
 * Created by martin.gwerder on 10.03.2016.
 */
public class RandomString {

    private static final char[] symbols;

    static {
        StringBuilder tmp = new StringBuilder();
        for (char ch = '0'; ch <= '9'; ++ch)
            tmp.append(ch);
        for (char ch = 'a'; ch <= 'z'; ++ch)
            tmp.append(ch);
        symbols = tmp.toString().toCharArray();
    }

    private static final Random random = new Random();

    private RandomString() {
        // dummy constructor to overrule the default constructor
    }

    public static String nextString(int length) {
        return nextString(length,new String(symbols));
    }

    public static String nextString(int length,String symbolString) {
        if (length < 1) {
            throw new IllegalArgumentException("length < 1: " + length);
        }
        char[] symbols=symbolString.toCharArray();
        char[] buf = new char[length];
        for (int i = 0; i < buf.length; ++i) {
            buf[i] = symbols[random.nextInt(symbols.length)];
        }
        return new String(buf);
    }
}

