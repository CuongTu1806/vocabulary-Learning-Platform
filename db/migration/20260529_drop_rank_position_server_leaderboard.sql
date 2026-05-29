-- Migration: remove rank_position from server_leaderboard
-- This column is no longer used by the application; leaderboard rank is computed on read.

START TRANSACTION;

ALTER TABLE server_leaderboard
  DROP COLUMN rank_position;

COMMIT;
