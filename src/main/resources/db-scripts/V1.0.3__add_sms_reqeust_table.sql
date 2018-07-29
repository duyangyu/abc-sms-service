CREATE TABLE SMS_REQUEST (
  id            int(11) NOT NULL AUTO_INCREMENT,
  biz_id        varchar(20),
  template_code varchar(20),
  phone_numbers varchar(8000),
  payload       varchar(4000),
  is_sent       tinyint(1),
  error_message varchar(4000),
  created_on    datetime,
  record_id     int(11),
  PRIMARY KEY (id)
)
