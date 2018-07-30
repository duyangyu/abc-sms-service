create table SMS_MESSAGE
(
  id             int auto_increment,
  phone_number   varchar(30),
  content        varchar(4000),
  biz_id         varchar(50),
  is_sent        tinyint(1),
  error_message  varchar(4000),
  sent_on        datetime,
  updated_on     datetime,
  sms_request_id int,
  record_id      int,
  PRIMARY KEY (id),
  KEY SMS_MESSAGE_biz_id_index (biz_id),
  KEY SMS_MESSAGE_record_id_index (record_id),
  KEY SMS_MESSAGE_phone_number_index (phone_number)
);

