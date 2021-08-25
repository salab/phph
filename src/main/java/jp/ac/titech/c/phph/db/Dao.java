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

    @SqlQuery("INSERT INTO chunks (commit_id, file, old_begin, old_end, new_begin, new_end, pattern_hash) VALUES (:commitId, :h.file, :h.oldBegin, :h.oldEnd, :h.newBegin, :h.newEnd, :h.pattern.hash.name) RETURNING id")
    long insertChunk(@Bind("commitId") final long commitId, @BindBean("h") final Chunk h);

    // -------

    @SqlUpdate("INSERT OR IGNORE INTO fragments (text, hash) VALUES (:f.text, :f.hash.name)")
    void insertFragment(@BindBean("f") final Fragment f);

    @SqlQuery("SELECT * FROM fragments")
    @RegisterRowMapper(FragmentRowMapper.class)
    ResultIterable<Fragment> listFragments();

    @SqlQuery("SELECT * FROM fragments WHERE hash LIKE ?")
    @RegisterRowMapper(FragmentRowMapper.class)
    ResultIterable<Fragment> searchFragments(final String pattern);

    @SqlQuery("SELECT count(*) FROM fragments WHERE hash LIKE ?")
    int countFragments(final String pattern);

    @SqlQuery("SELECT * FROM fragments WHERE hash = :h.name LIMIT 1")
    @RegisterRowMapper(FragmentRowMapper.class)
    Fragment findFragment(@BindBean("h") final Hash hash);

    class FragmentRowMapper implements RowMapper<Fragment> {
        @Override
        public Fragment map(final ResultSet rs, final StatementContext ctx) throws SQLException {
            return Fragment.of(rs.getString("text"), Hash.parse(rs.getString("hash")));
        }
    }

    // -------

    @SqlUpdate("INSERT OR IGNORE INTO patterns (old, new, type, hash) VALUES (:p.oldHash.name, :p.newHash.name, :p.type.id, :p.hash.name)")
    void insertPattern(@BindBean("p") final Pattern p);

    @SqlUpdate("UPDATE patterns AS p SET supportH = (SELECT count(*) FROM chunks AS h WHERE h.pattern_hash = p.hash)")
    void computeSupportH();

    @SqlUpdate("UPDATE patterns AS p SET supportC = (SELECT count(DISTINCT h.commit_id) FROM chunks AS h WHERE h.pattern_hash = p.hash)")
    void computeSupportC();

    @SqlUpdate("UPDATE patterns AS p SET confidenceH = CAST(p.supportH AS REAL) / (SELECT sum(p2.supportH) FROM patterns AS p2 WHERE p2.old = p.old)")
    void computeConfidenceH();

    @SqlUpdate("UPDATE patterns AS p SET confidenceC = CAST(p.supportC AS REAL) / (SELECT sum(p2.supportC) FROM patterns AS p2 WHERE p2.old = p.old)")
    void computeConfidenceC();

    @SqlUpdate("UPDATE patterns AS p SET matchO = (SELECT count(*) FROM matches AS m WHERE m.query = p.old)")
    void computeMatchO();

    @SqlUpdate("UPDATE patterns AS p SET matchN = (SELECT count(*) FROM matches AS m WHERE m.query = p.new)")
    void computeMatchN();

    @SqlQuery("SELECT * FROM patterns")
    @RegisterRowMapper(PatternRowMapper.class)
    ResultIterable<Pattern> listPatterns();

    @SqlQuery("SELECT * FROM patterns WHERE old = :f.hash.name OR new = :f.hash.name")
    @RegisterRowMapper(PatternRowMapper.class)
    ResultIterable<Pattern> listPatterns(@BindBean("f") final Fragment f);

    @SqlQuery("SELECT * FROM patterns AS p WHERE p.supportH >= ? AND p.confidenceH >= ?")
    @RegisterRowMapper(PatternRowMapper.class)
    ResultIterable<Pattern> listPatternsBySupportH(final int minSupportH, final float minConfidenceH);

    @SqlQuery("SELECT * FROM patterns AS p WHERE p.supportC >= ? AND p.confidenceC >= ?")
    @RegisterRowMapper(PatternRowMapper.class)
    ResultIterable<Pattern> listPatternsBySupportC(final int minSupportC, final float minConfidenceC);

    class PatternRowMapper implements RowMapper<Pattern> {
        @Override
        public Pattern map(final ResultSet rs, final StatementContext ctx) throws SQLException {
            return Pattern.of(Hash.parse(rs.getString("old")),
                              Hash.parse(rs.getString("new")),
                              Hash.parse(rs.getString("hash")));
        }
    }

    // -------

    @SqlQuery("INSERT INTO matches (query, file, begin, end) VALUES (:m.query.name, :m.file, :m.beginLine, :m.endLine) RETURNING id")
    long insertMatch(@BindBean("m") final Match m);

    @SqlUpdate("DELETE FROM matches")
    void clearMatches();
}
