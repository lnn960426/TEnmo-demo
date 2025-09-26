-- Lookup seeds
INSERT INTO transfer_status (transfer_status_desc) VALUES ('Pending'),('Approved'),('Rejected');
INSERT INTO transfer_type (transfer_type_desc) VALUES ('Request'),('Send');

-- Demo users (replace with real BCrypt hashes)
INSERT INTO tenmo_user (username, password_hash, role) VALUES
  ('demo1', '{BCryptHash_demo1}', 'USER'),
  ('demo2', '{BCryptHash_demo2}', 'USER');

-- Accounts for demo users
INSERT INTO account (user_id, balance)
SELECT user_id, 1000.00 FROM tenmo_user WHERE username IN ('demo1','demo2');