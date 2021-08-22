package jp.ac.titech.c.phph.parse;

import jp.ac.titech.c.phph.RepositoryAccess;
import jp.ac.titech.c.phph.diff.Differencer;
import jp.ac.titech.c.phph.model.Chunk;
import jp.ac.titech.c.phph.model.Statement;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.FilenameUtils;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.revwalk.RevCommit;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Log4j2
public class ChunkExtractor {
    private final Differencer<Statement> differencer;

    private final Splitter splitter;

    private final RepositoryAccess ra;

    public ChunkExtractor(final Differencer<Statement> differncer, final Splitter splitter, final RepositoryAccess ra) {
        this.differencer = differncer;
        this.splitter = splitter;
        this.ra = ra;
    }

    /**
     * Processes a commit and obtains a list of changes.
     */
    public List<Chunk> extract(final RevCommit c) {
        final List<DiffEntry> entries = ra.getChanges(c);
        return entries.stream()
                .filter(e -> isSupportedFileChange(e, "java"))
                .flatMap(this::extractChanges)
                .collect(Collectors.toList());
    }

    /**
     * Checks the filtering criteria.
     */
    protected boolean isSupportedFileChange(final DiffEntry e, final String extension) {
        final String oldPath = e.getOldPath();
        final String newPath = e.getNewPath();
        final String oldExtension = FilenameUtils.getExtension(oldPath);
        final String newExtension = FilenameUtils.getExtension(newPath);
        return oldPath.equals(newPath) && extension.equals(oldExtension) && extension.equals(newExtension);
    }

    /**
     * Extracts a stream of changes from a DiffEntry.
     */
    protected Stream<Chunk> extractChanges(final DiffEntry entry) {
        final String oldSource = ra.readBlob(entry.getOldId().toObjectId());
        final String newSource = ra.readBlob(entry.getNewId().toObjectId());
        final List<Statement> oldStatements = splitter.split(oldSource);
        final List<Statement> newStatements = splitter.split(newSource);
        return differencer.compute(oldStatements, newStatements).stream()
                .map(e -> Chunk.of(entry.getNewPath(), oldStatements, newStatements, e));
    }
}
