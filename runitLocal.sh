#!/bin/bash

mvn clean package

inputfile=$1
outputfile=$2

# Run with classpath including dependencies in lib/ directory
java -jar target/searchable-pdf-1.0.jar "$inputfile" "$outputfile"