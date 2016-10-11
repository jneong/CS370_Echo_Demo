package com.neong.voice.wolfpack;

import com.amazon.speech.slu.Intent;
import com.amazon.speech.speechlet.IntentRequest;
import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.neong.voice.model.base.Conversation;
import com.wolfpack.database.DbConnection;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;
import java.time.LocalDate;
import java.util.*;
import java.time.ZonedDateTime;

/**
 * This is an example implementation of a Conversation subclass. It is
 * important to register your intents by adding them to the supportedIntentNames
 * array in the constructor. Your conversation must internally track the current
 * state of the conversation and all state transitions so that it feels natural.
 * The state machine below is the simplest of examples so feel free to create a
 * more robust state-machine object for your more complex needs.
 **/

public class CalendarConversation extends Conversation {
    //Intent names
    private final static String INTENT_NEXTEVENT = "NextEventIntent";
    private final static TimeZone PST = TimeZone.getTimeZone("America/Los Angeles");
    private final static DateTimeFormatter TIMEFORMATTER = DateTimeFormatter.ofPattern("h:mm a");
    private final static DateTimeFormatter DATEFORMATTER = DateTimeFormatter.ofPattern("????MMdd");

    private DbConnection db;

    public CalendarConversation() {
	super();

	db = new DbConnection("DbCredentials.xml");
	//Add custom intent names for dispatcher use
	db.getRemoteConnection();
	supportedIntentNames.add(INTENT_NEXTEVENT);

    }


    @Override
	public SpeechletResponse respondToIntentRequest(IntentRequest intentReq, Session session) {
	Intent intent = intentReq.getIntent();
	String intentName = (intent != null) ? intent.getName() : null;
	SpeechletResponse response = null;

	if (INTENT_NEXTEVENT.equals(intentName))
	    response = handleNextEventIntent(intentReq, session);
	return response;
    }


    private SpeechletResponse handleNextEventIntent(IntentRequest intentReq, Session session) {
	Map<String, Vector<Object>> results =
	    db.runQuery("SELECT * FROM ssucalendar.event_info WHERE start > now() LIMIT 1;");

	if(results == null)
	    return newTellResponse("Sorry, I'm on break", false);

	String summary = (String) results.get("summary").get(0);
	Timestamp start = (Timestamp) results.get("start").get(0);
	String location = (String) results.get("name").get(0);

	if(location == null)
	    location = "Sonoma State University";


	ZonedDateTime localDateTime = start.toLocalDateTime().atZone(PST.toZoneId());
	String date = localDateTime.toLocalDate().format(DATEFORMATTER);
	String time = localDateTime.toLocalTime().format(TIMEFORMATTER);

	return newTellResponse("<speak> Okay, the next event is " + summary +
			       " on <say-as interpret-as=\"date\">" + date +
			       "</say-as> at <say-as interpret-as=\"time\">" + time + "</say-as> at "
			       + location + ". </speak>", true);
    }

}
