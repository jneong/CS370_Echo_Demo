package com.neong.voice.wolfpack;

import java.sql.Date;
import java.sql.Timestamp;

import java.text.SimpleDateFormat;

import java.util.Calendar;

import org.apache.commons.lang3.tuple.ImmutablePair;


public class AmazonDateParser {
	private static final int SUNDAY = 1;


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


	public static ImmutablePair<Date, Date> handleDay(final String date){
		final Timestamp firstDay = Timestamp.valueOf(date + " 00:00:00");
		final Date begin = new Date(firstDay.getTime());

		final Calendar calendar = Calendar.getInstance();
		calendar.setTime(begin);
		calendar.add(Calendar.DATE, 1);

		final Date end = calendarToDate(calendar);

		return ImmutablePair.of(begin, end);
	}


	public static ImmutablePair<Date, Date> handleWeek(final String date){
		final String[] pieces = date.split("-W");
		final int year = Integer.parseInt(pieces[0]);
		int weekNum = Integer.parseInt(pieces[1]);

		if (todayNumber() == SUNDAY)
			weekNum++;

		final Calendar calendar = Calendar.getInstance();
		calendar.setMinimalDaysInFirstWeek(4);
		calendar.set(Calendar.YEAR, year);
		calendar.set(Calendar.WEEK_OF_YEAR, weekNum);
		calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);

		final Date begin = calendarToDate(calendar);
		calendar.add(Calendar.DATE, 7);
		final Date end = calendarToDate(calendar);

		return ImmutablePair.of(begin, end);
	}


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
