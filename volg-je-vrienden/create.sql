
CREATE TABLE USERS(
	PID TEXT NOT NULL, 
	PHOTO BLOB NOT NULL, 
	PRIMARY KEY(PID)
);

CREATE TABLE ISFRIENDSWITH(
	PID1 TEXT NOT NULL,
	PID2 TEXT NOT NULL,
	PRIMARY KEY(PID1, PID2),
	FOREIGN KEY(PID1) REFERENCES USERS,
	FOREIGN KEY(PID2) REFERENCES USERS
);
