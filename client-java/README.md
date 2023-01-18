# Client Java

*Note: For general information about the repository please refer to the top level [README.md](../README.md).*

The purpose of this directory is to interpret the GameState sent by the [server](../server-python/) via `127.0.0.1:5001` and convert it to an iv4xr environment. Goals can be created using the iv4xr environment and can be resolved using self defined tactics. For specific information on iv4xr/aplib please refer to [their GitHub](https://github.com/iv4xr-project/aplib).

*Note: Code in iv4xr, especially MiniDungeon, is used as an inspiration source.*

## Platform

This Maven project should work on any OS, it has been tested on Ubuntu and Windows.

*Note: the entry points for this project are [App.java](src/main/java/App.java) and [Sandbox.java](src/main/java/nethack/agent/Sandbox.java).*

## IntelliJ IDEA

The project depends on aplib, in Eclipse this dependency is resolved when importing the module into the workspace. For IntelliJ the aplib needs to be built using f.e. maven install on the pom.xml file in the iv4xr-project. Only then the jar file will be created.

To use the log4j2.xml file for logging, configure the maven run with the vm option: `-Dlog4j2.configurationFile=log4j2.xml` as described [here](https://intellij-support.jetbrains.com/hc/zh-cn/community/posts/4419070922386-log4j2-not-finding-my-external-file-when-run-from-intelliJ).

## Socket connection code

Source code has been inspired by japyre code for socket connection which can be found [here](https://github.com/iv4xr-project/japyre).

The package contains code to handle a socket-based connection.
