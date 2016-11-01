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
	@JsonProperty("begin") private final Date _begin;
	@JsonProperty("end") private final Date _end;

	public DateRange(String dateString) {
		// TODO: actual implementation handling weeks, months, etc.
		_begin = Date.valueOf(dateString);
		_end = _begin;
	}

	@JsonCreator
	public DateRange(Map<String, Object> props) {
		_begin = Date.valueOf((String) props.get("begin"));
		_end = Date.valueOf((String) props.get("end"));
	}

	public Date getBegin() {
		return _begin;
	}

	public Date getEnd() {
		return _end;
	}

	public Timestamp getTimestamp() {
		return Timestamp.valueOf(_begin.toString() + " 00:00:00");
	}

	public String getDateSsml() {
		return CalendarHelper.formatDateSsml(getTimestamp());
	}
}
