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
	private final static String INTENT_GETENDTIME = "GetEndTimeIntent";
	private final static String INTENT_ALLCATEGORY = "AllCategoryIntent";
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

	// Other constants
	private final static String zoneString = " ::timestamp at time zone 'America/Los_Angeles' ";
	private final static int MAX_EVENTS = 5;

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
		supportedIntentNames.add(INTENT_GETENDTIME);

		supportedIntentNames.add(INTENT_ALLCATEGORY);
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

		else if (INTENT_GETENDTIME.equals(intentName) && state == 1000)
			response = handleGetEndTimeIntent(intentReq, session);

		else if (INTENT_ALLCATEGORY.equals(intentName) && state == 1001)
			response = handleNarrowDownIntent(intentReq, session, "all");
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
		Map<String, Vector<Object>> results = db.runQuery("SELECT * FROM event_info WHERE start > now() LIMIT 1;");

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
		HashMap<String, Integer> savedEventNames;

		String query = "SELECT event_id, summary, start FROM event_info WHERE ('" + givenDate +
				"'" + zoneString + " <= start) AND (date \'" + givenDate + "' + integer '1')" + zoneString + " > start;";
		
		System.out.println(query);

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

		if (numEvents <= MAX_EVENTS) {

			// Format eventDay as the day of the week
			Timestamp firstEventStart = (Timestamp) results.get("start").get(0);
			ZonedDateTime zonedDateTime = firstEventStart.toInstant().atZone(PST);
			String eventDate = zonedDateTime.format(DATEFORMATTER);

			response = "<speak> Okay, here is what I found. On <say-as interpret-as=\"date\">" + eventDate
					+ "</say-as> there is ";
			response += CalendarHelper.listEvents(results, givenDate);
			response += "</speak>";

			savedEventNames = new HashMap<String, Integer>();
			for (int i = 0; i < numEvents; i++) {
				savedEventNames.put(results.get("summary").get(i).toString(), new Integer(results.get("event_id").get(i).toString()));
			}
			session.setAttribute("recentlySaidEvents", savedEventNames);
			state = 1000;
			session.setAttribute("stateID", state);
			session.setAttribute("savedDate", givenDate);

			return newAskResponse(response, true,
					"<speak>was there anything you would like to know about those events?</speak>", true);
		}
		// MAX_EVENTS or more events
		else {
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
		HashMap<String, Integer> savedEventNames;
		int numEvents;
		String query;

		//This should never happen.
		if (session.getAttribute("savedDate") == null)
			return newTellResponse("I can't even remember which day we were talking about.", false);
		String givenDate = session.getAttribute("savedDate").toString();

		// Return the name and the time of all events within that category, or
		// if the query finds that there are no events on the day, Alexa tells
		// the user
		// she has nothing to return.
		Map<String, Vector<Object>> results;

		if(category == "all"){
			query = "SELECT event_id, summary, start FROM event_info WHERE ('" + givenDate +
					"'" + zoneString + " <= start) AND (date \'" + givenDate + "' + integer '1')" + zoneString + " > start;";
		}
		else{ 
			query = "SELECT event_id, summary, start FROM given_category('" + category + "', '" + givenDate + "', 1::smallint);";
		}

		results = db.runQuery(query);

		numEvents = results.get("summary").size();

		if (numEvents == 0) {
			return newTellResponse("<speak> There are no events happening in " + category + "on that day </speak>",
					true);
		}
		String responseString = CalendarHelper.listEvents(results, givenDate);

		int number2 = 1000;
		session.setAttribute("stateID", number2);

		savedEventNames = new HashMap<String, Integer>();

		for (int i = 0; i < numEvents; i++) {
			savedEventNames.put(results.get("summary").get(i).toString(), new Integer(results.get("event_id").get(i).toString()));
		}
		session.setAttribute("recentlySaidEvents", savedEventNames);

		return newAskResponse(responseString, true, "Are you still there?", false);
	}


	private SpeechletResponse handleGetFeeDetailsIntent(IntentRequest intentReq, Session session) {
		Intent theIntent = intentReq.getIntent();
		String eventName = theIntent.getSlot(EVENT_NAME).getValue();
		
		if (session.getAttribute("recentlySaidEvents") == null)
			return newTellResponse("wait for me to mention some events first.", false);
		
		Map<String, Integer> savedEvents = (HashMap<String, Integer>) session.getAttribute("recentlySaidEvents");

		System.out.println("I WAS GIVEN THE EVENT NAME: " + eventName);
		eventName = CosineSim.getBestMatch(eventName, savedEvents.keySet());
		Integer eventID = savedEvents.get(eventName);
		System.out.println("I'M THINKING THE CLOSEST NAME IS: " + eventName);

		Map<String, Vector<Object>> results = db
				.runQuery("SELECT summary, general_admission_fee FROM events WHERE event_id = '" + eventID + "';");

		if (results.get("general_admission_fee").get(0) == null)
			return newAskResponse("<speak>I wasn't able to find that information</speak>", true,
					"<speak> Did you want any other information? </speak>", false);

		String fee = results.get("general_admission_fee").get(0).toString();
		return newAskResponse("<speak> The general admission fee is " + fee + ". </speak>", true,
				"<speak> I'm sorry, I didn't quite catch that </speak>", false);

	}


	private SpeechletResponse handleGetLocationDetailsIntent(IntentRequest intentReq, Session session) {
		Intent theIntent = intentReq.getIntent();
		String eventNameSlot = theIntent.getSlot(EVENT_NAME).getValue();
		
		if (session.getAttribute("recentlySaidEvents") == null)
			return newTellResponse("wait for me to mention some events first.", false);

		Map<String, Integer> savedEvents = (HashMap<String, Integer>) session.getAttribute("recentlySaidEvents");

		System.out.println("I WAS GIVEN THE EVENT NAME: " + eventNameSlot);
		String eventName = CosineSim.getBestMatch(eventNameSlot, savedEvents.keySet());
		Integer eventID = savedEvents.get(eventName);
		System.out.println("I'M THINKING THE CLOSEST NAME IS: " + eventName);

		Map<String, Vector<Object>> results = db
				.runQuery("SELECT summary, location FROM event_info WHERE event_id = '" + eventID + "';");

		if (results.get("location").get(0) == null)
			return newAskResponse("<speak>I wasn't able to find that information</speak>", true,
					"<speak> Did you want any other information? </speak>", false);

		String event = results.get("summary").get(0).toString();
		String location = results.get("location").get(0).toString();

		return newAskResponse("<speak> The " + event + " is located at " + location + ". </speak>", true,
				"<speak>I'm sorry, I didn't quite catch that </speak>", false);

	}


	private SpeechletResponse handleGetEndTimeIntent(IntentRequest intentReq, Session session) {
		Intent theIntent = intentReq.getIntent();
		String eventName = theIntent.getSlot(EVENT_NAME).getValue();

		if (session.getAttribute("recentlySaidEvents") == null)
			return newTellResponse("wait for me to mention some events first.", false);

		Map<String, Integer> savedEvents = (HashMap<String, Integer>) session.getAttribute("recentlySaidEvents");
		System.out.println("I WAS GIVEN THE EVENT NAME: " + eventName);
		eventName = CosineSim.getBestMatch(eventName, savedEvents.keySet());
		Integer eventID = savedEvents.get(eventName);
		System.out.println("I'M THINKING THE CLOSEST NAME IS: " + eventName);

		Map<String, Vector<Object>> results = db
				.runQuery("SELECT summary, \"end\" FROM events WHERE event_id = '" + eventID + "';");

		if (results.get("end").get(0) == null)
			return newAskResponse("<speak>I wasn't able to find that information</speak>", true,
					"<speak> Did you want any other information? </speak>", false);

		Timestamp end = (Timestamp) results.get("end").get(0);
		ZonedDateTime zonedDateTime = end.toInstant().atZone(PST);
		String time = zonedDateTime.format(TIMEFORMATTER);

		String event = results.get("summary").get(0).toString();

		return newAskResponse(
				"<speak> The " + event + " ends at " + " <say-as interpret-as=\"time\">" + time + "</say-as>. </speak>",
				true, "<speak>I'm sorry, I didn't quite catch that </speak>", false);
	}

}