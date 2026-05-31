-- Create table to store per-question submission details for assignments
CREATE TABLE IF NOT EXISTS assignment_submission_detail (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  submission_id BIGINT NOT NULL,
  question_index INT,
  question_text TEXT,
  student_answer TEXT,
  expected_answer TEXT,
  correct_flag TINYINT(1),
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_asd_submission FOREIGN KEY (submission_id) REFERENCES assignment_submission(id) ON DELETE CASCADE
);
