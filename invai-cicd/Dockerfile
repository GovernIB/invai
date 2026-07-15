FROM dependencia-registry.plexus.services/jboss-eap-7-2:latest

RUN rm -rf standalone/deployments/*

COPY invai-ear/target/invai.ear standalone/deployments/

COPY scripts/configuracio/invai.system.properties standalone/configuration/invai.system.properties

COPY scripts/configuracio/invai.properties standalone/configuration/invai.properties

COPY scripts/configuracio/standalone.xml standalone/configuration/

COPY scripts/modules/system/layers/base/org/keycloak modules/system/layers/base/org/keycloak

COPY scripts/modules/system/layers/base/com/oracle modules/system/layers/base/com/oracle