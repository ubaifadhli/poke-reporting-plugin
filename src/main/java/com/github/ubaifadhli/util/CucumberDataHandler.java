package com.github.ubaifadhli.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.ubaifadhli.data.CucumberScenario;
import com.github.ubaifadhli.data.CucumberFeature;
import com.github.ubaifadhli.data.CucumberStep;

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

    public void calculateAdditionalData() {
        calculateScenarioEndTimestamp();
        calculateScenarioDuration();
        determineScenarioStatus();
    }

    public Timestamp getTestStartTime() {
        return cucumberFeature.getScenarios().get(0).getStartTimestamp();
    }

    public List<CucumberScenario> getScenarios() {
        return cucumberFeature.getScenarios();
    }

    public Timestamp getTestEndTime() {
        return cucumberFeature.getScenarios()
                .stream()
                .map(CucumberScenario::getEndTimestamp)
                .max(Timestamp::compareTo)
                .get();
    }

    public int getTotalScenario() {
        return cucumberFeature.getScenarios().size();
    }

    public int getFailedScenarioCount() {
        return (int) cucumberFeature.getScenarios()
                .stream()
                .filter(cucumberScenario -> !cucumberScenario.isPassed())
                .count();
    }

    private void calculateScenarioDuration() {
        cucumberFeature.getScenarios().forEach(cucumberScenario -> {
            Duration scenarioDuration = DatetimeHelper.between(cucumberScenario.getStartTimestamp(), cucumberScenario.getEndTimestamp());
            cucumberScenario.setDuration(scenarioDuration.getSeconds());
        });
    }

    private void calculateScenarioEndTimestamp() {
        for (CucumberScenario scenario : cucumberFeature.getScenarios()) {
            Timestamp timestamp = scenario.getStartTimestamp();

            Long scenarioDuration = 0L;

            if (scenario.hasBefore())
                scenarioDuration += scenario.getBefore().get(0).getResult().getDuration();

            if (scenario.hasAfter())
                scenarioDuration += scenario.getAfter().get(0).getResult().getDuration();

            for (CucumberStep step : scenario.getSteps()) {
                if (step.getResult().getDuration() != null)
                    scenarioDuration += step.getResult().getDuration();

                if (step.hasBefore())
                    scenarioDuration += step.getBefore().get(0).getResult().getDuration();

                if (step.hasAfter())
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

    private void determineScenarioStatus() {
        cucumberFeature.getScenarios().forEach(cucumberScenario -> {
            List<CucumberStep> steps = cucumberScenario.getSteps();

            CucumberStep lastStep = steps.get(steps.size() - 1);

            cucumberScenario.setPassed(lastStep.getResult().getStatus().equals("passed"));
        });
    }
}
