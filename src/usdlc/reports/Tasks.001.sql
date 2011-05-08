drop table if exists TaskStates;
CREATE TABLE TaskStates(
	id int primary key,
	cardinality int,
	state varchar(255),
	description varchar(255)
) AS SELECT * FROM CSVREAD('classpath:usdlc/reports/TaskStates.db.csv');

drop table if exists Priorities
CREATE TABLE Priorities(
	id int primary key,
	cardinality int,
	priority varchar(255),
	description varchar(255)
) AS SELECT * FROM CSVREAD('classpath:usdlc/reports/Priorities.db.csv');

drop table if exists Users
CREATE TABLE Users(
	id varchar(255) primary key,
	userName varchar(255),
	designation varchar(255),
	password varchar(255),
	location varchar(255),
	email varchar(255),
	groups varchar(255)
) AS SELECT * FROM CSVREAD('classpath:usdlc/reports/Users.db.csv');

drop table if exists Groups
CREATE TABLE Groups(
	cardinality int,
	id varchar(255) primary key,
	roles varchar(255),
	name varchar(255),
	description varchar(255)
) AS SELECT * FROM CSVREAD('classpath:usdlc/reports/Groups.db.csv');

drop table if exists Tasks
CREATE TABLE Tasks(
	id int primary key,
	title varchar(255),
	stateId int, foreign key(stateId) references TaskStates(id),
	cardinality varchar(255),
	priorityId int, foreign key(priorityId) references Priorities(id),
	dependencyId int, foreign key(dependencyId) references Tasks(id),
	ownerId varchar(255), foreign key(ownerId) references Users(id),
	assigneeId varchar(255), foreign key(assigneeId) references Users(id),
	due Date
);
