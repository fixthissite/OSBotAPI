package lemons.api.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class Base64
{
    static char[] b64e = new char[] {  '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 
                       'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 
                       'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 
                       'U', 'V', 'W', 'X', 'Y', 'Z', 
                       'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 
                       'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 
                       'u', 'v', 'w', 'x', 'y', 'z', 
                       '+', '/'};
	private static int w = 0;

    public static String encode(int value) 
    {
    	Boolean debug = false; //w++ % 10000 == 0;
    	String s = ""+value, r = "";
    	
    	int l = BigDecimal.valueOf(s.length())
    				.divide(BigDecimal.valueOf(2), 0, RoundingMode.CEILING)
    				.intValue();
    	for (int i = l; i > 0; i--) {
    		r += b64e[(value & (63 * (int) Math.pow(64, i - 1)) ) >> (6 * (i - 1))];
    	}
    	if (debug)
    		System.out.println("Length is "+s.length()+"("+l+") for "+value+" and outputs "
    				+r+" which decodes to "+decode(r));
    	w = w % 10000;
    	return r;
    	/*
        // length should be between 1 and 5 only
        if (length == 5)
        {
            char[] c = new char[5];
            c[0] = b64e[(value & 1056964608) >> 24];
            c[1] = b64e[(value & 16515072) >> 18];
            c[2] = b64e[(value & 258048) >> 12];
            c[3] = b64e[(value & 4032) >> 06];
            c[4] = b64e[(value & 63)];
            return new String(c);
        }
        else if (length == 4)
        {
            char[] c = new char[4];
            c[0] = b64e[(value & 16515072) >> 18];
            c[1] = b64e[(value & 258048) >> 12];
            c[2] = b64e[(value & 4032) >> 06];
            c[3] = b64e[(value & 63)];
            return new String(c);
        }
        else if (length == 3)
        {
            char[] c = new char[3];
            c[0] = b64e[(value & 258048) >> 12];
            c[1] = b64e[(value & 4032) >> 06];
            c[2] = b64e[(value & 63)];
            return new String(c);
        }
        else if (length == 2)
        {
            char[] c = new char[2];
            c[0] = b64e[(value & 4032) >> 06];
            c[1] = b64e[(value & 63)];
            return new String(c);
        }
        else
        {
            return ""+b64e[(value & 63)];
        }
        */
    }

    public static int decode(String s) {
        int r = 0, l = s.length();
        char[] c = s.toCharArray();
        for (int i = 0; i < l; i++) {
        	for (int a = 0; a < 64; a++) {
        		if (b64e[a] == c[i]) {
        			r += a << (s.length() - i - 1) * 6;
        			break;
        		}
        	}
        }
        return r;
    }
}