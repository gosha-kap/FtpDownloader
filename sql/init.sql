drop table if exists jobdetailentity;
drop table if exists credention;
drop table if exists downloadsettings;
drop table if exists ftpsettings;
drop table if exists hiwatchsettings;
drop table if exists telegramcredentions;
drop table if exists downloadjobentity;

create table downloadjobentity (
 id bigint  primary key,
 job_id varchar(255) unique
);

create table jobdetailentity (
 id bigint  primary key,
 downloadjob_id varchar,
 jobtype varchar not null,
 jobstatusvarchar varchar not null,
 alias  varchar not null,
 note  varchar,
 foreign key (downloadjob_id) references downloadjobentity (job_id)
);


create table credention(
  id bigint  primary key,
  downloadjob_id varchar,
  server varchar(15) not null ,
  port int not null ,
  login varchar(10) not null ,
  password varchar(255) not null ,
  foreign key (downloadjob_id) references downloadjobentity (job_id)
);

create table downloadsettings (
  id bigint  primary key,
  downloadjob_id varchar,
  saveFolder varchar,
  numOfTries int default 1,
  repeatLater boolean default  false,
  nextTimeRun bigint ,
  numOfRepeats int,
  foreign key (downloadjob_id) references downloadjobentity (job_id)
);

create table telegramcredentions(
  id bigint  primary key,
  downloadjob_id varchar,
  telegramKey varchar not null ,
  chatId varchar not null ,
  foreign key (downloadjob_id) references downloadjobentity (job_id)
);

create table ftpsettings(
 id bigint  primary key,
 downloadjob_id varchar,
 dataTimeOut int default 0,
 fileType int default 2,
 filePostfix varchar(10) default '',
 foreign key (downloadjob_id) references downloadjobentity (job_id)
);

create table hiwatchsettings(
  id bigint  primary key,
  downloadjob_id varchar,
  channel int default 101,
  searchMaxResult int default 50,
  searchResultPosition int default  0,
  fromtime timestamp,
  totime timestamp,
  timeshift boolean default false,
  foreign key (downloadjob_id) references downloadjobentity (job_id)
);

