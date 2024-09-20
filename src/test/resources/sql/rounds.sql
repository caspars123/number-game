DELETE FROM bet;
DELETE FROM round;

INSERT INTO round(id, started_at, result, total_bet, player_count)
VALUES ('52a8b1cf-99ff-41de-b338-d4aa240a9411', '2024-09-17-T12:00:00Z', 5, 525.55, 2),
       ('8445db2c-7ab2-4b1c-98ea-1fd9f2b351f8', '2024-09-17-T13:00:00Z', null, null, null);