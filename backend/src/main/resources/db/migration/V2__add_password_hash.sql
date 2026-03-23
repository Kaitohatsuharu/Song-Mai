-- Add password hash column for dashboard user authentication
ALTER TABLE accounts ADD COLUMN password_hash VARCHAR(255);
