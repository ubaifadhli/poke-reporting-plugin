# poke-reporting-plugin

A simple Maven plugin to generate test report from Cucumber JSON, especially one that contains Pokemon stuff.

Created to (hopefully) improve my flexibility to display things i want to see in my Test Report.

Built mainly using Jackson and Pebble Templates, published on JitPack.

You can see the report example [here](https://poke-reporting.bitbucket.io/).

![I read too many Hacker News comments.](https://camo.githubusercontent.com/d8a50bd3d4108524d8155b743764d4c05d41372aa4230978a0054c3b160b51f3/68747470733a2f2f696d672e736869656c64732e696f2f62616467652f646f6c7068696e732d736176652d626c75652e7376673f7374796c653d666c6174)

## Prerequisites
- Your testing project uses Cucumber and generates JSON as an output


## Usage Guide
Add this snippet into your `pom.xml` file. `<pluginRepositories>` tag is needed because I use JitPack to publish this plugin.
```
    <pluginRepositories>
        <pluginRepository>
            <id>jitpack.io</id>
            <url>https://jitpack.io</url>
        </pluginRepository>
    </pluginRepositories>

    <build>
        <plugins>
            <plugin>
                <groupId>com.github.ubaifadhli</groupId>
                <artifactId>poke-reporting-plugin</artifactId>
                <version>1.3.2</version>
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

Because the plugin will be executed in `post-integration-test` phase, you can run your project using `mvn clean verify` command.

## File Path Information
| Path | Description | 
| ----------- | ----------- |
| target/cucumber-report/cucumber.json | Path where my plugin expects the Cucumber output will be |
| target/poke-report | Path where the report would be generated |

