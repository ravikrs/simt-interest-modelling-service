mvn clean install -Dmaven.test.skip=true & java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005  -Xms1024M -Xmx35072M -jar target/cimt-keyphrase-extraction-0.0.1-SNAPSHOT.jar 
