DROP DATABASE IF EXISTS imdb_api;

CREATE DATABASE imdb_api;

USE imdb_api;

-- Metadata Tables
-- Show Type
CREATE TABLE show_type(
	id INTEGER NOT NULL AUTO_INCREMENT,
	name VARCHAR(100) NOT NULL,
	PRIMARY KEY (id)
);

INSERT INTO show_type VALUES (1, "MOVIE");
INSERT INTO show_type VALUES (2, "TV_SERIES");

select * from show_type;


-- Genre
CREATE TABLE genre(
	id INTEGER NOT NULL AUTO_INCREMENT,
	name VARCHAR(100) NOT NULL,
	PRIMARY KEY (id)
);

INSERT INTO genre VALUES (1, "ACTION");
INSERT INTO genre VALUES (2, "DRAMA");
INSERT INTO genre VALUES (3, "HORROR");
INSERT INTO genre VALUES (4, "COMEDY");
INSERT INTO genre VALUES (5, "CRIME");
INSERT INTO genre VALUES (6, "THRILLER");
INSERT INTO genre VALUES (7, "SCI_FI");

select * from genre;


-- Cast Crew Type
-- Its a mapping for the position of a person in a show
CREATE TABLE cast_crew_type(
	id INTEGER NOT NULL AUTO_INCREMENT,
	name VARCHAR(100) NOT NULL,
	PRIMARY KEY (id)
);

INSERT INTO cast_crew_type VALUES (1, "LEAD_CAST");
INSERT INTO cast_crew_type VALUES (2, "SUPPORTING_CAST");
INSERT INTO cast_crew_type VALUES (3, "DIRECTOR");
INSERT INTO cast_crew_type VALUES (4, "PRODUCER");
INSERT INTO cast_crew_type VALUES (5, "ASST_DIRECTOR");
INSERT INTO cast_crew_type VALUES (6, "CREW");

select * from cast_crew_type;

-- Language
CREATE TABLE language(
	id INTEGER NOT NULL AUTO_INCREMENT,
	name VARCHAR(100) NOT NULL,
	PRIMARY KEY (id)
);

INSERT INTO language VALUES (1, "HINDI");
INSERT INTO language VALUES (2, "ENGLISH");
INSERT INTO language VALUES (3, "BENGALI";
INSERT INTO language VALUES (4, "TAMIL");
INSERT INTO language VALUES (5, "TELUGU");
INSERT INTO language VALUES (6, "BHOJPURI");

select * from language;

-- Gender
CREATE TABLE gender(
	id INTEGER NOT NULL AUTO_INCREMENT,
	name VARCHAR(100) NOT NULL,
	PRIMARY KEY (id)
);

INSERT INTO gender VALUES (1, "MALE");
INSERT INTO gender VALUES (2, "FEMALE");

select * from gender;

-- Action Types
CREATE TABLE action_type(
	id INTEGER NOT NULL AUTO_INCREMENT,
	name VARCHAR(100) NOT NULL,
	PRIMARY KEY (id)
);

INSERT INTO action_type VALUES (1, "RATING");
INSERT INTO action_type VALUES (2, "REVIEW");

select * from action_type;


-- Data Tables

-- Show
-- Restricting on Delete is the default behaviour. Keeping it same to maintain data correctness.
CREATE TABLE shows(
	id INTEGER NOT NULL AUTO_INCREMENT,
	name VARCHAR(1024) NOT NULL,
	genre_id INTEGER NOT NULL,
	release_date TIMESTAMP,
	created_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP ,
	updated_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	language_id INTEGER,
	show_type_id INTEGER DEFAULT 1,
	PRIMARY KEY (id),
	FOREIGN KEY (genre_id) REFERENCES genre(id) ON DELETE RESTRICT,
	FOREIGN KEY (language_id) REFERENCES language(id) ON DELETE RESTRICT,
	FOREIGN KEY (show_type_id) REFERENCES show_type(id) ON DELETE RESTRICT
);


-- Table to keep details of all the people in the industry
CREATE TABLE cast_crew_members(
	id INTEGER NOT NULL AUTO_INCREMENT,
	name VARCHAR(1024) NOT NULL,
	gender_id INTEGER NOT NULL DEFAULT 1,
	date_of_birth TIMESTAMP,
	created_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP ,
	updated_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	
	PRIMARY KEY (id),
	FOREIGN KEY (gender_id) REFERENCES gender(id) ON DELETE RESTRICT
);

INSERT INTO cast_crew_members (name, gender_id) VALUES("Hritik Roshan", 1);
INSERT INTO cast_crew_members (name, gender_id) VALUES("Salman Khan", 1);
INSERT INTO cast_crew_members (name, gender_id) VALUES("Amir Khan", 1);
INSERT INTO cast_crew_members (name, gender_id) VALUES("Aishwarya Rai", 2);
INSERT INTO cast_crew_members (name, gender_id) VALUES("Madhuri Dixit", 2);

SELECT * FROM cast_crew_members;

-- To keep the details of people who acted in a movie, crew members etc.
CREATE TABLE show_cast(
	id INTEGER NOT NULL AUTO_INCREMENT,
	-- Cast/crew Member Id
	member_id INTEGER NOT NULL,
	show_id INTEGER NOT NULL,
	show_type_id INTEGER NOT NULL DEFAULT 1,
	cast_crew_type_id INTEGER NOT NULL,
	created_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP ,
	-- Updated on is never required here, but keeping just for future use in case required.
	updated_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	
	PRIMARY KEY (id),
	FOREIGN KEY (member_id) REFERENCES cast_crew_members(id) ON DELETE RESTRICT,
	FOREIGN KEY (show_id) REFERENCES shows(id) ON DELETE RESTRICT,
	FOREIGN KEY (show_type_id) REFERENCES show_type(id) ON DELETE RESTRICT,
	FOREIGN KEY (cast_crew_type_id) REFERENCES cast_crew_type(id) ON DELETE RESTRICT,

	INDEX USING BTREE(show_id),
	INDEX USING BTREE(member_id)

);

-- Customer Details
CREATE TABLE users(
	id INTEGER NOT NULL AUTO_INCREMENT,
	user_name VARCHAR(100) NOT NULL UNIQUE,
	email VARCHAR(100) NOT NULL UNIQUE,
	phone_number VARCHAR(20),
	password_hash VARCHAR(1024),
	password_salt VARCHAR(1024),
	created_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP ,
	updated_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	
	PRIMARY KEY (id),
	INDEX USING BTREE (email(100))
);


-- For storing individual Rating entries for all the shows
CREATE TABLE rating(
	id INTEGER NOT NULL AUTO_INCREMENT,
	user_id INTEGER NOT NULL,
	show_id INTEGER NOT NULL,
	show_type_id INTEGER NOT NULL,
	genre_id INTEGER,
	-- rating value can be 1-5. not putting constraint because if the limit changes, alter table would be too costly on this table.
	rating INTEGER,
	created_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP ,
	
	PRIMARY KEY (id),
	FOREIGN KEY (user_id) REFERENCES cast_crew_members(id) ON DELETE RESTRICT,
	FOREIGN KEY (show_id) REFERENCES shows(id) ON DELETE RESTRICT,
	FOREIGN KEY (show_type_id) REFERENCES show_type(id) ON DELETE RESTRICT,
	FOREIGN KEY (genre_id) REFERENCES genre(id) ON DELETE RESTRICT,

	-- Because queries would be on User and Genre only. Shows rating would come from a different table
	INDEX USING BTREE(user_id),
	INDEX USING BTREE(genre_id)

);

-- For Shows ratings
CREATE TABLE show_rating_summary(
	id INTEGER NOT NULL AUTO_INCREMENT,
	show_id INTEGER NOT NULL UNIQUE,
	rating_count INTEGER ,
	rating_sum INTEGER,
	genre_id INTEGER
	
	PRIMARY KEY (id),
	FOREIGN KEY (show_id) REFERENCES shows(id) ON DELETE RESTRICT,
	
	-- Because queries would be on Show Id to get ratings summary and genre_id to get top rated shows of a genre
	INDEX USING BTREE(show_id),
	INDEX USING BTREE(genre_id),
);


CREATE TABLE reviews(
	id INTEGER NOT NULL AUTO_INCREMENT,
	user_id INTEGER NOT NULL,
	show_id INTEGER NOT NULL,
	show_type_id INTEGER NOT NULL,
	comment VARCHAR(5000),
	created_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP ,
	
	PRIMARY KEY (id),
	FOREIGN KEY (user_id) REFERENCES cast_crew_members(id) ON DELETE RESTRICT,
	FOREIGN KEY (show_id) REFERENCES shows(id) ON DELETE RESTRICT,
	FOREIGN KEY (show_type_id) REFERENCES show_type(id) ON DELETE RESTRICT,

	-- Because queries would be on User and Show only.
	INDEX USING BTREE(user_id),
	INDEX USING BTREE(show_id)

);

-- For storing votes on Rating/Reviews
-- Action here means Rating or Review
CREATE TABLE votes(
	id INTEGER NOT NULL AUTO_INCREMENT,
	user_id INTEGER NOT NULL,
	-- Action user id is the user that made the rating or review
	action_user_id INTEGER NOT NULL,
	-- Rating ID or review ID
	action_id INTEGER NOT NULL,
	-- Rating or Review
	action_type_id INTEGER NOT NULL,
	is_upvote BOOLEAN NOT NULL DEFAULT true,
	created_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP ,
	
	PRIMARY KEY (id),
	FOREIGN KEY (user_id) REFERENCES cast_crew_members(id) ON DELETE RESTRICT,
	FOREIGN KEY (action_user_id) REFERENCES cast_crew_members(id) ON DELETE RESTRICT,
	FOREIGN KEY (action_type_id) REFERENCES action_type(id) ON DELETE RESTRICT,

	INDEX USING BTREE(action_id)
	
);


-- For storing review comments
CREATE TABLE review_comments(
	id INTEGER NOT NULL AUTO_INCREMENT,
	user_id INTEGER NOT NULL,
	-- Action user id is the user that made the rating or review
	review_id INTEGER NOT NULL,
	comment VARCHAR(2000) NOT NULL,
	created_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP ,
	
	PRIMARY KEY (id),
	FOREIGN KEY (user_id) REFERENCES cast_crew_members(id) ON DELETE RESTRICT,
	FOREIGN KEY (review_id) REFERENCES reviews(id) ON DELETE RESTRICT,

	INDEX USING BTREE(review_id)
	
);






