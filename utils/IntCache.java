package lemons.api.utils;

public class IntCache {
	private static final int MIN_CACHE = 0;
	private static final int MAX_CACHE = 20000;
	private static Integer[] intCache = new Integer[MAX_CACHE - MIN_CACHE + 1];
	
	public static Integer valueOf(int val)
	  {
	    if (val < MIN_CACHE || val > MAX_CACHE)
	        return new Integer(val);
	    synchronized (intCache)
	      {
		if (intCache[val - MIN_CACHE] == null)
		    intCache[val - MIN_CACHE] = new Integer(val);
		return intCache[val - MIN_CACHE];
	      }
	  }
}
