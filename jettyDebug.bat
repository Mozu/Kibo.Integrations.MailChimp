set GRADLE_OPTS=-Xdebug -Xrunjdwp:transport=dt_socket,address=9999,server=y,suspend=n
gradlew jettyRun
# gradlew jettyRun -Dhttps.proxyHost=localhost -Dhttps.proxyPort=8888