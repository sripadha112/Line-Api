-- Migration: add family_members table and patient fields to appointments

CREATE TABLE IF NOT EXISTS family_members (
  id BIGSERIAL PRIMARY KEY,
  user_id BIGINT NOT NULL,
  name VARCHAR(150) NOT NULL,
  relationship VARCHAR(50),
  dob VARCHAR(10),
  age INT,
  gender VARCHAR(10),
  contact VARCHAR(30),
  created_at TIMESTAMP DEFAULT now(),
  updated_at TIMESTAMP DEFAULT now()
);

-- Add patient fields to appointments table
ALTER TABLE appointments
  ADD COLUMN IF NOT EXISTS patient_member_id BIGINT,
  ADD COLUMN IF NOT EXISTS patient_name VARCHAR(150);

-- Optional: index for faster lookup of family members per user
CREATE INDEX IF NOT EXISTS idx_family_members_user_id ON family_members(user_id);
