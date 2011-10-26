drop table if exists versions;
CREATE TABLE versions(
	tableGroup varchar(255) primary key,
	version int
);
drop table if exists sessions;
CREATE TABLE sessions(
	session varchar(255),
	key varchar(255),
	value varchar(255)
);
CREATE INDEX pk ON sessions(session, key)
insert into versions values('classpath:usdlc/db/Core',1);
