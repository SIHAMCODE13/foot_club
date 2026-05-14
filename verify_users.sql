-- Script pour vérifier les utilisateurs créés récemment
SELECT 
    id,
    email,
    nom,
    prenom,
    role,
    account_status,
    actif,
    activation_token IS NOT NULL as has_token,
    date_inscription
FROM users 
ORDER BY id DESC 
LIMIT 10;
