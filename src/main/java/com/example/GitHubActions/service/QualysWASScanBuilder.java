package com.example.GitHubActions.service;

import com.example.GitHubActions.WASAuth.WASAuth;
import com.example.GitHubActions.WASClient.WASClient;
import com.example.GitHubActions.util.Helper;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class QualysWASScanBuilder {
    private static final Logger logger = LoggerFactory.getLogger(QualysWASScanBuilder.class);
    private final static int PROXY_PORT = 80;
    private String platform;
    private String apiServer;
    private String qualysUsername;
    private String qualysPasssword;
    private boolean useProxy = false;
    private String proxyServer;
    private int proxyPort = PROXY_PORT;
    private String proxyUsername;
    private String proxyPassword;
    private String webAppId;
    private String scanName;
    private String scanType;
    private String authRecord;
    private String authRecordId;
    private String optionProfile;
    private String optionProfileId;
    private String cancelOptions;
    private String cancelHours;
    private boolean isFailOnSevereVulns;
    private int severity1Limit;
    private int severity2Limit;
    private int severity3Limit;
    private int severity4Limit;
    private int severity5Limit;
    private boolean isSev1Vulns = false;
    private boolean isSev2Vulns = false;
    private boolean isSev3Vulns = false;
    private boolean isSev4Vulns = false;
    private boolean isSev5Vulns = false;
    private boolean isFailOnQidFound;
    private String qidList;
    private boolean isFailOnScanError = true;
    private String pollingInterval;
    private String vulnsTimeout;

    private final static int DEFAULT_POLLING_INTERVAL_FOR_VULNS = 5; //5 minutes
    private final static int DEFAULT_TIMEOUT_FOR_VULNS = 60 * 24;

    @Autowired
    private final Environment environment;

    public QualysWASScanBuilder(Environment environment) {
        this.environment = environment;
        this.platform = environment.getProperty("PLATFORM", "");
        this.apiServer = environment.getProperty("API_SERVER", "");
        this.qualysUsername = environment.getProperty("QUALYS_USERNAME", "");
        this.qualysPasssword = environment.getProperty("QUALYS_PASSWORD", "");
        this.useProxy = Boolean.parseBoolean(environment.getProperty("USE_PROXY", "false"));
        this.proxyServer = environment.getProperty("PROXY_SERVER", "");
        this.proxyPort = Integer.parseInt(environment.getProperty("PROXY_PORT", "0"));
        this.proxyUsername = environment.getProperty("PROXY_USERNAME", "");
        this.proxyPassword = environment.getProperty("PROXY_PASSWORD", "");
        this.webAppId = environment.getProperty("WEBAPP_ID", "");
        this.scanName = environment.getProperty("SCAN_NAME", "");
        this.scanType = environment.getProperty("SCAN_TYPE", "");
        this.authRecord = environment.getProperty("AUTH_RECORD", "");
        this.authRecordId = environment.getProperty("AUTH_RECORD_ID", "");
        this.optionProfile = environment.getProperty("OPTION_PROFILE", "");
        this.optionProfileId = environment.getProperty("OPTION_PROFILE_ID", "");
        this.cancelOptions = environment.getProperty("CANCEL_OPTION", "");
        this.cancelHours = environment.getProperty("CANCEL_HOURS", "");
        this.severity1Limit = Integer.parseInt(environment.getProperty("SEVERITY_ONE_LIMIT", "0"));
        this.severity2Limit = Integer.parseInt(environment.getProperty("SEVERITY_TWO_LIMIT", "0"));
        this.severity3Limit = Integer.parseInt(environment.getProperty("SEVERITY_THREE_LIMIT", "0"));
        this.severity4Limit = Integer.parseInt(environment.getProperty("SEVERITY_FOUR_LIMIT", ""));
        this.severity5Limit = Integer.parseInt(environment.getProperty("SEVERITY_FIVE_LIMIT", "0"));
        this.isSev1Vulns = Boolean.parseBoolean(environment.getProperty("IS_SEVERITY_ONE", "false"));
        this.isSev2Vulns = Boolean.parseBoolean(environment.getProperty("IS_SEVERITY_TWO", "false"));
        this.isSev3Vulns = Boolean.parseBoolean(environment.getProperty("IS_SEVERITY_THREE", "false"));
        this.isSev4Vulns = Boolean.parseBoolean(environment.getProperty("IS_SEVERITY_FOUR", "false"));
        this.isSev5Vulns = Boolean.parseBoolean(environment.getProperty("IS_SEVERITY_FIVE", "false"));
        this.isFailOnQidFound = Boolean.parseBoolean(environment.getProperty("IS_FAIL_ON_QID_FOUND", "false"));
        this.qidList = environment.getProperty("QID_LIST", "");
        this.isFailOnScanError = Boolean.parseBoolean(environment.getProperty("FAIL_ON_SCAN_ERROR", "false"));
    }

    public JsonObject getCriteriaAsJsonObject() {
        JsonObject obj = new JsonObject();

        JsonObject failConditionsObj = new JsonObject();
        Gson gson = new Gson();
        if (isFailOnQidFound) {
            if (this.qidList == null || this.qidList.isEmpty()) {
                JsonElement empty = new JsonArray();
                failConditionsObj.add("qids", empty);
            } else {
                List<String> qids = Arrays.asList(this.qidList.split(","));
                qids.replaceAll(String::trim);
                JsonElement element = gson.toJsonTree(qids, new TypeToken<List<String>>() {
                }.getType());
                failConditionsObj.add("qids", element);
            }
        }
        if (isFailOnSevereVulns) {
            JsonObject severities = new JsonObject();
            if (this.isSev5Vulns) severities.addProperty("5", this.severity5Limit);
            if (this.isSev4Vulns) severities.addProperty("4", this.severity4Limit);
            if (this.isSev3Vulns) severities.addProperty("3", this.severity3Limit);
            if (this.isSev2Vulns) severities.addProperty("2", this.severity2Limit);
            if (this.isSev1Vulns) severities.addProperty("1", this.severity1Limit);
            failConditionsObj.add("severities", severities);
        }
        if (isFailOnScanError) {
            failConditionsObj.addProperty("failOnScanError", true);
        }
        obj.add("failConditions", failConditionsObj);

        logger.info("Criteria Object to common library: " + obj);
        return obj;
    }

    private int setTimeoutInMinutes(String timeoutType, int defaultTimeoutInMins, String timeout) {
        if (!(timeout == null || timeout.isEmpty())) {
            try {
                //if timeout is a regex of form 2*60*60 seconds, calculate the timeout in seconds
                String[] numbers = timeout.split("\\*");
                int timeoutInMins = 1;
                for (int i = 0; i < numbers.length; ++i) {
                    timeoutInMins *= Long.parseLong(numbers[i]);
                }
                return timeoutInMins;
            } catch (Exception e) {
                logger.info("Invalid " + timeoutType + " time value. Cannot parse -" + e.getMessage());
                logger.info("Using default period of " + (timeoutType.equals("vulnsTimeout") ? "60*24" : defaultTimeoutInMins) + " minutes for " + timeoutType + ".");
            }
        }
        return defaultTimeoutInMins;
    }

    public void launchWebApplicationScan() {
        Map<String, String> platformObj = Helper.platformsList.get(platform);
        String portalUrl = apiServer;

        if (!platform.equalsIgnoreCase("pcp")) {
            setApiServer(platformObj.get("url"));
            logger.info("Qualys API Server URL: " + apiServer);
            portalUrl = platformObj.get("portal");
        }

        logger.info("Using Qualys Platform: " + platform + ". API Server: " + apiServer);

        try {
            WASAuth auth = new WASAuth();
            auth.setWasCredentials(apiServer, qualysUsername, qualysPasssword);

            if (useProxy) {
                auth.setProxyCredentials(proxyServer, proxyPort, proxyUsername, proxyPassword);
            }

            WASClient client = new WASClient(auth, System.out);
            try {
                logger.info("Testing connection with Qualys API Server...");
                client.testConnection();
                logger.info("Test connection successful.");
            } catch (Exception ex) {
                logger.error("Test connection failed. Reason: " + ex.getMessage());
            }

            if (webAppId == null || webAppId.isEmpty()) {
                logger.error("Web app id not found.");
                return;
            }

            boolean isFailConditionConfigured = false;
            this.isFailOnSevereVulns = this.isSev1Vulns || this.isSev2Vulns || this.isSev3Vulns || this.isSev4Vulns || this.isSev5Vulns;
            if (isFailOnQidFound || isFailOnSevereVulns || isFailOnScanError) {
                isFailConditionConfigured = true;
            }

            QualysWASScanService service = QualysWASScanService.builder()
                    .webAppId(webAppId)
                    .scanName(scanName)
                    .scanType(scanType)
                    .authRecord(authRecord)
                    .authRecordId(authRecordId)
                    .optionProfile(optionProfile)
                    .optionProfileId(optionProfileId)
                    .cancelOptions(cancelOptions)
                    .cancelHours(cancelHours)
                    .isFailConditionsConfigured(isFailConditionConfigured)
                    .pollingIntervalForVulns(Helper.setTimeoutInMinutes("pollingInterval", DEFAULT_POLLING_INTERVAL_FOR_VULNS, pollingInterval))
                    .vulnsTimeout(Helper.setTimeoutInMinutes("vulnsTimeout", DEFAULT_TIMEOUT_FOR_VULNS, vulnsTimeout))
                    .criteriaObject(getCriteriaAsJsonObject())
                    .apiServer(apiServer)
                    .apiUser(qualysUsername)
                    .apiPass(qualysPasssword)
                    .useProxy(useProxy)
                    .proxyServer(proxyServer)
                    .proxyPort(proxyPort)
                    .proxyUsername(proxyUsername)
                    .proxyPassword(proxyPassword)
                    .portalUrl(portalUrl)
                    .failOnScanError(isFailOnScanError)
                    .build();

            logger.info("Qualys task - Started Launching web app scanning with WAS");
            String scanId = service.launchScan();
            logger.info("Scan successfully launched with scan id: " + scanId);
        } catch (Exception ex) {
            logger.error("Something went wrong. Reason: " + ex.getMessage());
        }
    }
}
