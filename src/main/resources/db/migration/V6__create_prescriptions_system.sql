-- Create single prescriptions table with JSONB for medicines
CREATE TABLE IF NOT EXISTS prescriptions (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL REFERENCES user_details(id) ON DELETE CASCADE,
    doctor_id INTEGER NOT NULL REFERENCES doctor_details(id) ON DELETE CASCADE,
    appointment_id INTEGER REFERENCES appointments(id) ON DELETE SET NULL,
    medical_notes TEXT,
    medicines JSONB DEFAULT '[]'::jsonb,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for better query performance
CREATE INDEX IF NOT EXISTS idx_prescriptions_user_id ON prescriptions(user_id);
CREATE INDEX IF NOT EXISTS idx_prescriptions_doctor_id ON prescriptions(doctor_id);
CREATE INDEX IF NOT EXISTS idx_prescriptions_appointment_id ON prescriptions(appointment_id);
CREATE INDEX IF NOT EXISTS idx_prescriptions_medicines ON prescriptions USING GIN (medicines);
CREATE INDEX IF NOT EXISTS idx_all_medicines_name ON all_medicines(medicine_name);

-- Remove medical_notes and prescription columns from user_details (if they exist)
ALTER TABLE user_details DROP COLUMN IF EXISTS medical_notes;
ALTER TABLE user_details DROP COLUMN IF EXISTS prescription;
