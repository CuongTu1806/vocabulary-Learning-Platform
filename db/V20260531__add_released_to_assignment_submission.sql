-- Add released flag to assignment_submission so teacher can release details to student
ALTER TABLE assignment_submission
ADD COLUMN released TINYINT(1) DEFAULT 0;