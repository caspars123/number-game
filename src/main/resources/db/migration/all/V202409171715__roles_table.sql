CREATE TABLE roles
(
	id          UUID PRIMARY KEY,
	name        VARCHAR,
	label       VARCHAR,
	description VARCHAR,

	CONSTRAINT roles_name_udx UNIQUE (name)
);

CREATE INDEX fk_roles_name_idx ON roles (name);

CREATE TABLE users_roles
(
	id      SERIAL NOT NULL PRIMARY KEY ,
	user_id UUID   not null,
	role_id UUID   not null,
	CONSTRAINT fk_users_id
		FOREIGN KEY (user_id)
			REFERENCES users (id),
	CONSTRAINT fk_roles_id
		FOREIGN KEY (role_id)
			REFERENCES roles (id)
);

insert into roles (id, name, label, description)
values ('12cc398d-1259-461f-9328-81a0a2164a73', 'ROLE_USER', 'User',
        'Regular player'),
       ('3443178c-48c2-4dad-8e24-68763c8be285', 'ROLE_ADMIN', 'Administrator',
        'Admin user');