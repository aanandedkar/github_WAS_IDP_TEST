package com.example.GitHubActions.util;

import com.example.GitHubActions.WASAuth.WASAuth;
import com.example.GitHubActions.WASClient.WASClient;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.example.GitHubActions.WASClient.QualysWASResponse;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class Helper {
    private static final Logger logger = LoggerFactory.getLogger(Helper.class);
    private final static int TIMEOUT = (60 * 5) + 50; //5Hrs 50Minuts
    private final static int INTERVAL = 5; //5 minuts

    public Helper() {

    }

    public static final Map<String, Map<String, String>> platformsList;

    static {
        Map<String, Map<String, String>> aList = new LinkedHashMap<String, Map<String, String>>();

        Map<String, String> platform1 = new HashMap<String, String>();
        platform1.put("name", "US Platform 1");
        platform1.put("code", "US_PLATFORM_1");
        platform1.put("url", "https://qualysapi.qualys.com");
        platform1.put("portal", "https://qualysguard.qualys.com");
        aList.put("US_PLATFORM_1", platform1);

        Map<String, String> platform2 = new HashMap<String, String>();
        platform2.put("name", "US Platform 2");
        platform2.put("code", "US_PLATFORM_2");
        platform2.put("url", "https://qualysapi.qg2.apps.qualys.com");
        platform2.put("portal", "https://qualysguard.qg2.apps.qualys.com");
        aList.put("US_PLATFORM_2", platform2);

        Map<String, String> platform3 = new HashMap<String, String>();
        platform3.put("name", "US Platform 3");
        platform3.put("code", "US_PLATFORM_3");
        platform3.put("url", "https://qualysapi.qg3.apps.qualys.com");
        platform3.put("portal", "https://qualysguard.qg3.apps.qualys.com");
        aList.put("US_PLATFORM_3", platform3);

        Map<String, String> platform4 = new HashMap<String, String>();
        platform4.put("name", "US Platform 4");
        platform4.put("code", "US_PLATFORM_4");
        platform4.put("url", "https://qualysapi.qg4.apps.qualys.com");
        platform4.put("portal", "https://qualysguard.qg4.apps.qualys.com");
        aList.put("US_PLATFORM_4", platform4);

        Map<String, String> platform5 = new HashMap<String, String>();
        platform5.put("name", "EU Platform 1");
        platform5.put("code", "EU_PLATFORM_1");
        platform5.put("url", "https://qualysapi.qualys.eu");
        platform5.put("portal", "https://qualysguard.qualys.eu");
        aList.put("EU_PLATFORM_1", platform5);

        Map<String, String> platform6 = new HashMap<String, String>();
        platform6.put("name", "EU Platform 2");
        platform6.put("code", "EU_PLATFORM_2");
        platform6.put("url", "https://qualysapi.qg2.apps.qualys.eu");
        platform6.put("portal", "https://qualysguard.qg2.apps.qualys.eu");
        aList.put("EU_PLATFORM_2", platform6);

        Map<String, String> platform7 = new HashMap<String, String>();
        platform7.put("name", "INDIA Platform");
        platform7.put("code", "INDIA_PLATFORM");
        platform7.put("url", "https://qualysapi.qg1.apps.qualys.in");
        platform7.put("portal", "https://qualysguard.qg1.apps.qualys.in");
        aList.put("INDIA_PLATFORM", platform7);

        Map<String, String> platform8 = new HashMap<String, String>();
        platform8.put("name", "CANADA Platform");
        platform8.put("code", "CANADA_PLATFORM");
        platform8.put("url", "https://qualysapi.qg1.apps.qualys.ca");
        platform8.put("portal", "https://qualysguard.qg1.apps.qualys.ca");
        aList.put("CANADA_PLATFORM", platform8);

        Map<String, String> platform9 = new HashMap<String, String>();
        platform9.put("name", "Private Cloud Platform");
        platform9.put("code", "PCP");
        platform9.put("url", "");
        aList.put("PCP", platform9);

        platformsList = Collections.unmodifiableMap(aList);
    }

    private WASClient getWASClient(String server, String username, String password) {
        WASAuth auth = new WASAuth();
        auth.setWasCredentials(server, username, password);
        return new WASClient(auth, System.out);
    }

    public void testConnection(String server, String username, String password) throws Exception {
        WASClient client = getWASClient(server, username, password);
        client.testConnection();
        logger.info("Connection Test Successful!");
    }

    public String getStatus(String server, String username, String password, String scanId) {
        long startTime = System.currentTimeMillis();
        long timeoutInMillis = TimeUnit.MINUTES.toMillis(TIMEOUT);
        long intervalInMillis = TimeUnit.MINUTES.toMillis(INTERVAL);
        String status = null;

        try {
            WASClient client = getWASClient(server, username, password);
            while ((status = client.getScanFinishedStatus(scanId)) == null) {
                long endTime = System.currentTimeMillis();
                if ((endTime - startTime) > timeoutInMillis) {
                    logger.info(new Timestamp(System.currentTimeMillis()) + " Failed to get scan result; timeout of " + TIMEOUT + " minutes reached.");
                    throw new Exception("Timeout reached.");
                } else {
                    try {
                        logger.info(new Timestamp(System.currentTimeMillis()) + " Waiting for " + INTERVAL + " minute(s) before making next attempt for scanResult of scanId:" + scanId + "...");
                        Thread.sleep(intervalInMillis);
                    } catch (Exception ex) {
                        logger.info(ex.getMessage());
                    }
                }
            }
        } catch (Exception ex) {
            logger.info("Exception: " + ex.getMessage());
        }

        return status;
    }

    public JsonObject getScanResult(String server, String username, String password, String scanId) {
        WASClient client = getWASClient(server, username, password);
        QualysWASResponse qualysWASResponse = client.getScanResult(scanId);
        JsonObject scanResult = qualysWASResponse.response;
        return scanResult;
    }

    public static int setTimeoutInMinutes(String timeoutType, int defaultTimeoutInMins, String timeout) {
        if (!(timeout == null || timeout.isEmpty()) ){
            try {
                //if timeout is a regex of form 2*60*60 seconds, calculate the timeout in seconds
                String[] numbers = timeout.split("\\*");
                int timeoutInMins = 1;
                for (int i = 0; i<numbers.length ; ++i) {
                    timeoutInMins *= Long.parseLong(numbers[i]);
                }
                return timeoutInMins;
            } catch(Exception e) {
                logger.error("Invalid " + timeoutType + " time value. Cannot parse -"+e.getMessage());
                logger.error("Using default period of " + (timeoutType.equals("vulnsTimeout") ? "60*24" : defaultTimeoutInMins) + " minutes for " + timeoutType + ".");
            }
        }
        return defaultTimeoutInMins;
    }
}
