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
    pattern_hash TEXT
);
CREATE INDEX chunks_pattern_hash ON chunks(pattern_hash);

CREATE TABLE fragments (
    /* id INTEGER PRIMARY KEY AUTOINCREMENT, */
    text TEXT UNIQUE,
    hash TEXT UNIQUE
);
CREATE INDEX fragments_hash ON fragments(hash);

CREATE TABLE patterns (
    /* id INTEGER PRIMARY KEY AUTOINCREMENT, */
    old TEXT,
    new TEXT,
    type INTEGER,
    hash TEXT UNIQUE,
    supportH INTEGER,
    supportC INTEGER,
    confidenceH REAL,
    confidenceC REAL,
    UNIQUE (old, new)
);
CREATE INDEX patterns_old ON patterns(old);
CREATE INDEX patterns_new ON patterns(new);
CREATE INDEX patterns_hash ON patterns(hash);
CREATE INDEX patterns_supportH ON patterns(supportH, confidenceH);
CREATE INDEX patterns_supportC ON patterns(supportC, confidenceC);
CREATE INDEX patterns_confidenceH ON patterns(confidenceH);
CREATE INDEX patterns_confidenceC ON patterns(confidenceC);

DROP TABLE IF EXISTS a.patterns;
CREATE TABLE a.patterns (
      hash TEXT UNIQUE,
      matchO INTEGER,
      matchN INTEGER,
      UNIQUE (hash)
);
CREATE INDEX a.patterns_hash ON patterns(hash);

DROP TABLE IF EXISTS a.matches;
CREATE TABLE a.matches (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    query TEXT,
    file TEXT,
    begin INTEGER,
    end INTEGER
);
CREATE INDEX a.matches_query ON matches(query);
CREATE INDEX a.matches_file ON matches(file);
