

CREATE TABLE role_privileges (
     role_id BIGINT NOT NULL,
     privilege_id BIGINT NOT NULL,

     PRIMARY KEY (role_id, privilege_id),

     CONSTRAINT fk_role
         FOREIGN KEY (role_id)
             REFERENCES roles (id)
             ON DELETE CASCADE,

     CONSTRAINT fk_privilege
         FOREIGN KEY (privilege_id)
             REFERENCES privileges (id)
             ON DELETE CASCADE
);

INSERT INTO roles (name) VALUES
('USER'),
('ADMIN'),
('SUPER_ADMIN');

INSERT INTO privileges (name) VALUES
('READ_PROFILE'),
('UPDATE_PROFILE'),
('MANAGE_USERS'),
('VIEW_ALL_USERS'),
('MANAGE_CASES'),
('SYSTEM_CONFIG'),
('MANAGE_ROLES'),
('AUDIT_LOGS_ACCESS');

-- Map USER

INSERT INTO role_privileges (role_id, privilege_id)
SELECT r.id, p.id
FROM roles r, privileges p
WHERE r.name = 'USER'
  AND p.name IN ('READ_PROFILE', 'UPDATE_PROFILE');


-- Map ADMIN
INSERT INTO role_privileges (role_id, privilege_id)
SELECT r.id, p.id
FROM roles r, privileges p
WHERE r.name = 'ADMIN'
AND p.name IN (
 'READ_PROFILE',
 'UPDATE_PROFILE',
 'MANAGE_USERS',
 'VIEW_ALL_USERS',
 'MANAGE_CASES'
);

-- Map SUPER_ADMIN
INSERT INTO role_privileges (role_id, privilege_id)
SELECT r.id, p.id
FROM roles r, privileges p
WHERE r.name = 'SUPER_ADMIN';


