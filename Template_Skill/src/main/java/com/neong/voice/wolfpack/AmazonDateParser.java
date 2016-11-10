package com.neong.voice.wolfpack;

import java.sql.Date;
import java.sql.Timestamp;
import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;

import java.util.Calendar;

import org.apache.commons.lang3.tuple.ImmutablePair;


public class AmazonDateParser {
	private static final int SUNDAY = 1;
	
	private enum rangeType {
		DAY,
		WEEK,
		WEEKEND,
		MONTH
	}
	
	
	private static rangeType findRangeType(String givenDate){
		if (givenDate.length() == 7) {
			return rangeType.MONTH;
		} else if (givenDate.contains("WE")) {
			return rangeType.WEEKEND;
		} else if (givenDate.contains("W")) {
			return rangeType.WEEK;
		} else { //A single day
			return rangeType.DAY;
		}
	}
	
	
	/**
	 * Looks at the givenDate and determines what it is referring to, such as
	 * a day, this week, next week, a certain month, etc.
	 * 
	 * @param givenDate The string provided from the AMAZON.DATE slot.
	 * 
	 * @param usePreposition	Set to true if the string returned should include
	 * 							prepositions before the time range, such as "in"
	 * 							or "on". Set to false if only the time range
	 * 							should be returned.
	 * 
	 * @return 	A short string with a time frame like "December", "next week",
	 * 			"this weekend", etc.
	 */
	public static String timeRange(final String givenDate, boolean usePreposition){
		rangeType range = findRangeType(givenDate);
		String time = "";
		
		switch(range){
			case MONTH:
				if(usePreposition)
					time += "in ";
				return time + getMonth(givenDate);
			case WEEK:
				return time + getRelativeWeek(givenDate);
			case WEEKEND:
				return time + getRelativeWeekend(givenDate);
			case DAY:
				if(usePreposition)
					time += "on ";
				return time + getSingleDay(givenDate);
		}
		return "unknown range";
	}
	

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
			result = handleWeekend(givenDate);
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
	  * @param date	A String representing a weekend from Amazon. The first
	  * 			weekend of the year is the one that is after the first
	  * 			week in the year that has a Thursday.
	  * 			Example: 2010-W03-WE refers to 1/23/10 and 1/24/10.
	  * 
	  * @return The first date will be the Saturday of the specified weekend.
	  * 		The second date will be the Monday after the given weekend.
	  */
	public static ImmutablePair<Date, Date> handleWeekend(final String date){
		final String[] pieces = date.split("-W");
		final int year = Integer.parseInt(pieces[0]);
		pieces[1].replaceAll("-WE", "");
		final int weekendNum = Integer.parseInt(pieces[1]);
		
		final Calendar calendar = Calendar.getInstance();
		calendar.setFirstDayOfWeek(Calendar.SUNDAY);
		calendar.setMinimalDaysInFirstWeek(3);
		calendar.set(Calendar.YEAR, year);
		calendar.set(Calendar.WEEK_OF_YEAR, weekendNum);
		calendar.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
		
		final Date begin = calendarToDate(calendar);
		calendar.add(Calendar.DATE, 2);
		final Date end = calendarToDate(calendar);
		
		return ImmutablePair.of(begin,  end);
	}
	
	
	private static String getSingleDay(String givenDate){
		//Since it's going to format a date without a time, adding 12 hours
		//does not matter.
		final Timestamp oneDay = Timestamp.valueOf(givenDate + " 12:00:00");
		return CalendarHelper.formatDateSsml(oneDay);
	}
	
	
	/**
	 * 
	 * @param givenDate A string representing a month from Alexa such
	 * 					as 2016-02
	 * 
	 * @return			The name of the month that corresponds with the
	 * 					month in the string. January is 01 and December
	 * 					is 12.
	 */
	private static String getMonth(final String givenDate){
		final String[] pieces = givenDate.split("-");
		final int year = Integer.parseInt(pieces[0]);
		final int monthNum = Integer.parseInt(pieces[1]);
		return new DateFormatSymbols().getMonths()[monthNum-1];
	}
	
	
	/**
	 * 
	 * @param givenDate A string from Amazon representing a weekend.
	 * 					Example: 2016-W44-WE
	 * @return	A response depending on if the weekend is the next
	 * 			weekend coming up in the calendar (this weekend)
	 * 			or the weekend after that (next weekend).
	 */
	private static String getRelativeWeekend(final String givenDate){
		final String[] pieces = givenDate.split("-W");
		final int year = Integer.parseInt(pieces[0]);
		pieces[1].replaceAll("-WE", "");
		final int weekendNum = Integer.parseInt(pieces[1]);
		
		if(weekendNum == thisWeekNumber()){
			return "this weekend";
		}
		return "next weekend";
	}
	
	
	/**
	 * 
	 * @param givenDate A string from Amazon representing a week.
	 * 					Example: 2016-W44
	 * @return	A response depending on if the week is the same
	 * 			as this week, or next week.
	 */
	private static String getRelativeWeek(final String givenDate){
		final String[] pieces = givenDate.split("-W");
		final int year = Integer.parseInt(pieces[0]);
		int weekNum = Integer.parseInt(pieces[1]);
		
		if (todayNumber() == SUNDAY)
			weekNum++;
		
		if(weekNum == thisWeekNumber()){
			return "this week";
		}
		return "next week";
	}

	
	/**
	 * @return The day number of the week. Sunday is 1 and Saturday is 6.
	 */
	private static int todayNumber(){
		final Calendar today = Calendar.getInstance();
		final int dayOfWeek = today.get(Calendar.DAY_OF_WEEK);

		return dayOfWeek;
	}
	
	
	/**
	 * @return	This week's week number. The first week of the year is the first
	 * 			week that contains a Thursday. A week is considered to be Sunday
	 * 			through Saturday.
	 */
	private static int thisWeekNumber(){
		final Calendar calendar = Calendar.getInstance();
		calendar.setFirstDayOfWeek(Calendar.SUNDAY);
		calendar.setMinimalDaysInFirstWeek(3);
		
		int weekNum = calendar.get(Calendar.WEEK_OF_YEAR);
		if(todayNumber() == SUNDAY)
			weekNum++;
		return weekNum;
	}

	
	private static Date calendarToDate(final Calendar calendar) {
		// Calendar -> java.util.Date -> long
		final long time = calendar.getTime().getTime();

		return new Date(time);
	}
}
