package com.neong.voice.wolfpack;

import java.sql.Date;
import java.sql.Timestamp;

import java.text.SimpleDateFormat;

import java.util.Calendar;

import org.apache.commons.lang3.tuple.ImmutablePair;


public class AmazonDateParser {
	private static final int SUNDAY = 1;

	/**
	 * Parses the givenDate and comes up with two different dates related to the
	 * String. The first date should be the first day of the given range. The
	 * second date will be the date after the last day of the range.
	 * 
	 * @param givenDate	An ISO 8601 formatted string representing a date or range
	 * 					of dates from Amazon.
	 */
	public static ImmutablePair<Date, Date> parseAmazonDate(final String givenDate) {
		final ImmutablePair<Date, Date> result;

		if (givenDate.contains("X")) {
			// TODO: handle decade
			result = null;
		} else if (givenDate.length() == 4) {
			// TODO: handle year
			result = null;
		} else if (givenDate.contains("WI") || givenDate.contains("SP") ||
		           givenDate.contains("SU") || givenDate.contains("FA")) {
			// TODO: handle season
			result = null;
		} else if (givenDate.length() == 7) {
			result = handleMonth(givenDate);
		} else if (givenDate.contains("WE")) {
			// TODO: handle weekend
			result = null;
		} else if (givenDate.contains("W")) {
			result = handleWeek(givenDate);
		} else {
			result = handleDay(givenDate);
		}

		return result;
	}


	/**
	 * @param date	A string representing a date, formatted as yyyy-MM-dd
	 * @return 		The first date object returned is the date represented
	 * 				by the string. The second date is the next day.
	 */
	public static ImmutablePair<Date, Date> handleDay(final String date){
		final Timestamp firstDay = Timestamp.valueOf(date + " 00:00:00");
		final Date begin = new Date(firstDay.getTime());

		final Calendar calendar = Calendar.getInstance();
		calendar.setTime(begin);
		calendar.add(Calendar.DATE, 1);

		final Date end = calendarToDate(calendar);

		return ImmutablePair.of(begin, end);
	}


	/**
	 * Given a string representing a week from Amazon, the function will get the
	 * Sunday at the beginning of that week. It is assumed that the first week
	 * of the year is the first one that contains a Thursday. If the method is
	 * called on a Sunday, (as in the user asks on a Sunday), the method will
	 * look for the upcoming week instead of the previous week like regular ISO
	 * 8601 would require.
	 * 
	 * @param date	A string representing a date, formatted as yyyy-Wnum.
	 * 				Example: 2016-W44
	 * @return	The first date object will be the Sunday of the week. The second
	 * 			date will be the Sunday after the given week.
	 */
	public static ImmutablePair<Date, Date> handleWeek(final String date){
		final String[] pieces = date.split("-W");
		final int year = Integer.parseInt(pieces[0]);
		int weekNum = Integer.parseInt(pieces[1]);

		if (todayNumber() == SUNDAY)
			weekNum++;

		final Calendar calendar = Calendar.getInstance();
		calendar.setFirstDayOfWeek(Calendar.SUNDAY);
		calendar.setMinimalDaysInFirstWeek(3);
		calendar.set(Calendar.YEAR, year);
		calendar.set(Calendar.WEEK_OF_YEAR, weekNum);
		calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);

		final Date begin = calendarToDate(calendar);
		calendar.add(Calendar.DATE, 7);
		final Date end = calendarToDate(calendar);

		return ImmutablePair.of(begin, end);
	}


	/**
	 * @param date	A string representing a date in the format of yyyy-MM
	 * @return		The first date will be the first day of the given month.
	 * 				The second date will be the first date of the next month.
	 */
	public static ImmutablePair<Date, Date> handleMonth(final String date){
		final String[] pieces = date.split("-");
		final int year = Integer.parseInt(pieces[0]);
		final int monthNum = Integer.parseInt(pieces[1]);

		final String formattedDate = String.format("%d-%d-01 00:00:00", year, monthNum);
		final Timestamp firstDay = Timestamp.valueOf(formattedDate);
		final Date begin = new Date(firstDay.getTime());

		final Calendar calendar = Calendar.getInstance();
		calendar.setTime(begin);
		calendar.add(Calendar.MONTH, 1);

		final Date end = calendarToDate(calendar);

		return ImmutablePair.of(begin, end);
	}

	
	/**
	 * @return The day number of the week. Sunday is 1 and Saturday is 6.
	 */
	private static int todayNumber(){
		final Calendar today = Calendar.getInstance();
		final int dayOfWeek = today.get(Calendar.DAY_OF_WEEK);

		return dayOfWeek;
	}

	
	private static Date calendarToDate(final Calendar calendar) {
		// Calendar -> java.util.Date -> long
		final long time = calendar.getTime().getTime();

		return new Date(time);
	}
}
