CREATE DATABASE IF NOT EXISTS example;
ALTER DATABASE example CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

DROP TABLE IF EXISTS authorities;
DROP TABLE IF EXISTS verificationTokens;
DROP TABLE IF EXISTS accountTransfers;
DROP TABLE IF EXISTS accounts;
DROP TABLE IF EXISTS sessionTokens;

CREATE TABLE sessionTokens (
    id INT NOT NULL AUTO_INCREMENT,
    token VARCHAR(255) NOT NULL,
    createdOn DATETIME NOT NULL,
    expiresOn DATETIME NOT NULL,
    previousToken VARCHAR(255),

    UNIQUE (id),
    PRIMARY KEY (id),
    UNIQUE INDEX(token),
    INDEX(previousToken)
);

CREATE TABLE accounts (
    id INT NOT NULL AUTO_INCREMENT,
    publicUsername VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    phoneNumber VARCHAR(255),
    birthDate DATE,
    description VARCHAR(255) DEFAULT '',
    hashedPassword VARCHAR(64) NOT NULL COMMENT 'includes salt',
    enabled BOOLEAN NOT NULL DEFAULT FALSE,
    creation DATETIME NOT NULL,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    lastPasswordResetDate DATETIME NOT NULL,

    UNIQUE (id),
    UNIQUE (publicUsername),
    PRIMARY KEY (id),
    UNIQUE INDEX(email),
    INDEX(phoneNumber)
);

CREATE TABLE accountTransfers (
    id INT NOT NULL AUTO_INCREMENT,
    requestedOn DATETIME NOT NULL,
    emailOld VARCHAR(255) NOT NULL,
    emailNew VARCHAR(255) NOT NULL,
    tokenForOldEmail VARCHAR(255) NOT NULL,
    tokenForNewEmail VARCHAR(255) NOT NULL,
    confirmedOn DATETIME DEFAULT NULL,
    acceptedOn DATETIME DEFAULT NULL,
    confirmed BOOLEAN DEFAULT NULL,
    accepted BOOLEAN DEFAULT NULL,

    UNIQUE (id),
    PRIMARY KEY (id),
    UNIQUE INDEX(tokenForOldEmail),
    UNIQUE INDEX(tokenForNewEmail)
);

CREATE TABLE verificationTokens (
    id INT NOT NULL AUTO_INCREMENT,
    token VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    expiryDate DATETIME NOT NULL,

    UNIQUE (id),
    PRIMARY KEY (id),
    FOREIGN KEY (email) REFERENCES accounts(email) ON DELETE CASCADE ON UPDATE CASCADE,
    UNIQUE INDEX(token),
    INDEX(email)
);

CREATE TABLE authorities (
    email VARCHAR(64) NOT NULL,
    role VARCHAR(64) NOT NULL,

    FOREIGN KEY (email) REFERENCES accounts(email) ON DELETE CASCADE ON UPDATE CASCADE,
    UNIQUE INDEX(email, role)
);
