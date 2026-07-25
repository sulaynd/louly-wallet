ALTER TABLE partners ADD COLUMN description VARCHAR(500);

UPDATE partners SET description = 'Envoyer de l''argent vers un portefeuille numérique en devise locale'
    WHERE name = 'Wave ou Orange';

UPDATE partners SET description = 'Envoyer de l''argent à une adresse physique pour un retrait en personne'
    WHERE name = 'BNB Cash Pickup';

UPDATE partners SET description = 'Envoyer de l''argent directement sur un compte bancaire'
    WHERE name = 'Compte bancaire';
