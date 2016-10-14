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
import java.time.ZonedDateTime;

public class CalendarConversation extends Conversation {
	// Intent names
	private final static String INTENT_NEXTEVENT = "NextEventIntent";

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

		if (results == null)
			return newTellResponse("Sorry, I'm on break", false);

		String summary = (String) results.get("summary").get(0);
		Timestamp start = (Timestamp) results.get("start").get(0);
		String location = (String) results.get("name").get(0);

		if (location == null)
			location = "Sonoma State University";

		ZonedDateTime zonedDateTime = start.toInstant().atZone(PST);
		String date = zonedDateTime.format(DATEFORMATTER);
		String day = zonedDateTime.format(DAYFORMATTER);
		String time = zonedDateTime.format(TIMEFORMATTER);

		return newTellResponse("<speak> Okay, the next event is " + summary +
				" on " + day +  " <say-as interpret-as=\"date\">" + date +
				"</say-as> at <say-as interpret-as=\"time\">" + time + "</say-as> at "
				+ location + ". </speak>", true);
	}

}
