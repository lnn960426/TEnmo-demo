-- Lookup seeds
INSERT INTO transfer_status (transfer_status_desc) VALUES ('Pending'),('Approved'),('Rejected');
INSERT INTO transfer_type (transfer_type_desc) VALUES ('Request'),('Send');

-- Demo users (real BCrypt hashes)
INSERT INTO tenmo_user (username, password_hash, role) VALUES
  ('demo1', '$2b$10$1/hEwzyiv1GCgGT2el9B8OTH03fHipA7f.iNuUyMWhw1YH4eynDfi', 'USER'),
  ('demo2', '$2b$10$BLMHR3BYbp1w/9md6UKL3.cOOcEd555x58fqxgCsd6dEx7naCHAUC', 'USER');

-- Accounts for demo users
INSERT INTO account (user_id, balance)
SELECT user_id, 1000.00 FROM tenmo_user WHERE username IN ('demo1','demo2');