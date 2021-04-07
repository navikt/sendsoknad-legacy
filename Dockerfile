FROM navikt/java:8-appdynamics
ENV APPD_ENABLED=true
WORKDIR .
RUN ls -la /
RUN ls -la /app
RUN ls -la /home
RUN pwd
#RUN ls -la web/target
ADD web/target/sendsoknad-web-*.war /app
RUN ls -la /app
COPY init-scripts /init-scripts
ENV JAVA_OPTS="-Xmx3072m"
ENV MAIN_CLASS="no.nav.sbl.dialogarena.rest.SoknadApplication"
