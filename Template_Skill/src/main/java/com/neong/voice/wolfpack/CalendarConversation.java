package com.neong.voice.wolfpack;

import com.amazon.speech.slu.Intent;
import com.amazon.speech.speechlet.IntentRequest;
import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.neong.voice.model.base.Conversation;
import com.wolfpack.database.DbConnection;
import com.neong.voice.wolfpack.CalendarHelper;

import java.sql.Timestamp;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

import java.time.ZonedDateTime;
import com.neong.voice.wolfpack.CalendarHelper;

public class CalendarConversation extends Conversation {
	// Intent names
	private final static String INTENT_NEXTEVENT = "NextEventIntent";
	private final static String INTENT_GETEVENTSONDATE = "GetEventsOnDateIntent";
	private final static String INTENT_NARROWDOWN = "NarrowDownIntent";
	private final static String INTENT_GETFEEDETAILS = "GetFeeDetailsIntent";
	private final static String INTENT_GETLOCATIONDETAILS = "GetLocationDetailsIntent";

	
	// Slots (String value in quotes should be the slot name in the intent schema)
	private final static String EVENT_NAME = "eventName";
	private final static String AMAZON_DATE = "date";
	private final static String CATEGORY_TITLE = "CategoryTitle";

	// Timestamp formatting
	private final static ZoneId PST = ZoneId.of("America/Los_Angeles");
	private final static DateTimeFormatter TIMEFORMATTER = DateTimeFormatter.ofPattern("h:mm a");
	private final static DateTimeFormatter DAYFORMATTER = DateTimeFormatter.ofPattern("EEEE");
	private final static DateTimeFormatter DATEFORMATTER = DateTimeFormatter.ofPattern("????MMdd");
	
	// Database query pieces
	private final static String zoneString = " ::timestamp at time zone 'America/Los_Angeles' ";

	private DbConnection db;

	public CalendarConversation() {
		super();

		db = new DbConnection("DbCredentials.xml");
		db.getRemoteConnection();

		// Add custom intent names for dispatcher use.
		supportedIntentNames.add(INTENT_NEXTEVENT);
		supportedIntentNames.add(INTENT_GETEVENTSONDATE);
		supportedIntentNames.add(INTENT_NARROWDOWN);
		supportedIntentNames.add(INTENT_GETFEEDETAILS);
		supportedIntentNames.add(INTENT_GETLOCATIONDETAILS);
	}

	@Override
	public SpeechletResponse respondToIntentRequest(IntentRequest intentReq, Session session) {
		Intent intent = intentReq.getIntent();
		String intentName = (intent != null) ? intent.getName() : null;
		SpeechletResponse response = null;
		int state = (Integer) session.getAttribute("stateID");

		if (INTENT_NEXTEVENT.equals(intentName))
			response = handleNextEventIntent(intentReq, session);

		if (INTENT_GETEVENTSONDATE.equals(intentName))
			response = handleGetEventsOnDateIntent(intentReq, session);
		
		if (INTENT_NARROWDOWN.equals(intentName) && state == 1001)
			response = handleNarrowDownIntent(intentReq, session);
		
		if (INTENT_GETFEEDETAILS.equals(intentName) && state == 1000)
			response = handleGetFeeDetailsIntent(intentReq, session);
		
		if (INTENT_GETLOCATIONDETAILS.equals(intentName) && state == 1000)
			response = handleGetLocationDetailsIntent(intentReq, session);

		return response;

	}

	private SpeechletResponse handleNextEventIntent(IntentRequest intentReq, Session session) {
		Map<String, Vector<Object>> results = db
				.runQuery("SELECT * FROM ssucalendar.event_info WHERE start > now() LIMIT 1;");

		if (results == null)
			return newTellResponse("Sorry, I'm on break", false);

		String summary = (String) results.get("summary").get(0);
		Timestamp start = (Timestamp) results.get("start").get(0);
		String location = (String) results.get("location").get(0);

		if (location == null)
			location = "Sonoma State University";

		ZonedDateTime zonedDateTime = start.toInstant().atZone(PST);
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
		int numEvents;
		int state;
		String response;
		String[] savedEventNames;
		
		String query = "SELECT * FROM event_info WHERE (\'" + givenDate + "\'" + zoneString + " <= start) AND (date \'" + givenDate + "\' + integer '1')" + zoneString + " > start;";
		
		//Select all events on the same day as the givenDate.
		Map<String, Vector<Object>> results = db.runQuery(query);

		//If Alexa couldn't connect to the database or run the query:
		if (results == null)
			return newTellResponse("Sorry, I'm on break", false);
		
		numEvents = results.get("summary").size();
		
		//If there were not any events on the given day:
		if (numEvents == 0){
			return newTellResponse("<speak>I couldn't find any events on <say-as interpret-as=\"date\">" + givenDate + "</say-as> </speak>", true);
		}
		
		if(numEvents <= 13){
		
			//Format eventDay as the day of the week
			Timestamp firstEventStart = (Timestamp) results.get("start").get(0);
			ZonedDateTime zonedDateTime = firstEventStart.toInstant().atZone(PST);
			String eventDate = zonedDateTime.format(DATEFORMATTER);
			
			response = "<speak> Okay, here is what I found. On <say-as interpret-as=\"date\">" + eventDate + "</say-as> there is ";
			response += CalendarHelper.listEvents(results, givenDate);
			response += "</speak>";
			savedEventNames = new String[numEvents];
			for(int i = 0; i < numEvents; i++){
				savedEventNames[i] = results.get("summary").get(i).toString();
				
			}
			
			state = 1000;
			session.setAttribute("stateID", state);
			session.setAttribute("recentlySaidEvents", savedEventNames);

			return newAskResponse(response, true, "<speak>was there anything you would like to know about those events?</speak>", true);
		}
		//13 or more events
		else{
			savedEventNames = new String[numEvents];
			for(int i = 0; i < numEvents; i++){
				savedEventNames[i] = results.get("summary").get(i).toString();
				
			}
			state = 1001;
			session.setAttribute("stateID", state);
			session.setAttribute("recentlySaidEvents", savedEventNames);
			return newAskResponse("I was able to find " + numEvents + " different events. Would you like to hear about all of them, or just something like sports or performances?", true,
					"<speak>what kind of events did you want to hear about?</speak>", true);
		}

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
	
	private SpeechletResponse handleGetFeeDetailsIntent(IntentRequest intentReq, Session session) {
		Intent theIntent = intentReq.getIntent();				
		String eventname = theIntent.getSlot(EVENT_NAME).getValue();
			
		Map<String, Vector<Object>> results = db
			.runQuery("SELECT * FROM events WHERE summary = '" + eventname);
		
		String fee = results.get("general_admission_fee").get(0).toString();
		
		if (results.get("general_admission_fee").size() == 0)
			return newAskResponse("<speak>I wasn't able to find that information</speak>", true, "<speak> I'm sorry, I didn't quite catch that </speak>", false);
		
		return newAskResponse("<speak> The general admission fee is, " + fee + ". </speak>", true, "<speak> I'm sorry, I didn't quite catch that </speak>", false);
		
	}
	
	private SpeechletResponse handleGetLocationDetailsIntent(IntentRequest intentReq, Session session) {
		Intent theIntent = intentReq.getIntent();				
		String eventname = theIntent.getSlot(EVENT_NAME).getValue();
		
		Map<String, Vector<Object>> results = db
			.runQuery("SELECT summary, location FROM events WHERE summary = '" + eventname);
		
		String event = results.get("summary").get(0).toString();
		String location = results.get("location").get(0).toString();
		
		if (results.get("location").size() == 0)
			return newAskResponse("<speak>I wasn't able to find that information</speak>", true, "<speak> I'm sorry, I didn't quite catch that </speak>", false);
		
		return newAskResponse("<speak> The, " + event + "is located at, " + location +". </speak>", true, "<speak>I'm sorry, I didn't quite catch that</speak>", false);
				
	}
	
}

	
