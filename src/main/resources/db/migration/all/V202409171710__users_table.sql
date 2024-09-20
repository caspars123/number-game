CREATE TABLE users
(
	id         UUID    NOT NULL,
	email      TEXT UNIQUE,
	password   VARCHAR      NOT NULL,
	name       VARCHAR(100) NOT NULL UNIQUE,
	active     BOOLEAN,
	created_at TIMESTAMPTZ  NOT NULL,
	updated_at TIMESTAMPTZ,
	balance    numeric      NOT NULL default 0,

	CONSTRAINT users_pkey PRIMARY KEY (id)
);
