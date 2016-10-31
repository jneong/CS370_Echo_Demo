package com.neong.voice.wolfpack;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.sql.Date;
import java.sql.Timestamp;

import java.util.Map;

import com.neong.voice.wolfpack.CalendarHelper;


@JsonAutoDetect(fieldVisibility=Visibility.ANY,
                getterVisibility=Visibility.NONE,
                isGetterVisibility=Visibility.NONE)
public class DateRange {
	@JsonProperty("date") private final Date _date;
	@JsonProperty("range") private final String _range;

	public DateRange(String dateString) {
		// TODO: actual implementation handling weeks, months, etc.
		_date = Date.valueOf(dateString);
		_range = "day";
	}

	@JsonCreator
	public DateRange(Map<String, Object> props) {
		_date = Date.valueOf((String) props.get("date"));
		_range = (String) props.get("range");
	}

	public Date getDate() {
		return _date;
	}

	public Timestamp getTimestamp() {
		return Timestamp.valueOf(_date.toString() + " 00:00:00");
	}

	public String getRange() {
		return _range;
	}

	public String getDateSsml() {
		return CalendarHelper.formatDateSsml(getTimestamp());
	}
}
