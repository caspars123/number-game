package com.example.game.util;

import lombok.experimental.UtilityClass;

import java.time.Clock;
import java.time.OffsetDateTime;

@UtilityClass
public class DateUtil {

	private static Clock CLOCK = Clock.systemUTC();

	public static OffsetDateTime now() {
		return OffsetDateTime.now(CLOCK);
	}
}
