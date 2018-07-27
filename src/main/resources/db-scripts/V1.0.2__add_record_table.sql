create table RECORD
(
	id int auto_increment
		primary key,
	app_id varchar(20) null,
	entry_id varchar(20) null,
	data_id varchar(20) null,
	created_on datetime default CURRENT_TIMESTAMP not null,
	raw_message_id int null,
	constraint RECORD_RAW_MESSAGE_id_fk
		foreign key (raw_message_id) references RAW_MESSAGE (id)
)
;

