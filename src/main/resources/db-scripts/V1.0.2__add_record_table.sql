CREATE TABLE record (
  id             int(11) NOT NULL AUTO_INCREMENT,
  app_id         varchar(50),
  entry_id       varchar(50),
  data_id        varchar(50),
  error_message  varchar(4000),
  created_on     datetime,
  update_count   int(11),
  updated_on     datetime,
  raw_message_id int(11),
  PRIMARY KEY (id),
  KEY RECORD_update_count_index (update_count),
  KEY RECORD_updated_on_index (updated_on)
);
