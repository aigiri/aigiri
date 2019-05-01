#ROM openjdk:8-jre-alpine
FROM openjdk:10-slim

# Add a volume pointing to /tmp
VOLUME /tmp

# Make port 8080 available to the world outside this container
EXPOSE 8080
EXPOSE 8443

# The application's jar file
ARG JAR_FILE=kutumbini.jar

# Add the application's jar to the container
ADD ${JAR_FILE} kutumbini.jar

# Run the jar file 
ENTRYPOINT ["java", "-Xms1024m", "-Xmx1024m", "-jar", "/kutumbini.jar"]
