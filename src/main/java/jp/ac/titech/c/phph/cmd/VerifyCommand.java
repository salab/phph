package jp.ac.titech.c.phph.cmd;

import jp.ac.titech.c.phph.model.Fragment;
import jp.ac.titech.c.phph.model.Pattern;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine.Command;

import java.util.Optional;

@Slf4j
@Command(name = "verify", description = "Verify database")
public class VerifyCommand extends BaseCommand {
    @Override
    protected void process() {
        for (final Pattern p : dao.listPatterns()) {
            final Optional<Fragment> oldFragment = dao.findFragment(p.getOldHash());
            if (oldFragment.isEmpty()) {
                log.warn("{}: old fragment ({}) not found", p.getHash(), p.getOldHash());
            }
            final Optional<Fragment> newFragment = dao.findFragment(p.getNewHash());
            if (newFragment.isEmpty()) {
                log.warn("{}: new fragment ({}) not found", p.getHash(), p.getOldHash());
            }
        }
    }
}
