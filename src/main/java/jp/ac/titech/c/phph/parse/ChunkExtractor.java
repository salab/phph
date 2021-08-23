package jp.ac.titech.c.phph.parse;

import jp.ac.titech.c.phph.util.RepositoryAccess;
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

    private final int minSize;

    private final int maxSize;

    private final int contextSize;

    public ChunkExtractor(final Differencer<Statement> differencer, final Splitter splitter, final int minSize, final int maxSize, final int contextSize) {
        this.differencer = differencer;
        this.splitter = splitter;
        this.minSize = minSize;
        this.maxSize = maxSize;
        this.contextSize = contextSize;
    }

    /**
     * Processes a commit and obtains a list of changes.
     */
    public List<Chunk> extract(final RevCommit c, final RepositoryAccess ra) {
        final List<DiffEntry> entries = ra.getChanges(c);
        return entries.stream()
                .filter(e -> isSupportedFileChange(e, "java"))
                .flatMap(e -> extractChanges(e, ra))
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
    protected Stream<Chunk> extractChanges(final DiffEntry entry, final RepositoryAccess ra) {
        final String oldSource = ra.readBlob(entry.getOldId().toObjectId());
        final String newSource = ra.readBlob(entry.getNewId().toObjectId());
        final List<Statement> oldStatements = splitter.split(oldSource);
        final List<Statement> newStatements = splitter.split(newSource);
        return differencer.compute(oldStatements, newStatements).stream()
                .filter(e -> minSize <= e.getLengthA() && e.getLengthA() <= maxSize &&
                             minSize <= e.getLengthB() && e.getLengthB() <= maxSize)
                .map(e -> Chunk.of(entry.getNewPath(), oldStatements, newStatements, e, contextSize));
    }
}
