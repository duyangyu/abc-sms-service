TRUNCATE TABLE RAW_MESSAGE;

INSERT INTO RAW_MESSAGE (id, message, is_processed, created_on, processed_on)
VALUES (1, 'test', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO RAW_MESSAGE (id, message, is_processed, created_on, processed_on)
VALUES (2, 'test2', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
