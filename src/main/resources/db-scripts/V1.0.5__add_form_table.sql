CREATE TABLE form (
  id              int(11)     NOT NULL,
  app_id          varchar(50) NOT NULL,
  entry_id        varchar(50) NOT NULL,
  metadata_widget varchar(50) NOT NULL,
  message_widget  varchar(50) NOT NULL,
  name            varchar(50) NULL,
  PRIMARY KEY (id),
  KEY form_app_id_index (app_id),
  KEY form_entry_id_index (entry_id),
  CONSTRAINT unq_app_id_entry_id UNIQUE (app_id, entry_id)
)