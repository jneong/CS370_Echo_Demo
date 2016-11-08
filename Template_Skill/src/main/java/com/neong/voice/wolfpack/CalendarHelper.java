package com.neong.voice.wolfpack;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

import java.time.ZonedDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;


public class CalendarHelper {
	public static final String TIME_ZONE = "America/Los_Angeles";

	private static final ZoneId LOCAL_ZONEID = ZoneId.of(TIME_ZONE);
	private static final DateTimeFormatter DAY_FORMATTER = DateTimeFormatter.ofPattern("EEEE");
	private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("????MMdd");
	private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("h:mm a");


	public static boolean isCategorySupported(final String category) {
		final String[] supportedCategories = {
			"SportsCategoryIntent", "ArtsAndEntertainmentCategoryIntent",
			"LecturesCategoryIntent",  "ClubsCategoryIntent"
		};

		for (final String cat : supportedCategories)
			if (cat == category)
				return true;

		return false;
	}


	/**
	 * Format a message with fields from given events.
	 *
	 * @param format the template for the message.  Special tokens of the form "{field}" are replaced
	 *               with values from {@code events} at the offset {@code index}.  Timestamp fields
	 *               require additional specificity to determine whether to format the value as a time
	 *               or a date, using the extend token forms "{field:time}" and "{field:date}".
	 * @param events the result object from a {@link com.wolfpack.database.DbConnection DbConnection}
	 *               query.  The object must contain all columns referenced by the format string and
	 *               at least {@code index + 1} rows or an exception may be thrown.
	 * @param index  the row offset into {@code events} for the event to refer to.
	 *
	 * @return the message from {@code format} with all "{field}" tokens replaced with the values from
	 *         {@code events} at the row specified by {@code index}.  For example,
	 *         <code>"{title} is at {start:time}."</code> with valid {@code events} and {@code index}
	 *         might return the string {@code "IMS Basketball is at 4:00 PM"}.
	 */
	public static String formatEventSsml(final String format,
	                                     final Map<String, Vector<Object>> events,
	                                     final int index) {
		final int len = format.length();
		final StringBuilder resultBuilder = new StringBuilder(len);
		int i = 0;

		while (i < len) {
			final char c = format.charAt(i++);

			if (c == '{') {
				final StringBuilder fieldBuilder = new StringBuilder();
				char c1;

				// This should throw an exception if the format string is malformed.
				while ((c1 = format.charAt(i++)) != '}')
					fieldBuilder.append(c1);

				final String field = fieldBuilder.toString();
				final String value = formatEventFieldSsml(field, events, index);

				resultBuilder.append(value);
			} else {
				resultBuilder.append(c);
			}
		}

		final String result = resultBuilder.toString();

		return replaceUnspeakables(result);
	}


	public static String formatEventFieldSsml(final String field,
	                                          final Map<String, Vector<Object>> events,
	                                          final int index) {
		final String result;

		switch (field) {
		case "start:date":
		case "end:date": {
			final String fieldName = field.split(":")[0];
			final Timestamp start = (Timestamp) events.get(fieldName).get(index);
			result = formatDateSsml(start);
			break;
		}

		case "start:time":
		case "end:time": {
			final String fieldName = field.split(":")[0];
			final Timestamp end = (Timestamp) events.get(fieldName).get(index);
			result = formatTimeSsml(end);
			break;
		}

		case "location": {
			final String location = (String) events.get(field).get(index);
			result = formatLocationSsml(location);
			break;
		}

		case "student_admission_fee":
		case "general_admission_fee": {
			final String fee = (String) events.get(field).get(index);
			result = formatFeeSsml(fee);
			break;
		}

		default:
			result = (String) events.get(field).get(index);
			break;
		}

		return result;
	}


	public static String formatEventSsml(final String format,
	                                     final Map<String, Vector<Object>> events) {
		return formatEventSsml(format, events, 0);
	}


	public static String replaceUnspeakables(final String ssml) {
		return ssml.replaceAll("&", " and ");
	}


	public static String formatDateSsml(final Timestamp when) {
		final ZonedDateTime zonedDateTime = when.toInstant().atZone(LOCAL_ZONEID);
		final String day = zonedDateTime.format(DAY_FORMATTER);
		final String date = zonedDateTime.format(DATE_FORMATTER);

		return day + ", <say-as interpret-as=\"date\">" + date + "</say-as>";
	}


	public static String formatTimeSsml(final Timestamp when) {
		final ZonedDateTime zonedDateTime = when.toInstant().atZone(LOCAL_ZONEID);
		final String time = zonedDateTime.format(TIME_FORMATTER);

		return "<say-as interpret-as=\"time\">" + time + "</say-as>";
	}


	public static String formatLocationSsml(String location) {
		if (location == null)
			location = "Sonoma State University";

		return location;
	}


	public static String formatFeeSsml(String fee) {
		if (fee == null)
			fee = "not specified";

		return fee.replace("-", " to ");
	}


	public static String listEvents(final String format, final Map<String, Vector<Object>> events) {
		final int eventsLength = events.get("title").size();
		final StringBuilder responseBuilder = new StringBuilder(eventsLength * format.length());

		for (int i = 0; i < eventsLength; i++)
			responseBuilder.append(formatEventSsml(format, events, i));

		return responseBuilder.toString();
	}


	public static Map<String, Integer> extractEventIds(Map<String, Vector<Object>> events, int numEvents) {
		final Map<String, Integer> savedEvents = new HashMap<String, Integer>(numEvents);

		for (int i = 0; i < numEvents; i++) {
			final String key = (String) events.get("title").get(i);
			final Integer value = (Integer) events.get("event_id").get(i);
			savedEvents.put(key, value);
		}

		return savedEvents;
	}
}
