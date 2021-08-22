package jp.ac.titech.c.phph;

import lombok.extern.log4j.Log4j2;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.AndTreeFilter;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.eclipse.jgit.treewalk.filter.PathSuffixFilter;
import org.eclipse.jgit.treewalk.filter.TreeFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Log4j2
public class FileTree {
    /**
     * Obtains the contents of the given directory.
     */
    public static Map<String, String> retrieveLocalTree(final Path dir, final String suffix) throws IOException {
        return Files.walk(dir, Integer.MAX_VALUE)
                .filter(p -> p.endsWith(suffix))
                .collect(Collectors.toMap(Path::toString, FileTree::slurp));
    }

    /**
     * Obtains the contents of the given commit in a Git repository.
     */
    public static Map<String, String> retrieveGitTree(final Path repository, final String revision, final String prefix, final String suffix) throws IOException {
        final Map<String, String> result = new TreeMap<>();
        try (final RepositoryAccess ra = new RepositoryAccess(repository)) {
            final RevCommit commit = ra.resolve(revision);
            final TreeWalk tw = new TreeWalk(ra.getReader());
            tw.addTree(commit.getTree());
            tw.setRecursive(true);
            tw.setFilter(AndTreeFilter.create(prefix == null ? TreeFilter.ALL : PathFilter.create(prefix),
                                              suffix == null ? TreeFilter.ALL : PathSuffixFilter.create(suffix)));
            while (tw.next()) {
                final String path = tw.getPathString();
                final String source = ra.readBlob(tw.getObjectId(0));
                result.put(path, source);
            }
        }
        return result;
    }

    /**
     * Obtains the content of the given file without throwing IOException.
     */
    public static String slurp(final Path file) {
        try {
            return Files.readString(file, StandardCharsets.UTF_8);
        } catch (final IOException e) {
            log.error(e.getMessage(), e);
            return "";
        }
    }
}
