#!/bin/bash
rm -Rf Build
mkdir Build
cd Build
curl https://hub.spigotmc.org/jenkins/job/BuildTools/lastSuccessfulBuild/artifact/target/BuildTools.jar > BuildTools.jar
export MAVEN_OPTS="-Xmx2G"
java -Xmx2G -jar BuildTools.jar
cd Spigot
cp ../../src/CraftMetaItem.java Spigot-Server/src/main/java/org/bukkit/craftbukkit/inventory/.
cp ../../src/CraftMetaItemData.java Spigot-Server/src/main/java/org/bukkit/craftbukkit/inventory/.
cp ../../src/ItemMeta.java Spigot-API/src/main/java/org/bukkit/inventory/meta/.
cp ../../src/CraftMetaSkull.java Spigot-Server/src/main/java/org/bukkit/craftbukkit/inventory/.
/usr/local/maven/bin/mvn clean
/usr/local/maven/bin/mvn install
echo PATCH COMPLETE!
