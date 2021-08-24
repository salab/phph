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
CREATE INDEX chunks_pattern_hash on chunks(pattern_hash);

CREATE TABLE fragments (
    /* id INTEGER PRIMARY KEY AUTOINCREMENT, */
    text TEXT UNIQUE,
    hash TEXT UNIQUE
);
CREATE INDEX fragments_hash on fragments(hash);

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
    matchO INTEGER,
    matchN INTEGER,
    UNIQUE (old, new)
);
CREATE INDEX patterns_old_hash on patterns(old);
CREATE INDEX patterns_new_hash on patterns(new);
CREATE INDEX patterns_hash on patterns(hash);
CREATE INDEX patterns_supportH on patterns(supportH, confidenceH);
CREATE INDEX patterns_supportC on patterns(supportC, confidenceC);
CREATE INDEX patterns_confidenceH on patterns(confidenceH);
CREATE INDEX patterns_confidenceC on patterns(confidenceC);

CREATE TABLE matches (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    query TEXT,
    file TEXT,
    begin INTEGER,
    end INTEGER
);
CREATE INDEX matches_query on matches(query);
CREATE INDEX matches_file on matches(file);
