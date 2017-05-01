mvn clean install & java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005  -Xms1024M -Xmx35072M -jar target/cimt-keyphrase-extraction-0.0.1-SNAPSHOT.jar 
