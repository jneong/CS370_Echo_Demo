package com.neong.voice.wolfpack;

import com.amazon.speech.slu.Intent;
import com.amazon.speech.slu.Slot;
import com.amazon.speech.speechlet.IntentRequest;
import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.SpeechletResponse;

import com.neong.voice.wolfpack.CalendarHelper;
import com.neong.voice.wolfpack.CosineSim;
import com.neong.voice.wolfpack.DateRange;

import com.neong.voice.model.base.Conversation;

import com.wolfpack.database.DbConnection;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;


public class CalendarConversation extends Conversation {
	// Intent names
	private final static String INTENT_NEXTEVENT = "NextEventIntent";
	private final static String INTENT_GETEVENTSONDATE = "GetEventsOnDateIntent";
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
		SessionState state;
		String stateAttrib = (String) session.getAttribute(ATTRIB_STATEID);

		if (stateAttrib == null)
			state = null;
		else
		    state = SessionState.valueOf(stateAttrib);

		switch (state) {
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

		String eventFormat = "The next event is {title}, on {start:date} at {start:time}.";
		String eventSsml = CalendarHelper.formatEventSsml(eventFormat, results);
		String repromptSsml = "Is there anything you would like to know about this event?";

		Map<String, Integer> savedEvent = CalendarHelper.extractEventIds(results, 1);

		session.setAttribute(ATTRIB_RECENTLYSAIDEVENTS, savedEvent);
		session.setAttribute(ATTRIB_STATEID, SessionState.USER_HEARD_EVENTS);
		session.removeAttribute(ATTRIB_SAVEDDATE);

		return newAffirmativeResponse(eventSsml, repromptSsml);
	}


	private SpeechletResponse handleGetEventsOnDateIntent(IntentRequest intentReq, Session session) {
		SpeechletResponse response;

		Slot dateSlot = intentReq.getIntent().getSlot(SLOT_AMAZON_DATE);
		String givenDate;

		if (dateSlot == null || (givenDate = dateSlot.getValue()) == null)
			return newBadSlotResponse("date");

		DateRange dateRange = new DateRange(givenDate);

		// Select all events on the same day as the givenDate.
		Map<String, Vector<Object>> results;

		try {
			String query = "SELECT event_id, title, start, location FROM event_info " +
				"WHERE start >= ?::date AND start < ?::date";

			PreparedStatement ps = db.prepareStatement(query);
			ps.setDate(1, dateRange.getBegin());
			ps.setDate(2, dateRange.getEnd());

			results = DbConnection.executeStatement(ps);
		} catch (SQLException e) {
			System.out.println(e);
			return newInternalErrorResponse();
		}

		// If Alexa couldn't connect to the database or run the query:
		if (results == null)
			return newInternalErrorResponse();

		int numEvents = results.get("title").size();

		// If there were not any events on the given day:
		if (numEvents == 0) {
			String dateSsml = dateRange.getDateSsml();
			String responseSsml = "I couldn't find any events " + dateRange.getRelativeDate(true) + ".";
			String repromptSsml = "Can I help you find another event?";

			return newFailureResponse(responseSsml, repromptSsml);
		}

		Timestamp start = (Timestamp) results.get("start").get(0);

		if (numEvents <= MAX_EVENTS) {
			Map<String, Integer> savedEvents = CalendarHelper.extractEventIds(results, numEvents);

			session.setAttribute(ATTRIB_RECENTLYSAIDEVENTS, savedEvents);
			session.setAttribute(ATTRIB_STATEID, SessionState.USER_HEARD_EVENTS);

			String responsePrefix = "The events ";

			response = newEventListResponse(results, dateRange, responsePrefix);
		} else { // more than MAX_EVENTS
			session.setAttribute(ATTRIB_STATEID, SessionState.LIST_TOO_LONG);

			String dateSsml = dateRange.getDateSsml();
			String responseSsml = "I was able to find " + numEvents + " different events " +
					dateRange.getRelativeDate(true) +  
					". What kind of events would you like to hear about?";
			// TODO: only prompt for categories found in the list
			String repromptSsml = "Would you like to hear about sports, entertainment, " +
				"clubs, lectures, or all of the events?";

			response = newAffirmativeResponse(responseSsml, repromptSsml);
		}

		session.setAttribute(ATTRIB_SAVEDDATE, dateRange);

		return response;
	}


	private SpeechletResponse handleNarrowDownIntent(IntentRequest intentReq, Session session, String category) {
		@SuppressWarnings("unchecked")
		Map<String, Object> dateRangeAttrib =
			(Map<String, Object>) session.getAttribute(ATTRIB_SAVEDDATE);

		// This should never happen.
		if (dateRangeAttrib == null)
			return newBadStateResponse();

		DateRange dateRange = new DateRange(dateRangeAttrib);

		// Return the name and the time of all events within that category, or if
		// the query finds that there are no events on the day, Alexa tells the user
		// she has nothing to return.
		Map<String, Vector<Object>> results;

		try {
			PreparedStatement ps;
			int position = 1;

			if (category == "all") {
				String query =
					"SELECT event_id, title, start, location FROM event_info " +
					"    WHERE start >= ?::date AND start < ?::date";
				ps = db.prepareStatement(query);
			} else {
				String query =
					"SELECT event_id, title, start, location FROM given_category(?, ?::date, ?::date)";
				ps = db.prepareStatement(query);
				ps.setString(position++, category);
			}

			ps.setDate(position++, dateRange.getBegin());
			ps.setDate(position, dateRange.getEnd());

			results = DbConnection.executeStatement(ps);
		} catch (SQLException e) {
			System.out.println(e);
			return newInternalErrorResponse();
		}

		int numEvents = results.get("title").size();
		if (category.equals("all"))
			category="";
		if (numEvents == 0) {
			// There will always be events for "all", or else we wouldn't be here.
			String responseSsml = "I couldn't find any " + category + " events.";
			String repromptSsml = "Can I help you find another event?";

			return newFailureResponse(responseSsml, repromptSsml);
		}

		Map<String, Integer> savedEvents = CalendarHelper.extractEventIds(results, numEvents);

		session.setAttribute(ATTRIB_RECENTLYSAIDEVENTS, savedEvents);
		session.setAttribute(ATTRIB_STATEID, SessionState.USER_HEARD_EVENTS);

		Timestamp start = (Timestamp) results.get("start").get(0);

		// Format the first part of the response to indicate the category.
		String categoryPrefix = "Cool. Here are the " + category + " events that I was able to find. ";


		return dayByDayEventsResponse(results, dateRange, categoryPrefix);
	}


	private SpeechletResponse handleGetFeeDetailsIntent(IntentRequest intentReq, Session session) {
		@SuppressWarnings("unchecked")
		Map<String, Integer> savedEvents =
			(HashMap<String, Integer>) session.getAttribute(ATTRIB_RECENTLYSAIDEVENTS);

		if (savedEvents == null)
			return newBadStateResponse();

		Slot eventSlot = intentReq.getIntent().getSlot(SLOT_EVENT_NAME);
		String eventNameSlotValue;

		if (eventSlot == null || (eventNameSlotValue = eventSlot.getValue()) == null)
			return newBadSlotResponse("event");

		String eventName = CosineSim.getBestMatch(eventNameSlotValue, savedEvents.keySet());
		Integer eventId = savedEvents.get(eventName);

		Map<String, Vector<Object>> results;

		try {
			String query =
				"SELECT title, general_admission_fee FROM events " +
				"    WHERE event_id = ?";

			PreparedStatement ps = db.prepareStatement(query);
			ps.setInt(1, eventId);

			results = DbConnection.executeStatement(ps);
		} catch (SQLException e) {
			System.out.println(e);
			return newInternalErrorResponse();
		}

		if (results.get("title").size() == 0)
			return newInternalErrorResponse();

		String eventFormat = "General admission for {title} is {general_admission_fee}.";

		String eventSsml = CalendarHelper.formatEventSsml(eventFormat, results);

		return newAffirmativeResponse(eventSsml, "Would you like anymore info?");
	}


	private SpeechletResponse handleGetLocationDetailsIntent(IntentRequest intentReq, Session session) {
		@SuppressWarnings("unchecked")
		Map<String, Integer> savedEvents =
			(HashMap<String, Integer>) session.getAttribute(ATTRIB_RECENTLYSAIDEVENTS);

		if (savedEvents == null)
			return newBadStateResponse();

		Slot eventSlot = intentReq.getIntent().getSlot(SLOT_EVENT_NAME);
		String eventNameSlotValue;

		if (eventSlot == null || (eventNameSlotValue = eventSlot.getValue()) == null)
			return newBadSlotResponse("event");

		String eventName = CosineSim.getBestMatch(eventNameSlotValue, savedEvents.keySet());
		Integer eventId = savedEvents.get(eventName);

		Map<String, Vector<Object>> results;

		try {
			String query =
				"SELECT title, location FROM event_info " +
				"    WHERE event_id = ?";

			PreparedStatement ps = db.prepareStatement(query);
			ps.setInt(1, eventId);

			results = DbConnection.executeStatement(ps);
		} catch (SQLException e) {
			System.out.println(e);
			return newInternalErrorResponse();
		}

		if (results.get("title").size() == 0)
			return newInternalErrorResponse();

		String eventFormat = "Alrighty, {title} is located at {location}.";

		String eventSsml = CalendarHelper.formatEventSsml(eventFormat, results);

		return newAffirmativeResponse(eventSsml, "Would you like to hear anything else?");
	}


	private SpeechletResponse handleGetEndTimeIntent(IntentRequest intentReq, Session session) {
		@SuppressWarnings("unchecked")
		Map<String, Integer> savedEvents =
			(HashMap<String, Integer>) session.getAttribute(ATTRIB_RECENTLYSAIDEVENTS);

		if (savedEvents == null)
			return newBadStateResponse();

		Slot eventSlot = intentReq.getIntent().getSlot(SLOT_EVENT_NAME);
		String eventNameSlotValue;

		if (eventSlot == null || (eventNameSlotValue = eventSlot.getValue()) == null)
			return newBadSlotResponse("event");

		String eventName = CosineSim.getBestMatch(eventNameSlotValue, savedEvents.keySet());
		Integer eventId = savedEvents.get(eventName);

		Map<String, Vector<Object>> results;

		try {
			String query =
				"SELECT title, 'end' FROM events " +
				"    WHERE event_id = ?";

			PreparedStatement ps = db.prepareStatement(query);
			ps.setInt(1, eventId);

			results = DbConnection.executeStatement(ps);
		} catch (SQLException e) {
			System.out.println(e);
			return newInternalErrorResponse();
		}

		if (results.get("title").size() == 0)
			return newInternalErrorResponse();

		String eventFormat = "Okay, {title} ends at {end:time}.";
		String eventSsml = CalendarHelper.formatEventSsml(eventFormat, results);

		return newAffirmativeResponse(eventSsml, "Would you like to hear more?");
	}


	/**
	 * Generic response for a list of events on a given date
	 */
	private static SpeechletResponse newEventListResponse(Map<String, Vector<Object>> results,
	                                                      DateRange when, String prefix) {
		String dateSsml = when.getRelativeDate(true);
		String eventFormat = "<s>{title} at {start:time}</s>";
		String eventsSsml = CalendarHelper.listEvents(eventFormat, results);
		String responseSsml = prefix + dateSsml + " are: " + eventsSsml;
		String repromptSsml = "Is there anything you would like to know about those events?";

		return newAffirmativeResponse(responseSsml, repromptSsml);
	}
	
	
	/**
	 * 
	 * @param results	The results from a query. There must be start and title columns.
	 * @param when		A dateRange built from results.
	 * @param prefix	An introductory sentence. Example - "Okay, here is what I found."
	 * 					Pass an empty string if there should not be a prefix.
	 * @return			A string with a message such as "<prefix.> On <day> is <event>
	 * 					at <time>, <event2> at <time2>... On <day2> there is...etc.
	 */
	private static SpeechletResponse dayByDayEventsResponse(Map<String, Vector<Object>> results,
													DateRange when, String prefix){
		String eventFormat = "{title} at {start:time}";
		
		String responseSsml = prefix + CalendarHelper.listEventsWithDays(eventFormat, results);
		String repromptSsml = "Is there anything you would like to know about those events?";
		return newAffirmativeResponse(responseSsml, repromptSsml);
	}


	/**
	 * Generic response for when we have no information about the requested item
	 */
	private static SpeechletResponse newNoInfoResponse(String messageSsml) {
		return newFailureResponse(messageSsml, "Did you want any other information?");
	}


	/**
	 * Generic response for when we are missing a needed slot
	 */
	private static SpeechletResponse newBadSlotResponse(String slotName) {
		// FIXME: needs better messages?
		String messageSsml = "Which " + slotName + " are you interested in?";

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
		return newAskResponse("<speak>" + responseSsml + "</speak>", true,
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
