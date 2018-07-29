CREATE TABLE SMS_MESSAGE (
  id             int(11) NOT NULL AUTO_INCREMENT,
  phone_number   varchar(20),
  content        varchar(4000),
  biz_id         varchar(20),
  is_sent        tinyint(1),
  error_message  varchar(4000),
  sent_on        datetime,
  updated_on     datetime,
  sms_request_id int(11),
  PRIMARY KEY (id)
)
