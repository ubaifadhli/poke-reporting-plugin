# poke-reporting-plugin

A simple Maven plugin to generate test report from Cucumber JSON, especially one that contains Pokemon stuff.

Created to (hopefully) improve my flexibility on displaying things i want to see in my Test Report.

Mostly built using Jackson and Pebble Templates.

You can see the report example [here](https://poke-reporting.bitbucket.io/).

![I read too many Hacker News comments.](https://camo.githubusercontent.com/d8a50bd3d4108524d8155b743764d4c05d41372aa4230978a0054c3b160b51f3/68747470733a2f2f696d672e736869656c64732e696f2f62616467652f646f6c7068696e732d736176652d626c75652e7376673f7374796c653d666c6174)

## Prerequisites
- Your testing project uses Cucumber and generates JSON as an output
- You have GitHub account
- You can bear the pain of using unfinished project (such as this one)


## Usage Guide
Create `settings.xml` file in `~/.m2/` directory and copy the snippet below. If you already have one, adjust accordingly. 
```
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0
                      http://maven.apache.org/xsd/settings-1.0.0.xsd">

  <activeProfiles>
    <activeProfile>github</activeProfile>
  </activeProfiles>

  <profiles>
    <profile>
      <id>github</id>
      <pluginRepositories>
        <pluginRepository>
          <id>github</id>
          <url>https://maven.pkg.github.com/ubaifadhli/poke-reporting-plugin</url>
        </pluginRepository>
      </pluginRepositories>
    </profile>
  </profiles>

  <servers>
    <server>
      <id>github</id>
      <username>YOUR_GITHUB_USERNAME</username>
      <password>YOUR_PERSONAL_ACCESS_TOKEN</password>
    </server>
  </servers>
</settings>
```
Note that the snippet uses `pluginRepositories` instead of the usual `repositories` because this project would be used as a Maven plugin.
Also paste your GitHub username and PAT there. Your GitHub PAT also needs to have `read:packages` scope access (more about this [here](https://docs.github.com/en/packages/learn-github-packages/about-permissions-for-github-packages#about-scopes-and-permissions-for-package-registries)). 

After that, include the plugin into your project by adding this snippet into your `pom.xml` file.
```
<build>
        <plugins>
            <plugin>
                <groupId>org.ubaifadhli.future</groupId>
                <artifactId>poke-reporting-plugin</artifactId>
                <version>1.1</version>
                <executions>
                    <execution>
                        <id>report</id>
                        <phase>post-integration-test</phase>
                        <goals>
                            <goal>reporting</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
```

You're pretty much done. Congratulations!

## Why do I have to provide my GitHub credentials?
I want to ask the same thing, too. 

In short, access to read packages are currently limited to GitHub users. So even though it's someone else's package, you still need to provide your GitHub credentials to access it.

I'm just summarizing information I read from this [StackOverflow answer](https://stackoverflow.com/a/67776304), maybe you want to check that out instead.

## File Path Information
| Path | Description | 
| ----------- | ----------- |
| target/cucumber-report/cucumber.json | Path where my plugin expects the Cucumber output will be |
| target/poke-report | Path where the report would be generated |


