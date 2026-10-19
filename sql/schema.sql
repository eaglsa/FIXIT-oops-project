-- ============================================================
-- FixIt – Digital Complaint & Maintenance Tracker
-- Run this script once to create the database and seed data.
-- Usage: mysql -u root -p < sql/schema.sql
-- ============================================================

CREATE DATABASE IF NOT EXISTS fixit;
USE fixit;

-- ---- users -----------------------------------------------
CREATE TABLE IF NOT EXISTS users (
    id       INT AUTO_INCREMENT PRIMARY KEY,
    name     VARCHAR(100) NOT NULL UNIQUE,
    role     ENUM('STUDENT','STAFF') NOT NULL,
    password VARCHAR(100) NOT NULL
);

-- ---- complaints ------------------------------------------
CREATE TABLE IF NOT EXISTS complaints (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    type          ENUM('ELECTRICAL','PLUMBING','WIFI','GENERIC') NOT NULL,
    description   TEXT NOT NULL,
    location      VARCHAR(100) NOT NULL,
    status        ENUM('REPORTED','IN_PROGRESS','RESOLVED') NOT NULL DEFAULT 'REPORTED',
    raised_by     INT NOT NULL,
    date_reported DATE NOT NULL,
    FOREIGN KEY (raised_by) REFERENCES users(id)
);

-- ---- status_log ------------------------------------------
-- Tracks every status change for audit / history purposes.
CREATE TABLE IF NOT EXISTS status_log (
    id           INT AUTO_INCREMENT PRIMARY KEY,
    complaint_id INT NOT NULL,
    old_status   ENUM('REPORTED','IN_PROGRESS','RESOLVED') NOT NULL,
    new_status   ENUM('REPORTED','IN_PROGRESS','RESOLVED') NOT NULL,
    changed_by   INT NOT NULL,
    changed_on   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (complaint_id) REFERENCES complaints(id),
    FOREIGN KEY (changed_by)   REFERENCES users(id)
);

-- ---- seed data -------------------------------------------
-- Plain-text passwords are acceptable for this demo project.
INSERT IGNORE INTO users (name, role, password) VALUES
  ('warden',       'STAFF',   'staff123'),
  ('electrician',  'STAFF',   'staff123'),
  ('student1',     'STUDENT', 'pass123'),
  ('student2',     'STUDENT', 'pass123');
