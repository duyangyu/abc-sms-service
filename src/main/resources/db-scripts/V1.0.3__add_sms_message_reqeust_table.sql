create table SMS_REQUEST
(
	id int auto_increment
		primary key,
	biz_id varchar(20) null,
	template_id varchar(20) null,
	error_message nvarchar(4000) null,
	created_on datetime default CURRENT_TIMESTAMP null,
	record_id int null,
	constraint SMS_REQUEST_RECORD_id_fk
		foreign key (record_id) references RECORD (id)
)
;

create index SMS_REQUEST_biz_id_index
	on SMS_REQUEST (biz_id)
;

