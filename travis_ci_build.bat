choco install adoptopenjdk8 -y --force
call refreshenv
REM use short path to avoid dealing with spaces.
pushd %JAVA_HOME%
 for %%I in (.) do set JAVA_HOME=%%~sI
popd

java -version
call mvnw clean install -B -V
