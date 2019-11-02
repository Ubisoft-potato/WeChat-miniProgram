FROM java8
VOLUME /home/wxpicture
ADD photoShare-1.0.0-SNAPSHOT.jar /app/photoShare.jar
ENV JAVA_OPTS "-XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/tmp/java_pid%p.hprof -XX:+PrintGCDetails -Djava.security.egd=file:/dev/./urandom"
ENTRYPOINT ["sh" , "-c", "java  $JAVA_OPTS -Dspring.profiles.active=$ACTIVE_ENV -jar /app/photoShare.jar" ]
EXPOSE 8080
