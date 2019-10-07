FROM java8
VOLUME /home/wxpicture
ADD photoShare-1.0-SNAPSHOT.jar /app/photoShare.jar
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app/photoShare.jar"]
EXPOSE 8080
