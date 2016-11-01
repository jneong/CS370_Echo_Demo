package com.neong.voice.wolfpack;

import com.amazon.speech.slu.Intent;
import com.amazon.speech.speechlet.IntentRequest;
import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.neong.voice.model.base.Conversation;
import com.wolfpack.database.DbConnection;
import com.neong.voice.wolfpack.CalendarHelper;
import com.neong.voice.wolfpack.CalendarHelper.EventField;
import com.neong.voice.wolfpack.CosineSim;
import com.neong.voice.wolfpack.DateManip;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;


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

	// Slot names (from the intent schema)
	private final static String SLOT_EVENT_NAME = "eventName";
	private final static String SLOT_AMAZON_DATE = "date";

	// Session attribute names
	private final static String ATTRIB_STATEID = "stateId";
	private final static String ATTRIB_SAVEDDATE = "savedDate";
	private final static String ATTRIB_RECENTLYSAIDEVENTS = "recentlySaidEvents";

	// Other constants
	private final static int MAX_EVENTS = 5;

	private DbConnection db;

	private enum SessionState {
		NEW_SESSION, // The user hasn't asked about any events yet.
		USER_HEARD_EVENTS, // The user has heard a list of events and can now ask about specific ones.
		LIST_TOO_LONG, // The list of events is too long, so the user must narrow it down somehow.
	}


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
		SpeechletResponse response;
		// Intent requests are dispatched to us by name,
		// so we always know the intent and name are non-null.
		String intentName = intentReq.getIntent().getName();

		switch (intentName) {

		/*
		 * These intents are not sensitive to session state and can be invoked at any time.
		 */

		case INTENT_NEXTEVENT:
			response = handleNextEventIntent(intentReq, session);
			break;

		case INTENT_GETEVENTSONDATE:
			response = handleGetEventsOnDateIntent(intentReq, session);
			break;

		/*
		 * The rest of the intents are sensitive to the current state of the session.
		 */

		default:
			response = handleStateSensitiveIntents(intentReq, session);
			break;
		}

		return response;
	}


	/**
	 * Dispatch intent request handlers for state-sensitive intents
	 */
	private SpeechletResponse handleStateSensitiveIntents(IntentRequest intentReq, Session session) {
		SpeechletResponse response;
		SessionState state = SessionState.valueOf(session.getAttribute(ATTRIB_STATEID).toString());

		if (state == null)
			state = SessionState.NEW_SESSION;

		switch (state) {
		case NEW_SESSION:
			// What was this state for?
			response = newBadStateResponse();
			break;

		case USER_HEARD_EVENTS:
			response = handleDetailIntents(intentReq, session);
			break;

		case LIST_TOO_LONG:
			response = handleNarrowDownIntents(intentReq, session);
			break;

		default:
			throw new IllegalStateException("Unhandled SessionState value " + state);
		}

		return response;
	}


	/**
	 * Dispatch detail intents
	 */
	private SpeechletResponse handleDetailIntents(IntentRequest intentReq, Session session) {
		SpeechletResponse response;
		String intentName = intentReq.getIntent().getName();

		switch (intentName) {
		case INTENT_GETFEEDETAILS:
			response = handleGetFeeDetailsIntent(intentReq, session);
			break;

		case INTENT_GETLOCATIONDETAILS:
			response = handleGetLocationDetailsIntent(intentReq, session);
			break;

		case INTENT_GETENDTIME:
			response = handleGetEndTimeIntent(intentReq, session);
			break;

		default:
			response = newBadStateResponse();
			break;
		}

		return response;
	}


	/**
	 * Map narrow-down intents by category
	 */
	private SpeechletResponse handleNarrowDownIntents(IntentRequest intentReq, Session session) {
		String intentName = intentReq.getIntent().getName();
		String category;

		switch (intentName) {
		case INTENT_ALLCATEGORY:
			category = "all";
			break;

		case INTENT_SPORTSCATEGORY:
			category = "Athletics";
			break;

		case INTENT_ARTSANDENTERTAINMENTCATEGORY:
			category = "Arts and Entertainment";
			break;

		case INTENT_LECTURESCATEGORY:
			category = "Lectures and Films";
			break;

		case INTENT_CLUBSCATEGORY:
			category = "Club and Student Organizations";
			break;

		default:
			// TODO: Should inform the user what the categories are
			return newTellResponse("Sorry, I'm not quite sure what you meant.", false);
		}

		return handleNarrowDownIntent(intentReq, session, category);
	}


	private SpeechletResponse handleNextEventIntent(IntentRequest intentReq, Session session) {
		Map<String, Vector<Object>> results =
			db.runQuery("SELECT * FROM event_info WHERE start > now() LIMIT 1;");

		if (results == null)
			return newInternalErrorResponse();

		EventField[] fields = { EventField.SUMMARY, EventField.DATE, EventField.TIME };
		String eventSsml = CalendarHelper.formatEventSsml(0, results, fields);
		String responseSsml = "The next event is " + eventSsml;
		String repromptSsml = "Is there anything you would like to know about this event?";

		return newAffirmativeResponse(responseSsml, repromptSsml);
	}


	private SpeechletResponse handleGetEventsOnDateIntent(IntentRequest intentReq, Session session) {
		SpeechletResponse response;

		Intent theIntent = intentReq.getIntent();
		String givenDate = theIntent.getSlot(SLOT_AMAZON_DATE).getValue();

		// Select all events on the same day as the givenDate.
		Map<String, Vector<Object>> results;

		try {
			//CHANGE 'ARTS AND ENTERTAINMENT' TO 'ALL' ONCE GIVEN_CATEGORY() IS UPDATED
			String query = "SELECT * FROM given_category(text 'Club and Student Organizations', ? , " +
				"1::smallint)";

			PreparedStatement ps = db.prepareStatement(query);
			ps.setDate(1, java.sql.Date.valueOf(givenDate));
			results = db.executeStatement(ps);
		} catch (SQLException e) {
			System.out.println(e);
			return newInternalErrorResponse();
		}

		// If Alexa couldn't connect to the database or run the query:
		if (results == null)
			return newInternalErrorResponse();

		int numEvents = results.get("summary").size();

		// If there were not any events on the given day:
		if (numEvents == 0) {
			Timestamp ts = DateManip.dateToTimestamp(givenDate);
			String dateSsml = CalendarHelper.formatDateSsmlNoZone(ts);
			String responseSsml = "I couldn't find any events on " + dateSsml + ".";
			String repromptSsml = "Can I help you find another event?";
			return newFailureResponse(responseSsml, repromptSsml);
		}

		Timestamp start = (Timestamp) results.get("start").get(0);

		if (numEvents <= MAX_EVENTS) {
			Map<String, Integer> savedEventNames = new HashMap<String, Integer>(numEvents);

			for (int i = 0; i < numEvents; i++) {
				String key = results.get("summary").get(i).toString();
				Integer value = (Integer) results.get("event_id").get(i);
				savedEventNames.put(key, value);
			}

			session.setAttribute(ATTRIB_RECENTLYSAIDEVENTS, savedEventNames);
			session.setAttribute(ATTRIB_SAVEDDATE, DateManip.dateToTimestamp(givenDate));
			session.setAttribute(ATTRIB_STATEID, SessionState.USER_HEARD_EVENTS);

			response = newEventListResponse(results, DateManip.dateToTimestamp(givenDate));
		} else { // more than MAX_EVENTS
			session.setAttribute(ATTRIB_STATEID, SessionState.LIST_TOO_LONG);
			session.setAttribute(ATTRIB_SAVEDDATE, DateManip.dateToTimestamp(givenDate));

			String responseSsml = "I was able to find " + numEvents + " different events. " +
				"What kind of events would you like to hear about?";
			String repromptSsml = "Would you like to hear about sports, entertainment, " +
				"clubs, lectures, or all of the events?";

			response = newAffirmativeResponse(responseSsml, repromptSsml);
		}

		return response;
	}


	private SpeechletResponse handleNarrowDownIntent(IntentRequest intentReq, Session session, String category) {
		Timestamp start = new Timestamp((long) session.getAttribute(ATTRIB_SAVEDDATE));

		// This should never happen.
		if (start == null)
			return newBadStateResponse();

		// Return the name and the time of all events within that category, or if
		// the query finds that there are no events on the day, Alexa tells the user
		// she has nothing to return.
		String query;
		Map<String, Vector<Object>> results;

		if (category == "all") {
			query = "SELECT event_id, summary, start FROM event_info " +
				"WHERE ('" + start + "' <= start) " +
				"AND ('" + start + "'::date + integer '1') > start;";
		} else {
			query = "SELECT event_id, summary, start " +
				"FROM given_category('" + category + "', '" + start + "', 1::smallint);";
		}

		results = db.runQuery(query);

		int numEvents = results.get("summary").size();

		if (numEvents == 0)
			return newTellResponse("<speak>There are no events happening in" +
			                       category + "on that day.</speak>", true);

		Map<String, Integer> savedEventNames = new HashMap<String, Integer>(numEvents);

		for (int i = 0; i < numEvents; i++) {
			String key = results.get("summary").get(i).toString();
			Integer value = (Integer) results.get("event_id").get(i);
			savedEventNames.put(key, value);
		}

		session.setAttribute(ATTRIB_RECENTLYSAIDEVENTS, savedEventNames);
		session.setAttribute(ATTRIB_STATEID, SessionState.USER_HEARD_EVENTS);

		return newEventListResponse(results, start);
	}


	private SpeechletResponse handleGetFeeDetailsIntent(IntentRequest intentReq, Session session) {
		Intent theIntent = intentReq.getIntent();
		String eventNameSlotValue = theIntent.getSlot(SLOT_EVENT_NAME).getValue();

		if (session.getAttribute(ATTRIB_RECENTLYSAIDEVENTS) == null)
			return newTellResponse("Wait for me to mention some events first.", false);

		Map<String, Integer> savedEvents =
			(HashMap<String, Integer>) session.getAttribute(ATTRIB_RECENTLYSAIDEVENTS);

		System.out.println("I WAS GIVEN THE EVENT NAME: " + eventNameSlotValue);
		String eventName = CosineSim.getBestMatch(eventNameSlotValue, savedEvents.keySet());
		System.out.println("I'M THINKING THE CLOSEST NAME IS: " + eventName);

		Integer eventId = (Integer) savedEvents.get(eventName);

		Map<String, Vector<Object>> results =
			db.runQuery("SELECT summary, general_admission_fee FROM events WHERE event_id = '" + eventId + "';");

		if (results.get("general_admission_fee").get(0) == null) {
			String responseSsml = "There is no price is listed for " + eventName + ".";
			return newNoInfoResponse(responseSsml);
		}

		String fee = results.get("general_admission_fee").get(0).toString();
		fee = fee.replace("-", " to ");
		return newAffirmativeResponse("The general admission fee is " + fee + ".",
		                              "I'm sorry, I didn't quite catch that.");
	}


	private SpeechletResponse handleGetLocationDetailsIntent(IntentRequest intentReq, Session session) {
		Intent theIntent = intentReq.getIntent();
		String eventNameSlotValue = theIntent.getSlot(SLOT_EVENT_NAME).getValue();

		if (session.getAttribute(ATTRIB_RECENTLYSAIDEVENTS) == null)
			return newBadStateResponse();

		Map<String, Integer> savedEvents =
			(HashMap<String, Integer>) session.getAttribute(ATTRIB_RECENTLYSAIDEVENTS);

		System.out.println("I WAS GIVEN THE EVENT NAME: " + eventNameSlotValue);
		String eventName = CosineSim.getBestMatch(eventNameSlotValue, savedEvents.keySet());
		System.out.println("I'M THINKING THE CLOSEST NAME IS: " + eventName);

		Integer eventId = (Integer) savedEvents.get(eventName);

		Map<String, Vector<Object>> results =
			db.runQuery("SELECT summary, location FROM event_info WHERE event_id = '" + eventId + "';");

		if (results.get("location").get(0) == null) {
			String responseSsml = "This event doesn't specify a location.";
			return newNoInfoResponse(responseSsml);
		}

		String event = results.get("summary").get(0).toString();
		String location = results.get("location").get(0).toString();
		String locationSsml = CalendarHelper.formatLocationSsml(location);
		String responseSsml = "The " + event + " is located at " + locationSsml + ".";
		String repromptSsml = "I'm sorry, I didn't quite catch that.";

		return newAffirmativeResponse(responseSsml, repromptSsml);
	}


	private SpeechletResponse handleGetEndTimeIntent(IntentRequest intentReq, Session session) {
		Intent theIntent = intentReq.getIntent();
		String eventNameSlotValue = theIntent.getSlot(SLOT_EVENT_NAME).getValue();

		if (session.getAttribute("recentlySaidEvents") == null)
			return newTellResponse("wait for me to mention some events first.", false);

		Map<String, Integer> savedEvents =
			(HashMap<String, Integer>) session.getAttribute(ATTRIB_RECENTLYSAIDEVENTS);

		System.out.println("I WAS GIVEN THE EVENT NAME: " + eventNameSlotValue);
		String eventName = CosineSim.getBestMatch(eventNameSlotValue, savedEvents.keySet());
		System.out.println("I'M THINKING THE CLOSEST NAME IS: " + eventName);

		Integer eventId = (Integer) savedEvents.get(eventName);

		Map<String, Vector<Object>> results =
			db.runQuery("SELECT summary, \"end\" FROM events WHERE event_id = '" + eventId + "';");

		Timestamp end = (Timestamp) results.get("end").get(0);
		String timeSsml = CalendarHelper.formatTimeSsml(end);
		String event = results.get("summary").get(0).toString();
		String responseSsml = "The " + event + " ends at " + timeSsml + ".";
		String repromptSsml = "I'm sorry, I didn't quite catch that.";

		return newAffirmativeResponse(responseSsml, repromptSsml);
	}


	/**
	 * Generic response for a list of events on a given date
	 */
	private static SpeechletResponse newEventListResponse(Map<String, Vector<Object>> results,
	                                                      Timestamp when) {
		EventField[] fields = { EventField.SUMMARY, EventField.TIME };
		String dateSsml = CalendarHelper.formatDateSsmlNoZone(when);
		String eventsSsml = CalendarHelper.listEvents(results, fields);
		String responseSsml = "The events on " + dateSsml + " are: " + eventsSsml;
		String repromptSsml = "Is there anything you would like to know about those events?";

		return newAffirmativeResponse(responseSsml, repromptSsml);
	}


	/**
	 * Generic response for when we have no information about the requested item.
	 */
	private static SpeechletResponse newNoInfoResponse(String messageSsml) {
		return newFailureResponse(messageSsml, "Did you want any other information?");
	}


	/**
	 * Generic affirmative response wrapper
	 *
	 * @param responseSsml the main message to respond with.  SSML markup is allowed, but the string
	 *                     should not include {@code <speak>...</speak>} tags.
	 * @param repromptSsml the message to speak to the user if they do not respond within the timeout
	 *                     window.  SSML markup is allowed, but the string should not include
	 *                     {@code <speak>...</speak>} tags.
	 * @return a new ask response that keeps the session open and prepends "Okay. " to the front of
	 *         the specified response message.  Both the response message and the reprompt message
	 *         get wrapped in {@code <speak>...</speak>} tags.
	 */
	private static SpeechletResponse newAffirmativeResponse(String responseSsml, String repromptSsml) {
		return newAskResponse("<speak>Okay. " + responseSsml + "</speak>", true,
		                      "<speak>" + repromptSsml + "</speak>", true);
	}


	/**
	 * Generic failure response wrapper
	 *
	 * @param responseSsml the main message to respond with.  SSML markup is allowed, but the string
	 *                     should not include {@code <speak>...</speak>} tags.
	 * @param repromptSsml the message to speak to the user if they do not respond within the timeout
	 *                     window.  SSML markup is allowed, but the string should not include
	 *                     {@code <speak>...</speak>} tags.
	 * @return a new ask response that keeps the session open and prepends "Sorry. " to the front of
	 *         the specified response message.  Both the response message and the reprompt message
	 *         get wrapped in {@code <speak>...</speak>} tags.
	 */
	private static SpeechletResponse newFailureResponse(String responseSsml, String repromptSsml) {
		return newAskResponse("<speak>Sorry. " + responseSsml + "</speak>", true,
		                      "<speak>" + repromptSsml + "</speak>", true);
	}

	/**
	 * Generic response for when we experience an internal error
	 */
	private SpeechletResponse newInternalErrorResponse() {
		return newTellResponse("Sorry, I'm on break", false);
	}

	/**
	 * Generic response for when we don't know what's going on
	 */
	private SpeechletResponse newBadStateResponse() {
		return newTellResponse("Sorry, I forgot what we were talking about.", false);
	}
}
