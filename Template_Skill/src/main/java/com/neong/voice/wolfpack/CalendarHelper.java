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

	public enum EventField {
		START_DATE, START_TIME, END_TIME, SUMMARY, LOCATION, GENERAL_ADMISSION
	};


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


	public static String formatEventSsml(int index, Map<String, Vector<Object>> events, EventField[] fields) {
		String ssml = "<s>";

		for (EventField field : fields) {
			switch (field) {
			case SUMMARY:
				final String summary = (String) events.get("summary").get(index);
				ssml += summary;
				break;

			case START_DATE: {
				final Timestamp start = (Timestamp) events.get("start").get(index);
				ssml += "on " + formatDateSsml(start);
				break;
			}

			case START_TIME: {
				final Timestamp start = (Timestamp) events.get("start").get(index);
				ssml += "at " + formatTimeSsml(start);
				break;
			}

			case END_TIME:
				final Timestamp end = (Timestamp) events.get("end").get(index);
				ssml += "ends at " + formatTimeSsml(end);
				break;

			case LOCATION:
				final String location = (String) events.get("location").get(index);
				ssml += "at " + location;
				break;

			case GENERAL_ADMISSION:
				final String fee = (String) events.get("general_admission_fee").get(index);
				ssml += "the general admission fee is " + formatFeeSsml(fee);
				break;

			default:
				break;
			}

			ssml += " ";
		}

		ssml += "</s>";

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


	public static String listEvents(Map<String, Vector<Object>> events, EventField[] fields) {
		String response = "";

		final int eventsLength = events.get("summary").size();

		for (int i = 0; i < eventsLength; i++)
			response += formatEventSsml(i, events, fields);

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
