package com.github.ubaifadhli;

import com.github.ubaifadhli.data.CucumberEmbedding;
import com.github.ubaifadhli.data.CucumberHook;
import com.github.ubaifadhli.data.CucumberScenario;
import com.github.ubaifadhli.data.CucumberStep;
import lombok.extern.slf4j.Slf4j;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import com.github.ubaifadhli.util.*;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Mojo(name = "reporting", defaultPhase = LifecyclePhase.POST_INTEGRATION_TEST)
public class ReportingMojo extends AbstractMojo {
    @Inject
    CucumberDataHandler cucumberDataHandler;

    @Inject
    FileHelper fileHelper;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        long startTime = System.currentTimeMillis();

        try {
            cucumberDataHandler.loadJSON(FilePath.DEFAULT_CUCUMBER_PATH + FilePath.CUCUMBER_FILE_NAME);
            log.info("Cucumber JSON file has been loaded.");

        } catch (IOException e) {
            throw new MojoExecutionException("Cucumber JSON could not be loaded. Please make sure that the JSON exist in " + FilePath.DEFAULT_CUCUMBER_PATH, e);
        }

        cucumberDataHandler.calculateAdditionalData();

        Timestamp testStartTimestamp = cucumberDataHandler.getTestStartTime();
        Timestamp testEndTimestamp = cucumberDataHandler.getTestEndTime();

        int totalScenario = cucumberDataHandler.getTotalScenario();
        int failedScenarioCount = cucumberDataHandler.getFailedScenarioCount();

        Duration testDuration = DatetimeHelper.between(testStartTimestamp, testEndTimestamp);

        String testStartDate = DatetimeHelper.toFormattedDate(testStartTimestamp);
        String testStartTime = DatetimeHelper.toFormattedTime(testStartTimestamp);
        String testDurationText = DatetimeHelper.toMinutesSeconds(testDuration);

        List<CucumberScenario> scenarios = cucumberDataHandler.getScenarios();

        // Create PNG screenshot files from the MIME
        for (CucumberScenario scenario : scenarios) {
            for (CucumberStep step : scenario.getSteps()) {
                CucumberHook after = step.getAfter().get(0);

                CucumberEmbedding embedding;

                if (after.getEmbeddings() != null) {
                    embedding = after.getEmbeddings().get(0);

                    switch (embedding.getMimeType()) {
                        case "image/png":
                            try {
                                String imageName = fileHelper.saveImage(embedding.getData());
                                embedding.setFileName(imageName);

                            } catch (IOException e) {
                                log.error("Failed to create image file.");
                            }
                            break;

                        case "text/plain":
                            try {
                                String dataText = fileHelper.limitStringLength(embedding.getData());
                                String textFileName = fileHelper.saveText(embedding.getData());

                                embedding.setDataText(dataText);
                                embedding.setFileName(textFileName);
                            } catch (IOException e) {
                                log.error("Failed to create text file.");
                                e.printStackTrace();
                            }
                            break;
                    }
                }
            }
        }
        log.info("Attachments have been created.");

        Map<String, Object> context = new HashMap<>();
        context.put("testDate", testStartDate);
        context.put("totalScenario", totalScenario);
        context.put("passedScenarioCount", totalScenario - failedScenarioCount);
        context.put("failedScenarioCount", failedScenarioCount);
        context.put("testStartTime", testStartTime);
        context.put("testDuration", testDurationText);
        context.put("scenarios", scenarios);

        try {
            String output = TemplateEngine.generateReport(context);

            Path path = Paths.get(FilePath.DEFAULT_REPORT_PATH + FilePath.DEFAULT_REPORT_FILE_NAME);

            path.getParent().toFile().mkdirs();

            Files.write(path, output.getBytes(StandardCharsets.UTF_8));

            long endTime = System.currentTimeMillis();

            Duration elapsedTime = DatetimeHelper.between(new Timestamp(startTime), new Timestamp(endTime));

            log.info("Report has been successfully generated in {}.", DatetimeHelper.toMinutesSeconds(elapsedTime));

            // TODO Add log linking to the index.html path

        } catch (IOException e) {
            throw new MojoExecutionException("Failed to generate the report.", e);
        }
    }
}
