FROM openjdk:11-jre
COPY target/whakaoko-0.0.1-SNAPSHOT.jar /opt/whakaoko/whakaoko-0.0.1-SNAPSHOT.jar

COPY certs/gdig2.crt gdig2.crt
RUN /usr/local/openjdk-11/bin/keytool -import -alias gdig2 -keystore /usr/local/openjdk-11/lib/security/cacerts -file gdig2.crt -noprompt -storepass changeit

CMD ["java", "-XshowSettings:vm", "-XX:+PrintCommandLineFlags", "-XX:MaxRAMPercentage=75","-jar","/opt/whakaoko/whakaoko-0.0.1-SNAPSHOT.jar", "--spring.config.location=/opt/whakaoko/conf/whakaoko.properties"]
