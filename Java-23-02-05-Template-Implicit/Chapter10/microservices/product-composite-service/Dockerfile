# TH: specifies parent image of our custom image.
FROM openjdk:12.0.2

# TH: helps Docker listen on given port at runtime (i.e. TCP by default).
EXPOSE 8080

# TH: copies files from <src> to <dst>.
ADD ./build/libs/*.jar app.jar

# TH: helps configure container that will run as executable.
ENTRYPOINT ["java","-jar","/app.jar"]
