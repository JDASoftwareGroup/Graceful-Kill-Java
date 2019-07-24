@echo off
REM use short path to avoid dealing with spaces.
pushd %JAVA_HOME%
 for %%I in (.) do set JAVA_HOME=%%~sI
popd

%JAVA_HOME%\bin\java -cp "target\test-classes;target\*" sampleapp.SampleApplicationWithShutdownLogic
