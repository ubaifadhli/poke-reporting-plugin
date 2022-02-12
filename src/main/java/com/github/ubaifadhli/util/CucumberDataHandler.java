package com.github.ubaifadhli.util;

import com.github.ubaifadhli.constant.CucumberResultStatus;
import com.github.ubaifadhli.constant.FilePath;
import com.github.ubaifadhli.constant.SupportedMIMEType;
import com.github.ubaifadhli.data.*;
import org.apache.maven.plugin.MojoExecutionException;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

public class CucumberDataHandler {
    private List<CucumberFeature> cucumberFeatures;

    public CucumberDataHandler(String cucumberJSONDirectory) throws MojoExecutionException {
        List<CucumberFeature> duplicatedCucumberFeatures = JSONUtil.loadMultipleJSON(cucumberJSONDirectory, CucumberFeature[].class);
        cucumberFeatures = mergeIdenticalFeatures(duplicatedCucumberFeatures);
    }

    private List<CucumberFeature> mergeIdenticalFeatures(List<CucumberFeature> cucumberFeatures) {
        List<String> distinctFeaturesURI = cucumberFeatures.stream()
                .map(CucumberFeature::getUri)
                .distinct()
                .collect(Collectors.toList());

        List<CucumberFeature> mergedCucumberFeatures = new ArrayList<>();

        distinctFeaturesURI.forEach(
                uri -> {
                    List<CucumberFeature> features = cucumberFeatures.stream()
                            .filter(feature -> feature.getUri().equals(uri))
                            .collect(Collectors.toList());

                    int currentFeatureIndex = mergedCucumberFeatures.size();
                    mergedCucumberFeatures.add(features.get(0));

                    features.remove(0);

                    features.stream().map(CucumberFeature::getScenarios)
                            .forEach(scenarios -> {
                                mergedCucumberFeatures.get(currentFeatureIndex)
                                        .getScenarios()
                                        .addAll(scenarios);
                            });
                }
        );

        return mergedCucumberFeatures;
    }

    public List<Feature> getFeatures() {
        return cucumberFeatures.stream()
                .map(Feature::new)
                .collect(Collectors.toList());
    }

    public String getTestDate() {
        return DatetimeHelper.toFormattedDate(getTestStartTime()) + ", " + DatetimeHelper.toFormattedTime(getTestStartTime());
    }

    public int getTotalScenarios() {
        return (int) cucumberFeatures.stream()
                .map(CucumberFeature::getScenarios)
                .mapToLong(Collection::size)
                .sum();
    }

    public int getTotalPassedScenarios() {
        return getTotalScenariosBasedOnStatus(CucumberResultStatus.PASSED);
    }

    public int getTotalFailedScenarios() {
        return getTotalScenariosBasedOnStatus(CucumberResultStatus.FAILED);
    }

    public int getTotalSkippedScenarios() {
        return getTotalScenariosBasedOnStatus(CucumberResultStatus.SKIPPED);
    }

    public int getPassedPercentage() {
        return getTotalPassedScenarios() * 100 / getTotalScenarios();
    }

    public int getFailedPercentage() {
        return getTotalFailedScenarios() * 100 / getTotalScenarios();
    }

    public int getSkippedPercentage() {
        return getTotalSkippedScenarios() * 100 / getTotalScenarios();
    }

    public int getTotalScenariosBasedOnStatus(CucumberResultStatus status) {
        return getFeatures().stream()
                .map(feature -> feature.getTotalScenariosBasedOnStatus(status))
                .mapToInt(Integer::valueOf)
                .sum();
    }

    public Timestamp getTestStartTime() {
        return getFeatures().stream()
                .map(Feature::getScenarios)
                .flatMap(Collection::stream)
                .map(Scenario::getScenarioStartTime)
                .min(Timestamp::compareTo)
                .get();
    }

    public String getTestDuration() {
        Duration duration = DatetimeHelper.between(getTestStartTime(), getTestEndTime());
        return DatetimeHelper.toMinutesSeconds(duration);
    }

    private Timestamp getTestEndTime() {
        return getFeatures().stream()
                .map(Feature::getScenarios)
                .flatMap(Collection::stream)
                .map(Scenario::getScenarioEndTime)
                .max(Timestamp::compareTo)
                .get();
    }

    public List<TagOverview> getTagsOverview() {
        Map<String, Integer> tagTotalScenarioMap = new HashMap<>();
        Map<String, Integer> tagTotalPassedScenarioMap = new HashMap<>();
        Map<String, Integer> tagTotalFailedScenarioMap = new HashMap<>();
        Map<String, Integer> tagTotalSkippedScenarioMap = new HashMap<>();

        getFeatures().stream()
                .map(Feature::getScenarios)
                .flatMap(Collection::stream)
                .forEach(scenario -> {
                    scenario.getTagsName().forEach(
                            tagName -> {
                                if (!tagTotalScenarioMap.containsKey(tagName)) {
                                    tagTotalScenarioMap.put(tagName, 0);
                                    tagTotalPassedScenarioMap.put(tagName, 0);
                                    tagTotalFailedScenarioMap.put(tagName, 0);
                                    tagTotalSkippedScenarioMap.put(tagName, 0);
                                }

                                switch (scenario.getStatus()) {
                                    case PASSED:
                                        int currentTotalPassedScenario = tagTotalPassedScenarioMap.get(tagName);
                                        tagTotalPassedScenarioMap.put(tagName, currentTotalPassedScenario + 1);
                                        break;

                                    case FAILED:
                                        int currentTotalFailedScenario = tagTotalFailedScenarioMap.get(tagName);
                                        tagTotalFailedScenarioMap.put(tagName, currentTotalFailedScenario + 1);
                                        break;

                                    case SKIPPED:
                                        int currentTotalSkippedScenario = tagTotalSkippedScenarioMap.get(tagName);
                                        tagTotalSkippedScenarioMap.put(tagName, currentTotalSkippedScenario + 1);
                                        break;
                                }

                                int currentTotalScenario = tagTotalScenarioMap.get(tagName);
                                tagTotalScenarioMap.put(tagName, currentTotalScenario + 1);
                            }
                    );
                });

        return getFeatures().stream()
                .map(Feature::getScenarios)
                .flatMap(Collection::stream)
                .map(Scenario::getTagsName)
                .flatMap(Collection::stream)
                .distinct()
                .map(tagName -> new TagOverview(
                        tagName,
                        tagTotalScenarioMap.get(tagName),
                        tagTotalPassedScenarioMap.get(tagName),
                        tagTotalFailedScenarioMap.get(tagName),
                        tagTotalSkippedScenarioMap.get(tagName))
                )
                .collect(Collectors.toList());
    }

    public void createAttachmentFiles(String reportDestinationDirectory) throws IOException, MojoExecutionException {
        // TODO Currently only handles AfterStep, should be able to handle BeforeStep,
        //  BeforeScenario and AfterScenario in the future.

        Map<SupportedMIMEType, Integer> generatedFileCount = new HashMap<>();

        for (CucumberFeature cucumberFeature : cucumberFeatures)
            for (CucumberScenario cucumberScenario : cucumberFeature.getScenarios()) {
                List<CucumberHook> hooksWithEmbeddings = cucumberScenario.getSteps()
                        .stream()
                        .filter(step -> step.getAfter() != null)
                        .map(CucumberStep::getAfter)
                        .flatMap(Collection::stream)
                        .filter(Objects::nonNull)
                        .filter(hook -> hook.getEmbeddings() != null && hook.getEmbeddings().size() > 0)
                        .collect(Collectors.toList());

                for (CucumberHook hook : hooksWithEmbeddings) {
                    CucumberEmbedding embedding = hook.getFirstEmbedding();

                    SupportedMIMEType currentMIMEType = SupportedMIMEType.valueOfMIMEType(embedding.getMimeType());

                    if (!generatedFileCount.containsKey(currentMIMEType))
                        generatedFileCount.put(currentMIMEType, 1);

                    String baseDirectory = reportDestinationDirectory + FilePath.DEFAULT_REPORT_ATTACHMENTS_PATH;
                    String filename = "";

                    switch (currentMIMEType) {
                        case IMAGE_PNG:
                            filename = "image-" + generatedFileCount.get(currentMIMEType) + ".png";
                            String imagePath = baseDirectory + filename;
                            FileHelper.saveImage(hook.getFirstEmbedding().getData(), imagePath);
                            break;

                        case PLAIN_TEXT:
                            hook.setLogText(FileHelper.limitStringLength(hook.getFirstEmbedding().getData()));

                            filename = "log-" + generatedFileCount.get(currentMIMEType) + ".txt";
                            String textPath = baseDirectory + filename;
                            FileHelper.saveText(hook.getFirstEmbedding().getData(), textPath);
                            break;
                    }

                    hook.setFilename(FilePath.DEFAULT_REPORT_ATTACHMENTS_PATH + filename);
                    generatedFileCount.put(currentMIMEType, generatedFileCount.get(currentMIMEType) + 1);
                }
            }
    }

    public class Feature {
        private CucumberFeature cucumberFeature;

        public Feature(CucumberFeature cucumberFeature) {
            this.cucumberFeature = cucumberFeature;
        }

        public List<Scenario> getScenarios() {
            return cucumberFeature.getScenarios()
                    .stream()
                    .map(Scenario::new)
                    .collect(Collectors.toList());
        }

        public boolean isPassed() {
            return getTotalPassedScenarios() == getTotalScenarios();
        }

        public String getID() {
            return cucumberFeature.getId();
        }

        public String getName() {
            return cucumberFeature.getName();
        }

        public int getTotalScenarios() {
            return getScenarios().size();
        }

        public int getTotalPassedScenarios() {
            return getTotalScenariosBasedOnStatus(CucumberResultStatus.PASSED);
        }

        public int getTotalFailedScenarios() {
            return getTotalScenariosBasedOnStatus(CucumberResultStatus.FAILED);
        }

        public int getTotalSkippedScenarios() {
            return getTotalScenariosBasedOnStatus(CucumberResultStatus.SKIPPED);
        }

        public int getTotalScenariosBasedOnStatus(CucumberResultStatus status) {
            return (int) getScenarios().stream()
                    .map(Scenario::getStatus)
                    .filter(currentStatus -> currentStatus.equals(status))
                    .count();
        }

        public List<String> getTagsName() {
            return cucumberFeature.getTags().stream()
                    .map(CucumberTag::getName)
                    .collect(Collectors.toList());
        }

        public List<GroupedScenarios> getGroupedScenarios() {
            return getScenarios()
                    .stream()
                    .filter(Scenario::hasOutline)
                    .collect(Collectors.groupingBy(Scenario::getCommonID))
                    .values()
                    .stream()
                    .map(GroupedScenarios::new)
                    .collect(Collectors.toList());
        }

        public List<Scenario> getRegularScenarios() {
            return getScenarios()
                    .stream()
                    .filter(Scenario::doesNotHaveOutline)
                    .collect(Collectors.toList());
        }
    }

    public class TagOverview {
        private String name;
        private int totalPassedScenario;
        private int totalFailedScenario;
        private int totalSkippedScenario;
        private int totalScenario;

        public TagOverview(String name, int totalScenario, int totalPassedScenario, int totalFailedScenario, int totalSkippedScenario) {
            this.name = name;
            this.totalPassedScenario = totalPassedScenario;
            this.totalFailedScenario = totalFailedScenario;
            this.totalSkippedScenario = totalSkippedScenario;
            this.totalScenario = totalScenario;
        }

        public String getName() {
            return name;
        }

        private int getTotalPassedScenario() {
            return totalPassedScenario;
        }

        private int getTotalFailedScenario() {
            return totalFailedScenario;
        }

        private int getTotalSkippedScenario() {
            return totalSkippedScenario;
        }

        public int getPassedPercentage() {
            return totalPassedScenario * 100 / totalScenario;
        }

        public int getFailedPercentage() {
            return totalFailedScenario * 100 / totalScenario;
        }

        public int getSkippedPercentage() {
            return totalSkippedScenario * 100 / totalScenario;
        }
    }

    public class GroupedScenarios {
        private List<Scenario> scenarios;

        public GroupedScenarios(List<Scenario> scenarios) {
            this.scenarios = scenarios;
        }

        public CucumberResultStatus getStatus() {
            List<CucumberResultStatus> statusList = scenarios.stream()
                    .map(Scenario::getStatus)
                    .distinct()
                    .collect(Collectors.toList());

            for (CucumberResultStatus status : CucumberResultStatus.values())
                if (statusList.contains(status))
                    return status;

            // Defaulting the status to failed.
            return CucumberResultStatus.FAILED;
        }

        public List<String> getTagsName() {
            return scenarios.get(0).getTagsName();
        }

        public String getName() {
            return scenarios.get(0).getCommonID();
            // FIXME
        }

        public List<Scenario> getScenarios() {
            return scenarios;
        }
    }

    public class Scenario {
        private CucumberScenario cucumberScenario;

        public Scenario(CucumberScenario cucumberScenario) {
            this.cucumberScenario = cucumberScenario;
        }

        public List<Step> getSteps() {
            return cucumberScenario.getSteps()
                    .stream()
                    .map(Step::new)
                    .collect(Collectors.toList());
        }

        public boolean hasOutline() {
            return cucumberScenario.getKeyword().equals("Scenario Outline");
        }

        public boolean doesNotHaveOutline() {
            return !hasOutline();
        }

        public String getParameters() {
            // I know this regex looks horribly bad but removing characters outside quotation mark
            // does its job better because java 8 doesn't support global regex.
            String regex = "(?:[^\"](?=[^\"]*?(?:[\"][^\"]*?[\"][^\"]*?)+$|[^\"]*?$))*|(^[^\"]*[\"][^\"]*$)";

            return cucumberScenario.getName()
                    .replaceAll(regex, "")
                    .replace("\"\"", " | ")
                    .replace("\"", " ");
        }

        public String getCommonID() {
            String commonID = cucumberScenario.getId().split("[;]+")[1];
            return commonID
                    .replace("-", " ")
                    .replace("<", "")
                    .replace(">", "");
        }

        public String getName() {
            return cucumberScenario.getName();
        }

        public boolean isPassed() {
            return getStatus().equals(CucumberResultStatus.PASSED);
        }

        public CucumberResultStatus getStatus() {
            List<CucumberResultStatus> statusList = getSteps().stream()
                    .map(Step::getStatus)
                    .distinct()
                    .collect(Collectors.toList());

            for (CucumberResultStatus status : CucumberResultStatus.values())
                if (statusList.contains(status))
                    return status;

            // Defaulting the status to failed.
            return CucumberResultStatus.FAILED;
        }

        public List<String> getTagsName() {
            return cucumberScenario.getTags().stream()
                    .map(CucumberTag::getName)
                    .collect(Collectors.toList());
        }

        public Timestamp getScenarioStartTime() {
            return cucumberScenario.getStartTimestamp();
        }

        public Timestamp getScenarioEndTime() {
            int A_MILLION = (int) Math.pow(10, 6);

            Timestamp startTime = getScenarioStartTime();

            Long duration = 0L;

            if (hasBefore())
                duration += getBeforeDuration();

            duration += getSteps().stream()
                    .map(Step::getStepDuration)
                    .mapToLong(Long::longValue)
                    .sum();

            if (hasAfter())
                duration += getAfterDuration();

            // Timestamp stores as millis.
            Long durationInMillis = duration / A_MILLION;
            return new Timestamp(startTime.getTime() + durationInMillis);
        }

        public boolean hasBefore() {
            return cucumberScenario.getBefore() != null && cucumberScenario.getBefore().size() > 0;
        }

        private Long getBeforeDuration() {
            return cucumberScenario.getBefore()
                    .stream()
                    .map(CucumberHook::getResult)
                    .map(CucumberResult::getDuration)
                    .filter(Objects::nonNull)
                    .mapToLong(Long::longValue)
                    .sum();
        }

        public boolean hasAfter() {
            return cucumberScenario.getAfter() != null && cucumberScenario.getAfter().size() > 0;
        }

        private Long getAfterDuration() {
            return cucumberScenario.getAfter()
                    .stream()
                    .map(CucumberHook::getResult)
                    .map(CucumberResult::getDuration)
                    .filter(Objects::nonNull)
                    .mapToLong(Long::longValue)
                    .sum();
        }
    }

    public class Step {
        private CucumberStep cucumberStep;

        public Step(CucumberStep cucumberStep) {
            this.cucumberStep = cucumberStep;
        }

        public String getName() {
            return cucumberStep.getName();
        }

        public CucumberResultStatus getStatus() {
            return CucumberResultStatus.valueOf(cucumberStep.getResult().getStatus().toUpperCase());
        }

        public String getErrorMessage() {
            return cucumberStep.getResult().getErrorMessage();
        }

        public Long getStepDuration() {
            Long duration = 0L;

            if (hasBefore())
                duration += getBeforeDuration();

            if (hasResultDuration())
                duration += cucumberStep.getResult().getDuration();

            if (hasAfter())
                duration += getAfterDuration();

            return duration;
        }

        public List<Hook> getAfter() {
            return cucumberStep.getAfter()
                    .stream()
                    .map(Hook::new)
                    .collect(Collectors.toList());
        }

        public boolean hasBefore() {
            return cucumberStep.getBefore() != null && cucumberStep.getBefore().size() > 0;
        }

        private Long getBeforeDuration() {
            return cucumberStep.getBefore()
                    .stream()
                    .map(CucumberHook::getResult)
                    .map(CucumberResult::getDuration)
                    .filter(Objects::nonNull)
                    .mapToLong(Long::longValue)
                    .sum();
        }

        public boolean hasAfter() {
            return cucumberStep.getAfter() != null && cucumberStep.getAfter().size() > 0;
        }

        private Long getAfterDuration() {
            return cucumberStep.getAfter()
                    .stream()
                    .map(CucumberHook::getResult)
                    .map(CucumberResult::getDuration)
                    .filter(Objects::nonNull)
                    .mapToLong(Long::longValue)
                    .sum();
        }

        private boolean hasResultDuration() {
            return cucumberStep.getResult().getDuration() != null;
        }
    }

    public class Hook {
        private CucumberHook cucumberHook;

        public Hook(CucumberHook cucumberHook) {
            this.cucumberHook = cucumberHook;
        }

        public String getLogText() {
            return cucumberHook.getLogText();
        }

        public String getFilename() {
            return cucumberHook.getFilename();
        }
    }
}

