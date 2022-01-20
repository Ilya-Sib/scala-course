CREATE
DATABASE myimdb;
\c
myimdb;

CREATE TABLE users
(
    id            SERIAL PRIMARY KEY,
    username      VARCHAR(64)           NOT NULL,
    email         VARCHAR(128)          NOT NULL,
    password_sha  VARCHAR(255)          NOT NULL,
    admin         BOOLEAN DEFAULT FALSE NOT NULL,
    creation_time TIMESTAMP             NOT NULL
);

CREATE UNIQUE INDEX users_username_unique ON users (username);
CREATE UNIQUE INDEX users_email_unique ON users (email);
CREATE INDEX users_password_sha_index ON users (password_sha);

CREATE TABLE authors
(
    id         SERIAL PRIMARY KEY,
    first_name VARCHAR(64) NOT NULL,
    last_name  VARCHAR(64) NOT NULL
);

CREATE INDEX authors_first_name_index ON authors (first_name);
CREATE INDEX authors_last_name_index ON authors (last_name);

CREATE TABLE books
(
    id          SERIAL PRIMARY KEY,
    title       VARCHAR(64) NOT NULL,
    description TEXT DEFAULT NULL
);

CREATE INDEX books_title_index ON books (title);

CREATE TABLE books_author
(
    book_id   INTEGER NOT NULL REFERENCES books (id),
    author_id INTEGER NOT NULL REFERENCES authors (id),
    PRIMARY KEY (book_id, author_id)
);

CREATE TABLE books_user
(
    book_id INTEGER NOT NULL REFERENCES books (id),
    user_id INTEGER NOT NULL REFERENCES users (id),
    PRIMARY KEY (book_id, user_id)
);


