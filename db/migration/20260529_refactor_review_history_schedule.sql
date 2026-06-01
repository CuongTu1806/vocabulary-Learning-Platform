-- Migration: Refactor review_history and review_schedule
-- 1) Add audit columns to review_schedule
-- 2) Backfill previous interval/ease from latest review_history per schedule
-- 3) Drop denormalized columns from review_history and drop user_vocabulary_id
-- 4) Drop repetation_level from review_schedule
-- IMPORTANT: Backup your database before running. Run inside a transaction if your MySQL engine supports it.

-- BACKUP example (run before executing):
-- mysqldump -u <user> -p <database> > backup_before_refactor.sql

START TRANSACTION;

-- 1) Add new columns to review_schedule
ALTER TABLE review_schedule
  ADD COLUMN previous_interval_days INT NULL,
  ADD COLUMN previous_ease_factor DOUBLE NULL;

-- 2) Backfill using the most recent review_history row per review_schedule
UPDATE review_schedule rs
JOIN (
  SELECT r1.review_schedule_id, r1.old_interval_days, r1.old_ease_factor
  FROM review_history r1
  INNER JOIN (
    SELECT review_schedule_id, MAX(created_at) AS max_created
    FROM review_history
    WHERE review_schedule_id IS NOT NULL
    GROUP BY review_schedule_id
  ) r2 ON r1.review_schedule_id = r2.review_schedule_id AND r1.created_at = r2.max_created
) rh ON rs.id = rh.review_schedule_id
SET rs.previous_interval_days = rh.old_interval_days,
    rs.previous_ease_factor = rh.old_ease_factor;

-- 3) Remove denormalized/old columns from review_history
-- NOTE: If foreign keys exist on these columns (e.g. user_vocabulary_id), you may need to DROP FOREIGN KEY first.
SET @fk_name := (
  SELECT constraint_name
  FROM information_schema.KEY_COLUMN_USAGE
  WHERE table_schema = DATABASE()
    AND table_name = 'review_history'
    AND column_name = 'user_vocabulary_id'
    AND referenced_table_name IS NOT NULL
  LIMIT 1
);

SET @sql := IF(
  @fk_name IS NULL,
  'SELECT 1',
  CONCAT('ALTER TABLE review_history DROP FOREIGN KEY `', @fk_name, '`')
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

ALTER TABLE review_history
  DROP COLUMN old_ease_factor,
  DROP COLUMN new_ease_factor,
  DROP COLUMN old_interval_days,
  DROP COLUMN new_interval_days,
  DROP COLUMN pos,
  DROP COLUMN vocabulary_id,
  DROP COLUMN word_text,
  DROP COLUMN user_vocabulary_id;

-- 4) Drop repetation_level from review_schedule (if you want to remove it)
ALTER TABLE review_schedule
  DROP COLUMN repetation_level;

COMMIT;

-- After running migration, rebuild the application:
-- mvn -f learningVocabularyPlatform/pom.xml -DskipTests package

-- If any ALTER TABLE DROP COLUMN fails due to foreign keys, check the constraint name in information_schema.KEY_COLUMN_USAGE.
