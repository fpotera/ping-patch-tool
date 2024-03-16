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

import io.bluzy.pingidentity.tools.patch.auth.BasicAuthentication;
import io.bluzy.pingidentity.tools.patch.config.ConfigProperties;
import io.bluzy.pingidentity.tools.patch.config.RestAPIClient;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.google.common.collect.Streams.stream;
import static java.lang.String.valueOf;

public class PatchJob {
    private static final Logger logger = LoggerFactory.getLogger(PatchJob.class);

    private Map<String, Object> properties;

    private RestAPIClient paRestClient;

    private long patchCount;

    public PatchJob(Map<String, Object> properties) throws Exception {
        this.properties = properties;

        BasicAuthentication basicAuth = new BasicAuthentication(valueOf(properties.get(ConfigProperties.AUTH_USER)),
                valueOf(properties.get(ConfigProperties.AUTH_PASS)));
        paRestClient = new RestAPIClient((String) properties.get(ConfigProperties.PA_BASEURL), basicAuth);
    }

    public void doPatch(boolean dryRun, long limitCount) throws Exception {
        logger.info("dryRun: {}", dryRun);
        JSONObject ruleSet = getRuleSet();
        String ruleSetId = extractRuleSetId(ruleSet);

        JSONObject newRuleSet = new JSONObject(Map.of("id", ruleSetId, "type", "RuleSet"));
        logger.info("new RuleSet: {}", newRuleSet);

        JSONObject applications = getApplications();
        patchApplications(applications, newRuleSet, dryRun, limitCount);

        logger.info("=== count of patched applications: {} ===", patchCount);
    }

    private JSONObject getRuleSet() throws Exception {
        String getUrl = properties.get(ConfigProperties.PA_RULESETS_PATH)+"?filter="+properties.get(ConfigProperties.PA_RULESETS_FILTER);
        String ruleSet = paRestClient.getConfigJSON(getUrl);
        return new JSONObject(ruleSet);
    }
    private JSONObject getApplications() throws Exception {
        String apps = paRestClient.getConfigJSON((String)properties.get(ConfigProperties.PA_APPLICATIONS_PATH));
        return new JSONObject(apps);
    }

    private String extractRuleSetId(JSONObject ruleSet) {
        logger.debug("rule set: {}", ruleSet);
        return stream(ruleSet.getJSONArray("items"))
                .map(o->((JSONObject)o).get("id"))
                .findFirst().get().toString();
    }

    private void patchApplications(JSONObject applications, JSONObject newRuleSet, boolean dryRun, long limitCount) {
        logger.debug("applications: {}", applications.toString(4));

        long allCount = stream(applications.getJSONArray("items")).count();
        List<Object> filteredList = stream(applications.getJSONArray("items"))
                .filter(o->((JSONObject)o).getString("name")
                        .matches((String) properties.get(ConfigProperties.PA_APPLICATIONS_REGEXP)))
                .peek(o->logger.info("app to patch name: {}", ((JSONObject)o).getString("name")))
                .collect(Collectors.toList());

        long toPatchCount = filteredList.size();

        logger.info("all applications count: {}", allCount);
        logger.info("to patch applications count: {}", toPatchCount);

        filteredList.stream().limit(limitCount).forEach(a -> patchApplication(a, newRuleSet, dryRun));
    }

    private void patchApplication(Object application, JSONObject newRuleSet, boolean dryRun) {
        JSONObject app = (JSONObject) application;

        logger.info("now patching application: {}", app.getString("name"));
        String appId = String.valueOf(app.getInt("id"));

        logger.debug("application before patch: {}", app.toString(4));

        JSONObject policy = app.getJSONObject("policy");
        JSONArray ruleSet = null;
        if(app.getString("applicationType").equalsIgnoreCase("API")) {
            ruleSet = policy.getJSONArray("API");
        } else if(app.getString("applicationType").equalsIgnoreCase("Web")) {
            ruleSet = policy.getJSONArray("Web");
        }

        if(Objects.nonNull(ruleSet)) {
            boolean ruleSetExists = ruleSetExists(ruleSet, newRuleSet);
            if(ruleSetExists) {
                logger.warn("\tnew ruleSet exists");
                logger.warn("\tstop patching");
            }
            else {
                ruleSet.put(newRuleSet);
                logger.debug("application with patch: {}", app.toString(4));

                if(dryRun) {
                    logger.warn("\tdryRun active won't patch the app");
                }
                else {
                    String putUrl = properties.get(ConfigProperties.PA_APPLICATIONS_PATH) + "/" + appId;
                    String appPatched = paRestClient.putConfigJSON(putUrl, app.toString());

                    try {
                        String name = new JSONObject(appPatched).getString("name");

                        logger.info("\tapplication has been successfully patched");

                        logger.debug("patched application: {}",
                                new JSONObject(appPatched).toString(4));

                        patchCount++;
                    }
                    catch (Exception ex) {
                        logger.error("application patch error: {}",
                                new JSONObject(appPatched).toString(4));
                    }
                }
            }
        }
    }

    private boolean ruleSetExists(JSONArray ruleSet, JSONObject newRuleSet) {
        for(int i=0;i<ruleSet.length();i++) {
            JSONObject obj = ruleSet.getJSONObject(i);
            if((obj.getInt("id") == newRuleSet.getInt("id"))
                    && obj.getString("type").equalsIgnoreCase(newRuleSet.getString("type"))) {
                return true;
            }
        }
        return false;
    }
}
