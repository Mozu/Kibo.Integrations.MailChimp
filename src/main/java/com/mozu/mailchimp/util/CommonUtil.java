package com.mozu.mailchimp.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mozu.jobs.models.SkipItems;

/**
 * Common utility class
 * 
 * @author Amit
 */

public class CommonUtil {

	private static final Logger logger = LoggerFactory
			.getLogger(CommonUtil.class);

	public static String convertLocalTimeToUTC(TimeZone defaultTimeZone,
			String p_localDateTime) throws Exception {

		String dateFormateInUTC = "";
		Date localDate = null;
		SimpleDateFormat formatter;
		SimpleDateFormat parser;
		parser = new SimpleDateFormat("EE MMM dd HH:mm:ss z yyyy",
				Locale.ENGLISH);
		parser.setTimeZone(defaultTimeZone);
		localDate = parser.parse(p_localDateTime);
		formatter = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss");
		formatter.setTimeZone(defaultTimeZone);
		formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
		dateFormateInUTC = formatter.format(localDate);
		final SimpleDateFormat sdf = new SimpleDateFormat(
				"dd-MMM-yyyy HH:mm:ss", Locale.US);
		Date dt = sdf.parse(dateFormateInUTC);
		return dateFormateInUTC;
	}

	public static String getDDMonYYYY(Date d) {
		SimpleDateFormat formatter = new SimpleDateFormat(
				"dd-MMM-yyyy HH:mm:ss");
		String date = formatter.format(d);
		return date;

	}

}
