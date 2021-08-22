package jp.ac.titech.c.phph.db;

import jp.ac.titech.c.phph.model.Chunk;
import jp.ac.titech.c.phph.model.Fragment;
import jp.ac.titech.c.phph.model.Hash;
import jp.ac.titech.c.phph.model.Match;
import jp.ac.titech.c.phph.model.Pattern;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.result.ResultIterable;
import org.jdbi.v3.core.statement.StatementContext;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public interface Dao {

    @SqlQuery("INSERT INTO repositories (url) VALUES (?) RETURNING id")
    long insertRepository(final String url);

    // -------

    @SqlQuery("INSERT INTO commits (repository_id, hash, message) VALUES (?, ?, ?) RETURNING id")
    long insertCommit(final long repositoryId, final String hash, final String message);

    // -------

    @SqlQuery("INSERT INTO chunks (commit_id, file, old_begin, old_end, new_begin, new_end, pattern_hash) VALUES (:commitId, :h.file, :h.oldBegin, :h.oldEnd, :h.newBegin, :h.newEnd, :h.pattern.hash.raw) RETURNING id")
    long insertChunk(@Bind("commitId") final long commitId, @BindBean("h") final Chunk h);

    // -------

    @SqlUpdate("INSERT OR IGNORE INTO fragments (text, hash) VALUES (:f.text, :f.hash.raw)")
    void insertFragment(@BindBean("f") final Fragment f);

    @SqlQuery("SELECT * FROM fragments")
    @RegisterRowMapper(FragmentRowMapper.class)
    ResultIterable<Fragment> listFragments();

    @SqlQuery("SELECT * FROM fragments WHERE hash = :h.raw LIMIT 1")
    @RegisterRowMapper(FragmentRowMapper.class)
    Fragment findFragment(@BindBean("h") final Hash hash);

    class FragmentRowMapper implements RowMapper<Fragment> {
        @Override
        public Fragment map(final ResultSet rs, final StatementContext ctx) throws SQLException {
            return Fragment.of(rs.getString("text"), Hash.of(rs.getBytes("hash")));
        }
    }

    // -------

    @SqlUpdate("INSERT OR IGNORE INTO patterns (old, new, hash, type) VALUES (:p.oldHash.raw, :p.newHash.raw, :p.hash.raw, :p.type.id)")
    void insertPattern(@BindBean("p") final Pattern pattern);

    @SqlUpdate("UPDATE patterns AS p SET supportH = (SELECT count(*) from chunks As h WHERE h.pattern_hash = p.hash)")
    void computeSupportH();

    @SqlUpdate("UPDATE patterns AS p SET supportC = (SELECT count(DISTINCT h.commit_id) from chunks As h WHERE h.pattern_hash = p.hash)")
    void computeSupportC();

    @SqlUpdate("UPDATE patterns AS p SET confidenceH = CAST(p.supportH AS REAL) / (SELECT sum(p2.supportH) from patterns As p2 WHERE p2.old = p.old)")
    void computeConfidenceH();

    @SqlUpdate("UPDATE patterns AS p SET confidenceC = CAST(p.supportC AS REAL) / (SELECT sum(p2.supportC) from patterns As p2 WHERE p2.old = p.old)")
    void computeConfidenceC();

    @SqlQuery("SELECT * FROM patterns")
    @RegisterRowMapper(PatternRowMapper.class)
    ResultIterable<Pattern> listPatterns();

    @SqlQuery("SELECT * FROM patterns AS p WHERE p.supportH >= ? AND p.confidenceH >= ?")
    @RegisterRowMapper(PatternRowMapper.class)
    ResultIterable<Pattern> listPatternsBySupportH(final int minSupportH, final float minConfidenceH);

    @SqlQuery("SELECT * FROM patterns AS p WHERE p.supportC >= ? AND p.confidenceC >= ?")
    @RegisterRowMapper(PatternRowMapper.class)
    ResultIterable<Pattern> listPatternsBySupportC(final int minSupportC, final float minConfidenceC);

    class PatternRowMapper implements RowMapper<Pattern> {
        @Override
        public Pattern map(final ResultSet rs, final StatementContext ctx) throws SQLException {
            return Pattern.of(Hash.of(rs.getBytes("old")),
                    Hash.of(rs.getBytes("new")),
                    Hash.of(rs.getBytes("hash")));
        }
    }

    // -------

    @SqlQuery("INSERT INTO matches (query, file, begin, end) VALUES (:m.query.raw, :m.file, :m.beginLine, :m.endLine) RETURNING id")
    long insertMatch(@BindBean("m") final Match match);

    @SqlUpdate("DELETE FROM matches")
    long clearMatches();
}
