CREATE TABLE repositories (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    url TEXT UNIQUE
);
CREATE INDEX repositories_url ON repositories(url);

CREATE TABLE commits (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    repository_id INTEGER,
    hash TEXT,
    message TEXT,
    UNIQUE (repository_id, hash)
);
CREATE INDEX commits_repository_id ON commits(repository_id);
CREATE INDEX commits_hash ON commits(hash);

CREATE TABLE chunks (
    id INTEGER PRIMARY KEY,
    commit_id INTEGER,
    file TEXT,
    old_begin INTEGER,
    old_end INTEGER,
    new_begin INTEGER,
    new_end INTEGER,
    pattern BLOB
);

CREATE TABLE fragments (
    /* id INTEGER PRIMARY KEY AUTOINCREMENT, */
    text TEXT,
    hash BLOB UNIQUE
    /* , UNIQUE (text) */
);
CREATE INDEX fragments_hash on fragments(hash);

CREATE TABLE patterns (
    /* id INTEGER PRIMARY KEY AUTOINCREMENT, */
    old TEXT,
    new TEXT,
    /* pre TEXT, */
    /* post TEXT, */
    hash BLOB UNIQUE,
    type INTEGER
    /* , UNIQUE (old, new, pre, post) */
);
CREATE INDEX patterns_old_hash on patterns(old);
CREATE INDEX patterns_new_hash on patterns(new);
CREATE INDEX patterns_hash on patterns(hash);
