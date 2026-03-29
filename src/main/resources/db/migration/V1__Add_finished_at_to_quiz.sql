-- Add finished_at column to quiz table
ALTER TABLE quiz ADD COLUMN finished_at DATETIME NULL;

-- Optional: Add index for better filtering/sorting performance
-- CREATE INDEX idx_quiz_duration ON quiz(duration);
-- CREATE INDEX idx_quiz_finished_at ON quiz(finished_at);
