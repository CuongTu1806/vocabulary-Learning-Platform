-- =====================================================================
-- Dữ liệu mẫu cho bảng xếp hạng (API /api/leaderboard/global)
-- Leaderboard được tính từ bảng contest_submission:
--    rating = SUM(score), contestCount = số contest khác nhau đã có nộp bài.
--
-- Chạy (PowerShell ví dụ):
--   mysql -u root -p vocabulary_learning_platform < "d:\...\seed_demo_leaderboard.sql"
--
-- Tài khoản demo đăng nhập (đều password: demo123), username seed_player_01 .. 08
-- =====================================================================

USE vocabulary_learning_platform;

SET @pwd = '$2b$10$PYbzlgCL9YFv87NmW.qhC.5wCRNJPRAAmLI027s3EMZ.SjrKjKYuK';

-- --- 8 user demo (username trùng sẽ lỗi — xóa trước hoặc đổi prefix) -----
INSERT INTO users (username, password, email, role, created_at, updated_at)
VALUES ('seed_player_01', @pwd, 'seed_player_01@demo.local', 'USER', NOW(), NOW());
SET @u1 := LAST_INSERT_ID();

INSERT INTO users (username, password, email, role, created_at, updated_at)
VALUES ('seed_player_02', @pwd, 'seed_player_02@demo.local', 'USER', NOW(), NOW());
SET @u2 := LAST_INSERT_ID();

INSERT INTO users (username, password, email, role, created_at, updated_at)
VALUES ('seed_player_03', @pwd, 'seed_player_03@demo.local', 'USER', NOW(), NOW());
SET @u3 := LAST_INSERT_ID();

INSERT INTO users (username, password, email, role, created_at, updated_at)
VALUES ('seed_player_04', @pwd, 'seed_player_04@demo.local', 'USER', NOW(), NOW());
SET @u4 := LAST_INSERT_ID();

INSERT INTO users (username, password, email, role, created_at, updated_at)
VALUES ('seed_player_05', @pwd, 'seed_player_05@demo.local', 'USER', NOW(), NOW());
SET @u5 := LAST_INSERT_ID();

INSERT INTO users (username, password, email, role, created_at, updated_at)
VALUES ('seed_player_06', @pwd, 'seed_player_06@demo.local', 'USER', NOW(), NOW());
SET @u6 := LAST_INSERT_ID();

INSERT INTO users (username, password, email, role, created_at, updated_at)
VALUES ('seed_player_07', @pwd, 'seed_player_07@demo.local', 'USER', NOW(), NOW());
SET @u7 := LAST_INSERT_ID();

INSERT INTO users (username, password, email, role, created_at, updated_at)
VALUES ('seed_player_08', @pwd, 'seed_player_08@demo.local', 'USER', NOW(), NOW());
SET @u8 := LAST_INSERT_ID();

-- --- 3 contest (created_by = u1) -----------------------------------------
INSERT INTO contest (title, description, start_time, end_time, visibility, created_by, created_at, updated_at)
VALUES (
  'Demo contest X',
  'Dữ liệu seed — không xóa trong môi trường thật nếu không cần',
  DATE_SUB(NOW(), INTERVAL 14 DAY),
  DATE_ADD(NOW(), INTERVAL 30 DAY),
  'PUBLIC',
  @u1,
  NOW(),
  NOW()
);
SET @cX := LAST_INSERT_ID();

INSERT INTO contest (title, description, start_time, end_time, visibility, created_by, created_at, updated_at)
VALUES (
  'Demo contest Y',
  'Dữ liệu seed',
  DATE_SUB(NOW(), INTERVAL 10 DAY),
  DATE_ADD(NOW(), INTERVAL 30 DAY),
  'PUBLIC',
  @u1,
  NOW(),
  NOW()
);
SET @cY := LAST_INSERT_ID();

INSERT INTO contest (title, description, start_time, end_time, visibility, created_by, created_at, updated_at)
VALUES (
  'Demo contest Z',
  'Dữ liệu seed',
  DATE_SUB(NOW(), INTERVAL 5 DAY),
  DATE_ADD(NOW(), INTERVAL 30 DAY),
  'PUBLIC',
  @u1,
  NOW(),
  NOW()
);
SET @cZ := LAST_INSERT_ID();

-- Contest X: 2 câu
INSERT INTO contest_problem (title, description, wrong_answer, answer, difficulty, max_score, order_index, contest_id, created_at, updated_at)
VALUES ('CX — Câu 1', '', 'wrong', 'seed_a', 'easy', 50, 1, @cX, NOW(), NOW());
SET @pX1 := LAST_INSERT_ID();

INSERT INTO contest_problem (title, description, wrong_answer, answer, difficulty, max_score, order_index, contest_id, created_at, updated_at)
VALUES ('CX — Câu 2', '', 'wrong', 'seed_b', 'easy', 40, 2, @cX, NOW(), NOW());
SET @pX2 := LAST_INSERT_ID();

-- Contest Y: 2 câu
INSERT INTO contest_problem (title, description, wrong_answer, answer, difficulty, max_score, order_index, contest_id, created_at, updated_at)
VALUES ('CY — Câu 1', '', 'wrong', 'seed_c', 'medium', 45, 1, @cY, NOW(), NOW());
SET @pY1 := LAST_INSERT_ID();

INSERT INTO contest_problem (title, description, wrong_answer, answer, difficulty, max_score, order_index, contest_id, created_at, updated_at)
VALUES ('CY — Câu 2', '', 'wrong', 'seed_d', 'medium', 35, 2, @cY, NOW(), NOW());
SET @pY2 := LAST_INSERT_ID();

-- Contest Z: 1 câu
INSERT INTO contest_problem (title, description, wrong_answer, answer, difficulty, max_score, order_index, contest_id, created_at, updated_at)
VALUES ('CZ — Câu 1', '', 'wrong', 'seed_e', 'hard', 60, 1, @cZ, NOW(), NOW());
SET @pZ1 := LAST_INSERT_ID();

-- --- submissions: điểm giả cố định (đúng sai gì cũng ghi score trực tiếp trong DB demo) -

-- Contest X
INSERT INTO contest_submission (user_answer, score, status, submitted_at, contest_id, user_id, problem_id, created_at, updated_at) VALUES
('seed_a', 50, 'SUBMITTED', NOW(), @cX, @u1, @pX1, NOW(), NOW()),
('seed_b', 40, 'SUBMITTED', NOW(), @cX, @u1, @pX2, NOW(), NOW()),
('seed_a', 50, 'SUBMITTED', NOW(), @cX, @u2, @pX1, NOW(), NOW()),
('wrong',    25, 'SUBMITTED', NOW(), @cX, @u2, @pX2, NOW(), NOW()),
('seed_a', 50, 'SUBMITTED', NOW(), @cX, @u3, @pX1, NOW(), NOW()),
('seed_b', 20, 'SUBMITTED', NOW(), @cX, @u3, @pX2, NOW(), NOW()),
('wrong',    0, 'SUBMITTED', NOW(), @cX, @u4, @pX1, NOW(), NOW()),
('seed_b', 40, 'SUBMITTED', NOW(), @cX, @u4, @pX2, NOW(), NOW()),
('seed_a', 50, 'SUBMITTED', NOW(), @cX, @u5, @pX1, NOW(), NOW()),
('wrong',    10,'SUBMITTED', NOW(), @cX, @u5, @pX2, NOW(), NOW()),
('wrong',    0, 'SUBMITTED', NOW(), @cX, @u6, @pX1, NOW(), NOW()),
('wrong',    0, 'SUBMITTED', NOW(), @cX, @u6, @pX2, NOW(), NOW()),
('seed_a', 50, 'SUBMITTED', NOW(), @cX, @u7, @pX1, NOW(), NOW()),
('wrong',    30,'SUBMITTED', NOW(), @cX, @u7, @pX2, NOW(), NOW()),
('seed_a', 50, 'SUBMITTED', NOW(), @cX, @u8, @pX1, NOW(), NOW()),
('seed_b', 40, 'SUBMITTED', NOW(), @cX, @u8, @pX2, NOW(), NOW());

-- Contest Y
INSERT INTO contest_submission (user_answer, score, status, submitted_at, contest_id, user_id, problem_id, created_at, updated_at) VALUES
('seed_c', 45, 'SUBMITTED', NOW(), @cY, @u1, @pY1, NOW(), NOW()),
('wrong',    17,'SUBMITTED', NOW(), @cY, @u1, @pY2, NOW(), NOW()),
('wrong',    10,'SUBMITTED', NOW(), @cY, @u2, @pY1, NOW(), NOW()),
('seed_d', 35, 'SUBMITTED', NOW(), @cY, @u2, @pY2, NOW(), NOW()),
('wrong',    0, 'SUBMITTED', NOW(), @cY, @u3, @pY1, NOW(), NOW()),
('wrong',    0, 'SUBMITTED', NOW(), @cY, @u3, @pY2, NOW(), NOW()),
('seed_c', 45, 'SUBMITTED', NOW(), @cY, @u4, @pY1, NOW(), NOW()),
('wrong',    20,'SUBMITTED', NOW(), @cY, @u4, @pY2, NOW(), NOW()),
('wrong',    22,'SUBMITTED', NOW(), @cY, @u5, @pY1, NOW(), NOW()),
('wrong',    8, 'SUBMITTED', NOW(), @cY, @u5, @pY2, NOW(), NOW()),
('wrong',    0, 'SUBMITTED', NOW(), @cY, @u6, @pY1, NOW(), NOW()),
('wrong',    35,'SUBMITTED', NOW(), @cY, @u6, @pY2, NOW(), NOW()),
('seed_c', 45, 'SUBMITTED', NOW(), @cY, @u7, @pY1, NOW(), NOW()),
('wrong',    0, 'SUBMITTED', NOW(), @cY, @u7, @pY2, NOW(), NOW()),
('wrong',    33,'SUBMITTED', NOW(), @cY, @u8, @pY1, NOW(), NOW()),
('seed_d', 35, 'SUBMITTED', NOW(), @cY, @u8, @pY2, NOW(), NOW());

-- Contest Z
INSERT INTO contest_submission (user_answer, score, status, submitted_at, contest_id, user_id, problem_id, created_at, updated_at) VALUES
('seed_e', 60, 'SUBMITTED', NOW(), @cZ, @u1, @pZ1, NOW(), NOW()),
('wrong',    42,'SUBMITTED', NOW(), @cZ, @u2, @pZ1, NOW(), NOW()),
('wrong',    48,'SUBMITTED', NOW(), @cZ, @u3, @pZ1, NOW(), NOW()),
('wrong',    35,'SUBMITTED', NOW(), @cZ, @u4, @pZ1, NOW(), NOW()),
('wrong',    28,'SUBMITTED', NOW(), @cZ, @u5, @pZ1, NOW(), NOW()),
('wrong',    15,'SUBMITTED', NOW(), @cZ, @u6, @pZ1, NOW(), NOW()),
('wrong',    50,'SUBMITTED', NOW(), @cZ, @u7, @pZ1, NOW(), NOW()),
('wrong',    58,'SUBMITTED', NOW(), @cZ, @u8, @pZ1, NOW(), NOW());

-- =====================================================================
-- Dự kiến thứ hạng (rating = tổng điểm, contest_count = 3 cho mọi user):
--   u8: ~163   u7: ~125   u1: ~182   ...
-- Gọi GET /api/leaderboard/global sau khi chạy script để xem chính xác.
--
-- -------- Gỡ dữ liệu seed (comment bỏ nếu cần) -----------------------
-- DELETE FROM contest_submission WHERE contest_id IN (SELECT id FROM contest WHERE title LIKE 'Demo contest %');
-- DELETE FROM contest_problem WHERE contest_id IN (SELECT id FROM contest WHERE title LIKE 'Demo contest %');
-- DELETE FROM contest WHERE title LIKE 'Demo contest %';
-- DELETE FROM users WHERE username LIKE 'seed_player_%';
-- =====================================================================
