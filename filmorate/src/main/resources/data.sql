CREATE TABLE IF NOT EXISTS genre (
id int PRIMARY KEY,
name varchar(255)
);

CREATE TABLE IF NOT EXISTS mpa (
id int PRIMARY KEY,
name varchar(255)
);

insert into mpa values ('1', 'G');
insert into mpa values ('2', 'PG');
insert into mpa values ('3', 'PG-13');
insert into mpa values ('4', 'R');
insert into mpa values ('5', 'NC-17');

insert into genre values ('1', 'Комедия');
insert into genre values ('2', 'Драма');
insert into genre values ('3', 'Мультфильм');
insert into genre values ('4', 'Триллер');
insert into genre values ('5', 'Документальный');
insert into genre values ('6', 'Боевик');