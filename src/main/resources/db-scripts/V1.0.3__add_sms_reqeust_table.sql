CREATE TABLE sms_request (
  id            int(11) NOT NULL AUTO_INCREMENT,
  biz_id        varchar(50),
  template_code varchar(50),
  phone_numbers varchar(4000),
  payload       varchar(2000),
  is_sent       tinyint(1),
  error_message varchar(2000),
  created_on    datetime,
  record_id     int(11),
  PRIMARY KEY (id),
  KEY SMS_REQUEST_record_id_index (record_id)
);
