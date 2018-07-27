create table SMS_MESSAGE
(
	id int auto_increment
		primary key,
	phone_number varchar(15) null,
	content nvarchar(4000) null,
	is_sent tinyint(1) default '0' null,
	error_message nvarchar(4000) null,
	sent_on datetime null,
	updated_on datetime null,
	sms_request_id int null,
	constraint SMS_MESSAGE_SMS_REQUEST_id_fk
		foreign key (sms_request_id) references SMS_REQUEST (id)
)
;

