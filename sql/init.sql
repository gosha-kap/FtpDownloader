drop table if exists settings;

create table settings (
 id serial  primary key ,
 job_id varchar(255) unique,
 repeat_later boolean default false,
 next_time_run bigint not null,
 num_of_repeats int  not null
);