package util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import data.CucumberScenario;
import data.CucumberFeature;
import data.CucumberStep;

import javax.inject.Singleton;
import java.io.IOException;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.time.Duration;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

@Singleton
public class CucumberDataHandler {
    private CucumberFeature cucumberFeature;

    public void loadJSON(String jsonPath) throws IOException {
        ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

        List<CucumberFeature> cucumberFeatures = Arrays.asList(mapper.readValue(Paths.get(jsonPath).toFile(), CucumberFeature[].class));
        this.cucumberFeature = cucumberFeatures.get(0);
    }

    public Timestamp getTestStartTime() {
        return cucumberFeature.getScenarios().get(0).getStartTimestamp();
    }

    public List<CucumberScenario> getScenarios() {
        return cucumberFeature.getScenarios();
    }

    public Timestamp getTestEndTime() {
        Timestamp endTestTimestamp = cucumberFeature.getScenarios().get(0).getEndTimestamp();

        for (CucumberScenario scenario : cucumberFeature.getScenarios()) {
            if (endTestTimestamp.before(scenario.getEndTimestamp()))
                endTestTimestamp = scenario.getEndTimestamp();
        }

        return endTestTimestamp;
    }

    public int getTotalScenario() {
        return cucumberFeature.getScenarios().size();
    }

    public int getFailedScenarioCount() {
        int failedScenarioCount = 0;
        for (CucumberScenario scenario : cucumberFeature.getScenarios())
            if (!scenario.isPassed())
                failedScenarioCount++;

        return failedScenarioCount;
    }

    public void calculateAdditionalData() {
        calculateScenarioEndTimestamp();
        calculateScenarioDuration();
        determineScenarioStatus();
    }

    public void calculateScenarioDuration() {
        for (CucumberScenario scenario : cucumberFeature.getScenarios()) {
            Duration scenarioDuration = DatetimeHelper.between(scenario.getStartTimestamp(), scenario.getEndTimestamp());
            scenario.setDuration(scenarioDuration.getSeconds());
        }
    }

    public void calculateScenarioEndTimestamp() {
        for (CucumberScenario scenario : cucumberFeature.getScenarios()) {
            Timestamp timestamp = scenario.getStartTimestamp();

            Long scenarioDuration = 0L;

            if (scenario.getBefore().get(0).getResult().getDuration() != null)
                scenarioDuration += scenario.getBefore().get(0).getResult().getDuration();

            if (scenario.getAfter().get(0).getResult().getDuration() != null)
                scenarioDuration += scenario.getAfter().get(0).getResult().getDuration();

            for (CucumberStep step : scenario.getSteps()) {
                if (step.getResult().getDuration() != null)
                    scenarioDuration += step.getResult().getDuration();

                if (step.getBefore().get(0).getResult().getDuration() != null)
                    scenarioDuration += step.getBefore().get(0).getResult().getDuration();

                if (step.getAfter().get(0).getResult().getDuration() != null)
                    scenarioDuration += step.getAfter().get(0).getResult().getDuration();
            }

            Duration duration = Duration.ofNanos(scenarioDuration);

            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(timestamp.getTime());

            calendar.add(Calendar.SECOND, (int) duration.getSeconds());

            Timestamp endTime = new Timestamp(calendar.getTime().getTime());

            scenario.setEndTimestamp(endTime);
        }
    }

    public void determineScenarioStatus() {
        for (CucumberScenario scenario : cucumberFeature.getScenarios()) {
            List<CucumberStep> steps = scenario.getSteps();

            CucumberStep lastStep = steps.get(steps.size() - 1);

            scenario.setPassed(lastStep.getResult().getStatus().equals("passed"));
        }
    }
}
