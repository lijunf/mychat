/**
 *	mysql
 */
CREATE DATABASE mychat;
CREATE TABLE mychat.ct_message (
	mesid INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
	sender VARCHAR(128) NOT NULL,
	getter VARCHAR(128) NOT NULL,
	content TEXT NOT NULL,
	sendtime DATETIME NOT NULL,
	mtype INT NOT NULL,
	isread BOOL NOT NULL 
)  ENGINE=InnoDB DEFAULT CHARSET=utf8;
CREATE TABLE mychat.ct_user (
	username VARCHAR(128) NOT NULL PRIMARY KEY,
	password VARCHAR(128) NOT NULL,
	nickname VARCHAR(128) NOT NULL,
	email VARCHAR(128) NOT NULL,
	sex BOOL NOT NULL,
	header VARCHAR(128) NOT NULL,
	realname VARCHAR(128) NULL,
	engname VARCHAR(128) NULL,
	birthday INT NOT NULL,
	address VARCHAR(128) NULL,
	phone VARCHAR(128) NULL,
	profession VARCHAR(128) NULL,
	education VARCHAR(128) NULL,
	school VARCHAR(128) NULL,
	homepage VARCHAR(128) NULL,
	autograph VARCHAR(255) NULL,
	elucidate VARCHAR(800) NULL,
	ipaddr VARCHAR(128) NULL,
	firsttime DATETIME NOT NULL,
	lasttime DATETIME NOT NULL,
	loginnum INT NOT NULL,
	enabled BOOL NOT NULL,
	kefuFlag INT NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
CREATE INDEX i_username ON mychat.ct_user(username);
CREATE INDEX i_nickname ON mychat.ct_user(nickname);
CREATE TABLE mychat.ct_group (
	gid INT NOT NULL PRIMARY KEY,
	gname VARCHAR(128) NOT NULL,
	creator VARCHAR(128) NOT NULL,
	FOREIGN KEY (creator) REFERENCES ct_user(username) ON DELETE CASCADE 
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
CREATE TABLE mychat.ct_friend (
	gid INT NOT NULL,
	username VARCHAR(128) NOT NULL,
	notename VARCHAR(128) NULL,
	PRIMARY KEY (gid, username),
	FOREIGN KEY (gid) REFERENCES ct_group(gid) ON DELETE CASCADE,
	FOREIGN KEY (username) REFERENCES ct_user(username) ON DELETE NO ACTION 
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/**
 *	mssql
 */
CREATE DATABASE mychat;
CREATE TABLE mychat..ct_message (
	mesid INT NOT NULL PRIMARY KEY identity(1,1),
	sender VARCHAR(128) NOT NULL,
	getter VARCHAR(128) NOT NULL,
	content TEXT NOT NULL,
	sendtime DATETIME NOT NULL,
	mtype INT NOT NULL,
	isread bit NOT NULL 
);
CREATE TABLE mychat..ct_user (
	username VARCHAR(128) NOT NULL PRIMARY KEY,
	password VARCHAR(128) NOT NULL,
	nickname VARCHAR(128) NOT NULL,
	email VARCHAR(128) NOT NULL,
	sex bit NOT NULL,
	header VARCHAR(128) NOT NULL,
	realname VARCHAR(128) NULL,
	engname VARCHAR(128) NULL,
	birthday INT NOT NULL,
	address VARCHAR(128) NULL,
	phone VARCHAR(128) NULL,
	profession VARCHAR(128) NULL,
	education VARCHAR(128) NULL,
	school VARCHAR(128) NULL,
	homepage VARCHAR(128) NULL,
	autograph VARCHAR(255) NULL,
	elucidate VARCHAR(800) NULL,
	ipaddr VARCHAR(128) NULL,
	firsttime DATETIME NOT NULL,
	lasttime DATETIME NOT NULL,
	loginnum INT NOT NULL,
	enabled bit NOT NULL,
	kefuFlag INT NOT NULL
);
CREATE INDEX i_username ON mychat..ct_user(username);
CREATE INDEX i_nickname ON mychat..ct_user(nickname);
CREATE TABLE mychat..ct_group (
	gid INT NOT NULL PRIMARY KEY,
	gname VARCHAR(128) NOT NULL,
	creator VARCHAR(128) NOT NULL,
	FOREIGN KEY (creator) REFERENCES ct_user(username) ON DELETE CASCADE 
);
CREATE TABLE mychat..ct_friend (
	gid INT NOT NULL,
	username VARCHAR(128) NOT NULL,
	notename VARCHAR(128) NULL,
	PRIMARY KEY (gid, username),
	FOREIGN KEY (gid) REFERENCES ct_group(gid) ON DELETE CASCADE,
	FOREIGN KEY (username) REFERENCES ct_user(username) ON DELETE NO ACTION
)