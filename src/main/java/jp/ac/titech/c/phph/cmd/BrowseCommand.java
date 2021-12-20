package jp.ac.titech.c.phph.cmd;

import jp.ac.titech.c.phph.browse.WebApplication;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.web.context.WebApplicationContext;
import picocli.CommandLine.Command;

@Slf4j
@Command(name = "browse", description = "Browse objects")
public class BrowseCommand extends BaseCommand {

    @Override
    protected void process() {
        final SpringApplication app = new SpringApplicationBuilder(WebApplication.class)
                .bannerMode(Banner.Mode.OFF)
                .properties("server.port=8080")
                .build();
        final ConfigurableApplicationContext cxt = app.run();
        ((WebApplicationContext) cxt).getServletContext().setAttribute("dao", dao);
        try {
            for (;;) {
                Thread.sleep(Long.MAX_VALUE);
            }
        } catch (final InterruptedException e) {}
    }
}
