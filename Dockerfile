FROM tomcat:9-jre11
RUN rm -r /usr/local/tomcat/webapps/ROOT
COPY target/whakaoko-0.0.1-SNAPSHOT.war /usr/local/tomcat/webapps/ROOT.war
COPY catalina.properties /usr/local/tomcat/conf/catalina.properties
COPY certs/gdroot-g2_cross.crt gdroot-g2_cross.crt
RUN /usr/bin/keytool -import -alias gdroor-g2_cross -keystore /usr/lib/jvm/java-11-openjdk-amd64/lib/security/cacerts -file gdroot-g2_cross.crt -noprompt -storepass changeit
RUN /usr/bin/keytool -import -alias gdidg2_cross -keystore /usr/lib/jvm/java-11-openjdk-amd64/lib/security/cacerts -file gdig2.crt -noprompt -storepass changeit

