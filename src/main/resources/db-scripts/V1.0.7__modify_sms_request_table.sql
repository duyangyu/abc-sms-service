ALTER TABLE sms_request ADD COLUMN content VARCHAR(2000) NULL;
ALTER TABLE sms_request ADD COLUMN update_count INT(11);
ALTER TABLE sms_request ADD COLUMN updated_on DATETIME;
ALTER TABLE sms_request ADD COLUMN data_id VARCHAR(30) NULL;