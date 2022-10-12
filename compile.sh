#!/usr/bin/bash -ve

javac="javac -d bin -cp bin:phase2.jar"
java="java -cp bin:phase2.jar"


#check1302 src
mvn compile
mvn exec:java -Dprism.order=sw -Dexec.mainClass="cs1302.gallery.GalleryDriver"
