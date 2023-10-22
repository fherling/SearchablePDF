#!/bin/bash

mvn clean install


inputfile=$1
outputfile=$2
java -cp target/searchable-pdf-1.0.jar OcrRunner $inputfile $outputfile