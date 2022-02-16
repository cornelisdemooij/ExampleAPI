-- Accounts (password of account one is "abcd")
INSERT INTO `accounts`(`id`, `publicUsername`, `email`, `phoneNumber`, `birthDate`, `description`, `hashedPassword`, `enabled`, `creation`, `deleted`, `lastPasswordResetDate`)
    VALUES (1, 'cornelis', 'info@cornelisdemooij.com', '06-12345678', '1991-04-01', 'Co-founder of Example', '$2a$10$nzoZ2.C5qV7BQ/UrZ23PMePkbx/PYooH67lKSu3uXBkuvwoYCZTz.', true, '2021-02-28 16:37:45', false, '2021-03-13 14:56:05');

-- Authorities
INSERT INTO authorities(email, role) VALUES ('info@cornelisdemooij.com', 'USER');
