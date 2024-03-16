/*  Copyright 2024 Florin Potera

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/
package io.bluzy.pingidentity.tools.patch;

import ch.qos.logback.classic.Level;
import io.bluzy.pingidentity.tools.patch.config.ApplicationOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import static io.bluzy.pingidentity.tools.patch.config.ConfigProperties.LOG_LEVEL_ROOT;
import static io.bluzy.pingidentity.tools.patch.config.ConfigProperties.loadProperties;
import static java.lang.System.exit;
import static java.util.Objects.nonNull;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    private static final String DEFAULT_CONFIG_PROPERTIES_FILE_NAME = "config.properties";

    private static final String CONFIG_PROPERTIES_ENV_VAR = "CONFIG_PROPERTIES";

    private final ApplicationOptions appOptions;
    private Map<String,Object> properties;

    public Main(String[] args) {
        setRootLoggingLevel(Level.INFO);
        loadConfiguration();
        setRootLoggingLevel(Level.valueOf(String.valueOf(properties.get(LOG_LEVEL_ROOT))));
        appOptions = new ApplicationOptions(args);
    }

    public static void main(String[] args) throws Exception {
        new Main(args).run();
    }

    public void run() throws Exception {
        if(appOptions.getCommandLine().hasOption(ApplicationOptions.HELP_OPTION)) {
            appOptions.printHelp();
            return;
        }

        PatchJob patchJob = new PatchJob(properties);
        patchJob.doPatch(appOptions.getCommandLine().hasOption(ApplicationOptions.DRY_RUN_OPTION),
                Long.parseLong(appOptions.getCommandLine().getOptionValue(ApplicationOptions.LIMIT_OPTION,
                        String.valueOf(Long.MAX_VALUE))));
    }

    private void loadConfiguration() {
        String configPropertiesFileName = System.getenv(CONFIG_PROPERTIES_ENV_VAR);
        configPropertiesFileName = nonNull(configPropertiesFileName)? configPropertiesFileName: DEFAULT_CONFIG_PROPERTIES_FILE_NAME;

        logger.info("config properties used: {}", configPropertiesFileName);

        try (InputStream input = getClass().getClassLoader().getResourceAsStream(configPropertiesFileName)) {
            if (input == null) {
                logger.error("Sorry, unable to find: {}", configPropertiesFileName);
                exit(1);
            }
            properties = loadProperties(input);

            logger.info("properties: {}", properties);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void setRootLoggingLevel(Level level) {
        ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        root.setLevel(level);
    }

}
