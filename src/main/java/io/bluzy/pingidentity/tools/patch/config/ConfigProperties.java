package io.bluzy.pingidentity.tools.patch.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class ConfigProperties {
    public static final String PA_BASEURL = "pa.baseurl";

    public static final String PA_RULESETS_PATH = "pa.rulesets.path";
    public static final String PA_APPLICATIONS_PATH = "pa.applications.path";

    public static final String PA_RULESETS_FILTER = "pa.rulesets.filter";
    public static final String PA_APPLICATIONS_REGEXP = "pa.applications.regexp";

    public static final String AUTH_USER = "auth.user";
    public static final String AUTH_PASS = "auth.pass";

    public static final String LOG_LEVEL_ROOT = "log.level.root";

    public static Map<String, Object> loadProperties(InputStream input) throws IOException {
        Properties prop = new Properties();
        prop.load(input);

        Map<String, Object> properties = new HashMap<>();

        properties.put(PA_BASEURL, prop.getProperty(PA_BASEURL));
        properties.put(PA_RULESETS_PATH, prop.getProperty(PA_RULESETS_PATH));
        properties.put(PA_APPLICATIONS_PATH, prop.getProperty(PA_APPLICATIONS_PATH));

        properties.put(PA_RULESETS_FILTER, prop.getProperty(PA_RULESETS_FILTER));
        properties.put(PA_APPLICATIONS_REGEXP, prop.getProperty(PA_APPLICATIONS_REGEXP));

        properties.put(AUTH_USER, prop.getProperty(AUTH_USER));
        properties.put(AUTH_PASS, prop.getProperty(AUTH_PASS));

        properties.put(LOG_LEVEL_ROOT, prop.getProperty(LOG_LEVEL_ROOT));

        return properties;
    }
}
