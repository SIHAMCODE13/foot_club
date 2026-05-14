-- Script pour mettre à jour tous les emails en @gmail.com
-- Exécuter ce script dans votre base de données MySQL

-- Afficher les emails actuels avant modification
SELECT id, email, nom, prenom, role FROM users;

-- Mettre à jour tous les emails pour qu'ils se terminent par @gmail.com
-- Garde uniquement la partie avant le @ et ajoute @gmail.com
UPDATE users 
SET email = CONCAT(
    SUBSTRING_INDEX(email, '@', 1),  -- Garde la partie avant le @
    '@gmail.com'                      -- Ajoute @gmail.com
);

-- Afficher les emails après modification pour vérification
SELECT id, email, nom, prenom, role FROM users;

-- Exemples de transformations :
-- admin@club.com → admin@gmail.com
-- coach@club.com → coach@gmail.com
-- member@club.com → member@gmail.com
-- utilisateur@yahoo.fr → utilisateur@gmail.com
-- test@hotmail.com → test@gmail.com
