INSERT INTO tenants (id, slug, name, default_currency, theme_primary, theme_logo_url) VALUES
    ('0b9d6e21-7c3a-4f58-8a14-5e2c9d7b1a36', 'northwind', 'Northwind Footwear', 'EUR', '#1f5f4a', NULL),
    ('7a1e4c90-2d6b-4b83-a5f0-9c3e8d1b2a47', 'acme',      'Acme',               'USD', NULL,      NULL);

INSERT INTO users (id, tenant_id, email, password_hash, name, role) VALUES
    ('3f2c8a54-1b6d-4e0a-9c57-2d8e1f4a6b70', '0b9d6e21-7c3a-4f58-8a14-5e2c9d7b1a36',
     'ana@counthouse.example',  '$2a$10$XtSqa8goQ87Tu7qmQvLiieFEoxNfFCzbQ/.JwyOr/MxN8Mge4XBsC', 'Ana Torres',  'ADMIN'),
    ('ad8b2b4e-0fbd-41de-ad07-8ab26b1dea9c', '0b9d6e21-7c3a-4f58-8a14-5e2c9d7b1a36',
     'noor@counthouse.example', '$2a$10$c2Gwr4bjoNfSDU5SrPptzumGTcn7S27dOnIGHA6CRuQTJ4aOvC.tG', 'Noor Haddad', 'OPERATOR'),
    ('948f255b-bd50-44a9-818e-56b7cc377727', '7a1e4c90-2d6b-4b83-a5f0-9c3e8d1b2a47',
     'amir@counthouse.example', '$2a$10$RrJ/W8Ow0W0DqtbRMCpKv.REtE3TNb8dVRoqcfRK7iSM2Fiamhjw2', 'Amir Cole',   'ADMIN'),
    ('9a41d7e2-6c08-4b35-8e1f-3d5b7a2c9e60', '7a1e4c90-2d6b-4b83-a5f0-9c3e8d1b2a47',
     'omar@counthouse.example', '$2a$10$Xx4y1boReGQpyIabiPtcJ.n3nQfpsDXXlabPWDC5oRtxJF9dg3Jaa', 'Omar Reyes',  'OPERATOR');
     