package lemons.api.utils;

/**
 * Created by Kenneth on 8/6/2014.
 */
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.LinkedHashMap;

import lemons.api.org.json.JSONObject;

public class PriceGrabber {

	private static LinkedHashMap<Integer, Integer> cachedPrice = new LinkedHashMap<Integer, Integer>();
	public static int lookUp(int string) {
		if (string == 995)
			return 1;
		if (!cachedPrice.containsKey(string)) {
			try {
				cachedPrice.put(string, (new PriceGrabber(string)).getAverage());
			} catch (NullPointerException e) {
				// // log.info("Error find price for "+string+": "+e.getMessage());
				cachedPrice.put(string, 0);
			}
			// // log.info("[ItemPrice] Found price of "+string+" to be "+cachedPrice.get(string));
		}
		
		return cachedPrice.get(string);
	}

    private static final String URL_BASE = "https://api.rsbuddy.com/grandExchange?a=guidePrice&i=";
    private int itemId;

    private int average;
    private int low;
    private int high;

    public int getLow() {
        return low;
    }

    public int getHigh() {
        return high;
    }

    public int getAverage() {
        return average;
    }

    public int getId() {
        return itemId;
    }

    public PriceGrabber(final int id) {
        JSONObject price = new JSONObject(getJson(id));
        
        itemId = id;
        if (!price.has("overall")) {
        	low = 0;
        	average = 0;
        	high = 0;
        } else {
	        low = price.getInt("buying");
	        average = price.getInt("overall");
	        high = price.getInt("selling");
        }
    }

    private String getJson(int id) {
        try {
            URL url = new URL(URL_BASE + id);
            URLConnection urlconn = url.openConnection();
            urlconn.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.2; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/29.0.1547.57 Safari/537.36");
            urlconn.setRequestProperty("Connection", "close");
            BufferedReader in = new BufferedReader(new InputStreamReader(urlconn.getInputStream()));
            String s = in.readLine();
            in.close();
            return s;
        } catch(Exception a) {
            System.out.println("Error connecting to server.");
        }
        return null;
    }
}