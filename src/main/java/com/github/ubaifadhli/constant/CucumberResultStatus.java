package com.github.ubaifadhli.constant;

public enum CucumberResultStatus {
    FAILED("failed"),
    SKIPPED("skipped"),
    PASSED("passed");

    final String name;

    CucumberResultStatus(String name) {
        this.name = name;
    }

    public String getSomething() {
        return this.name;
    }
}
