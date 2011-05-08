drop table if exists versions;
CREATE TABLE versions(
	tableGroup varchar(255) primary key,
	version int
);
insert into versions values("Core",1);
