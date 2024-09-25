package com.example.game;


import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public class DbHelper {
	public static final OffsetDateTime NOW = OffsetDateTime.parse("2024-09-17T13:00:05Z");

	// User 1
	public static final UUID USER_1_ID = UUID.fromString("7d19190d-bb3e-46b5-b7d0-46aadd3d46a4");
	public static final String USER_1_EMAIL = "user1@gmail.com";
	public static final String USER_1_PASSWORD_TEXT = "password";
	public static final String USER_1_NAME = "user 1";
	public static final BigDecimal USER_1_BALANCE = BigDecimal.valueOf(5000);

	// User 2
	public static final UUID USER_2_ID = UUID.fromString("3c6ccf80-8866-4905-83e3-72f34866e7ed");
	public static final String USER_2_EMAIL = "user2@gmail.com";
	public static final String USER_2_PASSWORD_TEXT = "password";
	public static final BigDecimal USER_2_BALANCE = BigDecimal.valueOf(10000);
	public static final String USER_2_NAME = "user 2";

	// Round 1
	public static final UUID ROUND_1_ID = UUID.fromString("52a8b1cf-99ff-41de-b338-d4aa240a9411");
	public static final OffsetDateTime ROUND_1_START = OffsetDateTime.parse("2024-09-17T12:00:00Z");
	public static final BigDecimal ROUND_1_USER_1_BET_AMOUNT = BigDecimal.valueOf(400.12);
	public static final BigDecimal ROUND_1_USER_2_BET_AMOUNT = BigDecimal.valueOf(125.43);

	// Round 2
	public static final UUID ROUND_2_ID = UUID.fromString("8445db2c-7ab2-4b1c-98ea-1fd9f2b351f8");
	public static final OffsetDateTime ROUND_2_START = OffsetDateTime.parse("2024-09-17T13:00:00Z");
	public static final BigDecimal ROUND_2_USER_1_BET_AMOUNT = BigDecimal.valueOf(123.0);
	public static final BigDecimal ROUND_2_USER_2_BET_AMOUNT = BigDecimal.valueOf(321.45);
	public static final UUID ROUND_2_USER_2_BET_ID = UUID.fromString("c23e6f5c-7873-4c34-8aeb-c0cd73fcbf13");
	public static final int ROUND_2_USER_1_CHOSEN_NUMBER = 8;
	public static final int ROUND_2_USER_2_CHOSEN_NUMBER = 3;
	public static final int ROUND_2_NO_WINNERS_NUMBER = 5;

	public static final UUID LIVE_ROUND_ID = ROUND_2_ID;

	// Non existent data
	public static final UUID NON_EXISTENT_ROUND_ID = UUID.randomUUID();
	public static final OffsetDateTime NON_EXISTENT_ROUND_START = OffsetDateTime.parse("2024-09-18T12:00:00Z");
	public static final String NON_EXISTENT_USER_EMAIL = "non-existent-user@gmail.com";
}
