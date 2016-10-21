package com.neong.voice.wolfpack;

import java.sql.Timestamp;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Vector;

public  class CalendarHelper {
	public static boolean isCategorySupported(String category){
		String [] supportedCategories = {"Sports", "Arts and Entertainment", "Lectures"};
		for (int i = 0; i < 3; i++){
			if (category == supportedCategories[i])
				return true;
		}
		return false;
	}
	
	public static String listEvents(Map<String, Vector<Object>> results, String eventDate){

		String response;
		final DateTimeFormatter DATEFORMATTER = DateTimeFormatter.ofPattern("????MMdd");
		final DateTimeFormatter TIMEFORMATTER = DateTimeFormatter.ofPattern("h:mm a");
		final ZoneId PST = ZoneId.of("America/Los_Angeles");
		Timestamp firstEventStart = (Timestamp) results.get("start").get(0);
		ZonedDateTime zonedDateTime = firstEventStart.toInstant().atZone(PST);
		
		response = "<speak> Okay, the events on <say-as interpret-as=\"date\">" + eventDate + "</say-as> are ";
		for(int i = 0; i < results.get("summary").size() ; i++){
			String summary = (String) results.get("summary").get(i);
			Timestamp start = (Timestamp) results.get("start").get(i);
			zonedDateTime = start.toInstant().atZone(PST);
			String formattedStart = "<say-as interpret-as=\"time\">" + zonedDateTime.format(TIMEFORMATTER) + "</say-as>";

			response = response + summary + " at " + formattedStart + "<break time=\".25s\"/>";

		}
		response += "</speak>";
		return response;
	}
	
}

