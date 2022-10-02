create type status as enum  ('WAITING', 'APPROVED', 'REJECTED', 'CANCELED');
create type state as enum ('ALL', 'CURRENT', 'FUTURE', 'WAITING', 'REJECTED');

CREATE TABLE IF NOT EXISTS users (
                                     id BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
                                     name VARCHAR(100) NOT NULL UNIQUE,
                                     email VARCHAR(100) NOT NULL UNIQUE,
                                     CONSTRAINT pk_user PRIMARY KEY (id),
                                     CONSTRAINT UQ_USER_EMAIL UNIQUE (email)
);

CREATE TABLE IF NOT EXISTS items (
                                     id BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
                                     name VARCHAR(100) NOT NULL,
                                     description VARCHAR(512) NOT NULL,
                                     owner_id BIGINT NOT NULL,
                                     available boolean NOT NULL,
                                     request_id BIGINT,
                                     CONSTRAINT pk_item PRIMARY KEY (id),
                                     CONSTRAINT owner_of_item FOREIGN KEY(owner_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS bookings (
                                        id BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
                                        start timestamp NOT NULL ,
                                        finish timestamp NOT NULL,
                                        item_id BIGINT NOT NULL,
                                        booker_id BIGINT NOT NULL,
                                        status status NOT NULL,
                                        CONSTRAINT pk_booking PRIMARY KEY (id),
                                        CONSTRAINT item_for_booking FOREIGN KEY(item_id) REFERENCES items(id),
                                        CONSTRAINT booker_of_booking FOREIGN KEY(booker_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS requests (
                                        id BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
                                        description VARCHAR(512),
                                        requestor_id BIGINT NOT NULL,
                                        create_date timestamp,
                                        CONSTRAINT pk_request PRIMARY KEY (id),
                                        CONSTRAINT request_user_id FOREIGN KEY(requestor_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS comments (
                                        id BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
                                        item_id BIGINT NOT NULL,
                                        author_name VARCHAR,
                                        text VARCHAR NOT NULL,
                                        CONSTRAINT pk_comments PRIMARY KEY (id),
                                        CONSTRAINT comment_item_id FOREIGN KEY(item_id) REFERENCES items(id)

);