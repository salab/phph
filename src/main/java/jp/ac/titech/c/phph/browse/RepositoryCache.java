package jp.ac.titech.c.phph.browse;

import jp.ac.titech.c.phph.util.RepositoryAccess;

import java.util.HashMap;
import java.util.Map;

public class RepositoryCache {
    private final RepositoryAccess ra;

    private final Map<String, String> cache = new HashMap<>();

    public RepositoryCache(final RepositoryAccess ra) {
        this.ra = ra;
    }

    public String get(final String commit, final String path) {
        final String key = commit + "/" + path;
        return cache.computeIfAbsent(key, k -> ra.readFile(commit, path));
    }
}
