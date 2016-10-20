package com.neong.voice.wolfpack;

import com.amazon.speech.slu.Intent;
import com.amazon.speech.speechlet.IntentRequest;
import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.neong.voice.model.base.Conversation;
import com.wolfpack.database.DbConnection;

import java.sql.Timestamp;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

// import org.joda.time.DateTime;

import java.time.ZonedDateTime;
import com.neong.voice.wolfpack.CalendarHelper;

public class CalendarConversation extends Conversation {
	// Intent names
	private final static String INTENT_NEXTEVENT = "NextEventIntent";
	private final static String INTENT_GETEVENTSONDATE = "GetEventsOnDateIntent";
	private final static String INTENT_NARROWDOWN = "NarrowDownIntent";
	private final static String AMAZON_DATE = "date";
	private final static String CATEGORY_TITLE = "CategoryTitle";

	private final static ZoneId PST = ZoneId.of("America/Los_Angeles");
	private final static DateTimeFormatter TIMEFORMATTER = DateTimeFormatter.ofPattern("h:mm a");
	private final static DateTimeFormatter DAYFORMATTER = DateTimeFormatter.ofPattern("EEEE");
	private final static DateTimeFormatter DATEFORMATTER = DateTimeFormatter.ofPattern("????MMdd");

	private DbConnection db;

	public CalendarConversation() {
		super();

		db = new DbConnection("DbCredentials.xml");
		db.getRemoteConnection();

		// Add custom intent names for dispatcher use.
		supportedIntentNames.add(INTENT_NEXTEVENT);
		supportedIntentNames.add(INTENT_GETEVENTSONDATE);
		supportedIntentNames.add(INTENT_NARROWDOWN);
	}

	@Override
	public SpeechletResponse respondToIntentRequest(IntentRequest intentReq, Session session) {
		Intent intent = intentReq.getIntent();
		String intentName = (intent != null) ? intent.getName() : null;
		SpeechletResponse response = null;

		if (INTENT_NEXTEVENT.equals(intentName))
			response = handleNextEventIntent(intentReq, session);

		if (INTENT_GETEVENTSONDATE.equals(intentName))
			response = handleGetEventsOnDateIntent(intentReq, session);
		
		if (INTENT_NARROWDOWN.equals(intentName))
			response = handleNarrowDownIntent(intentReq, session);

		return response;

	}

	private SpeechletResponse handleNextEventIntent(IntentRequest intentReq, Session session) {
		Map<String, Vector<Object>> results = db
				.runQuery("SELECT * FROM ssucalendar.event_info WHERE start > now() LIMIT 1;");

		if (results == null)
			return newTellResponse("Sorry, I'm on break", false);

		String summary = (String) results.get("summary").get(0);
		Timestamp start = (Timestamp) results.get("start").get(0);
		String location = (String) results.get("name").get(0);

		if (location == null)
			location = "Sonoma State University";

		ZonedDateTime zonedDateTime = start.toLocalDateTime().atZone(PST);
		String date = zonedDateTime.format(DATEFORMATTER);
		String day = zonedDateTime.format(DAYFORMATTER);
		String time = zonedDateTime.format(TIMEFORMATTER);

		return newTellResponse("<speak> Okay, the next event is " + summary + " on " + day
				+ " <say-as interpret-as=\"date\">" + date + "</say-as> at <say-as interpret-as=\"time\">" + time
				+ "</say-as> at " + location + ". </speak>", true);
	}

	private SpeechletResponse handleGetEventsOnDateIntent(IntentRequest intentReq, Session session) {
		Intent theIntent = intentReq.getIntent();
		String givenDate = theIntent.getSlot(AMAZON_DATE).getValue();

		Map<String, Vector<Object>> results = db
				.runQuery("SELECT * FROM ssucalendar.event_info WHERE start = '" + givenDate + " 00:00:00.000000';");

		if (results == null)
			return newTellResponse("Sorry, I'm on break", false);

		String summary = (String) results.get("summary").get(0);
		Timestamp start = (Timestamp) results.get("start").get(0);
		ZonedDateTime zonedDateTime = start.toLocalDateTime().atZone(PST);
		String time = zonedDateTime.format(TIMEFORMATTER);

		return newTellResponse(
				"<speak> Okay, " + summary + "</say-as> is at <say-as interpret-as=\"time\">" + time + ". </speak>",
				true);

	}
	
	private SpeechletResponse handleNarrowDownIntent(IntentRequest intentReq, Session session) {
		Intent theIntent = intentReq.getIntent();
		String slotCategory = theIntent.getSlot(CATEGORY_TITLE).getValue();
		String givenDate = session.getAttribute("savedDate").toString();
		
		// If the category the user asks about is not in the list of categories/calendars we support, Alexa
		// will warn the user.
		if (!CalendarHelper.isCategorySupported(slotCategory)){
			int number = 1001;
			session.setAttribute("stateID", number);
			return newTellResponse(
					"<speak> Sorry, I do not know anything about that category. Would you like to know about"
					+ "sports, arts and entertainment or club events? . </speak>", true);
			
		}
		
		// Otherwise if the category the user asks for IS one of the categories we support. Return the name and the 
		// time of all events within that category, or if the query finds that there are no events on the day, 
		// Alexa tells the user she has nothing to return.
		Map<String, Vector<Object>> results = db 
		.runQuery("SELECT summary, start FROM events;");
		
		if (results.get("summary").size() == 0){
			return newTellResponse(
					"<speak> There are no events happening in"+ slotCategory +"on that day </speak>", true);
		}
		
		String responseString = CalendarHelper.listEvents(results, givenDate);
		int number2 = 1000;
		
		session.setAttribute("stateID", number2);
		return newAskResponse(responseString, true, "Are you still there?", false);
	}
	
}

	
