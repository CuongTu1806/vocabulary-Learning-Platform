-- Migration: add max_rating to server_leaderboard
-- Stores the highest rating a user has reached for profile display.

START TRANSACTION;

ALTER TABLE server_leaderboard
  ADD COLUMN max_rating INT NULL DEFAULT 0;

UPDATE server_leaderboard
SET max_rating = GREATEST(COALESCE(max_rating, 0), COALESCE(rating, 0));

ALTER TABLE server_leaderboard
  MODIFY COLUMN max_rating INT NOT NULL DEFAULT 0;

COMMIT;