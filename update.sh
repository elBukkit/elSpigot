#!/bin/bash
rm -Rf Build
mkdir Build
cd Build
curl https://hub.spigotmc.org/jenkins/job/BuildTools/lastSuccessfulBuild/artifact/target/BuildTools.jar > BuildTools.jar
export MAVEN_OPTS="-Xmx2G"
java -Xmx2G -jar BuildTools.jar
cd Spigot/Spigot-Server
git apply ../../../patches/*
/usr/local/maven/bin/mvn clean
/usr/local/maven/bin/mvn install
echo PATCH COMPLETE!
