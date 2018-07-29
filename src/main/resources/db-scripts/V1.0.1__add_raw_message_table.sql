CREATE TABLE RAW_MESSAGE (
  id           int(11) NOT NULL AUTO_INCREMENT,
  message      varchar(4000),
  is_processed tinyint(1),
  created_on   datetime,
  processed_on datetime,
  PRIMARY KEY (id),
  KEY RAW_MESSAGE_created_on_index (created_on),
  KEY RAW_MESSAGE_processed_index (is_processed)
)
