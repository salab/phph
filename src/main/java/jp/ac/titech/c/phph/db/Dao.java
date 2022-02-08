package jp.ac.titech.c.phph.db;

import jp.ac.titech.c.phph.model.Chunk;
import jp.ac.titech.c.phph.model.Fragment;
import jp.ac.titech.c.phph.model.Hash;
import jp.ac.titech.c.phph.model.Match;
import jp.ac.titech.c.phph.model.Pattern;
import jp.ac.titech.c.phph.model.Range;
import lombok.Value;
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
import java.util.Optional;

public interface Dao {

    @SqlQuery("INSERT INTO repositories (url) VALUES (?) RETURNING id")
    long insertRepository(final String url);

    @SqlQuery("SELECT url FROM repositories LIMIT 1")
    Optional<String> findRepository();

    // -------

    @SqlQuery("INSERT INTO commits (repository_id, hash, message) VALUES (?, ?, ?) RETURNING id")
    long insertCommit(final long repositoryId, final String hash, final String message);

    @SqlQuery("SELECT * FROM commits WHERE id = ?")
    @RegisterRowMapper(CommitRowMapper.class)
    Optional<Commit> findCommit(final int id);

    class CommitRowMapper implements RowMapper<Commit> {
        @Override
        public Commit map(final ResultSet rs, final StatementContext ctx) throws SQLException {
            return new Commit(rs.getString("hash"), rs.getString("message"));
        }
    }

    @Value
    class Commit {
        String hash;
        String message;
    }

    // -------

    @SqlQuery("INSERT INTO chunks (commit_id, file, old_begin, old_end, new_begin, new_end, pattern_hash) VALUES (:commitId, :h.file, :h.oldLines.begin, :h.oldLines.end, :h.newLines.begin, :h.newLines.end, :h.pattern.hash.name) RETURNING id")
    long insertChunk(@Bind("commitId") final long commitId, @BindBean("h") final Chunk chunk);

    @SqlQuery("SELECT * FROM chunks WHERE pattern_hash = :h.name")
    @RegisterRowMapper(ChunkRowMapper.class)
    ResultIterable<DBChunk> listChunks(@BindBean("h") final Hash patternHash);

    class ChunkRowMapper implements RowMapper<DBChunk> {
        @Override
        public DBChunk map(final ResultSet rs, final StatementContext ctx) throws SQLException {
            return new DBChunk(rs.getInt("commit_id"),
                    rs.getString("file"),
                    Range.of(rs.getInt("old_begin"), rs.getInt("old_end")),
                    Range.of(rs.getInt("new_begin"), rs.getInt("new_end")));
        }
    }

    @Value
    class DBChunk {
        // TODO: Merge
        int commitId;
        String file;
        Range oldLines;
        Range newLines;
    }

    // -------

    @SqlUpdate("INSERT OR IGNORE INTO fragments (text, hash) VALUES (:f.text, :f.hash.name)")
    void insertFragment(@BindBean("f") final Fragment fragment);

    @SqlQuery("SELECT * FROM fragments")
    @RegisterRowMapper(FragmentRowMapper.class)
    ResultIterable<Fragment> listFragments();

    @SqlQuery("SELECT * FROM fragments WHERE hash LIKE ?")
    @RegisterRowMapper(FragmentRowMapper.class)
    ResultIterable<Fragment> searchFragments(final String like);

    @SqlQuery("SELECT count(*) FROM fragments WHERE hash LIKE ?")
    int countFragments(final String like);

    @SqlQuery("SELECT * FROM fragments WHERE hash = :h.name LIMIT 1")
    @RegisterRowMapper(FragmentRowMapper.class)
    Optional<Fragment> findFragment(@BindBean("h") final Hash hash);

    @SqlQuery("SELECT * FROM fragments WHERE text = ? LIMIT 1")
    @RegisterRowMapper(FragmentRowMapper.class)
    Optional<Fragment> lookupFragment(final String text);

    class FragmentRowMapper implements RowMapper<Fragment> {
        @Override
        public Fragment map(final ResultSet rs, final StatementContext ctx) throws SQLException {
            return Fragment.of(rs.getString("text"), Hash.parse(rs.getString("hash")));
        }
    }

    // -------

    @SqlUpdate("INSERT OR IGNORE INTO patterns (old, new, type, hash) VALUES (:p.oldHash.name, :p.newHash.name, :p.type.id, :p.hash.name)")
    void insertPattern(@BindBean("p") final Pattern pattern);

    @SqlUpdate("INSERT INTO a.patterns (hash) SELECT hash FROM patterns")
    void prepareAPatterns();

    @SqlUpdate("DELETE FROM a.patterns")
    void clearAPatterns();

    @SqlUpdate("UPDATE patterns AS p SET supportH = (SELECT count(*) FROM chunks AS h WHERE h.pattern_hash = p.hash)")
    void computeSupportH();

    @SqlUpdate("UPDATE patterns AS p SET supportC = (SELECT count(DISTINCT h.commit_id) FROM chunks AS h WHERE h.pattern_hash = p.hash)")
    void computeSupportC();

    @SqlUpdate("UPDATE patterns AS p SET confidenceH = CAST(p.supportH AS REAL) / (SELECT sum(p2.supportH) FROM patterns AS p2 WHERE p2.old = p.old)")
    void computeConfidenceH();

    @SqlUpdate("UPDATE patterns AS p SET confidenceC = CAST(p.supportC AS REAL) / (SELECT sum(p2.supportC) FROM patterns AS p2 WHERE p2.old = p.old)")
    void computeConfidenceC();

    @SqlUpdate("UPDATE a.patterns AS ap SET matchO = (SELECT count(*) FROM matches AS m WHERE m.query = (SELECT p.old FROM patterns AS p WHERE p.hash = ap.hash))")
    void computeMatchO();

    @SqlUpdate("UPDATE a.patterns AS ap SET matchN = (SELECT count(*) FROM matches AS m WHERE m.query = (SELECT p.new FROM patterns AS p WHERE p.hash = ap.hash))")
    void computeMatchN();

    @SqlQuery("SELECT * FROM patterns AS p LEFT OUTER JOIN a.patterns AS ap ON p.hash = ap.hash WHERE hash = :h.name")
    @RegisterRowMapper(PatternRowMapper.class)
    Optional<Pattern> searchPatterns(@BindBean("h") final Hash hash);

    @SqlQuery("SELECT * FROM patterns AS p LEFT OUTER JOIN a.patterns AS ap ON p.hash = ap.hash WHERE hash LIKE ?")
    @RegisterRowMapper(PatternRowMapper.class)
    ResultIterable<Pattern> searchPatterns(final String like);

    @SqlQuery("SELECT * FROM patterns AS p LEFT OUTER JOIN a.patterns AS ap ON p.hash = ap.hash")
    @RegisterRowMapper(PatternRowMapper.class)
    ResultIterable<Pattern> listPatterns();

    @SqlQuery("SELECT * FROM patterns AS p LEFT OUTER JOIN a.patterns AS ap ON p.hash = ap.hash WHERE old = :f.hash.name OR new = :f.hash.name")
    @RegisterRowMapper(PatternRowMapper.class)
    ResultIterable<Pattern> listPatterns(@BindBean("f") final Fragment fragment);

    @SqlQuery("SELECT * FROM patterns AS p LEFT OUTER JOIN a.patterns AS ap ON p.hash = ap.hash WHERE supportH >= ? AND confidenceH >= ? ORDER BY supportH DESC, confidenceH DESC")
    @RegisterRowMapper(PatternRowMapper.class)
    ResultIterable<Pattern> listPatternsBySupportH(final int minSupportH, final float minConfidenceH);

    @SqlQuery("SELECT * FROM patterns AS p LEFT OUTER JOIN a.patterns AS ap ON p.hash = ap.hash WHERE supportC >= ? AND confidenceC >= ? ORDER BY supportC DESC, confidenceC DESC")
    @RegisterRowMapper(PatternRowMapper.class)
    ResultIterable<Pattern> listPatternsBySupportC(final int minSupportC, final float minConfidenceC);

    @SqlQuery("SELECT count(*) FROM patterns WHERE hash LIKE ?")
    int countPatterns(final String like);

    class PatternRowMapper implements RowMapper<Pattern> {
        @Override
        public Pattern map(final ResultSet rs, final StatementContext ctx) throws SQLException {
            return Pattern.of(Hash.parse(rs.getString("old")),
                              Hash.parse(rs.getString("new")),
                              Hash.parse(rs.getString("hash")));
        }
    }

    // -------

    @SqlQuery("INSERT INTO matches (query, file, begin, end) VALUES (:m.query.name, :m.file, :m.lines.begin, :m.lines.end) RETURNING id")
    long insertMatch(@BindBean("m") final Match match);

    @SqlQuery("SELECT * FROM a.matches WHERE query = :h.name")
    @RegisterRowMapper(MatchRowMapper.class)
    ResultIterable<Match> listMatches(@BindBean("h") final Hash h);

    @SqlUpdate("DELETE FROM a.matches")
    void clearMatches();

    class MatchRowMapper implements RowMapper<Match> {
        @Override
        public Match map(final ResultSet rs, final StatementContext ctx) throws SQLException {
            return new Match(Hash.parse(rs.getString("query")), rs.getString("file"),
                    Range.of(rs.getInt("begin"), rs.getInt("end")));
        }
    }
}
