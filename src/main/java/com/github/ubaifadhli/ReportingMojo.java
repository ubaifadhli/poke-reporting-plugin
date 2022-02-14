package com.github.ubaifadhli;

import com.github.ubaifadhli.constant.FilePath;
import com.github.ubaifadhli.constant.LogMessage;
import com.github.ubaifadhli.util.CucumberDataHandler;
import com.github.ubaifadhli.util.DatetimeHelper;
import com.github.ubaifadhli.util.TemplateEngine;
import lombok.extern.slf4j.Slf4j;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.Duration;

@Slf4j
@Mojo(name = "reporting", defaultPhase = LifecyclePhase.POST_INTEGRATION_TEST)
public class ReportingMojo extends AbstractMojo {

    @Parameter(property = "cucumberJSONDirectory", defaultValue = FilePath.DEFAULT_CUCUMBER_JSON_DIRECTORY)
    private String cucumberJSONDirectory;

    @Parameter(property = "reportDestinationDirectory", defaultValue = FilePath.DEFAULT_REPORT_DESTINATION_DIRECTORY)
    private String reportDestinationDirectory;

    @Override
    public void execute() throws MojoExecutionException {
        long startTime = System.currentTimeMillis();

        CucumberDataHandler cucumberDataHandler = new CucumberDataHandler(cucumberJSONDirectory);
        log.info(LogMessage.CUCUMBER_JSON_LOADED);

        try {
            cucumberDataHandler.createAttachmentFiles(reportDestinationDirectory);
        } catch (IOException e) {
            throw new MojoExecutionException("Failed to generate attachments.", e);
        }
        log.info(LogMessage.REPORT_ATTACHMENTS_CREATED);

        // Generate report for Dashboard
        try {
            TemplateEngine.generateWebpage(
                    cucumberDataHandler,
                    FilePath.DASHBOARD_TEMPLATE_FILE_NAME,
                    reportDestinationDirectory + FilePath.DASHBOARD_TEMPLATE_FILE_NAME
            );
        } catch (IOException e) {
            throw new MojoExecutionException("Failed to generate dashboard webpage.", e);
        }

        // Generate report for Features
        for (CucumberDataHandler.Feature feature : cucumberDataHandler.getFeatures()) {
            try {
                TemplateEngine.generateWebpage(
                        feature,
                        FilePath.FEATURES_TEMPLATE_FILE_NAME,
                        reportDestinationDirectory + feature.getID() + ".html"
                );
            } catch (IOException e) {
                throw new MojoExecutionException("Failed to generate webpage for feature : ." + feature.getName(), e);
            }
        }

        // Generate report for Email
        try {
            TemplateEngine.generateWebpage(
                    cucumberDataHandler,
                    FilePath.EMAIL_TEMPLATE_FILE_NAME,
                    reportDestinationDirectory + FilePath.EMAIL_TEMPLATE_FILE_NAME
            );
        } catch (IOException e) {
            throw new MojoExecutionException("Failed to generate email webpage.", e);
        }

        long endTime = System.currentTimeMillis();
        Duration elapsedTime = DatetimeHelper.between(new Timestamp(startTime), new Timestamp(endTime));

        log.info(LogMessage.REPORT_SUCCESSFULLY_GENERATED, DatetimeHelper.toMinutesSeconds(elapsedTime));
    }
}