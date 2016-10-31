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

	private static final DateTimeFormatter DAY_FORMATTER = DateTimeFormatter.ofPattern("EEEE");
	private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("????MMdd");
	private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("h:mm a");
	private static final ZoneId LOCAL_ZONEID = ZoneId.of(TIME_ZONE);


	public static boolean isCategorySupported(String category) {
		final String[] supportedCategories = {
			"SportsCategoryIntent", "ArtsAndEntertainmentCategoryIntent",
			"LecturesCategoryIntent",  "ClubsCategoryIntent"
		};

		for (String cat : supportedCategories)
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
	 *         <code>"{summary} is at {start:time}."</code> with valid {@code events} and {@code index}
	 *         might return the string {@code "IMS Basketball is at 4:00 PM"}.
	 */
	public static String formatEventSsml(String format, Map<String, Vector<Object>> events, int index) {
		int len = format.length(), i = 0;
		StringBuilder resultBuilder = new StringBuilder(len);

		while (i < len) {
			char c = format.charAt(i++);

			switch (c) {
			case '{': {
				StringBuilder fieldBuilder = new StringBuilder();

				// This should throw an exception if the format string is malformed.
				while ((c = format.charAt(i++)) != '}')
					fieldBuilder.append(c);

				String field = fieldBuilder.toString();

				switch (field) {
				case "start:date":
				case "end:date": {
					final String fieldName = field.split(":")[0];
					final Timestamp start = (Timestamp) events.get(fieldName).get(index);
					resultBuilder.append(formatDateSsml(start));
					break;
				}

				case "start:time":
				case "end:time": {
					final String fieldName = field.split(":")[0];
					final Timestamp end = (Timestamp) events.get(fieldName).get(index);
					resultBuilder.append(formatTimeSsml(end));
					break;
				}

				case "location": {
					final String location = (String) events.get(field).get(index);
					resultBuilder.append(formatLocationSsml(location));
					break;
				}

				case "student_admission_fee":
				case "general_admission_fee": {
					final String fee = (String) events.get(field).get(index);
					resultBuilder.append(formatFeeSsml(fee));
					break;
				}

				default: {
					final String value = (String) events.get(field).get(index);
					resultBuilder.append(value);
					break;
				}
				}
				break;
			}

			default:
				resultBuilder.append(c);
				break;
			}
		}

		String result = resultBuilder.toString();

		return replaceUnspeakables(result);
	}


	public static String formatEventSsml(String format, Map<String, Vector<Object>> events) {
		return formatEventSsml(format, events, 0);
	}


	public static String replaceUnspeakables(String ssml) {
		return ssml.replaceAll("&", " and ");
	}


	public static String formatDateSsml(Timestamp when) {
		final ZonedDateTime zonedDateTime = when.toInstant().atZone(LOCAL_ZONEID);
		final String day = zonedDateTime.format(DAY_FORMATTER);
		final String date = zonedDateTime.format(DATE_FORMATTER);

		return day + ", <say-as interpret-as=\"date\">" + date + "</say-as>";
	}


	public static String formatTimeSsml(Timestamp when) {
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


	public static String listEvents(String format, Map<String, Vector<Object>> events) {
		String response = "";

		final int eventsLength = events.get("summary").size();

		for (int i = 0; i < eventsLength; i++)
			response += formatEventSsml(format, events, i);

		return response;
	}


	public static Map<String, Integer> extractEventIds(Map<String, Vector<Object>> events, int numEvents) {
		Map<String, Integer> savedEvents = new HashMap<String, Integer>(numEvents);

		for (int i = 0; i < numEvents; i++) {
			String key = events.get("summary").get(i).toString();
			Integer value = (Integer) events.get("event_id").get(i);
			savedEvents.put(key, value);
		}

		return savedEvents;
	}
}
