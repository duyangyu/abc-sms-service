CREATE TABLE RECORD (
  id             int(11) NOT NULL AUTO_INCREMENT,
  app_id         varchar(20),
  entry_id       varchar(20),
  data_id        varchar(20),
  error_message  varchar(4000),
  created_on     datetime,
  update_count   int(11),
  updated_on     datetime,
  raw_message_id int(11),
  PRIMARY KEY (id)
)
