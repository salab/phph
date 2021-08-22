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
              "VALUES (:commitId, :h.file, :h.oldBegin, :h.oldEnd, :h.newBegin, :h.newEnd, :h.pattern.hash.raw) RETURNING id")
    long insertChunk(@Bind("commitId") final long commitId, @BindBean("h") final Chunk h);

    @SqlUpdate("INSERT OR IGNORE INTO fragments (text, hash) " +
               "VALUES (:f.text, :f.hash.raw)")
    boolean insertFragment(@BindBean("f") final Fragment f);

    @SqlUpdate("INSERT OR IGNORE INTO patterns (old, new, hash) " +
               "VALUES (:p.oldHash.raw, :p.newHash.raw, :p.hash.raw)")
    boolean insertPattern(@BindBean("p") final Pattern pattern);
}
