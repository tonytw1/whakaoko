FROM tomcat
RUN rm -r /usr/local/tomcat/webapps/ROOT
COPY target/whakaoko-0.0.1-SNAPSHOT.war /usr/local/tomcat/webapps/ROOT.war
COPY catalina.properties /usr/local/tomcat/conf/catalina.properties
