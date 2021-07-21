-- the core details table
CREATE TABLE ACCESSVERIFICATIONDETAILS
(
    -- the subject is generated at 32 bytes but this gives room for growth
    subject   VARCHAR(255),

    -- the public key can be long and so here is stored as a blob (some databases may prefer TEXT or CLOB)
    publicKey BLOB
);