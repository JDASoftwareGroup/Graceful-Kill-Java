choco install adoptopenjdk8 -y --force
call refreshenv
java -version
call mvnw clean install -B -V
