CREATE TABLE round
(
	id           UUID        NOT NULL,
	started_at   TIMESTAMPTZ NOT NULL,
	result       NUMERIC,
	total_bet    NUMERIC,
	total_won    NUMERIC,
	player_count int,
	finished_at  TIMESTAMPTZ,
	result_at    TIMESTAMPTZ,

	CONSTRAINT round_pkey PRIMARY KEY (id)
);
