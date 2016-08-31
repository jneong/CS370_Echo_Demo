package com.neong.voice.example;

import com.amazon.speech.slu.Intent;
	
import com.amazon.speech.speechlet.IntentRequest;
import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.neong.voice.model.base.Conversation;

/**
 * This is an example implementation of a Conversation subclass. It is
 * important to register your intents by adding them to the supportedIntentNames
 * array in the constructor. Your conversation must internally track the current
 * state of the conversation and all state transitions so that it feels natural.
 * The state machine below is the simplest of examples so feel free to create a
 * more robust state-machine object for your more complex needs.
 * 
 * @author Jeffrey Neong
 * @version 1.0
 * 
 */

public class KnockKnockConversation extends Conversation {
	//Intent names
	private final static String INTENT_START = "StartKnockIntent";
	private final static String INTENT_WHO_DER = "WhoDerIntent";
	private final static String INTENT_DR_WHO = "DrWhoIntent";

	//Slots
	//private final static String SLOT_RELATIVE_TIME = "timeOfDay";

	//State
	private final static Integer STATE_WAITING_WHO_DER = 100000;
	private final static Integer STATE_WAITING_DR_WHO = 100001;

	//Session state storage key
	private final static String SESSION_KNOCK_STATE = "knockState";

	public KnockKnockConversation() {
		super();
		
		//Add custom intent names for dispatcher use
		supportedIntentNames.add(INTENT_START);
		supportedIntentNames.add(INTENT_WHO_DER);
		supportedIntentNames.add(INTENT_DR_WHO);

	}


	@Override
	public SpeechletResponse respondToIntentRequest(IntentRequest intentReq, Session session) {
		Intent intent = intentReq.getIntent();
		String intentName = (intent != null) ? intent.getName() : null;
		SpeechletResponse response = null;
		
		if (INTENT_START.equals(intentName)) {
			response = handleStartJokeIntent(intentReq, session);
        }
		else if (INTENT_WHO_DER.equals(intentName)) {
			response = handleWhoThereIntent(intentReq, session);
        }
		else if (INTENT_DR_WHO.equals(intentName)) {
			response = handleDrWhoIntent(intentReq, session);
        }
		else {
			response = newTellResponse("Whatchu talkin' bout!", false);
		}
		
		return response;
	}

	
	private SpeechletResponse handleStartJokeIntent(IntentRequest intentReq, Session session) {
		SpeechletResponse response = newAskResponse("Knock knock.", false, "I said, Knock knock!", false);
		session.setAttribute(SESSION_KNOCK_STATE, STATE_WAITING_WHO_DER);
		return response;	
	}
	
	private SpeechletResponse handleWhoThereIntent(IntentRequest intentReq, Session session) {
		SpeechletResponse response = null;
		
		//check state
		if(session.getAttribute(SESSION_KNOCK_STATE) != null 
				&& STATE_WAITING_WHO_DER.compareTo((Integer)session.getAttribute(SESSION_KNOCK_STATE)) == 0) {
			response = newAskResponse("Doctor.", false," Doctor is here.",false);
			//Update state
			session.setAttribute(SESSION_KNOCK_STATE, STATE_WAITING_DR_WHO);
		}
		else {
			response = newTellResponse("You have to say knock knock first.", false);
		}
		
		return response;	
	}
	
	private SpeechletResponse handleDrWhoIntent(IntentRequest intentReq, Session session) {
		SpeechletResponse response = null;
		//check state
		if(session.getAttribute(SESSION_KNOCK_STATE) != null 
				&& STATE_WAITING_DR_WHO.compareTo((Integer)session.getAttribute(SESSION_KNOCK_STATE)) == 0) {			
			response = newTellResponse(" Exactly. How did you know?", false);
			//Clear final state
			session.removeAttribute(SESSION_KNOCK_STATE);
		}
		else {
			response = newTellResponse("You have to say knock knock first.", false);
		}
		return response;	
	}
	
}
