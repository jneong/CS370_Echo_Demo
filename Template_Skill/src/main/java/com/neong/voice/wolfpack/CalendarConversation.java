package com.neong.voice.wolfpack;

import com.amazon.speech.slu.Intent;
import com.amazon.speech.speechlet.IntentRequest;
import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.neong.voice.model.base.Conversation;
import com.wolfpack.database.DbConnection;
import com.neong.voice.wolfpack.CalendarHelper;
import com.neong.voice.wolfpack.CosineSim;

import java.sql.Timestamp;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

import java.time.ZonedDateTime;

public class CalendarConversation extends Conversation {
	// Intent names
	private final static String INTENT_NEXTEVENT = "NextEventIntent";
	private final static String INTENT_GETEVENTSONDATE = "GetEventsOnDateIntent";
	private final static String INTENT_NARROWDOWN = "NarrowDownIntent";
	private final static String INTENT_GETFEEDETAILS = "GetFeeDetailsIntent";
	private final static String INTENT_GETLOCATIONDETAILS = "GetLocationDetailsIntent";
	private final static String INTENT_SPORTSCATEGORY = "SportsCategoryIntent";
	private final static String INTENT_ARTSANDENTERTAINMENTCATEGORY = "ArtsAndEntertainmentCategoryIntent";
	private final static String INTENT_LECTURESCATEGORY = "LecturesCategoryIntent";
	private final static String INTENT_CLUBSCATEGORY = "ClubsCategoryIntent";

	// Slots (String value in quotes should be the slot name in the intent
	// schema)
	private final static String EVENT_NAME = "eventName";
	private final static String AMAZON_DATE = "date";

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

		supportedIntentNames.add(INTENT_SPORTSCATEGORY);
		supportedIntentNames.add(INTENT_ARTSANDENTERTAINMENTCATEGORY);
		supportedIntentNames.add(INTENT_LECTURESCATEGORY);
		supportedIntentNames.add(INTENT_CLUBSCATEGORY);
	}
	

	@Override
	public SpeechletResponse respondToIntentRequest(IntentRequest intentReq, Session session) {
		Intent intent = intentReq.getIntent();
		String intentName = (intent != null) ? intent.getName() : null;
		SpeechletResponse response = null;
		int state;
		if (session.getAttribute("stateID") == null)
			state = 0;
		else
			state = (Integer) session.getAttribute("stateID");

		if (INTENT_NEXTEVENT.equals(intentName))
			response = handleNextEventIntent(intentReq, session);

		else if (INTENT_GETEVENTSONDATE.equals(intentName))
			response = handleGetEventsOnDateIntent(intentReq, session);

		else if (INTENT_GETFEEDETAILS.equals(intentName) && state == 1000)
			response = handleGetFeeDetailsIntent(intentReq, session);

		else if (INTENT_GETLOCATIONDETAILS.equals(intentName) && state == 1000)
			response = handleGetLocationDetailsIntent(intentReq, session);

		else if (INTENT_SPORTSCATEGORY.equals(intentName) && state == 1001)
			response = handleNarrowDownIntent(intentReq, session, "Athletics");
		else if (INTENT_ARTSANDENTERTAINMENTCATEGORY.equals(intentName) && state == 1001)
			response = handleNarrowDownIntent(intentReq, session, "Arts and Entertainment");
		else if (INTENT_LECTURESCATEGORY.equals(intentName) && state == 1001)
			response = handleNarrowDownIntent(intentReq, session, "Films and Lectures");
		else if (INTENT_CLUBSCATEGORY.equals(intentName) && state == 1001)
			response = handleNarrowDownIntent(intentReq, session, "Club and Organization");

		else
			response = newTellResponse("<speak>Sorry, I'm not quite sure what you meant.</speak>", true);

		return response;

	}

	
	private SpeechletResponse handleNextEventIntent(IntentRequest intentReq, Session session) {
		Map<String, Vector<Object>> results = db
				.runQuery("SELECT * FROM erichtest.event_info WHERE start > now() LIMIT 1;");

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
		ArrayList<String> savedEventNames;

		// String query = "SELECT * FROM event_info WHERE (\'" + givenDate +
		// "\'" + zoneString + " <= start) AND (date \'" + givenDate + "\' +
		// integer '1')" + zoneString + " > start;";
		String query = "SELECT * FROM erichtest.event_info WHERE now() < start LIMIT 15;";

		// Select all events on the same day as the givenDate.
		Map<String, Vector<Object>> results = db.runQuery(query);

		// If Alexa couldn't connect to the database or run the query:
		if (results == null)
			return newTellResponse("Sorry, I'm on break", false);

		numEvents = results.get("summary").size();

		// If there were not any events on the given day:
		if (numEvents == 0) {
			return newTellResponse("<speak>I couldn't find any events on <say-as interpret-as=\"date\">" + givenDate
					+ "</say-as> </speak>", true);
		}

		if (numEvents <= 13) {

			// Format eventDay as the day of the week
			Timestamp firstEventStart = (Timestamp) results.get("start").get(0);
			ZonedDateTime zonedDateTime = firstEventStart.toInstant().atZone(PST);
			String eventDate = zonedDateTime.format(DATEFORMATTER);

			response = "<speak> Okay, here is what I found. On <say-as interpret-as=\"date\">" + eventDate
					+ "</say-as> there is ";
			response += CalendarHelper.listEvents(results, givenDate);
			response += "</speak>";

			state = 1000;
			session.setAttribute("stateID", state);
			session.setAttribute("savedDate", givenDate);

			return newAskResponse(response, true,
					"<speak>was there anything you would like to know about those events?</speak>", true);
		}
		// 13 or more events
		else {
			savedEventNames = new ArrayList<String>(numEvents);
			for (int i = 0; i < numEvents; i++) {
				savedEventNames.add(results.get("summary").get(i).toString());
			}
			session.setAttribute("recentlySaidEvents", savedEventNames);
			state = 1001;
			session.setAttribute("stateID", state);
			session.setAttribute("savedDate", givenDate);
			return newAskResponse(
				"I was able to find " + numEvents
					+ " different events. Would you like to hear about all of them, or just something like sports or performances?",
				true, "<speak>what kind of events did you want to hear about?</speak>", true);
		}

	}

	
	private SpeechletResponse handleNarrowDownIntent(IntentRequest intentReq, Session session, String category) {

		if (session.getAttribute("savedDate") == null)
			return newTellResponse("Well, something went wrong.", false);
		String givenDate = session.getAttribute("savedDate").toString();

		// Return the name and the time of all events within that category, or
		// if the query finds that there are no events on the day, Alexa tells the user
		// she has nothing to return.
		Map<String, Vector<Object>> results = db.runQuery("SELECT summary, start FROM events LIMIT 4;");

		if (results.get("summary").size() == 0) {
			return newTellResponse("<speak> There are no events happening in" + category + "on that day </speak>",
					true);
		}

		String responseString = CalendarHelper.listEvents(results, givenDate);
		int number2 = 1000;

		session.setAttribute("stateID", number2);
		return newAskResponse(responseString, true, "Are you still there?", false);
	}

	
	private SpeechletResponse handleGetFeeDetailsIntent(IntentRequest intentReq, Session session) {
		Intent theIntent = intentReq.getIntent();
		String eventName = theIntent.getSlot(EVENT_NAME).getValue();

		if (session.getAttribute("recentlySaidEvents") == null)
			return newTellResponse("wait for me to mention some events first.", false);
		ArrayList<String> savedEvents = (ArrayList<String>) session.getAttribute("recentlySaidEvents");
		System.out.println("I WAS GIVEN THE EVENT NAME: " + eventName);
		eventName = CosineSim.getBestMatch(eventName, savedEvents);
		System.out.println("I'M THINKING THE CLOSEST NAME IS: " + eventName);

		Map<String, Vector<Object>> results = db.runQuery("SELECT * FROM events WHERE summary = '" + eventName + "';");

		if (results.get("general_admission_fee").size() == 0)
			return newAskResponse("<speak>I wasn't able to find that information</speak>", true,
					"<speak> Did you want any other information? </speak>", false);

		String fee = results.get("general_admission_fee").get(0).toString();
		return newAskResponse("<speak> The general admission fee is " + fee + ". </speak>", true,
				"<speak> I'm sorry, I didn't quite catch that </speak>", false);

	}

	
	private SpeechletResponse handleGetLocationDetailsIntent(IntentRequest intentReq, Session session) {
		Intent theIntent = intentReq.getIntent();
		String eventName = theIntent.getSlot(EVENT_NAME).getValue();

		if (session.getAttribute("recentlySaidEvents") == null)
			return newTellResponse("wait for me to mention some events first.", false);
		ArrayList<String> savedEvents = (ArrayList<String>) session.getAttribute("recentlySaidEvents");
		System.out.println("I WAS GIVEN THE EVENT NAME: " + eventName);
		eventName = CosineSim.getBestMatch(eventName, savedEvents);
		System.out.println("I'M THINKING THE CLOSEST NAME IS: " + eventName);

		Map<String, Vector<Object>> results = db
				.runQuery("SELECT summary, location FROM event_info WHERE summary = '" + eventName + "';");

		if (results.get("location").size() == 0)
			return newAskResponse("<speak>I wasn't able to find that information</speak>", true,
					"<speak> Did you want any other information? </speak>", false);

		String event = results.get("summary").get(0).toString();
		String location = results.get("location").get(0).toString();

		return newAskResponse("<speak> The " + event + " is located at " + location + ". </speak>", true,
				"<speak>I'm sorry, I didn't quite catch that</speak>", false);
	}

}
