FROM openjdk:10-jre
COPY target/whakaoko-0.0.1-SNAPSHOT.jar /opt/whakaoko/whakaoko-0.0.1-SNAPSHOT.jar

COPY certs/gdig2.crt gdig2.crt
RUN /usr/bin/keytool -import -alias gdig2 -keystore /usr/lib/jvm/java-10-openjdk-amd64/lib/security/cacerts -file gdig2.crt -noprompt -storepass changeit

CMD ["cat", "/opt/whakaoko/conf/whakaoko.properties"]
