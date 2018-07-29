CREATE TABLE RECORD (
  id             int(11) NOT NULL AUTO_INCREMENT,
  app_id         varchar(20),
  entry_id       varchar(20),
  data_id        varchar(20),
  error_message  varchar(4000),
  created_on     datetime,
  updated_on_1   datetime,
  updated_on_2   datetime,
  raw_message_id int(11),
  PRIMARY KEY (id)
)
