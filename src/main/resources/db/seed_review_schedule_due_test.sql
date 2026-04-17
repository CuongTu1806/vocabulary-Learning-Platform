-- Seed data for review_schedule due testing
-- Rules:
-- - user_vocabulary_id 14..25 => next_review_date is TODAY
-- - user_vocabulary_id 26..30 => next_review_date is TOMORROW
-- - all rows are in REVIEW state
-- - interval_days is positive (review phase)

START TRANSACTION;

-- Remove old test rows for these vocab ids to avoid duplicates in due query
DELETE FROM review_schedule
WHERE user_vocabulary_id BETWEEN 14 AND 30;

INSERT INTO review_schedule (
    repetation_level,
    state,
    learning_step,
    interval_days,
    ease_factor,
    delay_factor,
    next_review_date,
    last_review_date,
    user_vocabulary_id,
    created_at,
    updated_at
)
SELECT
    CASE
        WHEN uv.id BETWEEN 14 AND 18 THEN 3
        WHEN uv.id BETWEEN 19 AND 23 THEN 5
        ELSE 7
    END AS repetation_level,
    'review' AS state,
    0 AS learning_step,
    CASE
        WHEN uv.id IN (14, 20, 26) THEN 1
        WHEN uv.id IN (15, 21, 27) THEN 2
        WHEN uv.id IN (16, 22, 28) THEN 3
        WHEN uv.id IN (17, 23, 29) THEN 4
        ELSE 5
    END AS interval_days,
    CASE
        WHEN uv.id BETWEEN 14 AND 18 THEN 2.10
        WHEN uv.id BETWEEN 19 AND 23 THEN 2.35
        ELSE 2.55
    END AS ease_factor,
    0.05 AS delay_factor,
    CASE
        WHEN uv.id BETWEEN 14 AND 25
            THEN TIMESTAMP(CURDATE(), '09:00:00')
        ELSE TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 1 DAY), '09:00:00')
    END AS next_review_date,
    CASE
        WHEN uv.id BETWEEN 14 AND 25
            THEN DATE_SUB(
                TIMESTAMP(CURDATE(), '09:00:00'),
                INTERVAL (
                    CASE
                        WHEN uv.id IN (14, 20) THEN 1
                        WHEN uv.id IN (15, 21) THEN 2
                        WHEN uv.id IN (16, 22) THEN 3
                        WHEN uv.id IN (17, 23) THEN 4
                        ELSE 5
                    END
                ) DAY
            )
        ELSE DATE_SUB(
                TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 1 DAY), '09:00:00'),
                INTERVAL (
                    CASE
                        WHEN uv.id IN (26) THEN 1
                        WHEN uv.id IN (27) THEN 2
                        WHEN uv.id IN (28) THEN 3
                        WHEN uv.id IN (29) THEN 4
                        ELSE 5
                    END
                ) DAY
            )
    END AS last_review_date,
    uv.id AS user_vocabulary_id,
    NOW() AS created_at,
    NOW() AS updated_at
FROM user_vocabulary uv
WHERE uv.id BETWEEN 14 AND 30;

COMMIT;

-- Validate seeded rows
SELECT
    user_vocabulary_id,
    state,
    learning_step,
    interval_days,
    ease_factor,
    delay_factor,
    next_review_date,
    last_review_date
FROM review_schedule
WHERE user_vocabulary_id BETWEEN 14 AND 30
ORDER BY user_vocabulary_id;

-- Quick due check (should return only 14..25 if current time is after 09:00)
SELECT
    user_vocabulary_id,
    next_review_date,
    state
FROM review_schedule
WHERE state = 'review'
  AND next_review_date <= NOW()
  AND user_vocabulary_id BETWEEN 14 AND 30
ORDER BY next_review_date;
