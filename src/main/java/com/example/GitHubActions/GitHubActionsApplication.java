package com.example.GitHubActions;

import com.example.GitHubActions.service.QualysWASScanBuilder;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import com.example.GitHubActions.util.Helper;
import org.springframework.core.env.Environment;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@SpringBootApplication
public class GitHubActionsApplication {
    private static final Logger logger = LoggerFactory.getLogger(GitHubActionsApplication.class);
    private static final Helper helper = new Helper();

    private static final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);

    public static void main(String[] args) throws Exception {

        ConfigurableApplicationContext ctx = SpringApplication.run(GitHubActionsApplication.class, args);
        Environment environment = ctx.getEnvironment();
        String username = System.getenv("USERNAME");
        String password = System.getenv("PASSWORD");
        String server = System.getenv("SERVER");
        boolean useProxy = Boolean.parseBoolean(System.getenv("USE_PROXY"));
        String scanId = System.getenv("SCAN_ID");

        logger.info("[Username: " + username + "]");
        logger.info("[Password: " + password + "]");
        logger.info("[Server: " + server + "]");
        logger.info("[Use-proxy: " + useProxy + "]");
        logger.info("[Scan-id: " + scanId + "]");

//        testConnection(server, username, password);
        helper.testConnection("https://qualysapi.qualys.com", "quays_pg19", "o34kLasNpg");
//        String status = helper.getStatus("https://qualysapi.qualys.com", "quays_pg19", "o34kLasNpg", "38501738");
//
//        if (status.equalsIgnoreCase("finished")) {
//            JsonObject scanResult = helper.getScanResult("https://qualysapi.qualys.com", "quays_pg19", "o34kLasNpg", "38501738");
//            logger.info(scanResult.toString());
//        } else if (status.equalsIgnoreCase("canceled")) {
//            throw new Exception("The scan(ScanId: "+ scanId + ") has been canceled.");
//        }else if (status.equalsIgnoreCase("error")) {
//            throw new Exception("The scan(ScanId: "+scanId+") is not completed due to an error.");
//        }

        QualysWASScanBuilder builder = new QualysWASScanBuilder(environment);

//        logger.info(status);
        ctx.getBean(GitHubActionsApplication.class);
        ctx.close();
    }
}
