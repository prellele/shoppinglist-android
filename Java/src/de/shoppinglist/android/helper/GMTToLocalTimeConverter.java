package de.shoppinglist.android.helper;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class GMTToLocalTimeConverter {

	public static Date convert(Date gmtDate) {
		// get the offset to GMT from localTime, because the timestamp in DB is
		// GMT +0:00
		Calendar calendar = Calendar.getInstance();
		TimeZone timeZone = calendar.getTimeZone();
		int offset = timeZone.getRawOffset();
		if (timeZone.inDaylightTime(new Date())) {
			offset = offset + timeZone.getDSTSavings();
		}
		int offsetHrs = offset / 1000 / 60 / 60;
		int offsetMins = offset / 1000 / 60 % 60;

		calendar.setTime(gmtDate);
		calendar.add(Calendar.HOUR_OF_DAY, offsetHrs);
		calendar.add(Calendar.MINUTE, offsetMins);

		return calendar.getTime();
	}
}
