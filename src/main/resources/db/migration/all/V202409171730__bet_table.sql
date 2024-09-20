CREATE TABLE bet
(
	id            UUID        NOT NULL,
	round_id      UUID        NOT NULL,
	user_id       UUID        NOT NULL,
	chosen_number SMALLINT    NOT NULL,
	amount        NUMERIC     NOT NULL,
	placed_at     TIMESTAMPTZ NOT NULL,

	CONSTRAINT bet_round_fkey FOREIGN KEY (round_id) REFERENCES round(id),
	CONSTRAINT bet_user_fkey FOREIGN KEY (user_id) REFERENCES users(id),
	CONSTRAINT bet_pkey PRIMARY KEY (id)
);
