package jp.ac.titech.c.phph.browse;

import com.google.common.collect.Lists;
import jp.ac.titech.c.phph.db.Dao;
import jp.ac.titech.c.phph.model.Chunk;
import jp.ac.titech.c.phph.model.Fragment;
import jp.ac.titech.c.phph.model.Hash;
import jp.ac.titech.c.phph.model.Match;
import jp.ac.titech.c.phph.model.Pattern;
import jp.ac.titech.c.phph.model.Range;
import jp.ac.titech.c.phph.util.RepositoryAccess;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.PumpStreamHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.ServletContext;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@Slf4j
public class ViewerController {
    @Autowired
    ServletContext context;

    private Dao getDao() {
        return (Dao) context.getAttribute("dao");
    }

    @GetMapping("/")
    public String root(final Model model) {
        return index(model, 0, 0.0f, 0, 0, 9999);
    }

    @GetMapping("/index")
    public String index(final Model model, final int minSupport, final float minConfidence, final int minMatchO, final int minMatchN, final int maxMatchO) {
        model.addAttribute("minSupport", minSupport);
        model.addAttribute("minConfidence", minConfidence);
        model.addAttribute("minMatchO", minMatchO);
        model.addAttribute("minMatchN", minMatchN);
        model.addAttribute("maxMatchO", maxMatchO);
        model.addAttribute("patterns", getDao().listPatterns(minSupport, minConfidence, minMatchO, minMatchN, maxMatchO));
        return "index";
    }

    @GetMapping("/pattern/{hash}")
    public String pattern(final Model model, @PathVariable final String hash) {
        final Pattern p = getDao().searchPattern(Hash.parse(hash)).get();
        model.addAttribute("pattern", p);

        final List<Dao.DBChunk> chunks = Lists.newArrayList(getDao().listChunks(p.getHash()));
        model.addAttribute("chunks", chunks);
        model.addAttribute("chunkgroups", chunks.stream().collect(Collectors.groupingBy(Dao.DBChunk::getCommitId)));

        model.addAttribute("matches", Lists.newArrayList(getDao().listMatches(p.getOldHash())));
        model.addAttribute("repository", getDao().findRepository().get());
        return "pattern";
    }

    @GetMapping("/fragment/{hash}")
    public String fragment(final Model model, @PathVariable final String hash) {
        final Fragment f = getDao().findFragment(Hash.parse(hash)).get();
        model.addAttribute("fragment", f);
        model.addAttribute("patterns", Lists.newArrayList(getDao().listPatterns(f)));
        model.addAttribute("repository", getDao().findRepository().get());
        return "fragment";
    }

    @GetMapping("/commit/{hash}")
    public String commit(final Model model, @PathVariable final String hash) {
        String repo = getDao().findRepository().get();
        final String cmd = String.format("git --git-dir=%s show %s", repo, hash);
        final String content = execute(cmd);
        model.addAttribute("content", content);
        return "commit";
    }

    @GetMapping("/file/{commit}")
    public String file(final Model model, @PathVariable final String commit, @RequestParam final String file) {
        String repository = getDao().findRepository().get();
        try (final RepositoryAccess ra = new RepositoryAccess(Path.of(repository))) {
            final String source = ra.readFile(commit, file);
            model.addAttribute("lines", source.lines().collect(Collectors.toList()));
        }
        return "file";
    }

    public String execute(final String command) {
        try {
            final OutputStream out = new ByteArrayOutputStream();
            final CommandLine cmdline = CommandLine.parse(command);
            final DefaultExecutor exec = new DefaultExecutor();
            final PumpStreamHandler streamHandler = new PumpStreamHandler(out);
            exec.setStreamHandler(streamHandler);
            exec.execute(cmdline);
            return out.toString();
        } catch (final IOException e) {
            return "error";
        }
    }
}
