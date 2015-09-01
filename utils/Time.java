package lemons.api.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;


public class Time {

	private final static DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	private static long time;
	private static String seconds;
	private static String minutes;
	private static String hours;
	private static String days;
	private static Date date;

	/**
	 * Parse Time from Long to String
	 *
	 * @param millis
	 * @return String of Parsed time long
	 */
	public static String parse(long millis) {
		time = millis / 1000;
		seconds = Integer.toString((int) (time % 60));
		minutes = Integer.toString((int) ((time % 3600) / 60));
		hours = Integer.toString((int) (time / 3600));
		days = Integer.toString((int) (time / (3600 * 24)));

		for (int i = 0; i < 5; i++) {
			if (Integer.parseInt(hours) >= 24) {
				hours = Integer.parseInt(hours) - 24 + "";
			}
		}

		for (int i = 0; i < 2; i++) {
			if (seconds.length() < 2)
				seconds = "0" + seconds;
			if (minutes.length() < 2)
				minutes = "0" + minutes;
			if (hours.length() < 2)
				hours = "0" + hours;
		}

		if (Integer.parseInt(days) > 0)
			return days + ":" + hours + ":" + minutes + ":" + seconds;

		return hours + ":" + minutes + ":" + seconds;
	}

	/**
	 * Get the current formated Date.
	 *
	 * @return String
	 */
	public static String getDate() {
		date = new Date();
		return dateFormat.format(date);
	}

	/**
	 * Get the time stamp.
	 *
	 * @return String
	 */
	public static String getTimeStamp() {
		return getDate().split(" ")[1];
	}
}
