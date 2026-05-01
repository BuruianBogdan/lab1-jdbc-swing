PRAGMA foreign_keys = ON;

CREATE TABLE IF NOT EXISTS categorii (
                                         id INTEGER PRIMARY KEY AUTOINCREMENT,
                                         nume TEXT NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS produse (
                                       id INTEGER PRIMARY KEY AUTOINCREMENT,
                                       nume TEXT NOT NULL,
                                       pret REAL NOT NULL CHECK (pret > 0),
    stoc INTEGER NOT NULL CHECK (stoc >= 0),
    categorie_id INTEGER NOT NULL,
    FOREIGN KEY (categorie_id) REFERENCES categorii(id) ON DELETE CASCADE
    );

CREATE TABLE IF NOT EXISTS etichete (
                                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                                        nume TEXT NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS produs_eticheta (
                                               produs_id INTEGER NOT NULL,
                                               eticheta_id INTEGER NOT NULL,
                                               PRIMARY KEY (produs_id, eticheta_id),
    FOREIGN KEY (produs_id) REFERENCES produse(id) ON DELETE CASCADE,
    FOREIGN KEY (eticheta_id) REFERENCES etichete(id) ON DELETE CASCADE
);