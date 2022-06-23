drop table if exists user_filmorate cascade;
drop table user_friends cascade;
drop table if exists FILM cascade;
drop table if exists genre cascade;
drop table if exists MPA cascade;
drop table if exists film_genre cascade;
drop table if exists like_status cascade;

CREATE TABLE IF NOT EXISTS user_filmorate (
id int GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
email varchar(255) UNIQUE,
login varchar(255),
name varchar(255),
birthday date
);

CREATE TABLE IF NOT EXISTS user_friends (
friend_id int REFERENCES user_filmorate (id),
user_filmorate_id int REFERENCES user_filmorate (id),
status boolean,
PRIMARY KEY (friend_id, user_filmorate_id)
);

CREATE TABLE IF NOT EXISTS film (
id int GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
name varchar(255),
description varchar(255),
duration int,
release_date date,
rate int,
MPA_id int,
UNIQUE (name, description, duration)
);

CREATE TABLE IF NOT EXISTS genre (
id int PRIMARY KEY,
name varchar(255)
);

CREATE TABLE IF NOT EXISTS MPA (
id int PRIMARY KEY,
name varchar(255)
);

CREATE TABLE IF NOT EXISTS film_genre (
film_id bigint,
genre_id int,
PRIMARY KEY (film_id, genre_id)
);

CREATE TABLE IF NOT EXISTS like_status (
film_id int,
user_id int,
PRIMARY KEY (film_id, user_id)
);

ALTER TABLE film ADD FOREIGN KEY (MPA_id) REFERENCES MPA (id);

ALTER TABLE film_genre ADD FOREIGN KEY (film_id) REFERENCES film (id);

ALTER TABLE film_genre ADD FOREIGN KEY (genre_id) REFERENCES genre (id);

ALTER TABLE like_status ADD FOREIGN KEY (user_id) REFERENCES user_filmorate (id);

ALTER TABLE like_status ADD FOREIGN KEY (film_id) REFERENCES film (id);
