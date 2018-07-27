create table RAW_MESSAGE
(
  id           int auto_increment
    primary key,
  message      nvarchar(4000)                     not null,
  is_processed tinyint(1) default '0'             null,
  created_on   datetime default CURRENT_TIMESTAMP null,
  processed_on datetime                           null
);

create index RAW_MESSAGE_created_on_index
  on RAW_MESSAGE (created_on);

create index RAW_MESSAGE_processed_index
  on RAW_MESSAGE (is_processed);

