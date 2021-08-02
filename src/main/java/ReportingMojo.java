import data.CucumberEmbedding;
import data.CucumberHook;
import data.CucumberScenario;
import data.CucumberStep;
import lombok.extern.slf4j.Slf4j;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import util.*;

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
    private final int CHARACTER_LIMIT = 256;

    @Inject
    CucumberDataHandler cucumberDataHandler;

    @Inject
    ImageHelper imageHelper;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            cucumberDataHandler.loadJSON(FilePath.DEFAULT_CUCUMBER_PATH + FilePath.CUCUMBER_FILE_NAME);
            log.info("Cucumber JSON file has been loaded.");

        } catch (IOException e) {
            log.error("Cucumber JSON could not be loaded. Please make sure that the JSON exist in " + FilePath.DEFAULT_CUCUMBER_PATH);
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
                                String imageName = imageHelper.saveImage(embedding.getData());
                                embedding.setFileName(imageName);

                            } catch (IOException e) {
                                log.error("Failed to create image file.");
                            }
                            break;

                        case "text/plain":
                            // TODO make it expandable
                            String dataText = new String(embedding.getData());

                            if (dataText.length() > CHARACTER_LIMIT) {
                                int remainingCharacter = dataText.length() - CHARACTER_LIMIT;
                                dataText = dataText.substring(0, CHARACTER_LIMIT) + "\\\r\\\n... and " + remainingCharacter + " more characters.";
                            }

                            embedding.setDataText(dataText);
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

        String output;

        try {
            output = TemplateEngine.generateReport(context);

            Path path = Paths.get(FilePath.DEFAULT_REPORT_PATH + FilePath.DEFAULT_REPORT_FILE_NAME);
            Files.write(path, output.getBytes(StandardCharsets.UTF_8));

            log.info("Report has been successfully generated.");
        } catch (IOException e) {
            log.error("Failed to generate the report.");
        }
    }
}
