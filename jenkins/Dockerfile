FROM jenkins/jenkins:lts

USER root

# Install Maven
RUN apt-get update && \
    apt-get install -y maven && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# Verify installations
RUN java -version && mvn -version

USER jenkins
