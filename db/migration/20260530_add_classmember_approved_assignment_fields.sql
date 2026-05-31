-- Migration: add approved flag to class_member and fields to assignment

START TRANSACTION;

ALTER TABLE class_member
  ADD COLUMN approved TINYINT(1) DEFAULT 0;

ALTER TABLE assignment
  ADD COLUMN `type` VARCHAR(32) DEFAULT 'file',
  ADD COLUMN questions TEXT NULL;

COMMIT;
