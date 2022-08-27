create type status as enum ('WAITING', 'APPROVED');

CREATE TABLE IF NOT EXISTS users (
                                     id BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
                                     name VARCHAR(100) NOT NULL,
                                     email VARCHAR(100) NOT NULL,
                                     CONSTRAINT pk_user PRIMARY KEY (id),
                                     CONSTRAINT UQ_USER_EMAIL UNIQUE (email)
);

CREATE TABLE IF NOT EXISTS items (
                                     id BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
                                     name VARCHAR(100) NOT NULL,
                                     description VARCHAR(512) NOT NULL,
                                     owner_Id BIGINT NOT NULL,
                                     available boolean NOT NULL,
                                     CONSTRAINT pk_item PRIMARY KEY (id),
                                     CONSTRAINT owner_of_item FOREIGN KEY(owner_Id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS bookings (
                                     id BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
                                     start timestamp NOT NULL ,
                                     finish timestamp NOT NULL,
                                     item_Id BIGINT NOT NULL,
                                     booker_Id BIGINT NOT NULL,
                                     status status,
                                     CONSTRAINT pk_booking PRIMARY KEY (id),
                                     CONSTRAINT item_for_booking FOREIGN KEY(item_Id) REFERENCES items(id),
                                     CONSTRAINT owner_of_booking FOREIGN KEY(booker_Id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS requests (
                                     id BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
                                     description VARCHAR(512) NOT NULL,
                                     requestorId BIGINT NOT NULL,
                                     createDate timestamp,
                                     CONSTRAINT pk_request PRIMARY KEY (id),
                                     CONSTRAINT requestor_id FOREIGN KEY(requestorId) REFERENCES users(id)
);


