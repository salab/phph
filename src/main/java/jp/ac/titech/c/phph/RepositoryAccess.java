package jp.ac.titech.c.phph;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.RawText;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.revwalk.filter.RevFilter;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.util.io.DisabledOutputStream;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

@Log4j2
public class RepositoryAccess implements AutoCloseable {
    @Getter
    private final Repository repository;

    @Getter
    private final RevWalk walk;

    @Getter
    private final ObjectReader reader;

    public RepositoryAccess(final Repository repository) {
        this.repository = repository;
        repository.incrementOpen();
        this.walk = new RevWalk(repository);
        this.reader = repository.newObjectReader();
    }

    public RepositoryAccess(final Path path) {
        this.repository = createRepository(path);
        this.walk = new RevWalk(repository);
        this.reader = repository.newObjectReader();
    }

    /**
     * Opens Git repository.
     */
    private static Repository createRepository(final Path path) {
        try {
            final FileRepositoryBuilder builder = new FileRepositoryBuilder();
            return builder.setGitDir(path.toFile()).readEnvironment().findGitDir().build();
        } catch (final IOException e) {
            log.error("Invalid repository path: {}", path);
            return null;
        }
    }

    /**
     * Creates the formatter.
     */
    private static DiffFormatter createFormatter(final Repository repo) {
        final DiffFormatter formatter = new DiffFormatter(DisabledOutputStream.INSTANCE);
        formatter.setRepository(repo);
        formatter.setDiffComparator(RawTextComparator.DEFAULT);
        formatter.setDetectRenames(true);
        return formatter;
    }

    /**
     * Resolves a revision name to a RevCommit.
     */
    public RevCommit resolve(final String name) {
        try {
            final ObjectId commitId = repository.resolve(name);
            return walk.parseCommit(commitId);
        } catch (final IOException e) {
            log.error(e);
            return null;
        }
    }

    /**
     * Walk commits.
     */
    public Iterable<RevCommit> walk(final String commitFrom, final String commitTo) {
        // from: exclusive (from, to]
        if (commitFrom != null) {
            try {
                final RevCommit c = walk.parseCommit(repository.resolve(commitFrom));
                log.info("Range from [exclusive]: {} ({})", commitFrom, c.getId().name());
                walk.markUninteresting(c);
            } catch (final IOException e) {
                log.error("Invalid rev: {} ({})", commitFrom, e);
            }
        }

        // end: inclusive (from, to]
        if (commitTo != null) {
            try {
                final RevCommit c = walk.parseCommit(repository.resolve(commitTo));
                log.info("Range to (inclusive): {} ({})", commitTo, c.getId().name());
                walk.markStart(c);
            } catch (final IOException e) {
                log.error("Invalid rev: {} ({})", commitTo, e);
            }
        }

        walk.setRevFilter(RevFilter.NO_MERGES);
        return walk;
    }

    /**
     * Reads a blob object.
     */
    public String readBlob(final ObjectId blobId) {
        try {
            final ObjectLoader loader = reader.open(blobId, Constants.OBJ_BLOB);
            final RawText rawText = new RawText(loader.getCachedBytes());
            // TODO UTF-8 only
            return rawText.getString(0, rawText.size(), false);
        } catch (final IOException e) {
            log.error(e);
            return "";
        }
    }

    /**
     * Gets the changes done in a commit, compared to its first parent.
     */
    public List<DiffEntry> getChanges(final RevCommit c) {
        try (final DiffFormatter fmt = createFormatter(repository)) {
            // gives null for a root commit
            final ObjectId parentId = c.getParentCount() == 1 ? c.getParent(0).getId() : null;
            return fmt.scan(parentId, c.getId());
        } catch (final IOException e) {
            log.error(e);
            return Collections.emptyList();
        }
    }

    @Override
    public void close() {
        if (reader != null) {
            reader.close();
        }
        if (walk != null) {
            walk.close();
        }
        repository.close();
    }
}
