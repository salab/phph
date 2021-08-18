package jp.ac.titech.c.phph.db;

import jp.ac.titech.c.phph.model.Chunk;
import jp.ac.titech.c.phph.model.Fragment;
import jp.ac.titech.c.phph.model.Pattern;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

public interface Dao {
    @SqlQuery("INSERT INTO repositories (url) VALUES (?) RETURNING id")
    long insertRepository(final String url);

    @SqlQuery("INSERT INTO commits (repository_id, hash, message) " +
              "VALUES (?, ?, ?) RETURNING id")
    long insertCommit(final long repositoryId, final String hash, final String message);

    @SqlQuery("INSERT INTO chunks (commit_id, file, old_begin, old_end, new_begin, new_end, pattern) " +
              "VALUES (:commitId, :c.file, :c.oldBegin, :c.oldEnd, :c.newBegin, :c.newEnd, :pattern) RETURNING id")
    long insertChunk(@Bind("commitId") final long commitId, @BindBean("c") final Chunk c, @Bind("pattern") final String patternHash);

    @SqlUpdate("INSERT OR IGNORE INTO fragments (text, hash) " +
               "VALUES (:f.text, :f.hash.name)")
    boolean insertFragment(@BindBean("f") final Fragment f);

    @SqlUpdate("INSERT OR IGNORE INTO patterns (old, new, hash) " +
               "VALUES (:p.oldFragment.hash.name, :p.newFragment.hash.name, :p.hash.name)")
    boolean insertPattern(@BindBean("p") final Pattern pattern);
}
