# Ping Access Patch Tool CLI

## How to build executable jar

```
$ cd root_of_git_repo
$ mvn clean package assembly:single
```

## How to run executable jar

```
$ cd root_of_git_repo
$ java -jar target/patch-tool-1.0-SNAPSHOT-jar-with-dependencies.jar
```

## Commandline parameters

-dryRun          - will do all code but without PUT operation
-limitCount  NR  - will do PUT operations limited just on this number of applications (default is unlimited)

## Run with an external configuration file

To execute the application with an external configuration file you should set the env variable CONFIG_PROPERTIES
with path to config.properties file.

## Important

The application will verify that the RuleSet has been already applied and report this.


