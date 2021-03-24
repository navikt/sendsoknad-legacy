FROM navikt/java:8-appdynamics
ENV APPD_ENABLED=true
COPY web/target/sendsoknad-web-* /app
COPY init-scripts /init-scripts
ENV JAVA_OPTS="-Xmx8192m"
ENV MAIN_CLASS="no.nav.sbl.dialogarena.rest.SoknadApplication"
