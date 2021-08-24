package jp.ac.titech.c.phph.cmd;

import lombok.extern.log4j.Log4j2;
import picocli.CommandLine.Command;

@Log4j2
@Command(name = "measure", description = "Compute metric values")
public class MeasureCommand extends BaseCommand {
    @Override
    protected void process() {
        log.debug("Compute chunk-based support...");
        dao.computeSupportH();
        log.debug("Compute commit-based support...");
        dao.computeSupportC();
        log.debug("Compute chunk-based confidence...");
        dao.computeConfidenceH();
        log.debug("Compute commit-based confidence...");
        dao.computeConfidenceC();
        log.debug("Compute MatchO...");
        dao.computeMatchO();
        log.debug("Compute MatchN...");
        dao.computeMatchN();
    }
}
