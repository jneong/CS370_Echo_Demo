package com.neong.voice.wolfpack;

import java.sql.Timestamp;

public class DateManip {
	/**
	 * Converts the given date in the format of yyyy-MM-dd to a timestamp
	 * object.
	 * 
	 * @param date
	 * @return a timestamp with the given date at midnight (00:00:00)
	 */
	public static Timestamp dateToTimestamp(String date){
		return Timestamp.valueOf(date + " 00:00:00");
	}
}
