DELETE
FROM bet;
DELETE
FROM users_roles;
DELETE
FROM users;

INSERT INTO users(id, email, password, name, active, created_at, updated_at, balance)
VALUES ('7d19190d-bb3e-46b5-b7d0-46aadd3d46a4', 'user1@gmail.com', '$2a$10$4oTjsZ0s2/GjXEkiILEp.uGJZriIw2B4CfCbXUHUtpCgjwmcBnJDu', 'user 1', true,
        '2024-09-01-T12:00:00Z', '2024-09-01-T12:00:00Z', 5000),
       ('3c6ccf80-8866-4905-83e3-72f34866e7ed', 'user2@gmail.com', '$2a$10$4oTjsZ0s2/GjXEkiILEp.uGJZriIw2B4CfCbXUHUtpCgjwmcBnJDu', 'user 2', true,
        '2024-09-02-T12:00:00Z', '2024-09-02-T12:00:00Z', 10000);

INSERT INTO users_roles(id, user_id, role_id)
VALUES (1, '7d19190d-bb3e-46b5-b7d0-46aadd3d46a4', '12cc398d-1259-461f-9328-81a0a2164a73'),
       (2, '3c6ccf80-8866-4905-83e3-72f34866e7ed', '12cc398d-1259-461f-9328-81a0a2164a73')
