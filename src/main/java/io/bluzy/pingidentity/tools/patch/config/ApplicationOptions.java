package io.bluzy.pingidentity.tools.patch.config;

import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.commons.cli.Option.builder;

public class ApplicationOptions {
    private static final Logger logger = LoggerFactory.getLogger(ApplicationOptions.class);

    public static final String HELP_OPTION = "help";

    public static final String DRY_RUN_OPTION = "dryRun";

    public static final String LIMIT_OPTION = "limitCount";

    private final Options options = new Options();
    private final CommandLine commandLine;

    public ApplicationOptions(String[] args) {
        Option help = builder(HELP_OPTION)
                .hasArg(false)
                .desc("print this message")
                .build();

        Option dryRun = builder(DRY_RUN_OPTION)
                .hasArg(false)
                .desc("dry run")
                .build();

        Option count = builder(LIMIT_OPTION)
                .hasArg()
                .desc("limit to count")
                .build();

        options.addOption(help);
        options.addOption(dryRun);
        options.addOption(count);

        CommandLineParser parser = new DefaultParser();
        try {
            commandLine = parser.parse(options, args);
        }
        catch (ParseException exp) {
            logger.error("Parsing failed.  Reason: " + exp.getMessage());
            throw new RuntimeException(exp);
        }
    }

    public CommandLine getCommandLine() {
        return commandLine;
    }

    public void printHelp() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("ping-patch-tool", options, true);
    }
}
