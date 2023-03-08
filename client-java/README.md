# Client Java

*Note: For general information about the repository please refer to the top level [README.md](../README.md).*

The purpose of this directory is to interpret the GameState sent by the [server](../server-python) via `127.0.0.1:5001` and convert it to an iv4XR environment. Goals can be created using the iv4XR environment and can be resolved using self defined tactics. For specific information on iv4XR/aplib please refer to [their GitHub](https://github.com/iv4xr-project/aplib).

*Note: Code in iv4XR, especially MiniDungeon, is used as an inspiration source.*

## Platform

This Maven project should work on any OS, it has been tested on Ubuntu and Windows.

*Note: the entry points for this project are [nethack.App.java](src/main/java/nethack/App.java) and [agent.App.java](src/main/java/agent/App.java).*

## IntelliJ IDEA

The project depends on aplib, in Eclipse this dependency is resolved when importing the module into the workspace. For IntelliJ the aplib needs to be built using f.e. maven install on the pom.xml file in the iv4xr-project. Only then the jar file will be created.

## Socket connection code

Source code has been inspired by Japyre code for socket connection which can be found [here](https://github.com/iv4xr-project/japyre).

The package contains code to handle a socket-based connection.

## Configurations

The configuration file used during normal operation is located [here](src/main/resources/config.properties).
The configurations that are possible are as follows:

| Field name       | Default value                 | Description                                                                                               |
| ---------------- | ----------------------------- |-----------------------------------------------------------------------------------------------------------|
| IP               | 127.0.0.1                     | IP address of the python server                                                                           |
| PORT             | 5001                          | Port to connect to on the server                                                                          |
| SEED             | -                             | Seed to use for the game run, if empty the seed is random. Pattern expected are two comma separated longs |
| START_TURN       | 1, 0                          | If set, automatically forwards to the given turn. The pattern is the same as the SEED field               |
| COLLECT_COVERAGE | false                         | Whether coverage of the run should be gathered                                                            |
| SUMMARY_TYPE     | json                          | The argument given to coverage collection script. Can be json or html                                     |
| SOUND            | true                          | Whether sound is on                                                                                   |
| LOG_CONFIG       | src/main/resources/log4j2.xml | The log4j2 log configuration file location                                                                |
